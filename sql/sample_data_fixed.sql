-- =====================================================
-- CORRECTED SAMPLE DATA - Fixed Foreign Key Issues
-- =====================================================

SET DEFINE OFF;

-- Clear any existing data first (optional - only if you want to start fresh)
DELETE FROM custom_list_books;
DELETE FROM book_tags;
DELETE FROM book_genres;
DELETE FROM book_authors;
DELETE FROM book_reviews;
DELETE FROM book_ratings;
DELETE FROM user_book_interactions;
DELETE FROM custom_lists;
DELETE FROM books;
DELETE FROM tags;
DELETE FROM genres;
DELETE FROM authors;
DELETE FROM users;
COMMIT;

-- Insert sample users (triggers will auto-create default lists)
INSERT INTO users (username, email, password_hash, first_name, last_name, bio) VALUES
('john_doe', 'john@booktrack.com', 'hashed_password_123', 'John', 'Doe', 'Love reading sci-fi and fantasy books');

INSERT INTO users (username, email, password_hash, first_name, last_name, bio) VALUES
('jane_smith', 'jane@booktrack.com', 'hashed_password_456', 'Jane', 'Smith', 'Passionate about mystery novels and thrillers');

INSERT INTO users (username, email, password_hash, first_name, last_name, bio) VALUES
('book_lover', 'booklover@booktrack.com', 'hashed_password_789', 'Alex', 'Johnson', 'Reading is my life');

-- Insert sample authors
INSERT INTO authors (author_name, author_bio, nationality) VALUES
('J.K. Rowling', 'British author best known for the Harry Potter series', 'British');

INSERT INTO authors (author_name, author_bio, nationality) VALUES
('George Orwell', 'English novelist and essayist, known for 1984 and Animal Farm', 'British');

INSERT INTO authors (author_name, author_bio, nationality) VALUES
('Agatha Christie', 'English writer known for detective novels', 'British');

-- Insert sample genres
INSERT INTO genres (genre_name, genre_description) VALUES
('Fantasy', 'Fantasy fiction with magical elements');

INSERT INTO genres (genre_name, genre_description) VALUES
('Science Fiction', 'Fiction dealing with advanced science and technology');

INSERT INTO genres (genre_name, genre_description) VALUES
('Mystery', 'Fiction dealing with puzzles and detective work');

INSERT INTO genres (genre_name, genre_description) VALUES
('Dystopian', 'Fiction depicting an imaginary society that is as dehumanizing as possible');

-- Insert sample tags
INSERT INTO tags (tag_name, tag_color) VALUES
('Must Read', '#e74c3c');

INSERT INTO tags (tag_name, tag_color) VALUES
('Classic', '#f39c12');

INSERT INTO tags (tag_name, tag_color) VALUES
('Page Turner', '#27ae60');

-- Insert sample books
INSERT INTO books (google_books_id, title, subtitle, description, isbn_13, published_date, publisher, page_count, language_code) VALUES
('kJjrDAAAQBAJ', 'Harry Potter and the Philosophers Stone', 'Book 1', 'The first book in the Harry Potter series', '9781408855652', DATE '1997-06-26', 'Bloomsbury', 223, 'en');

INSERT INTO books (title, description, isbn_13, published_date, publisher, page_count, language_code) VALUES
('Nineteen Eighty Four', 'A dystopian social science fiction novel', '9780451524935', DATE '1949-06-08', 'Secker and Warburg', 328, 'en');

INSERT INTO books (title, description, isbn_13, published_date, publisher, page_count, language_code) VALUES
('And Then There Were None', 'A mystery novel by Agatha Christie', '9780062073488', DATE '1939-11-06', 'Collins Crime Club', 264, 'en');

-- Commit to ensure all primary data is saved
COMMIT;

-- Now let's get the actual IDs and link them properly
-- Link books to authors using actual IDs from the database
INSERT INTO book_authors (book_id, author_id, author_role) 
SELECT b.book_id, a.author_id, 'Author' 
FROM books b, authors a 
WHERE b.title = 'Harry Potter and the Philosophers Stone' 
AND a.author_name = 'J.K. Rowling';

INSERT INTO book_authors (book_id, author_id, author_role) 
SELECT b.book_id, a.author_id, 'Author' 
FROM books b, authors a 
WHERE b.title = 'Nineteen Eighty Four' 
AND a.author_name = 'George Orwell';

INSERT INTO book_authors (book_id, author_id, author_role) 
SELECT b.book_id, a.author_id, 'Author' 
FROM books b, authors a 
WHERE b.title = 'And Then There Were None' 
AND a.author_name = 'Agatha Christie';

-- Link books to genres using actual IDs
INSERT INTO book_genres (book_id, genre_id) 
SELECT b.book_id, g.genre_id 
FROM books b, genres g 
WHERE b.title = 'Harry Potter and the Philosophers Stone' 
AND g.genre_name = 'Fantasy';

INSERT INTO book_genres (book_id, genre_id) 
SELECT b.book_id, g.genre_id 
FROM books b, genres g 
WHERE b.title = 'Nineteen Eighty Four' 
AND g.genre_name = 'Science Fiction';

