package com.medicore.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.medicore.MediCoreApplication;
import com.medicore.model.User;
import com.medicore.service.DatabaseService;
import com.medicore.util.SessionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for the Admin Dashboard
 */
public class AdminDashboardController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    // Navigation and User Controls
    @FXML private Button notificationButton;
    @FXML private MenuButton userMenuButton;
    
    // Statistics Labels
    @FXML private Text totalPatientsLabel;
    @FXML private Text activeStaffLabel;
    @FXML private Text availableBedsLabel;
    @FXML private Text revenueTodayLabel;
    
    // Charts
    @FXML private LineChart<String, Number> admissionsChart;
    @FXML private PieChart departmentChart;
    
    // Lists and Containers
    @FXML private ListView<String> recentActivitiesList;
    @FXML private VBox alertsContainer;
    
    // Status Bar
    @FXML private Label connectedUsersLabel;
    @FXML private Label serverStatusLabel;
    @FXML private Label currentTimeLabel;
    
    private Timer clockTimer;
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getCurrentUser();
        
        if (currentUser == null || !currentUser.isAdmin()) {
            logger.error("Unauthorized access to admin dashboard");
            MediCoreApplication.showError("Access Denied", "You don't have permission to access this page");
            handleLogout();
            return;
        }
        
        setupUI();
        loadDashboardData();
        startClockTimer();
        
        logger.info("Admin dashboard initialized for user: {}", currentUser.getUsername());
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Set user name in menu button
        userMenuButton.setText(currentUser.getFullName());
        
        // Setup charts
        setupCharts();
        
        // Setup recent activities list
        recentActivitiesList.setItems(FXCollections.observableArrayList());
    }
    
    /**
     * Setup charts with sample data
     */
    private void setupCharts() {
        // Setup admissions chart
        XYChart.Series<String, Number> admissionsSeries = new XYChart.Series<>();
        admissionsSeries.setName("Daily Admissions");
        
        admissionsSeries.getData().add(new XYChart.Data<>("Mon", 25));
        admissionsSeries.getData().add(new XYChart.Data<>("Tue", 32));
        admissionsSeries.getData().add(new XYChart.Data<>("Wed", 28));
        admissionsSeries.getData().add(new XYChart.Data<>("Thu", 35));
        admissionsSeries.getData().add(new XYChart.Data<>("Fri", 42));
        admissionsSeries.getData().add(new XYChart.Data<>("Sat", 38));
        admissionsSeries.getData().add(new XYChart.Data<>("Sun", 30));
        
        admissionsChart.getData().add(admissionsSeries);
        
        // Setup department chart
        ObservableList<PieChart.Data> departmentData = FXCollections.observableArrayList(
            new PieChart.Data("Emergency", 25),
            new PieChart.Data("Cardiology", 20),
            new PieChart.Data("Orthopedics", 15),
            new PieChart.Data("Pediatrics", 18),
            new PieChart.Data("General Medicine", 22)
        );
        departmentChart.setData(departmentData);
    }
    
    /**
     * Load dashboard data from database
     */
    private void loadDashboardData() {
        try {
            loadStatistics();
            loadRecentActivities();
            updateSystemStatus();
        } catch (Exception e) {
            logger.error("Failed to load dashboard data", e);
            MediCoreApplication.showError("Data Load Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    /**
     * Load statistics from database
     */
    private void loadStatistics() throws SQLException {
        // Load total patients
        String patientCountSql = "SELECT COUNT(*) FROM users WHERE role = 'PATIENT'";
        try (ResultSet rs = DatabaseService.executeQuery(patientCountSql)) {
            if (rs.next()) {
                totalPatientsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }
        
        // Load active staff
        String staffCountSql = "SELECT COUNT(*) FROM users WHERE role != 'PATIENT' AND status = 'ACTIVE'";
        try (ResultSet rs = DatabaseService.executeQuery(staffCountSql)) {
            if (rs.next()) {
                activeStaffLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }
        
        // Load available beds
        String bedCountSql = "SELECT COUNT(*) FROM bed_allocations WHERE status = 'AVAILABLE'";
        try (ResultSet rs = DatabaseService.executeQuery(bedCountSql)) {
            if (rs.next()) {
                availableBedsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }
        
        // Set sample revenue (would be calculated from actual billing data)
        revenueTodayLabel.setText("$45,678");
    }
    
    /**
     * Load recent activities
     */
    private void loadRecentActivities() throws SQLException {
        ObservableList<String> activities = FXCollections.observableArrayList();
        
        String sql = """
            SELECT u.first_name, u.last_name, al.action, al.created_at 
            FROM audit_logs al 
            JOIN users u ON al.user_id = u.id 
            ORDER BY al.created_at DESC 
            LIMIT 10
        """;
        
        try (ResultSet rs = DatabaseService.executeQuery(sql)) {
            while (rs.next()) {
                String activity = String.format("%s %s - %s (%s)",
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("action"),
                    rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                );
                activities.add(activity);
            }
        }
        
        recentActivitiesList.setItems(activities);
    }
    
    /**
     * Update system status information
     */
    private void updateSystemStatus() {
        // Update connected users count (would get from session manager)
        connectedUsersLabel.setText("Connected Users: " + SessionManager.getActiveSessions().size());
        
        // Update server status
        serverStatusLabel.setText("Server: Running");
    }
    
    /**
     * Start clock timer for status bar
     */
    private void startClockTimer() {
        clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    currentTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                });
            }
        }, 0, 1000);
    }
    
    // Navigation Event Handlers
    
    @FXML
    private void handleDashboard() {
        // Already on dashboard
        logger.info("Dashboard navigation clicked");
    }
    
    @FXML
    private void handleUserManagement() {
        logger.info("Navigating to user management");
        MediCoreApplication.navigateToScene("/fxml/user-management.fxml", "User Management");
    }
    
    @FXML
    private void handleStaffManagement() {
        logger.info("Navigating to staff management");
        MediCoreApplication.navigateToScene("/fxml/staff-management.fxml", "Staff Management");
    }
    
    @FXML
    private void handleShiftManagement() {
        logger.info("Navigating to shift management");
        MediCoreApplication.navigateToScene("/fxml/shift-management.fxml", "Shift Management");
    }
    
    @FXML
    private void handleReports() {
        logger.info("Navigating to reports");
        MediCoreApplication.navigateToScene("/fxml/reports.fxml", "Reports & Analytics");
    }
    
    @FXML
    private void handleInventory() {
        logger.info("Navigating to inventory");
        MediCoreApplication.navigateToScene("/fxml/inventory.fxml", "Inventory Management");
    }
    
    @FXML
    private void handleBedManagement() {
        logger.info("Navigating to bed management");
        MediCoreApplication.navigateToScene("/fxml/bed-management.fxml", "Bed Management");
    }
    
    @FXML
    private void handleEmergencyAlerts() {
        logger.info("Navigating to emergency alerts");
        MediCoreApplication.navigateToScene("/fxml/emergency-alerts.fxml", "Emergency Alerts");
    }
    
    @FXML
    private void handleSystemSettings() {
        logger.info("Navigating to system settings");
        MediCoreApplication.navigateToScene("/fxml/system-settings.fxml", "System Settings");
    }
    
    @FXML
    private void handleAuditLogs() {
        logger.info("Navigating to audit logs");
        MediCoreApplication.navigateToScene("/fxml/audit-logs.fxml", "Audit Logs");
    }
    
    // Top Bar Event Handlers
    
    @FXML
    private void handleNotifications() {
        logger.info("Notifications clicked");
        MediCoreApplication.showSuccess("Notifications", "No new notifications");
    }
    
    @FXML
    private void handleProfile() {
        logger.info("Profile clicked");
        MediCoreApplication.navigateToScene("/fxml/profile.fxml", "Profile");
    }
    
    @FXML
    private void handleSettings() {
        logger.info("Settings clicked");
        MediCoreApplication.navigateToScene("/fxml/settings.fxml", "Settings");
    }
    
    @FXML
    private void handleLogout() {
        logger.info("Logout clicked");
        
        if (MediCoreApplication.showConfirmation("Logout", "Are you sure you want to logout?")) {
            // Stop timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }
            
            // Clear session
            SessionManager.clearSession();
            
            // Navigate to login
            MediCoreApplication.navigateToScene("/fxml/login.fxml", "Login");
        }
    }
    
    // Quick Action Event Handlers
    
    @FXML
    private void handleAddUser() {
        logger.info("Add user clicked");
        MediCoreApplication.navigateToScene("/fxml/add-user.fxml", "Add New User");
    }
    
    @FXML
    private void handleGenerateReport() {
        logger.info("Generate report clicked");
        MediCoreApplication.showSuccess("Report", "Report generation started. You will be notified when complete.");
    }
    
    @FXML
    private void handleSendBroadcast() {
        logger.info("Send broadcast clicked");
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Send Broadcast");
        dialog.setHeaderText("Send Broadcast Message");
        dialog.setContentText("Enter your message:");
        
        dialog.showAndWait().ifPresent(message -> {
            if (!message.trim().isEmpty()) {
                // Here you would send the broadcast through the notification server
                MediCoreApplication.showSuccess("Broadcast", "Message broadcasted to all users");
                logger.info("Broadcast message sent: {}", message);
            }
        });
    }
    
    @FXML
    private void handleEmergencyAlert() {
        logger.info("Emergency alert clicked");
        
        if (MediCoreApplication.showConfirmation("Emergency Alert", 
                "Are you sure you want to send an emergency alert to all staff?")) {
            
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Emergency Alert");
            dialog.setHeaderText("Emergency Alert Message");
            dialog.setContentText("Enter emergency details:");
            
            dialog.showAndWait().ifPresent(message -> {
                if (!message.trim().isEmpty()) {
                    // Here you would send the emergency alert
                    MediCoreApplication.showSuccess("Emergency Alert", "Emergency alert sent to all staff");
                    logger.warn("Emergency alert sent: {}", message);
                }
            });
        }
    }
    
    /**
     * Cleanup resources when controller is destroyed
     */
    public void cleanup() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
    }
}
