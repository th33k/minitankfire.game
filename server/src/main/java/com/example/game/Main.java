package com.example.game;

import com.example.game.server.*;
import com.example.game.util.LoggerUtil;
import com.example.game.util.NetworkUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main entry point for the multiplayer game server.
 * Starts all server components and manages the game lifecycle.
 */
public class Main {
    private static final String CONFIG_FILE = "config.properties";

    public static void main(String[] args) {
        LoggerUtil.info("Starting Tank Game Server...");

        // Load configuration
        Properties config = loadConfig();
        if (config == null) {
            LoggerUtil.error("Failed to load configuration. Exiting.");
            return;
        }

        int tcpPort = Integer.parseInt(config.getProperty("tcp.port", "8080"));
        int udpPort = Integer.parseInt(config.getProperty("udp.port", "8081"));
        int nioPort = Integer.parseInt(config.getProperty("nio.port", "8082"));
        int tickRate = Integer.parseInt(config.getProperty("game.tick.rate", "60"));

        // Initialize game components
        GameStateManager gameStateManager = new GameStateManager();
        GameLoop gameLoop = new GameLoop(tickRate);

        // Integrate game loop with state manager
        gameLoop.setGameStateManager(gameStateManager);

        // Initialize servers
        final TcpServer tcpServer = new TcpServer(tcpPort, gameStateManager);
        final UdpServer udpServer = new UdpServer(udpPort);
        final NioServer nioServer = new NioServer(nioPort);

        BroadcastService broadcastService = null;
        try {
            broadcastService = new BroadcastService(udpPort);
        } catch (IOException e) {
            LoggerUtil.error("Failed to initialize broadcast service", e);
            return;
        }
        final BroadcastService finalBroadcastService = broadcastService;

        // Start servers in separate threads
        ExecutorService executor = Executors.newCachedThreadPool();

        executor.submit(tcpServer);
        executor.submit(udpServer);
        executor.submit(nioServer);

        // Start game loop
        gameLoop.start();

        LoggerUtil.info("Tank Game Server started successfully!");
        LoggerUtil.info("TCP Server: " + NetworkUtils.getLocalIpAddress() + ":" + tcpPort);
        LoggerUtil.info("UDP Server: " + NetworkUtils.getLocalIpAddress() + ":" + udpPort);
        LoggerUtil.info("NIO Server: " + NetworkUtils.getLocalIpAddress() + ":" + nioPort);
        LoggerUtil.info("Game tick rate: " + tickRate + " TPS");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerUtil.info("Shutting down Tank Game Server...");
            gameLoop.stop();
            tcpServer.stop();
            udpServer.stop();
            nioServer.stop();
            if (finalBroadcastService != null) {
                finalBroadcastService.close();
            }
            executor.shutdown();
            LoggerUtil.info("Tank Game Server shut down.");
        }));

        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LoggerUtil.info("Server interrupted, shutting down...");
        }
    }

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LoggerUtil.error("Configuration file not found: " + CONFIG_FILE);
                return null;
            }
            config.load(input);
            LoggerUtil.info("Configuration loaded successfully");
        } catch (IOException e) {
            LoggerUtil.error("Error loading configuration", e);
            return null;
        }
        return config;
    }
}