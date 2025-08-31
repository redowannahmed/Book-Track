package com.booktrack.ui.component;

import com.booktrack.dao.UserDAO;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for creating new custom lists
 */
public class CreateCustomListDialog extends JDialog {
    private static final int DIALOG_WIDTH = 450;
    private static final int DIALOG_HEIGHT = 450;
    
    private final Frame parent;
    private final UserDAO userDAO;
    private final Integer userId;
    private final Runnable onListCreatedCallback;
    
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JCheckBox isPublicCheckBox;
    private JButton createButton;
    private JButton cancelButton;
    
    public CreateCustomListDialog(Frame parent, Runnable onListCreatedCallback) {
        super(parent, "Create Custom List", true);
        this.parent = parent;
        this.userDAO = new UserDAO();
        this.userId = SessionManager.getInstance().getCurrentUserId();
        this.onListCreatedCallback = onListCreatedCallback;
        
        if (userId == null) {
            JOptionPane.showMessageDialog(parent, 
                "Please log in to create lists.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        
        initializeDialog();
        createComponents();
        layoutComponents();
        setupEventListeners();
    }
    
    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Set background
        getContentPane().setBackground(new Color(245, 247, 250));
    }
    
    private void createComponents() {
        // Name field
        nameField = new JTextField(20);
        nameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Description area
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Public/Private checkbox
        isPublicCheckBox = new JCheckBox("Make this list public", true);
        isPublicCheckBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        isPublicCheckBox.setBackground(new Color(245, 247, 250));
        isPublicCheckBox.setForeground(new Color(52, 73, 94));
        
        // Buttons
        createButton = new JButton("📚 Create List");
        createButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        createButton.setBackground(new Color(46, 204, 113));
        createButton.setForeground(Color.WHITE);
        createButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        createButton.setFocusPainted(false);
        createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelButton = new JButton("✕ Cancel");
        cancelButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        cancelButton.setBackground(new Color(149, 165, 166));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel headerLabel = new JLabel("📝 Create New List");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Organize your books into custom collections");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titleContainer = new JPanel(new BorderLayout(0, 5));
        titleContainer.setOpaque(false);
        titleContainer.add(headerLabel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main form panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // List name
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("List Name:");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        nameLabel.setForeground(new Color(52, 73, 94));
        mainPanel.add(nameLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(nameField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel descLabel = new JLabel("Description (optional):");
        descLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        descLabel.setForeground(new Color(52, 73, 94));
        mainPanel.add(descLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        mainPanel.add(descScrollPane, gbc);
        
        // Privacy setting
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(isPublicCheckBox, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        // Create button action
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createList();
            }
        });
        
        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Enter key in name field
        nameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isValidInput()) {
                    createList();
                }
            }
        });
        
        // Add hover effects
        createButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                createButton.setBackground(new Color(39, 174, 96));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                createButton.setBackground(new Color(46, 204, 113));
            }
        });
        
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(127, 140, 141));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(149, 165, 166));
            }
        });
    }
    
    private boolean isValidInput() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a name for your list.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        if (name.length() > 100) {
            JOptionPane.showMessageDialog(this,
                "List name cannot exceed 100 characters.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void createList() {
        if (!isValidInput()) {
            return;
        }
        
        String listName = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        int isPublic = isPublicCheckBox.isSelected() ? 1 : 0;
        
        // Disable buttons during creation
        createButton.setEnabled(false);
        cancelButton.setEnabled(false);
        createButton.setText("Creating...");
        
        SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                return userDAO.createCustomList(userId, listName, description, isPublic);
            }
            
            @Override
            protected void done() {
                try {
                    String[] result = get();
                    
                    if ("1".equals(result[0])) {
                        // Success
                        JOptionPane.showMessageDialog(CreateCustomListDialog.this,
                            "List \"" + listName + "\" created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        if (onListCreatedCallback != null) {
                            onListCreatedCallback.run();
                        }
                        
                        dispose();
                    } else {
                        // Error
                        JOptionPane.showMessageDialog(CreateCustomListDialog.this,
                            result[1],
                            "Error Creating List",
                            JOptionPane.ERROR_MESSAGE);
                        
                        // Re-enable buttons
                        createButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        createButton.setText("📚 Create List");
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CreateCustomListDialog.this,
                        "An error occurred while creating the list: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Re-enable buttons
                    createButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    createButton.setText("📚 Create List");
                }
            }
        };
        
        worker.execute();
    }
}
