package com.taskmanagement.dao;

import com.taskmanagement.database.DatabaseConnection;
import com.taskmanagement.model.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of TaskDAO.
 * This is the ONLY class that contains SQL for the tasks table.
 * Every query uses PreparedStatement (prevents SQL injection) and
 * try-with-resources (connections/statements/result sets are always closed).
 */
public class TaskDAOImpl implements TaskDAO {

    private static final String SELECT_COLUMNS =
            "SELECT id, title, description, due_date, priority, status FROM tasks";

    private static final String INSERT_SQL =
            "INSERT INTO tasks (title, description, due_date, priority, status) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = SELECT_COLUMNS + " ORDER BY id";
    private static final String SELECT_BY_ID_SQL = SELECT_COLUMNS + " WHERE id = ?";
    private static final String SEARCH_SQL = SELECT_COLUMNS
            + " WHERE id = ? OR title LIKE ? OR status LIKE ? OR priority LIKE ? ORDER BY id";
    private static final String UPDATE_SQL =
            "UPDATE tasks SET title = ?, description = ?, due_date = ?, priority = ?, status = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM tasks WHERE id = ?";
    private static final String COUNT_ALL_SQL = "SELECT COUNT(*) FROM tasks";
    private static final String COUNT_BY_STATUS_SQL = "SELECT COUNT(*) FROM tasks WHERE status = ?";

    @Override
    public boolean addTask(Task task) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setTaskParameters(ps, task);
            int rows = ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        }
    }

    @Override
    public List<Task> getAllTasks() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL)) {
            return readTasks(ps);
        }
    }

    @Override
    public Task getTaskById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            List<Task> tasks = readTasks(ps);
            return tasks.isEmpty() ? null : tasks.get(0);
        }
    }

    @Override
    public List<Task> searchTasks(String keyword) throws SQLException {
        String text = keyword == null ? "" : keyword.trim();

        // If the keyword is a number, also match it against the task id
        int idValue;
        try {
            idValue = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            idValue = -1; // no task has id -1, so the id condition simply won't match
        }

        String pattern = "%" + text + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
            ps.setInt(1, idValue);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            return readTasks(ps);
        }
    }

    @Override
    public boolean updateTask(Task task) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setTaskParameters(ps, task);
            ps.setInt(6, task.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteTask(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Task> filterTasks(String priority, String status) throws SQLException {
        // Build the WHERE clause only for the filters that are not "ALL"
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(" WHERE 1 = 1");
        List<String> params = new ArrayList<>();

        if (priority != null && !"ALL".equalsIgnoreCase(priority)) {
            sql.append(" AND priority = ?");
            params.add(priority);
        }
        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            return readTasks(ps);
        }
    }

    @Override
    public int countAllTasks() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ALL_SQL)) {
            return readCount(ps);
        }
    }

    @Override
    public int countTasksByStatus(String status) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_BY_STATUS_SQL)) {
            ps.setString(1, status);
            return readCount(ps);
        }
    }

    // ---------------------------------------------------------------
    // Helper methods (avoid repeating the same JDBC code everywhere)
    // ---------------------------------------------------------------

    /** Sets parameters 1-5 (title, description, due_date, priority, status). */
    private void setTaskParameters(PreparedStatement ps, Task task) throws SQLException {
        ps.setString(1, task.getTitle());
        ps.setString(2, task.getDescription());
        if (task.getDueDate() != null) {
            ps.setDate(3, Date.valueOf(task.getDueDate()));
        } else {
            ps.setNull(3, Types.DATE);
        }
        ps.setString(4, task.getPriority());
        ps.setString(5, task.getStatus());
    }

    /** Executes a SELECT and converts every row into a Task object. */
    private List<Task> readTasks(PreparedStatement ps) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    /** Converts the current ResultSet row into a Task. */
    private Task mapRow(ResultSet rs) throws SQLException {
        Date dueDate = rs.getDate("due_date");
        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                dueDate == null ? null : dueDate.toLocalDate(),
                rs.getString("priority"),
                rs.getString("status"));
    }

    /** Executes a COUNT(*) query and returns the number. */
    private int readCount(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
