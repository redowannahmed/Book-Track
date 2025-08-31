-- =====================================================
-- CUSTOM LIST MANAGEMENT PROCEDURES for BookTrack
-- =====================================================

-- Procedure to create a new custom list
CREATE OR REPLACE PROCEDURE sp_create_custom_list(
    p_user_id IN NUMBER,
    p_list_name IN VARCHAR2,
    p_list_description IN CLOB,
    p_is_public IN NUMBER DEFAULT 1,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2,
    p_list_id OUT NUMBER
) AS
    v_count NUMBER;
    v_new_list_id NUMBER;
BEGIN
    -- Check if list name already exists for this user
    SELECT COUNT(*) INTO v_count
    FROM custom_lists
    WHERE user_id = p_user_id AND UPPER(list_name) = UPPER(p_list_name);
    
    IF v_count > 0 THEN
        p_result := 0;
        p_message := 'A list with this name already exists';
        p_list_id := NULL;
        RETURN;
    END IF;
    
    -- Validate list name
    IF p_list_name IS NULL OR LENGTH(TRIM(p_list_name)) = 0 THEN
        p_result := 0;
        p_message := 'List name cannot be empty';
        p_list_id := NULL;
        RETURN;
    END IF;
    
    IF LENGTH(p_list_name) > 100 THEN
        p_result := 0;
        p_message := 'List name cannot exceed 100 characters';
        p_list_id := NULL;
        RETURN;
    END IF;
    
    -- Create the new custom list
    INSERT INTO custom_lists (
        user_id, list_name, list_description, list_type, is_public, is_default, books_count
    ) VALUES (
        p_user_id, p_list_name, p_list_description, 'CUSTOM', p_is_public, 0, 0
    ) RETURNING list_id INTO v_new_list_id;
    
    p_result := 1;
    p_message := 'Custom list created successfully';
    p_list_id := v_new_list_id;
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to create custom list: ' || SQLERRM;
        p_list_id := NULL;
END sp_create_custom_list;
/

