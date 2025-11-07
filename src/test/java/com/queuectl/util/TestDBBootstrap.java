package com.queuectl.util;

import com.queuectl.db.repository.DBConnection;
import java.sql.*;

public class TestDBBootstrap {
    private static boolean initialized = false;

    public static synchronized void ensureSchema() {
        if (initialized) return;
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
                    id VARCHAR(64) NOT NULL PRIMARY KEY,
                    command TEXT NOT NULL,
                    attempts INT DEFAULT 0,
                    last_error TEXT,
                    created_at DATETIME DEFAULT NOW()
                )
            """);

            System.out.println("[TEST-DB] Tables verified/created successfully.");
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("[TEST-DB] Schema setup failed: " + e.getMessage(), e);
        }
    }
}
