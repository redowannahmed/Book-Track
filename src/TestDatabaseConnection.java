import com.booktrack.config.DatabaseConfig;

/**
 * Test database connection
 */
public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            boolean connected = dbConfig.testConnection();
            
            if (connected) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Database connection failed!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error testing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
