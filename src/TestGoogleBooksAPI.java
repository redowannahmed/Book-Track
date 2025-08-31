import com.booktrack.service.GoogleBooksService;
import com.booktrack.model.Book;
import java.util.List;

/**
 * Simple test to verify Google Books API integration
 */
public class TestGoogleBooksAPI {
    public static void main(String[] args) {
        System.out.println("Testing Google Books API...");
        
        try {
            GoogleBooksService service = new GoogleBooksService();
            System.out.println("GoogleBooksService created successfully");
            
            // Test a simple search
            System.out.println("Testing simple search...");
            List<Book> books = service.searchBooks("bestseller", 3);
            System.out.println("Retrieved " + books.size() + " books");
            
            for (Book book : books) {
                System.out.println("Title: " + book.getTitle());
                if (book.getAuthors() != null && book.getAuthors().length > 0) {
                    System.out.println("Author: " + book.getAuthors()[0]);
                }
                System.out.println("---");
            }
            
            // Test landing page books
            System.out.println("\nTesting landing page books...");
            List<Book> landingBooks = service.getLandingPageBooks();
            System.out.println("Retrieved " + landingBooks.size() + " landing page books");
            
        } catch (Exception e) {
            System.err.println("Error testing Google Books API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
