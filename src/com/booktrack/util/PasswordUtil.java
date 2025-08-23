package com.booktrack.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password utility class for hashing and validation
 * Uses SHA-256 with salt for secure password storage
 */
public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final String CHARSET = "UTF-8";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Hash a password with a random salt
     * @param password The plain text password
     * @return The hashed password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(CHARSET));
            
            // Combine salt and hash
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            // Return base64 encoded string
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     * @param password The plain text password
     * @param storedHash The stored hash with salt
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Decode the stored hash
            byte[] combined = Base64.getDecoder().decode(storedHash);
            
            // Extract salt and hash
            byte[] salt = new byte[SALT_LENGTH];
            byte[] storedPassword = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, storedPassword, 0, storedPassword.length);
            
            // Hash the provided password with the extracted salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(CHARSET));
            
            // Compare the hashes
            return MessageDigest.isEqual(storedPassword, hashedPassword);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate password strength
     * @param password The password to validate
     * @return ValidationResult containing validation status and message
     */
    public static ValidationResult validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            return new ValidationResult(false, "Password cannot exceed 128 characters");
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (isSpecialCharacter(c)) hasSpecial = true;
        }
        
        if (!hasUpper) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter");
        }
        if (!hasLower) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter");
        }
        if (!hasDigit) {
            return new ValidationResult(false, "Password must contain at least one digit");
        }
        if (!hasSpecial) {
            return new ValidationResult(false, "Password must contain at least one special character");
        }
        
        return new ValidationResult(true, "Password is strong");
    }
    
    private static boolean isSpecialCharacter(char c) {
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        return specialChars.indexOf(c) >= 0;
    }
    
    /**
     * Generate a temporary password
     * @param length The length of the password
     * @return A randomly generated password
     */
    public static String generateTemporaryPassword(int length) {
        if (length < 8) length = 8;
        
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String allChars = upperCase + lowerCase + digits + special;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }
    
    private static String shuffleString(String str) {
        char[] chars = str.toCharArray();
        SecureRandom random = new SecureRandom();
        
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
