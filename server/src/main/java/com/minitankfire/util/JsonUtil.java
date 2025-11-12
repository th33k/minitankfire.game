package com.minitankfire.util;

import java.util.*;
import com.minitankfire.model.Player;
import com.minitankfire.model.Bullet;
import com.minitankfire.model.PowerUp;

/**
 * JSON utility class for serializing/deserializing game objects.
 * Uses only core Java APIs - no external JSON libraries.
 */
public class JsonUtil {

    /**
     * Escapes special characters in JSON strings
     */
    private static String escapeJson(String str) {
        if (str == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts a Player object to JSON string
     */
    public static String toJson(Player player) {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"x\":%d,\"y\":%d,\"angle\":%d,\"score\":%d," +
                        "\"alive\":%b,\"hasShield\":%b,\"speedBoost\":%b,\"doubleFire\":%b}",
                player.getId(), escapeJson(player.getName()), player.getX(), player.getY(),
                player.getAngle(), player.getScore(), player.isAlive(), player.hasShield(),
                player.hasSpeedBoost(), player.hasDoubleFire());
    }

    /**
     * Converts a Bullet object to JSON string
     */
    public static String toJson(Bullet bullet) {
        return String.format(
                "{\"id\":\"%s\",\"ownerId\":\"%s\",\"x\":%d,\"y\":%d,\"dx\":%d,\"dy\":%d}",
                bullet.getId(), bullet.getOwnerId(), bullet.getX(), bullet.getY(),
                bullet.getDx(), bullet.getDy());
    }

    /**
     * Converts a PowerUp object to JSON string
     */
    public static String toJson(PowerUp powerUp) {
        return String.format(
                "{\"id\":\"%s\",\"type\":\"%s\",\"x\":%d,\"y\":%d}",
                powerUp.getId(), powerUp.getType().name(), powerUp.getX(), powerUp.getY());
    }

    /**
     * Creates an update message with players, bullets, and powerups
     */
    public static String createUpdateMessage(Collection<Player> players,
            Collection<Bullet> bullets,
            Collection<PowerUp> powerUps) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"update\",\"players\":[");

        boolean first = true;
        for (Player player : players) {
            if (!first)
                sb.append(",");
            sb.append(toJson(player));
            first = false;
        }

        sb.append("],\"bullets\":[");
        first = true;
        for (Bullet bullet : bullets) {
            if (!first)
                sb.append(",");
            sb.append(toJson(bullet));
            first = false;
        }

        sb.append("],\"powerUps\":[");
        first = true;
        for (PowerUp powerUp : powerUps) {
            if (!first)
                sb.append(",");
            sb.append(toJson(powerUp));
            first = false;
        }

        sb.append("]}");
        return sb.toString();
    }

    /**
     * Creates a chat message
     */
    public static String createChatMessage(String msg) {
        return String.format("{\"type\":\"chat\",\"msg\":\"%s\"}", escapeJson(msg));
    }

    /**
     * Creates a hit message
     */
    public static String createHitMessage(String target, String shooter) {
        return String.format("{\"type\":\"hit\",\"target\":\"%s\",\"shooter\":\"%s\"}",
                target, shooter);
    }

    /**
     * Creates a respawn message
     */
    public static String createRespawnMessage(String playerId, int x, int y) {
        return String.format("{\"type\":\"respawn\",\"playerId\":\"%s\",\"x\":%d,\"y\":%d}",
                playerId, x, y);
    }

    /**
     * Parses a simple JSON string to extract key-value pairs
     * This is a simplified parser for the game's specific message format
     */
    public static Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();

        // Remove outer braces
        json = json.trim();
        if (json.startsWith("{"))
            json = json.substring(1);
        if (json.endsWith("}"))
            json = json.substring(0, json.length() - 1);

        // Simple state machine parser
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean inKey = true;
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escape) {
                if (inKey)
                    key.append(c);
                else
                    value.append(c);
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == ':') {
                    inKey = false;
                    continue;
                }
                if (c == ',') {
                    if (key.length() > 0) {
                        result.put(key.toString().trim(), value.toString().trim());
                    }
                    key = new StringBuilder();
                    value = new StringBuilder();
                    inKey = true;
                    continue;
                }
                if (Character.isWhitespace(c))
                    continue;
            }

            if (inKey) {
                key.append(c);
            } else {
                value.append(c);
            }
        }

        // Don't forget the last key-value pair
        if (key.length() > 0) {
            result.put(key.toString().trim(), value.toString().trim());
        }

        return result;
    }

    /**
     * Creates a game-over message containing the winner and a leaderboard summary
     */
    public static String createGameOverMessage(String winnerId, String winnerName, Collection<Player> players) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"game_over\",");
        sb.append(String.format("\"winnerId\":\"%s\",", winnerId));
        sb.append(String.format("\"winnerName\":\"%s\",", escapeJson(winnerName)));

        // Build leaderboard sorted by score desc
        List<Player> list = new ArrayList<>(players);
        list.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        sb.append("\"leaderboard\":[");
        boolean first = true;
        for (Player p : list) {
            if (!first)
                sb.append(',');
            sb.append(String.format("{\"name\":\"%s\",\"score\":%d}", escapeJson(p.getName()), p.getScore()));
            first = false;
        }
        sb.append("]}");

        return sb.toString();
    }

    /**
     * Creates a lobby info message containing player count, winning score, and
     * current leaderboard
     */
    public static String createLobbyInfoMessage(Collection<Player> players, int winningScore) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"lobby_info\",");
        sb.append(String.format("\"playerCount\":%d,", players.size()));
        sb.append(String.format("\"winningScore\":%d,", winningScore));

        // Build player list sorted by score desc
        List<Player> list = new ArrayList<>(players);
        list.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        sb.append("\"players\":[");
        boolean first = true;
        for (Player p : list) {
            if (!first)
                sb.append(',');
            sb.append(String.format("{\"name\":\"%s\",\"score\":%d}", escapeJson(p.getName()), p.getScore()));
            first = false;
        }
        sb.append("]}");

        return sb.toString();
    }
}
