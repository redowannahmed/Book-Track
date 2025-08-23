package com.booktrack.service;

import com.booktrack.dao.UserDAO;
import com.booktrack.model.AuthResponse;
import com.booktrack.model.User;
import com.booktrack.util.PasswordUtil;

/**
 * User Service class - Business Logic Layer
 * Handles user-related business logic and validation
 * Implements Single Responsibility Principle and Dependency Inversion
 */
public class UserService {
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    // Constructor for dependency injection (for testing)
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Register a new user with validation
     * @param username Username
     * @param email Email address
     * @param password Plain text password
     * @param firstName First name
     * @param lastName Last name
     * @return AuthResponse indicating success/failure
     */
    public AuthResponse registerUser(String username, String email, String password, 
                                   String firstName, String lastName) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            return AuthResponse.failure("Username is required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            return AuthResponse.failure("Email is required");
        }
        
        if (password == null || password.trim().isEmpty()) {
            return AuthResponse.failure("Password is required");
        }
        
        // Trim inputs
        username = username.trim();
        email = email.trim();
        firstName = firstName != null ? firstName.trim() : null;
        lastName = lastName != null ? lastName.trim() : null;
        
        // Create user object for validation
        User user = new User(username, email, null, firstName, lastName);
        
        // Validate username format
        if (!user.isValidUsername()) {
            return AuthResponse.failure("Username must be 3-50 characters long and contain only letters, numbers, and underscores");
        }
        
        // Validate email format
        if (!user.isValidEmail()) {
            return AuthResponse.failure("Please enter a valid email address");
        }
        
        // Validate password strength
        PasswordUtil.ValidationResult passwordValidation = PasswordUtil.validatePasswordStrength(password);
        if (!passwordValidation.isValid()) {
            return AuthResponse.failure(passwordValidation.getMessage());
        }
        
        // Check username availability
        if (!userDAO.isUsernameAvailable(username, null)) {
            return AuthResponse.failure("Username is already taken");
        }
        
        // Check email availability
        if (!userDAO.isEmailAvailable(email, null)) {
            return AuthResponse.failure("Email is already registered");
        }
        
        // Hash password and set in user object
        String passwordHash = PasswordUtil.hashPassword(password);
        user.setPasswordHash(passwordHash);
        
        // Register user
        return userDAO.registerUser(user);
    }
    
    /**
     * Authenticate user login
     * @param username Username
     * @param password Plain text password
     * @return AuthResponse with user details if successful
     */
    public AuthResponse loginUser(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            return AuthResponse.failure("Username is required");
        }
        
        if (password == null || password.trim().isEmpty()) {
            return AuthResponse.failure("Password is required");
        }
        
        username = username.trim();
        
        // Get user by username and verify password
        return userDAO.loginUserWithPasswordVerification(username, password);
    }
    
    /**
     * Update user profile
     * @param userId User ID
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param bio Bio
     * @return AuthResponse indicating success/failure
     */
    public AuthResponse updateUserProfile(Integer userId, String firstName, String lastName, 
                                        String email, String bio) {
        // Validate input
        if (userId == null) {
            return AuthResponse.failure("User ID is required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            return AuthResponse.failure("Email is required");
        }
        
        // Trim inputs
        email = email.trim();
        firstName = firstName != null ? firstName.trim() : null;
        lastName = lastName != null ? lastName.trim() : null;
        bio = bio != null ? bio.trim() : null;
        
        // Create user object for validation
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBio(bio);
        
        // Validate email format
        if (!user.isValidEmail()) {
            return AuthResponse.failure("Please enter a valid email address");
        }
        
        // Check email availability (excluding current user)
        if (!userDAO.isEmailAvailable(email, userId)) {
            return AuthResponse.failure("Email is already registered to another user");
        }
        
        // Update profile
        return userDAO.updateUserProfile(user);
    }
    
    /**
     * Change user password
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @param confirmPassword Confirm new password
     * @return AuthResponse indicating success/failure
     */
    public AuthResponse changePassword(Integer userId, String currentPassword, 
                                     String newPassword, String confirmPassword) {
        // Validate input
        if (userId == null) {
            return AuthResponse.failure("User ID is required");
        }
        
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return AuthResponse.failure("Current password is required");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return AuthResponse.failure("New password is required");
        }
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return AuthResponse.failure("Password confirmation is required");
        }
        
        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            return AuthResponse.failure("New password and confirmation do not match");
        }
        
        // Check if current and new password are the same
        if (currentPassword.equals(newPassword)) {
            return AuthResponse.failure("New password must be different from current password");
        }
        
        // Validate new password strength
        PasswordUtil.ValidationResult passwordValidation = PasswordUtil.validatePasswordStrength(newPassword);
        if (!passwordValidation.isValid()) {
            return AuthResponse.failure(passwordValidation.getMessage());
        }
        
        // Hash passwords
        String currentPasswordHash = PasswordUtil.hashPassword(currentPassword);
        String newPasswordHash = PasswordUtil.hashPassword(newPassword);
        
        // Change password
        return userDAO.changePassword(userId, currentPasswordHash, newPasswordHash);
    }
    
    /**
     * Get user details by ID
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userDAO.getUserById(userId);
    }
    
    /**
     * Check username availability
     * @param username Username to check
     * @param excludeUserId User ID to exclude from check (optional)
     * @return true if available
     */
    public boolean isUsernameAvailable(String username, Integer excludeUserId) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        username = username.trim();
        
        // Validate username format first
        User tempUser = new User();
        tempUser.setUsername(username);
        if (!tempUser.isValidUsername()) {
            return false;
        }
        
        return userDAO.isUsernameAvailable(username, excludeUserId);
    }
    
    /**
     * Check email availability
     * @param email Email to check
     * @param excludeUserId User ID to exclude from check (optional)
     * @return true if available
     */
    public boolean isEmailAvailable(String email, Integer excludeUserId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim();
        
        // Validate email format first
        User tempUser = new User();
        tempUser.setEmail(email);
        if (!tempUser.isValidEmail()) {
            return false;
        }
        
        return userDAO.isEmailAvailable(email, excludeUserId);
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return ValidationResult with result and message
     */
    public PasswordUtil.ValidationResult validatePassword(String password) {
        return PasswordUtil.validatePasswordStrength(password);
    }
}
