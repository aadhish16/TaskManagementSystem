package com.taskmanagement.dao;

import com.taskmanagement.database.DatabaseConnection;
import com.taskmanagement.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of UserDAO (used by the login screen).
 */
public class UserDAOImpl implements UserDAO {

    private static final String LOGIN_SQL =
            "SELECT id, username, role FROM users WHERE username = ? AND password = ?";
        private static final String STUDENTS_SQL =
            "SELECT id, username, role FROM users WHERE role = 'STUDENT' ORDER BY username";

    @Override
        public User authenticateUser(String username, String password) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(LOGIN_SQL)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"))
                        : null;
            }
        }
    }

    @Override
    public List<User> getStudents() throws SQLException {
        List<User> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(STUDENTS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
        }
        return students;
    }
}
