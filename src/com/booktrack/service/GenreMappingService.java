package com.booktrack.service;

import com.booktrack.config.DatabaseConfig;
import java.sql.*;
import java.util.*;

/**
 * Service for mapping Google Books categories to database genres
 */
public class GenreMappingService {
    private DatabaseConfig dbConfig;
    private Map<String, String> categoryToGenreMap;
    
    public GenreMappingService() {
        this.dbConfig = DatabaseConfig.getInstance();
        initializeCategoryMapping();
    }
    
    /**
     * Initialize the mapping from Google Books categories to database genres
     */
    private void initializeCategoryMapping() {
        categoryToGenreMap = new HashMap<>();
        
        // Fiction mappings
        categoryToGenreMap.put("Fiction", "Fiction");
        categoryToGenreMap.put("Literary Fiction", "Fiction");
        categoryToGenreMap.put("General Fiction", "Fiction");
        
        // Science Fiction & Fantasy
        categoryToGenreMap.put("Science Fiction", "Science Fiction");
        categoryToGenreMap.put("Fantasy", "Fantasy");
        categoryToGenreMap.put("Dystopian", "Science Fiction");
        categoryToGenreMap.put("Speculative Fiction", "Science Fiction");
        
        // Mystery & Thriller
        categoryToGenreMap.put("Mystery", "Mystery");
        categoryToGenreMap.put("Detective", "Mystery");
        categoryToGenreMap.put("Crime", "Mystery");
        categoryToGenreMap.put("Thriller", "Thriller");
        categoryToGenreMap.put("Suspense", "Thriller");
        
        // Romance
        categoryToGenreMap.put("Romance", "Romance");
        categoryToGenreMap.put("Love Stories", "Romance");
        
        // Horror
        categoryToGenreMap.put("Horror", "Horror");
        categoryToGenreMap.put("Ghost Stories", "Horror");
        
        // Young Adult & Children
        categoryToGenreMap.put("Young Adult Fiction", "Young Adult");
        categoryToGenreMap.put("Teen Fiction", "Young Adult");
        categoryToGenreMap.put("Juvenile Fiction", "Children");
        categoryToGenreMap.put("Children's Books", "Children");
        categoryToGenreMap.put("Picture Books", "Children");
        
        // Non-fiction categories
        categoryToGenreMap.put("Biography & Autobiography", "Biography");
        categoryToGenreMap.put("Biography", "Biography");
        categoryToGenreMap.put("Autobiography", "Biography");
        categoryToGenreMap.put("Memoir", "Biography");
        
        categoryToGenreMap.put("History", "History");
        categoryToGenreMap.put("Historical", "History");
        
        categoryToGenreMap.put("Self-Help", "Self-Help");
        categoryToGenreMap.put("Personal Growth", "Self-Help");
        categoryToGenreMap.put("Motivational", "Self-Help");
        
        categoryToGenreMap.put("Business & Economics", "Business");
        categoryToGenreMap.put("Business", "Business");
        categoryToGenreMap.put("Economics", "Economics");
        categoryToGenreMap.put("Finance", "Economics");
        
        categoryToGenreMap.put("Health & Fitness", "Health");
        categoryToGenreMap.put("Health", "Health");
        categoryToGenreMap.put("Medical", "Health");
        categoryToGenreMap.put("Fitness", "Health");
        
        categoryToGenreMap.put("Cooking", "Cooking");
        categoryToGenreMap.put("Food & Wine", "Cooking");
        categoryToGenreMap.put("Recipes", "Cooking");
        
        categoryToGenreMap.put("Travel", "Travel");
        categoryToGenreMap.put("Travel Guides", "Travel");
        
        categoryToGenreMap.put("Poetry", "Poetry");
        categoryToGenreMap.put("Poems", "Poetry");
        
        categoryToGenreMap.put("Drama", "Drama");
        categoryToGenreMap.put("Plays", "Drama");
        categoryToGenreMap.put("Theater", "Drama");
        
        categoryToGenreMap.put("Philosophy", "Philosophy");
        categoryToGenreMap.put("Religion", "Religion");
        categoryToGenreMap.put("Spirituality", "Religion");
        
        categoryToGenreMap.put("Science", "Science");
        categoryToGenreMap.put("Nature", "Science");
        categoryToGenreMap.put("Popular Science", "Science");
        
        categoryToGenreMap.put("Technology", "Technology");
        categoryToGenreMap.put("Computers", "Technology");
        categoryToGenreMap.put("Internet", "Technology");
        
        categoryToGenreMap.put("Art", "Art");
        categoryToGenreMap.put("Design", "Art");
        categoryToGenreMap.put("Photography", "Art");
        
        categoryToGenreMap.put("Music", "Music");
        categoryToGenreMap.put("Musicians", "Music");
        
        categoryToGenreMap.put("Sports & Recreation", "Sports");
        categoryToGenreMap.put("Sports", "Sports");
        categoryToGenreMap.put("Athletics", "Sports");
        
        categoryToGenreMap.put("Political Science", "Politics");
        categoryToGenreMap.put("Politics", "Politics");
        categoryToGenreMap.put("Government", "Politics");
        
        categoryToGenreMap.put("Psychology", "Psychology");
        categoryToGenreMap.put("Social Psychology", "Psychology");
        
        categoryToGenreMap.put("Education", "Education");
        categoryToGenreMap.put("Teaching", "Education");
        categoryToGenreMap.put("Study Aids", "Education");
        
        categoryToGenreMap.put("Reference", "Reference");
        categoryToGenreMap.put("Dictionaries", "Reference");
        categoryToGenreMap.put("Encyclopedias", "Reference");
    }
    
