-- =====================================================
-- USER AUTHENTICATION PROCEDURES for BookTrack
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
        
        -- Log error
        INSERT INTO error_log (error_message, table_name, operation) 
        VALUES ('sp_register_user: ' || SQLERRM, 'users', 'register');
        COMMIT;
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
        
        -- Log error
        INSERT INTO error_log (error_message, table_name, operation) 
        VALUES ('sp_login_user: ' || SQLERRM, 'users', 'login');
        COMMIT;
END sp_login_user;
/

-- Procedure to update user profile
CREATE OR REPLACE PROCEDURE sp_update_user_profile(
    p_user_id IN NUMBER,
    p_first_name IN VARCHAR2,
    p_last_name IN VARCHAR2,
    p_email IN VARCHAR2,
    p_bio IN CLOB,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
    v_current_email VARCHAR2(100);
BEGIN
    -- Check if user exists
    SELECT COUNT(*), MAX(email) 
    INTO v_count, v_current_email 
    FROM users 
    WHERE user_id = p_user_id;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'User not found';
        RETURN;
    END IF;
    
    -- Check if new email already exists (only if email is being changed)
    IF UPPER(p_email) != UPPER(v_current_email) THEN
        SELECT COUNT(*) INTO v_count 
        FROM users 
        WHERE UPPER(email) = UPPER(p_email) 
          AND user_id != p_user_id;
        
        IF v_count > 0 THEN
            p_result := 0;
            p_message := 'Email already exists';
            RETURN;
        END IF;
    END IF;
    
    -- Update user profile
    UPDATE users 
    SET first_name = p_first_name,
        last_name = p_last_name,
        email = p_email,
        bio = p_bio,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;
    
    p_result := 1;
    p_message := 'Profile updated successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Update failed: ' || SQLERRM;
        
        -- Log error
        INSERT INTO error_log (error_message, table_name, operation) 
        VALUES ('sp_update_user_profile: ' || SQLERRM, 'users', 'update_profile');
        COMMIT;
END sp_update_user_profile;
/

-- Procedure to change user password
CREATE OR REPLACE PROCEDURE sp_change_password(
    p_user_id IN NUMBER,
    p_old_password_hash IN VARCHAR2,
    p_new_password_hash IN VARCHAR2,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2
) AS
    v_count NUMBER;
BEGIN
    -- Verify current password
    SELECT COUNT(*) 
    INTO v_count 
    FROM users 
    WHERE user_id = p_user_id 
      AND password_hash = p_old_password_hash;
    
    IF v_count = 0 THEN
        p_result := 0;
        p_message := 'Current password is incorrect';
        RETURN;
    END IF;
    
    -- Update password
    UPDATE users 
    SET password_hash = p_new_password_hash,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;
    
    p_result := 1;
    p_message := 'Password changed successfully';
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := 'Password change failed: ' || SQLERRM;
        
        -- Log error
        INSERT INTO error_log (error_message, table_name, operation) 
        VALUES ('sp_change_password: ' || SQLERRM, 'users', 'change_password');
        COMMIT;
END sp_change_password;
/

-- Function to get user details by ID
CREATE OR REPLACE FUNCTION fn_get_user_details(p_user_id IN NUMBER)
RETURN SYS_REFCURSOR AS
    user_cursor SYS_REFCURSOR;
BEGIN
    OPEN user_cursor FOR
        SELECT user_id, username, email, first_name, last_name, 
               bio, date_joined, last_login, total_books_read, total_reviews
        FROM users 
        WHERE user_id = p_user_id AND is_active = 1;
    
    RETURN user_cursor;
END fn_get_user_details;
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
