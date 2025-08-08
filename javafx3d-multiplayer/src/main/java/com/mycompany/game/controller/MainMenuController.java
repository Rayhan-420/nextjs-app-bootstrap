package com.mycompany.game.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import com.mycompany.game.Main;
import com.mycompany.game.game.GameScene;
import com.mycompany.game.util.ConfigManager;

import java.util.logging.Logger;

/**
 * Controller for the main menu FXML
 */
public class MainMenuController {
    
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    
    @FXML
    private BorderPane root;
    
    @FXML
    private Button startGameButton;
    
    @FXML
    private Button multiplayerButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button exitButton;
    
    private Main mainApp;
    
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
    
    @FXML
    private void initialize() {
        // Initialize button actions
        startGameButton.setOnAction(e -> handleStartGame());
        multiplayerButton.setOnAction(e -> handleMultiplayer());
        settingsButton.setOnAction(e -> handleSettings());
        exitButton.setOnAction(e -> handleExit());
    }
    
    @FXML
    private void handleStartGame() {
        LOGGER.info("Starting single player game");
        // Implement game start logic
    }
    
    @FXML
    private void handleMultiplayer() {
        LOGGER.info("Opening multiplayer menu");
        // Implement multiplayer logic
    }
    
    @FXML
    private void handleSettings() {
        LOGGER.info("Opening settings");
        // Implement settings logic
    }
    
    @FXML
    private void handleExit() {
        LOGGER.info("Exiting application");
        System.exit(0);
    }
}
