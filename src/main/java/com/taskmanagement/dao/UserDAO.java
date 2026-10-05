package com.taskmanagement.dao;

import com.taskmanagement.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for login users.
 */
public interface UserDAO {

    /**
     * Returns the user's role when the credentials are valid, or null otherwise.
     */
    User authenticateUser(String username, String password) throws SQLException;

    List<User> getStudents() throws SQLException;
}
