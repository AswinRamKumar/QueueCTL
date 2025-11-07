package com.queuectl.engine;

import com.queuectl.db.models.Job;
import com.queuectl.db.repository.DBConnection;
import com.queuectl.db.repository.JobStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.UUID;

public class WorkerThreadTest {

    private JobStore store;

    @BeforeEach
    public void setup() {
        com.queuectl.util.TestDBBootstrap.ensureSchema();
        store = new JobStore();
    }

    @Test
    public void testRetryAndBackoff() throws Exception {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        Job job = new Job(0L, id, "invalidcmd", "pending", 0, 3, now, now, now, null);
        store.insert(job);

        WorkerThread worker = new WorkerThread();

        // Simulate 3 consecutive failures
        workerTestFailure(worker, job);
        workerTestFailure(worker, job);
        workerTestFailure(worker, job);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT state FROM jobs WHERE id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Assertions.assertEquals("dead", rs.getString("state"));
            }
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS cnt FROM dlq WHERE id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Assertions.assertTrue(rs.getInt("cnt") > 0);
            }
        }

        // Clean up DB rows for repeatable tests
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM dlq WHERE id='" + id + "'").executeUpdate();
            con.prepareStatement("DELETE FROM jobs WHERE id='" + id + "'").executeUpdate();
        }
    }

    private void workerTestFailure(WorkerThread worker, Job job) {
        try {
            // Access protected method logic via reflection : simulate failure
            var m = WorkerThread.class.getDeclaredMethod("handleFailure", Job.class, String.class);
            m.setAccessible(true);
            m.invoke(worker, job, "Simulated failure");
            job.setAttempts(job.getAttempts() + 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
