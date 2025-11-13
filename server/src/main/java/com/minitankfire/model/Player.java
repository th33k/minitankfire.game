package com.minitankfire.model;

/**
 * Player model representing a tank in the game.
 * Manages player state including position, health, and power-ups.
 */
public class Player {
    private String id;
    private String name;
    private int x, y;
    private int angle;
    private int score;
    private int health;
    private boolean alive;
    private long lastRespawnTime;
    private boolean hasShield;
    private long shieldEndTime;
    private boolean speedBoost;
    private long speedBoostEndTime;
    private boolean doubleFire;
    private long doubleFireEndTime;
    private long lastPowerUpCollectTime;
    private String lastPowerUpType;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
        this.health = 100;
        this.alive = true;
        this.hasShield = false;
        this.speedBoost = false;
        this.doubleFire = false;
        this.lastPowerUpCollectTime = 0;
        this.lastPowerUpType = null;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getAngle() { return angle; }
    public void setAngle(int angle) { this.angle = angle; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
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
    
    // Power-up collection tracking for animations
    public long getLastPowerUpCollectTime() { return lastPowerUpCollectTime; }
    public void setLastPowerUpCollectTime(long lastPowerUpCollectTime) { this.lastPowerUpCollectTime = lastPowerUpCollectTime; }
    public String getLastPowerUpType() { return lastPowerUpType; }
    public void setLastPowerUpType(String lastPowerUpType) { this.lastPowerUpType = lastPowerUpType; }
}
