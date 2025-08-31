package com.booktrack.ui.component;

import com.booktrack.model.AuthResponse;
import com.booktrack.model.User;
import com.booktrack.service.UserService;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modal dialog to edit basic user profile information (name, email, bio).
 */
public class EditProfileDialog extends JDialog {
    private final UserService userService;
    private final SessionManager sessionManager;
    private final Runnable onSavedCallback;
    private final User initialUser;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextArea bioArea;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    public EditProfileDialog(JFrame parent, User user, Runnable onSavedCallback) {
        super(parent, "Edit Profile", true);
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance();
        this.initialUser = user;
        this.onSavedCallback = onSavedCallback;

        initializeComponents();
        populateFields(user);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Update your profile");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(25);
        bioArea = new JTextArea(5, 25);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("First name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(firstNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Last name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(lastNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(emailField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHEAST;
        form.add(new JLabel("Bio:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(new JScrollPane(bioArea), gbc);

        content.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.DARK_GRAY);
        footer.add(statusLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        buttons.add(cancelButton);
        buttons.add(saveButton);
        footer.add(buttons, BorderLayout.EAST);

        content.add(footer, BorderLayout.SOUTH);
        add(content);

    cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(this::handleSave);
        getRootPane().setDefaultButton(saveButton);
    }

    private void populateFields(User user) {
        if (user == null) return;
        firstNameField.setText(nullToEmpty(user.getFirstName()));
        lastNameField.setText(nullToEmpty(user.getLastName()));
        emailField.setText(nullToEmpty(user.getEmail()));
        bioArea.setText(nullToEmpty(user.getBio()));
    }

    private void handleSave(ActionEvent evt) {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String bio = bioArea.getText() != null ? bioArea.getText().trim() : null;

        if (email.isEmpty()) {
            showStatus("Email is required", Color.RED);
            return;
        }

        setBusy(true);
        SwingUtilities.invokeLater(() -> {
            AuthResponse resp = userService.updateUserProfile(initialUser.getUserId(), first, last, email, bio);
            if (resp.isSuccess()) {
                // Refresh session user from DB to keep things in sync
                sessionManager.refreshCurrentUser();
                showStatus("Profile updated.", new Color(0, 128, 0));
                if (onSavedCallback != null) onSavedCallback.run();
                dispose();
            } else {
                showStatus(resp.getMessage(), Color.RED);
                setBusy(false);
            }
        });
    }

    private void setBusy(boolean busy) {
        saveButton.setEnabled(!busy);
        cancelButton.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
