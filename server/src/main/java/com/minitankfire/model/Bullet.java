package com.minitankfire.model;

/**
 * Bullet model representing a projectile fired by a player.
 * Manages bullet position and movement.
 */
public class Bullet {
    private String id;
    private String ownerId;
    private int x, y;
    private int dx, dy;
    private long creationTime;

    public Bullet(String id, String ownerId, int x, int y, int dx, int dy) {
        this.id = id;
        this.ownerId = ownerId;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.creationTime = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public long getCreationTime() { return creationTime; }

    public void updatePosition() {
        x += dx;
        y += dy;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > 3000; // 3 seconds
    }
}
