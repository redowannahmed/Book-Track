-- =====================================================
-- IMPROVED TRIGGERS for Business Logic and Data Integrity
-- =====================================================

-- Trigger to auto-populate primary keys using sequences
CREATE OR REPLACE TRIGGER trg_users_pk
    BEFORE INSERT ON users
    FOR EACH ROW
BEGIN
    IF :NEW.user_id IS NULL THEN
        :NEW.user_id := seq_user_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_books_pk
    BEFORE INSERT ON books
    FOR EACH ROW
BEGIN
    IF :NEW.book_id IS NULL THEN
        :NEW.book_id := seq_book_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_authors_pk
    BEFORE INSERT ON authors
    FOR EACH ROW
BEGIN
    IF :NEW.author_id IS NULL THEN
        :NEW.author_id := seq_author_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_genres_pk
    BEFORE INSERT ON genres
    FOR EACH ROW
BEGIN
    IF :NEW.genre_id IS NULL THEN
        :NEW.genre_id := seq_genre_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_tags_pk
    BEFORE INSERT ON tags
    FOR EACH ROW
BEGIN
    IF :NEW.tag_id IS NULL THEN
        :NEW.tag_id := seq_tag_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_custom_lists_pk
    BEFORE INSERT ON custom_lists
    FOR EACH ROW
BEGIN
    IF :NEW.list_id IS NULL THEN
        :NEW.list_id := seq_custom_list_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_book_reviews_pk
    BEFORE INSERT ON book_reviews
    FOR EACH ROW
BEGIN
    IF :NEW.review_id IS NULL THEN
        :NEW.review_id := seq_review_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_book_ratings_pk
    BEFORE INSERT ON book_ratings
    FOR EACH ROW
BEGIN
    IF :NEW.rating_id IS NULL THEN
        :NEW.rating_id := seq_rating_id.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_interactions_pk
    BEFORE INSERT ON user_book_interactions
    FOR EACH ROW
BEGIN
    IF :NEW.interaction_id IS NULL THEN
        :NEW.interaction_id := seq_interaction_id.NEXTVAL;
    END IF;
END;
/

-- Trigger to update timestamps on record updates
CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_custom_lists_updated_at
    BEFORE UPDATE ON custom_lists
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_book_ratings_updated_at
    BEFORE UPDATE ON book_ratings
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_book_reviews_updated_at
    BEFORE UPDATE ON book_reviews
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- IMPROVED: Trigger to automatically create default lists for new users with error handling
CREATE OR REPLACE TRIGGER trg_create_default_lists
    AFTER INSERT ON users
    FOR EACH ROW
DECLARE
    v_error_msg VARCHAR2(4000);
BEGIN
    BEGIN
        -- Create default "Want to Read" list
        INSERT INTO custom_lists (user_id, list_name, list_description, list_type, is_default, is_public)
        VALUES (:NEW.user_id, 'Want to Read', 'Books I want to read in the future', 'WANT_TO_READ', 1, 1);
        
        -- Create default "Currently Reading" list
        INSERT INTO custom_lists (user_id, list_name, list_description, list_type, is_default, is_public)
        VALUES (:NEW.user_id, 'Currently Reading', 'Books I am currently reading', 'CURRENTLY_READING', 1, 1);
        
        -- Create default "Have Read" list
        INSERT INTO custom_lists (user_id, list_name, list_description, list_type, is_default, is_public)
        VALUES (:NEW.user_id, 'Have Read', 'Books I have finished reading', 'HAVE_READ', 1, 1);
        
        -- Create default "Favorites" list
        INSERT INTO custom_lists (user_id, list_name, list_description, list_type, is_default, is_public)
        VALUES (:NEW.user_id, 'Favorites', 'My favorite books', 'FAVORITES', 1, 1);
        
    EXCEPTION
        WHEN OTHERS THEN
            v_error_msg := 'Error creating default lists for user ' || :NEW.user_id || ': ' || SQLERRM;
            -- Log error instead of failing the user creation
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'users', 'create_default_lists');
    END;
END;
/

