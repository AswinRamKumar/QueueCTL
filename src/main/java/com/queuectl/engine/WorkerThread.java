package com.queuectl.engine;

import com.queuectl.db.models.Job;
import com.queuectl.db.repository.DBConnection;
import com.queuectl.db.repository.JobStore;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;

public class WorkerThread extends Thread {

    private final JobStore store=new JobStore();
    private volatile boolean running=true;

    @Override
    public void run() {
        System.out.println("Worker " + Thread.currentThread().getName() + " started.");
        while (running) {
            Job job = getNextPendingJob();
            if (job == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }
            processJob(job);
        }
        System.out.println("Worker " + Thread.currentThread().getName() + " stopped.");
    }




    private Job getNextPendingJob() {
        String select = "SELECT seq,id,command,state,attempts,max_retries,created_at,updated_at,next_run_at,last_error "
                + "FROM jobs "
                + "WHERE state='pending' AND next_run_at<=NOW() "
                + "ORDER BY seq ASC LIMIT 1 FOR UPDATE SKIP LOCKED";
        String update = "UPDATE jobs SET state='processing', updated_at=? WHERE id=?";
        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    con.commit();
                    return null;
                }

                Job job = new Job(
                        rs.getLong("seq"),
                        rs.getString("id"),
                        rs.getString("command"),
                        rs.getString("state"),
                        rs.getInt("attempts"),
                        rs.getInt("max_retries"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime(),
                        rs.getTimestamp("next_run_at").toLocalDateTime(),
                        rs.getString("last_error")
                );

                try (PreparedStatement upd = con.prepareStatement(update)) {
                    upd.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    upd.setString(2, job.getId());
                    upd.executeUpdate();
                }

                con.commit();
                return job;
            }

        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            System.err.println("Error claiming job: " + e.getMessage());
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ignored) {}
        }
        return null;
    }


    private void processJob(Job job){
        try{
            System.out.println("Running: "+job.getCommand());
            ProcessBuilder pb=new ProcessBuilder("cmd.exe","/c",job.getCommand());
            pb.redirectErrorStream(true);
            Process proc=pb.start();
            BufferedReader reader=new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while((line=reader.readLine())!=null) System.out.println(line);
            int exit=proc.waitFor();
            if(exit==0){
                store.updateState(job.getId(),"completed");
                System.out.println("Job "+job.getId()+" completed successfully.");
            }else{
                handleFailure(job,"Exited with code "+exit);
            }
        }catch(Exception e){handleFailure(job,e.getMessage());}
    }

    private void handleFailure(Job job, String errorMsg) {
        try {
            int attempts = job.getAttempts() + 1;
            job.setAttempts(attempts);
            job.setLastError(errorMsg);
            job.setUpdatedAt(LocalDateTime.now());

            JobStore store = new JobStore();

            if (attempts >= job.getMaxRetries()) {
                store.markDead(job.getId(), attempts, errorMsg);

                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO dlq (id, command, attempts, last_error, created_at) VALUES (?, ?, ?, ?, NOW())")) {
                    ps.setString(1, job.getId());
                    ps.setString(2, job.getCommand());
                    ps.setInt(3, attempts);
                    ps.setString(4, errorMsg);
                    ps.executeUpdate();
                }

                System.out.println("Job " + job.getId() + " moved to DLQ after max retries.");
            } else {
                long backoff = (long) Math.pow(2, attempts);
                LocalDateTime next = LocalDateTime.now().plusSeconds(backoff);
                job.setNextRunAt(next);
                job.setState("pending");
                store.updateFailure(job.getId(), attempts, errorMsg, next, job.getState());
                System.out.println("Job " + job.getId() + " failed. Will retry in " + backoff + "s.");
            }

        } catch (Exception e) {
            System.err.println("Failure handling error: " + e.getMessage());
        }
    }
    /*private void moveToDLQ(Job job,String reason){
        String sql="INSERT INTO dlq(id,command,attempts,last_error) VALUES(?,?,?,?)";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setString(1,job.getId());
            ps.setString(2,job.getCommand());
            ps.setInt(3,job.getAttempts());
            ps.setString(4,reason);
            ps.executeUpdate();
        }catch(SQLException e){
            System.err.println("DLQ insert error: "+e.getMessage());
        }
    }*/

    private void sleepFor(long ms){
        try{
            Thread.sleep(ms);
        }catch(InterruptedException ignored) {
        }
    }
    public void stopWorker(){
        running=false;
    }
}
