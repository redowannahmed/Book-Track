import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import com.booktrack.config.DatabaseConfig;
import com.booktrack.ui.LoginForm;

/**
 * Main application class for BookTrack
 * Entry point for the application
 */
public class App {
    
    public static void main(String[] args) {
        // Test database connection first
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        if (!dbConfig.testConnection()) {
            JOptionPane.showMessageDialog(null, 
                "Unable to connect to database. Please check your database configuration.", 
                "Database Connection Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Launch the login form
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
