package com.queuectl.cli;

import com.queuectl.db.repository.DBConnection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.sql.*;

@Command(
        name = "list",
        description = "List jobs by state (e.g., pending, processing, completed, dead)"
)
public class ListJobs implements Runnable {

    @Option(names = "--state", description = "Filter by job state (pending, completed, dead, etc.)", required = false)
    private String state;

    @Override
    public void run() {
        String sql = (state != null)
                ? "SELECT id, command, state, attempts, max_retries, last_error FROM jobs WHERE state=? ORDER BY created_at DESC"
                : "SELECT id, command, state, attempts, max_retries, last_error FROM jobs ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (state != null) ps.setString(1, state);
            ResultSet rs = ps.executeQuery();

            System.out.println("Jobs:");
            System.out.println("------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf(
                        "ID: %s | State: %s | Attempts: %d/%d | Cmd: %s | Error: %s%n",
                        rs.getString("id"),
                        rs.getString("state"),
                        rs.getInt("attempts"),
                        rs.getInt("max_retries"),
                        rs.getString("command"),
                        rs.getString("last_error")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error listing jobs: " + e.getMessage());
        }
    }
}
