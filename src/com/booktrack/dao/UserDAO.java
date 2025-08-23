package com.booktrack.dao;

import com.booktrack.config.DatabaseConfig;
import com.booktrack.model.AuthResponse;
import com.booktrack.model.User;
import com.booktrack.util.PasswordUtil;

import java.io.StringReader;
import java.sql.*;

/**
 * User Data Access Object
 * Handles all database operations related to users
 * Implements Single Responsibility Principle
 */
public class UserDAO {
    private final DatabaseConfig dbConfig;
    
    public UserDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Register a new user
     * @param user User object with registration details
     * @return AuthResponse indicating success/failure and user ID
     */
    public AuthResponse registerUser(User user) {
        String sql = "{ call sp_register_user(?, ?, ?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set input parameters
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            
            // Register output parameters
            stmt.registerOutParameter(6, Types.INTEGER);    // p_result
            stmt.registerOutParameter(7, Types.VARCHAR);   // p_message
            stmt.registerOutParameter(8, Types.INTEGER);    // p_user_id
            
            stmt.execute();
            
            int result = stmt.getInt(6);
            String message = stmt.getString(7);
            Integer userId = stmt.getInt(8);
            
            if (result == 1) {
                return AuthResponse.registrationSuccess(userId);
            } else {
                return AuthResponse.failure(message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error during user registration: " + e.getMessage());
            return AuthResponse.failure("Registration failed: Database error");
        }
    }
    
    /**
     * Authenticate user login
     * @param username Username
     * @param passwordHash Hashed password
     * @return AuthResponse with user details if successful
     */
    public AuthResponse loginUser(String username, String passwordHash) {
        String sql = "{ call sp_login_user(?, ?, ?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set input parameters
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            
            // Register output parameters
            stmt.registerOutParameter(3, Types.INTEGER);    // p_result
            stmt.registerOutParameter(4, Types.VARCHAR);   // p_message
            stmt.registerOutParameter(5, Types.INTEGER);    // p_user_id
            stmt.registerOutParameter(6, Types.VARCHAR);   // p_first_name
            stmt.registerOutParameter(7, Types.VARCHAR);   // p_last_name
            stmt.registerOutParameter(8, Types.VARCHAR);   // p_email
            
            stmt.execute();
            
            int result = stmt.getInt(3);
            String message = stmt.getString(4);
            
            if (result == 1) {
                // Create user object with login details
                User user = new User();
                user.setUserId(stmt.getInt(5));
                user.setUsername(username);
                user.setFirstName(stmt.getString(6));
                user.setLastName(stmt.getString(7));
                user.setEmail(stmt.getString(8));
                
                return AuthResponse.loginSuccess(user);
            } else {
                return AuthResponse.failure(message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error during user login: " + e.getMessage());
            return AuthResponse.failure("Login failed: Database error");
        }
    }
    
    /**
     * Login user with password verification (handles salted passwords)
     * @param username Username
     * @param password Plain text password
     * @return AuthResponse with user details if successful
     */
    public AuthResponse loginUserWithPasswordVerification(String username, String password) {
        // First, get the user by username to retrieve the stored password hash
        String sql = "SELECT user_id, username, email, first_name, last_name, password_hash, is_active " +
                    "FROM users WHERE UPPER(username) = UPPER(?) AND is_active = 1";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPasswordHash = rs.getString("password_hash");
                    
                    // Verify password using the utility method
                    if (PasswordUtil.verifyPassword(password, storedPasswordHash)) {
                        // Password is correct, update last login and return user
                        Integer userId = rs.getInt("user_id");
                        
                        // Update last login
                        updateLastLogin(userId);
                        
                        // Create user object
                        User user = new User();
                        user.setUserId(userId);
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setFirstName(rs.getString("first_name"));
                        user.setLastName(rs.getString("last_name"));
                        
                        return AuthResponse.loginSuccess(user);
                    } else {
                        return AuthResponse.failure("Invalid username or password");
                    }
                } else {
                    return AuthResponse.failure("Invalid username or password");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error during password verification login: " + e.getMessage());
            return AuthResponse.failure("Login failed: Database error");
        }
    }
    
    /**
     * Helper method to update last login timestamp
     * @param userId User ID
     */
    private void updateLastLogin(Integer userId) {
        String sql = "UPDATE users SET last_login = SYSDATE WHERE user_id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            // Don't fail the login just because we couldn't update the timestamp
        }
    }
    
