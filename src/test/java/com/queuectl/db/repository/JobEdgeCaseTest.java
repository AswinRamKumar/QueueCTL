package com.queuectl.db.repository;

import com.queuectl.db.models.Job;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobEdgeCaseTest {

    private static JobStore store;

    @BeforeAll
    public static void init() {
        com.queuectl.util.TestDBBootstrap.ensureSchema();
        store = new JobStore();
    }

    @Test
    @Order(1)
    public void testDuplicateIdInsert() throws Exception {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        Job job1 = new Job(0L, id, "echo one", "pending", 0, 3, now, now, now, null);
        Job job2 = new Job(0L, id, "echo duplicate", "pending", 0, 3, now, now, now, null);
        store.insert(job1);
        Assertions.assertThrows(Exception.class, () -> store.insert(job2));
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM jobs WHERE id='" + id + "'").executeUpdate();
        }
    }

    @Test
    @Order(2)
    public void testInvalidCommandHandledGracefully() {
        String id = UUID.randomUUID().toString();
        Job job = new Job(0L, id, "invalidcmd", "pending", 0, 2,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        store.insert(job);
        try {
            com.queuectl.engine.WorkerThread w = new com.queuectl.engine.WorkerThread();
            var m = com.queuectl.engine.WorkerThread.class.getDeclaredMethod("processJob", com.queuectl.db.models.Job.class);
            m.setAccessible(true);
            m.invoke(w, job);
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage() == null || e.getMessage().contains("invalidcmd"));
        } finally {
            try (Connection con = DBConnection.getConnection()) {
                con.prepareStatement("DELETE FROM jobs WHERE id='" + id + "'").executeUpdate();
            } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(3)
    public void testPersistenceAcrossRestart() throws Exception {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        Job job = new Job(0L, id, "echo persist", "pending", 0, 3, now, now, now, null);
        store.insert(job);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS cnt FROM jobs WHERE id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            Assertions.assertTrue(rs.next() && rs.getInt("cnt") == 1);
        }
        //restarts by creating a new store
        JobStore newStore = new JobStore();
        boolean stillThere = newStore.getPendingJobs().stream().anyMatch(j -> j.getId().equals(id));
        Assertions.assertTrue(stillThere, "Job should survive restart (DB persistence)");
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM jobs WHERE id='" + id + "'").executeUpdate();
        }
    }
}
