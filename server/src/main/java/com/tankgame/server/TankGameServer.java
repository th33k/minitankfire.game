package com.tankgame.server;

import com.tankgame.common.Constants;
import com.tankgame.common.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main server class for Tank Game
 * Pure Java Socket-based multiplayer server
 */
public class TankGameServer {
    private ServerSocket serverSocket;
    private NetworkManager networkManager;
    private GameEngine gameEngine;
    private ExecutorService clientExecutor;
    private boolean running;
    
    public TankGameServer() {
        this.networkManager = new NetworkManager();
        this.gameEngine = new GameEngine(networkManager);
        this.clientExecutor = Executors.newCachedThreadPool();
        this.running = false;
    }
    
    /**
     * Start the server
     */
    public void start(int port) throws IOException {
        if (running) {
            System.out.println("[Server] Already running");
            return;
        }
        
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(Constants.SERVER_HOST));
        running = true;
        
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║         Tank Game Server - Pure Java Edition          ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("[Server] Starting on port " + port);
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[Server] Server IP: " + ip);
        } catch (Exception e) {
            System.out.println("[Server] Could not determine server IP");
        }
        System.out.println("[Server] Max players: " + Constants.MAX_PLAYERS);
        System.out.println("[Server] Map size: " + Constants.MAP_WIDTH + "x" + Constants.MAP_HEIGHT);
        System.out.println("[Server] Tick rate: " + (1000 / Constants.GAME_TICK_RATE) + " Hz");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // Start game engine
        gameEngine.start();
        
        // Accept client connections
        System.out.println("[Server] Waiting for connections...");
        acceptClients();
    }
    
    /**
     * Accept incoming client connections
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Check max players
                if (networkManager.getClientCount() >= Constants.MAX_PLAYERS) {
                    System.out.println("[Server] Server full, rejecting connection from " + 
                                     clientSocket.getInetAddress().getHostAddress());
                    clientSocket.close();
                    continue;
                }
                
                // Generate client ID
                String clientId = Utils.generateId();
                
                // Create and start client handler
                ClientHandler handler = new ClientHandler(
                    clientSocket, clientId, gameEngine, networkManager);
                
                networkManager.addClient(clientId, handler);
                clientExecutor.submit(handler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("[Server] Error accepting client: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        System.out.println("[Server] Shutting down...");
        running = false;
        
        // Stop accepting new connections
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[Server] Error closing server socket: " + e.getMessage());
        }
        
        // Disconnect all clients
        networkManager.disconnectAll();
        
        // Stop game engine
        gameEngine.stop();
        
        // Shutdown executor
        clientExecutor.shutdown();
        try {
            if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                clientExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[Server] Server stopped");
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        int port = Constants.SERVER_PORT;
        
        // Parse command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.err.println("Usage: java TankGameServer [port]");
                System.exit(1);
            }
        }
        
        final TankGameServer server = new TankGameServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Shutdown signal received");
            server.stop();
        }));
        
        try {
            server.start(port);
        } catch (IOException e) {
            System.err.println("[Server] Failed to start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
