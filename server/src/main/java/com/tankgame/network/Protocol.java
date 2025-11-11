package com.tankgame.network;

/**
 * Network protocol definitions and message types
 */
public class Protocol {
    // Message Types
    public static final String MSG_JOIN = "JOIN";
    public static final String MSG_MOVE = "MOVE";
    public static final String MSG_FIRE = "FIRE";
    public static final String MSG_CHAT = "CHAT";
    public static final String MSG_UPDATE = "UPDATE";
    public static final String MSG_HIT = "HIT";
    public static final String MSG_KILL = "KILL";
    public static final String MSG_RESPAWN = "RESPAWN";
    public static final String MSG_POWERUP_COLLECT = "POWERUP_COLLECT";
    public static final String MSG_DISCONNECT = "DISCONNECT";
    public static final String MSG_ERROR = "ERROR";
    public static final String MSG_PING = "PING";
    public static final String MSG_PONG = "PONG";
    public static final String MSG_VOICE_OFFER = "VOICE_OFFER";
    public static final String MSG_VOICE_ANSWER = "VOICE_ANSWER";
    public static final String MSG_VOICE_ICE = "VOICE_ICE";
    
    // Message format: TYPE|field1|field2|field3...\n
    // Examples:
    // JOIN|playerName
    // MOVE|x|y|angle
    // FIRE|angle
    // CHAT|message
    // UPDATE|tanksJson|bulletsJson|powerUpsJson
    // HIT|targetId|shooterId|damage
    // KILL|killerId|victimId
    // RESPAWN|tankId|x|y
    // POWERUP_COLLECT|powerUpId|tankId|type
    
    /**
     * Create a message string
     */
    public static String createMessage(String type, String... fields) {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        for (String field : fields) {
            sb.append("|");
            sb.append(field != null ? field : "");
        }
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * Parse a message string
     */
    public static String[] parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return new String[0];
        }
        // Remove trailing newline
        message = message.trim();
        return message.split("\\|", -1);
    }
    
    /**
     * Get message type from parsed message
     */
    public static String getMessageType(String[] parts) {
        return parts.length > 0 ? parts[0] : "";
    }
    
    /**
     * Get field from parsed message
     */
    public static String getField(String[] parts, int index) {
        return parts.length > index ? parts[index] : "";
    }
    
    /**
     * Get field as integer
     */
    public static int getFieldInt(String[] parts, int index, int defaultValue) {
        try {
            return Integer.parseInt(getField(parts, index));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Get field as long
     */
    public static long getFieldLong(String[] parts, int index, long defaultValue) {
        try {
            return Long.parseLong(getField(parts, index));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Validate message format
     */
    public static boolean isValidMessage(String message) {
        return message != null && !message.isEmpty() && message.contains("|");
    }
    
    // Private constructor
    private Protocol() {
        throw new AssertionError("Cannot instantiate Protocol class");
    }
}
