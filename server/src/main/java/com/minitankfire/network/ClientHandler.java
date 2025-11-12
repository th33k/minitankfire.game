package com.minitankfire.network;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import com.minitankfire.game.GameRoom;
import com.minitankfire.util.JsonUtil;

/**
 * Handles individual client connections.
 * Each client runs in its own thread, managing incoming messages and updates.
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
     * Main client thread execution.
     * Performs WebSocket handshake and processes incoming messages.
     */
    @Override
    public void run() {
        try {
            // Perform WebSocket handshake
            if (!webSocket.performHandshake()) {
                System.err.println("[HANDSHAKE] Failed for client: " + playerId);
                webSocket.close();
                return;
            }

            System.out.println("[CONNECTED] Client: " + playerId.substring(0, 8) +
                    " from " + webSocket.getSocket().getInetAddress());

            // Message processing loop
            while (running && webSocket.isConnected()) {
                String message = webSocket.readMessage();
                if (message == null) {
                    break; // Connection closed
                }

                handleMessage(message);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Client " + playerId.substring(0, 8) + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Handles incoming messages from the client.
     * Routes messages to appropriate game logic handlers.
     */
    private void handleMessage(String message) {
        try {
            Map<String, String> data = JsonUtil.parseJson(message);
            String type = data.get("type");

            if (type == null)
                return;

            switch (type) {
                case "lobby_info":
                    handleLobbyInfo();
                    break;

                case "join":
                    handleJoin(data);
                    break;

                case "move":
                    handleMove(data);
                    break;

                case "fire":
                    gameRoom.handleFire(playerId, data);
                    break;

                case "chat":
                    handleChat(data);
                    break;

                case "voice-offer":
                case "voice-answer":
                case "voice-ice":
                    forwardVoiceSignal(data, message);
                    break;

                default:
                    System.out.println("[UNKNOWN] Message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("[MESSAGE_ERROR] " + playerId.substring(0, 8) + ": " + e.getMessage());
        }
    }

    private void handleJoin(Map<String, String> data) {
        String name = data.get("name");
        if (name != null) {
            System.out.println("[JOIN] Player '" + name + "' (ID: " + playerId.substring(0, 8) + ")");
            gameRoom.addPlayer(playerId, name, this);
        }
    }

    private void handleLobbyInfo() {
        String lobbyInfo = gameRoom.getLobbyInfo();
        sendMessage(lobbyInfo);
    }

    private void handleMove(Map<String, String> data) {
        try {
            int x = Integer.parseInt(data.get("x"));
            int y = Integer.parseInt(data.get("y"));
            int angle = Integer.parseInt(data.get("angle"));
            gameRoom.handleMove(playerId, x, y, angle);
        } catch (NumberFormatException e) {
            // Invalid coordinates, ignore
        }
    }

    private void handleChat(Map<String, String> data) {
        String msg = data.get("msg");
        if (msg != null) {
            gameRoom.handleChat(playerId, msg);
        }
    }

    private void forwardVoiceSignal(Map<String, String> data, String message) {
        String target = data.get("target");
        if (target != null) {
            // Reconstruct message with from field
            String forwardMsg = message.replace("}", ",\"from\":\"" + playerId + "\"}");
            gameRoom.sendToPlayer(target, forwardMsg);
        }
    }

    /**
     * Sends a message to this client
     */
    public void sendMessage(String message) {
        try {
            webSocket.sendMessage(message);
        } catch (IOException e) {
            System.err.println("[SEND_ERROR] " + playerId.substring(0, 8) + ": " + e.getMessage());
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
        System.out.println("[DISCONNECTED] Client: " + playerId.substring(0, 8));
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
