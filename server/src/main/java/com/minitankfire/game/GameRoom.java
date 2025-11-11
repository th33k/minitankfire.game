package com.minitankfire.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.minitankfire.model.Player;
import com.minitankfire.model.Bullet;
import com.minitankfire.model.PowerUp;
import com.minitankfire.network.ClientHandler;
import com.minitankfire.util.JsonUtil;

/**
 * Game Room - Core game logic and state management.
 * Manages all players, bullets, power-ups, and game physics.
 * Runs game loop in separate thread for real-time updates.
 */
public class GameRoom {
    // Game constants
    private static final int MAP_WIDTH = 1920;
    private static final int MAP_HEIGHT = 1080;
    private static final int PLAYER_SPEED = 12;
    private static final int PLAYER_BOOST_SPEED = 20;
    private static final int BULLET_SPEED = 50;
    private static final int GAME_TICK_MS = 50; // 20 FPS
    private static final int RESPAWN_TIME_MS = 3000;
    private static final int SHIELD_DURATION_MS = 5000;
    private static final int SPEED_BOOST_DURATION_MS = 3000;
    private static final int DOUBLE_FIRE_DURATION_MS = 10000;
    private static final int POWERUP_LIFETIME_MS = 10000;
    private static final int BULLET_LIFETIME_MS = 1500;

    // Game state
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, Bullet> bullets = new ConcurrentHashMap<>();
    private Map<String, PowerUp> powerUps = new ConcurrentHashMap<>();
    private Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private Random random = new Random();

    // Game loop
    private volatile boolean gameRunning = false;
    private Thread gameLoopThread;
    private long gameStartTime;
    private int winningScore = Integer.MAX_VALUE;
    private volatile boolean gameOver = false;

    public GameRoom() {
        gameStartTime = System.currentTimeMillis();
        gameRunning = true;
        startGameLoop();
    }

    public void setWinningScore(int winningScore) {
        this.winningScore = winningScore;
        System.out.println("[GAME] Winning score configured: " + winningScore);
    }

    // ========== Player Management ==========

    public void addPlayer(String playerId, String name, ClientHandler clientHandler) {
        Player player = new Player(playerId, name);
        player.setX(random.nextInt(MAP_WIDTH));
        player.setY(random.nextInt(MAP_HEIGHT));
        player.setAngle(0);
        players.put(playerId, player);
        clientHandlers.put(playerId, clientHandler);
        System.out.println("[GAME] Player '" + name + "' joined. Total: " + players.size());
        broadcastUpdate();
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        clientHandlers.remove(playerId);
        // Remove bullets owned by this player
        bullets.entrySet().removeIf(entry -> entry.getValue().getOwnerId().equals(playerId));
        broadcastUpdate();
    }

    // ========== Input Handling ==========

    public void handleMove(String playerId, int x, int y, int angle) {
        Player player = players.get(playerId);
        if (player != null && player.isAlive()) {
            player.setX(Math.max(0, Math.min(MAP_WIDTH, x)));
            player.setY(Math.max(0, Math.min(MAP_HEIGHT, y)));
            player.setAngle(angle);
        }
    }

