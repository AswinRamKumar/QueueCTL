package com.queuectl.db.repository;

import com.queuectl.db.models.Job;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JobStoreTest {

    @Test
    public void testInsertAndFetchPending() throws Exception {
        String id=UUID.randomUUID().toString();
        LocalDateTime now=LocalDateTime.now();
        Job job=new Job(0L,id,"echo test","pending",0,3,now,now,now,null);

        JobStore store=new JobStore();
        store.insert(job);

        List<Job> pending=store.getPendingJobs();
        boolean found=pending.stream().anyMatch(j->j.getId().equals(id));
        Assertions.assertTrue(found,"Inserted job should be visible in pending jobs");

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement("DELETE FROM jobs WHERE id=?")){
            ps.setString(1,id);
            ps.executeUpdate();
        }
    }
}
