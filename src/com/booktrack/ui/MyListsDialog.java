package com.booktrack.ui;

import com.booktrack.dao.BookDAO;
import com.booktrack.dao.UserDAO;
import com.booktrack.model.Book;
import com.booktrack.ui.component.BookCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * My Lists Dialog - Shows user's custom book lists
 */
public class MyListsDialog extends JDialog {
    private final Integer userId;
    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private JList<String> listJList;
    private JPanel booksPanel;
    private JLabel statusLabel;
    private JScrollPane booksScrollPane;
    private DefaultListModel<String> listModel;
    private List<String[]> userLists;
    
    public MyListsDialog(Frame parent, Integer userId) {
        super(parent, "My Lists", true);
        this.userId = userId;
        this.userDAO = new UserDAO();
        this.bookDAO = new BookDAO();
        
        initializeDialog();
        setupLayout();
        loadUserLists();
    }
    
    private void initializeDialog() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("My Book Lists", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(titleLabel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Left panel - Lists
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Your Lists"));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        
        listModel = new DefaultListModel<>();
        listJList = new JList<>(listModel);
        listJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadBooksInSelectedList();
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(listJList);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Right panel - Books in selected list
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Books in List"));
        
        statusLabel = new JLabel("Select a list to view books");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(statusLabel, BorderLayout.NORTH);
        
        booksPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        booksPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        booksScrollPane = new JScrollPane(booksPanel);
        booksScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        booksScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightPanel.add(booksScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Bottom panel - Close button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
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
                    listModel.clear();
                    
                    if (userLists.isEmpty()) {
                        listModel.addElement("No lists found");
                        statusLabel.setText("No lists available");
                    } else {
                        for (String[] list : userLists) {
                            String listName = list[1]; // list_name
                            String booksCount = list[4]; // books_count
                            boolean isDefault = "1".equals(list[5]); // is_default
                            
                            String displayName = listName + " (" + booksCount + " books)" + 
                                               (isDefault ? " [Default]" : "");
                            listModel.addElement(displayName);
                        }
                        statusLabel.setText("Select a list to view books");
                    }
                } catch (Exception e) {
                    listModel.addElement("Error loading lists");
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void loadBooksInSelectedList() {
        int selectedIndex = listJList.getSelectedIndex();
        if (selectedIndex < 0 || userLists.isEmpty() || selectedIndex >= userLists.size()) {
            return;
        }
        
        String[] selectedList = userLists.get(selectedIndex);
        String listId = selectedList[0]; // list_id
        String listName = selectedList[1]; // list_name
        
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
                    booksPanel.removeAll();
                    
                    if (books.isEmpty()) {
                        statusLabel.setText("No books in " + listName);
                        JLabel emptyLabel = new JLabel("This list is empty. Add books by clicking on books and selecting this list.");
                        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        emptyLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
                        emptyLabel.setForeground(Color.GRAY);
                        booksPanel.add(emptyLabel);
                    } else {
                        statusLabel.setText(books.size() + " books in " + listName);
                        
                        for (Book book : books) {
                            BookCard bookCard = new BookCard(book, true); // Set My Lists mode
                            booksPanel.add(bookCard);
                        }
                    }
                    
                    booksPanel.revalidate();
                    booksPanel.repaint();
                    
                } catch (Exception e) {
                    statusLabel.setText("Error loading books: " + e.getMessage());
                    booksPanel.removeAll();
                    
                    JLabel errorLabel = new JLabel("Error loading books: " + e.getMessage());
                    errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    booksPanel.add(errorLabel);
                    
                    booksPanel.revalidate();
                    booksPanel.repaint();
                }
            }
        };
        worker.execute();
    }
}
