package com.queuectl.integartion;

import com.queuectl.db.models.Job;
import com.queuectl.db.repository.DBConnection;
import com.queuectl.db.repository.JobStore;
import com.queuectl.engine.WorkerThread;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueuectlIntegrationTest {

    private static JobStore store;
    private static String jobId;

    @BeforeAll
    public static void setup() {
        com.queuectl.util.TestDBBootstrap.ensureSchema();
        store = new JobStore();
    }

    @Test
    @Order(1)
    public void testFullLifecycle() throws Exception {
        jobId = UUID.randomUUID().toString();
        Job job = new Job(0L, jobId, "invalidcmd", "pending", 0, 2,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        store.insert(job);

        WorkerThread worker = new WorkerThread();

        // Simulate the worker running until job reaches DLQ
        for (int i = 0; i < 3; i++) {
            var m = WorkerThread.class.getDeclaredMethod("processJob", com.queuectl.db.models.Job.class);
            m.setAccessible(true);
            m.invoke(worker, job);
            job.setAttempts(job.getAttempts() + 1);
        }

        // Verify DLQ entry exists
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS cnt FROM dlq WHERE id=?")) {
            ps.setString(1, jobId);
            ResultSet rs = ps.executeQuery();
            Assertions.assertTrue(rs.next() && rs.getInt("cnt") > 0, "Job should move to DLQ");
        }

        // Verify job state is 'dead'
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT state FROM jobs WHERE id=?")) {
            ps.setString(1, jobId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Assertions.assertEquals("dead", rs.getString("state"));
            }
        }
    }

    @Test
    @Order(2)
    public void testStatusQueryReflectsChanges() throws Exception {
        String sqlJobs = "SELECT state, COUNT(*) AS cnt FROM jobs GROUP BY state";
        String sqlDlq = "SELECT COUNT(*) AS cnt FROM dlq";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery(sqlJobs);
            boolean foundDead = false;
            while (rs.next()) {
                if ("dead".equals(rs.getString("state"))) {
                    foundDead = true;
                    break;
                }
            }
            Assertions.assertTrue(foundDead, "Status query should reflect dead jobs");

            rs = st.executeQuery(sqlDlq);
            if (rs.next()) {
                Assertions.assertTrue(rs.getInt("cnt") > 0, "DLQ count should be > 0");
            }
        }
    }

    @AfterAll
    public static void cleanup() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM dlq WHERE id='" + jobId + "'").executeUpdate();
            con.prepareStatement("DELETE FROM jobs WHERE id='" + jobId + "'").executeUpdate();
        }
    }
}
