package com.medicore.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import com.medicore.MediCoreApplication;
import com.medicore.model.User;
import com.medicore.service.AuthenticationService;
import com.medicore.util.SessionManager;
import com.medicore.util.ValidationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login screen
 */
public class LoginController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label statusLabel;
    
    private AuthenticationService authService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthenticationService();
        setupUI();
        setupEventHandlers();
        logger.info("Login controller initialized");
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Set default role selection
        roleComboBox.getSelectionModel().selectFirst();
        
        // Set focus to username field
        Platform.runLater(() -> usernameField.requestFocus());
        
        // Setup enter key handling
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Role selection change handler
        roleComboBox.setOnAction(e -> {
            String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
            updateUIForRole(selectedRole);
        });
        
        // Input validation
        usernameField.textProperty().addListener((obs, oldText, newText) -> clearStatus());
        passwordField.textProperty().addListener((obs, oldText, newText) -> clearStatus());
    }
    
    /**
     * Update UI based on selected role
     */
    private void updateUIForRole(String role) {
        // Customize UI based on role if needed
        switch (role) {
            case "Patient":
                usernameField.setPromptText("Enter your patient ID or email");
                break;
            case "Doctor":
                usernameField.setPromptText("Enter your doctor ID or email");
                break;
            case "Admin":
                usernameField.setPromptText("Enter your admin username");
                break;
            default:
                usernameField.setPromptText("Enter your username or email");
                break;
        }
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        if (!validateInput()) {
            return;
        }
        
        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckBox.isSelected();
        
        // Disable login button and show loading
        loginButton.setDisabled(true);
        loginButton.setText("Logging in...");
        showStatus("Authenticating...", false);
        
        // Perform authentication in background thread
        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return authService.authenticate(username, password, selectedRole);
            }
            
            @Override
            protected void succeeded() {
                User user = getValue();
                if (user != null) {
                    handleLoginSuccess(user, rememberMe);
                } else {
                    handleLoginFailure("Invalid credentials");
                }
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                logger.error("Login failed", exception);
                handleLoginFailure("Login failed: " + exception.getMessage());
            }
        };
        
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }
    
    /**
     * Handle successful login
     */
    private void handleLoginSuccess(User user, boolean rememberMe) {
        Platform.runLater(() -> {
            logger.info("User logged in successfully: {} ({})", user.getUsername(), user.getRole());
            
            // Create session
            SessionManager.createSession(user, rememberMe);
            
            // Navigate to appropriate dashboard based on role
            navigateToDashboard(user.getRole());
            
            showStatus("Login successful! Redirecting...", false);
        });
    }
    
    /**
     * Handle login failure
     */
    private void handleLoginFailure(String message) {
        Platform.runLater(() -> {
            logger.warn("Login failed: {}", message);
            showStatus(message, true);
            
            // Re-enable login button
            loginButton.setDisabled(false);
            loginButton.setText("Login");
            
            // Clear password field
            passwordField.clear();
            passwordField.requestFocus();
        });
    }
    
    /**
     * Navigate to appropriate dashboard based on user role
     */
    private void navigateToDashboard(User.Role role) {
        String fxmlPath;
        String title;
        
        switch (role) {
            case ADMIN:
                fxmlPath = "/fxml/admin-dashboard.fxml";
                title = "Admin Dashboard";
                break;
            case DOCTOR:
                fxmlPath = "/fxml/doctor-dashboard.fxml";
                title = "Doctor Dashboard";
                break;
            case PATIENT:
                fxmlPath = "/fxml/patient-dashboard.fxml";
                title = "Patient Dashboard";
                break;
            case NURSE:
                fxmlPath = "/fxml/nurse-dashboard.fxml";
                title = "Nurse Dashboard";
                break;
            case PHARMACIST:
                fxmlPath = "/fxml/pharmacist-dashboard.fxml";
                title = "Pharmacist Dashboard";
                break;
            case SECURITY_GUARD:
                fxmlPath = "/fxml/security-dashboard.fxml";
                title = "Security Dashboard";
                break;
            case AMBULANCE_DRIVER:
                fxmlPath = "/fxml/ambulance-dashboard.fxml";
                title = "Ambulance Dashboard";
                break;
            case CANTEEN_WORKER:
                fxmlPath = "/fxml/canteen-dashboard.fxml";
                title = "Canteen Dashboard";
                break;
            case CLEANER:
                fxmlPath = "/fxml/cleaner-dashboard.fxml";
                title = "Cleaner Dashboard";
                break;
            case RECEPTIONIST:
                fxmlPath = "/fxml/receptionist-dashboard.fxml";
                title = "Receptionist Dashboard";
                break;
            default:
                fxmlPath = "/fxml/general-dashboard.fxml";
                title = "Dashboard";
                break;
        }
        
        MediCoreApplication.navigateToScene(fxmlPath, title);
    }
    
    /**
     * Handle register link click
     */
    @FXML
    private void handleRegister() {
        logger.info("Navigating to registration screen");
        MediCoreApplication.navigateToScene("/fxml/register.fxml", "Register");
    }
    
    /**
     * Handle forgot password link click
     */
    @FXML
    private void handleForgotPassword() {
        logger.info("Navigating to forgot password screen");
        MediCoreApplication.navigateToScene("/fxml/forgot-password.fxml", "Forgot Password");
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        // Check role selection
        if (roleComboBox.getSelectionModel().getSelectedItem() == null) {
            showStatus("Please select your role", true);
            roleComboBox.requestFocus();
            return false;
        }
        
        // Check username
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showStatus("Please enter your username or email", true);
            usernameField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidUsernameOrEmail(username)) {
            showStatus("Please enter a valid username or email", true);
            usernameField.requestFocus();
            return false;
        }
        
        // Check password
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showStatus("Please enter your password", true);
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters long", true);
            passwordField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Show status message
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        
        if (isError) {
            statusLabel.getStyleClass().removeAll("label-success", "label-info");
            statusLabel.getStyleClass().add("label-error");
        } else {
            statusLabel.getStyleClass().removeAll("label-error");
            statusLabel.getStyleClass().add("label-success");
        }
    }
    
    /**
     * Clear status message
     */
    private void clearStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }
}
