package com.medicore.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Server for real-time notifications and communication
 */
public class NotificationServer {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServer.class);
    
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService clientThreadPool;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // Connected clients map (session token -> client handler)
    private final ConcurrentHashMap<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    
    public NotificationServer(int port) {
        this.port = port;
        this.clientThreadPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "NotificationClient");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Start the notification server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running.set(true);
            
            logger.info("Notification server started on port {}", port);
            
            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientThreadPool.submit(clientHandler);
                    
                    logger.debug("New client connected: {}", clientSocket.getRemoteSocketAddress());
                    
                } catch (IOException e) {
                    if (running.get()) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to start notification server", e);
        } finally {
            stop();
        }
    }
    
    /**
     * Stop the notification server
     */
    public void stop() {
        running.set(false);
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        
        // Disconnect all clients
        connectedClients.values().forEach(ClientHandler::disconnect);
        connectedClients.clear();
        
        // Shutdown thread pool
        clientThreadPool.shutdown();
        
        logger.info("Notification server stopped");
    }
    
    /**
     * Register a client handler
     */
    public void registerClient(String sessionToken, ClientHandler clientHandler) {
        connectedClients.put(sessionToken, clientHandler);
        logger.info("Client registered with session: {}", sessionToken);
    }
    
    /**
     * Unregister a client handler
     */
    public void unregisterClient(String sessionToken) {
        ClientHandler removed = connectedClients.remove(sessionToken);
        if (removed != null) {
            logger.info("Client unregistered with session: {}", sessionToken);
        }
    }
    
    /**
     * Send notification to a specific user
     */
    public boolean sendNotificationToUser(String sessionToken, String message) {
        ClientHandler clientHandler = connectedClients.get(sessionToken);
        if (clientHandler != null) {
            return clientHandler.sendMessage(message);
        }
        
        logger.warn("No connected client found for session: {}", sessionToken);
        return false;
    }
    
    /**
     * Broadcast notification to all connected clients
     */
    public void broadcastNotification(String message) {
        logger.info("Broadcasting notification to {} clients", connectedClients.size());
        
        connectedClients.values().forEach(clientHandler -> {
            if (!clientHandler.sendMessage(message)) {
                logger.warn("Failed to send broadcast message to client");
            }
        });
    }
    
    /**
     * Broadcast notification to clients with specific roles
     */
    public void broadcastToRole(String role, String message) {
        logger.info("Broadcasting notification to role: {}", role);
        
        connectedClients.values().forEach(clientHandler -> {
            if (role.equals(clientHandler.getUserRole())) {
                clientHandler.sendMessage(message);
            }
        });
    }
    
    /**
     * Get number of connected clients
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Get connected clients info (for monitoring)
     */
    public ConcurrentHashMap<String, ClientHandler> getConnectedClients() {
        return new ConcurrentHashMap<>(connectedClients);
    }
}
