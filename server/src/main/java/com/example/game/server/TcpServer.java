package com.example.game.server;

import com.example.game.util.LoggerUtil;
import com.example.game.util.NetworkUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP server for reliable data transfer (login, chat, game setup).
 * Uses ServerSocket and Socket for blocking I/O.
 */
public class TcpServer implements Runnable {
    private final int port;
    private final ExecutorService executor;
    private final GameStateManager gameStateManager;
    private volatile boolean running = true;

    public TcpServer(int port, GameStateManager gameStateManager) {
        this.port = port;
        this.gameStateManager = gameStateManager;
        this.executor = Executors.newCachedThreadPool();
        LoggerUtil.info("TCP Server initialized on port " + port);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LoggerUtil.info("TCP Server started on " + NetworkUtils.getLocalIpAddress() + ":" + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    LoggerUtil.info("New TCP connection from " + clientSocket.getInetAddress());

                    // Handle client in a separate thread
                    executor.submit(new ClientHandler(clientSocket, gameStateManager));
                } catch (IOException e) {
                    if (running) {
                        LoggerUtil.error("Error accepting TCP connection", e);
                    }
                }
            }
        } catch (IOException e) {
            LoggerUtil.error("Failed to start TCP server", e);
        } finally {
            executor.shutdown();
            LoggerUtil.info("TCP Server stopped");
        }
    }

    public void stop() {
        running = false;
        executor.shutdownNow();
    }
}