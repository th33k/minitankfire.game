package com.example.game.model;

import java.io.Serializable;

/**
 * Represents a game action performed by a player.
 */
public class GameAction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ActionType {
        MOVE, FIRE, CHAT, JOIN, LEAVE
    }

    private String playerId;
    private ActionType type;
    private long timestamp;
    private Object data; // Additional data like position, message, etc.

    public GameAction(String playerId, ActionType type) {
        this.playerId = playerId;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public GameAction(String playerId, ActionType type, Object data) {
        this(playerId, type);
        this.data = data;
    }

    public String getPlayerId() { return playerId; }
    public ActionType getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public Object getData() { return data; }
}