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
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JLabel headerLabel = new JLabel("My Book Lists");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(headerLabel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        // Left panel - Lists
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Your Lists"));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        
        JScrollPane listsScrollPane = new JScrollPane(listsList);
        listsScrollPane.setPreferredSize(new Dimension(230, 0));
        leftPanel.add(listsScrollPane, BorderLayout.CENTER);
        
        // Right panel - Books in selected list
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Books in List"));
        
        rightPanel.add(statusLabel, BorderLayout.NORTH);
        rightPanel.add(booksScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
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
            return;
        }
        
        String[] selectedListData = userLists.get(selectedIndex);
        String listId = selectedListData[0];
        String listName = selectedListData[1];
        
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
            statusLabel.setText("No books in " + listName);
            JLabel emptyLabel = new JLabel("This list is empty. Add books by clicking on books and selecting this list.");
            emptyLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            booksPanel.add(emptyLabel);
        } else {
            statusLabel.setText(listName + " - " + books.size() + " book" + (books.size() != 1 ? "s" : ""));
            
            for (Book book : books) {
                JPanel bookItemPanel = createBookItemPanel(book);
                booksPanel.add(bookItemPanel);
                booksPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
            }
        }
        
        booksPanel.revalidate();
        booksPanel.repaint();
        
        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            booksScrollPane.getVerticalScrollBar().setValue(0);
        });
    }
    
    private JPanel createBookItemPanel(Book book) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setPreferredSize(new Dimension(0, 80));
        
        // Book info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(book.getTitle());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        
        JLabel authorLabel = new JLabel("by " + book.getAuthorsAsString());
        authorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        authorLabel.setForeground(Color.GRAY);
        
        JLabel ratingLabel = new JLabel(formatRating(book.getAverageRating()));
        ratingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        ratingLabel.setForeground(Color.ORANGE.darker());
        
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(authorLabel, BorderLayout.CENTER);
        infoPanel.add(ratingLabel, BorderLayout.SOUTH);

        // Make the entire panel clickable instead of a separate button
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showBookDetails(book);
            }
        });

        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void showBookDetails(Book book) {
        // Open the BookCard in "My Lists" mode which shows ONLY user's rating and review
        BookCard bookCard = new BookCard(book, true);
        bookCard.showBookOptionsDialog();
    }
    
    private String formatRating(double rating) {
        if (rating > 0) {
            return "★ " + String.format("%.1f", rating);
        }
        return "☆ No rating";
    }
}
