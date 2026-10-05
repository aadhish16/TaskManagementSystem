package com.taskmanagement.gui;

import com.taskmanagement.dao.TaskDAO;
import com.taskmanagement.dao.TaskDAOImpl;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;

/**
 * Main dashboard shown after a successful login.
 * Layout: header (NORTH), navigation buttons (WEST), summary cards + TaskPanel (CENTER).
 */
public class DashboardFrame extends JFrame {

    // POLYMORPHISM: the variable type is the interface, the object is the implementation
    private final TaskDAO taskDAO = new TaskDAOImpl();

    private final SummaryCard totalCard = new SummaryCard("Total Tasks", UIStyle.PRIMARY);
    private final SummaryCard pendingCard = new SummaryCard("Pending", UIStyle.WARNING);
    private final SummaryCard inProgressCard = new SummaryCard("In Progress", new Color(124, 58, 237));
    private final SummaryCard completedCard = new SummaryCard("Completed", UIStyle.SUCCESS);

    private final TaskPanel taskPanel;

    public DashboardFrame(String username) {
        super("Task Management System - Dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1300, 780);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        // TaskPanel calls refreshSummary() whenever tasks are added/updated/deleted
        taskPanel = new TaskPanel(taskDAO, this::refreshSummary);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIStyle.BACKGROUND);
        root.add(createHeader(username), BorderLayout.NORTH);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createCenter(), BorderLayout.CENTER);
        setContentPane(root);

        refreshSummary();
    }

    private JPanel createHeader(String username) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Task Management System");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + username + "   ");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(UIStyle.LABEL_FONT);

        JButton logoutButton = UIStyle.createButton("Logout", UIStyle.DANGER);
        logoutButton.addActionListener(e -> logout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(welcome);
        right.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JButton addButton = UIStyle.createButton("Add Task", UIStyle.SUCCESS);
        JButton viewButton = UIStyle.createButton("View Tasks", UIStyle.PRIMARY);
        JButton searchButton = UIStyle.createButton("Search Task", UIStyle.PRIMARY);
        JButton updateButton = UIStyle.createButton("Update Task", UIStyle.WARNING);
        JButton deleteButton = UIStyle.createButton("Delete Task", UIStyle.DANGER);
        JButton refreshButton = UIStyle.createButton("Refresh", UIStyle.NEUTRAL);

        // Each sidebar button reuses the same methods as the TaskPanel buttons
        addButton.addActionListener(e -> taskPanel.prepareNewTask());
        viewButton.addActionListener(e -> taskPanel.loadAllTasks());
        searchButton.addActionListener(e -> taskPanel.searchTasks());
        updateButton.addActionListener(e -> taskPanel.updateTask());
        deleteButton.addActionListener(e -> taskPanel.deleteTask());
        refreshButton.addActionListener(e -> taskPanel.refresh());

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 10));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(viewButton);
        buttons.add(searchButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);

        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setForeground(new Color(148, 163, 184));
        menuLabel.setFont(UIStyle.BUTTON_FONT);
        menuLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 10, 0));

        // The wrapper keeps the buttons at the top instead of stretching to full height
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(menuLabel, BorderLayout.NORTH);
        wrapper.add(buttons, BorderLayout.CENTER);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIStyle.PRIMARY_DARK);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        sidebar.setPreferredSize(new Dimension(170, 0));
        sidebar.add(wrapper, BorderLayout.NORTH);
        return sidebar;
    }

    private JPanel createCenter() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(totalCard);
        cards.add(pendingCard);
        cards.add(inProgressCard);
        cards.add(completedCard);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        center.add(cards, BorderLayout.NORTH);
        center.add(taskPanel, BorderLayout.CENTER);
        return center;
    }

    /** Reloads the numbers shown on the four summary cards. */
    public void refreshSummary() {
        try {
            totalCard.setValue(String.valueOf(taskDAO.countAllTasks()));
            pendingCard.setValue(String.valueOf(taskDAO.countTasksByStatus("PENDING")));
            inProgressCard.setValue(String.valueOf(taskDAO.countTasksByStatus("IN PROGRESS")));
            completedCard.setValue(String.valueOf(taskDAO.countTasksByStatus("COMPLETED")));
        } catch (SQLException ex) {
            // TaskPanel already shows a dialog for database errors; here we just show "--"
            totalCard.setValue("--");
            pendingCard.setValue("--");
            inProgressCard.setValue("--");
            completedCard.setValue("--");
            System.err.println("Could not load task summary: " + ex.getMessage());
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /**
     * A small colored card showing a label and a number.
     * INHERITANCE: SummaryCard extends JPanel.
     */
    private static class SummaryCard extends JPanel {

        private final JLabel valueLabel = new JLabel("0", SwingConstants.LEFT);

        SummaryCard(String title, Color accent) {
            super(new BorderLayout(0, 4));
            setBackground(UIStyle.CARD);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 6, 0, 0, accent),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(UIStyle.LABEL_FONT);
            titleLabel.setForeground(UIStyle.NEUTRAL);

            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
            valueLabel.setForeground(accent);

            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
        }

        void setValue(String value) {
            valueLabel.setText(value);
        }
    }
}
