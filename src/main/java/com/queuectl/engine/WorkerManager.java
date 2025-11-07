package com.queuectl.engine;

import java.util.ArrayList;
import java.util.List;

public class WorkerManager {

    private final List<WorkerThread> workers=new ArrayList<>();

    public void startWorkers(int count){
        for(int i=0;i<count;i++){
            WorkerThread w=new WorkerThread();
            w.setName("Worker-"+(i+1));
            workers.add(w);
            w.start();
        }
        System.out.println(count+" workers started.");
    }

    public void stopAll(){
        for(WorkerThread w:workers){
            w.stopWorker();
        }
        System.out.println("All workers stopped gracefully.");
    }
    public void resumePendingJobs() {
        try (java.sql.Connection con = com.queuectl.db.repository.DBConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(
                     "UPDATE jobs SET state='pending', updated_at=NOW() " +
                             "WHERE state IN ('processing','failed') AND next_run_at<=NOW()")) {
            int updated = ps.executeUpdate();
            if (updated > 0) System.out.println("Resumed " + updated + " job(s) for processing.");
        } catch (Exception e) {
            System.err.println("Error resuming jobs: " + e.getMessage());
        }
    }

}

