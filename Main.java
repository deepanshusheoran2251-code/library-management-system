package library;

import library.db.DatabaseManager;
import library.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point – Library Management System
 * Group 1 | CSE2003 | B.Tech CSE Semester 3 Mini Project
 */
public class Main {

    public static void main(String[] args) {
        // Set system Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager db = DatabaseManager.getInstance();
                new MainFrame(db);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Failed to start application:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
