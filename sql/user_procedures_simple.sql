-- =====================================================
-- SIMPLIFIED USER PROCEDURES (without error logging)
-- Use this for initial testing
-- =====================================================

-- Procedure to register a new user
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

-- Procedure to authenticate user login
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

-- Procedure to validate username availability
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

-- Procedure to validate email availability
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

SHOW ERRORS

SELECT 'User procedures created successfully!' as STATUS FROM DUAL;
