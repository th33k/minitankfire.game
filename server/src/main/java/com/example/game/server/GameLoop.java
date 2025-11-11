package com.example.game.server;

import com.example.game.util.LoggerUtil;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Game loop that maintains consistent tick rate for game updates.
 * Uses ScheduledExecutorService to run at fixed intervals.
 */
public class GameLoop {
    private final ScheduledExecutorService scheduler;
    private final int tickRate; // ticks per second
    private volatile boolean running = false;
    private GameStateManager gameStateManager;

    public GameLoop(int tickRate) {
        this.tickRate = tickRate;
        this.scheduler = Executors.newScheduledThreadPool(1);
        LoggerUtil.info("Game loop initialized with " + tickRate + " TPS");
    }

    public void setGameStateManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        if (running) {
            LoggerUtil.warning("Game loop is already running");
            return;
        }

        running = true;
        long periodMillis = 1000 / tickRate;

        scheduler.scheduleAtFixedRate(this::tick, 0, periodMillis, TimeUnit.MILLISECONDS);
        LoggerUtil.info("Game loop started with " + periodMillis + "ms intervals");
    }

    /**
     * Stops the game loop.
     */
    public void stop() {
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LoggerUtil.info("Game loop stopped");
    }

    /**
     * Called every tick to update game state.
     */
    private void tick() {
        if (!running) return;

        try {
            if (gameStateManager != null) {
                gameStateManager.update();
            }

            // Log tick (less frequently to avoid spam)
            if (System.currentTimeMillis() % 5000 < 100) { // Log roughly every 5 seconds
                LoggerUtil.debug("Game tick executed");
            }

        } catch (Exception e) {
            LoggerUtil.error("Error in game tick", e);
        }
    }

    public boolean isRunning() {
        return running;
    }
}