package com.queuectl.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.sql.*;

@Command(
        name = "retry",
        description = "Re-enqueue a job from the Dead Letter Queue"
)
public class DLQRetry implements Runnable {

    @Option(names = "--id", description = "ID of job to retry", required = true)
    private String id;

    @Override
    public void run() {
        String existsInDlq = "SELECT 1 FROM dlq WHERE id=?";
        String revive = "UPDATE jobs SET state='pending', attempts=0, last_error=NULL, next_run_at=NOW(), updated_at=NOW() WHERE id=?";
        String removeFromDlq = "DELETE FROM dlq WHERE id=?";

        try (Connection con = com.queuectl.db.repository.DBConnection.getConnection()) {
            con.setAutoCommit(false);

            boolean present;
            try (PreparedStatement ps = con.prepareStatement(existsInDlq)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    present = rs.next();
                }
            }
            if (!present) {
                con.rollback();
                System.err.println("No DLQ entry found for ID: " + id);
                return;
            }

            int updated;
            try (PreparedStatement ps = con.prepareStatement(revive)) {
                ps.setString(1, id);
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                con.rollback();
                System.err.println("Job not found in jobs table for ID: " + id);
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(removeFromDlq)) {
                ps.setString(1, id);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Job " + id + " revived from DLQ and queued for retry.");
        } catch (SQLException e) {
            System.err.println("Error retrying DLQ job: " + e.getMessage());
        }
    }
}
