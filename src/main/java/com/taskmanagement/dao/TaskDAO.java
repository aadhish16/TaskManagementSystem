package com.taskmanagement.dao;

import com.taskmanagement.model.Task;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Task.
 * Demonstrates ABSTRACTION: the GUI only knows WHAT operations exist,
 * not HOW they are implemented (see TaskDAOImpl).
 */
public interface TaskDAO {

    /** CREATE: inserts a task. Sets the generated id on the task object. */
    boolean addTask(Task task) throws SQLException;

    /** READ: returns all tasks ordered by id. */
    List<Task> getAllTasks() throws SQLException;

    /** READ: returns one task, or null if no task has that id. */
    Task getTaskById(int id) throws SQLException;

    /** READ: finds tasks whose id, title, status or priority matches the keyword. */
    List<Task> searchTasks(String keyword) throws SQLException;

    /** UPDATE: saves changes to an existing task. Returns false if the task does not exist. */
    boolean updateTask(Task task) throws SQLException;

    /** DELETE: removes a task. Returns false if the task does not exist. */
    boolean deleteTask(int id) throws SQLException;

    /** READ: filters by priority and status. Use "ALL" to skip a filter. */
    List<Task> filterTasks(String priority, String status) throws SQLException;

    /** Returns the total number of tasks (for the dashboard cards). */
    int countAllTasks() throws SQLException;

    /** Returns how many tasks have the given status (for the dashboard cards). */
    int countTasksByStatus(String status) throws SQLException;
}
