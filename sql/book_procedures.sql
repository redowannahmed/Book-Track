-- =====================================================
-- BOOK MANAGEMENT PROCEDURES for BookTrack
-- =====================================================

-- Procedure to add/update book from Google Books API
CREATE OR REPLACE PROCEDURE sp_add_or_update_book(
    p_google_books_id IN VARCHAR2,
    p_title IN VARCHAR2,
    p_subtitle IN VARCHAR2,
    p_description IN CLOB,
    p_isbn_10 IN VARCHAR2,
    p_isbn_13 IN VARCHAR2,
    p_published_date IN VARCHAR2, -- String date from API
    p_publisher IN VARCHAR2,
    p_page_count IN NUMBER,
    p_language_code IN VARCHAR2,
    p_cover_image_url IN VARCHAR2,
    p_preview_link IN VARCHAR2,
    p_info_link IN VARCHAR2,
    p_average_rating IN NUMBER,
    p_ratings_count IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2,
    p_book_id OUT NUMBER
) AS
    v_count NUMBER;
    v_book_id NUMBER;
    v_pub_date DATE;
BEGIN
    -- Check if book already exists
    SELECT COUNT(*), MAX(book_id) 
    INTO v_count, v_book_id 
    FROM books 
    WHERE google_books_id = p_google_books_id;
    
    -- Convert string date to DATE (handle various formats)
    BEGIN
        IF p_published_date IS NOT NULL AND LENGTH(p_published_date) >= 4 THEN
            -- Try different date formats
            IF LENGTH(p_published_date) = 4 THEN
                -- Year only (e.g., "2023")
                v_pub_date := TO_DATE(p_published_date || '-01-01', 'YYYY-MM-DD');
            ELSIF LENGTH(p_published_date) = 7 THEN
                -- Year-Month (e.g., "2023-05")
                v_pub_date := TO_DATE(p_published_date || '-01', 'YYYY-MM-DD');
            ELSE
                -- Full date (e.g., "2023-05-15")
                v_pub_date := TO_DATE(p_published_date, 'YYYY-MM-DD');
            END IF;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            v_pub_date := NULL; -- If date parsing fails, set to NULL
    END;
    
    IF v_count > 0 THEN
        -- Book exists, update it
        UPDATE books 
        SET title = p_title,
            subtitle = p_subtitle,
            description = p_description,
            isbn_10 = p_isbn_10,
            isbn_13 = p_isbn_13,
            published_date = v_pub_date,
            publisher = p_publisher,
            page_count = p_page_count,
            language_code = p_language_code,
            cover_image_url = p_cover_image_url,
            preview_link = p_preview_link,
            info_link = p_info_link,
            -- Do NOT overwrite aggregate fields on update; they are maintained by triggers on book_ratings
            -- average_rating = NVL(p_average_rating, 0),
            -- ratings_count = NVL(p_ratings_count, 0),
            updated_at = CURRENT_TIMESTAMP
        WHERE book_id = v_book_id;
        
        p_book_id := v_book_id;
        p_result := 1;
        p_message := 'Book updated successfully';
    ELSE
        -- Book doesn't exist, insert new one
        INSERT INTO books (
            google_books_id, title, subtitle, description, isbn_10, isbn_13,
            published_date, publisher, page_count, language_code, cover_image_url,
            preview_link, info_link, average_rating, ratings_count
        ) VALUES (
            p_google_books_id, p_title, p_subtitle, p_description, p_isbn_10, p_isbn_13,
            v_pub_date, p_publisher, p_page_count, p_language_code, p_cover_image_url,
            p_preview_link, p_info_link, 0, 0
        ) RETURNING book_id INTO v_book_id;
        
        p_book_id := v_book_id;
        p_result := 1;
        p_message := 'Book added successfully';
    END IF;
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to add/update book: ' || SQLERRM;
        p_book_id := NULL;
END sp_add_or_update_book;
/

