package com.tankgame.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages network communication and message broadcasting
 */
public class NetworkManager {
    private Map<String, ClientHandler> clients;
    
    public NetworkManager() {
        this.clients = new ConcurrentHashMap<>();
    }
    
    /**
     * Register a client handler
     */
    public void addClient(String clientId, ClientHandler handler) {
        clients.put(clientId, handler);
        System.out.println("[NetworkManager] Client registered: " + clientId + 
                         " (Total clients: " + clients.size() + ")");
    }
    
    /**
     * Unregister a client handler
     */
    public void removeClient(String clientId) {
        clients.remove(clientId);
        System.out.println("[NetworkManager] Client unregistered: " + clientId + 
                         " (Total clients: " + clients.size() + ")");
    }
    
    /**
     * Broadcast message to all clients
     */
    public void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }
    
    /**
     * Broadcast message to all clients except one
     */
    public void broadcastExcept(String message, String excludeClientId) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludeClientId)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
    
    /**
     * Send message to specific client
     */
    public void sendToClient(String clientId, String message) {
        ClientHandler client = clients.get(clientId);
        if (client != null) {
            client.sendMessage(message);
        }
    }
    
    /**
     * Get number of connected clients
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Check if client is connected
     */
    public boolean hasClient(String clientId) {
        return clients.containsKey(clientId);
    }
    
    /**
     * Disconnect all clients
     */
    public void disconnectAll() {
        System.out.println("[NetworkManager] Disconnecting all clients...");
        for (ClientHandler client : clients.values()) {
            client.close();
        }
        clients.clear();
    }
    
    /**
     * Clean up disconnected clients
     */
    public void cleanupDisconnected() {
        clients.entrySet().removeIf(entry -> !entry.getValue().isRunning());
    }
}
