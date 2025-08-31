import com.booktrack.service.BookService;
import com.booktrack.model.Book;
import java.util.List;

/**
 * Test BookService integration
 */
public class TestBookService {
    public static void main(String[] args) {
        System.out.println("Testing BookService...");
        
        try {
            BookService bookService = new BookService();
            System.out.println("BookService created successfully");
            
            // Test landing page books
            System.out.println("Testing getLandingPageBooks...");
            List<Book> books = bookService.getLandingPageBooks();
            System.out.println("Retrieved " + books.size() + " books");
            
            for (Book book : books) {
                System.out.println("Title: " + book.getTitle());
                if (book.getAuthors() != null && book.getAuthors().length > 0) {
                    System.out.println("Author: " + book.getAuthors()[0]);
                }
                System.out.println("Rating: " + book.getAverageRating());
                System.out.println("---");
            }
            
            System.out.println("✅ BookService test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error testing BookService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
