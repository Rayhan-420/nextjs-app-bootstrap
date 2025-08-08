package com.medicore.service;

import com.medicore.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for user authentication and authorization
 */
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    /**
     * Authenticate user with username/email and password
     */
    public User authenticate(String usernameOrEmail, String password, String selectedRole) throws SQLException {
        logger.info("Attempting authentication for user: {} with role: {}", usernameOrEmail, selectedRole);
        
        String sql = """
            SELECT id, username, password, email, first_name, last_name, phone_number, 
                   address, role, status, profile_picture, employee_id, department, 
                   specialization, license_number, salary, shift, created_at, updated_at, 
                   last_login, session_token
            FROM users 
            WHERE (username = ? OR email = ?) AND role = ?
        """;
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            pstmt.setString(3, selectedRole.toUpperCase().replace(" ", "_"));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    
                    // Verify password
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        User user = mapResultSetToUser(rs);
                        
                        // Check if user is active
                        if (user.getStatus() != User.Status.ACTIVE) {
                            logger.warn("Authentication failed - user not active: {}", usernameOrEmail);
                            throw new SQLException("Account is not active. Please contact administrator.");
                        }
                        
                        // Update last login and generate session token
                        updateLastLoginAndGenerateSession(user);
                        
                        logger.info("Authentication successful for user: {}", user.getUsername());
                        return user;
                    } else {
                        logger.warn("Authentication failed - invalid password for user: {}", usernameOrEmail);
                    }
                } else {
                    logger.warn("Authentication failed - user not found: {} with role: {}", usernameOrEmail, selectedRole);
                }
            }
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Register a new user
     */
    public User registerUser(User user, String password) throws SQLException {
        logger.info("Registering new user: {} with role: {}", user.getUsername(), user.getRole());
        
        // Check if username or email already exists
        if (isUsernameOrEmailExists(user.getUsername(), user.getEmail())) {
            throw new SQLException("Username or email already exists");
        }
        
        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        String sql = """
            INSERT INTO users (username, password, email, first_name, last_name, phone_number, 
                             address, role, status, employee_id, department, specialization, 
                             license_number, salary, shift) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFirstName());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getPhoneNumber());
            pstmt.setString(7, user.getAddress());
            pstmt.setString(8, user.getRole().name());
            pstmt.setString(9, user.getStatus().name());
            pstmt.setString(10, user.getEmployeeId());
            pstmt.setString(11, user.getDepartment());
            pstmt.setString(12, user.getSpecialization());
            pstmt.setString(13, user.getLicenseNumber());
            pstmt.setDouble(14, user.getSalary() != null ? user.getSalary() : 0.0);
            pstmt.setString(15, user.getShift());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                        logger.info("User registered successfully with ID: {}", user.getId());
                        
                        // Create role-specific records if needed
                        createRoleSpecificRecords(user);
                        
                        return user;
                    }
                }
            }
        }
        
        throw new SQLException("Failed to register user");
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) throws SQLException {
        logger.info("Attempting password change for user ID: {}", userId);
        
        // First verify current password
        String selectSql = "SELECT password FROM users WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(selectSql)) {
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String currentHashedPassword = rs.getString("password");
                    
                    if (!BCrypt.checkpw(currentPassword, currentHashedPassword)) {
                        logger.warn("Password change failed - current password incorrect for user ID: {}", userId);
                        return false;
                    }
                } else {
                    logger.warn("Password change failed - user not found: {}", userId);
                    return false;
                }
            }
        }
        
        // Update password
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String updateSql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(updateSql)) {
            pstmt.setString(1, hashedNewPassword);
            pstmt.setLong(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Password changed successfully for user ID: {}", userId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Reset password (for forgot password functionality)
     */
    public boolean resetPassword(String usernameOrEmail, String newPassword) throws SQLException {
        logger.info("Attempting password reset for user: {}", usernameOrEmail);
        
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ? OR email = ?";
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, hashedNewPassword);
            pstmt.setString(2, usernameOrEmail);
            pstmt.setString(3, usernameOrEmail);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Password reset successfully for user: {}", usernameOrEmail);
                return true;
            }
        }
        
        logger.warn("Password reset failed - user not found: {}", usernameOrEmail);
        return false;
    }
    
    /**
     * Logout user by clearing session token
     */
    public boolean logout(Long userId) throws SQLException {
        logger.info("Logging out user ID: {}", userId);
        
        String sql = "UPDATE users SET session_token = NULL WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("User logged out successfully: {}", userId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate session token
     */
    public User validateSession(String sessionToken) throws SQLException {
        String sql = """
            SELECT id, username, password, email, first_name, last_name, phone_number, 
                   address, role, status, profile_picture, employee_id, department, 
                   specialization, license_number, salary, shift, created_at, updated_at, 
                   last_login, session_token
            FROM users 
            WHERE session_token = ? AND status = 'ACTIVE'
        """;
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, sessionToken);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if username or email already exists
     */
    private boolean isUsernameOrEmailExists(String username, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Update last login time and generate session token
     */
    private void updateLastLoginAndGenerateSession(User user) throws SQLException {
        String sessionToken = UUID.randomUUID().toString();
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP, session_token = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, sessionToken);
            pstmt.setLong(2, user.getId());
            
            pstmt.executeUpdate();
            user.setSessionToken(sessionToken);
            user.updateLastLogin();
        }
    }
    
    /**
     * Create role-specific records (patients, doctors tables)
     */
    private void createRoleSpecificRecords(User user) throws SQLException {
        switch (user.getRole()) {
            case PATIENT:
                createPatientRecord(user);
                break;
            case DOCTOR:
                createDoctorRecord(user);
                break;
            // Add other role-specific record creation as needed
        }
    }
    
    /**
     * Create patient-specific record
     */
    private void createPatientRecord(User user) throws SQLException {
        String patientId = generatePatientId();
        String sql = "INSERT INTO patients (user_id, patient_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, patientId);
            pstmt.executeUpdate();
            
            logger.info("Patient record created with ID: {}", patientId);
        }
    }
    
    /**
     * Create doctor-specific record
     */
    private void createDoctorRecord(User user) throws SQLException {
        String doctorId = generateDoctorId();
        String sql = "INSERT INTO doctors (user_id, doctor_id, specialization) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, doctorId);
            pstmt.setString(3, user.getSpecialization());
            pstmt.executeUpdate();
            
            logger.info("Doctor record created with ID: {}", doctorId);
        }
    }
    
    /**
     * Generate unique patient ID
     */
    private String generatePatientId() {
        return "PAT" + System.currentTimeMillis() % 100000;
    }
    
    /**
     * Generate unique doctor ID
     */
    private String generateDoctorId() {
        return "DOC" + System.currentTimeMillis() % 100000;
    }
    
    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setAddress(rs.getString("address"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setStatus(User.Status.valueOf(rs.getString("status")));
        user.setProfilePicture(rs.getString("profile_picture"));
        user.setEmployeeId(rs.getString("employee_id"));
        user.setDepartment(rs.getString("department"));
        user.setSpecialization(rs.getString("specialization"));
        user.setLicenseNumber(rs.getString("license_number"));
        user.setSalary(rs.getDouble("salary"));
        user.setShift(rs.getString("shift"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        if (rs.getTimestamp("last_login") != null) {
            user.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
        }
        
        user.setSessionToken(rs.getString("session_token"));
        
        return user;
    }
}