INSERT INTO book_genres (book_id, genre_id) 
SELECT b.book_id, g.genre_id 
FROM books b, genres g 
WHERE b.title = 'Nineteen Eighty Four' 
AND g.genre_name = 'Dystopian';

INSERT INTO book_genres (book_id, genre_id) 
SELECT b.book_id, g.genre_id 
FROM books b, genres g 
WHERE b.title = 'And Then There Were None' 
AND g.genre_name = 'Mystery';

-- Link books to tags using actual IDs
INSERT INTO book_tags (book_id, tag_id) 
SELECT b.book_id, t.tag_id 
FROM books b, tags t 
WHERE b.title = 'Harry Potter and the Philosophers Stone' 
AND t.tag_name = 'Must Read';

INSERT INTO book_tags (book_id, tag_id) 
SELECT b.book_id, t.tag_id 
FROM books b, tags t 
WHERE b.title = 'Nineteen Eighty Four' 
AND t.tag_name = 'Classic';

INSERT INTO book_tags (book_id, tag_id) 
SELECT b.book_id, t.tag_id 
FROM books b, tags t 
WHERE b.title = 'And Then There Were None' 
AND t.tag_name = 'Classic';

INSERT INTO book_tags (book_id, tag_id) 
SELECT b.book_id, t.tag_id 
FROM books b, tags t 
WHERE b.title = 'And Then There Were None' 
AND t.tag_name = 'Page Turner';

-- Add books to user lists using actual list IDs
-- Get John's "Want to Read" list and add Harry Potter
INSERT INTO custom_list_books (list_id, book_id, notes) 
SELECT cl.list_id, b.book_id, 'Cannot wait to read this classic' 
FROM custom_lists cl, books b, users u 
WHERE u.username = 'john_doe' 
AND cl.user_id = u.user_id 
AND cl.list_type = 'WANT_TO_READ' 
AND b.title = 'Harry Potter and the Philosophers Stone';

-- Get Jane's "Currently Reading" list and add 1984
INSERT INTO custom_list_books (list_id, book_id, notes) 
SELECT cl.list_id, b.book_id, 'Currently reading this dystopian masterpiece' 
FROM custom_lists cl, books b, users u 
WHERE u.username = 'jane_smith' 
AND cl.user_id = u.user_id 
AND cl.list_type = 'CURRENTLY_READING' 
AND b.title = 'Nineteen Eighty Four';

-- Get Alex's "Have Read" list and add Agatha Christie book
INSERT INTO custom_list_books (list_id, book_id, notes) 
SELECT cl.list_id, b.book_id, 'Finished this amazing mystery' 
FROM custom_lists cl, books b, users u 
WHERE u.username = 'book_lover' 
AND cl.user_id = u.user_id 
AND cl.list_type = 'HAVE_READ' 
AND b.title = 'And Then There Were None';

-- Add some ratings using actual user and book IDs
INSERT INTO book_ratings (user_id, book_id, rating) 
SELECT u.user_id, b.book_id, 4.5 
FROM users u, books b 
WHERE u.username = 'john_doe' 
AND b.title = 'Harry Potter and the Philosophers Stone';

INSERT INTO book_ratings (user_id, book_id, rating) 
SELECT u.user_id, b.book_id, 5.0 
FROM users u, books b 
WHERE u.username = 'jane_smith' 
AND b.title = 'Nineteen Eighty Four';

INSERT INTO book_ratings (user_id, book_id, rating) 
SELECT u.user_id, b.book_id, 4.8 
FROM users u, books b 
WHERE u.username = 'book_lover' 
AND b.title = 'And Then There Were None';

-- Add some reviews using actual user and book IDs
INSERT INTO book_reviews (user_id, book_id, review_title, review_text, is_public) 
SELECT u.user_id, b.book_id, 'Magical Beginning', 'A wonderful start to an incredible series. The world-building is fantastic', 1 
FROM users u, books b 
WHERE u.username = 'john_doe' 
AND b.title = 'Harry Potter and the Philosophers Stone';

INSERT INTO book_reviews (user_id, book_id, review_title, review_text, is_public) 
SELECT u.user_id, b.book_id, 'Dystopian Masterpiece', 'A terrifying and prophetic vision. A must-read for everyone', 1 
FROM users u, books b 
WHERE u.username = 'jane_smith' 
AND b.title = 'Nineteen Eighty Four';

INSERT INTO book_reviews (user_id, book_id, review_title, review_text, is_public) 
SELECT u.user_id, b.book_id, 'Christie at Her Best', 'The plot twists kept me guessing until the very end. Brilliant mystery', 1 
FROM users u, books b 
WHERE u.username = 'book_lover' 
AND b.title = 'And Then There Were None';

COMMIT;

SET DEFINE ON;

-- Show success message and summary
SELECT 'Sample data inserted successfully!' as STATUS FROM DUAL;

SELECT 'SUMMARY:' as info, 
       (SELECT COUNT(*) FROM users) as users_count,
       (SELECT COUNT(*) FROM authors) as authors_count,
       (SELECT COUNT(*) FROM books) as books_count,
       (SELECT COUNT(*) FROM custom_lists) as lists_count,
       (SELECT COUNT(*) FROM book_ratings) as ratings_count,
       (SELECT COUNT(*) FROM book_reviews) as reviews_count
FROM DUAL;
