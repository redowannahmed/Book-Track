package com.booktrack.ui.component;

import com.booktrack.model.Book;
import com.booktrack.service.BookService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * RatingReviewDialog - Dialog for rating and reviewing a book before adding to "Have Read" list
 */
public class RatingReviewDialog extends JDialog {
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 400;
    
    private final Book book;
    private final BookService bookService;
    private final Runnable onSuccess;
    
    private JPanel starPanel;
    private JTextArea reviewTextArea;
    private JButton submitButton;
    private JButton cancelButton;
    private JCheckBox makePublicCheckbox;
    
    private int selectedRating = 0;
    private JLabel[] stars;
    
    public RatingReviewDialog(Frame parent, Book book, BookService bookService, Runnable onSuccess) {
        super(parent, "Rate and Review Book", true);
        this.book = book;
        this.bookService = bookService;
        this.onSuccess = onSuccess;
        
        initializeDialog();
        createComponents();
    }
    
    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Set background
        getContentPane().setBackground(Color.WHITE);
    }
    
    private void createComponents() {
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Rate and Review", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(51, 51, 51));
        
        JLabel bookLabel = new JLabel("\"" + book.getTitle() + "\"", JLabel.CENTER);
        bookLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        bookLabel.setForeground(new Color(102, 102, 102));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(bookLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Rating section
        JPanel ratingSection = createRatingSection();
        panel.add(ratingSection, BorderLayout.NORTH);
        
        // Review section
        JPanel reviewSection = createReviewSection();
        panel.add(reviewSection, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRatingSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBackground(Color.WHITE);
        
        JLabel ratingLabel = new JLabel("Your Rating (Required):");
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ratingLabel.setForeground(new Color(51, 51, 51));
        
        starPanel = createStarPanel();
        
        panel.add(ratingLabel, BorderLayout.NORTH);
        panel.add(starPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        panel.setBackground(Color.WHITE);
        
        stars = new JLabel[5];
        
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i] = new JLabel("☆");
            stars[i].setFont(new Font("Arial", Font.PLAIN, 30));
            stars[i].setForeground(new Color(255, 193, 7)); // Gold color
            stars[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Add mouse listeners for interactive rating
            stars[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    setRating(rating);
                }
                
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    highlightStars(rating);
                }
            });
            
            panel.add(stars[i]);
        }
        
        // Add mouse listener to panel to reset highlighting
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                updateStarDisplay();
            }
        });
        
        return panel;
    }
    
    private void setRating(int rating) {
        this.selectedRating = rating;
        updateStarDisplay();
        submitButton.setEnabled(true); // Enable submit button when rating is selected
    }
    
    private void highlightStars(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setText("★");
                stars[i].setForeground(new Color(255, 193, 7)); // Gold
            } else {
                stars[i].setText("☆");
                stars[i].setForeground(new Color(200, 200, 200)); // Light gray
            }
        }
    }
    
    private void updateStarDisplay() {
        for (int i = 0; i < stars.length; i++) {
            if (i < selectedRating) {
                stars[i].setText("★");
                stars[i].setForeground(new Color(255, 193, 7)); // Gold
            } else {
                stars[i].setText("☆");
                stars[i].setForeground(new Color(200, 200, 200)); // Light gray
            }
        }
    }
    
    private JPanel createReviewSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBackground(Color.WHITE);
        
        JLabel reviewLabel = new JLabel("Your Review (Optional):");
        reviewLabel.setFont(new Font("Arial", Font.BOLD, 14));
        reviewLabel.setForeground(new Color(51, 51, 51));
        
        reviewTextArea = new JTextArea(8, 30);
        reviewTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        reviewTextArea.setLineWrap(true);
        reviewTextArea.setWrapStyleWord(true);
        reviewTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reviewTextArea.setBackground(new Color(248, 249, 250));
        
        JScrollPane scrollPane = new JScrollPane(reviewTextArea);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        makePublicCheckbox = new JCheckBox("Make this review public");
        makePublicCheckbox.setBackground(Color.WHITE);
        makePublicCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        makePublicCheckbox.setSelected(true); // Default to public
        
        panel.add(reviewLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(makePublicCheckbox, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(Color.WHITE);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose());
        
        submitButton = new JButton("Add to Have Read");
        submitButton.setFont(new Font("Arial", Font.BOLD, 12));
        submitButton.setPreferredSize(new Dimension(140, 35));
        submitButton.setBackground(new Color(40, 167, 69));
        submitButton.setForeground(Color.WHITE);
        submitButton.setEnabled(false); // Disabled until rating is selected
        submitButton.addActionListener(this::handleSubmit);
        
        panel.add(cancelButton);
        panel.add(submitButton);
        
        return panel;
    }
    
    private void handleSubmit(ActionEvent e) {
        if (selectedRating == 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a rating before submitting.",
                "Rating Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show progress dialog
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Submitting rating and review...");
        
        JDialog progressDialog = new JDialog(this, "Processing", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 80);
        progressDialog.setLocationRelativeTo(this);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String reviewText = reviewTextArea.getText().trim();
                boolean isPublic = makePublicCheckbox.isSelected();
                
                // Submit rating and review
                return bookService.rateAndReviewBook(book, selectedRating, 
                    reviewText.isEmpty() ? null : reviewText, isPublic);
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    boolean success = get();
                    if (success) {
                        dispose(); // Close this dialog
                        if (onSuccess != null) {
                            onSuccess.run(); // Execute the callback to add to Have Read list
                        }
                    } else {
                        String errorMessage = bookService.getLastMessage();
                        JOptionPane.showMessageDialog(RatingReviewDialog.this,
                            errorMessage != null ? errorMessage : "Failed to submit rating and review. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RatingReviewDialog.this,
                        "An error occurred: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
}
