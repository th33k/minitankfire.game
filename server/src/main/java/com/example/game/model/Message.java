package com.example.game.model;

import java.io.Serializable;

/**
 * Represents a network message sent between client and server.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        PLAYER_UPDATE, BULLET_UPDATE, POWERUP_UPDATE, CHAT, GAME_STATE, ERROR
    }

    private MessageType type;
    private String senderId;
    private long timestamp;
    private Object payload;

    public Message(MessageType type, String senderId) {
        this.type = type;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(MessageType type, String senderId, Object payload) {
        this(type, senderId);
        this.payload = payload;
    }

    public MessageType getType() { return type; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
    public Object getPayload() { return payload; }
}