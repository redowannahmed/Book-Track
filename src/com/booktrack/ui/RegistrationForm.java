package com.booktrack.ui;

import com.booktrack.model.AuthResponse;
import com.booktrack.service.UserService;
import com.booktrack.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Registration Form GUI
 * Handles user registration interface
 */
public class RegistrationForm extends JFrame {
    private final UserService userService;
    private final LoginForm parentLoginForm;
    
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel statusLabel;
    private JLabel passwordStrengthLabel;
    private JCheckBox showPasswordCheckBox;
    
    public RegistrationForm(LoginForm parentLoginForm) {
        this.userService = new UserService();
        this.parentLoginForm = parentLoginForm;
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("BookTrack - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parentLoginForm);
    }
    
    private void initializeComponents() {
        // Create components
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        registerButton = new JButton("Register");
        backToLoginButton = new JButton("Back to Login");
        statusLabel = new JLabel(" ");
        passwordStrengthLabel = new JLabel(" ");
        showPasswordCheckBox = new JCheckBox("Show Passwords");
        
        // Set component properties
        usernameField.setToolTipText("3-50 characters, letters, numbers, and underscores only");
        emailField.setToolTipText("Enter a valid email address");
        passwordField.setToolTipText("At least 8 characters with uppercase, lowercase, number, and special character");
        confirmPasswordField.setToolTipText("Confirm your password");
        firstNameField.setToolTipText("Your first name (optional)");
        lastNameField.setToolTipText("Your last name (optional)");
        
        // Set label properties
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Set button properties
        registerButton.setPreferredSize(new Dimension(120, 30));
        backToLoginButton.setPreferredSize(new Dimension(120, 30));
        
        // Make register button default
        getRootPane().setDefaultButton(registerButton);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(41, 128, 185));
        titlePanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registration Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Username: *"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);
        
        // Email field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email: *"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(emailField, gbc);
        
        // First name field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(firstNameField, gbc);
        
        // Last name field
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(lastNameField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password: *"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);
        
        // Password strength label
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordStrengthLabel, gbc);
        
        // Confirm password field
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Confirm Password: *"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(confirmPasswordField, gbc);
        
        // Show password checkbox
        gbc.gridx = 1; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(showPasswordCheckBox, gbc);
        
        // Required fields note
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel requiredLabel = new JLabel("* Required fields");
        requiredLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        requiredLabel.setForeground(Color.GRAY);
        formPanel.add(requiredLabel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(registerButton);
        buttonPanel.add(backToLoginButton);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel);
        
        // Add to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventListeners() {
        // Register button action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration();
            }
        });
        
        // Back to login button action
        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToLogin();
            }
        });
        
        // Show password checkbox
        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckBox.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                    confirmPasswordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('*');
                    confirmPasswordField.setEchoChar('*');
                }
            }
        });
        
        // Password field focus listener for strength validation
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validatePasswordStrength();
            }
        });
        
        // Username field focus listener for availability check
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkUsernameAvailability();
            }
        });
        
        // Email field focus listener for availability check
        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkEmailAvailability();
            }
        });
    }
    
    private void performRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        
        // Clear previous status
        showStatus("", Color.BLACK);
        
        // Basic validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showStatus("Please fill in all required fields", Color.RED);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showStatus("Passwords do not match", Color.RED);
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }
        
        // Disable button and show loading
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");
        showStatus("Creating account...", Color.BLUE);
        
        // Perform registration in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                AuthResponse response = userService.registerUser(username, email, password, 
                    firstName.isEmpty() ? null : firstName, 
                    lastName.isEmpty() ? null : lastName);
                
                SwingUtilities.invokeLater(() -> {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                    
                    if (response.isSuccess()) {
                        showStatus("Registration successful! Redirecting to login...", Color.GREEN);
                        
                        // Clear password fields
                        passwordField.setText("");
                        confirmPasswordField.setText("");
                        
                        // Redirect to login after a short delay
                        Timer timer = new Timer(2000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                goBackToLoginWithUsername(username);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        showStatus(response.getMessage(), Color.RED);
                        passwordField.setText("");
                        confirmPasswordField.setText("");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                    showStatus("Registration failed: " + ex.getMessage(), Color.RED);
                    passwordField.setText("");
                    confirmPasswordField.setText("");
                });
            }
        });
    }
    
    private void validatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        if (!password.isEmpty()) {
            PasswordUtil.ValidationResult result = userService.validatePassword(password);
            if (result.isValid()) {
                passwordStrengthLabel.setText("Password strength: Strong");
                passwordStrengthLabel.setForeground(new Color(34, 139, 34));
            } else {
                passwordStrengthLabel.setText("Password strength: " + result.getMessage());
                passwordStrengthLabel.setForeground(Color.RED);
            }
        } else {
            passwordStrengthLabel.setText(" ");
        }
    }
    
    private void checkUsernameAvailability() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                boolean available = userService.isUsernameAvailable(username, null);
                if (!available) {
                    showStatus("Username is not available", Color.RED);
                } else if (username.length() >= 3) {
                    showStatus("Username is available", new Color(34, 139, 34));
                }
            });
        }
    }
    
    private void checkEmailAvailability() {
        String email = emailField.getText().trim();
        if (!email.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                boolean available = userService.isEmailAvailable(email, null);
                if (!available) {
                    showStatus("Email is already registered", Color.RED);
                } else if (email.contains("@")) {
                    showStatus("Email is available", new Color(34, 139, 34));
                }
            });
        }
    }
    
    private void goBackToLogin() {
        parentLoginForm.setVisible(true);
        this.dispose();
    }
    
    private void goBackToLoginWithUsername(String username) {
        parentLoginForm.showWithUsername(username);
        this.dispose();
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        
        // Auto-clear status after 5 seconds if it's not an error
        if (!color.equals(Color.RED)) {
            Timer timer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    statusLabel.setText(" ");
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
}
