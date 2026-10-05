package com.taskmanagement.gui;

import com.taskmanagement.dao.TaskDAO;
import com.taskmanagement.model.Task;
import com.taskmanagement.util.ValidationException;
import com.taskmanagement.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/**
 * Main working area: the task form (left) and the task table with
 * search / filter tools (right).
 *
 * The panel never runs SQL itself - it only calls TaskDAO methods.
 */
public class TaskPanel extends JPanel {

    private static final String SELECT_OPTION = "-- Select --";
    private static final String ALL_OPTION = "ALL";
    private static final String[] TABLE_COLUMNS = {"ID", "Title", "Description", "Due Date", "Priority", "Status"};

    private final TaskDAO taskDAO;
    private final Runnable onDataChanged;
    private final boolean canManageTasks;

    // ----- Form fields -----
    private final JTextField titleField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(5, 20);
    private final JTextField dueDateField = new JTextField(20);
    private final JComboBox<String> priorityCombo = new JComboBox<>(withFirst(SELECT_OPTION, Task.PRIORITIES));
    private final JComboBox<String> statusCombo = new JComboBox<>(withFirst(SELECT_OPTION, Task.STATUSES));
    private final JLabel selectedLabel = new JLabel("Selected Task ID: none");

    // ----- Search & filter -----
    private final JTextField searchField = new JTextField(16);
    private final JComboBox<String> priorityFilter = new JComboBox<>(withFirst(ALL_OPTION, Task.PRIORITIES));
    private final JComboBox<String> statusFilter = new JComboBox<>(withFirst(ALL_OPTION, Task.STATUSES));

