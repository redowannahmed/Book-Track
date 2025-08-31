package com.booktrack.ui.component;

import com.booktrack.dao.UserDAO;
import com.booktrack.dao.BookDAO;
import com.booktrack.model.Book;
import com.booktrack.session.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Dialog to show user's custom lists and books in each list
 */
public class MyListsDialog extends JDialog {
    private static final int DIALOG_WIDTH = 800;
    private static final int DIALOG_HEIGHT = 600;
    
    private final Frame parent;
    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final Integer userId;
    
    private JList<String> listsList;
    private DefaultListModel<String> listsModel;
    private JPanel booksPanel;
    private JScrollPane booksScrollPane;
    private JLabel statusLabel;
    private JButton addBooksButton; // Add books button for selected list
    
    private List<String[]> userLists; // [list_id, list_name, list_description, list_type, books_count, is_default]
    
    public MyListsDialog(Frame parent) {
        super(parent, "My Lists", true);
        this.parent = parent;
        this.userDAO = new UserDAO();
        this.bookDAO = new BookDAO();
        this.userId = SessionManager.getInstance().getCurrentUserId();
        
        if (userId == null) {
            JOptionPane.showMessageDialog(parent, 
                "Please log in to view your lists.", 
                "Login Required", 
                JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        
        initializeDialog();
        createComponents();
        layoutComponents();
        loadUserLists();
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
        // Lists panel components
        listsModel = new DefaultListModel<>();
        listsList = new JList<>(listsModel);
        listsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listsList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        listsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadBooksForSelectedList();
            }
        });
        
