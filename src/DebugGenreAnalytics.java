import com.booktrack.config.DatabaseConfig;
import java.sql.*;

/**
 * Debug analytics genre query
 */
public class DebugGenreAnalytics {
    public static void main(String[] args) {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        
        try (Connection conn = dbConfig.getConnection()) {
            
            // First, let's see what user ID we're working with
            System.out.println("=== CHECKING USER DATA ===");
            String userSql = "SELECT user_id, username FROM users ORDER BY user_id";
            try (PreparedStatement stmt = conn.prepareStatement(userSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("User ID: " + rs.getInt("user_id") + ", Username: " + rs.getString("username"));
                }
            }
            
            // Check what books have been rated
            System.out.println("\n=== BOOKS RATED BY USERS ===");
            String ratedSql = """
                SELECT br.user_id, br.book_id, b.title, br.rating
                FROM book_ratings br
                JOIN books b ON br.book_id = b.book_id
                ORDER BY br.user_id, br.book_id
            """;
            try (PreparedStatement stmt = conn.prepareStatement(ratedSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("User " + rs.getInt("user_id") + " rated '" + 
                                     rs.getString("title") + "' (Book ID: " + rs.getInt("book_id") + 
                                     ") with " + rs.getDouble("rating") + " stars");
                }
            }
            
            // Check which of those rated books have genre assignments
            System.out.println("\n=== GENRE ASSIGNMENTS FOR RATED BOOKS ===");
            String genreAnalyticsSql = """
                SELECT 
                    br.user_id,
                    b.title,
                    g.genre_name,
                    br.rating
                FROM book_ratings br
                JOIN books b ON br.book_id = b.book_id
                JOIN book_genres bg ON b.book_id = bg.book_id
                JOIN genres g ON bg.genre_id = g.genre_id
                ORDER BY br.user_id, g.genre_name
            """;
            try (PreparedStatement stmt = conn.prepareStatement(genreAnalyticsSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    System.out.println("User " + rs.getInt("user_id") + ": '" + 
                                     rs.getString("title") + "' -> " + rs.getString("genre_name") + 
                                     " (Rating: " + rs.getDouble("rating") + ")");
                }
                if (!hasData) {
                    System.out.println("NO GENRE DATA FOR RATED BOOKS! This is the problem.");
                }
            }
            
            // Check the actual analytics query that's failing
            System.out.println("\n=== TESTING ACTUAL ANALYTICS QUERY ===");
            String analyticsQuerySql = """
                SELECT 
                    g.genre_name as genre,
                    COUNT(*) as book_count,
                    ROUND(AVG(br.rating), 2) as avg_rating,
                    COUNT(rev.review_id) as reviews_written
                FROM book_ratings br
                JOIN books b ON br.book_id = b.book_id
                JOIN book_genres bg ON b.book_id = bg.book_id
                JOIN genres g ON bg.genre_id = g.genre_id
                LEFT JOIN book_reviews rev ON br.user_id = rev.user_id AND br.book_id = rev.book_id
                WHERE br.user_id = ?
                GROUP BY g.genre_name
                ORDER BY book_count DESC
            """;
            
            // Try with user ID 1 (assuming that's the test user)
            try (PreparedStatement stmt = conn.prepareStatement(analyticsQuerySql)) {
                stmt.setInt(1, 1);
                ResultSet rs = stmt.executeQuery();
                
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    System.out.println("Genre: " + rs.getString("genre") + 
                                     ", Books: " + rs.getInt("book_count") +
                                     ", Avg Rating: " + rs.getDouble("avg_rating") +
                                     ", Reviews: " + rs.getInt("reviews_written"));
                }
                if (!hasResults) {
                    System.out.println("Analytics query returned no results for user ID 1");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
