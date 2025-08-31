-- =====================================================
-- GENRE INITIALIZATION SCRIPT
-- =====================================================

-- Clear existing data safely
DELETE FROM book_genres;
DELETE FROM genres;
COMMIT;

-- Insert common book genres
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Fiction', 'Literary fiction and general novels');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Science Fiction', 'Speculative fiction dealing with futuristic concepts');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Fantasy', 'Fiction involving magical or supernatural elements');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Mystery', 'Fiction dealing with puzzles, crimes, and detective work');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Thriller', 'Fast-paced fiction designed to create suspense');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Romance', 'Fiction focusing on love and relationships');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Horror', 'Fiction intended to frighten, unsettle, or create suspense');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Biography', 'Non-fiction accounts of real people''s lives');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'History', 'Non-fiction about past events and people');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Self-Help', 'Books designed to help readers improve their lives');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Business', 'Books about business, economics, and entrepreneurship');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Health', 'Books about health, fitness, and wellness');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Cooking', 'Cookbooks and culinary guides');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Travel', 'Travel guides and travel literature');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Young Adult', 'Fiction targeted at teenage readers');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Children', 'Books for children and young readers');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Poetry', 'Collections of poems and verse');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Drama', 'Plays and theatrical works');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Philosophy', 'Philosophical works and treatises');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Religion', 'Religious and spiritual texts');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Science', 'Scientific books and popular science');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Technology', 'Books about technology and computing');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Art', 'Books about art, design, and creativity');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Music', 'Books about music and musicians');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Sports', 'Books about sports and athletics');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Politics', 'Books about politics and government');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Economics', 'Books about economics and finance');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Psychology', 'Books about psychology and human behavior');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Education', 'Educational books and textbooks');
INSERT INTO genres (genre_id, genre_name, genre_description) VALUES (seq_genre_id.NEXTVAL, 'Reference', 'Reference books, dictionaries, and encyclopedias');

COMMIT;

-- Display loaded genres
SELECT genre_id, genre_name FROM genres ORDER BY genre_name;
