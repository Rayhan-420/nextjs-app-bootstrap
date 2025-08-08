package com.medicore.util;

import com.medicore.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility class for audit logging
 */
public class AuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);
    
    /**
     * Log user action for audit trail
     */
    public static void logUserAction(Long userId, String action, String description, String details) {
        try {
            String sql = """
                INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values, ip_address, user_agent) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = DatabaseService.getConnection().prepareStatement(sql)) {
                pstmt.setLong(1, userId != null ? userId : 0);
                pstmt.setString(2, action);
                pstmt.setString(3, "users"); // Default table
                pstmt.setLong(4, userId != null ? userId : 0);
                pstmt.setString(5, description);
                pstmt.setString(6, details);
                pstmt.setString(7, "127.0.0.1"); // Default IP
                pstmt.setString(8, "JavaFX Application"); // Default user agent
                
                pstmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            logger.error("Failed to log audit entry", e);
        }
    }
}
