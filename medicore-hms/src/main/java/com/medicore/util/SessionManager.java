package com.medicore.util;

import com.medicore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages user sessions and authentication state
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private static User currentUser;
    private static String currentSessionToken;
    private static LocalDateTime sessionStartTime;
    private static boolean rememberMe = false;
    
    // Session timeout in minutes
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    // Active sessions map (for multi-user support in future)
    private static final ConcurrentHashMap<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    // Scheduled executor for session cleanup
    private static final ScheduledExecutorService sessionCleanupExecutor = 
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SessionCleanup");
            t.setDaemon(true);
            return t;
        });
    
    static {
        // Start session cleanup task
        sessionCleanupExecutor.scheduleAtFixedRate(
            SessionManager::cleanupExpiredSessions, 
            5, 5, TimeUnit.MINUTES
        );
    }
    
    /**
     * Create a new session for the user
     */
    public static void createSession(User user, boolean rememberMe) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        currentUser = user;
        currentSessionToken = user.getSessionToken();
        sessionStartTime = LocalDateTime.now();
        SessionManager.rememberMe = rememberMe;
        
        // Add to active sessions
        SessionInfo sessionInfo = new SessionInfo(user, LocalDateTime.now(), rememberMe);
        activeSessions.put(currentSessionToken, sessionInfo);
        
        logger.info("Session created for user: {} ({})", user.getUsername(), user.getRole());
        
        // Log session creation for audit
        AuditLogger.logUserAction(user.getId(), "SESSION_CREATED", "User logged in", null);
    }
    
    /**
     * Get current logged-in user
     */
    public static User getCurrentUser() {
        if (isSessionValid()) {
            return currentUser;
        }
        return null;
    }
    
    /**
     * Get current session token
     */
    public static String getCurrentSessionToken() {
        return currentSessionToken;
    }
    
    /**
     * Check if current session is valid
     */
    public static boolean isSessionValid() {
        if (currentUser == null || currentSessionToken == null || sessionStartTime == null) {
            return false;
        }
        
        // Check if session has expired
        if (!rememberMe) {
            LocalDateTime expiryTime = sessionStartTime.plusMinutes(SESSION_TIMEOUT_MINUTES);
            if (LocalDateTime.now().isAfter(expiryTime)) {
                logger.info("Session expired for user: {}", currentUser.getUsername());
                clearSession();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Refresh session (extend timeout)
     */
    public static void refreshSession() {
        if (currentUser != null && !rememberMe) {
            sessionStartTime = LocalDateTime.now();
            
            // Update session info in active sessions
            SessionInfo sessionInfo = activeSessions.get(currentSessionToken);
            if (sessionInfo != null) {
                sessionInfo.setLastActivity(LocalDateTime.now());
            }
            
            logger.debug("Session refreshed for user: {}", currentUser.getUsername());
        }
    }
    
    /**
     * Clear current session
     */
    public static void clearSession() {
        if (currentUser != null) {
            logger.info("Session cleared for user: {}", currentUser.getUsername());
            
            // Log session end for audit
            AuditLogger.logUserAction(currentUser.getId(), "SESSION_ENDED", "User logged out", null);
            
            // Remove from active sessions
            if (currentSessionToken != null) {
                activeSessions.remove(currentSessionToken);
            }
        }
        
        currentUser = null;
        currentSessionToken = null;
        sessionStartTime = null;
        rememberMe = false;
    }
    
    /**
     * Check if user has specific role
     */
    public static boolean hasRole(User.Role role) {
        User user = getCurrentUser();
        return user != null && user.hasRole(role);
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public static boolean hasAnyRole(User.Role... roles) {
        User user = getCurrentUser();
        if (user == null) return false;
        
        for (User.Role role : roles) {
            if (user.hasRole(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole(User.Role.ADMIN);
    }
    
    /**
     * Check if current user is doctor
     */
    public static boolean isDoctor() {
        return hasRole(User.Role.DOCTOR);
    }
    
    /**
     * Check if current user is patient
     */
    public static boolean isPatient() {
        return hasRole(User.Role.PATIENT);
    }
    
    /**
     * Check if current user is staff (not patient)
     */
    public static boolean isStaff() {
        User user = getCurrentUser();
        return user != null && user.isStaff();
    }
    
    /**
     * Get session duration in minutes
     */
    public static long getSessionDurationMinutes() {
        if (sessionStartTime == null) return 0;
        
        return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Get remaining session time in minutes (for non-remember-me sessions)
     */
    public static long getRemainingSessionMinutes() {
        if (rememberMe || sessionStartTime == null) return -1; // Unlimited for remember-me
        
        LocalDateTime expiryTime = sessionStartTime.plusMinutes(SESSION_TIMEOUT_MINUTES);
        long remaining = java.time.Duration.between(LocalDateTime.now(), expiryTime).toMinutes();
        return Math.max(0, remaining);
    }
    
    /**
     * Check if session will expire soon (within 5 minutes)
     */
    public static boolean isSessionExpiringSoon() {
        if (rememberMe) return false;
        
        long remaining = getRemainingSessionMinutes();
        return remaining > 0 && remaining <= 5;
    }
    
    /**
     * Get all active sessions (for admin monitoring)
     */
    public static ConcurrentHashMap<String, SessionInfo> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
    
    /**
     * Force logout a specific session (admin function)
     */
    public static boolean forceLogout(String sessionToken) {
        SessionInfo sessionInfo = activeSessions.remove(sessionToken);
        if (sessionInfo != null) {
            logger.info("Session forcefully terminated: {}", sessionInfo.getUser().getUsername());
            
            // If it's the current session, clear it
            if (sessionToken.equals(currentSessionToken)) {
                clearSession();
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Clean up expired sessions
     */
    private static void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        
        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo sessionInfo = entry.getValue();
            
            // Skip remember-me sessions
            if (sessionInfo.isRememberMe()) {
                return false;
            }
            
            // Check if session has expired
            LocalDateTime expiryTime = sessionInfo.getLastActivity().plusMinutes(SESSION_TIMEOUT_MINUTES);
            if (now.isAfter(expiryTime)) {
                logger.info("Cleaning up expired session for user: {}", 
                    sessionInfo.getUser().getUsername());
                return true;
            }
            
            return false;
        });
    }
    
    /**
     * Shutdown session manager
     */
    public static void shutdown() {
        sessionCleanupExecutor.shutdown();
        activeSessions.clear();
        clearSession();
        logger.info("Session manager shutdown completed");
    }
    
    /**
     * Session information holder
     */
    public static class SessionInfo {
        private final User user;
        private final LocalDateTime createdAt;
        private LocalDateTime lastActivity;
        private final boolean rememberMe;
        
        public SessionInfo(User user, LocalDateTime createdAt, boolean rememberMe) {
            this.user = user;
            this.createdAt = createdAt;
            this.lastActivity = createdAt;
            this.rememberMe = rememberMe;
        }
        
        public User getUser() {
            return user;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public LocalDateTime getLastActivity() {
            return lastActivity;
        }
        
        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }
        
        public boolean isRememberMe() {
            return rememberMe;
        }
        
        public long getDurationMinutes() {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        }
        
        public boolean isExpired() {
            if (rememberMe) return false;
            
            LocalDateTime expiryTime = lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES);
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
