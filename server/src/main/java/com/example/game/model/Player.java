package com.example.game.model;

import java.io.Serializable;

/**
 * Represents a player in the game with position, score, and power-ups.
 */
public class Player extends GameObject {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private int angle;
    private int score;
    private boolean alive;
    private long lastRespawnTime;
    private boolean hasShield;
    private long shieldEndTime;
    private boolean speedBoost;
    private long speedBoostEndTime;
    private boolean doubleFire;
    private long doubleFireEndTime;

    public Player(String id, String name) {
        super(id, 0, 0); // Initialize with default position
        this.id = id;
        this.name = name;
        this.score = 0;
        this.alive = true;
        this.hasShield = false;
        this.speedBoost = false;
        this.doubleFire = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getAngle() { return angle; }
    public void setAngle(int angle) { this.angle = angle; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public long getLastRespawnTime() { return lastRespawnTime; }
    public void setLastRespawnTime(long lastRespawnTime) { this.lastRespawnTime = lastRespawnTime; }
    public boolean hasShield() { return hasShield; }
    public void setShield(boolean hasShield) { this.hasShield = hasShield; }
    public long getShieldEndTime() { return shieldEndTime; }
    public void setShieldEndTime(long shieldEndTime) { this.shieldEndTime = shieldEndTime; }
    public boolean hasSpeedBoost() { return speedBoost; }
    public void setSpeedBoost(boolean speedBoost) { this.speedBoost = speedBoost; }
    public long getSpeedBoostEndTime() { return speedBoostEndTime; }
    public void setSpeedBoostEndTime(long speedBoostEndTime) { this.speedBoostEndTime = speedBoostEndTime; }
    public boolean hasDoubleFire() { return doubleFire; }
    public void setDoubleFire(boolean doubleFire) { this.doubleFire = doubleFire; }
    public long getDoubleFireEndTime() { return doubleFireEndTime; }
    public void setDoubleFireEndTime(long doubleFireEndTime) { this.doubleFireEndTime = doubleFireEndTime; }

    @Override
    public void update() {
        // Player position is updated externally via movement commands
        // Power-up effects are handled in GameStateManager
    }
}