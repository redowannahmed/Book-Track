import com.booktrack.service.GoogleBooksService;
import com.booktrack.dao.BookDAO;
import com.booktrack.model.Book;
import java.util.List;

/**
 * Test to add a book and check if genre assignment works
 */
public class TestAddBookWithGenres {
    public static void main(String[] args) {
        System.out.println("Testing Book Addition with Automatic Genre Assignment...\n");
        
        GoogleBooksService googleService = new GoogleBooksService();
        BookDAO bookDAO = new BookDAO();
        
        try {
            // Search for a book with clear categories
            System.out.println("Searching for 'The Lord of the Rings'...");
            List<Book> books = googleService.searchBooks("The Lord of the Rings", 1);
            
            if (!books.isEmpty()) {
                Book book = books.get(0);
                System.out.println("Found book: " + book.getTitle());
                System.out.println("Categories: " + book.getCategoriesAsString());
                System.out.println("Authors: " + (book.getAuthors() != null ? String.join(", ", book.getAuthors()) : "Unknown"));
                
                // Add book to database
                System.out.println("\nAdding book to database...");
                Integer bookId = bookDAO.addOrUpdateBook(book);
                
                if (bookId != null) {
                    System.out.println("Book added successfully with ID: " + bookId);
                    
                    // Now check if genres were assigned
                    System.out.println("\nChecking if genres were assigned...");
                    CheckDatabaseState.main(new String[]{});
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
    }
}
