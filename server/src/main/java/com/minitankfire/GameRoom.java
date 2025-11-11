package com.minitankfire;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Game room that manages all game state and logic
 * Demonstrates concurrent programming with thread-safe collections
 * Uses multi-threading for game loop
 */
public class GameRoom {
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final int PLAYER_SPEED = 3;
    private static final int BULLET_SPEED = 8;

    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, Bullet> bullets = new ConcurrentHashMap<>();
    private Map<String, PowerUp> powerUps = new ConcurrentHashMap<>();
    private Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private Random random = new Random();
    private long gameStartTime;
    private volatile boolean gameRunning = false;
    private Thread gameLoopThread;

    public GameRoom() {
        gameStartTime = System.currentTimeMillis();
        gameRunning = true;
        startGameLoop();
    }

    public void addPlayer(String playerId, String name, ClientHandler clientHandler) {
        Player player = new Player(playerId, name);
        player.setX(random.nextInt(MAP_WIDTH));
        player.setY(random.nextInt(MAP_HEIGHT));
        player.setAngle(0);
        players.put(playerId, player);
        clientHandlers.put(playerId, clientHandler);
        System.out.println("[GAME] Player '" + name + "' joined. Total players: " + players.size());
        broadcastUpdate();
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        clientHandlers.remove(playerId);
        // Remove bullets owned by this player
        bullets.entrySet().removeIf(entry -> entry.getValue().getOwnerId().equals(playerId));
        broadcastUpdate();
    }

    public void handleMove(String playerId, int x, int y, int angle) {
        Player player = players.get(playerId);
        if (player != null && player.isAlive()) {
            player.setX(Math.max(0, Math.min(MAP_WIDTH, x)));
            player.setY(Math.max(0, Math.min(MAP_HEIGHT, y)));
            player.setAngle(angle);
        }
    }

    public void handleFire(String playerId) {
        Player player = players.get(playerId);
        if (player != null && player.isAlive()) {
            double rad = Math.toRadians(player.getAngle());
            int dx = (int) (BULLET_SPEED * Math.cos(rad));
            int dy = (int) (BULLET_SPEED * Math.sin(rad));

            String bulletId = UUID.randomUUID().toString();
            Bullet bullet = new Bullet(bulletId, playerId, player.getX(), player.getY(), dx, dy);
            bullets.put(bulletId, bullet);

            if (player.hasDoubleFire()) {
                // Fire second bullet slightly offset
                String bulletId2 = UUID.randomUUID().toString();
                Bullet bullet2 = new Bullet(bulletId2, playerId, player.getX(), player.getY(), dx, dy);
                bullets.put(bulletId2, bullet2);
            }
        }
    }

    public void handleChat(String playerId, String msg) {
        Player player = players.get(playerId);
        if (player != null) {
            String chatMessage = JsonUtil.createChatMessage(player.getName() + ": " + msg);
            broadcastMessage(chatMessage);
        }
    }

    private void updateGameState() {
        // Update bullets
        bullets.entrySet().removeIf(entry -> {
            Bullet bullet = entry.getValue();
            bullet.updatePosition();
            if (bullet.isExpired() || bullet.getX() < 0 || bullet.getX() > MAP_WIDTH || bullet.getY() < 0
                    || bullet.getY() > MAP_HEIGHT) {
                return true;
            }
            return false;
        });

        // Check collisions
        checkCollisions();

        // Update power-ups
        powerUps.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // Spawn power-ups randomly
        if (random.nextInt(1000) < 5) { // 0.5% chance per tick
            spawnPowerUp();
        }

        // Update player power-ups
        long now = System.currentTimeMillis();
        for (Player player : players.values()) {
            if (player.hasShield() && now > player.getShieldEndTime()) {
                player.setShield(false);
            }
            if (player.hasSpeedBoost() && now > player.getSpeedBoostEndTime()) {
                player.setSpeedBoost(false);
            }
            if (player.hasDoubleFire() && now > player.getDoubleFireEndTime()) {
                player.setDoubleFire(false);
            }
        }

        // Respawn dead players
        for (Player player : players.values()) {
            if (!player.isAlive() && now - player.getLastRespawnTime() > 3000) { // 3 seconds
                respawnPlayer(player);
            }
        }

        // Broadcast update
        broadcastUpdate();
    }

