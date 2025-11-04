package com.minitankfire.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.minitankfire.model.Player;
import com.minitankfire.model.Bullet;
import com.minitankfire.model.PowerUp;

public class Message {
    private static final Gson gson = new Gson();

    public static class JoinMessage {
        public String type = "join";
        public String name;
    }

    public static class MoveMessage {
        public String type = "move";
        public int x, y;
        public int angle;
    }

    public static class FireMessage {
        public String type = "fire";
    }

    public static class ChatMessage {
        public String type = "chat";
        public String msg;
    }

    public static class UpdateMessage {
        public String type = "update";
        public Player[] players;
        public Bullet[] bullets;
        public PowerUp[] powerUps;
    }

    public static class HitMessage {
        public String type = "hit";
        public String target;
        public String shooter;
    }

    public static class ScoreMessage {
        public String type = "score";
        public JsonObject scores;
    }

    public static class RespawnMessage {
        public String type = "respawn";
        public String playerId;
        public int x, y;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}