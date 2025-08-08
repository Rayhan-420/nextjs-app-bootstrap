package com.mycompany.game.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import com.mycompany.game.Main;
import com.mycompany.game.game.GameScene;
import com.mycompany.game.util.ConfigManager;

import java.util.logging.Logger;

/**
 * Modern main menu screen with smooth animations and professional styling
 */
public class MenuScreen {
    
    private static final Logger LOGGER = Logger.getLogger(MenuScreen.class.getName());
    private BorderPane layout;
    private Main mainApp;
    private VBox menuContainer;
    
    public MenuScreen(Main mainApp) {
        this.mainApp = mainApp;
        initializeLayout();
        setupAnimations();
    }
    
    /**
     * Initialize the menu layout with modern styling
     */
    private void initializeLayout() {
        layout = new BorderPane();
        layout.getStyleClass().add("root");
        
        // Create title
        Label titleLabel = new Label(ConfigManager.getGameTitle());
        titleLabel.getStyleClass().add("game-title");
        
        // Create menu container
        menuContainer = new VBox(20);
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setPadding(new Insets(40));
        menuContainer.getStyleClass().add("menu-container");
        menuContainer.setMaxWidth(400);
        
        // Create menu buttons
        Button startGameBtn = createMenuButton("Start Game", this::startGame);
        Button multiplayerBtn = createMenuButton("Multiplayer", this::showMultiplayer);
        Button settingsBtn = createMenuButton("Settings", this::showSettings);
        Button exitBtn = createMenuButton("Exit", this::exitGame);
        
        // Add buttons to container
        menuContainer.getChildren().addAll(
            titleLabel,
            startGameBtn,
            multiplayerBtn,
            settingsBtn,
            exitBtn
        );
        
        // Create status bar
        HBox statusBar = createStatusBar();
        
        // Layout assembly
        layout.setCenter(menuContainer);
        layout.setBottom(statusBar);
        
        LOGGER.info("Menu screen initialized");
    }
    
    /**
     * Create a styled menu button with hover effects
     */
    private Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        
        // Add click action
        button.setOnAction(e -> {
            playButtonClickAnimation(button);
            action.run();
        });
        
        // Add hover animations
        button.setOnMouseEntered(e -> playButtonHoverAnimation(button, true));
        button.setOnMouseExited(e -> playButtonHoverAnimation(button, false));
        
        return button;
    }
    
    /**
     * Create status bar with connection and server information
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        Label versionLabel = new Label("Version 1.0.0");
        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-connected");
        
        statusBar.getChildren().addAll(versionLabel, statusLabel);
        
        return statusBar;
    }
    
    /**
     * Setup entrance animations for menu elements
     */
    private void setupAnimations() {
        // Fade in animation for the entire menu
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), menuContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        // Slide up animation
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(800), menuContainer);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        
        // Play animations
        fadeIn.play();
        slideUp.play();
    }
    
    /**
     * Play button click animation
     */
    private void playButtonClickAnimation(Button button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        
        scaleDown.setOnFinished(e -> scaleUp.play());
        scaleDown.play();
    }
    
    /**
     * Play button hover animation
     */
    private void playButtonHoverAnimation(Button button, boolean hover) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
        if (hover) {
            scale.setToX(1.05);
            scale.setToY(1.05);
        } else {
            scale.setToX(1.0);
            scale.setToY(1.0);
        }
        scale.play();
    }
    
    /**
     * Start single player game
     */
    private void startGame() {
        LOGGER.info("Starting single player game");
        try {
            GameScene gameScene = new GameScene(
                ConfigManager.getGameWidth(),
                ConfigManager.getGameHeight()
            );
            
            Scene scene = gameScene.getScene();
            scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
            );
            
            mainApp.getPrimaryStage().setScene(scene);
            gameScene.startGame();
            
        } catch (Exception e) {
            LOGGER.severe("Failed to start game: " + e.getMessage());
            showError("Failed to start game", e.getMessage());
        }
    }
    
    /**
     * Show multiplayer options
     */
    private void showMultiplayer() {
        LOGGER.info("Opening multiplayer menu");
        try {
            MultiplayerMenu multiplayerMenu = new MultiplayerMenu(mainApp, this);
            Scene scene = new Scene(multiplayerMenu.getLayout(), 
                ConfigManager.getGameWidth(), 
                ConfigManager.getGameHeight());
            scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
            );
            mainApp.getPrimaryStage().setScene(scene);
        } catch (Exception e) {
            LOGGER.severe("Failed to open multiplayer menu: " + e.getMessage());
            showError("Multiplayer Error", e.getMessage());
        }
    }
    
    /**
     * Show settings menu
     */
    private void showSettings() {
        LOGGER.info("Opening settings menu");
        // TODO: Implement settings menu
        showInfo("Settings", "Settings menu coming soon!");
    }
    
    /**
     * Exit the game
     */
    private void exitGame() {
        LOGGER.info("Exiting game");
        mainApp.getPrimaryStage().close();
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Get the layout for this screen
     */
    public BorderPane getLayout() {
        return layout;
    }
}
