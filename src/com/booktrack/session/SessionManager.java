package com.booktrack.session;

import com.booktrack.model.User;
import com.booktrack.service.UserService;

/**
 * Session Manager - Manages user session state
 * Implements Singleton pattern for global session management
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private final UserService userService;
    
    private SessionManager() {
        this.userService = new UserService();
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Login user and start session
     * @param user User object
     */
    public void login(User user) {
        this.currentUser = user;
        System.out.println("User session started for: " + user.getDisplayName());
    }
    
    /**
     * Logout user and end session
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("User session ended for: " + currentUser.getDisplayName());
            this.currentUser = null;
        }
    }
    
    /**
     * Check if user is logged in
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get current user ID
     * @return Current user ID or null if not logged in
     */
    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
    
    /**
     * Refresh current user data from database
     * @return true if refresh successful
     */
    public boolean refreshCurrentUser() {
        if (currentUser == null) {
            return false;
        }
        
        User refreshedUser = userService.getUserById(currentUser.getUserId());
        if (refreshedUser != null) {
            this.currentUser = refreshedUser;
            return true;
        }
        
        return false;
    }
    
    /**
     * Update current user profile information
     * @param firstName First name
     * @param lastName Last name
     * @param email Email
     * @param bio Bio
     * @return true if update successful
     */
    public boolean updateCurrentUserProfile(String firstName, String lastName, String email, String bio) {
        if (currentUser == null) {
            return false;
        }
        
        // Update local user object
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setBio(bio);
        
        return true;
    }
    
    /**
     * Check if current user has permission for an action
     * @param requiredUserId Required user ID for the action
     * @return true if current user has permission
     */
    public boolean hasPermission(Integer requiredUserId) {
        if (currentUser == null || requiredUserId == null) {
            return false;
        }
        
        return currentUser.getUserId().equals(requiredUserId);
    }
    
    /**
     * Get current user's display name
     * @return Display name or "Guest" if not logged in
     */
    public String getCurrentUserDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : "Guest";
    }
    
    /**
     * Get current user's full name
     * @return Full name or username if no full name, or "Guest" if not logged in
     */
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
    
    /**
     * Check if current user has a complete profile
     * @return true if user has first and last name
     */
    public boolean currentUserHasCompleteProfile() {
        return currentUser != null && currentUser.hasProfile();
    }
}