-- IMPROVED: Compound trigger to update book's average rating (avoids mutating table errors)
CREATE OR REPLACE TRIGGER trg_update_book_rating_compound
    FOR INSERT OR UPDATE OR DELETE ON book_ratings
    COMPOUND TRIGGER
    
    -- Collection to store book IDs that need rating updates
    TYPE book_ids_t IS TABLE OF NUMBER;
    g_book_ids book_ids_t := book_ids_t();
    
    AFTER EACH ROW IS
    BEGIN
        -- Collect book IDs that need updates
        IF INSERTING OR UPDATING THEN
            g_book_ids.EXTEND;
            g_book_ids(g_book_ids.COUNT) := :NEW.book_id;
        ELSIF DELETING THEN
            g_book_ids.EXTEND;
            g_book_ids(g_book_ids.COUNT) := :OLD.book_id;
        END IF;
    END AFTER EACH ROW;
    
    AFTER STATEMENT IS
    BEGIN
        -- Update all affected books in one go
        FOR i IN 1..g_book_ids.COUNT LOOP
            UPDATE books 
            SET average_rating = (
                SELECT NVL(ROUND(AVG(rating), 2), 0) 
                FROM book_ratings 
                WHERE book_id = g_book_ids(i)
            ),
            ratings_count = (
                SELECT COUNT(*) 
                FROM book_ratings 
                WHERE book_id = g_book_ids(i)
            ),
            updated_at = CURRENT_TIMESTAMP
            WHERE book_id = g_book_ids(i);
        END LOOP;
        
        -- Clear the collection for next execution
        g_book_ids.DELETE;
    END AFTER STATEMENT;
END trg_update_book_rating_compound;
/

-- IMPROVED: Compound trigger to update book's total reviews count and user's review count
CREATE OR REPLACE TRIGGER trg_update_book_reviews_count_compound
    FOR INSERT OR UPDATE OR DELETE ON book_reviews
    COMPOUND TRIGGER
    
    TYPE book_user_rec IS RECORD (
        book_id NUMBER,
        user_id NUMBER
    );
    TYPE book_user_t IS TABLE OF book_user_rec;
    g_affected_records book_user_t := book_user_t();
    
    AFTER EACH ROW IS
    BEGIN
        IF INSERTING THEN
            g_affected_records.EXTEND;
            g_affected_records(g_affected_records.COUNT).book_id := :NEW.book_id;
            g_affected_records(g_affected_records.COUNT).user_id := :NEW.user_id;
        ELSIF UPDATING THEN
            -- Handle both old and new if they're different
            g_affected_records.EXTEND;
            g_affected_records(g_affected_records.COUNT).book_id := :NEW.book_id;
            g_affected_records(g_affected_records.COUNT).user_id := :NEW.user_id;
            
            IF :OLD.book_id != :NEW.book_id OR :OLD.user_id != :NEW.user_id THEN
                g_affected_records.EXTEND;
                g_affected_records(g_affected_records.COUNT).book_id := :OLD.book_id;
                g_affected_records(g_affected_records.COUNT).user_id := :OLD.user_id;
            END IF;
        ELSIF DELETING THEN
            g_affected_records.EXTEND;
            g_affected_records(g_affected_records.COUNT).book_id := :OLD.book_id;
            g_affected_records(g_affected_records.COUNT).user_id := :OLD.user_id;
        END IF;
    END AFTER EACH ROW;
    
    AFTER STATEMENT IS
    BEGIN
        -- Update book review counts
        FOR i IN 1..g_affected_records.COUNT LOOP
            UPDATE books 
            SET total_reviews = (
                SELECT COUNT(*) 
                FROM book_reviews 
                WHERE book_id = g_affected_records(i).book_id AND is_public = 1
            )
            WHERE book_id = g_affected_records(i).book_id;
            
            -- Update user's total reviews count
            UPDATE users 
            SET total_reviews = (
                SELECT COUNT(*) 
                FROM book_reviews 
                WHERE user_id = g_affected_records(i).user_id AND is_public = 1
            )
            WHERE user_id = g_affected_records(i).user_id;
        END LOOP;
        
        -- Clear the collection
        g_affected_records.DELETE;
    END AFTER STATEMENT;
END trg_update_book_reviews_count_compound;
/

