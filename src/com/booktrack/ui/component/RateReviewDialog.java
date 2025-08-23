package com.booktrack.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for rating and reviewing books
 */
public class RateReviewDialog extends JDialog {
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 600;
    
    private final Frame parent;
    private final String bookTitle;
    private ActionListener onSubmit;
    
    private JSlider ratingSlider;
    private JLabel ratingValueLabel;
    private JTextField reviewTitleField;
    private JTextArea reviewTextArea;
    private JCheckBox isSpoilerCheck;
    private JCheckBox isPublicCheck;
    private JButton submitButton;
    private JButton cancelButton;
    
    private double selectedRating = 0;
    private String reviewTitle = "";
    private String reviewText = "";
    private boolean isSpoiler = false;
    private boolean isPublic = true;
    
    public RateReviewDialog(Frame parent, String bookTitle, ActionListener onSubmit) {
        super(parent, "Rate & Review - " + bookTitle, true);
        this.parent = parent;
        this.bookTitle = bookTitle;
        this.onSubmit = onSubmit;
        
        initializeDialog();
        createComponents();
        layoutComponents();
        attachListeners();
    }
    
    public RateReviewDialog(Frame parent, String bookTitle, double currentRating, 
                           String currentReviewTitle, String currentReviewText, 
                           boolean currentIsSpoiler, boolean currentIsPublic, 
                           ActionListener onSubmit) {
        this(parent, bookTitle, onSubmit);
        
        // Set existing values
        ratingSlider.setValue((int) currentRating);
        ratingValueLabel.setText(String.valueOf((int) currentRating) + " stars");
        selectedRating = currentRating;
        
        if (currentReviewTitle != null) {
            reviewTitleField.setText(currentReviewTitle);
        }
        if (currentReviewText != null) {
            reviewTextArea.setText(currentReviewText);
        }
        isSpoilerCheck.setSelected(currentIsSpoiler);
        isPublicCheck.setSelected(currentIsPublic);
    }
    
    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }
    
    private void createComponents() {
        // Rating components
        ratingSlider = new JSlider(1, 5, 3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setSnapToTicks(true);
        
        ratingValueLabel = new JLabel("3 stars");
        ratingValueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        ratingValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Review components
        reviewTitleField = new JTextField();
        reviewTitleField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        reviewTextArea = new JTextArea(10, 30);
        reviewTextArea.setWrapStyleWord(true);
        reviewTextArea.setLineWrap(true);
        reviewTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        isSpoilerCheck = new JCheckBox("Contains spoilers");
        isPublicCheck = new JCheckBox("Make review public", true);
        
        // Action buttons
        submitButton = new JButton("Submit");
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        submitButton.setBackground(new Color(0, 123, 255));
        submitButton.setForeground(Color.WHITE);
        submitButton.setPreferredSize(new Dimension(100, 35));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("Rate & Review: " + truncateTitle(bookTitle, 40));
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Rating section
        JPanel ratingPanel = new JPanel(new BorderLayout(5, 5));
        ratingPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Rating", 
            0, 0, 
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        JLabel ratingLabel = new JLabel("How would you rate this book?");
        ratingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(ratingSlider, BorderLayout.CENTER);
        sliderPanel.add(ratingValueLabel, BorderLayout.SOUTH);
        
        ratingPanel.add(ratingLabel, BorderLayout.NORTH);
        ratingPanel.add(sliderPanel, BorderLayout.CENTER);
        
        // Review section
        JPanel reviewPanel = new JPanel(new BorderLayout(5, 5));
        reviewPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Review (Optional)", 
            0, 0, 
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        JLabel titleLabel = new JLabel("Review Title:");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JLabel textLabel = new JLabel("Your Review:");
        textLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JScrollPane textScrollPane = new JScrollPane(reviewTextArea);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textScrollPane.setPreferredSize(new Dimension(400, 200));
        
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.add(isSpoilerCheck);
        checkboxPanel.add(isPublicCheck);
        
        JPanel reviewContentPanel = new JPanel(new BorderLayout(5, 5));
        reviewContentPanel.add(titleLabel, BorderLayout.NORTH);
        reviewContentPanel.add(reviewTitleField, BorderLayout.CENTER);
        
        JPanel reviewTextPanel = new JPanel(new BorderLayout(5, 5));
        reviewTextPanel.add(textLabel, BorderLayout.NORTH);
        reviewTextPanel.add(textScrollPane, BorderLayout.CENTER);
        reviewTextPanel.add(checkboxPanel, BorderLayout.SOUTH);
        
        reviewPanel.add(reviewContentPanel, BorderLayout.NORTH);
        reviewPanel.add(reviewTextPanel, BorderLayout.CENTER);
        
        contentPanel.add(ratingPanel, BorderLayout.NORTH);
        contentPanel.add(reviewPanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void attachListeners() {
        ratingSlider.addChangeListener(e -> {
            int value = ratingSlider.getValue();
            ratingValueLabel.setText(value + " star" + (value != 1 ? "s" : ""));
            selectedRating = value;
        });
        
        submitButton.addActionListener(e -> handleSubmit());
        cancelButton.addActionListener(e -> dispose());
        
        // Enter key support
        reviewTitleField.addActionListener(e -> reviewTextArea.requestFocus());
    }
    
    private void handleSubmit() {
        reviewTitle = reviewTitleField.getText().trim();
        reviewText = reviewTextArea.getText().trim();
        isSpoiler = isSpoilerCheck.isSelected();
        isPublic = isPublicCheck.isSelected();
        
        // Validate rating
        if (selectedRating < 1 || selectedRating > 5) {
            JOptionPane.showMessageDialog(this, 
                "Please select a rating between 1 and 5 stars.", 
                "Invalid Rating", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // If there's review text, validate it
        if (!reviewText.isEmpty() && reviewText.length() < 10) {
            JOptionPane.showMessageDialog(this, 
                "Review text must be at least 10 characters long or leave it empty.", 
                "Invalid Review", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Call the submit handler
        if (onSubmit != null) {
            onSubmit.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "submit"));
        }
        
        dispose();
    }
    
    private String truncateTitle(String title, int maxLength) {
        if (title == null) return "";
        if (title.length() <= maxLength) return title;
        return title.substring(0, maxLength - 3) + "...";
    }
    
    // Getters for the form data
    public double getSelectedRating() {
        return selectedRating;
    }
    
    public String getReviewTitle() {
        return reviewTitle;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public boolean isSpoiler() {
        return isSpoiler;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    /**
     * Add a submit listener
     * @param listener The action listener to handle submit events
     */
    public void addSubmitListener(ActionListener listener) {
        this.onSubmit = listener;
    }
}
