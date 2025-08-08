package com.medicore.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtil {
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Username validation pattern (alphanumeric, underscore, hyphen, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    
    // Phone number validation pattern (supports various formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[1-9]?[0-9]{7,15}$"
    );
    
    // Password strength pattern (at least 8 characters, one uppercase, one lowercase, one digit)
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$"
    );
    
    // Name validation pattern (letters, spaces, hyphens, apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{2,50}$");
    
    // Employee ID pattern
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{3,6}$");
    
    // License number pattern
    private static final Pattern LICENSE_PATTERN = Pattern.compile("^[A-Z0-9]{5,20}$");
    
    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate username
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * Validate username or email
     */
    public static boolean isValidUsernameOrEmail(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = input.trim();
        return isValidUsername(trimmed) || isValidEmail(trimmed);
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Phone number is optional in many cases
        }
        
        // Remove spaces, hyphens, and parentheses for validation
        String cleaned = phoneNumber.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validate basic password (minimum length only)
     */
    public static boolean isValidPassword(String password, int minLength) {
        return password != null && password.length() >= minLength;
    }
    
    /**
     * Validate name (first name, last name)
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate employee ID
     */
    public static boolean isValidEmployeeId(String employeeId) {
        return employeeId != null && EMPLOYEE_ID_PATTERN.matcher(employeeId.trim()).matches();
    }
    
    /**
     * Validate license number
     */
    public static boolean isValidLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            return true; // License number is optional for some roles
        }
        
        return LICENSE_PATTERN.matcher(licenseNumber.trim()).matches();
    }
    
    /**
     * Validate age (must be between min and max)
     */
    public static boolean isValidAge(int age, int minAge, int maxAge) {
        return age >= minAge && age <= maxAge;
    }
    
    /**
     * Validate salary (must be positive)
     */
    public static boolean isValidSalary(Double salary) {
        return salary == null || salary >= 0;
    }
    
    /**
     * Validate required field (not null and not empty)
     */
    public static boolean isRequired(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) return false;
        
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validate numeric string
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate integer string
     */
    public static boolean isInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate positive integer
     */
    public static boolean isPositiveInteger(String value) {
        if (!isInteger(value)) {
            return false;
        }
        
        return Integer.parseInt(value.trim()) > 0;
    }
    
    /**
     * Validate date format (YYYY-MM-DD)
     */
    public static boolean isValidDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        return datePattern.matcher(date.trim()).matches();
    }
    
    /**
     * Validate time format (HH:MM)
     */
    public static boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        
        Pattern timePattern = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
        return timePattern.matcher(time.trim()).matches();
    }
    
    /**
     * Sanitize input string (remove potentially harmful characters)
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove HTML tags and script content
        String sanitized = input.replaceAll("<[^>]*>", "");
        
        // Remove potentially harmful characters
        sanitized = sanitized.replaceAll("[<>\"'%;()&+]", "");
        
        return sanitized.trim();
    }
    
    /**
     * Validate file extension
     */
    public static boolean isValidFileExtension(String filename, String[] allowedExtensions) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(filename);
        if (extension == null) {
            return false;
        }
        
        for (String allowed : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * Validate blood group
     */
    public static boolean isValidBloodGroup(String bloodGroup) {
        if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
            return true; // Optional field
        }
        
        String[] validBloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        String trimmed = bloodGroup.trim().toUpperCase();
        
        for (String valid : validBloodGroups) {
            if (valid.equals(trimmed)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate gender
     */
    public static boolean isValidGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return true; // Optional field
        }
        
        String[] validGenders = {"MALE", "FEMALE", "OTHER"};
        String trimmed = gender.trim().toUpperCase();
        
        for (String valid : validGenders) {
            if (valid.equals(trimmed)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get validation error message for password
     */
    public static String getPasswordValidationMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        
        return null; // Valid password
    }
    
    /**
     * Check if two passwords match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}
