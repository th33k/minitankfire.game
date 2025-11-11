package com.tankgame.network;

/**
 * Parses network messages into structured data
 */
public class MessageParser {
    
    /**
     * Parse JOIN message
     * Format: JOIN|playerName
     */
    public static String parseJoin(String[] parts) {
        return Protocol.getField(parts, 1);
    }
    
    /**
     * Parse MOVE message
     * Format: MOVE|x|y|angle
     * Returns: [x, y, angle]
     */
    public static int[] parseMove(String[] parts) {
        int x = Protocol.getFieldInt(parts, 1, 0);
        int y = Protocol.getFieldInt(parts, 2, 0);
        int angle = Protocol.getFieldInt(parts, 3, 0);
        return new int[]{x, y, angle};
    }
    
    /**
     * Parse FIRE message
     * Format: FIRE|angle
     */
    public static int parseFire(String[] parts) {
        return Protocol.getFieldInt(parts, 1, 0);
    }
    
    /**
     * Parse CHAT message
     * Format: CHAT|message
     */
    public static String parseChat(String[] parts) {
        return Protocol.getField(parts, 1);
    }
    
    /**
     * Parse HIT message
     * Format: HIT|targetId|shooterId|damage
     * Returns: [targetId, shooterId, damage]
     */
    public static Object[] parseHit(String[] parts) {
        String targetId = Protocol.getField(parts, 1);
        String shooterId = Protocol.getField(parts, 2);
        int damage = Protocol.getFieldInt(parts, 3, 0);
        return new Object[]{targetId, shooterId, damage};
    }
    
    /**
     * Parse KILL message
     * Format: KILL|killerId|victimId
     * Returns: [killerId, victimId]
     */
    public static String[] parseKill(String[] parts) {
        String killerId = Protocol.getField(parts, 1);
        String victimId = Protocol.getField(parts, 2);
        return new String[]{killerId, victimId};
    }
    
    /**
     * Parse RESPAWN message
     * Format: RESPAWN|tankId|x|y
     * Returns: [tankId, x, y]
     */
    public static Object[] parseRespawn(String[] parts) {
        String tankId = Protocol.getField(parts, 1);
        int x = Protocol.getFieldInt(parts, 2, 0);
        int y = Protocol.getFieldInt(parts, 3, 0);
        return new Object[]{tankId, x, y};
    }
    
    /**
     * Parse POWERUP_COLLECT message
     * Format: POWERUP_COLLECT|powerUpId|tankId|type
     * Returns: [powerUpId, tankId, typeId]
     */
    public static Object[] parsePowerUpCollect(String[] parts) {
        String powerUpId = Protocol.getField(parts, 1);
        String tankId = Protocol.getField(parts, 2);
        int typeId = Protocol.getFieldInt(parts, 3, 0);
        return new Object[]{powerUpId, tankId, typeId};
    }
    
    /**
     * Parse ERROR message
     * Format: ERROR|errorMessage
     */
    public static String parseError(String[] parts) {
        return Protocol.getField(parts, 1);
    }
    
    /**
     * Parse PING/PONG message
     * Format: PING|timestamp or PONG|timestamp
     */
    public static long parsePingPong(String[] parts) {
        return Protocol.getFieldLong(parts, 1, 0);
    }
    
    /**
     * Parse UPDATE message tanks field
     * Format: id:name:x:y:angle:health:kills:deaths:alive:shield:speedBoost:doubleFire;...
     */
    public static TankData[] parseTanks(String tanksStr) {
        if (tanksStr == null || tanksStr.isEmpty()) {
            return new TankData[0];
        }
        
        String[] tankStrings = tanksStr.split(";");
        TankData[] tanks = new TankData[tankStrings.length];
        
        for (int i = 0; i < tankStrings.length; i++) {
            String[] fields = tankStrings[i].split(":", -1);
            tanks[i] = new TankData(
                fields.length > 0 ? fields[0] : "",
                fields.length > 1 ? fields[1] : "",
                parseInt(fields, 2, 0),
                parseInt(fields, 3, 0),
                parseInt(fields, 4, 0),
                parseInt(fields, 5, 100),
                parseInt(fields, 6, 0),
                parseInt(fields, 7, 0),
                parseInt(fields, 8, 0) == 1,
                parseInt(fields, 9, 0) == 1,
                parseInt(fields, 10, 0) == 1,
                parseInt(fields, 11, 0) == 1
            );
        }
        return tanks;
    }
    
    /**
     * Parse UPDATE message bullets field
     * Format: id:ownerId:x:y:vx:vy;...
     */
    public static BulletData[] parseBullets(String bulletsStr) {
        if (bulletsStr == null || bulletsStr.isEmpty()) {
            return new BulletData[0];
        }
        
        String[] bulletStrings = bulletsStr.split(";");
        BulletData[] bullets = new BulletData[bulletStrings.length];
        
        for (int i = 0; i < bulletStrings.length; i++) {
            String[] fields = bulletStrings[i].split(":", -1);
            bullets[i] = new BulletData(
                fields.length > 0 ? fields[0] : "",
                fields.length > 1 ? fields[1] : "",
                parseInt(fields, 2, 0),
                parseInt(fields, 3, 0),
                parseInt(fields, 4, 0),
                parseInt(fields, 5, 0)
            );
        }
        return bullets;
    }
    
    /**
     * Parse UPDATE message powerups field
     * Format: id:type:x:y;...
     */
    public static PowerUpData[] parsePowerUps(String powerUpsStr) {
        if (powerUpsStr == null || powerUpsStr.isEmpty()) {
            return new PowerUpData[0];
        }
        
        String[] powerUpStrings = powerUpsStr.split(";");
        PowerUpData[] powerUps = new PowerUpData[powerUpStrings.length];
        
        for (int i = 0; i < powerUpStrings.length; i++) {
            String[] fields = powerUpStrings[i].split(":", -1);
            powerUps[i] = new PowerUpData(
                fields.length > 0 ? fields[0] : "",
                parseInt(fields, 1, 0),
                parseInt(fields, 2, 0),
                parseInt(fields, 3, 0)
            );
        }
        return powerUps;
    }
    
    // Helper method
    private static int parseInt(String[] fields, int index, int defaultValue) {
        try {
            return fields.length > index ? Integer.parseInt(fields[index]) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // Data classes for parsed data
    public static class TankData {
        public final String id;
        public final String name;
        public final int x, y, angle;
        public final int health, kills, deaths;
        public final boolean alive, shield, speedBoost, doubleFire;
        
        public TankData(String id, String name, int x, int y, int angle, 
                       int health, int kills, int deaths, boolean alive,
                       boolean shield, boolean speedBoost, boolean doubleFire) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.health = health;
            this.kills = kills;
            this.deaths = deaths;
            this.alive = alive;
            this.shield = shield;
            this.speedBoost = speedBoost;
            this.doubleFire = doubleFire;
        }
    }
    
    public static class BulletData {
        public final String id;
        public final String ownerId;
        public final int x, y;
        public final int vx, vy;
        
        public BulletData(String id, String ownerId, int x, int y, int vx, int vy) {
            this.id = id;
            this.ownerId = ownerId;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }
    
    public static class PowerUpData {
        public final String id;
        public final int type;
        public final int x, y;
        
        public PowerUpData(String id, int type, int x, int y) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }
}