-- IMPROVED: Compound trigger to update custom list book count and user's total books read
CREATE OR REPLACE TRIGGER trg_update_list_books_count_compound
    FOR INSERT OR DELETE ON custom_list_books
    COMPOUND TRIGGER
    
    TYPE list_user_rec IS RECORD (
        list_id NUMBER,
        user_id NUMBER
    );
    TYPE list_user_t IS TABLE OF list_user_rec;
    g_affected_lists list_user_t := list_user_t();
    
    AFTER EACH ROW IS
        v_user_id NUMBER;
    BEGIN
        IF INSERTING THEN
            SELECT user_id INTO v_user_id FROM custom_lists WHERE list_id = :NEW.list_id;
            g_affected_lists.EXTEND;
            g_affected_lists(g_affected_lists.COUNT).list_id := :NEW.list_id;
            g_affected_lists(g_affected_lists.COUNT).user_id := v_user_id;
        ELSIF DELETING THEN
            SELECT user_id INTO v_user_id FROM custom_lists WHERE list_id = :OLD.list_id;
            g_affected_lists.EXTEND;
            g_affected_lists(g_affected_lists.COUNT).list_id := :OLD.list_id;
            g_affected_lists(g_affected_lists.COUNT).user_id := v_user_id;
        END IF;
    END AFTER EACH ROW;
    
    AFTER STATEMENT IS
    BEGIN
        FOR i IN 1..g_affected_lists.COUNT LOOP
            -- Update list book count
            UPDATE custom_lists 
            SET books_count = (
                SELECT COUNT(*) 
                FROM custom_list_books 
                WHERE list_id = g_affected_lists(i).list_id
            )
            WHERE list_id = g_affected_lists(i).list_id;
            
            -- Update user's total books read count
            UPDATE users 
            SET total_books_read = (
                SELECT COUNT(DISTINCT clb.book_id)
                FROM custom_list_books clb
                JOIN custom_lists cl ON clb.list_id = cl.list_id
                WHERE cl.user_id = g_affected_lists(i).user_id
                AND cl.list_type = 'HAVE_READ'
            )
            WHERE user_id = g_affected_lists(i).user_id;
        END LOOP;
        
        -- Clear the collection
        g_affected_lists.DELETE;
    END AFTER STATEMENT;
END trg_update_list_books_count_compound;
/

-- IMPROVED: Trigger to log user interactions with better error handling
CREATE OR REPLACE TRIGGER trg_log_rating_interaction
    AFTER INSERT ON book_ratings
    FOR EACH ROW
DECLARE
    v_error_msg VARCHAR2(4000);
BEGIN
    BEGIN
        INSERT INTO user_book_interactions (
            user_id, book_id, interaction_type, interaction_details, new_value
        ) VALUES (
            :NEW.user_id, :NEW.book_id, 'RATED', 
            'User rated book with ' || TO_CHAR(:NEW.rating) || ' stars', 
            TO_CHAR(:NEW.rating)
        );
    EXCEPTION
        WHEN OTHERS THEN
            v_error_msg := 'Error logging rating interaction: ' || SQLERRM;
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'book_ratings', 'log_interaction');
    END;
END;
/

CREATE OR REPLACE TRIGGER trg_log_review_interaction
    AFTER INSERT ON book_reviews
    FOR EACH ROW
DECLARE
    v_error_msg VARCHAR2(4000);
BEGIN
    BEGIN
        INSERT INTO user_book_interactions (
            user_id, book_id, interaction_type, interaction_details
        ) VALUES (
            :NEW.user_id, :NEW.book_id, 'REVIEWED', 
            'User wrote a review: ' || SUBSTR(NVL(:NEW.review_title, 'Untitled Review'), 1, 100)
        );
    EXCEPTION
        WHEN OTHERS THEN
            v_error_msg := 'Error logging review interaction: ' || SQLERRM;
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'book_reviews', 'log_interaction');
    END;
END;
/

CREATE OR REPLACE TRIGGER trg_log_list_add_interaction
    AFTER INSERT ON custom_list_books
    FOR EACH ROW
DECLARE
    v_user_id NUMBER;
    v_list_name VARCHAR2(100);
    v_list_type VARCHAR2(20);
    v_error_msg VARCHAR2(4000);
