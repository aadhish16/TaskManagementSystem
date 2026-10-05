package com.taskmanagement;

import com.taskmanagement.gui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Entry point of the Task Management System.
 * It only sets the look and feel and opens the login window.
 */
public class Main {

    public static void main(String[] args) {
        // Use plain (non-bold) fonts with the cross-platform "Metal" look and feel
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set look and feel, using default. Reason: " + e.getMessage());
        }

        // Swing components must be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
