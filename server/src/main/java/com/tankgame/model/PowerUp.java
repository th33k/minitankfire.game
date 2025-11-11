package com.tankgame.model;

import com.tankgame.common.Constants;

/**
 * Represents a power-up collectible in the game
 */
public class PowerUp {
    public enum Type {
        SHIELD(0, "Shield"),
        SPEED_BOOST(1, "Speed Boost"),
        DOUBLE_FIRE(2, "Double Fire");
        
        private final int id;
        private final String displayName;
        
        Type(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public int getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static Type fromId(int id) {
            for (Type type : values()) {
                if (type.id == id) return type;
            }
            return SHIELD; // default
        }
    }
    
    private String id;
    private Type type;
    private Position position;
    private long spawnTime;
    
    public PowerUp(String id, Type type, int x, int y) {
        this.id = id;
        this.type = type;
        this.position = new Position(x, y);
        this.spawnTime = System.currentTimeMillis();
    }
    
    // Getters
    public String getId() { return id; }
    public Type getType() { return type; }
    public Position getPosition() { return position; }
    public long getSpawnTime() { return spawnTime; }
    
    /**
     * Check if power-up is expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > Constants.POWERUP_LIFETIME;
    }
    
    @Override
    public String toString() {
        return String.format("PowerUp[%s, %s, %s]", id, type.getDisplayName(), position);
    }
}
