package com.minitankfire;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebSocket
public class GameWebSocket {
    private static GameRoom gameRoom = new GameRoom();
    private String playerId;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.playerId = java.util.UUID.randomUUID().toString();
        System.out.println("Player connected: " + playerId);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "join":
                    String name = json.get("name").getAsString();
                    gameRoom.addPlayer(playerId, name, session);
                    break;
                case "move":
                    int x = json.get("x").getAsInt();
                    int y = json.get("y").getAsInt();
                    int angle = json.get("angle").getAsInt();
                    gameRoom.handleMove(playerId, x, y, angle);
                    break;
                case "fire":
                    gameRoom.handleFire(playerId);
                    break;
                case "chat":
                    String msg = json.get("msg").getAsString();
                    gameRoom.handleChat(playerId, msg);
                    break;
                case "voice-offer":
                case "voice-answer":
                case "voice-ice":
                    // Forward voice chat signaling messages
                    String target = json.get("target").getAsString();
                    json.addProperty("from", playerId);
                    gameRoom.sendToPlayer(target, json.toString());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Player disconnected: " + playerId);
        gameRoom.removePlayer(playerId);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}