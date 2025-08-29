import com.booktrack.config.DatabaseConfig;
import java.sql.*;

/**
 * Test to check current database state
 */
public class CheckDatabaseState {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        
        try (Connection conn = dbConfig.getConnection()) {
            
            // Check genres table
            System.out.println("=== GENRES TABLE ===");
            String genresSql = "SELECT genre_id, genre_name FROM genres ORDER BY genre_id";
            try (PreparedStatement stmt = conn.prepareStatement(genresSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                int count = 0;
                while (rs.next() && count < 10) {
                    System.out.println("ID: " + rs.getInt("genre_id") + ", Name: " + rs.getString("genre_name"));
                    count++;
                }
                if (count == 10) System.out.println("... (showing first 10)");
            }
            
            // Check books table
            System.out.println("\n=== BOOKS TABLE ===");
            String booksSql = "SELECT COUNT(*) as book_count FROM books";
            try (PreparedStatement stmt = conn.prepareStatement(booksSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total books in database: " + rs.getInt("book_count"));
                }
            }
            
            // Check book_genres relationships
            System.out.println("\n=== BOOK-GENRE RELATIONSHIPS ===");
            String relationSql = "SELECT COUNT(*) as relationship_count FROM book_genres";
            try (PreparedStatement stmt = conn.prepareStatement(relationSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total book-genre relationships: " + rs.getInt("relationship_count"));
                }
            }
            
            // Check reading list entries
            System.out.println("\n=== CUSTOM LIST ENTRIES ===");
            String listSql = "SELECT COUNT(*) as list_count FROM custom_list_books";
            try (PreparedStatement stmt = conn.prepareStatement(listSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total custom list entries: " + rs.getInt("list_count"));
                }
            }
            
            // Check specific book-genre data for analytics
            System.out.println("\n=== SAMPLE BOOK-GENRE DATA FOR ANALYTICS ===");
            String analyticsPreviewSql = """
                SELECT b.title, g.genre_name 
                FROM books b 
                JOIN book_genres bg ON b.book_id = bg.book_id 
                JOIN genres g ON bg.genre_id = g.genre_id 
                WHERE ROWNUM <= 5
            """;
            try (PreparedStatement stmt = conn.prepareStatement(analyticsPreviewSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    System.out.println("Book: " + rs.getString("title") + " -> Genre: " + rs.getString("genre_name"));
                }
                if (!hasData) {
                    System.out.println("No book-genre relationships found for analytics!");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
