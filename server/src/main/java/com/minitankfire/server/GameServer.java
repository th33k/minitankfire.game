package com.minitankfire.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.minitankfire.game.GameRoom;
import com.minitankfire.network.ClientHandler;

/**
 * Tank Game Server - Main server entry point.
 * 
 * Manages:
 * - TCP ServerSocket listening for WebSocket connections
 * - Thread pool for concurrent client handling
 * - Game room initialization and lifecycle
 * 
 * Pure Java implementation using only core APIs:
 * - java.net.ServerSocket (TCP server)
 * - java.net.Socket (client connections)
 * - java.util.concurrent (threading)
 */
public class GameServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_CLIENTS = 100;

    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private GameRoom gameRoom;
    private volatile boolean running;
    private int winningScore = 10;

    public void setWinningScore(int winningScore) {
        this.winningScore = winningScore;
        System.out.println("[CONFIG] Winning score set to: " + winningScore);
        if (this.gameRoom != null) {
            this.gameRoom.setWinningScore(winningScore);
        }
    }

    public GameServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.gameRoom = new GameRoom();
        this.running = true;

        printBanner(port);
    }

    /**
     * Prints welcome banner with server information
     */
    private void printBanner(int port) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║      🎮 Tank Game Server - Pure Java Network Programming   ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  Architecture:                                             ║");
        System.out.println("║  ✓ Multi-threaded Client Handling                          ║");
        System.out.println("║  ✓ WebSocket Protocol (RFC 6455)                           ║");
        System.out.println("║  ✓ Real-time Game Loop (20 FPS)                            ║");
        System.out.println("║  ✓ Concurrent State Management                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  Server Address: 0.0.0.0:" + String.format("%-43s", port) + "║");
        System.out.println("║  WebSocket URI: ws://localhost:" + String.format("%-37s", port + "/game") + "║");
        System.out.println("║  Max Clients: " + String.format("%-48s", MAX_CLIENTS) + "║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Main server loop - accepts and handles client connections
     */
    public void start() {
        System.out.println("\n[SERVER] Waiting for connections...\n");

        while (running) {
            try {
                // Accept incoming connection (blocking call)
                Socket clientSocket = serverSocket.accept();

                // Configure socket options
                clientSocket.setTcpNoDelay(true); // Disable Nagle's algorithm for real-time game
                clientSocket.setSoTimeout(0); // No read timeout

                String clientAddr = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                System.out.println("[ACCEPT] New client from: " + clientAddr);

                // Create client handler and submit to thread pool
                ClientHandler handler = new ClientHandler(clientSocket, gameRoom);
                clientThreadPool.execute(handler);

            } catch (IOException e) {
                if (running) {
                    System.err.println("[ERROR] Accept failed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gracefully shuts down the server
     */
    public void shutdown() {
        System.out.println("\n[SHUTDOWN] Server shutting down...");
        running = false;

        try {
            // Stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Stop game room
            gameRoom.stop();

            // Shutdown thread pool gracefully
            clientThreadPool.shutdown();
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("[SHUTDOWN] Force terminating remaining threads...");
                clientThreadPool.shutdownNow();
            }

            System.out.println("[SHUTDOWN] Server stopped successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Shutdown error: " + e.getMessage());
        }
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Allow custom port via command line argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] Invalid port, using default: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        int winningScore = 10;

        // Winning score via second arg or prompt
        if (args.length > 1) {
            try {
                winningScore = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] Invalid winning score argument, using default: " + winningScore);
            }
        } else {
            try {
                System.out.print("Enter winning score (default " + winningScore + "): ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String line = br.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    try {
                        winningScore = Integer.parseInt(line.trim());
                    } catch (NumberFormatException e) {
                        System.err.println("[ERROR] Invalid number entered, using default: " + winningScore);
                    }
                }
            } catch (IOException e) {
                // ignore and proceed with default
            }
        }

        GameServer server = null;
        try {
            server = new GameServer(port);
            // set winning score after server created
            server.setWinningScore(winningScore);

            // Add shutdown hook for graceful termination (Ctrl+C)
            final GameServer finalServer = server;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[SIGNAL] Received shutdown signal");
                finalServer.shutdown();
            }));

            // Start server (blocking)
            server.start();

        } catch (IOException e) {
            System.err.println("[FATAL] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
