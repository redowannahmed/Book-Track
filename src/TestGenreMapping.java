import com.booktrack.service.GoogleBooksService;
import com.booktrack.service.GenreMappingService;
import com.booktrack.dao.BookDAO;
import com.booktrack.model.Book;
import java.util.List;
import java.util.Set;

/**
 * Test class to verify the complete genre mapping system
 */
public class TestGenreMapping {
    public static void main(String[] args) {
        System.out.println("Testing Genre Mapping System...\n");
        
        // Test 1: Test GenreMappingService directly
        GenreMappingService genreService = new GenreMappingService();
        
        // Test category mapping
        String[] testCategories = {"Fiction", "Science Fiction", "Mystery"};
        Set<Integer> genreIds = genreService.mapCategoriesToGenreIds(testCategories);
        
        System.out.println("Test 1 - Category Mapping:");
        System.out.println("Categories: Fiction, Science Fiction, Mystery");
        System.out.println("Mapped Genre IDs: " + genreIds);
        System.out.println();
        
        // Test 2: Test Google Books API with category extraction
        GoogleBooksService googleService = new GoogleBooksService();
        System.out.println("Test 2 - Google Books API with Categories:");
        
        try {
            List<Book> books = googleService.searchBooks("Dune", 1);
            if (!books.isEmpty()) {
                Book book = books.get(0);
                System.out.println("Book: " + book.getTitle());
                System.out.println("Categories: " + book.getCategoriesAsString());
                
                // Test automatic genre mapping
                if (book.getCategories() != null) {
                    Set<Integer> mappedGenres = genreService.mapCategoriesToGenreIds(book.getCategories());
                    System.out.println("Mapped Genres: " + mappedGenres);
                }
            }
        } catch (Exception e) {
            System.out.println("Google Books API test failed: " + e.getMessage());
        }
        
        System.out.println("\nGenre Mapping System test completed!");
    }
}
