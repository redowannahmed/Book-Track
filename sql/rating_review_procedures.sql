-- =====================================================
-- RATING AND REVIEW PROCEDURES for BookTrack
-- =====================================================

-- Procedure to add or update a book rating
CREATE OR REPLACE PROCEDURE sp_rate_book(
    p_user_id IN NUMBER,
    p_book_id IN NUMBER,
    p_rating IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    -- Validate rating value (must be between 1 and 5)
    IF p_rating < 1 OR p_rating > 5 THEN
        p_result := 0;
        p_message := 'Rating must be between 1 and 5';
        RETURN;
    END IF;
    
    -- Check if user has already rated this book
    SELECT COUNT(*) INTO v_count
    FROM book_ratings
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    IF v_count > 0 THEN
        -- Update existing rating
        UPDATE book_ratings
        SET rating = p_rating, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = p_user_id AND book_id = p_book_id;
        
        p_result := 1;
        p_message := 'Rating updated successfully';
    ELSE
        -- Insert new rating
        INSERT INTO book_ratings (user_id, book_id, rating)
        VALUES (p_user_id, p_book_id, p_rating);
        
        p_result := 1;
        p_message := 'Rating added successfully';
    END IF;
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to rate book: ' || SQLERRM;
END sp_rate_book;
/

-- Procedure to add or update a book review
CREATE OR REPLACE PROCEDURE sp_review_book(
    p_user_id IN NUMBER,
    p_book_id IN NUMBER,
    p_review_title IN VARCHAR2,
    p_review_text IN CLOB,
    p_is_spoiler IN NUMBER DEFAULT 0,
    p_is_public IN NUMBER DEFAULT 1,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    -- Validate inputs
    IF p_review_text IS NULL OR LENGTH(TRIM(p_review_text)) = 0 THEN
        p_result := 0;
        p_message := 'Review text cannot be empty';
        RETURN;
    END IF;
    
    -- Check if user has already reviewed this book
    SELECT COUNT(*) INTO v_count
    FROM book_reviews
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    IF v_count > 0 THEN
        -- Update existing review
        UPDATE book_reviews
        SET review_title = p_review_title,
            review_text = p_review_text,
            is_spoiler = p_is_spoiler,
            is_public = p_is_public,
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = p_user_id AND book_id = p_book_id;
        
        p_result := 1;
        p_message := 'Review updated successfully';
    ELSE
        -- Insert new review
        INSERT INTO book_reviews (user_id, book_id, review_title, review_text, is_spoiler, is_public)
        VALUES (p_user_id, p_book_id, p_review_title, p_review_text, p_is_spoiler, p_is_public);
        
        p_result := 1;
        p_message := 'Review added successfully';
    END IF;
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to add/update review: ' || SQLERRM;
END sp_review_book;
/

-- Function to get user's rating for a specific book
CREATE OR REPLACE FUNCTION fn_get_user_rating(
    p_user_id IN NUMBER,
    p_book_id IN NUMBER
) RETURN NUMBER AS
    v_rating NUMBER;
BEGIN
    SELECT rating INTO v_rating
    FROM book_ratings
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    RETURN v_rating;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
    WHEN OTHERS THEN
        RETURN NULL;
END fn_get_user_rating;
/

-- Function to get user's review for a specific book
CREATE OR REPLACE FUNCTION fn_get_user_review(
    p_user_id IN NUMBER,
    p_book_id IN NUMBER
) RETURN SYS_REFCURSOR AS
    review_cursor SYS_REFCURSOR;
BEGIN
    OPEN review_cursor FOR
        SELECT review_id, review_title, 
               CASE 
                   WHEN review_text IS NOT NULL THEN 
                       SUBSTR(review_text, 1, 4000)  -- Convert CLOB to VARCHAR2
                   ELSE 
                       NULL 
               END as review_text,
               is_spoiler, is_public, created_at, updated_at
        FROM book_reviews
        WHERE user_id = p_user_id AND book_id = p_book_id;
    
    RETURN review_cursor;
END fn_get_user_review;
/

-- Function to get all reviews for a book (public reviews only)
CREATE OR REPLACE FUNCTION fn_get_book_reviews(
    p_book_id IN NUMBER,
    p_limit IN NUMBER DEFAULT 10
) RETURN SYS_REFCURSOR AS
    reviews_cursor SYS_REFCURSOR;
BEGIN
    OPEN reviews_cursor FOR
        SELECT br.review_id, br.review_title, 
               CASE 
                   WHEN br.review_text IS NOT NULL THEN 
                       SUBSTR(br.review_text, 1, 4000)  -- Convert CLOB to VARCHAR2
                   ELSE 
                       NULL 
               END as review_text,
               br.is_spoiler, br.created_at, br.updated_at, br.likes_count,
               u.username, u.first_name, u.last_name,
               -- Get user's rating for this book if they rated it
               (SELECT rating FROM book_ratings br2 WHERE br2.user_id = br.user_id AND br2.book_id = br.book_id) as user_rating
        FROM book_reviews br
        JOIN users u ON br.user_id = u.user_id
        WHERE br.book_id = p_book_id 
          AND br.is_public = 1
        ORDER BY br.created_at DESC
        FETCH FIRST p_limit ROWS ONLY;
    
    RETURN reviews_cursor;
END fn_get_book_reviews;
/

-- Function to get aggregate rating statistics for a book
CREATE OR REPLACE FUNCTION fn_get_book_rating_stats(p_book_id IN NUMBER)
RETURN SYS_REFCURSOR AS
    stats_cursor SYS_REFCURSOR;
BEGIN
    OPEN stats_cursor FOR
        SELECT 
            NVL(ROUND(AVG(rating), 2), 0) as average_rating,
            COUNT(*) as total_ratings,
            COUNT(CASE WHEN rating = 5 THEN 1 END) as five_stars,
            COUNT(CASE WHEN rating = 4 THEN 1 END) as four_stars,
            COUNT(CASE WHEN rating = 3 THEN 1 END) as three_stars,
            COUNT(CASE WHEN rating = 2 THEN 1 END) as two_stars,
            COUNT(CASE WHEN rating = 1 THEN 1 END) as one_star
        FROM book_ratings
        WHERE book_id = p_book_id;
    
    RETURN stats_cursor;
END fn_get_book_rating_stats;
/

-- Function to get total review count for a book
CREATE OR REPLACE FUNCTION fn_get_book_review_count(p_book_id IN NUMBER)
RETURN NUMBER AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM book_reviews
    WHERE book_id = p_book_id AND is_public = 1;
    
    RETURN v_count;
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_get_book_review_count;
/

-- Procedure to delete a book rating
CREATE OR REPLACE PROCEDURE sp_delete_rating(
    p_user_id IN NUMBER,
    p_book_id IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    -- Check if rating exists
    SELECT COUNT(*) INTO v_count
    FROM book_ratings
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'No rating found to delete';
        RETURN;
    END IF;
    
    -- Delete rating
    DELETE FROM book_ratings
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    p_result := 1;
    p_message := 'Rating deleted successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to delete rating: ' || SQLERRM;
END sp_delete_rating;
/

-- Pro 
CREATE OR REPLACE PROCEDURE sp_delete_review(
    p_user_id IN NUMBER,
    p_book_id IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    -- Check if review exists
    SELECT COUNT(*) INTO v_count
    FROM book_reviews
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'No review found to delete';
        RETURN;
    END IF;
    
    -- Delete review
    DELETE FROM book_reviews
    WHERE user_id = p_user_id AND book_id = p_book_id;
    
    p_result := 1;
    p_message := 'Review deleted successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to delete review: ' || SQLERRM;
END sp_delete_review;
/

SHOW ERRORS;

SELECT 'Rating and Review procedures created successfully!' as STATUS FROM DUAL;
