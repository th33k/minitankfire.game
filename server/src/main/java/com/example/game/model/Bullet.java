package com.example.game.model;

/**
 * Represents a bullet fired by a player.
 */
public class Bullet extends GameObject {
    private static final long serialVersionUID = 1L;

    private String ownerId;
    private int dx, dy;
    private long creationTime;

    public Bullet(String id, String ownerId, int x, int y, int dx, int dy) {
        super(id, x, y);
        this.ownerId = ownerId;
        this.dx = dx;
        this.dy = dy;
        this.creationTime = System.currentTimeMillis();
    }

    public String getOwnerId() { return ownerId; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public long getCreationTime() { return creationTime; }

    @Override
    public void update() {
        // Move the bullet
        x += dx;
        y += dy;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > 5000; // 5 seconds lifetime
    }
}