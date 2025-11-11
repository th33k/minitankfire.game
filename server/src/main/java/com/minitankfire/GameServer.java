package com.minitankfire;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Pure Java Network Programming implementation of Tank Game Server
 * 
 * Key Network Programming Concepts Demonstrated:
 * 1. ServerSocket - TCP server socket for accepting client connections
 * 2. Socket - Individual client connections
 * 3. Multi-threading - Thread pool (ExecutorService) for concurrent client
 * handling
 * 4. WebSocket Protocol - Manual implementation of RFC 6455
 * 5. Client-Server Architecture - Multiple clients connecting to a central
 * server
 * 6. Concurrent Programming - Thread-safe data structures (ConcurrentHashMap)
 * 7. I/O Streams - Reading/writing data over network sockets
 * 
 * NO EXTERNAL FRAMEWORKS - Only core Java APIs used:
 * - java.net.ServerSocket
 * - java.net.Socket
 * - java.io.InputStream / OutputStream
 * - java.util.concurrent (for thread management)
 */
public class GameServer {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 100;

    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private GameRoom gameRoom;
    private volatile boolean running;

    public GameServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.gameRoom = new GameRoom();
        this.running = true;

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║      Tank Game Server - Pure Java Network Programming     ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  Network Concepts Demonstrated:                           ║");
        System.out.println("║  ✓ ServerSocket (TCP Server)                              ║");
        System.out.println("║  ✓ Socket Programming (Client Connections)                ║");
        System.out.println("║  ✓ Multi-threading (Thread Pool)                          ║");
        System.out.println("║  ✓ WebSocket Protocol (RFC 6455 Manual Implementation)    ║");
        System.out.println("║  ✓ Client-Server Communication                            ║");
        System.out.println("║  ✓ Concurrent Programming (Thread-safe Collections)       ║");
        System.out.println("║  ✓ I/O Streams (Network Data Transfer)                    ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  Server started on: 0.0.0.0:" + port + "                            ║");
        System.out.println("║  Max concurrent clients: " + MAX_CLIENTS + "                             ║");
        System.out.println("║  WebSocket endpoint: ws://localhost:" + port + "/game              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Main server loop - accepts client connections
     * Demonstrates ServerSocket.accept() blocking I/O
     */
    public void start() {
        System.out.println("\n[SERVER] Waiting for client connections...\n");

        while (running) {
            try {
                // Accept incoming connection (blocking call)
                Socket clientSocket = serverSocket.accept();

                // Configure socket options
                clientSocket.setTcpNoDelay(true); // Disable Nagle's algorithm for real-time game
                clientSocket.setSoTimeout(0); // No read timeout

                System.out.println("[CONNECTION] New client from: " +
                        clientSocket.getInetAddress().getHostAddress() +
                        ":" + clientSocket.getPort());

                // Create client handler and submit to thread pool
                ClientHandler handler = new ClientHandler(clientSocket, gameRoom);
                clientThreadPool.execute(handler);

            } catch (IOException e) {
                if (running) {
                    System.err.println("[ERROR] Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gracefully shuts down the server
     */
    public void shutdown() {
        System.out.println("\n[SERVER] Shutting down...");
        running = false;

        try {
            // Stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Stop game room
            gameRoom.stop();

            // Shutdown thread pool
            clientThreadPool.shutdown();
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }

            System.out.println("[SERVER] Server stopped successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Error during shutdown: " + e.getMessage());
        }
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        int port = PORT;

        // Allow custom port via command line argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: " + PORT);
            }
        }

        GameServer server = null;
        try {
            server = new GameServer(port);

            // Add shutdown hook for graceful termination
            final GameServer finalServer = server;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[SHUTDOWN] Received shutdown signal");
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