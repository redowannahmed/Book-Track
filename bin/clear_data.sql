-- =====================================================
-- CLEAR ALL DATA FROM TABLES
-- =====================================================

-- Disable foreign key constraints temporarily for easier deletion
-- (Oracle doesn't have this feature, so we'll delete in proper order)

-- Delete data in reverse dependency order to avoid FK constraint violations
DELETE FROM user_book_interactions;
DELETE FROM custom_list_books;
DELETE FROM book_tags;
DELETE FROM book_genres;
DELETE FROM book_authors;
DELETE FROM book_reviews;
DELETE FROM book_ratings;
DELETE FROM custom_lists;
DELETE FROM books;
DELETE FROM tags;
DELETE FROM genres;
DELETE FROM authors;
DELETE FROM users;

-- Reset sequences to start from 1
DROP SEQUENCE seq_user_id;
DROP SEQUENCE seq_book_id;
DROP SEQUENCE seq_author_id;
DROP SEQUENCE seq_genre_id;
DROP SEQUENCE seq_tag_id;
DROP SEQUENCE seq_custom_list_id;
DROP SEQUENCE seq_review_id;
DROP SEQUENCE seq_rating_id;
DROP SEQUENCE seq_interaction_id;

-- Recreate sequences
CREATE SEQUENCE seq_user_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_book_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_author_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_genre_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tag_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_custom_list_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_review_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_rating_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_interaction_id START WITH 1 INCREMENT BY 1;

COMMIT;

-- Verify tables are empty
SELECT 'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'authors', COUNT(*) FROM authors
UNION ALL
SELECT 'genres', COUNT(*) FROM genres
UNION ALL
SELECT 'tags', COUNT(*) FROM tags
UNION ALL
SELECT 'books', COUNT(*) FROM books
UNION ALL
SELECT 'custom_lists', COUNT(*) FROM custom_lists
UNION ALL
SELECT 'book_ratings', COUNT(*) FROM book_ratings
UNION ALL
SELECT 'book_reviews', COUNT(*) FROM book_reviews
UNION ALL
SELECT 'user_book_interactions', COUNT(*) FROM user_book_interactions;
