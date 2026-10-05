package com.taskmanagement.dao;

import com.taskmanagement.database.DatabaseConnection;
import com.taskmanagement.model.Task;
import com.taskmanagement.model.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JDBC implementation of TaskDAO.
 * This is the ONLY class that contains SQL for the tasks table.
 * Every query uses PreparedStatement (prevents SQL injection) and
 * try-with-resources (connections/statements/result sets are always closed).
 */
public class TaskDAOImpl implements TaskDAO {

    private static final String SELECT_COLUMNS =
            "SELECT t.id, t.title, t.description, t.due_date, t.priority, t.status, "
                    + "t.assigned_user_id, u.username AS assigned_username "
                    + "FROM tasks t JOIN users u ON u.id = t.assigned_user_id";
    private static final String INSERT_SQL =
            "INSERT INTO tasks (title, description, due_date, priority, status, assigned_user_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE tasks SET title = ?, description = ?, due_date = ?, priority = ?, status = ?, "
                    + "assigned_user_id = ? WHERE id = ?";
    private static final String UPDATE_STATUS_SQL =
            "UPDATE tasks SET status = ? WHERE id = ? AND assigned_user_id = ?";
    private static final String DELETE_SQL = "DELETE FROM tasks WHERE id = ?";

    @Override
    public boolean addTask(Task task, User actor) throws SQLException {
        requireAdmin(actor);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setTaskParameters(ps, task);
            ps.setInt(6, task.getAssignedUserId());
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
    public List<Task> getAllTasks(User actor) throws SQLException {
        requireKnownUser(actor);
        String sql = SELECT_COLUMNS + (actor.isAdmin() ? "" : " WHERE t.assigned_user_id = ?") + " ORDER BY t.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!actor.isAdmin()) {
                ps.setInt(1, actor.id());
            }
            return readTasks(ps);
        }
    }

    @Override
    public Task getTaskById(int id, User actor) throws SQLException {
        requireKnownUser(actor);
        String sql = SELECT_COLUMNS + " WHERE t.id = ?" + (actor.isAdmin() ? "" : " AND t.assigned_user_id = ?");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (!actor.isAdmin()) {
                ps.setInt(2, actor.id());
            }
            List<Task> tasks = readTasks(ps);
            return tasks.isEmpty() ? null : tasks.get(0);
        }
    }

    @Override
    public List<Task> searchTasks(String keyword, User actor) throws SQLException {
        requireKnownUser(actor);
        String text = keyword == null ? "" : keyword.trim();
        int idValue;
        try {
            idValue = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            idValue = -1;
        }

        String sql = SELECT_COLUMNS + " WHERE (t.id = ? OR t.title LIKE ? OR t.status LIKE ? OR t.priority LIKE ?)"
                + (actor.isAdmin() ? "" : " AND t.assigned_user_id = ?") + " ORDER BY t.id";
        String pattern = "%" + text + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idValue);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            if (!actor.isAdmin()) {
                ps.setInt(5, actor.id());
            }
            return readTasks(ps);
        }
    }

    @Override
    public boolean updateTask(Task task, User actor) throws SQLException {
        requireAdmin(actor);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setTaskParameters(ps, task);
            ps.setInt(6, task.getAssignedUserId());
            ps.setInt(7, task.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateTaskStatus(int id, String status, User actor) throws SQLException {
        requireStudent(actor);
        if (!Arrays.asList(Task.STATUSES).contains(status)) {
            throw new SQLException("Invalid task status.");
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.setInt(3, actor.id());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteTask(int id, User actor) throws SQLException {
        requireAdmin(actor);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Task> filterTasks(String priority, String status, User actor) throws SQLException {
        requireKnownUser(actor);
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (!actor.isAdmin()) {
            sql.append(" AND t.assigned_user_id = ?");
            params.add(actor.id());
        }
        if (priority != null && !"ALL".equalsIgnoreCase(priority)) {
            sql.append(" AND t.priority = ?");
            params.add(priority);
        }
        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY t.id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Integer id) {
                    ps.setInt(i + 1, id);
                } else {
                    ps.setString(i + 1, (String) value);
                }
            }
            return readTasks(ps);
        }
    }

    @Override
    public int countAllTasks(User actor) throws SQLException {
        requireKnownUser(actor);
        String sql = "SELECT COUNT(*) FROM tasks" + (actor.isAdmin() ? "" : " WHERE assigned_user_id = ?");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!actor.isAdmin()) {
                ps.setInt(1, actor.id());
            }
            return readCount(ps);
        }
    }

    @Override
    public int countTasksByStatus(String status, User actor) throws SQLException {
        requireKnownUser(actor);
        String sql = "SELECT COUNT(*) FROM tasks WHERE status = ?"
                + (actor.isAdmin() ? "" : " AND assigned_user_id = ?");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (!actor.isAdmin()) {
                ps.setInt(2, actor.id());
            }
            return readCount(ps);
        }
    }

    private void requireKnownUser(User actor) throws SQLException {
        if (actor == null || (!actor.isAdmin() && !actor.isStudent())) {
            throw new SQLException("Authenticated user role is not supported.");
        }
    }

    private void requireAdmin(User actor) throws SQLException {
        if (actor == null || !actor.isAdmin()) {
            throw new SQLException("Only administrators can manage task details.");
        }
    }

    private void requireStudent(User actor) throws SQLException {
        if (actor == null || !actor.isStudent()) {
            throw new SQLException("Only students can update their task status.");
        }
    }

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

    private List<Task> readTasks(PreparedStatement ps) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Date dueDate = rs.getDate("due_date");
        Task task = new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                dueDate == null ? null : dueDate.toLocalDate(),
                rs.getString("priority"),
                rs.getString("status"));
        task.setAssignedUserId(rs.getInt("assigned_user_id"));
        task.setAssignedUsername(rs.getString("assigned_username"));
        return task;
    }

    private int readCount(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
