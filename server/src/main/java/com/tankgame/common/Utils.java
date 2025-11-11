package com.tankgame.common;

import java.util.Random;

/**
 * Utility methods for the game
 */
public class Utils {
    private static final Random random = new Random();
    
    /**
     * Calculate distance between two points
     */
    public static double distance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Check if two circles collide
     */
    public static boolean circleCollision(int x1, int y1, int r1, int x2, int y2, int r2) {
        return distance(x1, y1, x2, y2) < (r1 + r2);
    }
    
    /**
     * Clamp value between min and max
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Generate random integer between min (inclusive) and max (exclusive)
     */
    public static int randomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }
    
    /**
     * Generate random double between 0.0 and 1.0
     */
    public static double randomDouble() {
        return random.nextDouble();
    }
    
    /**
     * Generate unique ID
     */
    public static String generateId() {
        return String.valueOf(System.nanoTime()) + "_" + randomInt(1000, 9999);
    }
    
    /**
     * Escape special characters in strings for protocol
     */
    public static String escape(String str) {
        if (str == null) return "";
        return str.replace("|", "\\|")
                  .replace("\n", "\\n")
                  .replace("\"", "\\\"");
    }
    
    /**
     * Unescape special characters
     */
    public static String unescape(String str) {
        if (str == null) return "";
        return str.replace("\\|", "|")
                  .replace("\\n", "\n")
                  .replace("\\\"", "\"");
    }
    
    /**
     * Format time in seconds
     */
    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    // Private constructor to prevent instantiation
    private Utils() {
        throw new AssertionError("Cannot instantiate Utils class");
    }
}
