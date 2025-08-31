-- =====================================================
-- TRIGGER TESTING SCRIPT
-- =====================================================

-- Test 1: Verify default lists were created for users
SELECT 'Default Lists Test' as test_name,
       u.username,
       cl.list_name,
       cl.list_type,
       cl.is_default
FROM users u
JOIN custom_lists cl ON u.user_id = cl.user_id
WHERE cl.is_default = 1
ORDER BY u.username, cl.list_type;

-- Test 2: Verify book rating calculations
SELECT 'Rating Calculations Test' as test_name,
       b.title,
       b.average_rating,
       b.ratings_count,
       (SELECT AVG(rating) FROM book_ratings WHERE book_id = b.book_id) as calculated_avg,
       (SELECT COUNT(*) FROM book_ratings WHERE book_id = b.book_id) as calculated_count
FROM books b
WHERE b.ratings_count > 0;

-- Test 3: Verify user interaction logging
SELECT 'Interaction Logging Test' as test_name,
       u.username,
       b.title,
       ubi.interaction_type,
       ubi.interaction_details,
       ubi.interaction_date
FROM user_book_interactions ubi
JOIN users u ON ubi.user_id = u.user_id
JOIN books b ON ubi.book_id = b.book_id
ORDER BY ubi.interaction_date DESC;

-- Test 4: Verify list book counts
SELECT 'List Book Counts Test' as test_name,
       u.username,
       cl.list_name,
       cl.books_count,
       (SELECT COUNT(*) FROM custom_list_books WHERE list_id = cl.list_id) as actual_count
FROM custom_lists cl
JOIN users u ON cl.user_id = u.user_id
ORDER BY u.username, cl.list_name;

-- Test 5: Verify user total books read count
SELECT 'User Books Read Test' as test_name,
       u.username,
       u.total_books_read,
       (SELECT COUNT(DISTINCT clb.book_id)
        FROM custom_list_books clb
        JOIN custom_lists cl ON clb.list_id = cl.list_id
        WHERE cl.user_id = u.user_id AND cl.list_type = 'HAVE_READ') as calculated_read_count
FROM users u;

-- Test 6: Verify review counts
SELECT 'Review Counts Test' as test_name,
       b.title,
       b.total_reviews as book_review_count,
       (SELECT COUNT(*) FROM book_reviews WHERE book_id = b.book_id AND is_public = 1) as actual_review_count
FROM books b;

-- Test 7: Check for any errors in error_log
SELECT 'Error Log Test' as test_name,
       COUNT(*) as error_count,
       CASE WHEN COUNT(*) = 0 THEN 'PASS - No errors' ELSE 'CHECK - Has errors' END as status
FROM error_log;

-- If there are errors, show them
SELECT 'Recent Errors' as test_name,
       error_message,
       error_date,
       table_name,
       operation
FROM error_log
WHERE error_date >= SYSDATE - 1
ORDER BY error_date DESC;
