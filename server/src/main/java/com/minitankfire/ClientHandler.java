package com.minitankfire;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * Handles individual client connections using multi-threading
 * Each client runs in its own thread, demonstrating concurrent programming
 */
public class ClientHandler implements Runnable {
    private WebSocketHandler webSocket;
    private GameRoom gameRoom;
    private String playerId;
    private volatile boolean running;

    public ClientHandler(Socket socket, GameRoom gameRoom) throws IOException {
        this.webSocket = new WebSocketHandler(socket);
        this.gameRoom = gameRoom;
        this.playerId = webSocket.getClientId();
        this.running = true;
    }

    /**
     * Main client thread execution
     * Demonstrates thread lifecycle and message handling loop
     */
    @Override
    public void run() {
        try {
            // Perform WebSocket handshake
            if (!webSocket.performHandshake()) {
                System.err.println("WebSocket handshake failed for client: " + playerId);
                webSocket.close();
                return;
            }

            System.out.println("Client connected: " + playerId + " from " +
                    webSocket.getSocket().getInetAddress());

            // Message processing loop
            while (running && webSocket.isConnected()) {
                String message = webSocket.readMessage();
                if (message == null) {
                    break; // Connection closed
                }

                handleMessage(message);
            }

        } catch (Exception e) {
            System.err.println("Error handling client " + playerId + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Handles incoming messages from client
     * Demonstrates message parsing and game logic integration
     */
    private void handleMessage(String message) {
        try {
            Map<String, String> data = JsonUtil.parseJson(message);
            String type = data.get("type");

            if (type == null)
                return;

            switch (type) {
                case "join":
                    String name = data.get("name");
                    if (name != null) {
                        System.out
                                .println("[JOIN] Player '" + name + "' joining (ID: " + playerId.substring(0, 8) + ")");
                        gameRoom.addPlayer(playerId, name, this);
                    }
                    break;

                case "move":
                    try {
                        int x = Integer.parseInt(data.get("x"));
                        int y = Integer.parseInt(data.get("y"));
                        int angle = Integer.parseInt(data.get("angle"));
                        gameRoom.handleMove(playerId, x, y, angle);
                    } catch (NumberFormatException e) {
                        // Invalid coordinates, ignore
                    }
                    break;

                case "fire":
                    gameRoom.handleFire(playerId);
                    break;

                case "chat":
                    String msg = data.get("msg");
                    if (msg != null) {
                        gameRoom.handleChat(playerId, msg);
                    }
                    break;

                case "voice-offer":
                case "voice-answer":
                case "voice-ice":
                    // Forward voice chat signaling messages
                    String target = data.get("target");
                    if (target != null) {
                        // Reconstruct message with from field
                        String forwardMsg = message.replace("}", ",\"from\":\"" + playerId + "\"}");
                        gameRoom.sendToPlayer(target, forwardMsg);
                    }
                    break;

                default:
                    System.out.println("Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Error processing message from " + playerId + ": " + e.getMessage());
        }
    }

    /**
     * Sends a message to this client
     */
    public void sendMessage(String message) {
        try {
            webSocket.sendMessage(message);
        } catch (IOException e) {
            System.err.println("Error sending message to " + playerId + ": " + e.getMessage());
            stop();
        }
    }

    /**
     * Stops the client handler
     */
    public void stop() {
        running = false;
    }

    /**
     * Cleanup resources when client disconnects
     */
    private void cleanup() {
        System.out.println("Client disconnected: " + playerId);
        gameRoom.removePlayer(playerId);
        webSocket.close();
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isConnected() {
        return running && webSocket.isConnected();
    }
}
