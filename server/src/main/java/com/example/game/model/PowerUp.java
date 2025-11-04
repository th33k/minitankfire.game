package com.example.game.model;

/**
 * Represents a power-up that players can collect.
 */
public class PowerUp extends GameObject {
    private static final long serialVersionUID = 1L;

    public enum Type {
        SHIELD, SPEED_BOOST, DOUBLE_FIRE
    }

    private Type type;
    private long spawnTime;

    public PowerUp(String id, Type type, int x, int y) {
        super(id, x, y);
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }

    public Type getType() { return type; }
    public long getSpawnTime() { return spawnTime; }

    @Override
    public void update() {
        // Power-ups don't move, but could have animations
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > 10000; // 10 seconds
    }
}