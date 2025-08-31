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
        // Create components with larger sizes
        usernameField = new JTextField(25);
        passwordField = new JPasswordField(25);
        loginButton = new JButton("Login");
        registerButton = new JButton("Create Account");
        statusLabel = new JLabel(" ");
        showPasswordCheckBox = new JCheckBox("Show Password");
        
        // Style text fields
        usernameField.setPreferredSize(new Dimension(320, 45));
        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        usernameField.setBackground(new Color(248, 249, 250));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        usernameField.setToolTipText("Enter your username");
        
        passwordField.setPreferredSize(new Dimension(320, 45));
        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        passwordField.setBackground(new Color(248, 249, 250));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        passwordField.setToolTipText("Enter your password");
        
        // Style buttons
        loginButton.setPreferredSize(new Dimension(150, 45));
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        loginButton.setBackground(new Color(52, 73, 94));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        registerButton.setPreferredSize(new Dimension(150, 45));
        registerButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Style checkbox
        showPasswordCheckBox.setBackground(new Color(245, 247, 250));
        showPasswordCheckBox.setForeground(new Color(52, 73, 94));
        showPasswordCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Set status label properties
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Add hover effects
        addButtonHoverEffect(loginButton, new Color(52, 73, 94), new Color(44, 62, 80));
        addButtonHoverEffect(registerButton, new Color(46, 204, 113), new Color(39, 174, 96));
        
        // Make login button default
        getRootPane().setDefaultButton(loginButton);
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
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(245, 247, 250));
        
        // Header panel with gradient background
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(52, 73, 94),
                    0, getHeight(), new Color(44, 62, 80)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        headerPanel.setPreferredSize(new Dimension(0, 120));
        
        // Title and subtitle
        JLabel titleLabel = new JLabel("📚 BookTrack");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Track Your Reading Journey");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel instructionLabel = new JLabel("Please sign in to your account");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(108, 117, 125));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        formPanel.add(welcomeLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(instructionLabel);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        usernameLabel.setForeground(new Color(52, 73, 94));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setMaximumSize(usernameField.getPreferredSize());
        
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        passwordLabel.setForeground(new Color(52, 73, 94));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setMaximumSize(passwordField.getPreferredSize());
        
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Show password checkbox
        showPasswordCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(showPasswordCheckBox);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        formPanel.add(buttonPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(statusLabel);
        formPanel.add(statusPanel);
        
        // Add to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Set window size - increased height to ensure all components are visible
        setPreferredSize(new Dimension(500, 750));
        pack();
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
