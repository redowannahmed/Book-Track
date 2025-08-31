-- =====================================================
-- DATABASE SETUP VALIDATION SCRIPT
-- =====================================================

-- Test 1: Check if all tables exist
SELECT 'Tables Check' as test_type, COUNT(*) as count_found, 
       CASE WHEN COUNT(*) = 10 THEN 'PASS' ELSE 'FAIL' END as status
FROM user_tables 
WHERE table_name IN ('USERS', 'BOOKS', 'AUTHORS', 'GENRES', 'TAGS', 
                     'CUSTOM_LISTS', 'BOOK_RATINGS', 'BOOK_REVIEWS', 
                     'USER_BOOK_INTERACTIONS', 'ERROR_LOG');

-- Test 2: Check if all sequences exist
SELECT 'Sequences Check' as test_type, COUNT(*) as count_found,
       CASE WHEN COUNT(*) = 9 THEN 'PASS' ELSE 'FAIL' END as status
FROM user_sequences 
WHERE sequence_name LIKE 'SEQ_%';

-- Test 3: Check if all triggers exist and are enabled
SELECT 'Triggers Check' as test_type, COUNT(*) as count_found,
       CASE WHEN COUNT(*) >= 15 THEN 'PASS' ELSE 'FAIL' END as status
FROM user_triggers 
WHERE status = 'ENABLED';

-- Test 4: List any compilation errors
SELECT 'Compilation Errors' as test_type, COUNT(*) as error_count,
       CASE WHEN COUNT(*) = 0 THEN 'PASS' ELSE 'FAIL' END as status
FROM user_errors;

-- If there are errors, show them
SELECT object_name, object_type, line, position, text as error_message
FROM user_errors
WHERE object_type IN ('TRIGGER', 'PROCEDURE', 'FUNCTION')
ORDER BY object_name, line;
