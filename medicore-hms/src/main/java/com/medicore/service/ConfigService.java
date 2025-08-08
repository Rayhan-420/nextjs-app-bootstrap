package com.medicore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Service for managing application configuration
 */
public class ConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private static Properties properties = new Properties();
    private static boolean initialized = false;
    
    /**
     * Initialize configuration from properties file
     */
    public static void initialize() throws IOException {
        if (initialized) return;
        
        try (InputStream input = ConfigService.class.getClassLoader()
                .getResourceAsStream("config/application.properties")) {
            
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            
            properties.load(input);
            initialized = true;
            logger.info("Configuration loaded successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
            throw e;
        }
    }
    
    /**
     * Get property value with default fallback
     */
    private static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get integer property value with default fallback
     */
    private static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property: {}", key);
            return defaultValue;
        }
    }
    
    /**
     * Get boolean property value with default fallback
     */
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(key, String.valueOf(defaultValue)));
    }
    
    // Database Configuration
    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/medicore_hms");
    }
    
    public static String getDbUsername() {
        return getProperty("db.username", "root");
    }
    
    public static String getDbPassword() {
        return getProperty("db.password", "password");
    }
    
    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    public static int getDbPoolSize() {
        return getIntProperty("db.pool.size", 10);
    }
    
    // Server Configuration
    public static int getServerPort() {
        return getIntProperty("server.port", 8080);
    }
    
    public static int getSocketPort() {
        return getIntProperty("server.socket.port", 9090);
    }
    
    public static int getMaxConnections() {
        return getIntProperty("server.max.connections", 100);
    }
    
    // Application Configuration
    public static String getAppName() {
        return getProperty("app.name", "mediCore Hospital Management System");
    }
    
    public static String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }
    
    public static String getAppTheme() {
        return getProperty("app.theme", "light");
    }
    
    public static int getSessionTimeout() {
        return getIntProperty("app.session.timeout", 30);
    }
    
    // Security Configuration
    public static int getPasswordMinLength() {
        return getIntProperty("security.password.min.length", 8);
    }
    
    public static String getSessionKey() {
        return getProperty("security.session.key", "medicore_session_2024");
    }
    
    public static String getEncryptionAlgorithm() {
        return getProperty("security.encryption.algorithm", "AES");
    }
    
    // File Upload Configuration
    public static String getFileUploadPath() {
        return getProperty("file.upload.path", "uploads/");
    }
    
    public static String getFileMaxSize() {
        return getProperty("file.max.size", "10MB");
    }
    
    public static String getAllowedFileTypes() {
        return getProperty("file.allowed.types", "pdf,jpg,jpeg,png,doc,docx");
    }
    
    // Email Configuration
    public static String getEmailSmtpHost() {
        return getProperty("email.smtp.host", "smtp.gmail.com");
    }
    
    public static int getEmailSmtpPort() {
        return getIntProperty("email.smtp.port", 587);
    }
    
    public static String getEmailUsername() {
        return getProperty("email.username", "medicore@hospital.com");
    }
    
    public static String getEmailPassword() {
        return getProperty("email.password", "your_email_password");
    }
    
    public static String getEmailFrom() {
        return getProperty("email.from", "medicore@hospital.com");
    }
    
    // PDF Configuration
    public static String getPdfTemplatePath() {
        return getProperty("pdf.template.path", "templates/");
    }
    
    public static String getPdfOutputPath() {
        return getProperty("pdf.output.path", "reports/");
    }
    
    // Notification Configuration
    public static boolean isNotificationEnabled() {
        return getBooleanProperty("notification.enabled", true);
    }
    
    public static boolean isNotificationSoundEnabled() {
        return getBooleanProperty("notification.sound.enabled", true);
    }
    
    public static int getNotificationPopupDuration() {
        return getIntProperty("notification.popup.duration", 5000);
    }
    
    // Logging Configuration
    public static String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }
    
    public static String getLoggingFilePath() {
        return getProperty("logging.file.path", "logs/");
    }
    
    public static String getLoggingFileMaxSize() {
        return getProperty("logging.file.max.size", "10MB");
    }
    
    /**
     * Update a property value at runtime
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Get all properties for debugging
     */
    public static Properties getAllProperties() {
        return new Properties(properties);
    }
    
    /**
     * Check if configuration is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
