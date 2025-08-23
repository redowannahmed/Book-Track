package com.booktrack.ui.component;

import com.booktrack.model.Book;
import com.booktrack.service.BookService;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import javax.imageio.ImageIO;

/**
 * BookCard component - displays a book with thumbnail and basic info
 * Clickable component that shows book options when clicked
 */
public class BookCard extends JPanel {
    private static final int CARD_WIDTH = 160;
    private static final int CARD_HEIGHT = 260;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 180;
    
    private final Book book;
    private final BookService bookService;
    private final boolean isMyListsMode; // New flag for My Lists mode
    private JLabel imageLabel;
    private JLabel titleLabel;
    private JLabel authorLabel;
    private JLabel ratingLabel;
    
    public BookCard(Book book) {
        this(book, false); // Default constructor for normal mode
    }
    
    public BookCard(Book book, boolean isMyListsMode) {
        this.book = book;
        this.bookService = new BookService();
        this.isMyListsMode = isMyListsMode;
        initializeCard();
        setupLayout();
    // On creation, prefer DB aggregate rating over any API rating if available
    try { bookService.populateDbAggregateRating(this.book); } catch (Exception ignored) {}
        loadBookImage();
        addHoverEffect();
        addClickListener();
    }
    
    private void initializeCard() {
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        setBackground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        
        // Image panel
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        imageLabel.setBackground(Color.LIGHT_GRAY);
        imageLabel.setOpaque(true);
        
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout(2, 2));
        infoPanel.setBackground(Color.WHITE);
        
        // Title
        titleLabel = new JLabel(truncateText(book.getTitle(), 20));
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setToolTipText(book.getTitle());
        
        // Author
        authorLabel = new JLabel(truncateText(book.getAuthorsAsString(), 25));
        authorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        authorLabel.setForeground(Color.GRAY);
        authorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authorLabel.setToolTipText(book.getAuthorsAsString());
        
        // Rating
        ratingLabel = new JLabel(formatRating(book.getAverageRating()));
        ratingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        ratingLabel.setForeground(Color.ORANGE.darker());
        ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(authorLabel, BorderLayout.CENTER);
        infoPanel.add(ratingLabel, BorderLayout.SOUTH);
        
