package com.minitankfire;

public class PowerUp {
    public enum Type {
        SHIELD, SPEED_BOOST, DOUBLE_FIRE
    }

    private String id;
    private Type type;
    private int x, y;
    private long spawnTime;

    public PowerUp(String id, Type type, int x, int y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.spawnTime = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public Type getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public long getSpawnTime() { return spawnTime; }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > 10000; // 10 seconds
    }
}