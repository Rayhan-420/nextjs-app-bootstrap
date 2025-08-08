package com.mycompany.game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.mycompany.game.ui.MenuScreen;
import com.mycompany.game.network.MultiplayerServer;
import com.mycompany.game.database.DatabaseManager;
import com.mycompany.game.util.ConfigManager;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main application class for JavaFX 3D Multiplayer Game
 * Handles application initialization, server startup, and database connection
 */
public class Main extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private MultiplayerServer server;
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Initialize configuration
            ConfigManager.loadConfig();
            LOGGER.info("Configuration loaded successfully");
            
            // Initialize database connection
            initializeDatabase();
            
            // Create and setup the main menu
            MenuScreen menuScreen = new MenuScreen(this);
            Scene scene = new Scene(menuScreen.getLayout(), 
                ConfigManager.getGameWidth(), 
                ConfigManager.getGameHeight());
            
            // Apply modern styling
            scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
            );
            
            // Configure primary stage
            primaryStage.setTitle(ConfigManager.getGameTitle());
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.DECORATED);
            
            // Center the stage on screen
            primaryStage.centerOnScreen();
            
            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                shutdown();
                Platform.exit();
                System.exit(0);
            });
            
            primaryStage.show();
            LOGGER.info("Application started successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing the application", e);
            showErrorAndExit("Failed to initialize application: " + e.getMessage());
        }
    }
    
    /**
     * Initialize database connection
     */
    private void initializeDatabase() {
        try {
            DatabaseManager.connect(
                ConfigManager.getDbUrl(),
                ConfigManager.getDbUser(),
                ConfigManager.getDbPassword()
            );
            DatabaseManager.initializeTables();
            LOGGER.info("Database connected and initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database connection failed", e);
            // Continue without database - game can still work in offline mode
        }
    }
    
    /**
     * Start the multiplayer server
     */
    public void startServer() {
        if (server == null) {
            try {
                server = new MultiplayerServer(ConfigManager.getServerPort());
                Thread serverThread = new Thread(() -> {
                    server.startServer();
                }, "ServerThread");
                serverThread.setDaemon(true);
                serverThread.start();
                LOGGER.info("Multiplayer server started on port " + ConfigManager.getServerPort());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start server", e);
            }
        }
    }
    
    /**
     * Stop the multiplayer server
     */
    public void stopServer() {
        if (server != null) {
            server.stopServer();
            server = null;
            LOGGER.info("Multiplayer server stopped");
        }
    }
    
    /**
     * Get the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Shutdown all resources
     */
    private void shutdown() {
        try {
            stopServer();
            DatabaseManager.disconnect();
            LOGGER.info("Application shutdown completed");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during shutdown", e);
        }
    }
    
    /**
     * Show error message and exit application
     */
    private void showErrorAndExit(String message) {
        LOGGER.severe(message);
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Application Error");
            alert.setHeaderText("Fatal Error");
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
            System.exit(1);
        });
    }
    
    /**
     * Main method - entry point of the application
     */
    public static void main(String[] args) {
        // Set system properties for better JavaFX performance
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("prism.vsync", "false");
        
        LOGGER.info("Starting JavaFX 3D Multiplayer Game...");
        launch(args);
    }
}
