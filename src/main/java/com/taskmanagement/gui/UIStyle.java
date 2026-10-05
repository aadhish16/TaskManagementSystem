package com.taskmanagement.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Shared colors, fonts and a button factory so every screen looks consistent
 * (avoids repeating the same styling code in each GUI class).
 */
public final class UIStyle {

    public static final Color PRIMARY = new Color(37, 99, 235);      // blue
    public static final Color PRIMARY_DARK = new Color(30, 41, 59);  // dark slate
    public static final Color SUCCESS = new Color(22, 163, 74);      // green
    public static final Color WARNING = new Color(217, 119, 6);      // amber
    public static final Color DANGER = new Color(220, 38, 38);       // red
    public static final Color NEUTRAL = new Color(100, 116, 139);    // grey
    public static final Color BACKGROUND = new Color(241, 245, 249); // light grey
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(203, 213, 225);

    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    public static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 15);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    private UIStyle() {
    }

    /** Creates a flat, colored button. */
    public static JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 36));
        return button;
    }
}
