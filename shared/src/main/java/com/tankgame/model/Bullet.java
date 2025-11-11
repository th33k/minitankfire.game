package com.tankgame.model;

import com.tankgame.common.Constants;

/**
 * Represents a bullet fired by a tank
 */
public class Bullet {
    private String id;
    private String ownerId;
    private Position position;
    private int velocityX;
    private int velocityY;
    private long creationTime;
    
    public Bullet(String id, String ownerId, int x, int y, int angle) {
        this.id = id;
        this.ownerId = ownerId;
        this.position = new Position(x, y, angle);
        this.creationTime = System.currentTimeMillis();
        
        // Calculate velocity based on angle
        double radians = Math.toRadians(angle);
        this.velocityX = (int) Math.round(Constants.BULLET_SPEED * Math.cos(radians));
        this.velocityY = (int) Math.round(Constants.BULLET_SPEED * Math.sin(radians));
    }
    
    // Getters
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public Position getPosition() { return position; }
    public int getVelocityX() { return velocityX; }
    public int getVelocityY() { return velocityY; }
    public long getCreationTime() { return creationTime; }
    
    /**
     * Update bullet position
     */
    public void update() {
        position.move(velocityX, velocityY);
    }
    
    /**
     * Check if bullet is expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > Constants.BULLET_LIFETIME;
    }
    
    /**
     * Check if bullet is out of bounds
     */
    public boolean isOutOfBounds() {
        return position.getX() < 0 || position.getX() > Constants.MAP_WIDTH ||
               position.getY() < 0 || position.getY() > Constants.MAP_HEIGHT;
    }
    
    @Override
    public String toString() {
        return String.format("Bullet[%s, owner:%s, %s]", id, ownerId, position);
    }
}