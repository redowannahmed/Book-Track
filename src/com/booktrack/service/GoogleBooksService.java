package com.booktrack.service;

import com.booktrack.model.Book;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google Books API Service
 * Handles communication with Google Books API
 */
public class GoogleBooksService {
    private static final String API_KEY = "AIzaSyA21Asz8kvHtu-p6YOAQLTDBCyFsu3arZI";
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final int DEFAULT_MAX_RESULTS = 12;
    
    /**
     * Search for books by query
     * @param query Search query
     * @param maxResults Maximum number of results
     * @return List of books
     */
    public List<Book> searchBooks(String query, int maxResults) {
        List<Book> books = new ArrayList<>();
        
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String urlString = String.format("%s?q=%s&maxResults=%d&key=%s", 
                                            BASE_URL, encodedQuery, maxResults, API_KEY);
            
            String jsonResponse = makeHttpRequest(urlString);
            if (jsonResponse != null) {
                books = parseBooks(jsonResponse);
            }
            
        } catch (Exception e) {
            System.err.println("Error searching books: " + e.getMessage());
            e.printStackTrace();
        }
        
        return books;
    }
    
    /**
     * Get popular books (using a predefined search)
     * @return List of popular books
     */
    public List<Book> getPopularBooks() {
        // Use a mix of popular search terms to get diverse results - focus on English
        String[] popularQueries = {
            "bestseller fiction english",
            "popular novels 2024 english",
            "award winning books english language"
        };
        
        List<Book> allBooks = new ArrayList<>();
        for (String query : popularQueries) {
            List<Book> books = searchBooks(query, 4);
            allBooks.addAll(books);
        }
        
        // Return first 12 unique books
        return allBooks.size() > DEFAULT_MAX_RESULTS ? 
               allBooks.subList(0, DEFAULT_MAX_RESULTS) : allBooks;
    }
    
    /**
     * Get trending books (recent publications)
     * @return List of trending books
     */
    public List<Book> getTrendingBooks() {
        return searchBooks("subject:fiction+newer:2023+language:en", DEFAULT_MAX_RESULTS);
    }
    
    /**
     * Get classic books
     * @return List of classic books
     */
    public List<Book> getClassicBooks() {
        return searchBooks("subject:classics+language:en+author:\"Charles Dickens\"+OR+author:\"Jane Austen\"+OR+author:\"Mark Twain\"", DEFAULT_MAX_RESULTS);
    }
    
    /**
     * Get books by category
     * @param category Book category
     * @return List of books in the category
     */
    public List<Book> getBooksByCategory(String category) {
        return searchBooks("subject:" + category, DEFAULT_MAX_RESULTS);
    }
    
    /**
     * Get popular books for landing page - optimized for speed
     * @return List of books for display
     */
    public List<Book> getLandingPageBooks() {
        // Use a single API call with a broad query for popular/trending books
        // This will be much faster than multiple API calls
        List<Book> books = searchBooks("bestseller fiction 2024 popular", DEFAULT_MAX_RESULTS);
        
        // If the query doesn't return enough books, try a fallback
        if (books.size() < DEFAULT_MAX_RESULTS) {
            List<Book> fallbackBooks = searchBooks("popular books english", DEFAULT_MAX_RESULTS - books.size());
            books.addAll(fallbackBooks);
        }
        
        return books.size() > DEFAULT_MAX_RESULTS ? 
               books.subList(0, DEFAULT_MAX_RESULTS) : books;
    }
    
    /**
     * Make HTTP request to Google Books API
     * @param urlString URL to request
     * @return JSON response as string
     */
    private String makeHttpRequest(String urlString) {
        System.out.println("Making request to: " + urlString);
        try {
            URI uri = URI.create(urlString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
            
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                String result = response.toString();
                System.out.println("API Response length: " + result.length());
                return result;
            } else {
                System.err.println("HTTP Error: " + responseCode);
                // Try to read error response
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)
                );
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("Error response: " + errorResponse.toString());
            }
            
        } catch (Exception e) {
            System.err.println("Error making HTTP request: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Parse JSON response into Book objects (simplified regex-based parsing)
     * @param jsonResponse JSON response from API
     * @return List of Book objects
     */
    private List<Book> parseBooks(String jsonResponse) {
        List<Book> books = new ArrayList<>();
        
        try {
            System.out.println("Parsing JSON response...");
            
            // First, let's try a simpler approach - look for the "items" array
            int itemsStart = jsonResponse.indexOf("\"items\":");
            if (itemsStart == -1) {
                System.out.println("No 'items' array found in response");
                return getSampleBooks();
            }
            
            // Find the start of the items array
            int arrayStart = jsonResponse.indexOf("[", itemsStart);
            if (arrayStart == -1) {
                System.out.println("No array start found after 'items'");
                return getSampleBooks();
            }
            
            // Parse individual book objects within the items array
            // Look for objects that contain "kind": "books#volume"
            String itemsSection = jsonResponse.substring(arrayStart);
            
            // Simple approach: split by "kind": "books#volume" and process each segment
            String[] segments = itemsSection.split("\"kind\":\\s*\"books#volume\"");
            
            System.out.println("Found " + (segments.length - 1) + " potential book segments");
            
            for (int i = 1; i < segments.length && books.size() < DEFAULT_MAX_RESULTS; i++) {
                // Reconstruct a partial JSON object for this book
                String bookSegment = "{\"kind\": \"books#volume\"" + segments[i];
                
                // Find the end of this book object by looking for the next book or end of array
                int endPos = findBookObjectEnd(bookSegment);
                if (endPos > 0) {
                    bookSegment = bookSegment.substring(0, endPos);
                }
                
                Book book = parseBookSimple(bookSegment);
                if (book != null) {
                    books.add(book);
                    System.out.println("Parsed book: " + book.getTitle());
                } else {
                    System.out.println("Failed to parse book from segment " + i);
                }
            }
            
            System.out.println("Successfully parsed " + books.size() + " books");
            
        } catch (Exception e) {
            System.err.println("Error parsing books JSON: " + e.getMessage());
            e.printStackTrace();
            // Return some sample books if API fails
            System.out.println("Falling back to sample books due to parsing error");
            return getSampleBooks();
        }
        
        if (books.isEmpty()) {
            System.out.println("No books parsed, falling back to sample books");
            return getSampleBooks();
        }
        
        return books;
    }
    
    /**
     * Find the end of a book object in JSON
     * @param bookSegment JSON segment starting with a book object
     * @return End position of the book object
     */
    private int findBookObjectEnd(String bookSegment) {
        int braceCount = 0;
        boolean inString = false;
        boolean escaped = false;
        
        for (int i = 0; i < bookSegment.length(); i++) {
            char c = bookSegment.charAt(i);
            
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\') {
                escaped = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        return i + 1;
                    }
                }
            }
        }
        
        return bookSegment.length();
    }
    
    /**
     * Parse a single book from JSON using simple regex
     * @param bookJson JSON string representing a book
     * @return Book object or null if parsing fails
     */
    private Book parseBookSimple(String bookJson) {
        try {
            Book book = new Book();
            
            // Extract ID
            String id = extractJsonValue(bookJson, "id");
            if (id != null) book.setGoogleBooksId(id);
            
            // Extract title
            String title = extractJsonValue(bookJson, "title");
            if (title != null && !title.trim().isEmpty()) {
                // Filter out non-English titles
                if (!isEnglishText(title)) {
                    System.out.println("Skipping non-English book: " + title);
                    return null; // Skip books with non-English titles
                }
                book.setTitle(title);
            } else {
                return null; // Skip books without title
            }
            
            // Extract authors (simplified - just take first author)
            String authorsSection = extractJsonSection(bookJson, "authors");
            if (authorsSection != null) {
                String author = extractFirstArrayValue(authorsSection);
                if (author != null && isEnglishText(author)) {
                    book.setAuthors(new String[]{author});
                }
            }
            
            // Extract description
            String description = extractJsonValue(bookJson, "description");
            if (description != null) book.setDescription(description);
            
            // Extract publisher
            String publisher = extractJsonValue(bookJson, "publisher");
            if (publisher != null) book.setPublisher(publisher);
            
            // Extract thumbnail
            String thumbnail = extractJsonValue(bookJson, "thumbnail");
            if (thumbnail != null) {
                book.setThumbnailUrl(thumbnail.replace("http://", "https://"));
            }
            
            // Extract rating
            String ratingStr = extractJsonValue(bookJson, "averageRating");
            if (ratingStr != null) {
                try {
                    book.setAverageRating(Double.parseDouble(ratingStr));
                } catch (NumberFormatException e) {
                    // Ignore invalid rating
                }
            }
            
            // Extract categories
            String categoriesSection = extractJsonSection(bookJson, "categories");
            if (categoriesSection != null) {
                String[] categories = extractArrayValues(categoriesSection);
                if (categories != null && categories.length > 0) {
                    book.setCategories(categories);
                }
            }
            
            return book;
            
        } catch (Exception e) {
            System.err.println("Error parsing individual book: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extract a JSON value using regex - simplified to avoid catastrophic backtracking
     */
    private String extractJsonValue(String json, String key) {
        // Use a simpler pattern that avoids catastrophic backtracking
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(json);
        if (matcher.find()) {
            String value = matcher.group(1);
            // Decode Unicode escape sequences
            return decodeUnicodeEscapes(value);
        }
        return null;
    }
    
    /**
     * Decode Unicode escape sequences in JSON strings
     * @param input String with potential Unicode escapes
     * @return Decoded string
     */
    private String decodeUnicodeEscapes(String input) {
        if (input == null) return null;
        
        StringBuilder result = new StringBuilder();
        int length = input.length();
        
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            
            if (c == '\\' && i + 1 < length) {
                char next = input.charAt(i + 1);
                
                switch (next) {
                    case 'u':
                        // Unicode escape sequence \\uXXXX
                        if (i + 5 < length) {
                            try {
                                String hex = input.substring(i + 2, i + 6);
                                int codePoint = Integer.parseInt(hex, 16);
                                result.append((char) codePoint);
                                i += 5; // Skip the entire escape sequence
                            } catch (NumberFormatException e) {
                                // Invalid Unicode escape, keep as is
                                result.append(c);
                            }
                        } else {
                            result.append(c);
                        }
                        break;
                    case 'n':
                        result.append('\n');
                        i++;
                        break;
                    case 't':
                        result.append('\t');
                        i++;
                        break;
                    case 'r':
                        result.append('\r');
                        i++;
                        break;
                    case '\\':
                        result.append('\\');
                        i++;
                        break;
                    case '"':
                        result.append('"');
                        i++;
                        break;
                    default:
                        result.append(c);
                        break;
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Extract a JSON section (for arrays/objects)
     */
    private String extractJsonSection(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\[[^\\]]*\\])");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extract first value from a JSON array - simplified pattern
     */
    private String extractFirstArrayValue(String arrayJson) {
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(arrayJson);
        if (matcher.find()) {
            return decodeUnicodeEscapes(matcher.group(1));
        }
        return null;
    }
    
    /**
     * Extract all values from a JSON array - simplified pattern
     */
    private String[] extractArrayValues(String arrayJson) {
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(arrayJson);
        java.util.List<String> values = new java.util.ArrayList<>();
        
        while (matcher.find()) {
            values.add(decodeUnicodeEscapes(matcher.group(1)));
        }
        
        return values.isEmpty() ? null : values.toArray(new String[0]);
    }
    
    /**
     * Get sample books for when API fails or for testing
     * @return List of sample books
     */
    private List<Book> getSampleBooks() {
        List<Book> books = new ArrayList<>();
        
        System.out.println("Creating sample books...");
        
        // Create some sample books with real book covers
        Book book1 = new Book("sample1", "The Great Gatsby", new String[]{"F. Scott Fitzgerald"}, 
                              "https://covers.openlibrary.org/b/isbn/9780743273565-M.jpg");
        book1.setDescription("A classic American novel about the Jazz Age");
        book1.setPublisher("Scribner");
        book1.setAverageRating(4.0);
        books.add(book1);
        
        Book book2 = new Book("sample2", "To Kill a Mockingbird", new String[]{"Harper Lee"}, 
                              "https://covers.openlibrary.org/b/isbn/9780060935467-M.jpg");
        book2.setDescription("A gripping tale of racial injustice and childhood innocence");
        book2.setPublisher("J.B. Lippincott & Co.");
        book2.setAverageRating(4.3);
        books.add(book2);
        
        Book book3 = new Book("sample3", "1984", new String[]{"George Orwell"}, 
                              "https://covers.openlibrary.org/b/isbn/9780451524935-M.jpg");
        book3.setDescription("A dystopian social science fiction novel");
        book3.setPublisher("Secker & Warburg");
        book3.setAverageRating(4.1);
        books.add(book3);
        
        // Add more sample books with different cover sources
        String[] sampleTitles = {
            "Pride and Prejudice", "The Catcher in the Rye", "Lord of the Flies",
            "The Lord of the Rings", "Harry Potter", "The Hobbit",
            "Brave New World", "Jane Eyre", "Wuthering Heights"
        };
        
        String[] sampleAuthors = {
            "Jane Austen", "J.D. Salinger", "William Golding",
            "J.R.R. Tolkien", "J.K. Rowling", "J.R.R. Tolkien", 
            "Aldous Huxley", "Charlotte Brontë", "Emily Brontë"
        };
        
        String[] sampleCovers = {
            "https://covers.openlibrary.org/b/isbn/9780141439518-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780316769174-M.jpg", 
            "https://covers.openlibrary.org/b/isbn/9780571056866-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780547928227-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780439708180-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780547928227-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780060850524-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780141441146-M.jpg",
            "https://covers.openlibrary.org/b/isbn/9780141439556-M.jpg"
        };
        
        // Add more sample books...
        for (int i = 0; i < Math.min(sampleTitles.length, 9); i++) {
            Book book = new Book("sample" + (i + 4), sampleTitles[i], new String[]{sampleAuthors[i]}, 
                                sampleCovers[i]);
            book.setDescription("This is a classic literary work");
            book.setPublisher("Classic Publishers");
            book.setAverageRating(3.5 + (i % 3) * 0.5);
            books.add(book);
        }
        
        System.out.println("Created " + books.size() + " sample books");
        return books;
    }
    
    /**
     * Check if text is primarily English (basic Latin characters)
     * @param text Text to check
     * @return true if text appears to be English, false otherwise
     */
    private boolean isEnglishText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // Count characters that are basic Latin (English)
        int totalChars = 0;
        int englishChars = 0;
        
        for (char c : text.toCharArray()) {
            // Skip spaces, punctuation, and numbers
            if (Character.isLetter(c)) {
                totalChars++;
                // Basic Latin range (English letters)
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    englishChars++;
                }
            }
        }
        
        // If no letters at all, consider it English (might be numbers/symbols only)
        if (totalChars == 0) {
            return true;
        }
        
        // Require at least 80% of letters to be English
        double englishRatio = (double) englishChars / totalChars;
        return englishRatio >= 0.8;
    }
}
