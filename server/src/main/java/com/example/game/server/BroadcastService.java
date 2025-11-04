package com.example.game.server;

import com.example.game.model.Message;
import com.example.game.util.LoggerUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for broadcasting game state updates to all connected clients.
 * Uses UDP for fast broadcasting of real-time updates.
 */
public class BroadcastService {
    private final DatagramSocket socket;
    private final Map<String, ClientInfo> clients;
    private final int udpPort;

    public BroadcastService(int udpPort) throws IOException {
        this.udpPort = udpPort;
        this.socket = new DatagramSocket();
        this.clients = new ConcurrentHashMap<>();
        LoggerUtil.info("Broadcast service initialized on UDP port " + udpPort);
    }

    /**
     * Registers a client for broadcasting.
     */
    public void registerClient(String clientId, InetAddress address, int port) {
        clients.put(clientId, new ClientInfo(address, port));
        LoggerUtil.debug("Client registered for broadcast: " + clientId);
    }

    /**
     * Unregisters a client.
     */
    public void unregisterClient(String clientId) {
        clients.remove(clientId);
        LoggerUtil.debug("Client unregistered from broadcast: " + clientId);
    }

    /**
     * Broadcasts a message to all registered clients.
     */
    public void broadcast(Message message) {
        try {
            byte[] data = serializeMessage(message);

            for (ClientInfo client : clients.values()) {
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, client.address, client.port);
                    socket.send(packet);
                } catch (IOException e) {
                    LoggerUtil.error("Failed to send broadcast to client", e);
                }
            }

            LoggerUtil.debug("Broadcasted message to " + clients.size() + " clients");
        } catch (Exception e) {
            LoggerUtil.error("Error broadcasting message", e);
        }
    }

    /**
     * Sends a message to a specific client.
     */
    public void sendToClient(String clientId, Message message) {
        ClientInfo client = clients.get(clientId);
        if (client != null) {
            try {
                byte[] data = serializeMessage(message);
                DatagramPacket packet = new DatagramPacket(data, data.length, client.address, client.port);
                socket.send(packet);
                LoggerUtil.debug("Sent message to client: " + clientId);
            } catch (IOException e) {
                LoggerUtil.error("Failed to send message to client " + clientId, e);
            }
        }
    }

    /**
     * Simple serialization of message (in real implementation, use proper serialization).
     */
    private byte[] serializeMessage(Message message) {
        // Simplified serialization - in practice, use ObjectOutputStream or JSON
        String serialized = message.getType() + "|" + message.getSenderId() + "|" +
                           message.getTimestamp() + "|" + message.getPayload();
        return serialized.getBytes();
    }

    public void close() {
        socket.close();
        LoggerUtil.info("Broadcast service closed");
    }

    private static class ClientInfo {
        final InetAddress address;
        final int port;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }
    }
}