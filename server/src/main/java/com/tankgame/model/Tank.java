package com.tankgame.model;

import com.tankgame.common.Constants;

/**
 * Represents a player's tank in the game
 */
public class Tank {
    private String id;
    private String playerName;
    private Position position;
    private int health;
    private int kills;
    private int deaths;
    private boolean alive;
    private long lastFireTime;
    private long lastRespawnTime;
    
    // Power-ups
    private boolean hasShield;
    private long shieldEndTime;
    private boolean hasSpeedBoost;
    private long speedBoostEndTime;
    private boolean hasDoubleFire;
    private long doubleFireEndTime;
    
    public Tank(String id, String playerName, int x, int y) {
        this.id = id;
        this.playerName = playerName;
        this.position = new Position(x, y, 0);
        this.health = Constants.TANK_MAX_HEALTH;
        this.kills = 0;
        this.deaths = 0;
        this.alive = true;
        this.lastFireTime = 0;
        this.lastRespawnTime = 0;
        this.hasShield = false;
        this.hasSpeedBoost = false;
        this.hasDoubleFire = false;
    }
    
    // Getters
    public String getId() { return id; }
    public String getPlayerName() { return playerName; }
    public Position getPosition() { return position; }
    public int getHealth() { return health; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public boolean isAlive() { return alive; }
    public long getLastFireTime() { return lastFireTime; }
    public long getLastRespawnTime() { return lastRespawnTime; }
    
    // Setters
    public void setHealth(int health) { 
        this.health = Math.max(0, Math.min(Constants.TANK_MAX_HEALTH, health)); 
        if (this.health == 0) {
            this.alive = false;
        }
    }
    
    public void setAlive(boolean alive) { this.alive = alive; }
    public void setLastFireTime(long time) { this.lastFireTime = time; }
    public void setLastRespawnTime(long time) { this.lastRespawnTime = time; }
    
    // Game actions
    public void takeDamage(int damage) {
        if (hasShield && System.currentTimeMillis() < shieldEndTime) {
            return; // Shield absorbs damage
        }
        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
            deaths++;
        }
    }
    
    public void kill() {
        kills++;
    }
    
    public void respawn(int x, int y) {
        this.position.setX(x);
        this.position.setY(y);
        this.position.setAngle(0);
        this.health = Constants.TANK_MAX_HEALTH;
        this.alive = true;
        this.lastRespawnTime = System.currentTimeMillis();
        clearPowerUps();
    }
    
    public boolean canFire() {
        return alive && (System.currentTimeMillis() - lastFireTime) >= Constants.FIRE_COOLDOWN;
    }
    
    public int getSpeed() {
        if (hasSpeedBoost && System.currentTimeMillis() < speedBoostEndTime) {
            return Constants.TANK_SPEED_BOOST;
        }
        return Constants.TANK_SPEED;
    }
    
    public int getScore() {
        return kills * 10 - deaths * 5;
    }
    
    // Power-ups
    public boolean hasShield() { 
        return hasShield && System.currentTimeMillis() < shieldEndTime; 
    }
    
    public void activateShield() {
        this.hasShield = true;
        this.shieldEndTime = System.currentTimeMillis() + Constants.POWERUP_EFFECT_DURATION;
    }
    
    public boolean hasSpeedBoost() { 
        return hasSpeedBoost && System.currentTimeMillis() < speedBoostEndTime; 
    }
    
    public void activateSpeedBoost() {
        this.hasSpeedBoost = true;
        this.speedBoostEndTime = System.currentTimeMillis() + Constants.POWERUP_EFFECT_DURATION;
    }
    
    public boolean hasDoubleFire() { 
        return hasDoubleFire && System.currentTimeMillis() < doubleFireEndTime; 
    }
    
    public void activateDoubleFire() {
        this.hasDoubleFire = true;
        this.doubleFireEndTime = System.currentTimeMillis() + Constants.POWERUP_EFFECT_DURATION;
    }
    
    public void clearPowerUps() {
        this.hasShield = false;
        this.hasSpeedBoost = false;
        this.hasDoubleFire = false;
    }
    
    @Override
    public String toString() {
        return String.format("Tank[%s, %s, HP:%d, K:%d, D:%d]", 
            id, playerName, health, kills, deaths);
    }
}
