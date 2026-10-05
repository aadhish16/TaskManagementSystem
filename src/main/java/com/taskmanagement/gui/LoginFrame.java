package com.taskmanagement.gui;

import com.taskmanagement.dao.UserDAO;
import com.taskmanagement.dao.UserDAOImpl;
import com.taskmanagement.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

/**
 * Login window.
 * Demonstrates INHERITANCE: LoginFrame "is a" JFrame.
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserDAO userDAO = new UserDAOImpl();

    public LoginFrame() {
        super("Task Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 340);
        setResizable(false);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIStyle.BACKGROUND);

        // ----- Header -----
        JLabel header = new JLabel("Task Management System", SwingConstants.CENTER);
        header.setFont(UIStyle.TITLE_FONT);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(UIStyle.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        root.add(header, BorderLayout.NORTH);

        // ----- Form (GridBagLayout) -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIStyle.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel subtitle = new JLabel("Please sign in to continue");
        subtitle.setFont(UIStyle.HEADING_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(subtitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        form.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(createLabel("Password:"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        root.add(form, BorderLayout.CENTER);

        // ----- Buttons (FlowLayout) -----
        JButton loginButton = UIStyle.createButton("Login", UIStyle.PRIMARY);
        JButton clearButton = UIStyle.createButton("Clear", UIStyle.NEUTRAL);

        // Event handling with lambda expressions (ActionListener)
        loginButton.addActionListener(e -> handleLogin());
        clearButton.addActionListener(e -> clearFields());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttons.setBackground(UIStyle.BACKGROUND);
        buttons.add(loginButton);
        buttons.add(clearButton);

        JLabel hint = new JLabel("Default login: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(UIStyle.NEUTRAL);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(UIStyle.BACKGROUND);
        south.add(buttons, BorderLayout.CENTER);
        south.add(hint, BorderLayout.SOUTH);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(loginButton); // pressing Enter clicks Login
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.LABEL_FONT);
        return label;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Basic validation
        if (ValidationUtil.isEmpty(username)) {
            showError("Username cannot be empty.");
            usernameField.requestFocusInWindow();
            return;
        }
        if (ValidationUtil.isEmpty(password)) {
            showError("Password cannot be empty.");
            passwordField.requestFocusInWindow();
            return;
        }

        try {
            String role = userDAO.authenticateUser(username, password);
            if (role != null) {
                dispose();
                new DashboardFrame(username, role).setVisible(true);
            } else {
                showError("Invalid username or password.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
