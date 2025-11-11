package com.example.game.server;

import com.example.game.model.Bullet;
import com.example.game.model.GameObject;
import com.example.game.model.Player;
import com.example.game.model.PowerUp;
import com.example.game.util.LoggerUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Manages the overall game state including players, bullets, and power-ups.
 * Uses concurrent collections for thread safety.
 */
public class GameStateManager {
    private final ConcurrentHashMap<String, Player> players;
    private final CopyOnWriteArrayList<Bullet> bullets;
    private final CopyOnWriteArrayList<PowerUp> powerUps;
    private final CopyOnWriteArrayList<Consumer<GameStateManager>> updateListeners;

    public GameStateManager() {
        this.players = new ConcurrentHashMap<>();
        this.bullets = new CopyOnWriteArrayList<>();
        this.powerUps = new CopyOnWriteArrayList<>();
        this.updateListeners = new CopyOnWriteArrayList<>();
        LoggerUtil.info("Game state manager initialized");
    }

    /**
     * Adds an update listener that gets notified after each game update.
     */
    public void addUpdateListener(Consumer<GameStateManager> listener) {
        updateListeners.add(listener);
    }

    /**
     * Removes an update listener.
     */
    public void removeUpdateListener(Consumer<GameStateManager> listener) {
        updateListeners.remove(listener);
    }

    /**
     * Adds a new player to the game.
     */
    public void addPlayer(Player player) {
        players.put(player.getId(), player);
        LoggerUtil.info("Player added: " + player.getName() + " (ID: " + player.getId() + ")");
    }

    /**
     * Removes a player from the game.
     */
    public void removePlayer(String playerId) {
        Player player = players.remove(playerId);
        if (player != null) {
            LoggerUtil.info("Player removed: " + player.getName());
        }
    }

    /**
     * Gets a player by ID.
     */
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    /**
     * Gets all players.
     */
    public ConcurrentHashMap<String, Player> getAllPlayers() {
        return new ConcurrentHashMap<>(players);
    }

