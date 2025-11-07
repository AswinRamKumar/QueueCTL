package com.queuectl.engine;

import com.queuectl.db.models.Job;
import com.queuectl.db.repository.DBConnection;
import com.queuectl.db.repository.JobStore;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiWorkerTest {

    private static JobStore store;

    @BeforeAll
    public static void setup() {
        com.queuectl.util.TestDBBootstrap.ensureSchema();
        store = new JobStore();
    }

    @Test
    @Order(1)
    public void testMultipleWorkersNoOverlap() throws Exception {
        List<String> ids = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 4; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            store.insert(new Job(0L, id, "echo job_" + i, "pending", 0, 3, now, now, now, null));
        }

        WorkerManager wm = new WorkerManager();
        wm.startWorkers(2);
        Thread.sleep(6000);
        wm.stopAll();

        int completed = 0;
        Set<String> seen = new HashSet<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id,state FROM jobs WHERE id IN (" +
                     String.join(",", ids.stream().map(x -> "'" + x + "'").toList()) + ")")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String state = rs.getString("state");
                if (seen.contains(id))
                    Assertions.fail("Duplicate job processed: " + id);
                seen.add(id);
                Assertions.assertEquals("completed", state, "Job not completed: " + id);
                completed++;
            }
        }

        Assertions.assertEquals(4, completed, "All jobs should complete without duplication");

        try (Connection con = DBConnection.getConnection()) {
            for (String id : ids) {
                con.prepareStatement("DELETE FROM jobs WHERE id='" + id + "'").executeUpdate();
            }
        }
    }
}
