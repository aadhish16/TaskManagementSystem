package com.taskmanagement.dao;

import java.sql.SQLException;

/**
 * DAO interface for login users.
 */
public interface UserDAO {

    /**
     * Checks whether the username and password match a row in the users table.
     *
     * @return true if the credentials are valid
     */
    boolean validateUser(String username, String password) throws SQLException;
}
