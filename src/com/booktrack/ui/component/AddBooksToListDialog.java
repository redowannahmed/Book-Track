package com.booktrack.ui.component;

import com.booktrack.dao.UserDAO;
import com.booktrack.dao.BookDAO;
import com.booktrack.service.BookService;
import com.booktrack.model.Book;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Dialog for searching and adding books to custom lists
 */
public class AddBooksToListDialog extends JDialog {
    private static final int DIALOG_WIDTH = 700;
    private static final int DIALOG_HEIGHT = 600;
    
    private final Frame parent;
    private final BookService bookService;
    private final BookDAO bookDAO;
    private final UserDAO userDAO;
    private final Integer userId;
    private final Integer listId;
    private final String listName;
    private final Runnable onBookAddedCallback;
    
    private JTextField searchField;
    private JButton searchButton;
    private JPanel booksPanel;
    private JScrollPane booksScrollPane;
    private JLabel statusLabel;
    private List<Book> searchResults;
    
    public AddBooksToListDialog(Frame parent, Integer listId, String listName, Runnable onBookAddedCallback) {
        super(parent, "Add Books to " + listName, true);
        this.parent = parent;
        this.bookService = new BookService();
        this.bookDAO = new BookDAO();
        this.userDAO = new UserDAO();
        this.userId = SessionManager.getInstance().getCurrentUserId();
        this.listId = listId;
        this.listName = listName;
        this.onBookAddedCallback = onBookAddedCallback;
        
        if (userId == null) {
            JOptionPane.showMessageDialog(parent, 
                "Please log in to add books to lists.", 
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
        setResizable(true);
        
        // Set background
        getContentPane().setBackground(new Color(245, 247, 250));
    }
    
    private void createComponents() {
        // Search field
        searchField = new JTextField(30);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        searchField.setBackground(Color.WHITE);
        
        // Search button
        searchButton = new JButton("🔍 Search");
        searchButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Books panel
        booksPanel = new JPanel();
        booksPanel.setLayout(new BoxLayout(booksPanel, BoxLayout.Y_AXIS));
        booksPanel.setBackground(Color.WHITE);
        
        booksScrollPane = new JScrollPane(booksPanel);
        booksScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        booksScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        booksScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Status label
        statusLabel = new JLabel("Search for books to add to your list");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel headerLabel = new JLabel("📚 Add Books to " + listName);
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Search and select books to add to your collection");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titleContainer = new JPanel(new BorderLayout(0, 5));
        titleContainer.setOpaque(false);
        titleContainer.add(headerLabel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 25));
        
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(booksScrollPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        // Back to Home button
        JButton backToHomeButton = new JButton("🏠 Back to Home");
        backToHomeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        backToHomeButton.setBackground(new Color(52, 152, 219));
        backToHomeButton.setForeground(Color.WHITE);
        backToHomeButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backToHomeButton.setFocusPainted(false);
        backToHomeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToHomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                if (parent instanceof com.booktrack.ui.LandingPage) {
                    ((com.booktrack.ui.LandingPage) parent).refreshBooks();
                }
            }
        });
        
