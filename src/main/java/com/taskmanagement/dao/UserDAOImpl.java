package com.taskmanagement.dao;

import com.taskmanagement.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC implementation of UserDAO (used by the login screen).
 */
public class UserDAOImpl implements UserDAO {

    private static final String LOGIN_SQL =
            "SELECT role FROM users WHERE username = ? AND password = ?";

    @Override
        public String authenticateUser(String username, String password) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(LOGIN_SQL)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("role") : null;
            }
        }
    }
}
