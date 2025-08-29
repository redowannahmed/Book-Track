package com.booktrack.ui.component;

import com.booktrack.model.Book;
import com.booktrack.service.BookService;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import javax.imageio.ImageIO;

/**
 * BookDetailsDialog - Shows comprehensive book details when a book is clicked
 * This dialog displays all available information about a book including:
 * - Cover image, title, subtitle, authors
 * - Publication details, publisher, page count
 * - Full description/synopsis
 * - Categories/genres, language, ISBN
 * - Rating information
 * - Action buttons to add to lists, rate, review
 */
public class BookDetailsDialog extends JDialog {
    private static final int DIALOG_WIDTH = 700;
    private static final int DIALOG_HEIGHT = 600;
    private static final int COVER_WIDTH = 150;
    private static final int COVER_HEIGHT = 220;
    
    private final Book book;
    private final BookService bookService;
    private double[] bookRatingStats; // [avg_rating, total_ratings, 5_star, 4_star, 3_star, 2_star, 1_star]
    private int reviewCount;
    private JLabel coverImageLabel;
    private JPanel actionsPanel;
    
    public BookDetailsDialog(Frame parent, Book book) {
        super(parent, "Book Details", true);
        this.book = book;
        this.bookService = new BookService();
        
        // Load BookTrack user ratings and reviews
        loadBookTrackRatings();
        
        initializeDialog();
        createComponents();
        loadCoverImage();
    }
    
    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Set a nice background color
        getContentPane().setBackground(Color.WHITE);
    }
    
    private void createComponents() {
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Left panel - Cover image
        JPanel leftPanel = createCoverPanel();
        
        // Right panel - Book details
        JPanel rightPanel = createDetailsPanel();
        
        // Bottom panel - Action buttons
        JPanel bottomPanel = createActionsPanel();
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createCoverPanel() {
        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setBackground(Color.WHITE);
        
        // Cover image
        coverImageLabel = new JLabel();
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        coverImageLabel.setPreferredSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
        coverImageLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        coverImageLabel.setBackground(Color.LIGHT_GRAY);
        coverImageLabel.setOpaque(true);
        
        // Set placeholder initially
        setPlaceholderImage();
        
        coverPanel.add(coverImageLabel, BorderLayout.CENTER);
        
        return coverPanel;
    }
    
    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBackground(Color.WHITE);
        
        // Top section - Title and basic info
        JPanel topSection = createTopSection();
        
        // Middle section - Description
        JPanel middleSection = createDescriptionSection();
        
        // Bottom section - Additional details
        JPanel bottomSection = createAdditionalDetailsSection();
        
        detailsPanel.add(topSection, BorderLayout.NORTH);
        detailsPanel.add(middleSection, BorderLayout.CENTER);
        detailsPanel.add(bottomSection, BorderLayout.SOUTH);
        
        return detailsPanel;
    }
    
    private JPanel createTopSection() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);
        
        // Title and subtitle
        JPanel titlePanel = new JPanel(new BorderLayout(5, 2));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("<html><b>" + (book.getTitle() != null ? book.getTitle() : "Unknown Title") + "</b></html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        
        if (book.getSubtitle() != null && !book.getSubtitle().trim().isEmpty()) {
            JLabel subtitleLabel = new JLabel(book.getSubtitle());
            subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            subtitleLabel.setForeground(new Color(108, 117, 125));
            titlePanel.add(subtitleLabel, BorderLayout.CENTER);
        }
        
        // Authors
        JLabel authorsLabel = new JLabel("<html><i>by " + book.getAuthorsAsString() + "</i></html>");
        authorsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        authorsLabel.setForeground(new Color(40, 167, 69));
        
        // Rating and basic info
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        infoPanel.setBackground(Color.WHITE);
        
        // Rating - Use BookTrack user ratings instead of Google API ratings
        String ratingText = formatRating(bookRatingStats[0], (int)bookRatingStats[1]);
        JLabel ratingLabel = new JLabel(ratingText);
        ratingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        ratingLabel.setForeground(new Color(255, 193, 7));
        infoPanel.add(ratingLabel);
        
        // Publisher and publication date
        if (book.getPublisher() != null || book.getPublishedDate() != null) {
            StringBuilder pubInfo = new StringBuilder();
            if (book.getPublisher() != null) {
                pubInfo.append("Published by ").append(book.getPublisher());
            }
            if (book.getPublishedDate() != null) {
                if (pubInfo.length() > 0) pubInfo.append(" • ");
                pubInfo.append(book.getPublishedDate());
            }
            
            JLabel pubLabel = new JLabel(pubInfo.toString());
            pubLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            pubLabel.setForeground(new Color(108, 117, 125));
            infoPanel.add(pubLabel);
        }
        
        // Page count and language
        StringBuilder additionalInfo = new StringBuilder();
        if (book.getPageCount() > 0) {
            additionalInfo.append(book.getPageCount()).append(" pages");
        }
        if (book.getLanguage() != null) {
            if (additionalInfo.length() > 0) additionalInfo.append(" • ");
            additionalInfo.append("Language: ").append(book.getLanguage().toUpperCase());
        }
        
        if (additionalInfo.length() > 0) {
            JLabel additionalLabel = new JLabel(additionalInfo.toString());
            additionalLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            additionalLabel.setForeground(new Color(108, 117, 125));
            infoPanel.add(additionalLabel);
        }
        
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(authorsLabel, BorderLayout.CENTER);
        topPanel.add(infoPanel, BorderLayout.SOUTH);
        
        return topPanel;
    }
    
    private JPanel createDescriptionSection() {
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBackground(Color.WHITE);
        descPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            "Description",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 14),
            new Color(33, 37, 41)
        ));
        
        String description = book.getDescription();
        if (description == null || description.trim().isEmpty()) {
            description = "No description available for this book.";
        }
        
        JTextArea descArea = new JTextArea(description);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        descArea.setForeground(new Color(33, 37, 41));
        descArea.setBackground(Color.WHITE);
        descArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(descArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(450, 150));
        scrollPane.setBorder(null);
        
        descPanel.add(scrollPane, BorderLayout.CENTER);
        
        return descPanel;
    }
    
    private JPanel createAdditionalDetailsSection() {
        JPanel additionalPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        additionalPanel.setBackground(Color.WHITE);
        additionalPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            "Additional Information",
            0, 0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            new Color(33, 37, 41)
        ));
        
        // Categories/Genres
        if (book.getCategories() != null && book.getCategories().length > 0) {
            additionalPanel.add(createDetailLabel("Categories:", book.getCategoriesAsString()));
        }
        
        // ISBN information
        if (book.getIsbn10() != null || book.getIsbn13() != null) {
            StringBuilder isbnInfo = new StringBuilder();
            if (book.getIsbn13() != null) {
                isbnInfo.append("ISBN-13: ").append(book.getIsbn13());
            }
            if (book.getIsbn10() != null) {
                if (isbnInfo.length() > 0) isbnInfo.append("\n");
                isbnInfo.append("ISBN-10: ").append(book.getIsbn10());
            }
            
            JPanel isbnPanel = new JPanel(new BorderLayout());
            isbnPanel.setBackground(Color.WHITE);
            
            JLabel isbnTitleLabel = new JLabel("ISBN:");
            isbnTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            isbnTitleLabel.setForeground(new Color(73, 80, 87));
            
            JTextArea isbnTextArea = new JTextArea(isbnInfo.toString());
            isbnTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            isbnTextArea.setForeground(new Color(33, 37, 41));
            isbnTextArea.setBackground(Color.WHITE);
            isbnTextArea.setEditable(false);
            isbnTextArea.setBorder(null);
            
            isbnPanel.add(isbnTitleLabel, BorderLayout.NORTH);
            isbnPanel.add(isbnTextArea, BorderLayout.CENTER);
            
            additionalPanel.add(isbnPanel);
        }
        
        // External links (if available)
        if (book.getPreviewLink() != null || book.getInfoLink() != null) {
            JPanel linksPanel = new JPanel(new BorderLayout());
            linksPanel.setBackground(Color.WHITE);
            
            JLabel linksLabel = new JLabel("External Links:");
            linksLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            linksLabel.setForeground(new Color(73, 80, 87));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            buttonPanel.setBackground(Color.WHITE);
            
            if (book.getPreviewLink() != null) {
                JButton previewBtn = new JButton("Preview");
                previewBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                previewBtn.setPreferredSize(new Dimension(70, 25));
                previewBtn.addActionListener(_ -> openUrl(book.getPreviewLink()));
                buttonPanel.add(previewBtn);
            }
            
            if (book.getInfoLink() != null) {
                JButton infoBtn = new JButton("More Info");
                infoBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                infoBtn.setPreferredSize(new Dimension(70, 25));
                infoBtn.addActionListener(_ -> openUrl(book.getInfoLink()));
                buttonPanel.add(infoBtn);
            }
            
            linksPanel.add(linksLabel, BorderLayout.NORTH);
            linksPanel.add(buttonPanel, BorderLayout.CENTER);
            
            additionalPanel.add(linksPanel);
        }
        
        return additionalPanel;
    }
    
    private JPanel createDetailLabel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        titleLabel.setForeground(new Color(73, 80, 87));
        
        JLabel valueLabel = new JLabel("<html>" + (value != null ? value : "N/A") + "</html>");
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        valueLabel.setForeground(new Color(33, 37, 41));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Add to library buttons
        JButton wantToReadBtn = createActionButton("Want to Read", new Color(108, 117, 125));
        wantToReadBtn.addActionListener(_ -> addToList("WANT_TO_READ", "Want to Read"));
        
        JButton currentlyReadingBtn = createActionButton("Currently Reading", new Color(255, 193, 7));
        currentlyReadingBtn.addActionListener(_ -> addToList("CURRENTLY_READING", "Currently Reading"));
        
        JButton haveReadBtn = createActionButton("Have Read", new Color(40, 167, 69));
        haveReadBtn.addActionListener(_ -> showRatingReviewDialog());
        
        JButton favoritesBtn = createActionButton("Favorites", new Color(220, 53, 69));
        favoritesBtn.addActionListener(_ -> addToList("FAVORITES", "Favorites"));
        
        // View Reviews button - only show if there are reviews
        JButton viewReviewsBtn = createActionButton("View Reviews (" + reviewCount + ")", new Color(23, 162, 184));
        viewReviewsBtn.addActionListener(_ -> showReviewsDialog());
        viewReviewsBtn.setEnabled(reviewCount > 0); // Disable if no reviews
        
        // Close button
        JButton closeBtn = createActionButton("Close", new Color(108, 117, 125));
        closeBtn.addActionListener(_ -> dispose());
        
        actionsPanel.add(wantToReadBtn);
        actionsPanel.add(currentlyReadingBtn);
        actionsPanel.add(haveReadBtn);
        actionsPanel.add(favoritesBtn);
        actionsPanel.add(viewReviewsBtn);
        actionsPanel.add(Box.createHorizontalStrut(20)); // Spacer
        actionsPanel.add(closeBtn);
        
        return actionsPanel;
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void setPlaceholderImage() {
        String placeholderText = "<html><center><div style='text-align: center;'>" +
                                "<div style='font-size: 24px; margin-bottom: 10px;'>📚</div>" +
                                "<div style='font-size: 12px;'>" + truncateText(book.getTitle(), 25) + "</div>" +
                                "</center></html>";
        
        coverImageLabel.setText(placeholderText);
        coverImageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        coverImageLabel.setForeground(new Color(108, 117, 125));
        coverImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverImageLabel.setVerticalAlignment(SwingConstants.CENTER);
    }
    
    private void loadCoverImage() {
        String imageUrl = book.getThumbnailUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            SwingWorker<ImageIcon, Void> imageLoader = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    try {
                        URI uri = URI.create(imageUrl);
                        BufferedImage originalImage = ImageIO.read(uri.toURL());
                        if (originalImage != null) {
                            Image scaledImage = originalImage.getScaledInstance(
                                COVER_WIDTH - 20, COVER_HEIGHT - 20, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImage);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load book cover for: " + book.getTitle());
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            coverImageLabel.setIcon(icon);
                            coverImageLabel.setText("");
                        }
                    } catch (Exception e) {
                        // Keep placeholder
                    }
                }
            };
            imageLoader.execute();
        }
    }
    
    private void showRatingReviewDialog() {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        
        if (userId == null) {
            JOptionPane.showMessageDialog(this, 
                "Please log in to rate and review books.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create and show rating/review dialog
        RatingReviewDialog ratingDialog = new RatingReviewDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            book, 
            bookService, 
            () -> {
                // This callback runs after successful rating/review submission
                addToList("HAVE_READ", "Have Read");
            }
        );
        
        ratingDialog.setVisible(true);
    }
    
    private void addToList(String listType, String listDisplayName) {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        
        if (userId == null) {
            JOptionPane.showMessageDialog(this, 
                "Please log in to add books to your list.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show progress
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Adding to " + listDisplayName + "...");
        
        JDialog progressDialog = new JDialog(this, "Processing", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 80);
        progressDialog.setLocationRelativeTo(this);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bookService.addBookToUserList(userId, listType, book, null);
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    boolean success = get();
                    if (success) {
                        if ("HAVE_READ".equalsIgnoreCase(listType)) {
                            JOptionPane.showMessageDialog(BookDetailsDialog.this,
                                "Successfully added to Have Read with your rating and review!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(BookDetailsDialog.this, 
                                "Successfully added '" + book.getTitle() + "' to " + listDisplayName + "!", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        String reason = bookService.getLastMessage();
                        JOptionPane.showMessageDialog(BookDetailsDialog.this, 
                            (reason != null && !reason.isEmpty() ? reason : ("Failed to add book to " + listDisplayName + ". Please try again.")), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookDetailsDialog.this, 
                        "Error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not open URL: " + url,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Show a dialog displaying all public reviews for this book
     */
    private void showReviewsDialog() {
        try {
            java.util.List<String[]> reviews = bookService.getBookReviews(book, 50);
            
            if (reviews.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No reviews found for this book.",
                    "Reviews",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create reviews dialog
            JDialog reviewsDialog = new JDialog(this, "Book Reviews", true);
            reviewsDialog.setSize(600, 500);
            reviewsDialog.setLocationRelativeTo(this);
            
            // Create scrollable content
            JPanel reviewsPanel = new JPanel();
            reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
            reviewsPanel.setBackground(Color.WHITE);
            reviewsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Add reviews
            for (String[] review : reviews) {
                JPanel reviewItem = createReviewItem(review);
                reviewsPanel.add(reviewItem);
                reviewsPanel.add(Box.createVerticalStrut(10));
            }
            
            JScrollPane scrollPane = new JScrollPane(reviewsPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            reviewsDialog.add(scrollPane, BorderLayout.CENTER);
            
            // Close button
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(_ -> reviewsDialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(closeBtn);
            reviewsDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            reviewsDialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("Error showing reviews: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error loading reviews: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Create a panel for displaying a single review
     * @param review Array with [review_title, review_text, is_spoiler, username, first_name, last_name, created_at, user_rating]
     */
    private JPanel createReviewItem(String[] review) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        // Header with username, rating, and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        String username = review[3]; // username
        String firstName = review[4]; // first_name
        String lastName = review[5]; // last_name
        String ratingStr = review[7]; // user_rating
        String date = review[6]; // created_at
        
        // Build display name
        String displayName = username;
        if (firstName != null && !firstName.trim().isEmpty()) {
            displayName = firstName + (lastName != null && !lastName.trim().isEmpty() ? " " + lastName : "");
        }
        
        JLabel userLabel = new JLabel(displayName);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Handle rating - might be null if user didn't rate
        JLabel ratingLabel;
        if (ratingStr != null && !ratingStr.equals("null")) {
            try {
                double rating = Double.parseDouble(ratingStr);
                int starCount = (int) Math.round(rating);
                String stars = "★".repeat(Math.max(0, starCount)) + "☆".repeat(Math.max(0, 5 - starCount));
                ratingLabel = new JLabel(stars + " " + rating + "/5");
                ratingLabel.setForeground(new Color(255, 193, 7));
            } catch (NumberFormatException e) {
                ratingLabel = new JLabel("No rating");
                ratingLabel.setForeground(Color.GRAY);
            }
        } else {
            ratingLabel = new JLabel("No rating");
            ratingLabel.setForeground(Color.GRAY);
        }
        ratingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        dateLabel.setForeground(Color.GRAY);
        
        headerPanel.add(userLabel, BorderLayout.WEST);
        headerPanel.add(ratingLabel, BorderLayout.CENTER);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        // Review text
        String reviewText = review[1]; // review_text
        JTextArea textArea = new JTextArea(reviewText);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(textArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Load BookTrack user ratings and review count for this book
     */
    private void loadBookTrackRatings() {
        try {
            // Get rating statistics from BookTrack database
            bookRatingStats = bookService.getBookRatingStats(book);
            reviewCount = bookService.getBookReviewCount(book);
            
            System.out.println("Loaded BookTrack ratings - Avg: " + bookRatingStats[0] + 
                             ", Total: " + (int)bookRatingStats[1] + ", Reviews: " + reviewCount);
        } catch (Exception e) {
            System.err.println("Error loading BookTrack ratings: " + e.getMessage());
            bookRatingStats = new double[]{0, 0, 0, 0, 0, 0, 0};
            reviewCount = 0;
        }
    }
    
    private String formatRating(double rating, int ratingsCount) {
        if (rating > 0) {
            String stars = "★".repeat((int) Math.round(rating)) + "☆".repeat(5 - (int) Math.round(rating));
            return String.format("%s %.1f/5 (%d ratings)", stars, rating, ratingsCount);
        }
        return "☆☆☆☆☆ No ratings yet";
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
