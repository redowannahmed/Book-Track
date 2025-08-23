package com.booktrack.model;

import java.time.LocalDateTime;

/**
 * User model class representing user entity
 * Encapsulates user data and business rules
 */
public class User {
    private Integer userId;
    private String username;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String bio;
    private LocalDateTime dateJoined;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private int totalBooksRead;
    private int totalReviews;
    
    // Default constructor
    public User() {
        this.isActive = true;
        this.totalBooksRead = 0;
        this.totalReviews = 0;
    }
    
    // Constructor for registration
    public User(String username, String email, String passwordHash, 
                String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Constructor with all fields (for database retrieval)
    public User(Integer userId, String username, String email, String firstName, 
                String lastName, String bio, LocalDateTime dateJoined, 
                LocalDateTime lastLogin, boolean isActive, int totalBooksRead, 
                int totalReviews) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.dateJoined = dateJoined;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.totalBooksRead = totalBooksRead;
        this.totalReviews = totalReviews;
    }
    
    // Business logic methods
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName.append(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName.trim());
        }
        return fullName.length() > 0 ? fullName.toString() : username;
    }
    
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.equals(username) ? username : fullName + " (" + username + ")";
    }
    
    public boolean hasProfile() {
        return firstName != null && !firstName.trim().isEmpty() && 
               lastName != null && !lastName.trim().isEmpty();
    }
    
    // Validation methods
    public boolean isValidForRegistration() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               passwordHash != null && !passwordHash.trim().isEmpty();
    }
    
    public boolean isValidEmail() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    public boolean isValidUsername() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        // Username should be 3-50 characters, alphanumeric with underscores
        String usernameRegex = "^[a-zA-Z0-9_]{3,50}$";
        return username.matches(usernameRegex);
    }
    
    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public LocalDateTime getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDateTime dateJoined) { this.dateJoined = dateJoined; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getTotalBooksRead() { return totalBooksRead; }
    public void setTotalBooksRead(int totalBooksRead) { this.totalBooksRead = totalBooksRead; }
    
    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId != null && userId.equals(user.userId);
    }
    
    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