    private void checkCollisions() {
        List<String> bulletsToRemove = new ArrayList<>();
        List<String> playersToHit = new ArrayList<>();

        for (Bullet bullet : bullets.values()) {
            for (Player player : players.values()) {
                if (player.isAlive() && !player.getId().equals(bullet.getOwnerId())) {
                    if (Math.abs(bullet.getX() - player.getX()) < 20 && Math.abs(bullet.getY() - player.getY()) < 20) {
                        if (!player.hasShield()) {
                            player.setAlive(false);
                            player.setLastRespawnTime(System.currentTimeMillis());
                            player.setScore(player.getScore() - 1);
                            playersToHit.add(player.getId());

                            // Award point to shooter
                            Player shooter = players.get(bullet.getOwnerId());
                            if (shooter != null) {
                                shooter.setScore(shooter.getScore() + 1);
                            }

                            // Broadcast hit message
                            String hitMessage = JsonUtil.createHitMessage(player.getId(), bullet.getOwnerId());
                            broadcastMessage(hitMessage);
                        } else {
                            player.setShield(false);
                        }
                        bulletsToRemove.add(bullet.getId());
                        break;
                    }
                }
            }
        }

        // Remove bullets
        for (String bulletId : bulletsToRemove) {
            bullets.remove(bulletId);
        }

        // Check power-up collection
        for (Player player : players.values()) {
            if (player.isAlive()) {
                powerUps.entrySet().removeIf(entry -> {
                    PowerUp powerUp = entry.getValue();
                    if (Math.abs(powerUp.getX() - player.getX()) < 20
                            && Math.abs(powerUp.getY() - player.getY()) < 20) {
                        applyPowerUp(player, powerUp.getType());
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void applyPowerUp(Player player, PowerUp.Type type) {
        long now = System.currentTimeMillis();
        switch (type) {
            case SHIELD:
                player.setShield(true);
                player.setShieldEndTime(now + 5000);
                break;
            case SPEED_BOOST:
                player.setSpeedBoost(true);
                player.setSpeedBoostEndTime(now + 3000);
                break;
            case DOUBLE_FIRE:
                player.setDoubleFire(true);
                player.setDoubleFireEndTime(now + 10000);
                break;
        }
    }

    private void spawnPowerUp() {
        String id = UUID.randomUUID().toString();
        PowerUp.Type type = PowerUp.Type.values()[random.nextInt(PowerUp.Type.values().length)];
        int x = random.nextInt(MAP_WIDTH);
        int y = random.nextInt(MAP_HEIGHT);
        powerUps.put(id, new PowerUp(id, type, x, y));
    }

    private void respawnPlayer(Player player) {
        player.setAlive(true);
        player.setX(random.nextInt(MAP_WIDTH));
        player.setY(random.nextInt(MAP_HEIGHT));
        String respawnMessage = JsonUtil.createRespawnMessage(player.getId(), player.getX(), player.getY());
        broadcastMessage(respawnMessage);
    }

    private void broadcastUpdate() {
        String updateMessage = JsonUtil.createUpdateMessage(
                players.values(),
                bullets.values(),
                powerUps.values());
        broadcastMessage(updateMessage);
    }

    /**
     * Broadcasts a message to all connected clients
     * Demonstrates concurrent message distribution
     */
    private void broadcastMessage(String message) {
        for (ClientHandler handler : clientHandlers.values()) {
            try {
                if (handler.isConnected()) {
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                System.err.println("[GAME] Error broadcasting to client: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a message to a specific player
     */
    public void sendToPlayer(String playerId, String message) {
        ClientHandler handler = clientHandlers.get(playerId);
        if (handler != null && handler.isConnected()) {
            try {
                handler.sendMessage(message);
            } catch (Exception e) {
                System.err.println("Error sending to player " + playerId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Starts the game loop in a separate thread
     * Demonstrates multi-threading for game state updates
     */
    private void startGameLoop() {
        gameLoopThread = new Thread(() -> {
            System.out.println("Game loop started");
            while (gameRunning) {
                updateGameState();
                try {
                    Thread.sleep(50); // 20 FPS (50ms per frame)
                } catch (InterruptedException e) {
                    System.out.println("Game loop interrupted");
                    break;
                }
            }
            System.out.println("Game loop stopped");
        }, "GameLoop");
        gameLoopThread.setDaemon(false);
        gameLoopThread.start();
    }

    /**
     * Stops the game room and all related threads
     */
    public void stop() {
        gameRunning = false;
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
        }
        // Disconnect all clients
        for (ClientHandler handler : clientHandlers.values()) {
            handler.stop();
        }
        clientHandlers.clear();
        players.clear();
        bullets.clear();
        powerUps.clear();
    }
}