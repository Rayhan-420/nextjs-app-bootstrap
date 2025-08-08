package com.medicore.service;

import com.medicore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for database operations
 */
public class DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static Connection connection;
    
    /**
     * Initialize database connection
     */
    public static void initialize() throws SQLException {
        try {
            Class.forName(ConfigService.getDbDriver());
            connection = DriverManager.getConnection(
                ConfigService.getDbUrl(),
                ConfigService.getDbUsername(),
                ConfigService.getDbPassword()
            );
            logger.info("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Database driver not found", e);
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            throw e;
        }
    }
    
    /**
     * Create all necessary tables
     */
    public static void createTables() throws SQLException {
        createUsersTable();
        createPatientsTable();
        createDoctorsTable();
        createAppointmentsTable();
        createPrescriptionsTable();
        createMedicinesTable();
        createInventoryTable();
        createBedAllocationsTable();
        createVisitorLogsTable();
        createNotificationsTable();
        createAuditLogsTable();
        
        // Insert default admin user if not exists
        insertDefaultAdminUser();
        
        logger.info("Database tables created successfully");
    }
    
    /**
     * Create users table
     */
    private static void createUsersTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                phone_number VARCHAR(20),
                address TEXT,
                role ENUM('ADMIN', 'DOCTOR', 'PATIENT', 'NURSE', 'PHARMACIST', 
                         'SECURITY_GUARD', 'AMBULANCE_DRIVER', 'CANTEEN_WORKER', 
                         'CLEANER', 'RECEPTIONIST') NOT NULL,
                status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_APPROVAL') DEFAULT 'PENDING_APPROVAL',
                profile_picture VARCHAR(255),
                employee_id VARCHAR(20),
                department VARCHAR(50),
                specialization VARCHAR(100),
                license_number VARCHAR(50),
                salary DECIMAL(10,2),
                shift VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                last_login TIMESTAMP NULL,
                session_token VARCHAR(255)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create patients table for additional patient-specific information
     */
    private static void createPatientsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS patients (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                patient_id VARCHAR(20) UNIQUE NOT NULL,
                date_of_birth DATE,
                gender ENUM('MALE', 'FEMALE', 'OTHER'),
                blood_group VARCHAR(5),
                emergency_contact_name VARCHAR(100),
                emergency_contact_phone VARCHAR(20),
                medical_history TEXT,
                allergies TEXT,
                insurance_number VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create doctors table for additional doctor-specific information
     */
    private static void createDoctorsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS doctors (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                doctor_id VARCHAR(20) UNIQUE NOT NULL,
                specialization VARCHAR(100),
                qualification VARCHAR(255),
                experience_years INT,
                consultation_fee DECIMAL(8,2),
                available_days VARCHAR(50),
                available_time_start TIME,
                available_time_end TIME,
                room_number VARCHAR(10),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create appointments table
     */
    private static void createAppointmentsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS appointments (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                patient_id BIGINT NOT NULL,
                doctor_id BIGINT NOT NULL,
                appointment_date DATE NOT NULL,
                appointment_time TIME NOT NULL,
                status ENUM('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
                reason TEXT,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES users(id),
                FOREIGN KEY (doctor_id) REFERENCES users(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create prescriptions table
     */
    private static void createPrescriptionsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS prescriptions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                patient_id BIGINT NOT NULL,
                doctor_id BIGINT NOT NULL,
                appointment_id BIGINT,
                prescription_date DATE NOT NULL,
                diagnosis TEXT,
                medicines TEXT NOT NULL,
                instructions TEXT,
                follow_up_date DATE,
                status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES users(id),
                FOREIGN KEY (doctor_id) REFERENCES users(id),
                FOREIGN KEY (appointment_id) REFERENCES appointments(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create medicines table
     */
    private static void createMedicinesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS medicines (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                generic_name VARCHAR(100),
                manufacturer VARCHAR(100),
                category VARCHAR(50),
                dosage_form VARCHAR(50),
                strength VARCHAR(50),
                unit_price DECIMAL(8,2),
                description TEXT,
                side_effects TEXT,
                contraindications TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create inventory table
     */
    private static void createInventoryTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS inventory (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                medicine_id BIGINT NOT NULL,
                batch_number VARCHAR(50),
                quantity_in_stock INT NOT NULL DEFAULT 0,
                minimum_stock_level INT DEFAULT 10,
                expiry_date DATE,
                supplier VARCHAR(100),
                purchase_date DATE,
                purchase_price DECIMAL(8,2),
                location VARCHAR(50),
                status ENUM('AVAILABLE', 'LOW_STOCK', 'OUT_OF_STOCK', 'EXPIRED') DEFAULT 'AVAILABLE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (medicine_id) REFERENCES medicines(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create bed allocations table
     */
    private static void createBedAllocationsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS bed_allocations (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                bed_number VARCHAR(10) NOT NULL,
                ward VARCHAR(50) NOT NULL,
                patient_id BIGINT,
                allocation_date DATE,
                discharge_date DATE,
                bed_type ENUM('GENERAL', 'PRIVATE', 'ICU', 'EMERGENCY') DEFAULT 'GENERAL',
                status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'RESERVED') DEFAULT 'AVAILABLE',
                daily_rate DECIMAL(8,2),
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES users(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create visitor logs table
     */
    private static void createVisitorLogsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS visitor_logs (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                visitor_name VARCHAR(100) NOT NULL,
                visitor_phone VARCHAR(20),
                visitor_id_type VARCHAR(20),
                visitor_id_number VARCHAR(50),
                patient_name VARCHAR(100),
                purpose VARCHAR(100),
                entry_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                exit_time TIMESTAMP NULL,
                security_guard_id BIGINT,
                status ENUM('ENTERED', 'EXITED') DEFAULT 'ENTERED',
                notes TEXT,
                FOREIGN KEY (security_guard_id) REFERENCES users(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create notifications table
     */
    private static void createNotificationsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS notifications (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(200) NOT NULL,
                message TEXT NOT NULL,
                type ENUM('INFO', 'WARNING', 'ERROR', 'SUCCESS') DEFAULT 'INFO',
                priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
                is_read BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                read_at TIMESTAMP NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create audit logs table
     */
    private static void createAuditLogsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS audit_logs (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT,
                action VARCHAR(100) NOT NULL,
                table_name VARCHAR(50),
                record_id BIGINT,
                old_values TEXT,
                new_values TEXT,
                ip_address VARCHAR(45),
                user_agent TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Insert default admin user
     */
    private static void insertDefaultAdminUser() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // No admin user exists, create default one
                String insertSql = """
                    INSERT INTO users (username, password, email, first_name, last_name, 
                                     role, status, employee_id, department) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "$2a$10$N9qo8uLOickgx2ZMRZoMye1VdLSnqq3vyPfB3BaLpinIDGiGcpWJy"); // "admin123" hashed
                    pstmt.setString(3, "admin@medicore.com");
                    pstmt.setString(4, "System");
                    pstmt.setString(5, "Administrator");
                    pstmt.setString(6, "ADMIN");
                    pstmt.setString(7, "ACTIVE");
                    pstmt.setString(8, "EMP001");
                    pstmt.setString(9, "Administration");
                    
                    pstmt.executeUpdate();
                    logger.info("Default admin user created successfully");
                }
            }
        }
    }
    
    /**
     * Get database connection
     */
    public static Connection getConnection() {
        return connection;
    }
    
    /**
     * Execute a query and return ResultSet
     */
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
    
    /**
     * Execute an update query
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Close database connection
     */
    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
    
    /**
     * Check if database connection is valid
     */
    public static boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
