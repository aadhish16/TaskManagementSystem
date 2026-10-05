package com.taskmanagement.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections to the MySQL database.
 *
 * Change DB_USER and DB_PASSWORD below to match your own MySQL installation.
 */
public final class DatabaseConnection {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/task_management"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // Utility class: no objects should be created
    private DatabaseConnection() {
    }

    /**
     * Opens a new connection. The caller must close it
     * (the DAO classes do this automatically with try-with-resources).
     *
     * @return an open JDBC connection
     * @throws SQLException if the driver is missing or MySQL cannot be reached
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Not strictly required with JDBC 4+, but makes a missing driver easy to diagnose
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. "
                    + "Make sure mysql-connector-j is on the classpath (run via Maven).", e);
        }

        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("Unable to connect to the database 'task_management'.\n"
                    + "Check that MySQL is running and the username/password in "
                    + "DatabaseConnection.java are correct.\n\nDetails: " + e.getMessage(),
                    e.getSQLState(), e.getErrorCode(), e);
        }
    }
}
