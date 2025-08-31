-- Extracted User Procedures from Database
-- Generated on: Sun Aug 31 04:05:06 PDT 2025

-- ================================================
-- Procedure: SP_ADD_BOOK_AUTHORS
-- ================================================
CREATE OR REPLACE PROCEDURE sp_add_book_authors(
    p_book_id IN NUMBER,
    p_authors IN VARCHAR2, -- Comma-separated list of author names
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_author_id NUMBER;
    v_author_name VARCHAR2(200);
    v_start_pos NUMBER := 1;
    v_end_pos NUMBER;
    v_count NUMBER;
BEGIN
    -- First, delete existing authors for this book
    DELETE FROM book_authors WHERE book_id = p_book_id;

    -- If no authors provided, exit successfully
    IF p_authors IS NULL OR TRIM(p_authors) = '' THEN
        p_result := 1;
        p_message := 'No authors to add';
        COMMIT;
        RETURN;
    END IF;

    -- Parse comma-separated authors and add each one
    WHILE v_start_pos <= LENGTH(p_authors) LOOP
        -- Find the next comma or end of string
        v_end_pos := INSTR(p_authors, ',', v_start_pos);
        IF v_end_pos = 0 THEN
            v_end_pos := LENGTH(p_authors) + 1;
        END IF;

        -- Extract author name and trim whitespace
        v_author_name := TRIM(SUBSTR(p_authors, v_start_pos, v_end_pos - v_start_pos));

        IF v_author_name IS NOT NULL AND LENGTH(v_author_name) > 0 THEN
            -- Check if author already exists
            SELECT COUNT(*) INTO v_count FROM authors WHERE UPPER(author_name) = UPPER(v_author_name);

            IF v_count = 0 THEN
                -- Create new author
                INSERT INTO authors (author_name) VALUES (v_author_name) RETURNING author_id INTO v_author_id;
            ELSE
                -- Get existing author ID
                SELECT author_id INTO v_author_id FROM authors WHERE UPPER(author_name) = UPPER(v_author_name);
            END IF;

            -- Link author to book
            INSERT INTO book_authors (book_id, author_id) VALUES (p_book_id, v_author_id);
        END IF;

        -- Move to next author
        v_start_pos := v_end_pos + 1;
    END LOOP;

    p_result := 1;
    p_message := 'Authors added successfully';
    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Failed to add authors: ' || SQLERRM;
END sp_add_book_authors;

/

-- ================================================
-- Procedure: SP_ADD_BOOK_TO_LIST
-- ================================================
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

-- ================================================
-- Procedure: SP_ADD_OR_UPDATE_BOOK
-- ================================================
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

-- ================================================
-- Procedure: SP_CHECK_EMAIL_AVAILABILITY
-- ================================================
CREATE OR REPLACE PROCEDURE sp_check_email_availability(
    p_email IN VARCHAR2,
    p_user_id IN NUMBER DEFAULT NULL,
    p_available OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    IF p_user_id IS NULL THEN
        -- Check for new registration
        SELECT COUNT(*) INTO v_count 
        FROM users 
        WHERE UPPER(email) = UPPER(p_email);
    ELSE
        -- Check for update (exclude current user)
        SELECT COUNT(*) INTO v_count 
        FROM users 
        WHERE UPPER(email) = UPPER(p_email) 
          AND user_id != p_user_id;
    END IF;

    IF v_count > 0 THEN
        p_available := 0;
        p_message := 'Email is not available';
    ELSE
        p_available := 1;
        p_message := 'Email is available';
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_available := 0;
        p_message := 'Error checking email: ' || SQLERRM;
END sp_check_email_availability;

/

-- ================================================
-- Procedure: SP_CHECK_USERNAME_AVAILABILITY
-- ================================================
CREATE OR REPLACE PROCEDURE sp_check_username_availability(
    p_username IN VARCHAR2,
    p_user_id IN NUMBER DEFAULT NULL,
    p_available OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    IF p_user_id IS NULL THEN
        -- Check for new registration
        SELECT COUNT(*) INTO v_count 
        FROM users 
        WHERE UPPER(username) = UPPER(p_username);
    ELSE
        -- Check for update (exclude current user)
        SELECT COUNT(*) INTO v_count 
        FROM users 
        WHERE UPPER(username) = UPPER(p_username) 
          AND user_id != p_user_id;
    END IF;

    IF v_count > 0 THEN
        p_available := 0;
        p_message := 'Username is not available';
    ELSE
        p_available := 1;
        p_message := 'Username is available';
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_available := 0;
        p_message := 'Error checking username: ' || SQLERRM;
END sp_check_username_availability;

/

-- ================================================
-- Procedure: SP_DELETE_RATING
-- ================================================
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

-- ================================================
-- Procedure: SP_DELETE_REVIEW
-- ================================================
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

-- ================================================
-- Procedure: SP_LOGIN_USER
-- ================================================
CREATE OR REPLACE PROCEDURE sp_login_user(
    p_username IN VARCHAR2,
    p_password_hash IN VARCHAR2,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2,
    p_user_id OUT NUMBER,
    p_first_name OUT VARCHAR2,
    p_last_name OUT VARCHAR2,
    p_email OUT VARCHAR2
) AS
    v_count NUMBER;
    v_is_active NUMBER;
BEGIN
    -- Check if user exists and get details
    SELECT COUNT(*), 
           MAX(user_id), 
           MAX(first_name), 
           MAX(last_name), 
           MAX(email),
           MAX(is_active)
    INTO v_count, p_user_id, p_first_name, p_last_name, p_email, v_is_active
    FROM users 
    WHERE UPPER(username) = UPPER(p_username) 
      AND password_hash = p_password_hash;

    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'Invalid username or password';
        p_user_id := NULL;
        p_first_name := NULL;
        p_last_name := NULL;
        p_email := NULL;
        RETURN;
    END IF;

    IF v_is_active = 0 THEN
        p_result := 0;
        p_message := 'Account is deactivated';
        p_user_id := NULL;
        p_first_name := NULL;
        p_last_name := NULL;
        p_email := NULL;
        RETURN;
    END IF;

    -- Update last login timestamp
    UPDATE users 
    SET last_login = SYSDATE 
    WHERE user_id = p_user_id;

    p_result := 1;
    p_message := 'Login successful';

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Login failed: ' || SQLERRM;
        p_user_id := NULL;
        p_first_name := NULL;
        p_last_name := NULL;
        p_email := NULL;
END sp_login_user;

/

-- ================================================
-- Procedure: SP_RATE_BOOK
-- ================================================
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

-- ================================================
-- Procedure: SP_REGISTER_USER
-- ================================================
CREATE OR REPLACE PROCEDURE sp_register_user(
    p_username IN VARCHAR2,
    p_email IN VARCHAR2,
    p_password_hash IN VARCHAR2,
    p_first_name IN VARCHAR2,
    p_last_name IN VARCHAR2,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2,
    p_user_id OUT NUMBER
) AS
    v_count NUMBER;
    v_new_user_id NUMBER;
BEGIN
    -- Check if username already exists
    SELECT COUNT(*) INTO v_count 
    FROM users 
    WHERE UPPER(username) = UPPER(p_username);

    IF v_count > 0 THEN
        p_result := 0;
        p_message := 'Username already exists';
        p_user_id := NULL;
        RETURN;
    END IF;

    -- Check if email already exists
    SELECT COUNT(*) INTO v_count 
    FROM users 
    WHERE UPPER(email) = UPPER(p_email);

    IF v_count > 0 THEN
        p_result := 0;
        p_message := 'Email already exists';
        p_user_id := NULL;
        RETURN;
    END IF;

    -- Insert new user
    INSERT INTO users (
        username, email, password_hash, first_name, last_name
    ) VALUES (
        p_username, p_email, p_password_hash, p_first_name, p_last_name
    ) RETURNING user_id INTO v_new_user_id;

    p_result := 1;
    p_message := 'User registered successfully';
    p_user_id := v_new_user_id;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Registration failed: ' || SQLERRM;
        p_user_id := NULL;
END sp_register_user;

/

-- ================================================
-- Procedure: SP_REMOVE_BOOK_FROM_LIST
-- ================================================
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

-- ================================================
-- Procedure: SP_REVIEW_BOOK
-- ================================================
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

