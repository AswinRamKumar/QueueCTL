package com.queuectl.engine;

import com.queuectl.db.models.Job;
import com.queuectl.db.repository.DBConnection;
import com.queuectl.db.repository.JobStore;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobSuccessTest {

    private static JobStore store;
    private static String jobId;

    @BeforeAll
    public static void setup() {
        com.queuectl.util.TestDBBootstrap.ensureSchema();
        store = new JobStore();
    }

    @Test
    @Order(1)
    public void testJobCompletesSuccessfully() throws Exception {
        jobId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        Job job = new Job(0L, jobId, "echo success", "pending", 0, 3, now, now, now, null);
        store.insert(job);

        WorkerThread worker = new WorkerThread();
        var m = WorkerThread.class.getDeclaredMethod("processJob", com.queuectl.db.models.Job.class);
        m.setAccessible(true);
        m.invoke(worker, job);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT state FROM jobs WHERE id=?")) {
            ps.setString(1, jobId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Assertions.assertEquals("completed", rs.getString("state"), "Job should complete successfully");
            }
        }

        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM jobs WHERE id='" + jobId + "'").executeUpdate();
        }
    }
}