        add(imageLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void loadBookImage() {
        // Set placeholder first
        setPlaceholderImage();
        
        // Load actual image in background
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
                                IMAGE_WIDTH - 10, IMAGE_HEIGHT - 10, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImage);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load image for: " + book.getTitle());
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            imageLabel.setIcon(icon);
                            imageLabel.setText("");
                        }
                    } catch (Exception e) {
                        // Keep placeholder
                    }
                }
            };
            imageLoader.execute();
        }
    }
    
    private void setPlaceholderImage() {
        imageLabel.setText("<html><center>📚<br>" + 
                          truncateText(book.getTitle(), 15) + "</center></html>");
        imageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        imageLabel.setForeground(Color.DARK_GRAY);
    }
    
    private void addHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(7, 7, 7, 7)
                ));
                setBackground(new Color(248, 249, 250));
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
                setBackground(Color.WHITE);
                repaint();
            }
        });
    }
    
    private void addClickListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showBookOptionsDialog();
            }
        });
    }
    
    public void showBookOptionsDialog() {
        if (isMyListsMode) {
            showMyListsBookDialog();
        } else {
            showStandardBookDialog();
        }
    }
    
    private void showMyListsBookDialog() {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this, 
                "Please log in to view your book details.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "My Rating & Review - " + book.getTitle(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Book info header
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.add(new JLabel("Title: " + book.getTitle()));
        infoPanel.add(new JLabel("Author: " + book.getAuthorsAsString()));
        if (book.getPublisher() != null) {
            infoPanel.add(new JLabel("Publisher: " + book.getPublisher()));
        }
        
    // Load user's rating and review
    SwingWorker<Void, Void> dataLoader = new SwingWorker<Void, Void>() {
            private Double userRating;
            private String[] userReview;
            
            @Override
            protected Void doInBackground() throws Exception {
                userRating = bookService.getUserRating(userId, book);
                userReview = bookService.getUserReview(userId, book);
                return null;
            }
            
            @Override
            protected void done() {
        // Replace loading state with actual content in the SAME dialog
        panel.removeAll();
                
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                
                // Rating section
                JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                ratingPanel.setBorder(BorderFactory.createTitledBorder("My Rating"));
                
                if (userRating != null) {
                    JLabel ratingLabel = new JLabel("★".repeat(userRating.intValue()) + 
                                                  "☆".repeat(5 - userRating.intValue()) + 
                                                  " (" + userRating + "/5)");
                    ratingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                    ratingLabel.setForeground(Color.ORANGE.darker());
                    ratingPanel.add(ratingLabel);
                } else {
                    JLabel noRatingLabel = new JLabel("You haven't rated this book yet");
                    noRatingLabel.setForeground(Color.GRAY);
                    noRatingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
                    ratingPanel.add(noRatingLabel);
                }
                
                // Review section
                JPanel reviewPanel = new JPanel(new BorderLayout());
                reviewPanel.setBorder(BorderFactory.createTitledBorder("My Review"));
                
                if (userReview != null) {
                    String reviewTitle = userReview[0];
                    String reviewText = userReview[1];
                    
                    if (reviewTitle != null && !reviewTitle.trim().isEmpty()) {
                        JLabel titleLabel = new JLabel(reviewTitle);
                        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                        reviewPanel.add(titleLabel, BorderLayout.NORTH);
                    }
                    
                    JTextArea reviewArea = new JTextArea(reviewText);
                    reviewArea.setWrapStyleWord(true);
                    reviewArea.setLineWrap(true);
                    reviewArea.setEditable(false);
                    reviewArea.setBackground(panel.getBackground());
                    reviewArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                    
                    JScrollPane scrollPane = new JScrollPane(reviewArea);
                    scrollPane.setPreferredSize(new Dimension(400, 150));
                    reviewPanel.add(scrollPane, BorderLayout.CENTER);
                } else {
                    JLabel noReviewLabel = new JLabel("You haven't reviewed this book yet");
                    noReviewLabel.setForeground(Color.GRAY);
                    noReviewLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
                    reviewPanel.add(noReviewLabel, BorderLayout.CENTER);
                }
                
                contentPanel.add(ratingPanel, BorderLayout.NORTH);
                contentPanel.add(reviewPanel, BorderLayout.CENTER);
                
                panel.add(infoPanel, BorderLayout.NORTH);
                panel.add(contentPanel, BorderLayout.CENTER);
                
                // Close button
                JPanel buttonPanel = new JPanel(new FlowLayout());
                JButton closeBtn = new JButton("Close");
                closeBtn.addActionListener(e -> dialog.dispose());
                buttonPanel.add(closeBtn);
                
                panel.add(buttonPanel, BorderLayout.SOUTH);
                
                // Refresh the shown dialog without re-showing it
                panel.revalidate();
                panel.repaint();
            }
        };
        
        // Show loading dialog
        JLabel loadingLabel = new JLabel("Loading your rating and review...", SwingConstants.CENTER);
    panel.add(loadingLabel, BorderLayout.CENTER);
    dialog.add(panel);
        
    // Start background work BEFORE showing the modal dialog
    dataLoader.execute();
    dialog.setVisible(true);
    }
    
    private void showStandardBookDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "Book Options - " + book.getTitle(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Book info
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.add(new JLabel("Title: " + book.getTitle()));
        infoPanel.add(new JLabel("Author: " + book.getAuthorsAsString()));
        if (book.getPublisher() != null) {
            infoPanel.add(new JLabel("Publisher: " + book.getPublisher()));
        }
        infoPanel.add(new JLabel("Rating: " + formatRating(book.getAverageRating())));
        
        if (book.getDescription() != null) {
            JTextArea descArea = new JTextArea(book.getShortDescription());
            descArea.setWrapStyleWord(true);
            descArea.setLineWrap(true);
            descArea.setEditable(false);
            descArea.setBackground(panel.getBackground());
            JScrollPane scrollPane = new JScrollPane(descArea);
            scrollPane.setPreferredSize(new Dimension(450, 80));
            scrollPane.setBorder(BorderFactory.createTitledBorder("Description"));
            infoPanel.add(scrollPane);
        }
        
        // Action buttons arranged in a grid
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        // First row - Add to lists
        JButton addToWantToReadBtn = new JButton("Want to Read");
        addToWantToReadBtn.setToolTipText("Add to Want to Read list");
        addToWantToReadBtn.addActionListener(e -> addBookToList("WANT_TO_READ", "Want to Read", dialog));
        
        JButton addToCurrentlyReadingBtn = new JButton("Currently Reading");
        addToCurrentlyReadingBtn.setToolTipText("Add to Currently Reading list");
        addToCurrentlyReadingBtn.addActionListener(e -> addBookToList("CURRENTLY_READING", "Currently Reading", dialog));
        
        // Second row - More lists
        JButton addToHaveReadBtn = new JButton("Have Read");
        addToHaveReadBtn.setToolTipText("Add to Have Read list");
        addToHaveReadBtn.addActionListener(e -> addBookToList("HAVE_READ", "Have Read", dialog));
        
        JButton addToFavoritesBtn = new JButton("Favorites");
        addToFavoritesBtn.setToolTipText("Add to Favorites list");
        addToFavoritesBtn.addActionListener(e -> addBookToList("FAVORITES", "Favorites", dialog));
        
        // Third row - Rate and Review
        JButton rateReviewBtn = new JButton("Rate & Review");
        rateReviewBtn.setToolTipText("Rate and/or review this book");
        rateReviewBtn.addActionListener(e -> showRateReviewDialog(dialog));
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addToWantToReadBtn);
        buttonPanel.add(addToCurrentlyReadingBtn);
        buttonPanel.add(addToHaveReadBtn);
        buttonPanel.add(addToFavoritesBtn);
        buttonPanel.add(rateReviewBtn);
        buttonPanel.add(closeBtn);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showRateReviewDialog(JDialog parentDialog) {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        
        if (userId == null) {
            JOptionPane.showMessageDialog(parentDialog, 
                "Please log in to rate and review books.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if user has already rated/reviewed this book
        SwingWorker<Object[], Void> worker = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Double existingRating = bookService.getUserRating(userId, book);
                String[] existingReview = bookService.getUserReview(userId, book);
                return new Object[]{existingRating, existingReview};
            }
            
            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    Double existingRating = (Double) result[0];
                    String[] existingReview = (String[]) result[1];
                    
                    if (existingRating != null || existingReview != null) {
                        // User has existing rating/review
                        double rating = existingRating != null ? existingRating : 3.0;
                        String title = existingReview != null ? existingReview[0] : "";
                        String text = existingReview != null ? existingReview[1] : "";
                        boolean isSpoiler = existingReview != null && "1".equals(existingReview[2]);
                        boolean isPublic = existingReview != null ? "1".equals(existingReview[3]) : true;
                        
                        showExistingRateReviewDialog(parentDialog, rating, title, text, isSpoiler, isPublic);
                    } else {
                        // New rating/review
                        showNewRateReviewDialog(parentDialog);
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentDialog, 
                        "Error loading existing rating/review: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void showNewRateReviewDialog(JDialog parentDialog) {
        RateReviewDialog rateDialog = new RateReviewDialog(
            (Frame) SwingUtilities.getWindowAncestor(BookCard.this),
            book.getTitle(),
            null // We'll handle the callback differently
        );
        
        // Override the submit button action
        rateDialog.addSubmitListener(actionEvent -> handleRateReviewSubmit(rateDialog, parentDialog));
        rateDialog.setVisible(true);
    }
    
    private void showExistingRateReviewDialog(JDialog parentDialog, double rating, String title, 
                                            String text, boolean isSpoiler, boolean isPublic) {
        RateReviewDialog rateDialog = new RateReviewDialog(
            (Frame) SwingUtilities.getWindowAncestor(BookCard.this),
            book.getTitle(),
            rating, title, text, isSpoiler, isPublic,
            null // We'll handle the callback differently
        );
        
        // Override the submit button action
        rateDialog.addSubmitListener(actionEvent -> handleRateReviewSubmit(rateDialog, parentDialog));
        rateDialog.setVisible(true);
    }
    
    private void handleRateReviewSubmit(RateReviewDialog rateDialog, JDialog parentDialog) {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        
        double rating = rateDialog.getSelectedRating();
        String reviewTitle = rateDialog.getReviewTitle();
        String reviewText = rateDialog.getReviewText();
        boolean isSpoiler = rateDialog.isSpoiler();
        boolean isPublic = rateDialog.isPublic();
        
        // Show progress
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Submitting rating and review...");
        
        JDialog progressDialog = new JDialog(parentDialog, "Submitting", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 80);
        progressDialog.setLocationRelativeTo(parentDialog);
        
        SwingWorker<Boolean[], Void> worker = new SwingWorker<Boolean[], Void>() {
            @Override
            protected Boolean[] doInBackground() throws Exception {
                boolean ratingSuccess = bookService.rateBook(userId, book, rating);
                boolean reviewSuccess = true;
                
                // Only submit review if there's content
                if (!reviewText.trim().isEmpty()) {
                    reviewSuccess = bookService.reviewBook(userId, book, reviewTitle, reviewText, isSpoiler, isPublic);
                }
                
                return new Boolean[]{ratingSuccess, reviewSuccess};
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    Boolean[] results = get();
                    boolean ratingSuccess = results[0];
                    boolean reviewSuccess = results[1];
                    
                    if (ratingSuccess && reviewSuccess) {
                        JOptionPane.showMessageDialog(parentDialog, 
                            "Rating and review submitted successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        parentDialog.dispose();
                        
                        // Update the book card rating display if needed
                        updateBookRatingDisplay();
                    } else {
                        String message = "Failed to submit ";
                        if (!ratingSuccess && !reviewSuccess) {
                            message += "rating and review";
                        } else if (!ratingSuccess) {
                            message += "rating";
                        } else {
                            message += "review";
                        }
                        message += ". Please try again.";
                        
                        JOptionPane.showMessageDialog(parentDialog, 
                            message, 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentDialog, 
                        "Error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    private void updateBookRatingDisplay() {
        // Refresh from DB to reflect new aggregate average on the card
        boolean updated = bookService.populateDbAggregateRating(this.book);
        if (updated && ratingLabel != null) {
            ratingLabel.setText(formatRating(book.getAverageRating()));
        }
        repaint();
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private String formatRating(double rating) {
        if (rating > 0) {
            return "★ " + String.format("%.1f", rating);
        }
        return "☆ No rating";
    }
    
    public Book getBook() {
        return book;
    }
    
    /**
     * Add book to user's list
     * @param listType Type of list (to_read, currently_reading, etc.)
     * @param listDisplayName Display name for user feedback
     * @param parentDialog Parent dialog to close after success
     */
    private void addBookToList(String listType, String listDisplayName, JDialog parentDialog) {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        
        if (userId == null) {
            JOptionPane.showMessageDialog(parentDialog, 
                "Please log in to add books to your list.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show loading message
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Adding book to " + listDisplayName + "...");
        
        JDialog progressDialog = new JDialog(parentDialog, "Adding Book", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 80);
        progressDialog.setLocationRelativeTo(parentDialog);
        
        // Add book in background thread
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
                        JOptionPane.showMessageDialog(parentDialog, 
                            "Successfully added '" + book.getTitle() + "' to " + listDisplayName + "!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        parentDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(parentDialog, 
                            "Failed to add book to " + listDisplayName + ". Please try again.", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentDialog, 
                        "Error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
}
