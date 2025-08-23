package com.booktrack.ui;

import com.booktrack.model.Book;
import com.booktrack.model.User;
import com.booktrack.service.BookService;
import com.booktrack.ui.component.BookCard;
import com.booktrack.ui.component.MyListsDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Landing Page - Main page after login
 * Shows popular books and search functionality
 */
public class LandingPage extends JFrame {
    private static final int GRID_COLUMNS = 4;
    private static final int MAX_BOOKS_DISPLAY = 12;
    
    private final User currentUser;
    private final BookService bookService;
    private JPanel booksPanel;
    private JPanel searchPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JLabel statusLabel;
    private JScrollPane scrollPane;
    
    public LandingPage(User user) {
        this.currentUser = user;
        this.bookService = new BookService();
        
        initializeFrame();
        createMenuBar();
        createHeader();
        createSearchSection();
        createBooksSection();
        loadInitialBooks();
        
        setVisible(true);
    }
    
    private void initializeFrame() {
        setTitle("BookTrack - Welcome " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Set application icon (if available)
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("resources/icon.png"));
        } catch (Exception e) {
            // Ignore if icon not found
        }
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> showProfile());
        
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(profileItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);
        
        // Books Menu
        JMenu booksMenu = new JMenu("Books");
        
        JMenuItem myListsItem = new JMenuItem("My Lists");
        myListsItem.addActionListener(e -> showMyLists());
        
        JMenuItem browseItem = new JMenuItem("Browse Books");
        browseItem.addActionListener(e -> browseBooks());
        
        booksMenu.add(myListsItem);
        booksMenu.add(browseItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(booksMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setBackground(new Color(0, 123, 255));
        
        JLabel welcomeLabel = new JLabel("Welcome to BookTrack, " + currentUser.getFirstName() + "!");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Discover your next favorite book");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createSearchSection() {
        // Build but don't add to frame yet; we'll place this above the books grid
        searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel searchLabel = new JLabel("Search Books:");
        searchLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 30));
        
        searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(80, 30));
        searchButton.addActionListener(e -> performSearch());
        
        // Enter key support for search
        searchField.addActionListener(e -> performSearch());
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);
        
        JPanel searchTopPanel = new JPanel(new BorderLayout());
        searchTopPanel.add(searchLabel, BorderLayout.WEST);
        searchTopPanel.add(searchInputPanel, BorderLayout.CENTER);
        
        // Quick search buttons
        JPanel quickSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickSearchPanel.add(new JLabel("Quick searches: "));
        
        String[] quickSearches = {"Fiction", "Romance", "Mystery", "Sci-Fi", "Biography", "History"};
        for (String category : quickSearches) {
            JButton quickBtn = new JButton(category);
            quickBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            quickBtn.addActionListener(e -> quickSearch(category));
            quickSearchPanel.add(quickBtn);
        }
        
    searchPanel.add(searchTopPanel, BorderLayout.NORTH);
    searchPanel.add(quickSearchPanel, BorderLayout.SOUTH);
    }
    
    private void createBooksSection() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
    // Status label
    statusLabel = new JLabel("Loading popular books...");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
    statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        // Books grid panel
        booksPanel = new JPanel(new GridLayout(0, GRID_COLUMNS, 15, 15));
        booksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        scrollPane = new JScrollPane(booksPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
    // Top panel contains Search bar and status text stacked
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(searchPanel, BorderLayout.NORTH);
    topPanel.add(statusLabel, BorderLayout.SOUTH);
    mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Replace any existing center component (if present) with the books section including search bar
        Component centerComponent = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            remove(centerComponent);
        }
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadInitialBooks() {
        statusLabel.setText("Loading popular books...");
        booksPanel.removeAll();
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return bookService.getLandingPageBooks();
            }
            
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    displayBooks(books, "Popular Books");
                } catch (Exception e) {
                    showError("Failed to load books: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term", "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Searching for: " + query);
        booksPanel.removeAll();
        booksPanel.revalidate();
        booksPanel.repaint();
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return bookService.searchBooks(query, MAX_BOOKS_DISPLAY);
            }
            
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    displayBooks(books, "Search Results for: " + query);
                } catch (Exception e) {
                    showError("Search failed: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void quickSearch(String category) {
        searchField.setText(category);
        performSearch();
    }
    
    private void displayBooks(List<Book> books, String title) {
        booksPanel.removeAll();
        
        if (books == null || books.isEmpty()) {
            statusLabel.setText("No books found");
            JLabel noResultsLabel = new JLabel("No books found. Try a different search term.");
            noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noResultsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            noResultsLabel.setForeground(Color.GRAY);
            booksPanel.add(noResultsLabel);
        } else {
            statusLabel.setText(title + " (" + books.size() + " books)");
            
            for (Book book : books) {
                BookCard bookCard = new BookCard(book);
                booksPanel.add(bookCard);
            }
        }
        
        booksPanel.revalidate();
        booksPanel.repaint();
    }
    
    private void showError(String message) {
        statusLabel.setText("Error occurred");
        booksPanel.removeAll();
        
        JLabel errorLabel = new JLabel("<html><center>Error: " + message + "<br>Please try again later.</center></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        errorLabel.setForeground(Color.RED);
        booksPanel.add(errorLabel);
        
        booksPanel.revalidate();
        booksPanel.repaint();
    }
    
    // Menu action handlers
    private void showProfile() {
        JOptionPane.showMessageDialog(this, 
            "Profile page coming soon!\n\nUser: " + currentUser.getFullName() + 
            "\nEmail: " + currentUser.getEmail(),
            "Profile", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showMyLists() {
        MyListsDialog myListsDialog = new MyListsDialog(this);
        myListsDialog.setVisible(true);
    }
    
    private void browseBooks() {
        JOptionPane.showMessageDialog(this, 
            "Browse Books feature coming soon!\nThis will show books by categories.",
            "Browse Books", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this, 
            "BookTrack v1.0\n\nA book tracking application for book lovers.\n" +
            "Track your reading progress, discover new books, and share reviews.",
            "About BookTrack", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Logout", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}
