package com.sfexplorer.ui;

import com.sfexplorer.db.Database;
import com.sfexplorer.db.UserRepository;
import com.sfexplorer.security.PasswordHasher;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final Database db;
    private final UserRepository users;

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    private final JLabel status = new JLabel(" ");

    private boolean authenticated = false;
    private String authenticatedUser = null;

    public LoginDialog(Frame owner, Database db) throws Exception {
        super(owner, "SF Explorer Login", true);
        this.db = db;
        this.users = new UserRepository(db);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(420, 240);
        setLocationRelativeTo(owner);

        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        boolean firstRun = !users.anyUsersExist();
        status.setText(firstRun
                ? "First run: create an account (Register)."
                : "Enter username and password.");

        if (firstRun) usernameField.setText("student");
    }

    private JComponent buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        p.add(new JLabel("Username:"), gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        p.add(usernameField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        p.add(new JLabel("Password:"), gc);

        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1;
        p.add(passwordField, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        status.setFont(status.getFont().deriveFont(12f));
        p.add(status, gc);

        return p;
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        JButton cancel = new JButton("Cancel");

        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());
        cancel.addActionListener(e -> dispose());

        p.add(register);
        p.add(login);
        p.add(cancel);

        getRootPane().setDefaultButton(login);
        return p;
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();

        if (username.isBlank() || pass.length == 0) {
            status.setText("Username and password are required.");
            return;
        }

        try {
            if (users.userExists(username)) {
                status.setText("User already exists. Try Login.");
                return;
            }

            PasswordHasher.HashResult hr = PasswordHasher.hash(pass);
            users.createUser(username, hr.saltB64(), hr.hashB64(), hr.iterations());

            status.setText("Registered! Now click Login.");
        } catch (Exception ex) {
            status.setText("Register failed: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(pass, '\0');
        }
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();

        if (username.isBlank() || pass.length == 0) {
            status.setText("Username and password are required.");
            return;
        }

        try {
            UserRepository.UserRecord ur = users.getUser(username);
            if (ur == null) {
                status.setText("No such user. Click Register.");
                return;
            }

            boolean ok = PasswordHasher.verify(pass, ur.saltB64(), ur.hashB64(), ur.iterations());
            if (!ok) {
                status.setText("Wrong password.");
                return;
            }

            authenticated = true;
            authenticatedUser = username;
            dispose();
        } catch (Exception ex) {
            status.setText("Login failed: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(pass, '\0');
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }
}
