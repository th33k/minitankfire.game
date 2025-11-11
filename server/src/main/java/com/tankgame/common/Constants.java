package com.tankgame.common;

/**
 * Game constants and configuration values
 */
public class Constants {
    // Server Configuration
    public static final int SERVER_PORT = 8080;
    public static final String SERVER_HOST = "0.0.0.0";
    public static final int MAX_PLAYERS = 50;
    
    // Game World
    public static final int MAP_WIDTH = 1920;
    public static final int MAP_HEIGHT = 1080;
    public static final int GAME_TICK_RATE = 50; // milliseconds (20 FPS)
    
    // Player/Tank
    public static final int TANK_SIZE = 30;
    public static final int TANK_SPEED = 10;
    public static final int TANK_SPEED_BOOST = 15;
    public static final int TANK_MAX_HEALTH = 100;
    public static final int TANK_RESPAWN_TIME = 3000; // 3 seconds
    
    // Bullet
    public static final int BULLET_SPEED = 35;
    public static final int BULLET_SIZE = 5;
    public static final int BULLET_DAMAGE = 20;
    public static final int BULLET_LIFETIME = 1000; // 3 seconds
    public static final int FIRE_COOLDOWN = 500; // milliseconds
    
    // Power-ups
    public static final int POWERUP_SIZE = 20;
    public static final int POWERUP_LIFETIME = 15000; // 15 seconds
    public static final int POWERUP_EFFECT_DURATION = 10000; // 10 seconds
    public static final double POWERUP_SPAWN_CHANCE = 0.005; // 0.5% per tick
    
    // Collision Detection
    public static final int COLLISION_RADIUS_TANK = 15;
    public static final int COLLISION_RADIUS_BULLET = 3;
    public static final int COLLISION_RADIUS_POWERUP = 10;
    
    // Protocol
    public static final String MESSAGE_DELIMITER = "\n";
    public static final String FIELD_DELIMITER = "|";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
