package com.tankgame.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.tankgame.network.*;

/**
 * Handles communication with a single client
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private String clientId;
    private GameEngine gameEngine;
    private NetworkManager networkManager;
    private BufferedReader reader;
    private PrintWriter writer;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BlockingQueue<String> messageQueue;
    private Thread sendThread;
    private boolean running;
    private boolean isWebSocket;
    
    public ClientHandler(Socket socket, String clientId, GameEngine gameEngine, 
                        NetworkManager networkManager) {
        this.socket = socket;
        this.clientId = clientId;
        this.gameEngine = gameEngine;
        this.networkManager = networkManager;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.isWebSocket = false;
    }
    
    /**
     * Start handling client
     */
    @Override
    public void run() {
        try {
            // Setup streams
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            writer = new PrintWriter(outputStream, true);
            
            // Check if this is a WebSocket connection
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("GET")) {
                // This is a WebSocket handshake
                isWebSocket = true;
                System.out.println("[ClientHandler] WebSocket connection from " + 
                                 socket.getInetAddress().getHostAddress());
                
                if (!WebSocketHandler.performHandshake(reader, writer)) {
                    System.err.println("[ClientHandler] WebSocket handshake failed");
                    return;
                }
                
                System.out.println("[ClientHandler] WebSocket handshake successful: " + clientId);
            } else {
                // Raw TCP connection, process the first line as a message
                if (firstLine != null && !firstLine.isEmpty()) {
                    processMessage(firstLine);
                }
            }
            
            // Start send thread
            sendThread = new Thread(this::sendLoop, "ClientSender-" + clientId);
            sendThread.start();
            
            System.out.println("[ClientHandler] Client connected: " + clientId + 
                             " from " + socket.getInetAddress().getHostAddress() +
                             " (WebSocket: " + isWebSocket + ")");
            
            // Receive loop
            receiveLoop();
            
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error handling client " + clientId + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    /**
     * Receive messages from client
     */
    private void receiveLoop() {
        try {
            String message;
            if (isWebSocket) {
                // Read WebSocket frames
                while (running) {
                    message = WebSocketHandler.readFrame(inputStream);
                    if (message == null) break;
                    if (message.isEmpty()) continue; // Skip pings and control frames
                    processMessage(message);
                }
            } else {
                // Read raw TCP lines
                while (running && (message = reader.readLine()) != null) {
                    processMessage(message);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[ClientHandler] Connection lost with " + clientId);
            }
        }
    }
    
    /**
     * Send messages to client
     */
    private void sendLoop() {
        try {
            while (running) {
                String message = messageQueue.take(); // Blocking wait
                
                if (isWebSocket) {
                    // Send as WebSocket frame
                    WebSocketHandler.writeFrame(outputStream, message);
                } else {
                    // Send as raw TCP line
                    writer.println(message);
                    if (writer.checkError()) {
                        System.err.println("[ClientHandler] Error sending to " + clientId);
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (running) {
                System.err.println("[ClientHandler] Error sending to " + clientId);
            }
        }
    }
    
    /**
     * Process incoming message
     */
    private void processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        String[] parts = Protocol.parseMessage(message);
        if (parts.length == 0) {
            return;
        }
        
        String msgType = Protocol.getMessageType(parts);
        
        try {
            switch (msgType) {
                case Protocol.MSG_JOIN:
                    handleJoin(parts);
                    break;
                    
                case Protocol.MSG_MOVE:
                    handleMove(parts);
                    break;
                    
                case Protocol.MSG_FIRE:
                    handleFire(parts);
                    break;
                    
                case Protocol.MSG_CHAT:
                    handleChat(parts);
                    break;
                    
                case Protocol.MSG_PING:
                    sendMessage(MessageBuilder.buildPong());
                    break;
                    
                case Protocol.MSG_VOICE_OFFER:
                    handleVoiceOffer(parts);
                    break;
                    
                case Protocol.MSG_VOICE_ANSWER:
                    handleVoiceAnswer(parts);
                    break;
                    
                case Protocol.MSG_VOICE_ICE:
                    handleVoiceIce(parts);
                    break;
                    
                default:
                    System.out.println("[ClientHandler] Unknown message type: " + msgType);
                    break;
            }
        } catch (Exception e) {
            System.err.println("[ClientHandler] Error processing message from " + 
                             clientId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleJoin(String[] parts) {
        String playerName = MessageParser.parseJoin(parts);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player" + clientId.substring(0, 4);
        }
        
        gameEngine.addPlayer(clientId, playerName);
        System.out.println("[ClientHandler] Player " + playerName + " joined as " + clientId);
    }
    
    private void handleMove(String[] parts) {
        int[] moveData = MessageParser.parseMove(parts);
        gameEngine.handleMove(clientId, moveData[0], moveData[1], moveData[2]);
    }
    
    private void handleFire(String[] parts) {
        int angle = MessageParser.parseFire(parts);
        gameEngine.handleFire(clientId, angle);
    }
    
    private void handleChat(String[] parts) {
        String chatMessage = MessageParser.parseChat(parts);
        if (chatMessage != null && !chatMessage.trim().isEmpty()) {
            gameEngine.handleChat(clientId, chatMessage);
        }
    }
    
    private void handleVoiceOffer(String[] parts) {
        // VOICE_OFFER|fromId|targetId|offerJson
        if (parts.length >= 4) {
            String fromId = parts[1];
            String targetId = parts[2];
            String offerJson = parts[3];
            networkManager.sendToClient(targetId, MessageBuilder.buildVoiceOffer(fromId, targetId, offerJson));
        }
    }
    
    private void handleVoiceAnswer(String[] parts) {
        // VOICE_ANSWER|fromId|targetId|answerJson
        if (parts.length >= 4) {
            String fromId = parts[1];
            String targetId = parts[2];
            String answerJson = parts[3];
            networkManager.sendToClient(targetId, MessageBuilder.buildVoiceAnswer(fromId, targetId, answerJson));
        }
    }
    
    private void handleVoiceIce(String[] parts) {
        // VOICE_ICE|fromId|targetId|candidateJson
        if (parts.length >= 4) {
            String fromId = parts[1];
            String targetId = parts[2];
            String candidateJson = parts[3];
            networkManager.sendToClient(targetId, MessageBuilder.buildVoiceIce(fromId, targetId, candidateJson));
        }
    }
    
    /**
     * Send message to this client (non-blocking)
     */
    public void sendMessage(String message) {
        if (running) {
            messageQueue.offer(message);
        }
    }
    
    /**
     * Close connection
     */
    public void close() {
        running = false;
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error closing socket: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        running = false;
        
        // Remove player from game
        gameEngine.removePlayer(clientId);
        
        // Remove from network manager
        networkManager.removeClient(clientId);
        
        // Close streams
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore
        }
        
        // Wait for send thread
        if (sendThread != null) {
            try {
                sendThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("[ClientHandler] Client disconnected: " + clientId);
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public boolean isRunning() {
        return running;
    }
}