    /**
     * Map Google Books categories to database genre IDs
     * @param categories Array of Google Books categories
     * @return Set of genre IDs from database
     */
    public Set<Integer> mapCategoriesToGenreIds(String[] categories) {
        Set<Integer> genreIds = new HashSet<>();
        
        if (categories == null || categories.length == 0) {
            // Default to Fiction if no categories
            genreIds.add(getGenreId("Fiction"));
            return genreIds;
        }
        
        for (String category : categories) {
            String mappedGenre = categoryToGenreMap.get(category);
            if (mappedGenre != null) {
                Integer genreId = getGenreId(mappedGenre);
                if (genreId != null) {
                    genreIds.add(genreId);
                }
            }
        }
        
        // If no mappings found, try partial matching
        if (genreIds.isEmpty()) {
            for (String category : categories) {
                Integer genreId = findBestGenreMatch(category);
                if (genreId != null) {
                    genreIds.add(genreId);
                    break; // Just add one best match
                }
            }
        }
        
        // Still no match? Default to Fiction
        if (genreIds.isEmpty()) {
            genreIds.add(getGenreId("Fiction"));
        }
        
        return genreIds;
    }
    
    /**
     * Get genre ID by name from database
     */
    private Integer getGenreId(String genreName) {
        String sql = "SELECT genre_id FROM genres WHERE UPPER(genre_name) = UPPER(?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, genreName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("genre_id");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting genre ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find best genre match using partial string matching
     */
    private Integer findBestGenreMatch(String category) {
        String sql = "SELECT genre_id FROM genres WHERE UPPER(genre_name) LIKE UPPER(?) OR UPPER(?) LIKE UPPER(genre_name)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + category + "%");
            stmt.setString(2, "%" + category + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("genre_id");
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding genre match: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Save book-genre relationships to database
     */
    public void saveBookGenres(int bookId, Set<Integer> genreIds) {
        // First, remove existing genres for this book
        String deleteSql = "DELETE FROM book_genres WHERE book_id = ?";
        
        try (Connection conn = dbConfig.getConnection()) {
            
            // Handle auto-commit properly
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit) {
                conn.setAutoCommit(false);
            }
            
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, bookId);
                deleteStmt.executeUpdate();
                
                // Insert new genre relationships
                String insertSql = "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Integer genreId : genreIds) {
                        insertStmt.setInt(1, bookId);
                        insertStmt.setInt(2, genreId);
                        insertStmt.executeUpdate();
                    }
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                if (autoCommit) {
                    conn.setAutoCommit(true);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving book genres: " + e.getMessage());
        }
    }
    
    /**
     * Get all available genres from database
     */
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT genre_name FROM genres ORDER BY genre_name";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                genres.add(rs.getString("genre_name"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all genres: " + e.getMessage());
        }
        
        return genres;
    }
}