        // Add right-click context menu for list management
        listsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            private void showContextMenu(java.awt.event.MouseEvent e) {
                int index = listsList.locationToIndex(e.getPoint());
                if (index >= 0 && userLists != null && index < userLists.size()) {
                    listsList.setSelectedIndex(index);
                    showListContextMenu(userLists.get(index), e.getX(), e.getY());
                }
            }
        });
        
        // Books panel
        booksPanel = new JPanel();
        booksPanel.setLayout(new BoxLayout(booksPanel, BoxLayout.Y_AXIS));
        booksScrollPane = new JScrollPane(booksPanel);
        booksScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        booksScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Status label
        statusLabel = new JLabel("Loading your lists...");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(15, 15));
        
        // Header with gradient-like appearance
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel headerLabel = new JLabel("📚 My Book Lists");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Organize and manage your reading collections");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titleContainer = new JPanel(new BorderLayout(0, 5));
        titleContainer.setOpaque(false);
        titleContainer.add(headerLabel, BorderLayout.CENTER);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(20, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        
        // Left panel - Lists with modern card design
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(new Color(245, 247, 250));
        leftPanel.setPreferredSize(new Dimension(280, 0));
        
        JLabel listsTitle = new JLabel("📋 Your Collections");
        listsTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        listsTitle.setForeground(new Color(52, 73, 94));
        
        // Create button panel for list management
        JPanel listHeaderPanel = new JPanel(new BorderLayout());
        listHeaderPanel.setOpaque(false);
        listHeaderPanel.add(listsTitle, BorderLayout.WEST);
        
        // Add "Create New List" button
        JButton createListButton = new JButton("+ New List");
        createListButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        createListButton.setBackground(new Color(46, 204, 113));
        createListButton.setForeground(Color.WHITE);
        createListButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        createListButton.setFocusPainted(false);
        createListButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyListsDialog.this.showCreateListDialog();
            }
        });
        
        // Add hover effect
        createListButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                createListButton.setBackground(new Color(39, 174, 96));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                createListButton.setBackground(new Color(46, 204, 113));
            }
        });
        
        listHeaderPanel.add(createListButton, BorderLayout.EAST);
        leftPanel.add(listHeaderPanel, BorderLayout.NORTH);
        
        // Style the lists
        listsList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        listsList.setBackground(Color.WHITE);
        listsList.setSelectionBackground(new Color(52, 152, 219));
        listsList.setSelectionForeground(Color.WHITE);
        listsList.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        listsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                if (isSelected) {
                    setBackground(new Color(52, 152, 219));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(44, 62, 80));
                }
                return this;
            }
        });
        
        JScrollPane listsScrollPane = new JScrollPane(listsList);
        listsScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        listsScrollPane.setBackground(Color.WHITE);
        leftPanel.add(listsScrollPane, BorderLayout.CENTER);
        
        // Right panel - Books with modern card design
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(245, 247, 250));
        
        // Books header with add button
        JPanel booksHeaderPanel = new JPanel(new BorderLayout());
        booksHeaderPanel.setOpaque(false);
        
        JLabel booksTitle = new JLabel("📖 Books in Collection");
        booksTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        booksTitle.setForeground(new Color(52, 73, 94));
        booksHeaderPanel.add(booksTitle, BorderLayout.WEST);
        
        // Add "Add Books" button (initially hidden)
        addBooksButton = new JButton("+ Add Books");
        addBooksButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        addBooksButton.setBackground(new Color(52, 152, 219));
        addBooksButton.setForeground(Color.WHITE);
        addBooksButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        addBooksButton.setFocusPainted(false);
        addBooksButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBooksButton.setVisible(false); // Initially hidden
        addBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = listsList.getSelectedIndex();
                if (selectedIndex >= 0 && userLists != null && selectedIndex < userLists.size()) {
                    String[] selectedListData = userLists.get(selectedIndex);
                    showAddBooksDialog(selectedListData[0], selectedListData[1]);
                }
            }
        });
        
        // Add hover effect
        addBooksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                addBooksButton.setBackground(new Color(41, 128, 185));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                addBooksButton.setBackground(new Color(52, 152, 219));
            }
        });
        
        booksHeaderPanel.add(addBooksButton, BorderLayout.EAST);
        rightPanel.add(booksHeaderPanel, BorderLayout.NORTH);
        
        // Status label with modern styling
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        
        // Books panel container
        JPanel booksContainer = new JPanel(new BorderLayout());
        booksContainer.setBackground(Color.WHITE);
        booksContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        booksScrollPane.setBorder(null);
        booksScrollPane.setBackground(Color.WHITE);
        booksContainer.add(statusLabel, BorderLayout.NORTH);
        booksContainer.add(booksScrollPane, BorderLayout.CENTER);
        
        rightPanel.add(booksContainer, BorderLayout.CENTER);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        // Back to Home button with attractive styling
        JButton backToHomeButton = new JButton("🏠 Back to Home");
        backToHomeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        backToHomeButton.setBackground(new Color(52, 152, 219)); // Blue color
        backToHomeButton.setForeground(Color.WHITE);
        backToHomeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        backToHomeButton.setFocusPainted(false);
        backToHomeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToHomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close this dialog and refresh the landing page
                dispose();
                
                // Cast parent to LandingPage and refresh
                if (parent instanceof com.booktrack.ui.LandingPage) {
                    ((com.booktrack.ui.LandingPage) parent).refreshBooks();
                }
            }
        });
        
        // Add hover effect for back to home button
        backToHomeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backToHomeButton.setBackground(new Color(41, 128, 185)); // Darker blue on hover
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backToHomeButton.setBackground(new Color(52, 152, 219)); // Original blue
            }
        });
        
        JButton closeButton = new JButton("✕ Close");
        closeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        closeButton.setBackground(new Color(231, 76, 60));
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
                closeButton.setBackground(new Color(192, 57, 43));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setBackground(new Color(231, 76, 60));
            }
        });
        
        buttonPanel.add(backToHomeButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadUserLists() {
        SwingWorker<List<String[]>, Void> worker = new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                return userDAO.getUserCustomLists(userId);
            }
            
            @Override
            protected void done() {
                try {
                    userLists = get();
                    updateListsDisplay();
                } catch (Exception e) {
                    statusLabel.setText("Error loading lists: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void updateListsDisplay() {
        listsModel.clear();
        
        if (userLists == null || userLists.isEmpty()) {
            statusLabel.setText("No lists found. Lists should be created automatically when you register.");
            return;
        }
        
        for (String[] listData : userLists) {
            String listName = listData[1];
            String booksCount = listData[4];
            String isDefault = listData[5];
            
            String displayText = listName + " (" + booksCount + " books)";
            if ("1".equals(isDefault)) {
                displayText += " [Default]";
            }
            
            listsModel.addElement(displayText);
        }
        
        statusLabel.setText("Select a list to view books");
        
        // Auto-select first list if available
        if (!userLists.isEmpty()) {
            listsList.setSelectedIndex(0);
        }
    }
    
    private void loadBooksForSelectedList() {
        int selectedIndex = listsList.getSelectedIndex();
        if (selectedIndex < 0 || userLists == null || selectedIndex >= userLists.size()) {
            addBooksButton.setVisible(false);
            return;
        }
        
        String[] selectedListData = userLists.get(selectedIndex);
        String listId = selectedListData[0];
        String listName = selectedListData[1];
        
        // Show the add books button when a list is selected
        addBooksButton.setVisible(true);
        
        statusLabel.setText("Loading books from " + listName + "...");
        booksPanel.removeAll();
        booksPanel.revalidate();
        booksPanel.repaint();
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return bookDAO.getBooksInList(Integer.parseInt(listId));
            }
            
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    displayBooksInList(books, listName);
                } catch (Exception e) {
                    statusLabel.setText("Error loading books: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void displayBooksInList(List<Book> books, String listName) {
        booksPanel.removeAll();
        
        if (books == null || books.isEmpty()) {
            statusLabel.setText("📚 " + listName + " is empty");
            
            // Create modern empty state
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
            
            JLabel emptyIcon = new JLabel("📖");
            emptyIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 72));
            emptyIcon.setHorizontalAlignment(SwingConstants.CENTER);
            emptyIcon.setForeground(new Color(189, 195, 199));
            
            JLabel emptyTitle = new JLabel("No Books Yet");
            emptyTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            emptyTitle.setHorizontalAlignment(SwingConstants.CENTER);
            emptyTitle.setForeground(new Color(127, 140, 141));
            
            JLabel emptyDesc = new JLabel("<html><center>This collection is waiting for books!<br/>Start adding books to organize your reading journey.</center></html>");
            emptyDesc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            emptyDesc.setHorizontalAlignment(SwingConstants.CENTER);
            emptyDesc.setForeground(new Color(149, 165, 166));
            
            JPanel textPanel = new JPanel(new BorderLayout(0, 10));
            textPanel.setOpaque(false);
            textPanel.add(emptyTitle, BorderLayout.NORTH);
            textPanel.add(emptyDesc, BorderLayout.CENTER);
            
            emptyPanel.add(emptyIcon, BorderLayout.NORTH);
            emptyPanel.add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.CENTER);
            emptyPanel.add(textPanel, BorderLayout.SOUTH);
            
            booksPanel.add(emptyPanel);
        } else {
            statusLabel.setText("📚 " + listName + " • " + books.size() + " book" + (books.size() != 1 ? "s" : ""));
            
            for (Book book : books) {
                JPanel bookItemPanel = createBookItemPanel(book);
                booksPanel.add(bookItemPanel);
                booksPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Modern spacing
            }
        }
        
        booksPanel.revalidate();
        booksPanel.repaint();
        
        // Scroll to top with smooth animation feel
        SwingUtilities.invokeLater(() -> {
            booksScrollPane.getVerticalScrollBar().setValue(0);
        });
    }
    
    private JPanel createBookItemPanel(Book book) {
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
        bookIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 36));
        bookIcon.setHorizontalAlignment(SwingConstants.CENTER);
        bookIcon.setVerticalAlignment(SwingConstants.CENTER);
        bookIcon.setBackground(new Color(236, 240, 241));
        bookIcon.setOpaque(true);
        bookIcon.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        iconPanel.add(bookIcon, BorderLayout.CENTER);
        
        // Center - Book info
        JPanel infoPanel = new JPanel(new BorderLayout(5, 8));
        infoPanel.setOpaque(false);
        
        // Title with modern styling
        JLabel titleLabel = new JLabel(book.getTitle());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLabel.setForeground(new Color(44, 62, 80));
        
        // Rating section with clear labeling
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ratingPanel.setOpaque(false);
        
        JLabel ratingIcon = new JLabel("⭐ ");
        ratingIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JLabel ratingText = new JLabel("Your Rating: ");
        ratingText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        ratingText.setForeground(new Color(127, 140, 141));
        
        // Get the user's actual rating for this book
        Double userRating = null;
        try {
            Integer bookId = bookDAO.getBookIdByGoogleId(book.getGoogleBooksId());
            if (bookId != null) {
                userRating = bookDAO.getUserRating(userId, bookId);
            }
        } catch (Exception e) {
            System.err.println("Error fetching user rating: " + e.getMessage());
        }
        
        JLabel ratingValue = new JLabel(formatUserRating(userRating));
        ratingValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        ratingValue.setForeground(new Color(230, 126, 34));
        
        ratingPanel.add(ratingIcon);
        ratingPanel.add(ratingText);
        ratingPanel.add(ratingValue);
        
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(ratingPanel, BorderLayout.CENTER);
        
        // Right side - Action indicator
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(30, 80));
        
        JLabel actionIcon = new JLabel("▶");
        actionIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        actionIcon.setForeground(new Color(149, 165, 166));
        actionIcon.setHorizontalAlignment(SwingConstants.CENTER);
        actionIcon.setVerticalAlignment(SwingConstants.CENTER);
        
        actionPanel.add(actionIcon, BorderLayout.CENTER);

        // Add hover effects
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showBookDetails(book);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(248, 249, 250));
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                    BorderFactory.createEmptyBorder(14, 19, 14, 19)
                ));
                actionIcon.setForeground(new Color(52, 152, 219));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
                actionIcon.setForeground(new Color(149, 165, 166));
            }
        });

        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void showBookDetails(Book book) {
        // Open the BookCard in "My Lists" mode which shows ONLY user's rating and review
        BookCard bookCard = new BookCard(book, true);
        bookCard.showBookOptionsDialog();
    }
    
    private String formatUserRating(Double rating) {
        if (rating != null && rating > 0) {
            return String.format("%.1f/5.0", rating);
        }
        return "Not rated yet";
    }
    
    /**
     * Show dialog to create a new custom list
     */
    private void showCreateListDialog() {
        CreateCustomListDialog dialog = new CreateCustomListDialog(parent, new Runnable() {
            @Override
            public void run() {
                // Refresh the lists after creating a new one
                loadUserLists();
            }
        });
        dialog.setVisible(true);
    }
    
    /**
     * Show dialog to add books to a custom list
     */
    private void showAddBooksDialog(String listId, String listName) {
        AddBooksToListDialog dialog = new AddBooksToListDialog(parent, Integer.parseInt(listId), listName, new Runnable() {
            @Override
            public void run() {
                // Refresh the current list view after adding books
                loadBooksForSelectedList();
                // Also refresh the lists to update book counts
                loadUserLists();
            }
        });
        dialog.setVisible(true);
    }
    
    /**
     * Show context menu for custom lists (edit, delete, add books)
     */
    private void showListContextMenu(String[] listData, int x, int y) {
        String listId = listData[0];
        String listName = listData[1];
        String listType = listData[3];
        String isDefault = listData[5];
        
        JPopupMenu contextMenu = new JPopupMenu();
        
        // Add Books option (for all lists)
        JMenuItem addBooksItem = new JMenuItem("📚 Add Books");
        addBooksItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addBooksItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddBooksDialog(listId, listName);
            }
        });
        contextMenu.add(addBooksItem);
        
        // Only show edit/delete for custom lists (not default ones)
        if ("0".equals(isDefault) && "CUSTOM".equals(listType)) {
            contextMenu.addSeparator();
            
            // Edit List option
            JMenuItem editItem = new JMenuItem("✏️ Edit List");
            editItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            editItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showEditListDialog(listData);
                }
            });
            contextMenu.add(editItem);
            
            // Delete List option
            JMenuItem deleteItem = new JMenuItem("🗑️ Delete List");
            deleteItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            deleteItem.setForeground(new Color(231, 76, 60));
            deleteItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteCustomList(listData);
                }
            });
            contextMenu.add(deleteItem);
        }
        
        contextMenu.show(listsList, x, y);
    }
    
    /**
     * Show dialog to edit a custom list
     */
    private void showEditListDialog(String[] listData) {
        // For now, just show a simple dialog. You could create a full EditCustomListDialog later
        String listId = listData[0];
        String currentName = listData[1];
        String currentDescription = listData[2];
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("List Name:"));
        JTextField nameField = new JTextField(currentName);
        panel.add(nameField);
        
        panel.add(new JLabel("Description:"));
        JTextArea descArea = new JTextArea(currentDescription, 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        panel.add(descScroll);
        
        panel.add(new JLabel("Public:"));
        JCheckBox publicCheck = new JCheckBox("", true); // Assume public for now
        panel.add(publicCheck);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit List: " + currentName, 
                                                   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newDescription = descArea.getText().trim();
            int isPublic = publicCheck.isSelected() ? 1 : 0;
            
            if (!newName.isEmpty()) {
                SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
                    @Override
                    protected String[] doInBackground() throws Exception {
                        return userDAO.updateCustomList(userId, Integer.parseInt(listId), newName, newDescription, isPublic);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            String[] updateResult = get();
                            if ("1".equals(updateResult[0])) {
                                JOptionPane.showMessageDialog(MyListsDialog.this, "List updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                loadUserLists(); // Refresh the list
                            } else {
                                JOptionPane.showMessageDialog(MyListsDialog.this, updateResult[1], "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(MyListsDialog.this, "Error updating list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        }
    }
    
    /**
     * Delete a custom list after confirmation
     */
    private void deleteCustomList(String[] listData) {
        String listId = listData[0];
        String listName = listData[1];
        String booksCount = listData[4];
        
        String message = "Are you sure you want to delete the list \"" + listName + "\"?";
        if (Integer.parseInt(booksCount) > 0) {
            message += "\n\nThis list contains " + booksCount + " book(s). They will be removed from the list but not deleted from the database.";
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", 
                                                   JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
                @Override
                protected String[] doInBackground() throws Exception {
                    return userDAO.deleteCustomList(userId, Integer.parseInt(listId));
                }
                
                @Override
                protected void done() {
                    try {
                        String[] deleteResult = get();
                        if ("1".equals(deleteResult[0])) {
                            JOptionPane.showMessageDialog(MyListsDialog.this, deleteResult[1], "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadUserLists(); // Refresh the list
                            // Clear the books panel since the selected list was deleted
                            booksPanel.removeAll();
                            booksPanel.revalidate();
                            booksPanel.repaint();
                            statusLabel.setText("Select a list to view books");
                        } else {
                            JOptionPane.showMessageDialog(MyListsDialog.this, deleteResult[1], "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MyListsDialog.this, "Error deleting list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}