    // ----- Table -----
    private final DefaultTableModel tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // editing happens in the form, not in the table
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Integer.class : String.class; // numeric sort for ID
        }
    };
    private final JTable taskTable = new JTable(tableModel);

    /** Id of the task selected in the table, or -1 when nothing is selected. */
    private int selectedTaskId = -1;

    public TaskPanel(TaskDAO taskDAO, Runnable onDataChanged, boolean canManageTasks) {
        super(new BorderLayout(14, 0));
        this.taskDAO = taskDAO;
        this.onDataChanged = onDataChanged;
        this.canManageTasks = canManageTasks;
        setOpaque(false);

        if (canManageTasks) {
            add(createFormPanel(), BorderLayout.WEST);
        }
        add(createTablePanel(), BorderLayout.CENTER);

        loadAllTasks();
    }

    // =================================================================
    // UI construction
    // =================================================================

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIStyle.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        form.setPreferredSize(new Dimension(330, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel heading = new JLabel("Task Details");
        heading.setFont(UIStyle.HEADING_FONT);
        int row = 0;
        gbc.gridy = row++;
        form.add(heading, gbc);

        selectedLabel.setForeground(UIStyle.NEUTRAL);
        gbc.gridy = row++;
        form.add(selectedLabel, gbc);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(UIStyle.LABEL_FONT);
        dueDateField.setToolTipText("Format: " + ValidationUtil.DATE_PATTERN + " (e.g. 2026-12-31)");

        row = addField(form, gbc, row, "Task Title *", titleField);
        row = addField(form, gbc, row, "Description *", new JScrollPane(descriptionArea));
        row = addField(form, gbc, row, "Due Date * (" + ValidationUtil.DATE_PATTERN + ")", dueDateField);
        row = addField(form, gbc, row, "Priority *", priorityCombo);
        row = addField(form, gbc, row, "Status *", statusCombo);

        JButton addButton = UIStyle.createButton("Add Task", UIStyle.SUCCESS);
        JButton updateButton = UIStyle.createButton("Update", UIStyle.WARNING);
        JButton deleteButton = UIStyle.createButton("Delete", UIStyle.DANGER);
        JButton clearButton = UIStyle.createButton("Clear", UIStyle.NEUTRAL);

        addButton.addActionListener(e -> addTask());
        updateButton.addActionListener(e -> updateTask());
        deleteButton.addActionListener(e -> deleteTask());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridy = row++;
        gbc.insets = new Insets(14, 0, 4, 0);
        form.add(buttons, gbc);

        // Empty filler pushes everything to the top of the panel
        gbc.gridy = row;
        gbc.weighty = 1;
        form.add(new JLabel(), gbc);
        return form;
    }

    /** Adds a label + component pair to the form and returns the next free row. */
    private int addField(JPanel form, GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.LABEL_FONT);
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 0, 2, 0);
        form.add(label, gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 2, 0);
        form.add(field, gbc);
        return row;
    }

    private JPanel createTablePanel() {
        // ----- Toolbar: search + filters -----
        JButton searchButton = UIStyle.createButton("Search", UIStyle.PRIMARY);
        JButton filterButton = UIStyle.createButton("Filter", UIStyle.PRIMARY);
        JButton refreshButton = UIStyle.createButton("Refresh", UIStyle.NEUTRAL);

        searchButton.addActionListener(e -> searchTasks());
        searchField.addActionListener(e -> searchTasks()); // Enter key in the search box
        filterButton.addActionListener(e -> filterTasks());
        refreshButton.addActionListener(e -> refresh());
        searchField.setToolTipText("Search by ID, title, status or priority");

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Search:"));
        searchRow.add(searchField);
        searchRow.add(searchButton);
        searchRow.add(refreshButton);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Priority:"));
        filterRow.add(priorityFilter);
        filterRow.add(new JLabel("Status:"));
        filterRow.add(statusFilter);
        filterRow.add(filterButton);

        JPanel toolbar = new JPanel(new GridLayout(2, 1));
        toolbar.setOpaque(false);
        toolbar.add(searchRow);
        toolbar.add(filterRow);

        // ----- Table -----
        taskTable.setRowHeight(26);
        taskTable.setFont(UIStyle.LABEL_FONT);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setAutoCreateRowSorter(true);
        taskTable.setFillsViewportHeight(true);
        taskTable.setGridColor(UIStyle.BORDER);
        taskTable.setSelectionBackground(new Color(191, 219, 254));
        taskTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = taskTable.getTableHeader();
        header.setFont(UIStyle.BUTTON_FONT);
        header.setBackground(UIStyle.PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        int[] widths = {50, 170, 300, 100, 80, 110};
        for (int i = 0; i < widths.length; i++) {
            taskTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Event handling: load the selected row into the form
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedTaskIntoForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIStyle.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // =================================================================
    // Actions (public ones are also used by the dashboard sidebar)
    // =================================================================

    /** CREATE */
    public void addTask() {
        if (!canManageTasks) {
            showWarning("Your account can only view tasks.");
            return;
        }
        try {
            Task task = readTaskFromForm();
            if (taskDAO.addTask(task)) {
                showInfo("Task added successfully (ID: " + task.getId() + ").");
                clearForm();
                loadAllTasks();
                onDataChanged.run();
            } else {
                showError("The task could not be added.");
            }
        } catch (ValidationException ex) {
            showWarning(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** READ (all) */
    public void loadAllTasks() {
        try {
            showTasks(taskDAO.getAllTasks());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** READ (search) */
    public void searchTasks() {
        String keyword = searchField.getText().trim();
        if (ValidationUtil.isEmpty(keyword)) {
            showWarning("Please enter an ID, title, status or priority to search for.");
            searchField.requestFocusInWindow();
            return;
        }
        try {
            List<Task> results = taskDAO.searchTasks(keyword);
            showTasks(results);
            if (results.isEmpty()) {
                showInfo("No tasks found matching \"" + keyword + "\".");
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** READ (filter) */
    public void filterTasks() {
        String priority = (String) priorityFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();
        try {
            List<Task> results = taskDAO.filterTasks(priority, status);
            showTasks(results);
            if (results.isEmpty()) {
                showInfo("No tasks match the selected filters.");
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** UPDATE */
    public void updateTask() {
        if (!canManageTasks) {
            showWarning("Your account can only view tasks.");
            return;
        }
        if (!isTaskSelected()) {
            return;
        }
        try {
            Task task = readTaskFromForm();
            task.setId(selectedTaskId);
            if (taskDAO.updateTask(task)) {
                showInfo("Task ID " + task.getId() + " updated successfully.");
                clearForm();
                loadAllTasks();
                onDataChanged.run();
            } else {
                showError("Task ID " + task.getId() + " no longer exists. Please refresh the list.");
            }
        } catch (ValidationException ex) {
            showWarning(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** DELETE */
    public void deleteTask() {
        if (!canManageTasks) {
            showWarning("Your account can only view tasks.");
            return;
        }
        if (!isTaskSelected()) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this task?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = selectedTaskId;
            if (taskDAO.deleteTask(id)) {
                showInfo("Task ID " + id + " deleted successfully.");
            } else {
                showError("Task ID " + id + " was not found. It may already be deleted.");
            }
            clearForm();
            loadAllTasks();
            onDataChanged.run();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** Clears the form so the user can type a new task. */
    public void prepareNewTask() {
        if (!canManageTasks) {
            return;
        }
        clearForm();
        titleField.requestFocusInWindow();
    }

    /** Resets search/filters, reloads the table and the summary cards. */
    public void refresh() {
        searchField.setText("");
        priorityFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        clearForm();
        loadAllTasks();
        onDataChanged.run();
    }

    public void clearForm() {
        selectedTaskId = -1;
        taskTable.clearSelection();
        titleField.setText("");
        descriptionArea.setText("");
        dueDateField.setText("");
        priorityCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        selectedLabel.setText("Selected Task ID: none");
    }

    // =================================================================
    // Helpers
    // =================================================================

    /** Validates the form and converts it into a Task (throws ValidationException if invalid). */
    private Task readTaskFromForm() throws ValidationException {
        return ValidationUtil.buildTask(
                titleField.getText(),
                descriptionArea.getText(),
                dueDateField.getText(),
                (String) priorityCombo.getSelectedItem(),
                (String) statusCombo.getSelectedItem());
    }

    /** Called when the table selection changes. */
    private void loadSelectedTaskIntoForm() {
        if (!canManageTasks) {
            return;
        }
        int viewRow = taskTable.getSelectedRow();
        if (viewRow < 0) {
            return; // selection was cleared
        }
        int modelRow = taskTable.convertRowIndexToModel(viewRow);
        int id = (Integer) tableModel.getValueAt(modelRow, 0);

        try {
            Task task = taskDAO.getTaskById(id);
            if (task == null) {
                showError("Task ID " + id + " no longer exists. The list will be refreshed.");
                loadAllTasks();
                return;
            }
            selectedTaskId = task.getId();
            selectedLabel.setText("Selected Task ID: " + task.getId());
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription() == null ? "" : task.getDescription());
            dueDateField.setText(task.getDueDate() == null ? "" : task.getDueDate().toString());
            selectComboValue(priorityCombo, task.getPriority());
            selectComboValue(statusCombo, task.getStatus());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    /** Selects the value in the combo box, or the "-- Select --" option if it is unknown. */
    private void selectComboValue(JComboBox<String> combo, String value) {
        combo.setSelectedIndex(0);
        if (value != null) {
            combo.setSelectedItem(value);
        }
    }

    private boolean isTaskSelected() {
        if (selectedTaskId < 0) {
            showWarning("Please select a task from the table first.");
            return false;
        }
        return true;
    }

    private void showTasks(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            tableModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate() == null ? "" : task.getDueDate().toString(),
                    task.getPriority(),
                    task.getStatus()
            });
        }
    }

    /** Builds a combo box option list with an extra first item, e.g. "ALL". */
    private static String[] withFirst(String first, String[] rest) {
        String[] result = new String[rest.length + 1];
        result[0] = first;
        System.arraycopy(rest, 0, result, 1, rest.length);
        return result;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
