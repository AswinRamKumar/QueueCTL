package com.queuectl.cli;

import picocli.CommandLine.Command;
import java.sql.*;

@Command(
        name = "list",
        description = "List jobs in the Dead Letter Queue"
)
public class DLQList implements Runnable {

    @Override
    public void run() {
        String sql = "SELECT id, command, attempts, last_error FROM dlq ORDER BY id ASC";
        try (Connection con = com.queuectl.db.repository.DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Dead Letter Queue:");
            System.out.println("-------------------------------------------");
            while (rs.next()) {
                System.out.printf("ID: %s | Attempts: %d | Command: %s | Error: %s%n",
                        rs.getString("id"),
                        rs.getInt("attempts"),
                        rs.getString("command"),
                        rs.getString("last_error"));
            }
        } catch (SQLException e) {
            System.err.println("Error listing DLQ: " + e.getMessage());
        }
    }
}
