package com.booktrack.dao;

import com.booktrack.config.DatabaseConfig;
import com.booktrack.model.Book;

import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Book Data Access Object
 * Handles all database operations related to books
 */
public class BookDAO {
    private final DatabaseConfig dbConfig;
    
    public BookDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Add or update book from Google Books API data
     * @param book Book object with API data
     * @return Book ID if successful, null if failed
     */
    public Integer addOrUpdateBook(Book book) {
        String sql = "{ call sp_add_or_update_book(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set input parameters
            stmt.setString(1, book.getGoogleBooksId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getSubtitle());
            
            if (book.getDescription() != null) {
                stmt.setClob(4, new StringReader(book.getDescription()));
            } else {
                stmt.setNull(4, Types.CLOB);
            }
            
            stmt.setString(5, book.getIsbn10());
            stmt.setString(6, book.getIsbn13());
            stmt.setString(7, book.getPublishedDate());
            stmt.setString(8, book.getPublisher());
            
            if (book.getPageCount() > 0) {
                stmt.setInt(9, book.getPageCount());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            stmt.setString(10, book.getLanguage());
            stmt.setString(11, book.getThumbnailUrl());
            stmt.setString(12, book.getPreviewLink());
            stmt.setString(13, book.getInfoLink());
            
            if (book.getAverageRating() > 0) {
                stmt.setDouble(14, book.getAverageRating());
            } else {
                stmt.setNull(14, Types.DOUBLE);
            }
            
            if (book.getRatingsCount() > 0) {
                stmt.setInt(15, book.getRatingsCount());
            } else {
                stmt.setNull(15, Types.INTEGER);
            }
            
            // Register output parameters
            stmt.registerOutParameter(16, Types.INTEGER);   // p_result
            stmt.registerOutParameter(17, Types.VARCHAR);   // p_message
            stmt.registerOutParameter(18, Types.INTEGER);   // p_book_id
            
            stmt.execute();
            
            int result = stmt.getInt(16);
            String message = stmt.getString(17);
            Integer bookId = stmt.getInt(18);
            
            if (result == 1) {
                return bookId;
            } else {
                System.err.println("Failed to add/update book: " + message);
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding/updating book: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Add book to user's list
     * @param userId User ID
     * @param listType List type (WANT_TO_READ, CURRENTLY_READING, HAVE_READ, FAVORITES)
     * @param bookId Book ID
     * @param notes Optional notes
     * @return true if successful
     */
    public boolean addBookToList(Integer userId, String listType, Integer bookId, String notes) {
        String sql = "{ call sp_add_book_to_list(?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, listType);
            stmt.setInt(3, bookId);
            
            if (notes != null && !notes.trim().isEmpty()) {
                stmt.setClob(4, new StringReader(notes));
            } else {
                stmt.setNull(4, Types.CLOB);
            }
            
            stmt.registerOutParameter(5, Types.INTEGER);   // p_result
            stmt.registerOutParameter(6, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(5);
            String message = stmt.getString(6);
            
            if (result == 1) {
                return true;
            } else {
                System.err.println("Failed to add book to list: " + message);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding book to list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove book from user's list
     * @param userId User ID
     * @param listType List type
     * @param bookId Book ID
     * @return true if successful
     */
    public boolean removeBookFromList(Integer userId, String listType, Integer bookId) {
        String sql = "{ call sp_remove_book_from_list(?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, listType);
            stmt.setInt(3, bookId);
            
            stmt.registerOutParameter(4, Types.INTEGER);   // p_result
            stmt.registerOutParameter(5, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(4);
            return result == 1;
            
        } catch (SQLException e) {
            System.err.println("Error removing book from list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if book is in user's specific list
     * @param userId User ID
     * @param listType List type
     * @param bookId Book ID
     * @return true if book is in list
     */
    public boolean isBookInList(Integer userId, String listType, Integer bookId) {
        String sql = "{ ? = call fn_is_book_in_list(?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, userId);
            stmt.setString(3, listType);
            stmt.setInt(4, bookId);
            
            stmt.execute();
            
            return stmt.getInt(1) > 0;
            
        } catch (SQLException e) {
            System.err.println("Error checking if book is in list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get book details by ID
     * @param bookId Book ID
     * @return Book object or null
     */
    public Book getBookById(Integer bookId) {
        String sql = "{ ? = call fn_get_book_details(?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, bookId);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting book by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Search books in database
     * @param searchTerm Search term
     * @return List of books
     */
    public List<Book> searchBooks(String searchTerm) {
        String sql = "{ ? = call fn_search_books(?) }";
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, searchTerm);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * Get books in a specific list
     * @param listId List ID
     * @return List of books with notes
     */
    public List<Book> getBooksInList(Integer listId) {
        String sql = "{ ? = call fn_get_books_in_list(?) }";
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, listId);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    Book book = mapResultSetToBook(rs);
                    // Add notes if available
                    String notes = rs.getString("notes");
                    if (notes != null && !notes.trim().isEmpty()) {
                        // You could add a notes field to Book model or handle separately
                        // For now, we'll add it to description
                        book.setDescription((book.getDescription() != null ? book.getDescription() + "\n\nNotes: " : "Notes: ") + notes);
                    }
                    books.add(book);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting books in list: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * Helper method to map ResultSet to Book object
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        
        book.setGoogleBooksId(rs.getString("google_books_id"));
        book.setTitle(rs.getString("title"));
        book.setSubtitle(rs.getString("subtitle"));
        book.setDescription(rs.getString("description"));
        
        // Handle ISBN columns - they might not exist in all queries
        try {
            if (rs.getString("isbn_10") != null) {
                book.setIsbn10(rs.getString("isbn_10"));
            }
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            if (rs.getString("isbn_13") != null) {
                book.setIsbn13(rs.getString("isbn_13"));
            }
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        Date publishedDate = rs.getDate("published_date");
        if (publishedDate != null) {
            book.setPublishedDate(publishedDate.toString());
        }
        
        book.setPublisher(rs.getString("publisher"));
        book.setPageCount(rs.getInt("page_count"));
        
        // Handle language column - might be language_code or language
        try {
            book.setLanguage(rs.getString("language_code"));
        } catch (SQLException e) {
            try {
                book.setLanguage(rs.getString("language"));
            } catch (SQLException e2) {
                // Column doesn't exist, ignore
            }
        }
        
        // Handle image URL column - might be cover_image_url or thumbnail_url
        try {
            book.setThumbnailUrl(rs.getString("cover_image_url"));
        } catch (SQLException e) {
            try {
                book.setThumbnailUrl(rs.getString("thumbnail_url"));
            } catch (SQLException e2) {
                // Column doesn't exist, ignore
            }
        }
        
        // Handle preview and info links - they might not exist in all queries
        try {
            book.setPreviewLink(rs.getString("preview_link"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            book.setInfoLink(rs.getString("info_link"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        book.setAverageRating(rs.getDouble("average_rating"));
        book.setRatingsCount(rs.getInt("ratings_count"));
        
        return book;
    }
    
    /**
     * Get book ID by Google Books ID
     * @param googleBooksId Google Books ID
     * @return Book ID or null if not found
     */
    public Integer getBookIdByGoogleId(String googleBooksId) {
        if (googleBooksId == null || googleBooksId.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT book_id FROM books WHERE google_books_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, googleBooksId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("book_id");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting book ID by Google ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Rate a book
     * @param userId User ID
     * @param bookId Book ID
     * @param rating Rating value (1-5)
     * @return true if successful
     */
    public boolean rateBook(Integer userId, Integer bookId, double rating) {
        String sql = "{ call sp_rate_book(?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setDouble(3, rating);
            
            stmt.registerOutParameter(4, Types.INTEGER);   // p_result
            stmt.registerOutParameter(5, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(4);
            String message = stmt.getString(5);
            
            if (result == 1) {
                return true;
            } else {
                System.err.println("Failed to rate book: " + message);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error rating book: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Review a book
     * @param userId User ID
     * @param bookId Book ID
     * @param reviewTitle Review title
     * @param reviewText Review text
     * @param isSpoiler Is spoiler (1 or 0)
     * @param isPublic Is public (1 or 0)
     * @return true if successful
     */
    public boolean reviewBook(Integer userId, Integer bookId, String reviewTitle, String reviewText, 
                             boolean isSpoiler, boolean isPublic) {
        String sql = "{ call sp_review_book(?, ?, ?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setString(3, reviewTitle);
            stmt.setClob(4, new StringReader(reviewText));
            stmt.setInt(5, isSpoiler ? 1 : 0);
            stmt.setInt(6, isPublic ? 1 : 0);
            
            stmt.registerOutParameter(7, Types.INTEGER);   // p_result
            stmt.registerOutParameter(8, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(7);
            String message = stmt.getString(8);
            
            if (result == 1) {
                return true;
            } else {
                System.err.println("Failed to review book: " + message);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error reviewing book: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user's rating for a book
     * @param userId User ID
     * @param bookId Book ID
     * @return Rating value or null if not found
     */
    public Double getUserRating(Integer userId, Integer bookId) {
        String sql = "{ ? = call fn_get_user_rating(?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.DOUBLE);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            
            stmt.execute();
            
            double rating = stmt.getDouble(1);
            return stmt.wasNull() ? null : rating;
            
        } catch (SQLException e) {
            System.err.println("Error getting user rating: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user's review for a book
     * @param userId User ID
     * @param bookId Book ID
     * @return Review data as array [title, text, isSpoiler, isPublic] or null
     */
    public String[] getUserReview(Integer userId, Integer bookId) {
        String sql = "{ ? = call fn_get_user_review(?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new String[] {
                        rs.getString("review_title"),
                        rs.getString("review_text"),
                        String.valueOf(rs.getInt("is_spoiler")),
                        String.valueOf(rs.getInt("is_public"))
                    };
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user review: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Delete user's rating for a book
     * @param userId User ID
     * @param bookId Book ID
     * @return true if successful
     */
    public boolean deleteUserRating(Integer userId, Integer bookId) {
        String sql = "{ call sp_delete_rating(?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            stmt.registerOutParameter(3, Types.INTEGER);   // p_result
            stmt.registerOutParameter(4, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(3);
            return result == 1;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user rating: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete user's review for a book
     * @param userId User ID
     * @param bookId Book ID
     * @return true if successful
     */
    public boolean deleteUserReview(Integer userId, Integer bookId) {
        String sql = "{ call sp_delete_review(?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            stmt.registerOutParameter(3, Types.INTEGER);   // p_result
            stmt.registerOutParameter(4, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(3);
            return result == 1;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user review: " + e.getMessage());
            return false;
        }
    }
}
