package com.mycompany.game.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.mycompany.game.Main;
import com.mycompany.game.game.GameScene;
import com.mycompany.game.network.GameClient;
import com.mycompany.game.util.ConfigManager;

import java.util.logging.Logger;

/**
 * Multiplayer menu for hosting and joining games
 */
public class MultiplayerMenu {
    
    private static final Logger LOGGER = Logger.getLogger(MultiplayerMenu.class.getName());
    private BorderPane layout;
    private Main mainApp;
    private MenuScreen menuScreen;
    private VBox contentContainer;
    private Label statusLabel;
    
    public MultiplayerMenu(Main mainApp, MenuScreen menuScreen) {
        this.mainApp = mainApp;
        this.menuScreen = menuScreen;
        initializeLayout();
        setupAnimations();
    }
    
    /**
     * Initialize the multiplayer menu layout
     */
    private void initializeLayout() {
        layout = new BorderPane();
        layout.getStyleClass().add("root");
        
        // Create title
        Label titleLabel = new Label("Multiplayer");
        titleLabel.getStyleClass().add("game-title");
        titleLabel.setStyle("-fx-font-size: 36px;");
        
        // Create content container
        contentContainer = new VBox(20);
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(40));
        contentContainer.getStyleClass().add("menu-container");
        contentContainer.setMaxWidth(500);
        
        // Create host game section
        VBox hostSection = createHostSection();
        
        // Create join game section
        VBox joinSection = createJoinSection();
        
        // Create navigation buttons
        HBox navigationBox = createNavigationButtons();
        
        // Status label
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().addAll("status-connected", "hud-element");
        
        // Add all components
        contentContainer.getChildren().addAll(
            titleLabel,
            hostSection,
            new Separator(),
            joinSection,
            statusLabel,
            navigationBox
        );
        
        layout.setCenter(contentContainer);
        
