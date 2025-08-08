package com.medicore;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.medicore.service.DatabaseService;
import com.medicore.service.ConfigService;
import com.medicore.network.NotificationServer;
import com.medicore.util.SessionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main Application class for mediCore Hospital Management System
 * Handles application initialization, database setup, and server startup
 */
public class MediCoreApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(MediCoreApplication.class);
    private static Stage primaryStage;
    private NotificationServer notificationServer;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        try {
            // Initialize configuration
            ConfigService.initialize();
            logger.info("Configuration loaded successfully");
            
            // Initialize database
            initializeDatabase();
            
            // Start notification server
            startNotificationServer();
            
            // Load login screen
            showLoginScreen();
            
            // Configure primary stage
            configurePrimaryStage();
            
            logger.info("mediCore Hospital Management System started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }
    
    /**
     * Initialize database connection and create tables
     */
    private void initializeDatabase() {
        try {
            DatabaseService.initialize();
            DatabaseService.createTables();
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Start the notification server for real-time communication
     */
    private void startNotificationServer() {
        try {
            notificationServer = new NotificationServer(ConfigService.getSocketPort());
            Thread serverThread = new Thread(notificationServer::start, "NotificationServer");
            serverThread.setDaemon(true);
            serverThread.start();
            logger.info("Notification server started on port {}", ConfigService.getSocketPort());
        } catch (Exception e) {
            logger.warn("Failed to start notification server", e);
            // Continue without notification server - not critical for basic functionality
        }
    }
    
    /**
     * Show the login screen
     */
    private void showLoginScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        
        // Apply CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/main-style.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Configure the primary stage properties
     */
    private void configurePrimaryStage() {
        primaryStage.setTitle("mediCore - Hospital Management System");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.setMaximized(true);
        
        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        } catch (Exception e) {
            logger.warn("Could not load application icon", e);
        }
        
        // Handle application close
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Application closing...");
            shutdown();
            Platform.exit();
            System.exit(0);
        });
        
        // Center the stage
        primaryStage.centerOnScreen();
    }
    
    /**
     * Navigate to a new scene
     */
    public static void navigateToScene(String fxmlPath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MediCoreApplication.class.getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Apply CSS styling
            scene.getStylesheets().add(MediCoreApplication.class.getResource("/css/main-style.css").toExternalForm());
            
            primaryStage.setScene(scene);
            if (title != null) {
                primaryStage.setTitle("mediCore - " + title);
            }
            
            logger.info("Navigated to scene: {}", fxmlPath);
            
        } catch (IOException e) {
            logger.error("Failed to navigate to scene: {}", fxmlPath, e);
            showError("Navigation Error", "Failed to load screen: " + e.getMessage());
        }
    }
    
    /**
     * Show error dialog and exit application
     */
    private void showErrorAndExit(String message) {
        logger.error("Fatal error: {}", message);
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Fatal Error");
            alert.setHeaderText("Application Error");
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
            System.exit(1);
        });
    }
    
    /**
     * Show error dialog
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle(title);
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Show success dialog
     */
    public static void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
            );
            alert.setTitle(title);
            alert.setHeaderText("Success");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        alert.setTitle(title);
        alert.setHeaderText("Confirmation");
        alert.setContentText(message);
        
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }
    
    /**
     * Get the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Shutdown all resources
     */
    private void shutdown() {
        try {
            // Stop notification server
            if (notificationServer != null) {
                notificationServer.stop();
            }
            
            // Clear session
            SessionManager.clearSession();
            
            // Close database connections
            DatabaseService.shutdown();
            
            logger.info("Application shutdown completed");
            
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }
    }
    
    /**
     * Main method - entry point of the application
     */
    public static void main(String[] args) {
        // Set system properties for better JavaFX performance
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("prism.vsync", "false");
        System.setProperty("prism.lcdtext", "false");
        
        logger.info("Starting mediCore Hospital Management System...");
        launch(args);
    }
}
