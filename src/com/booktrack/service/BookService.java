package com.booktrack.service;

import com.booktrack.dao.BookDAO;
import com.booktrack.model.Book;

import java.util.List;

/**
 * Book Service - handles book-related business logic
 * Coordinates between Google Books API and database operations
 */
public class BookService {
    private final BookDAO bookDAO;
    private final GoogleBooksService googleBooksService;
    
    public BookService() {
        this.bookDAO = new BookDAO();
        this.googleBooksService = new GoogleBooksService();
    }

    public String getLastMessage() {
        return bookDAO.getLastMessage();
    }
    
    // Constructor for dependency injection (testing)
    public BookService(BookDAO bookDAO, GoogleBooksService googleBooksService) {
        this.bookDAO = bookDAO;
        this.googleBooksService = googleBooksService;
    }
    
    /**
     * Search books - first check database, then Google Books API
     * @param query Search query
     * @param maxResults Maximum results to return
     * @return List of books
     */
    public List<Book> searchBooks(String query, int maxResults) {
        // First search in local database
        List<Book> localBooks = bookDAO.searchBooks(query);
        
        // If we have enough results from database, return them
        if (localBooks.size() >= maxResults) {
            return localBooks.subList(0, maxResults);
        }
        
        // Otherwise, search Google Books API for fresh results
        List<Book> apiBooks = googleBooksService.searchBooks(query, maxResults);
        
        // Combine results, prioritizing local books (users' saved books)
        if (!localBooks.isEmpty()) {
            // Add API books that aren't already in local results
            for (Book apiBook : apiBooks) {
                boolean exists = localBooks.stream()
                    .anyMatch(localBook -> 
                        apiBook.getGoogleBooksId() != null && 
                        apiBook.getGoogleBooksId().equals(localBook.getGoogleBooksId())
                    );
                if (!exists && localBooks.size() < maxResults) {
                    localBooks.add(apiBook);
                }
            }
            return localBooks;
        }
        
        return apiBooks;
    }
    
    /**
     * Get books for landing page (popular books)
     * Optimized for speed - skips individual DB lookups
     * @return List of popular books
     */
    public List<Book> getLandingPageBooks() {
        // Fetch books from Google Books API
        List<Book> books = googleBooksService.getLandingPageBooks();

        // For performance, we'll skip individual database lookups for landing page
        // The books already have default values (0.0 for rating, 0 for count)
        return books;
    }
    
