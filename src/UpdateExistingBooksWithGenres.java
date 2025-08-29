import com.booktrack.config.DatabaseConfig;
import com.booktrack.service.GenreMappingService;
import com.booktrack.model.Book;
import java.sql.*;
import java.util.Set;

/**
 * Update existing books with genre assignments
 */
public class UpdateExistingBooksWithGenres {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        GenreMappingService genreMappingService = new GenreMappingService();
        
        System.out.println("Updating existing books with genre assignments...\n");
        
        try (Connection conn = dbConfig.getConnection()) {
            
            // Get all books that don't have genre assignments
            String getBooksWithoutGenresSql = """
                SELECT b.book_id, b.title, b.google_books_id
                FROM books b
                WHERE b.book_id NOT IN (
                    SELECT DISTINCT bg.book_id 
                    FROM book_genres bg
                )
                ORDER BY b.book_id
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(getBooksWithoutGenresSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                int booksUpdated = 0;
                while (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    String title = rs.getString("title");
                    String googleBooksId = rs.getString("google_books_id");
                    
                    System.out.println("Processing Book ID " + bookId + ": " + title);
                    
                    // Assign genres based on title analysis
                    String[] inferredCategories = inferCategoriesFromTitle(title);
                    Set<Integer> genreIds = genreMappingService.mapCategoriesToGenreIds(inferredCategories);
                    
                    if (!genreIds.isEmpty()) {
                        genreMappingService.saveBookGenres(bookId, genreIds);
                        System.out.println("  -> Assigned genres: " + genreIds);
                        booksUpdated++;
                    } else {
                        System.out.println("  -> No genres assigned (using default)");
                    }
                }
                
                System.out.println("\nTotal books updated: " + booksUpdated);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test the analytics query again
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Testing analytics after genre assignment...");
        DebugGenreAnalytics.main(new String[]{});
    }
    
    /**
     * Infer categories from book title (simple heuristic approach)
     */
    private static String[] inferCategoriesFromTitle(String title) {
        if (title == null) return new String[]{"Fiction"};
        
        String titleLower = title.toLowerCase();
        
        // Mystery/Crime keywords
        if (titleLower.contains("mystery") || titleLower.contains("shadow") || 
            titleLower.contains("detective") || titleLower.contains("crime")) {
            return new String[]{"Mystery"};
        }
        
        // Romance keywords
        if (titleLower.contains("romance") || titleLower.contains("love") || 
            titleLower.contains("anglicising romance")) {
            return new String[]{"Romance"};
        }
        
        // Business/Guide keywords
        if (titleLower.contains("bestseller") || titleLower.contains("guide") || 
            titleLower.contains("making of")) {
            return new String[]{"Business"};
        }
        
        // Classic literature (specific titles)
        if (titleLower.contains("brothers karamazov") || titleLower.contains("dostoevsky")) {
            return new String[]{"Fiction", "Literature"};
        }
        
        // Default to Fiction
        return new String[]{"Fiction"};
    }
}