    /**
     * Update user profile
     * @param user User object with updated details
     * @return AuthResponse indicating success/failure
     */
    public AuthResponse updateUserProfile(User user) {
        String sql = "{ call sp_update_user_profile(?, ?, ?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set input parameters
            stmt.setInt(1, user.getUserId());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getEmail());
            
            // Handle CLOB for bio
            if (user.getBio() != null) {
                stmt.setClob(5, new StringReader(user.getBio()));
            } else {
                stmt.setNull(5, Types.CLOB);
            }
            
            // Register output parameters
            stmt.registerOutParameter(6, Types.INTEGER);    // p_result
            stmt.registerOutParameter(7, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(6);
            String message = stmt.getString(7);
            
            if (result == 1) {
                return AuthResponse.success(message);
            } else {
                return AuthResponse.failure(message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            return AuthResponse.failure("Profile update failed: Database error");
        }
    }
    
    /**
     * Change user password
     * @param userId User ID
     * @param oldPasswordHash Current password hash
     * @param newPasswordHash New password hash
     * @return AuthResponse indicating success/failure
     */
    public AuthResponse changePassword(Integer userId, String oldPasswordHash, String newPasswordHash) {
        String sql = "{ call sp_change_password(?, ?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            // Set input parameters
            stmt.setInt(1, userId);
            stmt.setString(2, oldPasswordHash);
            stmt.setString(3, newPasswordHash);
            
            // Register output parameters
            stmt.registerOutParameter(4, Types.INTEGER);    // p_result
            stmt.registerOutParameter(5, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            int result = stmt.getInt(4);
            String message = stmt.getString(5);
            
            if (result == 1) {
                return AuthResponse.success(message);
            } else {
                return AuthResponse.failure(message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return AuthResponse.failure("Password change failed: Database error");
        }
    }
    
    /**
     * Get user details by ID
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(Integer userId) {
        String sql = "{ ? = call fn_get_user_details(?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, userId);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setBio(rs.getString("bio"));
                    
                    // Handle timestamp conversion
                    Timestamp dateJoined = rs.getTimestamp("date_joined");
                    if (dateJoined != null) {
                        user.setDateJoined(dateJoined.toLocalDateTime());
                    }
                    
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) {
                        user.setLastLogin(lastLogin.toLocalDateTime());
                    }
                    
                    user.setTotalBooksRead(rs.getInt("total_books_read"));
                    user.setTotalReviews(rs.getInt("total_reviews"));
                    
                    return user;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Check username availability
     * @param username Username to check
     * @param excludeUserId User ID to exclude from check (for updates)
     * @return true if available
     */
    public boolean isUsernameAvailable(String username, Integer excludeUserId) {
        String sql = "{ call sp_check_username_availability(?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, username);
            if (excludeUserId != null) {
                stmt.setInt(2, excludeUserId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.registerOutParameter(3, Types.INTEGER);    // p_available
            stmt.registerOutParameter(4, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            return stmt.getInt(3) == 1;
            
        } catch (SQLException e) {
            System.err.println("Error checking username availability: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check email availability
     * @param email Email to check
     * @param excludeUserId User ID to exclude from check (for updates)
     * @return true if available
     */
    public boolean isEmailAvailable(String email, Integer excludeUserId) {
        String sql = "{ call sp_check_email_availability(?, ?, ?, ?) }";
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, email);
            if (excludeUserId != null) {
                stmt.setInt(2, excludeUserId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.registerOutParameter(3, Types.INTEGER);    // p_available
            stmt.registerOutParameter(4, Types.VARCHAR);   // p_message
            
            stmt.execute();
            
            return stmt.getInt(3) == 1;
            
        } catch (SQLException e) {
            System.err.println("Error checking email availability: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user's custom lists
     * @param userId User ID
     * @return List of custom lists data as [list_id, list_name, list_description, list_type, books_count, is_default]
     */
    public java.util.List<String[]> getUserCustomLists(Integer userId) {
        String sql = "{ ? = call fn_get_user_lists(?) }";
        java.util.List<String[]> lists = new java.util.ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, userId);
            
            stmt.execute();
            
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    lists.add(new String[] {
                        String.valueOf(rs.getInt("list_id")),
                        rs.getString("list_name"),
                        rs.getString("list_description"),
                        rs.getString("list_type"),
                        String.valueOf(rs.getInt("books_count")),
                        String.valueOf(rs.getInt("is_default"))
                    });
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user custom lists: " + e.getMessage());
        }
        
        return lists;
    }
}
