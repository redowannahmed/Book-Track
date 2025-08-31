-- Fix for CLOB issue in FN_GET_BOOK_DETAILS function
-- Replace the existing function with this version that handles CLOB properly

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

-- Also fix the search function to handle CLOB consistently
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

-- Fix the get books in list function as well
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

SHOW ERRORS;

SELECT 'CLOB functions fixed successfully!' as STATUS FROM DUAL;