-- Procedure to update a custom list
CREATE OR REPLACE PROCEDURE sp_update_custom_list(
    p_user_id IN NUMBER,
    p_list_id IN NUMBER,
    p_list_name IN VARCHAR2,
    p_list_description IN CLOB,
    p_is_public IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
    v_is_default NUMBER;
BEGIN
    -- Check if list exists and belongs to user
    SELECT COUNT(*), MAX(is_default) INTO v_count, v_is_default
    FROM custom_lists
    WHERE list_id = p_list_id AND user_id = p_user_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'List not found or access denied';
        RETURN;
    END IF;
    
    -- Don't allow editing default lists
    IF v_is_default = 1 THEN
        p_result := 0;
        p_message := 'Cannot edit default system lists';
        RETURN;
    END IF;
    
    -- Check if new name conflicts with existing lists (except current one)
    SELECT COUNT(*) INTO v_count
    FROM custom_lists
    WHERE user_id = p_user_id 
      AND UPPER(list_name) = UPPER(p_list_name)
      AND list_id != p_list_id;
    
    IF v_count > 0 THEN
        p_result := 0;
        p_message := 'A list with this name already exists';
        RETURN;
    END IF;
    
    -- Validate list name
    IF p_list_name IS NULL OR LENGTH(TRIM(p_list_name)) = 0 THEN
        p_result := 0;
        p_message := 'List name cannot be empty';
        RETURN;
    END IF;
    
    IF LENGTH(p_list_name) > 100 THEN
        p_result := 0;
        p_message := 'List name cannot exceed 100 characters';
        RETURN;
    END IF;
    
    -- Update the list
    UPDATE custom_lists
    SET list_name = p_list_name,
        list_description = p_list_description,
        is_public = p_is_public,
        updated_at = CURRENT_TIMESTAMP
    WHERE list_id = p_list_id AND user_id = p_user_id;
    
    p_result := 1;
    p_message := 'Custom list updated successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to update custom list: ' || SQLERRM;
END sp_update_custom_list;
/

-- Procedure to delete a custom list
CREATE OR REPLACE PROCEDURE sp_delete_custom_list(
    p_user_id IN NUMBER,
    p_list_id IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
    v_is_default NUMBER;
    v_books_count NUMBER;
BEGIN
    -- Check if list exists and belongs to user
    SELECT COUNT(*), MAX(is_default), MAX(books_count) 
    INTO v_count, v_is_default, v_books_count
    FROM custom_lists
    WHERE list_id = p_list_id AND user_id = p_user_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'List not found or access denied';
        RETURN;
    END IF;
    
    -- Don't allow deleting default lists
    IF v_is_default = 1 THEN
        p_result := 0;
        p_message := 'Cannot delete default system lists';
        RETURN;
    END IF;
    
    -- Delete the list (cascade will handle books in list)
    DELETE FROM custom_lists
    WHERE list_id = p_list_id AND user_id = p_user_id;
    
    p_result := 1;
    p_message := 'Custom list deleted successfully';
    IF v_books_count > 0 THEN
        p_message := p_message || ' (' || v_books_count || ' books were removed from the list)';
    END IF;
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to delete custom list: ' || SQLERRM;
END sp_delete_custom_list;
/

-- Procedure to add book to a specific custom list (by list_id)
CREATE OR REPLACE PROCEDURE sp_add_book_to_custom_list(
    p_user_id IN NUMBER,
    p_list_id IN NUMBER,
    p_book_id IN NUMBER,
    p_notes IN CLOB,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
    v_list_exists NUMBER;
BEGIN
    -- Check if list exists and belongs to user
    SELECT COUNT(*) INTO v_list_exists
    FROM custom_lists
    WHERE list_id = p_list_id AND user_id = p_user_id;
    
    IF v_list_exists = 0 THEN
        p_result := 0;
        p_message := 'List not found or access denied';
        RETURN;
    END IF;
    
    -- Check if book exists
    SELECT COUNT(*) INTO v_count
    FROM books
    WHERE book_id = p_book_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'Book not found';
        RETURN;
    END IF;
    
    -- Check if book is already in this list
    SELECT COUNT(*) INTO v_count
    FROM custom_list_books
    WHERE list_id = p_list_id AND book_id = p_book_id;
    
    IF v_count > 0 THEN
        p_result := 0;
        p_message := 'Book is already in this list';
        RETURN;
    END IF;
    
    -- Add book to list
    INSERT INTO custom_list_books (list_id, book_id, notes)
    VALUES (p_list_id, p_book_id, p_notes);
    
    p_result := 1;
    p_message := 'Book added to custom list successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to add book to custom list: ' || SQLERRM;
END sp_add_book_to_custom_list;
/

-- Procedure to remove book from a specific custom list (by list_id)
CREATE OR REPLACE PROCEDURE sp_remove_book_from_custom_list(
    p_user_id IN NUMBER,
    p_list_id IN NUMBER,
    p_book_id IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
    v_list_exists NUMBER;
BEGIN
    -- Check if list exists and belongs to user
    SELECT COUNT(*) INTO v_list_exists
    FROM custom_lists
    WHERE list_id = p_list_id AND user_id = p_user_id;
    
    IF v_list_exists = 0 THEN
        p_result := 0;
        p_message := 'List not found or access denied';
        RETURN;
    END IF;
    
    -- Check if book exists in this list
    SELECT COUNT(*) INTO v_count
    FROM custom_list_books
    WHERE list_id = p_list_id AND book_id = p_book_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'Book is not in this list';
        RETURN;
    END IF;
    
    -- Remove book from list
    DELETE FROM custom_list_books
    WHERE list_id = p_list_id AND book_id = p_book_id;
    
    p_result := 1;
    p_message := 'Book removed from custom list successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to remove book from custom list: ' || SQLERRM;
END sp_remove_book_from_custom_list;
/

-- Function to get all lists for a user (including both default and custom)
CREATE OR REPLACE FUNCTION fn_get_all_user_lists(p_user_id IN NUMBER)
RETURN SYS_REFCURSOR AS
    list_cursor SYS_REFCURSOR;
BEGIN
    OPEN list_cursor FOR
        SELECT list_id, list_name, list_description, list_type, 
               books_count, is_default, is_public, created_at, updated_at
        FROM custom_lists 
        WHERE user_id = p_user_id 
        ORDER BY is_default DESC, list_type, list_name;
    
    RETURN list_cursor;
END fn_get_all_user_lists;
/

-- Function to search books for adding to custom lists
CREATE OR REPLACE FUNCTION fn_search_books_for_list(p_search_term IN VARCHAR2, p_limit IN NUMBER DEFAULT 20)
RETURN SYS_REFCURSOR AS
    books_cursor SYS_REFCURSOR;
BEGIN
    OPEN books_cursor FOR
        SELECT book_id, google_books_id, title, subtitle, 
               CASE 
                   WHEN description IS NOT NULL THEN 
                       SUBSTR(description, 1, 200)  -- Shorter description for search results
                   ELSE 
                       NULL 
               END as description,
               publisher, published_date, cover_image_url,
               average_rating, ratings_count,
               -- Get authors as concatenated string
               (SELECT LISTAGG(a.author_name, ', ') WITHIN GROUP (ORDER BY a.author_name)
                FROM authors a
                JOIN book_authors ba ON a.author_id = ba.author_id
                WHERE ba.book_id = b.book_id) as authors
        FROM books b
        WHERE UPPER(b.title) LIKE UPPER('%' || p_search_term || '%')
           OR UPPER(b.subtitle) LIKE UPPER('%' || p_search_term || '%')
           OR UPPER(b.publisher) LIKE UPPER('%' || p_search_term || '%')
           OR EXISTS (
               SELECT 1 FROM authors a
               JOIN book_authors ba ON a.author_id = ba.author_id
               WHERE ba.book_id = b.book_id 
                 AND UPPER(a.author_name) LIKE UPPER('%' || p_search_term || '%')
           )
        ORDER BY 
            CASE WHEN UPPER(b.title) LIKE UPPER(p_search_term || '%') THEN 1 ELSE 2 END,
            b.average_rating DESC,
            b.ratings_count DESC
        FETCH FIRST p_limit ROWS ONLY;
    
    RETURN books_cursor;
END fn_search_books_for_list;
/

-- Function to check if a book exists in a specific custom list
CREATE OR REPLACE FUNCTION fn_is_book_in_custom_list(
    p_list_id IN NUMBER,
    p_book_id IN NUMBER
) RETURN NUMBER AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM custom_list_books
    WHERE list_id = p_list_id AND book_id = p_book_id;
    
    RETURN v_count;
    
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_is_book_in_custom_list;
/

SHOW ERRORS;

SELECT 'Custom list management procedures created successfully!' as STATUS FROM DUAL;

