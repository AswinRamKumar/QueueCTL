package com.queuectl.db.repository;

import com.queuectl.util.ConfigStore;
import com.queuectl.util.ConfigLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        String url = ConfigStore.get("db.url");
        if (url == null) url = ConfigLoader.get("db.url");

        String user = ConfigStore.get("db.user");
        if (user == null) user = ConfigLoader.get("db.user");

        String password = ConfigStore.get("db.password");
        if (password == null) password = ConfigLoader.get("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}
