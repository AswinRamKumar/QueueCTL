package com.queuectl.db.repository;

import com.queuectl.db.models.Job;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobStore {

    private static boolean initialized = false;

    public JobStore() {
        if (!initialized) {
            synchronized (JobStore.class) {
                if (!initialized) {
                    initSchema();
                    initialized = true;
                }
            }
        }
    }

    private void initSchema() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS jobs (
                    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
                    id VARCHAR(64) NOT NULL UNIQUE,
                    command TEXT NOT NULL,
                    state VARCHAR(32) NOT NULL,
                    attempts INT DEFAULT 0,
                    max_retries INT DEFAULT 3,
                    created_at DATETIME,
                    updated_at DATETIME,
                    next_run_at DATETIME,
                    last_error TEXT
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS dlq (
                    id VARCHAR(64) PRIMARY KEY,
                    command TEXT,
                    attempts INT,
                    last_error TEXT,
                    created_at DATETIME DEFAULT NOW()
                )
            """);

            System.out.println("[DB] Tables verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing tables: " + e.getMessage());
        }
    }

    public void insert(Job job) {
        String sql = "INSERT INTO jobs(id,command,state,attempts,max_retries,created_at,updated_at,next_run_at,last_error) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, job.getId());
            ps.setString(2, job.getCommand());
            ps.setString(3, job.getState());
            ps.setInt(4, job.getAttempts());
            ps.setInt(5, job.getMaxRetries());
            ps.setTimestamp(6, Timestamp.valueOf(job.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(job.getUpdatedAt()));
            ps.setTimestamp(8, Timestamp.valueOf(job.getNextRunAt()));
            ps.setString(9, job.getLastError());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }

    public List<Job> getPendingJobs() {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE state='pending' ORDER BY created_at ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Job j = new Job(
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
                jobs.add(j);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fetch failed: " + e.getMessage(), e);
        }
        return jobs;
    }

    public void updateState(String id, String newState) {
        String sql = "UPDATE jobs SET state=?, updated_at=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newState);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update state failed: " + e.getMessage(), e);
        }
    }

    public void updateFailure(String id, int attempts, String lastError, java.time.LocalDateTime nextRunAt, String newState) {
        String sql = "UPDATE jobs SET state=?, attempts=?, last_error=?, next_run_at=?, updated_at=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newState);
            ps.setInt(2, attempts);
            ps.setString(3, lastError);
            ps.setTimestamp(4, Timestamp.valueOf(nextRunAt));
            ps.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failure update failed: " + e.getMessage(), e);
        }
    }

    public void markDead(String id,int attempts,String lastError){
        String sql="UPDATE jobs SET state='dead',attempts=?,last_error=?,updated_at=? WHERE id=?";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,attempts);
            ps.setString(2,lastError);
            ps.setTimestamp(3,Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(4,id);
            ps.executeUpdate();
        }catch(SQLException e){throw new RuntimeException("Mark dead failed: "+e.getMessage(),e);}
    }

}