BEGIN
    BEGIN
        SELECT user_id, list_name, list_type 
        INTO v_user_id, v_list_name, v_list_type
        FROM custom_lists 
        WHERE list_id = :NEW.list_id;
        
        INSERT INTO user_book_interactions (
            user_id, book_id, interaction_type, interaction_details, list_id
        ) VALUES (
            v_user_id, :NEW.book_id, 
            CASE v_list_type
                WHEN 'WANT_TO_READ' THEN 'ADDED_TO_WANT'
                WHEN 'CURRENTLY_READING' THEN 'ADDED_TO_READING'
                WHEN 'HAVE_READ' THEN 'ADDED_TO_READ'
                WHEN 'FAVORITES' THEN 'ADDED_TO_FAVORITES'
                ELSE 'ADDED_TO_CUSTOM_LIST'
            END,
            'Added to list: ' || v_list_name, :NEW.list_id
        );
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_error_msg := 'List not found for list_id: ' || :NEW.list_id;
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'custom_list_books', 'log_interaction');
        WHEN OTHERS THEN
            v_error_msg := 'Error logging list add interaction: ' || SQLERRM;
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'custom_list_books', 'log_interaction');
    END;
END;
/

-- NEW: Trigger to log when books are removed from lists
CREATE OR REPLACE TRIGGER trg_log_list_remove_interaction
    AFTER DELETE ON custom_list_books
    FOR EACH ROW
DECLARE
    v_user_id NUMBER;
    v_list_name VARCHAR2(100);
    v_list_type VARCHAR2(20);
    v_error_msg VARCHAR2(4000);
BEGIN
    BEGIN
        SELECT user_id, list_name, list_type 
        INTO v_user_id, v_list_name, v_list_type
        FROM custom_lists 
        WHERE list_id = :OLD.list_id;
        
        INSERT INTO user_book_interactions (
            user_id, book_id, interaction_type, interaction_details, list_id
        ) VALUES (
            v_user_id, :OLD.book_id, 
            CASE v_list_type
                WHEN 'WANT_TO_READ' THEN 'REMOVED_FROM_WANT'
                WHEN 'CURRENTLY_READING' THEN 'REMOVED_FROM_READING'
                WHEN 'HAVE_READ' THEN 'REMOVED_FROM_READ'
                WHEN 'FAVORITES' THEN 'REMOVED_FROM_FAVORITES'
                ELSE 'REMOVED_FROM_CUSTOM_LIST'
            END,
            'Removed from list: ' || v_list_name, :OLD.list_id
        );
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_error_msg := 'List not found for list_id: ' || :OLD.list_id;
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'custom_list_books', 'log_remove_interaction');
        WHEN OTHERS THEN
            v_error_msg := 'Error logging list remove interaction: ' || SQLERRM;
            INSERT INTO error_log (error_message, error_date, table_name, operation) 
            VALUES (v_error_msg, SYSDATE, 'custom_list_books', 'log_remove_interaction');
    END;
END;
/

-- NEW: Trigger to update book popularity score based on interactions
CREATE OR REPLACE TRIGGER trg_update_book_popularity
    AFTER INSERT ON user_book_interactions
    FOR EACH ROW
DECLARE
    v_popularity_score NUMBER;
    v_weight NUMBER;
BEGIN
    -- Different weights for different interaction types
    v_weight := CASE :NEW.interaction_type
        WHEN 'ADDED_TO_READ' THEN 3
        WHEN 'RATED' THEN 2
        WHEN 'REVIEWED' THEN 2
        WHEN 'ADDED_TO_FAVORITES' THEN 4
        WHEN 'ADDED_TO_WANT' THEN 1
        WHEN 'ADDED_TO_READING' THEN 1
        WHEN 'VIEWED' THEN 0.1
        ELSE 0.5
    END;
    
    -- Calculate new popularity score (recent interactions get more weight)
    SELECT COALESCE(popularity_score, 0) + v_weight * 
           POWER(0.9, TRUNC(SYSDATE) - TRUNC(:NEW.interaction_date))
    INTO v_popularity_score
    FROM books 
    WHERE book_id = :NEW.book_id;
    
    UPDATE books 
    SET popularity_score = v_popularity_score
    WHERE book_id = :NEW.book_id;
END;
/

-- NOTE: Duplicate prevention is handled by UNIQUE constraints in the table DDL
-- No triggers needed for duplicate prevention as the database constraints will handle this
-- UNIQUE constraint on (user_id, book_id) in both book_ratings and book_reviews tables