    /**
     * Add book to user's list
     * This method first saves the book to database, then adds to user's list
     * @param userId User ID
     * @param listType List type
     * @param book Book to add
     * @param notes Optional notes
     * @return true if successful
     */
    public boolean addBookToUserList(Integer userId, String listType, Book book, String notes) {
        try {
            // First, ensure the book exists in our database
            Integer bookId = bookDAO.addOrUpdateBook(book);
            
            if (bookId == null) {
                System.err.println("Failed to save book to database");
                return false;
            }
            
            // Map the list type to database format
            String dbListType = mapListTypeToDatabase(listType);
            
            // Then add the book to user's list
            return bookDAO.addBookToList(userId, dbListType, bookId, notes);
            
        } catch (Exception e) {
            System.err.println("Error adding book to user list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map UI list types to database list types
     * @param uiListType UI list type (to_read, currently_reading, etc.)
     * @return Database list type (WANT_TO_READ, CURRENTLY_READING, etc.)
     */
    private String mapListTypeToDatabase(String uiListType) {
        switch (uiListType.toLowerCase()) {
            case "to_read":
            case "want_to_read":
                return "WANT_TO_READ";
            case "currently_reading":
            case "reading":
                return "CURRENTLY_READING";
            case "read":
            case "have_read":
            case "finished":
                return "HAVE_READ";
            case "favorites":
            case "favourite":
            case "fav":
                return "FAVORITES";
            default:
                // If it's already in database format, return as is
                return uiListType.toUpperCase();
        }
    }
    
    /**
     * Remove book from user's list
     * @param userId User ID
     * @param listType List type
     * @param book Book to remove
     * @return true if successful
     */
    public boolean removeBookFromUserList(Integer userId, String listType, Book book) {
        try {
            // Find the book ID in our database
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            
            if (bookId == null) {
                System.err.println("Book not found in database");
                return false;
            }
            
            // Map the list type to database format
            String dbListType = mapListTypeToDatabase(listType);
            
            return bookDAO.removeBookFromList(userId, dbListType, bookId);
            
        } catch (Exception e) {
            System.err.println("Error removing book from user list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if book is in user's specific list
     * @param userId User ID
     * @param listType List type
     * @param book Book to check
     * @return true if book is in list
     */
    public boolean isBookInUserList(Integer userId, String listType, Book book) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            
            if (bookId == null) {
                return false; // Book not in database means not in any list
            }
            
            // Map the list type to database format
            String dbListType = mapListTypeToDatabase(listType);
            
            return bookDAO.isBookInList(userId, dbListType, bookId);
            
        } catch (Exception e) {
            System.err.println("Error checking if book is in user list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get detailed book information
     * First tries to get from database, then from Google Books API
     * @param googleBooksId Google Books ID
     * @return Book with detailed information
     */
    public Book getBookDetails(String googleBooksId) {
        try {
            // First try to get from database
            Integer bookId = findBookIdByGoogleId(googleBooksId);
            if (bookId != null) {
                Book book = bookDAO.getBookById(bookId);
                if (book != null) {
                    return book;
                }
            }
            
            // If not in database, get from API (this would require a single book API call)
            // For now, we'll return null since GoogleBooksService doesn't have a getBookById method
            // You could extend GoogleBooksService to add this functionality
            return null;
            
        } catch (Exception e) {
            System.err.println("Error getting book details: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get books in a specific user list
     * @param listId List ID
     * @return List of books
     */
    public List<Book> getBooksInList(Integer listId) {
        return bookDAO.getBooksInList(listId);
    }
    
    /**
     * Helper method to find book ID by Google Books ID
     * This searches our database for the book
     * @param googleBooksId Google Books ID
     * @return Book ID or null if not found
     */
    private Integer findBookIdByGoogleId(String googleBooksId) {
        return bookDAO.getBookIdByGoogleId(googleBooksId);
    }

    /**
     * Populate the given Book with aggregate rating/count from DB if it exists there.
     * Returns true if DB values were applied; false otherwise.
     */
    public boolean populateDbAggregateRating(Book book) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            if (bookId == null) return false;
            Book db = bookDAO.getBookById(bookId);
            if (db == null) return false;
            book.setAverageRating(db.getAverageRating());
            book.setRatingsCount(db.getRatingsCount());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Quick search for books (combines local and API results)
     * @param category Category to search
     * @return List of books
     */
    public List<Book> quickSearch(String category) {
        return searchBooks(category, 12);
    }
    
    /**
     * Rate a book
     * @param userId User ID
     * @param book Book to rate
     * @param rating Rating value (1-5)
     * @return true if successful
     */
    public boolean rateBook(Integer userId, Book book, double rating) {
        try {
            // First, ensure the book exists in our database
            Integer bookId = bookDAO.addOrUpdateBook(book);
            
            if (bookId == null) {
                System.err.println("Failed to save book to database");
                return false;
            }
            
            // Rate the book
            return bookDAO.rateBook(userId, bookId, rating);
            
        } catch (Exception e) {
            System.err.println("Error rating book: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Review a book
     * @param userId User ID
     * @param book Book to review
     * @param reviewTitle Review title
     * @param reviewText Review text
     * @param isSpoiler Is spoiler
     * @param isPublic Is public
     * @return true if successful
     */
    public boolean reviewBook(Integer userId, Book book, String reviewTitle, String reviewText, 
                             boolean isSpoiler, boolean isPublic) {
        try {
            // First, ensure the book exists in our database
            Integer bookId = bookDAO.addOrUpdateBook(book);
            
            if (bookId == null) {
                System.err.println("Failed to save book to database");
                return false;
            }
            
            // Review the book
            return bookDAO.reviewBook(userId, bookId, reviewTitle, reviewText, isSpoiler, isPublic);
            
        } catch (Exception e) {
            System.err.println("Error reviewing book: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user's rating for a book
     * @param userId User ID
     * @param book Book to check
     * @return Rating value or null if not found
     */
    public Double getUserRating(Integer userId, Book book) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            if (bookId == null) {
                return null; // Book not in database
            }
            
            return bookDAO.getUserRating(userId, bookId);
            
        } catch (Exception e) {
            System.err.println("Error getting user rating: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user's review for a book
     * @param userId User ID
     * @param book Book to check
     * @return Review data as array [title, text, isSpoiler, isPublic] or null
     */
    public String[] getUserReview(Integer userId, Book book) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            if (bookId == null) {
                return null; // Book not in database
            }
            
            return bookDAO.getUserReview(userId, bookId);
            
        } catch (Exception e) {
            System.err.println("Error getting user review: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all public reviews for a book
     * @param book Book to get reviews for
     * @param limit Maximum number of reviews
     * @return List of review data arrays
     */
    public java.util.List<String[]> getBookReviews(Book book, int limit) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            
            if (bookId == null) {
                return new java.util.ArrayList<>(); // No reviews if book not in database
            }
            
            return bookDAO.getBookReviews(bookId, limit);
            
        } catch (Exception e) {
            System.err.println("Error getting book reviews: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get rating statistics for a book
     * @param book Book to get stats for
     * @return Array with [average_rating, total_ratings, 5_star, 4_star, 3_star, 2_star, 1_star]
     */
    public double[] getBookRatingStats(Book book) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            
            if (bookId == null) {
                return new double[]{0, 0, 0, 0, 0, 0, 0}; // No ratings if book not in database
            }
            
            return bookDAO.getBookRatingStats(bookId);
            
        } catch (Exception e) {
            System.err.println("Error getting book rating stats: " + e.getMessage());
            return new double[]{0, 0, 0, 0, 0, 0, 0};
        }
    }
    
    /**
     * Get total review count for a book
     * @param book Book to get review count for
     * @return Review count
     */
    public int getBookReviewCount(Book book) {
        try {
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            
            if (bookId == null) {
                return 0; // No reviews if book not in database
            }
            
            return bookDAO.getBookReviewCount(bookId);
            
        } catch (Exception e) {
            System.err.println("Error getting book review count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Rate and review a book (combines rating and review submission)
     * @param book Book to rate and review
     * @param rating Rating (1-5)
     * @param reviewText Review text (can be null)
     * @param isPublic Whether review should be public
     * @return True if successful
     */
    public boolean rateAndReviewBook(Book book, int rating, String reviewText, boolean isPublic) {
        try {
            // First ensure book exists in database
            Integer bookId = findBookIdByGoogleId(book.getGoogleBooksId());
            
            if (bookId == null) {
                // Add book to database first using the stored procedure that handles authors
                bookId = bookDAO.addOrUpdateBook(book);
                if (bookId == null) {
                    return false;
                }
            }
            
            // Submit rating and review
            boolean success = bookDAO.rateBook(com.booktrack.session.SessionManager.getInstance().getCurrentUserId(), 
                                               bookId, rating, reviewText, isPublic);
            
            return success;
            
        } catch (Exception e) {
            System.err.println("Error rating and reviewing book: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get access to Google Books Service for API calls
     */
    public GoogleBooksService getGoogleBooksService() {
        return this.googleBooksService;
    }
    
    /**
     * Update authors for a book by fetching fresh data from Google Books API
     * Returns a Book with updated author information
     */
    public Book updateBookAuthors(Book book) {
        try {
            if (book == null) {
                return book;
            }
            
            String searchTerm = null;
            // Try ISBN13 first, then ISBN10, then title search
            if (book.getIsbn13() != null && !book.getIsbn13().isEmpty()) {
                searchTerm = "isbn:" + book.getIsbn13();
            } else if (book.getIsbn10() != null && !book.getIsbn10().isEmpty()) {
                searchTerm = "isbn:" + book.getIsbn10();
            } else if (book.getTitle() != null && !book.getTitle().isEmpty()) {
                searchTerm = book.getTitle();
            }
            
            if (searchTerm != null) {
                // Search for the book to get fresh author data
                List<Book> searchResults = googleBooksService.searchBooks(searchTerm, 5);
                
                if (!searchResults.isEmpty()) {
                    Book freshBook = searchResults.get(0);
                    if (freshBook.getAuthors() != null && freshBook.getAuthors().length > 0) {
                        // Update the book object with fresh authors
                        book.setAuthors(freshBook.getAuthors());
                        return book;
                    }
                }
            }
            
            return book;
            
        } catch (Exception e) {
            System.err.println("Error updating book authors: " + e.getMessage());
            return book;
        }
    }
}