        LOGGER.info("Multiplayer menu initialized");
    }
    
    /**
     * Create host game section
     */
    private VBox createHostSection() {
        VBox hostSection = new VBox(15);
        hostSection.setAlignment(Pos.CENTER);
        
        Label hostLabel = new Label("Host Game");
        hostLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Server port input
        HBox portBox = new HBox(10);
        portBox.setAlignment(Pos.CENTER);
        Label portLabel = new Label("Port:");
        TextField portField = new TextField(String.valueOf(ConfigManager.getServerPort()));
        portField.getStyleClass().add("modern-text-field");
        portField.setPrefWidth(100);
        portBox.getChildren().addAll(portLabel, portField);
        
        // Max players input
        HBox playersBox = new HBox(10);
        playersBox.setAlignment(Pos.CENTER);
        Label playersLabel = new Label("Max Players:");
        TextField playersField = new TextField(String.valueOf(ConfigManager.getMaxClients()));
        playersField.getStyleClass().add("modern-text-field");
        playersField.setPrefWidth(100);
        playersBox.getChildren().addAll(playersLabel, playersField);
        
        // Host button
        Button hostButton = new Button("Start Server");
        hostButton.getStyleClass().add("menu-button");
        hostButton.setOnAction(e -> hostGame(
            Integer.parseInt(portField.getText()),
            Integer.parseInt(playersField.getText())
        ));
        
        hostSection.getChildren().addAll(hostLabel, portBox, playersBox, hostButton);
        return hostSection;
    }
    
    /**
     * Create join game section
     */
    private VBox createJoinSection() {
        VBox joinSection = new VBox(15);
        joinSection.setAlignment(Pos.CENTER);
        
        Label joinLabel = new Label("Join Game");
        joinLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Server address input
        HBox addressBox = new HBox(10);
        addressBox.setAlignment(Pos.CENTER);
        Label addressLabel = new Label("Server Address:");
        TextField addressField = new TextField("localhost");
        addressField.getStyleClass().add("modern-text-field");
        addressField.setPrefWidth(150);
        addressBox.getChildren().addAll(addressLabel, addressField);
        
        // Server port input
        HBox portBox = new HBox(10);
        portBox.setAlignment(Pos.CENTER);
        Label portLabel = new Label("Port:");
        TextField portField = new TextField(String.valueOf(ConfigManager.getServerPort()));
        portField.getStyleClass().add("modern-text-field");
        portField.setPrefWidth(100);
        portBox.getChildren().addAll(portLabel, portField);
        
        // Player name input
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Player Name:");
        TextField nameField = new TextField("Player" + (int)(Math.random() * 1000));
        nameField.getStyleClass().add("modern-text-field");
        nameField.setPrefWidth(150);
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        // Join button
        Button joinButton = new Button("Connect to Server");
        joinButton.getStyleClass().add("menu-button");
        joinButton.setOnAction(e -> joinGame(
            addressField.getText(),
            Integer.parseInt(portField.getText()),
            nameField.getText()
        ));
        
        joinSection.getChildren().addAll(joinLabel, addressBox, portBox, nameBox, joinButton);
        return joinSection;
    }
    
    /**
     * Create navigation buttons
     */
    private HBox createNavigationButtons() {
        HBox navigationBox = new HBox(20);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().add("menu-button");
        backButton.setOnAction(e -> goBackToMenu());
        
        navigationBox.getChildren().add(backButton);
        return navigationBox;
    }
    
    /**
     * Setup entrance animations
     */
    private void setupAnimations() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), contentContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Host a multiplayer game
     */
    private void hostGame(int port, int maxPlayers) {
        LOGGER.info("Hosting game on port " + port + " with max " + maxPlayers + " players");
        
        try {
            updateStatus("Starting server...", "status-connecting");
            
            // Update configuration
            ConfigManager.setProperty("server.port", String.valueOf(port));
            ConfigManager.setProperty("server.max_clients", String.valueOf(maxPlayers));
            
            // Start the server
            mainApp.startServer();
            
            updateStatus("Server started successfully", "status-connected");
            
            // Start the game as host
            startMultiplayerGame(true);
            
        } catch (Exception e) {
            LOGGER.severe("Failed to host game: " + e.getMessage());
            updateStatus("Failed to start server", "status-disconnected");
            showError("Server Error", "Failed to start server: " + e.getMessage());
        }
    }
    
    /**
     * Join a multiplayer game
     */
    private void joinGame(String address, int port, String playerName) {
        LOGGER.info("Joining game at " + address + ":" + port + " as " + playerName);
        
        try {
            updateStatus("Connecting to server...", "status-connecting");
            
            // Create game client
            GameClient client = new GameClient(address, port, playerName);
            
            // Attempt to connect
            if (client.connect()) {
                updateStatus("Connected successfully", "status-connected");
                startMultiplayerGame(false, client);
            } else {
                updateStatus("Connection failed", "status-disconnected");
                showError("Connection Error", "Failed to connect to server");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Failed to join game: " + e.getMessage());
            updateStatus("Connection failed", "status-disconnected");
            showError("Connection Error", "Failed to connect: " + e.getMessage());
        }
    }
    
    /**
     * Start multiplayer game
     */
    private void startMultiplayerGame(boolean isHost) {
        startMultiplayerGame(isHost, null);
    }
    
    private void startMultiplayerGame(boolean isHost, GameClient client) {
        try {
            GameScene gameScene = new GameScene(
                ConfigManager.getGameWidth(),
                ConfigManager.getGameHeight()
            );
            
            // Configure for multiplayer
            gameScene.setMultiplayerMode(true, isHost, client);
            
            Scene scene = gameScene.getScene();
            scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
            );
            
            mainApp.getPrimaryStage().setScene(scene);
            gameScene.startGame();
            
        } catch (Exception e) {
            LOGGER.severe("Failed to start multiplayer game: " + e.getMessage());
            showError("Game Error", "Failed to start game: " + e.getMessage());
        }
    }
    
    /**
     * Go back to main menu
     */
    private void goBackToMenu() {
        LOGGER.info("Returning to main menu");
        Scene scene = new Scene(menuScreen.getLayout(), 
            ConfigManager.getGameWidth(), 
            ConfigManager.getGameHeight());
        scene.getStylesheets().add(
            getClass().getResource("/style.css").toExternalForm()
        );
        mainApp.getPrimaryStage().setScene(scene);
    }
    
    /**
     * Update status label
     */
    private void updateStatus(String message, String styleClass) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-connected", "status-disconnected", "status-connecting");
        statusLabel.getStyleClass().add(styleClass);
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
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
