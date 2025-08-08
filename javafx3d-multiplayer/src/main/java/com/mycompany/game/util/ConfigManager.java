package com.mycompany.game.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Configuration manager for loading and accessing application settings
 */
public class ConfigManager {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static Properties properties = new Properties();
    private static boolean loaded = false;
    
    /**
     * Load configuration from properties file
     */
    public static void loadConfig() throws IOException {
        if (loaded) return;
        
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            
            properties.load(input);
            loaded = true;
            LOGGER.info("Configuration loaded successfully");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load configuration", e);
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
            LOGGER.warning("Invalid integer value for property: " + key);
            return defaultValue;
        }
    }
    
    /**
     * Get double property value with default fallback
     */
    private static double getDoubleProperty(String key, double defaultValue) {
        try {
            return Double.parseDouble(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid double value for property: " + key);
            return defaultValue;
        }
    }
    
    // Database Configuration
    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:mysql://localhost:3306/game_db");
    }
    
    public static String getDbUser() {
        return getProperty("db.user", "root");
    }
    
    public static String getDbPassword() {
        return getProperty("db.password", "password");
    }
    
    public static String getDbDriver() {
        return getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
    
    // Server Configuration
    public static int getServerPort() {
        return getIntProperty("server.port", 5555);
    }
    
    public static int getMaxClients() {
        return getIntProperty("server.max_clients", 50);
    }
    
    // Game Configuration
    public static String getGameTitle() {
        return getProperty("game.title", "JavaFX 3D Multiplayer Game");
    }
    
    public static int getGameWidth() {
        return getIntProperty("game.width", 1280);
    }
    
    public static int getGameHeight() {
        return getIntProperty("game.height", 720);
    }
    
    public static int getGameFPS() {
        return getIntProperty("game.fps", 60);
    }
    
    // 3D Scene Configuration
    public static double getCameraDistance() {
        return getDoubleProperty("scene.camera.distance", 1000.0);
    }
    
    public static double getAnimationSpeed() {
        return getDoubleProperty("scene.animation.speed", 90.0);
    }
    
    public static double getAmbientLighting() {
        return getDoubleProperty("scene.lighting.ambient", 0.3);
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
}
