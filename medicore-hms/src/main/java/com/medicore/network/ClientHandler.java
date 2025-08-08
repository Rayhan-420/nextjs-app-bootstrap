package com.medicore.network;

import com.medicore.model.User;
import com.medicore.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 * Handles individual client connections for the notification server
 */
public class ClientHandler implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    
    private final Socket clientSocket;
    private final NotificationServer server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String sessionToken;
    private User user;
    private boolean connected = true;
    private final LocalDateTime connectedAt;
    
    public ClientHandler(Socket clientSocket, NotificationServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.connectedAt = LocalDateTime.now();
        
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            logger.error("Failed to initialize client handler", e);
            disconnect();
        }
    }
    
    @Override
    public void run() {
        try {
            // First message should be authentication with session token
            String authMessage = reader.readLine();
            if (authMessage != null && authMessage.startsWith("AUTH:")) {
                sessionToken = authMessage.substring(5);
                
                // Validate session token
                AuthenticationService authService = new AuthenticationService();
                user = authService.validateSession(sessionToken);
                
                if (user != null) {
                    server.registerClient(sessionToken, this);
                    sendMessage("AUTH_SUCCESS:Welcome " + user.getFullName());
                    logger.info("Client authenticated: {} ({})", user.getUsername(), user.getRole());
                } else {
                    sendMessage("AUTH_FAILED:Invalid session token");
                    logger.warn("Authentication failed for session: {}", sessionToken);
                    disconnect();
                    return;
                }
            } else {
                sendMessage("AUTH_REQUIRED:Please authenticate first");
                disconnect();
                return;
            }
            
            // Handle incoming messages
            String message;
            while (connected && (message = reader.readLine()) != null) {
                handleMessage(message);
            }
            
        } catch (IOException e) {
            if (connected) {
                logger.error("Error handling client communication", e);
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Handle incoming message from client
     */
    private void handleMessage(String message) {
        logger.debug("Received message from {}: {}", user.getUsername(), message);
        
        try {
            if (message.startsWith("PING")) {
                sendMessage("PONG");
            } else if (message.startsWith("CHAT:")) {
                handleChatMessage(message.substring(5));
            } else if (message.startsWith("STATUS:")) {
                handleStatusUpdate(message.substring(7));
            } else if (message.startsWith("EMERGENCY:")) {
                handleEmergencyAlert(message.substring(10));
            } else {
                logger.warn("Unknown message type from {}: {}", user.getUsername(), message);
            }
        } catch (Exception e) {
            logger.error("Error processing message from {}", user.getUsername(), e);
        }
    }
    
    /**
     * Handle chat message
     */
    private void handleChatMessage(String chatMessage) {
        // Parse chat message format: "TO:username:message"
        String[] parts = chatMessage.split(":", 3);
        if (parts.length == 3) {
            String targetUser = parts[1];
            String message = parts[2];
            
            // Find target user's session and send message
            // This would require a user-to-session mapping
            logger.info("Chat message from {} to {}: {}", user.getUsername(), targetUser, message);
            
            // For now, just acknowledge receipt
            sendMessage("CHAT_SENT:Message sent to " + targetUser);
        }
    }
    
    /**
     * Handle status update
     */
    private void handleStatusUpdate(String status) {
        logger.info("Status update from {}: {}", user.getUsername(), status);
        
        // Update user status in database or cache
        // Broadcast status to relevant users (e.g., same department)
        
        sendMessage("STATUS_UPDATED:Status updated successfully");
    }
    
    /**
     * Handle emergency alert
     */
    private void handleEmergencyAlert(String alert) {
        logger.warn("EMERGENCY ALERT from {}: {}", user.getUsername(), alert);
        
        // Broadcast emergency alert to all relevant staff
        String emergencyMessage = "EMERGENCY:" + user.getFullName() + " - " + alert;
        
        // Broadcast to security, admin, and medical staff
        server.broadcastToRole("ADMIN", emergencyMessage);
        server.broadcastToRole("SECURITY_GUARD", emergencyMessage);
        server.broadcastToRole("DOCTOR", emergencyMessage);
        server.broadcastToRole("NURSE", emergencyMessage);
        
        sendMessage("EMERGENCY_SENT:Emergency alert broadcasted");
    }
    
    /**
     * Send message to client
     */
    public boolean sendMessage(String message) {
        if (!connected || writer == null) {
            return false;
        }
        
        try {
            writer.println(message);
            return !writer.checkError();
        } catch (Exception e) {
            logger.error("Failed to send message to client", e);
            return false;
        }
    }
    
    /**
     * Send notification to client
     */
    public boolean sendNotification(String title, String message, String type) {
        String notification = String.format("NOTIFICATION:%s:%s:%s", type, title, message);
        return sendMessage(notification);
    }
    
    /**
     * Send system alert to client
     */
    public boolean sendAlert(String alert) {
        return sendMessage("ALERT:" + alert);
    }
    
    /**
     * Disconnect client
     */
    public void disconnect() {
        connected = false;
        
        if (sessionToken != null) {
            server.unregisterClient(sessionToken);
        }
        
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing client connection", e);
        }
        
        if (user != null) {
            logger.info("Client disconnected: {}", user.getUsername());
        }
    }
    
    /**
     * Check if client is connected
     */
    public boolean isConnected() {
        return connected && !clientSocket.isClosed();
    }
    
    /**
     * Get user associated with this client
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Get user role
     */
    public String getUserRole() {
        return user != null ? user.getRole().name() : null;
    }
    
    /**
     * Get session token
     */
    public String getSessionToken() {
        return sessionToken;
    }
    
    /**
     * Get connection time
     */
    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }
    
    /**
     * Get client address
     */
    public String getClientAddress() {
        return clientSocket.getRemoteSocketAddress().toString();
    }
    
    /**
     * Get connection duration in minutes
     */
    public long getConnectionDurationMinutes() {
        return java.time.Duration.between(connectedAt, LocalDateTime.now()).toMinutes();
    }
}