-- Procedure to add book to user's custom list
CREATE OR REPLACE PROCEDURE sp_add_book_to_list(
    p_user_id IN NUMBER,
    p_list_type IN VARCHAR2, -- 'WANT_TO_READ', 'CURRENTLY_READING', 'HAVE_READ', 'FAVORITES'
    p_book_id IN NUMBER,
    p_notes IN CLOB,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_list_id      NUMBER;
    v_count        NUMBER;
    v_want_id      NUMBER;
    v_reading_id   NUMBER;
    v_have_id      NUMBER;
    v_conflict     NUMBER;
BEGIN
    -- Resolve default list ids for cross-list logic (ignore if not found)
    BEGIN
        SELECT list_id INTO v_want_id FROM custom_lists 
        WHERE user_id = p_user_id AND list_type = 'WANT_TO_READ' AND is_default = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_want_id := NULL; END;

    BEGIN
        SELECT list_id INTO v_reading_id FROM custom_lists 
        WHERE user_id = p_user_id AND list_type = 'CURRENTLY_READING' AND is_default = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_reading_id := NULL; END;

    BEGIN
        SELECT list_id INTO v_have_id FROM custom_lists 
        WHERE user_id = p_user_id AND list_type = 'HAVE_READ' AND is_default = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_have_id := NULL; END;

    -- Find the user's default list of the requested type
    SELECT list_id INTO v_list_id
    FROM custom_lists 
    WHERE user_id = p_user_id 
      AND list_type = p_list_type 
      AND is_default = 1;

    -- Enforce coherence between core lists
    IF p_list_type = 'WANT_TO_READ' THEN
        -- Block if already finished
        IF v_have_id IS NOT NULL THEN
            SELECT COUNT(*) INTO v_conflict FROM custom_list_books 
            WHERE list_id = v_have_id AND book_id = p_book_id;
            IF v_conflict > 0 THEN
                p_result := 0; p_message := 'This book is in Have Read; cannot add to Want to Read.'; RETURN;
            END IF;
        END IF;
        -- Remove from Currently Reading
        IF v_reading_id IS NOT NULL THEN
            DELETE FROM custom_list_books WHERE list_id = v_reading_id AND book_id = p_book_id;
        END IF;
    ELSIF p_list_type = 'CURRENTLY_READING' THEN
        -- Block if already finished
        IF v_have_id IS NOT NULL THEN
            SELECT COUNT(*) INTO v_conflict FROM custom_list_books 
            WHERE list_id = v_have_id AND book_id = p_book_id;
            IF v_conflict > 0 THEN
                p_result := 0; p_message := 'This book is in Have Read; cannot move to Currently Reading.'; RETURN;
            END IF;
        END IF;
        -- Remove from Want to Read
        IF v_want_id IS NOT NULL THEN
            DELETE FROM custom_list_books WHERE list_id = v_want_id AND book_id = p_book_id;
        END IF;
    ELSIF p_list_type = 'HAVE_READ' THEN
        -- Finishing a book removes it from Want to Read and Currently Reading
        IF v_want_id IS NOT NULL THEN
            DELETE FROM custom_list_books WHERE list_id = v_want_id AND book_id = p_book_id;
        END IF;
        IF v_reading_id IS NOT NULL THEN
            DELETE FROM custom_list_books WHERE list_id = v_reading_id AND book_id = p_book_id;
        END IF;
    ELSE
        -- FAVORITES or other custom lists: allowed alongside others
        NULL;
    END IF;

    -- Check if book is already in this list
    SELECT COUNT(*) INTO v_count
    FROM custom_list_books
    WHERE list_id = v_list_id AND book_id = p_book_id;
    
    IF v_count > 0 THEN
        p_result := 0;
        p_message := 'Book is already in this list';
        RETURN;
    END IF;
    
    -- Add book to list
    INSERT INTO custom_list_books (list_id, book_id, notes)
    VALUES (v_list_id, p_book_id, p_notes);
    
    p_result := 1;
    p_message := 'Book added to list successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Default list not found for this list type';
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to add book to list: ' || SQLERRM;
END sp_add_book_to_list;
/

-- Function to get user's custom lists
CREATE OR REPLACE FUNCTION fn_get_user_lists(p_user_id IN NUMBER)
RETURN SYS_REFCURSOR AS
    list_cursor SYS_REFCURSOR;
BEGIN
    OPEN list_cursor FOR
        SELECT list_id, list_name, list_description, list_type, 
               books_count, is_default, created_at
        FROM custom_lists 
        WHERE user_id = p_user_id 
        ORDER BY is_default DESC, list_name;
    
    RETURN list_cursor;
END fn_get_user_lists;
/

-- Function to get books in a specific list
CREATE OR REPLACE FUNCTION fn_get_books_in_list(p_list_id IN NUMBER)
RETURN SYS_REFCURSOR AS
    books_cursor SYS_REFCURSOR;
BEGIN
    OPEN books_cursor FOR
        SELECT b.book_id, b.google_books_id, b.title, b.subtitle, 
               CASE 
                   WHEN b.description IS NOT NULL THEN 
                       SUBSTR(b.description, 1, 4000)  -- Convert CLOB to VARCHAR2
                   ELSE 
                       NULL 
               END as description,
               b.publisher, b.published_date, b.page_count,
               b.cover_image_url, b.average_rating, b.ratings_count,
               clb.date_added, clb.notes,
               -- Get authors as concatenated string
               (SELECT LISTAGG(a.author_name, ', ') WITHIN GROUP (ORDER BY a.author_name)
                FROM authors a
                JOIN book_authors ba ON a.author_id = ba.author_id
                WHERE ba.book_id = b.book_id) as authors
        FROM books b
        INNER JOIN custom_list_books clb ON b.book_id = clb.book_id
        WHERE clb.list_id = p_list_id
        ORDER BY clb.date_added DESC;
    
    RETURN books_cursor;
END fn_get_books_in_list;
/

-- Function to search books in database
CREATE OR REPLACE FUNCTION fn_search_books(p_search_term IN VARCHAR2)
RETURN SYS_REFCURSOR AS
    books_cursor SYS_REFCURSOR;
BEGIN
    OPEN books_cursor FOR
        SELECT book_id, google_books_id, title, subtitle, 
               CASE 
                   WHEN description IS NOT NULL THEN 
                       SUBSTR(description, 1, 4000)  -- Convert CLOB to VARCHAR2
                   ELSE 
                       NULL 
               END as description,
               publisher, published_date, page_count, cover_image_url,
               average_rating, ratings_count, preview_link, info_link
        FROM books 
        WHERE UPPER(title) LIKE UPPER('%' || p_search_term || '%')
           OR UPPER(subtitle) LIKE UPPER('%' || p_search_term || '%')
           OR UPPER(publisher) LIKE UPPER('%' || p_search_term || '%')
           OR UPPER(SUBSTR(description, 1, 4000)) LIKE UPPER('%' || p_search_term || '%')
        ORDER BY 
            CASE WHEN UPPER(title) LIKE UPPER(p_search_term || '%') THEN 1 ELSE 2 END,
            average_rating DESC,
            ratings_count DESC;
    
    RETURN books_cursor;
END fn_search_books;
/

-- Function to get book details by ID
CREATE OR REPLACE FUNCTION fn_get_book_details(p_book_id IN NUMBER)
RETURN SYS_REFCURSOR AS
    book_cursor SYS_REFCURSOR;
BEGIN
    OPEN book_cursor FOR
        SELECT book_id, google_books_id, title, subtitle, 
               CASE 
                   WHEN description IS NOT NULL THEN 
                       SUBSTR(description, 1, 4000)  -- Convert CLOB to VARCHAR2, max 4000 chars
                   ELSE 
                       NULL 
               END as description,
               isbn_10, isbn_13, published_date, publisher, page_count,
               language_code, cover_image_url, preview_link, info_link,
               average_rating, ratings_count, total_reviews, popularity_score,
               created_at, updated_at
        FROM books 
        WHERE book_id = p_book_id;
    
    RETURN book_cursor;
END fn_get_book_details;
/

-- Procedure to remove book from list
CREATE OR REPLACE PROCEDURE sp_remove_book_from_list(
    p_user_id IN NUMBER,
    p_list_type IN VARCHAR2,
    p_book_id IN NUMBER,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_list_id NUMBER;
    v_count NUMBER;
BEGIN
    -- Find the user's default list of the specified type
    SELECT list_id INTO v_list_id
    FROM custom_lists 
    WHERE user_id = p_user_id 
      AND list_type = p_list_type 
      AND is_default = 1;
    
    -- Check if book exists in this list
    SELECT COUNT(*) INTO v_count
    FROM custom_list_books
    WHERE list_id = v_list_id AND book_id = p_book_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'Book is not in this list';
        RETURN;
    END IF;
    
    -- Remove book from list
    DELETE FROM custom_list_books
    WHERE list_id = v_list_id AND book_id = p_book_id;
    
    p_result := 1;
    p_message := 'Book removed from list successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'List not found';
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to remove book from list: ' || SQLERRM;
END sp_remove_book_from_list;
/

-- Function to check if book is in user's specific list
CREATE OR REPLACE FUNCTION fn_is_book_in_list(
    p_user_id IN NUMBER,
    p_list_type IN VARCHAR2,
    p_book_id IN NUMBER
) RETURN NUMBER AS
    v_count NUMBER;
    v_list_id NUMBER;
BEGIN
    -- Find the user's default list of the specified type
    SELECT list_id INTO v_list_id
    FROM custom_lists 
    WHERE user_id = p_user_id 
      AND list_type = p_list_type 
      AND is_default = 1;
    
    -- Check if book exists in this list
    SELECT COUNT(*) INTO v_count
    FROM custom_list_books
    WHERE list_id = v_list_id AND book_id = p_book_id;
    
    RETURN v_count;
    
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END fn_is_book_in_list;
/

SHOW ERRORS

SELECT 'Book management procedures created successfully!' as STATUS FROM DUAL;
