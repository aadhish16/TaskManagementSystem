package com.taskmanagement.dao;

import java.sql.SQLException;

/**
 * DAO interface for login users.
 */
public interface UserDAO {

    /**
     * Returns the user's role when the credentials are valid, or null otherwise.
     */
    String authenticateUser(String username, String password) throws SQLException;
}
