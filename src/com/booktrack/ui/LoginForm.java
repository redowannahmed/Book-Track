package com.booktrack.ui;

import com.booktrack.model.AuthResponse;
import com.booktrack.model.User;
import com.booktrack.service.UserService;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login Form GUI
 * Handles user authentication interface
 */
public class LoginForm extends JFrame {
    private final UserService userService;
    private final SessionManager sessionManager;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    
    public LoginForm() {
        this.userService = new UserService();
        this.sessionManager = SessionManager.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("BookTrack - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Create components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        statusLabel = new JLabel(" ");
        showPasswordCheckBox = new JCheckBox("Show Password");
        
        // Set component properties
        usernameField.setToolTipText("Enter your username");
        passwordField.setToolTipText("Enter your password");
        
        // Set status label properties
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Set button properties
        loginButton.setPreferredSize(new Dimension(100, 30));
        registerButton.setPreferredSize(new Dimension(100, 30));
        
        // Make login button default
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("BookTrack");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(41, 128, 185));
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Track Your Reading Journey");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.add(titlePanel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Login to Your Account"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);
        
        // Show password checkbox
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(showPasswordCheckBox, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.add(statusLabel);
        
        // Add to main panel
        mainPanel.add(titleContainer, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventListeners() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Register button action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationForm();
            }
        });
        
        // Show password checkbox
        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckBox.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('*');
                }
            }
        });
        
        // Enter key listener for password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        
        // Enter key listener for username field
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password", Color.RED);
            return;
        }
        
        // Disable button and show loading
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        showStatus("Authenticating...", Color.BLUE);
        
        // Perform login in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                AuthResponse response = userService.loginUser(username, password);
                
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    
                    if (response.isSuccess()) {
                        sessionManager.login(response.getUser());
                        showStatus("Login successful! Welcome " + response.getUser().getDisplayName(), Color.GREEN);
                        
                        // Clear password field
                        passwordField.setText("");
                        
                        // Open main application window
                        SwingUtilities.invokeLater(() -> {
                            openMainApplication();
                        });
                    } else {
                        showStatus(response.getMessage(), Color.RED);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    showStatus("Login failed: " + ex.getMessage(), Color.RED);
                    passwordField.setText("");
                });
            }
        });
    }
    
    private void openRegistrationForm() {
        RegistrationForm registrationForm = new RegistrationForm(this);
        registrationForm.setVisible(true);
        this.setVisible(false);
    }
    
    private void openMainApplication() {
        // Get the logged-in user from session manager
        User loggedInUser = sessionManager.getCurrentUser();
        if (loggedInUser != null) {
            // Close this login window
            this.dispose();
            
            // Open the landing page
            SwingUtilities.invokeLater(() -> {
                new LandingPage(loggedInUser);
            });
        } else {
            JOptionPane.showMessageDialog(this, 
                "Error: Unable to retrieve user information", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
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
    
    /**
     * Show login form with a pre-filled username (after registration)
     * @param username Username to pre-fill
     */
    public void showWithUsername(String username) {
        usernameField.setText(username);
        passwordField.requestFocus();
        showStatus("Registration successful! Please login with your credentials.", Color.GREEN);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
