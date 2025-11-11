package com.tankgame.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the complete state of the game
 */
public class GameState {
    private Map<String, Tank> tanks;
    private Map<String, Bullet> bullets;
    private Map<String, PowerUp> powerUps;
    private long startTime;
    
    public GameState() {
        this.tanks = new ConcurrentHashMap<>();
        this.bullets = new ConcurrentHashMap<>();
        this.powerUps = new ConcurrentHashMap<>();
        this.startTime = System.currentTimeMillis();
    }
    
    // Tank management
    public void addTank(Tank tank) {
        tanks.put(tank.getId(), tank);
    }
    
    public void removeTank(String tankId) {
        tanks.remove(tankId);
        // Remove bullets owned by this tank
        bullets.values().removeIf(bullet -> bullet.getOwnerId().equals(tankId));
    }
    
    public Tank getTank(String tankId) {
        return tanks.get(tankId);
    }
    
    public Collection<Tank> getAllTanks() {
        return tanks.values();
    }
    
    public int getTankCount() {
        return tanks.size();
    }
    
    // Bullet management
    public void addBullet(Bullet bullet) {
        bullets.put(bullet.getId(), bullet);
    }
    
    public void removeBullet(String bulletId) {
        bullets.remove(bulletId);
    }
    
    public Bullet getBullet(String bulletId) {
        return bullets.get(bulletId);
    }
    
    public Collection<Bullet> getAllBullets() {
        return bullets.values();
    }
    
    public void updateBullets() {
        // Update all bullet positions
        for (Bullet bullet : bullets.values()) {
            bullet.update();
        }
        
        // Remove expired or out-of-bounds bullets
        bullets.values().removeIf(bullet -> bullet.isExpired() || bullet.isOutOfBounds());
    }
    
    // Power-up management
    public void addPowerUp(PowerUp powerUp) {
        powerUps.put(powerUp.getId(), powerUp);
    }
    
    public void removePowerUp(String powerUpId) {
        powerUps.remove(powerUpId);
    }
    
    public PowerUp getPowerUp(String powerUpId) {
        return powerUps.get(powerUpId);
    }
    
    public Collection<PowerUp> getAllPowerUps() {
        return powerUps.values();
    }
    
    public void updatePowerUps() {
        // Remove expired power-ups
        powerUps.values().removeIf(PowerUp::isExpired);
    }
    
    // Game time
    public long getGameTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Get leaderboard sorted by score
     */
    public List<Tank> getLeaderboard() {
        List<Tank> leaderboard = new ArrayList<>(tanks.values());
        leaderboard.sort((t1, t2) -> Integer.compare(t2.getScore(), t1.getScore()));
        return leaderboard;
    }
    
    /**
     * Clear all game state
     */
    public void clear() {
        tanks.clear();
        bullets.clear();
        powerUps.clear();
    }
}
