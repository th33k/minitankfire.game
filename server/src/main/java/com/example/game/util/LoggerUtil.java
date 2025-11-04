package com.example.game.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility class for standardized logging across the game server.
 * Uses Java's built-in logging framework.
 */
public class LoggerUtil {

    private static final Logger logger = Logger.getLogger("GameServer");

    static {
        // Configure console handler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);

        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false); // Disable parent handlers
    }

    /**
     * Logs an info message.
     * @param message the message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning message.
     * @param message the message to log
     */
    public static void warning(String message) {
        logger.warning(message);
    }

    /**
     * Logs an error message.
     * @param message the message to log
     */
    public static void error(String message) {
        logger.severe(message);
    }

    /**
     * Logs an error message with exception.
     * @param message the message to log
     * @param throwable the exception to log
     */
    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs a debug message.
     * @param message the message to log
     */
    public static void debug(String message) {
        logger.fine(message);
    }
}