    /**
     * Adds a bullet to the game.
     */
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
        LoggerUtil.debug("Bullet added from player: " + bullet.getOwnerId());
    }

    /**
     * Removes a bullet from the game.
     */
    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
    }

    /**
     * Gets all bullets.
     */
    public CopyOnWriteArrayList<Bullet> getAllBullets() {
        return new CopyOnWriteArrayList<>(bullets);
    }

    /**
     * Adds a power-up to the game.
     */
    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
        LoggerUtil.debug("Power-up added: " + powerUp.getType());
    }

    /**
     * Removes a power-up from the game.
     */
    public void removePowerUp(PowerUp powerUp) {
        powerUps.remove(powerUp);
    }

    /**
     * Gets all power-ups.
     */
    public CopyOnWriteArrayList<PowerUp> getAllPowerUps() {
        return new CopyOnWriteArrayList<>(powerUps);
    }

    /**
     * Updates the game state (called by game loop).
     */
    public void update() {
        // Update bullets
        bullets.removeIf(bullet -> {
            bullet.update();
            return bullet.isExpired();
        });

        // Update power-ups
        powerUps.removeIf(PowerUp::isExpired);

        // Update players (power-up effects, etc.)
        players.values().forEach(player -> {
            // Check for expired power-ups
            long now = System.currentTimeMillis();
            if (player.hasShield() && now > player.getShieldEndTime()) {
                player.setShield(false);
            }
            if (player.hasSpeedBoost() && now > player.getSpeedBoostEndTime()) {
                player.setSpeedBoost(false);
            }
            if (player.hasDoubleFire() && now > player.getDoubleFireEndTime()) {
                player.setDoubleFire(false);
            }
        });

        // Check collisions, etc. (simplified)
        checkCollisions();

        // Notify listeners of game state update
        for (Consumer<GameStateManager> listener : updateListeners) {
            try {
                listener.accept(this);
            } catch (Exception e) {
                LoggerUtil.error("Error notifying update listener", e);
            }
        }
    }

    /**
     * Checks for collisions between game objects.
     */
    private void checkCollisions() {
        // Bullet vs Player collisions
        for (Bullet bullet : bullets) {
            for (Player player : players.values()) {
                if (!player.getId().equals(bullet.getOwnerId()) && isColliding(bullet, player)) {
                    // Handle hit
                    LoggerUtil.info("Player " + player.getName() + " hit by bullet from " + bullet.getOwnerId());
                    removeBullet(bullet);
                    // Could respawn player, etc.
                    break;
                }
            }
        }

        // Player vs Power-up collisions
        for (PowerUp powerUp : powerUps) {
            for (Player player : players.values()) {
                if (isColliding(powerUp, player)) {
                    // Apply power-up
                    applyPowerUp(player, powerUp);
                    removePowerUp(powerUp);
                    break;
                }
            }
        }
    }

    private boolean isColliding(GameObject obj1, GameObject obj2) {
        int dx = obj1.getX() - obj2.getX();
        int dy = obj1.getY() - obj2.getY();
        int distance = (int) Math.sqrt(dx * dx + dy * dy);
        return distance < 20; // Simple collision radius
    }

    private void applyPowerUp(Player player, PowerUp powerUp) {
        long now = System.currentTimeMillis();
        switch (powerUp.getType()) {
            case SHIELD:
                player.setShield(true);
                player.setShieldEndTime(now + 10000); // 10 seconds
                break;
            case SPEED_BOOST:
                player.setSpeedBoost(true);
                player.setSpeedBoostEndTime(now + 5000); // 5 seconds
                break;
            case DOUBLE_FIRE:
                player.setDoubleFire(true);
                player.setDoubleFireEndTime(now + 8000); // 8 seconds
                break;
        }
        LoggerUtil.info("Player " + player.getName() + " collected " + powerUp.getType());
    }

    /**
     * Adds a WebSocket player to the game.
     */
    public String addWebSocketPlayer(String clientAddress) {
        String playerId = "ws_" + System.currentTimeMillis() + "_" + clientAddress.hashCode();
        Player player = new Player(playerId, "Player");
        player.setX(600); // Default position
        player.setY(400);
        player.setAngle(0);
        addPlayer(player);
        return playerId;
    }

    /**
     * Sets the name of a WebSocket player.
     */
    public void setPlayerName(String playerId, String name) {
        Player player = players.get(playerId);
        if (player != null) {
            player.setName(name);
            LoggerUtil.info("Player " + playerId + " renamed to " + name);
        }
    }

    /**
     * Updates a player's position and angle.
     */
    public void updatePlayerPosition(String playerId, double x, double y, double angle) {
        Player player = players.get(playerId);
        if (player != null) {
            player.setX((int) x);
            player.setY((int) y);
            player.setAngle((int) angle);
        }
    }

    /**
     * Handles a player firing a bullet.
     */
    public void playerFire(String playerId) {
        Player player = players.get(playerId);
        if (player != null && player.isAlive()) {
            // Create bullet
            double bulletX = player.getX() + Math.cos(Math.toRadians(player.getAngle())) * 20;
            double bulletY = player.getY() + Math.sin(Math.toRadians(player.getAngle())) * 20;
            double speed = 10; // Bullet speed
            int dx = (int) (Math.cos(Math.toRadians(player.getAngle())) * speed);
            int dy = (int) (Math.sin(Math.toRadians(player.getAngle())) * speed);

            Bullet bullet = new Bullet("bullet_" + System.nanoTime(), playerId, (int) bulletX, (int) bulletY, dx, dy);
            addBullet(bullet);
        }
    }

    /**
     * Adds a chat message from a player.
     */
    public void addChatMessage(String playerId, String message) {
        Player player = players.get(playerId);
        if (player != null) {
            LoggerUtil.info("Chat: " + player.getName() + ": " + message);
            // In a full implementation, you'd broadcast this to all clients
        }
    }

    /**
     * Gets the current game state for WebSocket clients.
     */
    public GameState getGameState() {
        return new GameState(
            new ConcurrentHashMap<>(players),
            new CopyOnWriteArrayList<>(bullets),
            new CopyOnWriteArrayList<>(powerUps)
        );
    }

    /**
     * Simple data class for game state.
     */
    public static class GameState {
        public final ConcurrentHashMap<String, Player> players;
        public final CopyOnWriteArrayList<Bullet> bullets;
        public final CopyOnWriteArrayList<PowerUp> powerUps;

        public GameState(ConcurrentHashMap<String, Player> players,
                        CopyOnWriteArrayList<Bullet> bullets,
                        CopyOnWriteArrayList<PowerUp> powerUps) {
            this.players = players;
            this.bullets = bullets;
            this.powerUps = powerUps;
        }
    }
}