package com.tankgame.client;

import com.tankgame.network.*;
import com.tankgame.common.Constants;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple Java client for Tank Game
 * This is a text-based client for testing and demonstration
 */
public class TankGameClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private BlockingQueue<String> messageQueue;
    private Thread receiveThread;
    private Thread sendThread;
    private boolean running;
    private String playerName;
    
    public TankGameClient(String host, int port, String playerName) throws IOException {
        this.socket = new Socket(host, port);
        this.playerName = playerName;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = true;
        
        // Setup streams
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        
        System.out.println("Connected to server at " + host + ":" + port);
    }
    
    /**
     * Start the client
     */
    public void start() {
        // Start receive thread
        receiveThread = new Thread(this::receiveLoop, "ClientReceiver");
        receiveThread.start();
        
        // Start send thread
        sendThread = new Thread(this::sendLoop, "ClientSender");
        sendThread.start();
        
        // Send join message
        sendMessage(MessageBuilder.buildJoin(playerName));
        
        System.out.println("Client started. Joined as: " + playerName);
    }
    
    /**
     * Receive messages from server
     */
    private void receiveLoop() {
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send messages to server
     */
    private void sendLoop() {
        try {
            while (running) {
                String message = messageQueue.take();
                writer.println(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Handle incoming message
     */
    private void handleMessage(String message) {
        String[] parts = Protocol.parseMessage(message);
        if (parts.length == 0) return;
        
        String msgType = Protocol.getMessageType(parts);
        
        switch (msgType) {
            case Protocol.MSG_UPDATE:
                handleUpdate(parts);
                break;
                
            case Protocol.MSG_HIT:
                handleHit(parts);
                break;
                
            case Protocol.MSG_KILL:
                handleKill(parts);
                break;
                
            case Protocol.MSG_RESPAWN:
                handleRespawn(parts);
                break;
                
            case Protocol.MSG_POWERUP_COLLECT:
                handlePowerUpCollect(parts);
                break;
                
            case Protocol.MSG_CHAT:
                handleChat(parts);
                break;
                
            case Protocol.MSG_PING:
                sendMessage(MessageBuilder.buildPong());
                break;
                
            default:
                // Unknown message type
                break;
        }
    }
    
    private void handleUpdate(String[] parts) {
        // Parse game state
        String tanksData = Protocol.getField(parts, 1);
        String bulletsData = Protocol.getField(parts, 2);
        String powerUpsData = Protocol.getField(parts, 3);
        
        MessageParser.TankData[] tanks = MessageParser.parseTanks(tanksData);
        MessageParser.BulletData[] bullets = MessageParser.parseBullets(bulletsData);
        MessageParser.PowerUpData[] powerUps = MessageParser.parsePowerUps(powerUpsData);
        
        // Display stats (simple text output)
        System.out.printf("\rPlayers: %d | Bullets: %d | PowerUps: %d", 
            tanks.length, bullets.length, powerUps.length);
    }
    
    private void handleHit(String[] parts) {
        Object[] hitData = MessageParser.parseHit(parts);
        System.out.println("\n[HIT] Tank " + hitData[0] + " hit by " + hitData[1]);
    }
    
    private void handleKill(String[] parts) {
        String[] killData = MessageParser.parseKill(parts);
        System.out.println("\n[KILL] " + killData[0] + " killed " + killData[1]);
    }
    
    private void handleRespawn(String[] parts) {
        Object[] respawnData = MessageParser.parseRespawn(parts);
        System.out.println("\n[RESPAWN] Tank " + respawnData[0] + " respawned");
    }
    
    private void handlePowerUpCollect(String[] parts) {
        Object[] collectData = MessageParser.parsePowerUpCollect(parts);
        System.out.println("\n[POWERUP] Tank " + collectData[1] + " collected power-up");
    }
    
    private void handleChat(String[] parts) {
        String chatMsg = MessageParser.parseChat(parts);
        System.out.println("\n[CHAT] " + chatMsg);
    }
    
    /**
     * Send message to server
     */
    public void sendMessage(String message) {
        messageQueue.offer(message);
    }
    
    /**
     * Move tank
     */
    public void move(int x, int y, int angle) {
        sendMessage(MessageBuilder.buildMove(x, y, angle));
    }
    
    /**
     * Fire weapon
     */
    public void fire(int angle) {
        sendMessage(MessageBuilder.buildFire(angle));
    }
    
    /**
     * Send chat message
     */
    public void chat(String message) {
        sendMessage(MessageBuilder.buildChat(message));
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        running = false;
        
        try {
            if (socket != null) socket.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        
        if (receiveThread != null) {
            try {
                receiveThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (sendThread != null) {
            try {
                sendThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("\nDisconnected from server");
    }
    
    /**
     * Main entry point for testing
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = Constants.SERVER_PORT;
        String playerName = "TestPlayer";
        
        // Parse arguments
        if (args.length > 0) host = args[0];
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[1]);
                return;
            }
        }
        if (args.length > 2) playerName = args[2];
        
        try {
            TankGameClient client = new TankGameClient(host, port, playerName);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down...");
                client.disconnect();
            }));
            
            client.start();
            
            // Simple test: send some commands
            System.out.println("\nRunning simple test...");
            Thread.sleep(1000);
            
            // Move around
            client.move(100, 100, 0);
            Thread.sleep(100);
            client.move(200, 100, 45);
            Thread.sleep(100);
            
            // Fire
            client.fire(45);
            Thread.sleep(100);
            
            // Chat
            client.chat("Hello from Java client!");
            
            // Keep running
            System.out.println("\nClient running. Press Ctrl+C to exit.");
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}