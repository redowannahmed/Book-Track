-- =====================================================
-- TEST SCRIPT for User Procedures
-- =====================================================

-- Test 1: Check username availability
DECLARE
    v_available NUMBER;
    v_message VARCHAR2(100);
BEGIN
    sp_check_username_availability('testuser', NULL, v_available, v_message);
    DBMS_OUTPUT.PUT_LINE('Username available: ' || v_available || ' - ' || v_message);
END;
/

-- Test 2: Register a test user
DECLARE
    v_result NUMBER;
    v_message VARCHAR2(100);
    v_user_id NUMBER;
BEGIN
    sp_register_user(
        'testuser', 
        'test@example.com', 
        'hashedpassword123', 
        'Test', 
        'User', 
        v_result, 
        v_message, 
        v_user_id
    );
    DBMS_OUTPUT.PUT_LINE('Registration result: ' || v_result || ' - ' || v_message || ' - User ID: ' || v_user_id);
END;
/

-- Test 3: Try to login with the test user
DECLARE
    v_result NUMBER;
    v_message VARCHAR2(100);
    v_user_id NUMBER;
    v_first_name VARCHAR2(50);
    v_last_name VARCHAR2(50);
    v_email VARCHAR2(100);
BEGIN
    sp_login_user(
        'testuser', 
        'hashedpassword123', 
        v_result, 
        v_message, 
        v_user_id, 
        v_first_name, 
        v_last_name, 
        v_email
    );
    DBMS_OUTPUT.PUT_LINE('Login result: ' || v_result || ' - ' || v_message);
    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('User: ' || v_first_name || ' ' || v_last_name || ' (' || v_email || ')');
    END IF;
END;
/

-- Clean up test data
DELETE FROM users WHERE username = 'testuser';
COMMIT;

SELECT 'Test completed!' as STATUS FROM DUAL;
