package com.booktrack.ui;

import com.booktrack.model.Book;
import com.booktrack.model.User;
import com.booktrack.service.BookService;
import com.booktrack.ui.component.BookCard;
import com.booktrack.ui.component.MyListsDialog;
import com.booktrack.ui.component.AnalyticsDialog;
import com.booktrack.ui.component.EditProfileDialog;

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
    // Header references for live updates
    private JPanel userCardPanel;
    private JLabel userNameLabel;
    private JLabel userEmailLabel;
    
    public LandingPage(User user) {
        this.currentUser = user;
        this.bookService = new BookService();
        
        initializeFrame();
        createMenuBar();
        createHeader();
        createSearchSection();
        createBooksSection();
        
        // Show the UI immediately
        setVisible(true);
        
        // Load books asynchronously after UI is shown
        SwingUtilities.invokeLater(() -> loadInitialBooks());
    }
    
    private void initializeFrame() {
        setTitle("BookTrack - Welcome " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Modern background
        getContentPane().setBackground(new Color(245, 247, 250));
        
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
    profileItem.addActionListener(e -> { showProfile(); e.getWhen(); });
        
        JMenuItem logoutItem = new JMenuItem("Logout");
    logoutItem.addActionListener(e -> { logout(); e.getWhen(); });
        
        JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(e -> { System.exit(0); e.getWhen(); });
        
        fileMenu.add(profileItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);
        
        // Books Menu
        JMenu booksMenu = new JMenu("Books");
        
        JMenuItem myListsItem = new JMenuItem("My Lists");
    myListsItem.addActionListener(e -> { showMyLists(); e.getWhen(); });
        
        JMenuItem analyticsItem = new JMenuItem("Analytics");
    analyticsItem.addActionListener(e -> { showAnalytics(); e.getWhen(); });
        
        JMenuItem browseItem = new JMenuItem("Browse Books");
    browseItem.addActionListener(e -> { browseBooks(); e.getWhen(); });
        
        booksMenu.add(myListsItem);
        booksMenu.add(analyticsItem);
        booksMenu.add(browseItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(e -> { showAbout(); e.getWhen(); });
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(booksMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createHeader() {
        // Modern gradient-style header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(52, 73, 94),
                    0, getHeight(), new Color(44, 62, 80)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout(20, 20));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        headerPanel.setPreferredSize(new Dimension(0, 120));
        
        // Left side - Welcome text
        JPanel welcomePanel = new JPanel(new BorderLayout(0, 8));
        welcomePanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("📚 Welcome to BookTrack, " + currentUser.getFirstName() + "!");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Discover, track, and share your reading journey");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        welcomePanel.add(welcomeLabel, BorderLayout.NORTH);
        welcomePanel.add(subtitleLabel, BorderLayout.CENTER);
        
        // Right side - User info card
    userCardPanel = new JPanel(new BorderLayout(10, 5));
        userCardPanel.setBackground(new Color(255, 255, 255, 30));
        userCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
    // Don't lock a fixed width here; we'll size dynamically below so long emails aren't clipped
        
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        userIcon.setForeground(Color.WHITE);
        
        JPanel userInfoPanel = new JPanel(new BorderLayout(0, 3));
        userInfoPanel.setOpaque(false);
        
    userNameLabel = new JLabel(currentUser.getFullName());
        userNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        userNameLabel.setForeground(Color.WHITE);
        
    userEmailLabel = new JLabel(currentUser.getEmail());
        userEmailLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        userEmailLabel.setForeground(new Color(189, 195, 199));
    // Show full email on hover in case it still overflows in smaller windows
    userEmailLabel.setToolTipText(currentUser.getEmail());
        
        userInfoPanel.add(userNameLabel, BorderLayout.NORTH);
        userInfoPanel.add(userEmailLabel, BorderLayout.CENTER);
        
        userCardPanel.add(userIcon, BorderLayout.WEST);
        userCardPanel.add(userInfoPanel, BorderLayout.CENTER);

        // Make the card clickable to edit profile
        userCardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        String tooltip = "View / Edit profile";
        userCardPanel.setToolTipText(tooltip);
        userInfoPanel.setToolTipText(tooltip);
        userNameLabel.setToolTipText(tooltip);
        userEmailLabel.setToolTipText(currentUser.getEmail());
        userCardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openEditProfile(); }
        });
        userInfoPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openEditProfile(); }
        });

        // Dynamically size the user card width so text doesn't get cut off
        // Calculate required width based on the label contents and paddings
        FontMetrics nameFm = userNameLabel.getFontMetrics(userNameLabel.getFont());
        FontMetrics emailFm = userEmailLabel.getFontMetrics(userEmailLabel.getFont());
        int textWidth = Math.max(
            nameFm.stringWidth(userNameLabel.getText()),
            emailFm.stringWidth(userEmailLabel.getText())
        );
        int iconWidth = userIcon.getPreferredSize().width; // emoji label width
        int hGap = 10; // BorderLayout hgap between icon and text
        int sidePadding = 40; // Empty border: 20 left + 20 right
        int minWidth = 250; // keep a sensible minimum
        int computedWidth = iconWidth + hGap + textWidth + sidePadding;
        userCardPanel.setPreferredSize(new Dimension(Math.max(minWidth, computedWidth), 0));
        
        headerPanel.add(welcomePanel, BorderLayout.CENTER);
        headerPanel.add(userCardPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }

    private void openEditProfile() {
        Runnable afterSave = () -> {
            // Update current user object with new values shown in dialog (Session may have refreshed too)
            // Re-read from labels via existing currentUser reference if session changed
            // For simplicity, fetch name/email from session if available
            try {
                com.booktrack.session.SessionManager sm = com.booktrack.session.SessionManager.getInstance();
                if (sm.getCurrentUser() != null && sm.getCurrentUser().getUserId().equals(currentUser.getUserId())) {
                    currentUser.setFirstName(sm.getCurrentUser().getFirstName());
                    currentUser.setLastName(sm.getCurrentUser().getLastName());
                    currentUser.setEmail(sm.getCurrentUser().getEmail());
                }
            } catch (Exception ignored) {}
            updateHeaderUserInfo();
        };
        new EditProfileDialog(this, currentUser, afterSave);
    }

    private void updateHeaderUserInfo() {
        // Update labels
        userNameLabel.setText(currentUser.getFullName());
        userEmailLabel.setText(currentUser.getEmail());
        userEmailLabel.setToolTipText(currentUser.getEmail());
        // Recompute card width to avoid clipping
        FontMetrics nameFm = userNameLabel.getFontMetrics(userNameLabel.getFont());
        FontMetrics emailFm = userEmailLabel.getFontMetrics(userEmailLabel.getFont());
        int textWidth = Math.max(nameFm.stringWidth(userNameLabel.getText()), emailFm.stringWidth(userEmailLabel.getText()));
        int iconWidth = 28; // approx width of the emoji/icon
        int hGap = 10;
        int sidePadding = 40;
        int minWidth = 250;
        int computedWidth = iconWidth + hGap + textWidth + sidePadding;
        userCardPanel.setPreferredSize(new Dimension(Math.max(minWidth, computedWidth), 0));
        userCardPanel.revalidate();
        userCardPanel.repaint();
    }
    
    private void createSearchSection() {
        // Modern search card design
        searchPanel = new JPanel(new BorderLayout(0, 15));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        // Search title with icon
        JLabel searchTitle = new JLabel("🔍 Search Books");
        searchTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        searchTitle.setForeground(new Color(52, 73, 94));
        
        // Search input section
        JPanel searchInputSection = new JPanel(new BorderLayout(15, 0));
        searchInputSection.setOpaque(false);
        
        // Modern search field
        searchField = new JTextField();
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(400, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        searchField.setBackground(new Color(248, 249, 250));
        
        // Modern search button
        searchButton = new JButton("🔍 Search");
        searchButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(120, 40));
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    searchButton.addActionListener(evt -> { performSearch(); evt.getActionCommand(); });
        
        // Hover effect for search button
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
        
        // Enter key support for search
    searchField.addActionListener(evt -> { performSearch(); evt.getActionCommand(); });
        
        searchInputSection.add(searchField, BorderLayout.CENTER);
        searchInputSection.add(searchButton, BorderLayout.EAST);
        
        // Quick search section
        JPanel quickSearchSection = new JPanel(new BorderLayout(0, 10));
        quickSearchSection.setOpaque(false);
        
        JLabel quickSearchLabel = new JLabel("📂 Quick Categories:");
        quickSearchLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        quickSearchLabel.setForeground(new Color(127, 140, 141));
        
        JPanel quickButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        quickButtonsPanel.setOpaque(false);
        
        String[] quickSearches = {"Fiction", "Romance", "Mystery", "Sci-Fi", "Biography", "History"};
        for (String category : quickSearches) {
            JButton quickBtn = new JButton(category);
            quickBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            quickBtn.setBackground(new Color(236, 240, 241));
            quickBtn.setForeground(new Color(52, 73, 94));
            quickBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            quickBtn.setFocusPainted(false);
            quickBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            quickBtn.addActionListener(evt -> { quickSearch(category); evt.getActionCommand(); });
            
            // Hover effect for quick buttons
            quickBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    quickBtn.setBackground(new Color(52, 152, 219));
                    quickBtn.setForeground(Color.WHITE);
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    quickBtn.setBackground(new Color(236, 240, 241));
                    quickBtn.setForeground(new Color(52, 73, 94));
                }
            });
            
            quickButtonsPanel.add(quickBtn);
        }
        
        quickSearchSection.add(quickSearchLabel, BorderLayout.NORTH);
        quickSearchSection.add(quickButtonsPanel, BorderLayout.CENTER);
        
        searchPanel.add(searchTitle, BorderLayout.NORTH);
        searchPanel.add(searchInputSection, BorderLayout.CENTER);
        searchPanel.add(quickSearchSection, BorderLayout.SOUTH);
    }
    
    private void createBooksSection() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 30, 30));
        
        // Search panel container
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setOpaque(false);
        searchContainer.add(searchPanel, BorderLayout.CENTER);
        
        // Books section with modern header
        JPanel booksContainer = new JPanel(new BorderLayout(0, 15));
        booksContainer.setOpaque(false);
        
        // Status/title section
        JPanel titleSection = new JPanel(new BorderLayout());
        titleSection.setOpaque(false);
        
        statusLabel = new JLabel("📚 Loading popular books...");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        statusLabel.setForeground(new Color(52, 73, 94));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        titleSection.add(statusLabel, BorderLayout.WEST);
        
        // Books grid with modern spacing
        booksPanel = new JPanel(new GridLayout(0, GRID_COLUMNS, 20, 25));
        booksPanel.setBackground(new Color(245, 247, 250));
        booksPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        // Modern scroll pane
        scrollPane = new JScrollPane(booksPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(245, 247, 250));
        scrollPane.getViewport().setBackground(new Color(245, 247, 250));
        
        // Style the scrollbar
        scrollPane.getVerticalScrollBar().setBackground(new Color(245, 247, 250));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(189, 195, 199);
                this.trackColor = new Color(236, 240, 241);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }
            
            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        
        booksContainer.add(titleSection, BorderLayout.NORTH);
        booksContainer.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(searchContainer, BorderLayout.NORTH);
        mainPanel.add(booksContainer, BorderLayout.CENTER);
        
        // Replace any existing center component
        Component centerComponent = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            remove(centerComponent);
        }
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadInitialBooks() {
        statusLabel.setText("📚 Loading popular books...");
        booksPanel.removeAll();
        
        // Add loading state
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(245, 247, 250));
        loadingPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel loadingLabel = new JLabel("⏳ Loading amazing books for you...");
        loadingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setForeground(new Color(52, 152, 219));
        
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        booksPanel.add(loadingPanel);
        booksPanel.revalidate();
        booksPanel.repaint();
        
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
            JOptionPane.showMessageDialog(this, 
                "Please enter a search term to find books!", 
                "Search Required", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        statusLabel.setText("🔍 Searching for: " + query);
        booksPanel.removeAll();
        
        // Add searching state
        JPanel searchingPanel = new JPanel(new BorderLayout());
        searchingPanel.setBackground(new Color(245, 247, 250));
        searchingPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel searchingLabel = new JLabel("🔍 Searching for \"" + query + "\"...");
        searchingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        searchingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        searchingLabel.setForeground(new Color(52, 152, 219));
        
        searchingPanel.add(searchingLabel, BorderLayout.CENTER);
        booksPanel.add(searchingPanel);
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
                    displayBooks(books, "Search Results for \"" + query + "\"");
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
            statusLabel.setText("📚 No books found");
            
            // Modern empty state
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(new Color(245, 247, 250));
            emptyPanel.setPreferredSize(new Dimension(0, 300));
            
            JPanel emptyContent = new JPanel(new BorderLayout(0, 15));
            emptyContent.setOpaque(false);
            emptyContent.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
            
            JLabel emptyIcon = new JLabel("📖");
            emptyIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 72));
            emptyIcon.setHorizontalAlignment(SwingConstants.CENTER);
            emptyIcon.setForeground(new Color(189, 195, 199));
            
            JLabel emptyTitle = new JLabel("No Books Found");
            emptyTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            emptyTitle.setHorizontalAlignment(SwingConstants.CENTER);
            emptyTitle.setForeground(new Color(127, 140, 141));
            
            JLabel emptyDesc = new JLabel("<html><center>We couldn't find any books matching your search.<br/>Try different keywords or browse our categories.</center></html>");
            emptyDesc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            emptyDesc.setHorizontalAlignment(SwingConstants.CENTER);
            emptyDesc.setForeground(new Color(149, 165, 166));
            
            JPanel textPanel = new JPanel(new BorderLayout(0, 10));
            textPanel.setOpaque(false);
            textPanel.add(emptyTitle, BorderLayout.NORTH);
            textPanel.add(emptyDesc, BorderLayout.CENTER);
            
            emptyContent.add(emptyIcon, BorderLayout.NORTH);
            emptyContent.add(textPanel, BorderLayout.CENTER);
            
            emptyPanel.add(emptyContent, BorderLayout.CENTER);
            booksPanel.add(emptyPanel);
        } else {
            statusLabel.setText("📚 " + title + " • " + books.size() + " book" + (books.size() != 1 ? "s" : ""));
            
            for (Book book : books) {
                BookCard bookCard = new BookCard(book);
                booksPanel.add(bookCard);
            }
        }
        
        booksPanel.revalidate();
        booksPanel.repaint();
    }
    
    private void showError(String message) {
        statusLabel.setText("⚠️ Error occurred");
        booksPanel.removeAll();
        
        // Modern error state
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(new Color(245, 247, 250));
        errorPanel.setPreferredSize(new Dimension(0, 300));
        
        JPanel errorContent = new JPanel(new BorderLayout(0, 15));
        errorContent.setOpaque(false);
        errorContent.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
        
        JLabel errorIcon = new JLabel("⚠️");
        errorIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 72));
        errorIcon.setHorizontalAlignment(SwingConstants.CENTER);
        errorIcon.setForeground(new Color(231, 76, 60));
        
        JLabel errorTitle = new JLabel("Oops! Something went wrong");
        errorTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        errorTitle.setHorizontalAlignment(SwingConstants.CENTER);
        errorTitle.setForeground(new Color(231, 76, 60));
        
        JLabel errorDesc = new JLabel("<html><center>" + message + "<br/>Please try again later or contact support if the problem persists.</center></html>");
        errorDesc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        errorDesc.setHorizontalAlignment(SwingConstants.CENTER);
        errorDesc.setForeground(new Color(127, 140, 141));
        
        JPanel textPanel = new JPanel(new BorderLayout(0, 10));
        textPanel.setOpaque(false);
        textPanel.add(errorTitle, BorderLayout.NORTH);
        textPanel.add(errorDesc, BorderLayout.CENTER);
        
        errorContent.add(errorIcon, BorderLayout.NORTH);
        errorContent.add(textPanel, BorderLayout.CENTER);
        
        errorPanel.add(errorContent, BorderLayout.CENTER);
        booksPanel.add(errorPanel);
        
        booksPanel.revalidate();
        booksPanel.repaint();
    }
    
    // Menu action handlers
    private void showProfile() {
        openEditProfile();
    }
    
    private void showMyLists() {
        MyListsDialog myListsDialog = new MyListsDialog(this);
        myListsDialog.setVisible(true);
    }
    
    private void showAnalytics() {
        AnalyticsDialog analyticsDialog = new AnalyticsDialog(this);
        analyticsDialog.setVisible(true);
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
            SwingUtilities.invokeLater(() -> {
                try {
                    new com.booktrack.ui.LoginForm().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Error opening login form: " + e.getMessage());
                }
            });
        }
    }
}
