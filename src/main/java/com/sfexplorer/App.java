package com.sfexplorer;

import com.sfexplorer.db.Database;
import com.sfexplorer.ui.LoginDialog;
import com.sfexplorer.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        EventQueue.invokeLater(() -> {
            try {
                Database db = new Database();
                db.init();

                LoginDialog login = new LoginDialog(null, db);
                login.setVisible(true);

                if (!login.isAuthenticated()) {
                    // user closed dialog / failed auth
                    System.exit(0);
                }

                MainFrame frame = new MainFrame(db, login.getAuthenticatedUser());
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start SF Explorer:\n" + ex.getMessage(),
                        "SF Explorer - Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
