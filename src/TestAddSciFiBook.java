import com.booktrack.service.GoogleBooksService;
import com.booktrack.dao.BookDAO;
import com.booktrack.model.Book;
import java.util.List;

/**
 * Test to add a Science Fiction book 
 */
public class TestAddSciFiBook {
    public static void main(String[] args) {
        System.out.println("Testing Science Fiction Book Addition...\n");
        
        GoogleBooksService googleService = new GoogleBooksService();
        BookDAO bookDAO = new BookDAO();
        
        try {
            // Search for a Science Fiction book
            System.out.println("Searching for 'Foundation Isaac Asimov'...");
            List<Book> books = googleService.searchBooks("Foundation Isaac Asimov", 1);
            
            if (!books.isEmpty()) {
                Book book = books.get(0);
                System.out.println("Found book: " + book.getTitle());
                System.out.println("Categories: " + book.getCategoriesAsString());
                
                // Add book to database
                System.out.println("\nAdding book to database...");
                Integer bookId = bookDAO.addOrUpdateBook(book);
                
                if (bookId != null) {
                    System.out.println("Book added successfully with ID: " + bookId);
                } else {
                    System.out.println("Failed to add book!");
                }
            } else {
                System.out.println("No books found!");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Check current state
        System.out.println("\n" + "=".repeat(50));
        CheckDatabaseState.main(new String[]{});
    }
}
