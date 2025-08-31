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
        // Create components with larger sizes
        usernameField = new JTextField(25);
        emailField = new JTextField(25);
        passwordField = new JPasswordField(25);
        confirmPasswordField = new JPasswordField(25);
        firstNameField = new JTextField(25);
        lastNameField = new JTextField(25);
        registerButton = new JButton("Create Account");
        backToLoginButton = new JButton("Back to Login");
        statusLabel = new JLabel(" ");
        passwordStrengthLabel = new JLabel(" ");
        showPasswordCheckBox = new JCheckBox("Show Passwords");
        
        // Style text fields
        Dimension fieldSize = new Dimension(320, 40);
        Font fieldFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        Color fieldBg = new Color(248, 249, 250);
        
        JTextField[] fields = {usernameField, emailField, firstNameField, lastNameField};
        JPasswordField[] passFields = {passwordField, confirmPasswordField};
        
        for (JTextField field : fields) {
            field.setPreferredSize(fieldSize);
            field.setFont(fieldFont);
            field.setBackground(fieldBg);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
        }
        
        for (JPasswordField field : passFields) {
            field.setPreferredSize(fieldSize);
            field.setFont(fieldFont);
            field.setBackground(fieldBg);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
        }
        
        // Set tooltips
        usernameField.setToolTipText("3-50 characters, letters, numbers, and underscores only");
        emailField.setToolTipText("Enter a valid email address");
        passwordField.setToolTipText("At least 8 characters with uppercase, lowercase, number, and special character");
        confirmPasswordField.setToolTipText("Confirm your password");
        firstNameField.setToolTipText("Your first name (optional)");
        lastNameField.setToolTipText("Your last name (optional)");
        
        // Style buttons
        registerButton.setPreferredSize(new Dimension(160, 40));
        registerButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        backToLoginButton.setPreferredSize(new Dimension(160, 40));
        backToLoginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        backToLoginButton.setBackground(new Color(149, 165, 166));
        backToLoginButton.setForeground(Color.WHITE);
        backToLoginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Style checkbox
        showPasswordCheckBox.setBackground(new Color(245, 247, 250));
        showPasswordCheckBox.setForeground(new Color(52, 73, 94));
        showPasswordCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Set label properties
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        passwordStrengthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordStrengthLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        
        // Add hover effects
        addButtonHoverEffect(registerButton, new Color(46, 204, 113), new Color(39, 174, 96));
        addButtonHoverEffect(backToLoginButton, new Color(149, 165, 166), new Color(127, 140, 141));
        
        // Make register button default
        getRootPane().setDefaultButton(registerButton);
    }
    
    private void addButtonHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));
        
        // Create main panel with better spacing
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        mainPanel.setBackground(new Color(245, 247, 250));
        
        // Header panel with gradient background
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(46, 204, 113),
                    0, getHeight(), new Color(39, 174, 96)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        headerPanel.setPreferredSize(new Dimension(0, 100));
        
        // Title
        JLabel titleLabel = new JLabel("✨ Join BookTrack");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Start your reading journey today");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Create Your Account");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel instructionLabel = new JLabel("Fill in the information below to get started");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        instructionLabel.setForeground(new Color(108, 117, 125));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        formPanel.add(welcomeLabel);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(instructionLabel);
        formPanel.add(Box.createVerticalStrut(18));
        
        // Create two-column layout for name fields
        JPanel namePanel = createTwoColumnPanel("First Name", firstNameField, "Last Name", lastNameField);
        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Username field
        addFieldToPanel(formPanel, "Username *", usernameField);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Email field  
        addFieldToPanel(formPanel, "Email Address *", emailField);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Password field
        addFieldToPanel(formPanel, "Password *", passwordField);
        
        // Password strength indicator
        passwordStrengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(passwordStrengthLabel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Confirm password field
        addFieldToPanel(formPanel, "Confirm Password *", confirmPasswordField);
        formPanel.add(Box.createVerticalStrut(8));
        
        // Show password checkbox
        showPasswordCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(showPasswordCheckBox);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Required fields note
        JLabel requiredLabel = new JLabel("* Required fields");
        requiredLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        requiredLabel.setForeground(new Color(108, 117, 125));
        requiredLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(requiredLabel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(backToLoginButton);
        
        formPanel.add(buttonPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(statusLabel);
        formPanel.add(statusPanel);
        
        // Add to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Set window size - increased height significantly to ensure all components are visible
        setPreferredSize(new Dimension(520, 850));
        pack();
    }
    
    private JPanel createTwoColumnPanel(String label1, JTextField field1, String label2, JTextField field2) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 8));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(400, 90));
        
        JLabel lbl1 = new JLabel(label1);
        lbl1.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        lbl1.setForeground(new Color(52, 73, 94));
        
        JLabel lbl2 = new JLabel(label2);
        lbl2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        lbl2.setForeground(new Color(52, 73, 94));
        
        // Adjust field sizes for two columns
        field1.setPreferredSize(new Dimension(145, 40));
        field1.setMaximumSize(new Dimension(145, 40));
        field2.setPreferredSize(new Dimension(145, 40));
        field2.setMaximumSize(new Dimension(145, 40));
        
        panel.add(lbl1);
        panel.add(lbl2);
        panel.add(field1);
        panel.add(field2);
        
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(panel);
        wrapper.add(Box.createHorizontalGlue());
        
        return wrapper;
    }
    
    private void addFieldToPanel(JPanel parent, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setForeground(new Color(52, 73, 94));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(field.getPreferredSize());
        
        parent.add(label);
        parent.add(Box.createVerticalStrut(8));
        parent.add(field);
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
