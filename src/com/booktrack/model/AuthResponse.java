package com.booktrack.model;

/**
 * Authentication response class
 * Encapsulates the result of authentication operations
 */
public class AuthResponse {
    private boolean success;
    private String message;
    private User user;
    private Integer userId;
    
    public AuthResponse() {}
    
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AuthResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.userId = user != null ? user.getUserId() : null;
    }
    
    public AuthResponse(boolean success, String message, Integer userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }
    
    // Static factory methods for common responses
    public static AuthResponse success(String message) {
        return new AuthResponse(true, message);
    }
    
    public static AuthResponse success(String message, User user) {
        return new AuthResponse(true, message, user);
    }
    
    public static AuthResponse success(String message, Integer userId) {
        return new AuthResponse(true, message, userId);
    }
    
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message);
    }
    
    public static AuthResponse loginSuccess(User user) {
        return new AuthResponse(true, "Login successful", user);
    }
    
    public static AuthResponse registrationSuccess(Integer userId) {
        return new AuthResponse(true, "Registration successful", userId);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public User getUser() { return user; }
    public void setUser(User user) { 
        this.user = user; 
        this.userId = user != null ? user.getUserId() : null;
    }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", userId=" + userId +
                '}';
    }
}