    public void handleFire(String playerId, Map<String, String> data) {
        Player player = players.get(playerId);
        if (player != null && player.isAlive()) {
            int angle = player.getAngle();
            int heatLevel = 0;
            Integer mouseX = null;
            Integer mouseY = null;
            
            if (data != null) {
                if (data.containsKey("angle")) {
                    try {
                        angle = Integer.parseInt(data.get("angle"));
                    } catch (NumberFormatException e) {
                        // Use player's angle
                    }
                }
                if (data.containsKey("heatLevel")) {
                    try {
                        heatLevel = Integer.parseInt(data.get("heatLevel"));
                    } catch (NumberFormatException e) {
                        // Use default
                    }
                }
                // Parse mouse coordinates
                if (data.containsKey("mouseX")) {
                    try {
                        mouseX = Integer.parseInt(data.get("mouseX"));
                    } catch (NumberFormatException e) {
                        // Use angle-based trajectory
                    }
                }
                if (data.containsKey("mouseY")) {
                    try {
                        mouseY = Integer.parseInt(data.get("mouseY"));
                    } catch (NumberFormatException e) {
                        // Use angle-based trajectory
                    }
                }
            }
            
            createBullet(playerId, player, angle, heatLevel, mouseX, mouseY, 0);

            if (player.hasDoubleFire()) {
                // Fire second bullet with slight offset to create dual fire effect
                createBullet(playerId, player, angle, heatLevel, mouseX, mouseY, 15);
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

    private void createBullet(String playerId, Player player, int angle) {
        createBullet(playerId, player, angle, 0, null, null, 0);
    }

    private void createBullet(String playerId, Player player, int angle, int heatLevel) {
        createBullet(playerId, player, angle, heatLevel, null, null, 0);
    }

    private void createBullet(String playerId, Player player, int angle, int heatLevel, Integer mouseX, Integer mouseY) {
        createBullet(playerId, player, angle, heatLevel, mouseX, mouseY, 0);
    }

    private void createBullet(String playerId, Player player, int angle, int heatLevel, Integer mouseX, Integer mouseY, int angleOffset) {
        int dx, dy;
        
        // If mouse coordinates are provided, calculate trajectory towards mouse position
        if (mouseX != null && mouseY != null) {
            // Calculate direction vector from player to mouse
            double deltaX = mouseX - player.getX();
            double deltaY = mouseY - player.getY();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            
            // Normalize and scale by bullet speed
            if (distance > 0) {
                dx = (int) ((deltaX / distance) * BULLET_SPEED);
                dy = (int) ((deltaY / distance) * BULLET_SPEED);
            } else {
                // Fallback to angle-based calculation if mouse is exactly on player
                double rad = Math.toRadians(angle + angleOffset);
                dx = (int) (BULLET_SPEED * Math.cos(rad));
                dy = (int) (BULLET_SPEED * Math.sin(rad));
            }
        } else {
            // Fallback to angle-based calculation
            double rad = Math.toRadians(angle + angleOffset);
            dx = (int) (BULLET_SPEED * Math.cos(rad));
            dy = (int) (BULLET_SPEED * Math.sin(rad));
        }

        String bulletId = UUID.randomUUID().toString();
        
        // Calculate damage based on heat level
        // Base damage is 25, increases with heat level up to 40 at max heat (100)
        int damage = 25 + (heatLevel / 4); // 25 + up to 25 = 50 max damage
        
        Bullet bullet = new Bullet(bulletId, playerId, player.getX(), player.getY(), dx, dy, damage);
        bullets.put(bulletId, bullet);
    }

    // ========== Game State Updates ==========

    private void updateGameState() {
        updateBullets();
        checkCollisions();
        updatePowerUps();
        updatePlayerPowerUps();
        respawnDeadPlayers();
        broadcastUpdate();
    }

    private void updateBullets() {
        bullets.entrySet().removeIf(entry -> {
            Bullet bullet = entry.getValue();
            bullet.updatePosition();

            // Remove if expired or out of bounds
            if (bullet.isExpired() ||
                    bullet.getX() < 0 || bullet.getX() > MAP_WIDTH ||
                    bullet.getY() < 0 || bullet.getY() > MAP_HEIGHT) {
                return true;
            }
            return false;
        });
    }

    private void checkCollisions() {
        checkBulletPlayerCollisions();
        checkPowerUpCollisions();
    }

    private void checkBulletPlayerCollisions() {
        List<String> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets.values()) {
            for (Player player : players.values()) {
                if (isValidTarget(player, bullet)) {
                    handlePlayerHit(player, bullet);
                    bulletsToRemove.add(bullet.getId());
                    break;
                }
            }
        }

        bulletsToRemove.forEach(bullets::remove);
    }

    private boolean isValidTarget(Player player, Bullet bullet) {
        return player.isAlive() &&
                !player.getId().equals(bullet.getOwnerId()) &&
                Math.abs(bullet.getX() - player.getX()) < 20 &&
                Math.abs(bullet.getY() - player.getY()) < 20;
    }

    private void handlePlayerHit(Player player, Bullet bullet) {
        if (!player.hasShield()) {
            player.setAlive(false);
            player.setLastRespawnTime(System.currentTimeMillis());
            player.setScore(player.getScore() - 1);

            // Award point to shooter
            Player shooter = players.get(bullet.getOwnerId());
            if (shooter != null) {
                shooter.setScore(shooter.getScore() + 1);
                // Check winning condition
                if (!gameOver && shooter.getScore() >= winningScore) {
                    endGame(shooter);
                }
            }

            String hitMessage = JsonUtil.createHitMessage(player.getId(), bullet.getOwnerId());
            broadcastMessage(hitMessage);
        } else {
            player.setShield(false);
        }
    }

    private void endGame(Player winner) {
        gameOver = true;
        gameRunning = false; // stop game loop
        System.out.println("[GAME] Game over! Winner: " + (winner != null ? winner.getName() : "unknown"));

        // Build and broadcast game over message with leaderboard
        String gameOverMsg = JsonUtil.createGameOverMessage(winner.getId(), winner.getName(), players.values());
        broadcastMessage(gameOverMsg);
    }

    private void checkPowerUpCollisions() {
        for (Player player : players.values()) {
            if (player.isAlive()) {
                powerUps.entrySet().removeIf(entry -> {
                    PowerUp powerUp = entry.getValue();
                    if (Math.abs(powerUp.getX() - player.getX()) < 20 &&
                            Math.abs(powerUp.getY() - player.getY()) < 20) {
                        applyPowerUp(player, powerUp.getType());
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void updatePowerUps() {
        powerUps.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // Spawn power-ups randomly (0.5% chance per tick)
        if (random.nextInt(1000) < 5) {
            spawnPowerUp();
        }
    }

    private void updatePlayerPowerUps() {
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
    }

    private void respawnDeadPlayers() {
        long now = System.currentTimeMillis();
        for (Player player : players.values()) {
            if (!player.isAlive() && now - player.getLastRespawnTime() > RESPAWN_TIME_MS) {
                respawnPlayer(player);
            }
        }
    }

    private void applyPowerUp(Player player, PowerUp.Type type) {
        long now = System.currentTimeMillis();
        
        // Track power-up collection for animation
        player.setLastPowerUpCollectTime(now);
        player.setLastPowerUpType(type.toString());
        
        switch (type) {
            case SHIELD:
                player.setShield(true);
                player.setShieldEndTime(now + SHIELD_DURATION_MS);
                break;
            case SPEED_BOOST:
                player.setSpeedBoost(true);
                player.setSpeedBoostEndTime(now + SPEED_BOOST_DURATION_MS);
                break;
            case DOUBLE_FIRE:
                player.setDoubleFire(true);
                player.setDoubleFireEndTime(now + DOUBLE_FIRE_DURATION_MS);
                break;
        }
    }

    private void spawnPowerUp() {
        String id = UUID.randomUUID().toString();
        
        // Weighted randomization for better variety
        // Use weighted distribution: 30% Shield, 35% Speed Boost, 35% Double Fire
        int randomValue = random.nextInt(100);
        PowerUp.Type type;
        if (randomValue < 30) {
            type = PowerUp.Type.SHIELD;
        } else if (randomValue < 65) {
            type = PowerUp.Type.SPEED_BOOST;
        } else {
            type = PowerUp.Type.DOUBLE_FIRE;
        }
        
        // Randomize spawn location with better distribution
        int x = random.nextInt(MAP_WIDTH);
        int y = random.nextInt(MAP_HEIGHT);
        
        // Avoid spawning too close to edges
        x = Math.max(100, Math.min(MAP_WIDTH - 100, x));
        y = Math.max(100, Math.min(MAP_HEIGHT - 100, y));
        
        powerUps.put(id, new PowerUp(id, type, x, y));
    }

    private void respawnPlayer(Player player) {
        player.setAlive(true);
        player.setX(random.nextInt(MAP_WIDTH));
        player.setY(random.nextInt(MAP_HEIGHT));
        String respawnMessage = JsonUtil.createRespawnMessage(player.getId(), player.getX(), player.getY());
        broadcastMessage(respawnMessage);
    }

    // ========== Broadcasting ==========

    private void broadcastUpdate() {
        String updateMessage = JsonUtil.createUpdateMessage(
                players.values(),
                bullets.values(),
                powerUps.values());
        broadcastMessage(updateMessage);
    }

    private void broadcastMessage(String message) {
        for (ClientHandler handler : clientHandlers.values()) {
            try {
                if (handler.isConnected()) {
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                System.err.println("[BROADCAST_ERROR] " + e.getMessage());
            }
        }
    }

    public void sendToPlayer(String playerId, String message) {
        ClientHandler handler = clientHandlers.get(playerId);
        if (handler != null && handler.isConnected()) {
            try {
                handler.sendMessage(message);
            } catch (Exception e) {
                System.err.println("[SEND_ERROR] To player " + playerId.substring(0, 8) + ": " + e.getMessage());
            }
        }
    }

    // ========== Game Loop ==========

    private void startGameLoop() {
        gameLoopThread = new Thread(() -> {
            System.out.println("[GAME_LOOP] Started (20 FPS, " + GAME_TICK_MS + "ms per frame)");
            while (gameRunning) {
                try {
                    updateGameState();
                    Thread.sleep(GAME_TICK_MS);
                } catch (InterruptedException e) {
                    System.out.println("[GAME_LOOP] Interrupted");
                    break;
                }
            }
            System.out.println("[GAME_LOOP] Stopped");
        }, "GameLoop");
        gameLoopThread.setDaemon(false);
        gameLoopThread.start();
    }

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
