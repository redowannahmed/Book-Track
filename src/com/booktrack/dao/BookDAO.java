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
    private String lastMessage;
    
    public BookDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    public String getLastMessage() {
        return lastMessage;
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
            this.lastMessage = message;
            
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
            this.lastMessage = stmt.getString(5);
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

                    // FALLBACK: if authors were not populated from the result set,
                    // query the authors table directly using the book_id from the result set.
                    if (book.getAuthors() == null || book.getAuthors().length == 0) {
                        try {
                            int bookId = rs.getInt("book_id");
                            String[] authors = fetchAuthorsForBook(conn, bookId);
                            if (authors != null && authors.length > 0) {
                                book.setAuthors(authors);
                            }
                        } catch (SQLException ignored) {
                            // If book_id column isn't present, we can't do the fallback.
                            // We swallow it so the rest of the book loads normally.
                        }
                    }

                    // Add notes to description if available (existing logic)
                    String notes = null;
                    try {
                        notes = rs.getString("notes");
                    } catch (SQLException ignored) { }
                    if (notes != null && !notes.trim().isEmpty()) {
                        book.setDescription((book.getDescription() != null ? book.getDescription() + "\n\nNotes: " : "Notes: ")
                                            + notes);
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
 * Helper to fetch authors for a book id (returns null if none)
 */
    private String[] fetchAuthorsForBook(Connection conn, int bookId) throws SQLException {
        String sql = "SELECT a.author_name FROM authors a " +
                    "JOIN book_authors ba ON a.author_id = ba.author_id " +
                    "WHERE ba.book_id = ? ORDER BY a.author_name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet ars = ps.executeQuery()) {
                java.util.List<String> list = new java.util.ArrayList<>();
                while (ars.next()) {
                    String name = ars.getString(1);
                    if (name != null && !name.trim().isEmpty()) {
                        list.add(name.trim());
                    }
                }
                return list.isEmpty() ? null : list.toArray(new String[0]);
            }
        }
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
        
        // Handle authors - might be concatenated string from database
        try {
            String authorsString = rs.getString("authors");
            if (authorsString != null && !authorsString.trim().isEmpty()) {
                book.setAuthors(authorsString.split(", "));
            }
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
    
    /**
     * Get all public reviews for a book
     * @param bookId Book ID
     * @param limit Maximum number of reviews to return
     * @return List of review data arrays
     */
    public java.util.List<String[]> getBookReviews(Integer bookId, int limit) {
        String sql = "{ ? = call fn_get_book_reviews(?, ?) }";
        java.util.List<String[]> reviews = new java.util.ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, bookId);
            stmt.setInt(3, limit);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    String[] review = new String[8];
                    review[0] = rs.getString("review_title");
                    review[1] = rs.getString("review_text");
                    review[2] = String.valueOf(rs.getInt("is_spoiler"));
                    review[3] = rs.getString("username");
                    review[4] = rs.getString("first_name");
                    review[5] = rs.getString("last_name");
                    review[6] = rs.getTimestamp("created_at").toString();
                    review[7] = rs.getObject("user_rating") != null ? String.valueOf(rs.getDouble("user_rating")) : null;
                    reviews.add(review);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting book reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Get rating statistics for a book
     * @param bookId Book ID
     * @return Array with [average_rating, total_ratings, 5_star, 4_star, 3_star, 2_star, 1_star]
     */
    public double[] getBookRatingStats(Integer bookId) {
        String sql = "{ ? = call fn_get_book_rating_stats(?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, bookId);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new double[]{
                        rs.getDouble("average_rating"),
                        rs.getDouble("total_ratings"),
                        rs.getDouble("five_stars"),
                        rs.getDouble("four_stars"),
                        rs.getDouble("three_stars"),
                        rs.getDouble("two_stars"),
                        rs.getDouble("one_star")
                    };
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting book rating stats: " + e.getMessage());
        }
        
        return new double[]{0, 0, 0, 0, 0, 0, 0}; // Default values
    }
    
    /**
     * Get total review count for a book
     * @param bookId Book ID
     * @return Review count
     */
    public int getBookReviewCount(Integer bookId) {
        String sql = "{ ? = call fn_get_book_review_count(?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, bookId);
            
            stmt.execute();
            
            return stmt.getInt(1);
            
        } catch (SQLException e) {
            System.err.println("Error getting book review count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Add a book to the database from Google Books API data
     * @param book Book object with Google Books data
     * @return Book ID if successful, null otherwise
     */
    public Integer addBook(Book book) {
        String sql = "INSERT INTO books (google_books_id, title, subtitle, description, " +
                    "isbn_10, isbn_13, published_date, publisher, page_count, " +
                    "language_code, cover_image_url, preview_link, info_link) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            stmt = conn.prepareStatement(sql, new String[]{"book_id"});
            
            stmt.setString(1, book.getGoogleBooksId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getSubtitle());
            
            // Handle CLOB for description
            if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                Clob descriptionClob = conn.createClob();
                descriptionClob.setString(1, book.getDescription());
                stmt.setClob(4, descriptionClob);
            } else {
                stmt.setNull(4, Types.CLOB);
            }
            
            stmt.setString(5, book.getIsbn10());
            stmt.setString(6, book.getIsbn13());
            
            // Handle published_date - try to parse it or set null
            if (book.getPublishedDate() != null && !book.getPublishedDate().isEmpty()) {
                try {
                    // Try to parse the date - Google Books often returns just year
                    java.sql.Date sqlDate;
                    if (book.getPublishedDate().length() == 4) {
                        // Just year, convert to January 1st of that year
                        sqlDate = java.sql.Date.valueOf(book.getPublishedDate() + "-01-01");
                    } else {
                        sqlDate = java.sql.Date.valueOf(book.getPublishedDate());
                    }
                    stmt.setDate(7, sqlDate);
                } catch (Exception e) {
                    stmt.setNull(7, Types.DATE);
                }
            } else {
                stmt.setNull(7, Types.DATE);
            }
            
            stmt.setString(8, book.getPublisher());
            stmt.setInt(9, book.getPageCount());
            stmt.setString(10, book.getLanguage());
            stmt.setString(11, book.getThumbnailUrl()); // Store thumbnail as cover_image_url
            stmt.setString(12, book.getPreviewLink());
            stmt.setString(13, book.getInfoLink());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated book_id
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer bookId = rs.getInt(1);
                    
                    // Now add authors if they exist
                    if (book.getAuthors() != null && book.getAuthors().length > 0) {
                        addAuthorsForBook(conn, bookId, book.getAuthors());
                    }
                    
                    conn.commit(); // Commit transaction
                    return bookId;
                }
            }
            
            conn.rollback();
            return null;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error adding book: " + e.getMessage());
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Helper method to add authors for a book
     * @param conn Database connection (within transaction)
     * @param bookId Book ID
     * @param authors Array of author names
     */
    private void addAuthorsForBook(Connection conn, Integer bookId, String[] authors) throws SQLException {
        String authorSql = "INSERT INTO authors (author_name) VALUES (?) ";
        String checkAuthorSql = "SELECT author_id FROM authors WHERE UPPER(author_name) = UPPER(?)";
        String linkSql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        
        for (String authorName : authors) {
            if (authorName != null && !authorName.trim().isEmpty()) {
                Integer authorId = null;
                
                // Check if author already exists
                try (PreparedStatement checkStmt = conn.prepareStatement(checkAuthorSql)) {
                    checkStmt.setString(1, authorName.trim());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            authorId = rs.getInt("author_id");
                        }
                    }
                }
                
                // If author doesn't exist, create it
                if (authorId == null) {
                    try (PreparedStatement authorStmt = conn.prepareStatement(authorSql, new String[]{"author_id"})) {
                        authorStmt.setString(1, authorName.trim());
                        authorStmt.executeUpdate();
                        
                        try (ResultSet rs = authorStmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                authorId = rs.getInt(1);
                            }
                        }
                    }
                }
                
                // Link book and author
                if (authorId != null) {
                    try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                        linkStmt.setInt(1, bookId);
                        linkStmt.setInt(2, authorId);
                        linkStmt.executeUpdate();
                    } catch (SQLException e) {
                        // Ignore if link already exists (duplicate key)
                        if (!e.getMessage().contains("unique constraint")) {
                            throw e;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add a review for a book
     * @param userId User ID
     * @param bookId Book ID  
     * @param reviewText Review text
     * @param isPublic Whether review is public
     * @return true if successful
     */
    public boolean reviewBook(Integer userId, Integer bookId, String reviewText, boolean isPublic) {
        String sql = "{ call sp_review_book(?, ?, ?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setString(3, "User Review"); // Default title
            
            // Handle CLOB for review text
            Clob reviewClob = conn.createClob();
            reviewClob.setString(1, reviewText);
            stmt.setClob(4, reviewClob);
            
            stmt.setInt(5, 0); // is_spoiler (default false)
            stmt.setInt(6, isPublic ? 1 : 0); // is_public
            
            stmt.registerOutParameter(7, Types.INTEGER);   // p_result
            stmt.registerOutParameter(8, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(7);
            String message = stmt.getString(8);
            
            if (result == 1) {
                return true;
            } else {
                System.err.println("Failed to add review: " + message);
                this.lastMessage = message;
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            this.lastMessage = "Database error while adding review";
            return false;
        }
    }
    
    /**
     * Rate and review a book in one operation
     * @param userId User ID
     * @param bookId Book ID
     * @param rating Rating (1-5)
     * @param reviewText Review text (optional)
     * @param isPublic Whether review is public
     * @return true if successful
     */
    public boolean rateBook(Integer userId, Integer bookId, int rating, String reviewText, boolean isPublic) {
        try {
            // First submit the rating
            boolean ratingSuccess = rateBook(userId, bookId, (double) rating);
            
            if (!ratingSuccess) {
                return false;
            }
            
            // If review text is provided, submit the review
            if (reviewText != null && !reviewText.trim().isEmpty()) {
                boolean reviewSuccess = reviewBook(userId, bookId, reviewText, isPublic);
                if (!reviewSuccess) {
                    System.err.println("Rating submitted but review failed");
                    // We don't return false here because rating was successful
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error rating and reviewing book: " + e.getMessage());
            return false;
        }
    }
}
