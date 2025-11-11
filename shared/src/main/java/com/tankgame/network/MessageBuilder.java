package com.tankgame.network;

import com.tankgame.model.*;
import java.util.Collection;

/**
 * Builds network messages from game objects
 */
public class MessageBuilder {
    
    /**
     * Build JOIN message
     */
    public static String buildJoin(String playerName) {
        return Protocol.createMessage(Protocol.MSG_JOIN, playerName);
    }
    
    /**
     * Build MOVE message
     */
    public static String buildMove(int x, int y, int angle) {
        return Protocol.createMessage(Protocol.MSG_MOVE, 
            String.valueOf(x), 
            String.valueOf(y), 
            String.valueOf(angle));
    }
    
    /**
     * Build FIRE message
     */
    public static String buildFire(int angle) {
        return Protocol.createMessage(Protocol.MSG_FIRE, String.valueOf(angle));
    }
    
    /**
     * Build CHAT message
     */
    public static String buildChat(String message) {
        return Protocol.createMessage(Protocol.MSG_CHAT, message);
    }
    
    /**
     * Build UPDATE message with complete game state
     */
    public static String buildUpdate(Collection<Tank> tanks, 
                                    Collection<Bullet> bullets, 
                                    Collection<PowerUp> powerUps) {
        StringBuilder sb = new StringBuilder();
        sb.append(Protocol.MSG_UPDATE).append("|");
        
        // Tanks
        sb.append(serializeTanks(tanks)).append("|");
        
        // Bullets
        sb.append(serializeBullets(bullets)).append("|");
        
        // PowerUps
        sb.append(serializePowerUps(powerUps));
        
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * Build HIT message
     */
    public static String buildHit(String targetId, String shooterId, int damage) {
        return Protocol.createMessage(Protocol.MSG_HIT, targetId, shooterId, String.valueOf(damage));
    }
    
    /**
     * Build KILL message
     */
    public static String buildKill(String killerId, String victimId) {
        return Protocol.createMessage(Protocol.MSG_KILL, killerId, victimId);
    }
    
    /**
     * Build RESPAWN message
     */
    public static String buildRespawn(String tankId, int x, int y) {
        return Protocol.createMessage(Protocol.MSG_RESPAWN, tankId, String.valueOf(x), String.valueOf(y));
    }
    
    /**
     * Build POWERUP_COLLECT message
     */
    public static String buildPowerUpCollect(String powerUpId, String tankId, PowerUp.Type type) {
        return Protocol.createMessage(Protocol.MSG_POWERUP_COLLECT, 
            powerUpId, tankId, String.valueOf(type.getId()));
    }
    
    /**
     * Build ERROR message
     */
    public static String buildError(String errorMessage) {
        return Protocol.createMessage(Protocol.MSG_ERROR, errorMessage);
    }
    
    /**
     * Build PING message
     */
    public static String buildPing() {
        return Protocol.createMessage(Protocol.MSG_PING, String.valueOf(System.currentTimeMillis()));
    }
    
    /**
     * Build PONG message
     */
    public static String buildPong() {
        return Protocol.createMessage(Protocol.MSG_PONG, String.valueOf(System.currentTimeMillis()));
    }
    
    // Serialization helpers
    private static String serializeTanks(Collection<Tank> tanks) {
        if (tanks.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Tank tank : tanks) {
            if (sb.length() > 0) sb.append(";");
            
            sb.append(tank.getId()).append(":")
              .append(tank.getPlayerName()).append(":")
              .append(tank.getPosition().getX()).append(":")
              .append(tank.getPosition().getY()).append(":")
              .append(tank.getPosition().getAngle()).append(":")
              .append(tank.getHealth()).append(":")
              .append(tank.getKills()).append(":")
              .append(tank.getDeaths()).append(":")
              .append(tank.isAlive() ? "1" : "0").append(":")
              .append(tank.hasShield() ? "1" : "0").append(":")
              .append(tank.hasSpeedBoost() ? "1" : "0").append(":")
              .append(tank.hasDoubleFire() ? "1" : "0");
        }
        return sb.toString();
    }
    
    private static String serializeBullets(Collection<Bullet> bullets) {
        if (bullets.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Bullet bullet : bullets) {
            if (sb.length() > 0) sb.append(";");
            
            sb.append(bullet.getId()).append(":")
              .append(bullet.getOwnerId()).append(":")
              .append(bullet.getPosition().getX()).append(":")
              .append(bullet.getPosition().getY()).append(":")
              .append(bullet.getVelocityX()).append(":")
              .append(bullet.getVelocityY());
        }
        return sb.toString();
    }
    
    private static String serializePowerUps(Collection<PowerUp> powerUps) {
        if (powerUps.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (PowerUp powerUp : powerUps) {
            if (sb.length() > 0) sb.append(";");
            
            sb.append(powerUp.getId()).append(":")
              .append(powerUp.getType().getId()).append(":")
              .append(powerUp.getPosition().getX()).append(":")
              .append(powerUp.getPosition().getY());
        }
        return sb.toString();
    }
    
    /**
     * Build VOICE_OFFER message
     */
    public static String buildVoiceOffer(String fromId, String targetId, String offerJson) {
        return Protocol.createMessage(Protocol.MSG_VOICE_OFFER, fromId, targetId, offerJson);
    }
    
    /**
     * Build VOICE_ANSWER message
     */
    public static String buildVoiceAnswer(String fromId, String targetId, String answerJson) {
        return Protocol.createMessage(Protocol.MSG_VOICE_ANSWER, fromId, targetId, answerJson);
    }
    
    /**
     * Build VOICE_ICE message
     */
    public static String buildVoiceIce(String fromId, String targetId, String candidateJson) {
        return Protocol.createMessage(Protocol.MSG_VOICE_ICE, fromId, targetId, candidateJson);
    }
}