        // Add hover effect for back to home button
        backToHomeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backToHomeButton.setBackground(new Color(41, 128, 185));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backToHomeButton.setBackground(new Color(52, 152, 219));
            }
        });
        
        JButton closeButton = new JButton("✕ Close");
        closeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        closeButton.setBackground(new Color(149, 165, 166));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Add hover effect
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeButton.setBackground(new Color(127, 140, 141));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setBackground(new Color(149, 165, 166));
            }
        });
        
        buttonPanel.add(backToHomeButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        // Search button action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Enter key in search field
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Add hover effect to search button
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                searchButton.setBackground(new Color(41, 128, 185));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                searchButton.setBackground(new Color(52, 152, 219));
            }
        });
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a search term.",
                "Search Required",
                JOptionPane.WARNING_MESSAGE);
            searchField.requestFocus();
            return;
        }
        
        statusLabel.setText("Searching for books...");
        booksPanel.removeAll();
        booksPanel.revalidate();
        booksPanel.repaint();
        
        // Disable search during operation
        searchButton.setEnabled(false);
        searchField.setEnabled(false);
        searchButton.setText("Searching...");
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return bookService.searchBooks(searchTerm, 20);
            }
            
            @Override
            protected void done() {
                try {
                    searchResults = get();
                    displaySearchResults();
                } catch (Exception e) {
                    statusLabel.setText("Error searching books: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Re-enable search
                    searchButton.setEnabled(true);
                    searchField.setEnabled(true);
                    searchButton.setText("🔍 Search");
                }
            }
        };
        
        worker.execute();
    }
    
    private void displaySearchResults() {
        booksPanel.removeAll();
        
        if (searchResults == null || searchResults.isEmpty()) {
            statusLabel.setText("No books found matching your search");
            
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
            
            JLabel emptyLabel = new JLabel("<html><center>📚<br><br>No books found<br>Try different search terms</center></html>");
            emptyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            emptyLabel.setForeground(new Color(127, 140, 141));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            booksPanel.add(emptyPanel);
        } else {
            statusLabel.setText("Found " + searchResults.size() + " book" + (searchResults.size() != 1 ? "s" : ""));
            
            for (Book book : searchResults) {
                JPanel bookPanel = createBookSearchResultPanel(book);
                booksPanel.add(bookPanel);
                booksPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        booksPanel.revalidate();
        booksPanel.repaint();
        
        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            booksScrollPane.getVerticalScrollBar().setValue(0);
        });
    }
    
    private JPanel createBookSearchResultPanel(Book book) {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setPreferredSize(new Dimension(0, 100));
        
        // Left side - Book icon
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(60, 80));
        
        JLabel bookIcon = new JLabel("📖");
        bookIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        bookIcon.setHorizontalAlignment(SwingConstants.CENTER);
        bookIcon.setVerticalAlignment(SwingConstants.CENTER);
        bookIcon.setBackground(new Color(236, 240, 241));
        bookIcon.setOpaque(true);
        bookIcon.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        iconPanel.add(bookIcon, BorderLayout.CENTER);
        
        // Center - Book info
        JPanel infoPanel = new JPanel(new BorderLayout(5, 8));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(book.getTitle());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titleLabel.setForeground(new Color(44, 62, 80));
        
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        detailsPanel.setOpaque(false);
        
        if (book.getAuthors() != null && book.getAuthors().length > 0) {
            JLabel authorLabel = new JLabel("By " + book.getAuthorsAsString());
            authorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            authorLabel.setForeground(new Color(127, 140, 141));
            detailsPanel.add(authorLabel);
        }
        
        if (book.getPublisher() != null && !book.getPublisher().trim().isEmpty()) {
            JLabel publisherLabel = new JLabel(" • " + book.getPublisher());
            publisherLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            publisherLabel.setForeground(new Color(127, 140, 141));
            detailsPanel.add(publisherLabel);
        }
        
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Right side - Add button
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(80, 80));
        
        JButton addButton = new JButton("+ Add");
        addButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Check if book is already in the list (we'll check this differently since it might be a new book from API)
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBookToList(book, addButton);
            }
        });
        
        // Add hover effect for buttons
        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (addButton.isEnabled()) {
                    addButton.setBackground(new Color(39, 174, 96));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (addButton.isEnabled()) {
                    addButton.setBackground(new Color(46, 204, 113));
                }
            }
        });
        
        actionPanel.add(addButton, BorderLayout.CENTER);

        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void addBookToList(Book book, JButton addButton) {
        addButton.setEnabled(false);
        addButton.setText("Adding...");
        
        SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                // First, ensure the book is in the database
                Integer bookId = book.getBookId();
                if (bookId == null) {
                    // Book is from Google Books API, need to add it to database first
                    bookId = bookDAO.addOrUpdateBook(book);
                    if (bookId == null) {
                        return new String[]{"0", "Failed to add book to database"};
                    }
                }
                
                // Now add the book to the custom list
                return userDAO.addBookToCustomList(userId, listId, bookId, null);
            }
            
            @Override
            protected void done() {
                try {
                    String[] result = get();
                    
                    if ("1".equals(result[0])) {
                        // Success
                        addButton.setText("Added");
                        addButton.setBackground(new Color(149, 165, 166));
                        
                        // Show success message
                        JOptionPane.showMessageDialog(AddBooksToListDialog.this,
                            "\"" + book.getTitle() + "\" added to " + listName + "!",
                            "Book Added",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        if (onBookAddedCallback != null) {
                            onBookAddedCallback.run();
                        }
                    } else {
                        // Error
                        JOptionPane.showMessageDialog(AddBooksToListDialog.this,
                            result[1],
                            "Error Adding Book",
                            JOptionPane.ERROR_MESSAGE);
                        
                        addButton.setEnabled(true);
                        addButton.setText("+ Add");
                        addButton.setBackground(new Color(46, 204, 113));
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AddBooksToListDialog.this,
                        "An error occurred while adding the book: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    addButton.setEnabled(true);
                    addButton.setText("+ Add");
                    addButton.setBackground(new Color(46, 204, 113));
                }
            }
        };
        
        worker.execute();
    }
}
