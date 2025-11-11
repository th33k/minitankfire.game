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
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final int PLAYER_SPEED = 3;
    private static final int BULLET_SPEED = 8;
    private static final int GAME_TICK_MS = 50; // 20 FPS
    private static final int RESPAWN_TIME_MS = 3000;
    private static final int SHIELD_DURATION_MS = 5000;
    private static final int SPEED_BOOST_DURATION_MS = 3000;
    private static final int DOUBLE_FIRE_DURATION_MS = 10000;
    private static final int POWERUP_LIFETIME_MS = 10000;
    private static final int BULLET_LIFETIME_MS = 3000;

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

    public GameRoom() {
        gameStartTime = System.currentTimeMillis();
        gameRunning = true;
        startGameLoop();
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

    public void handleFire(String playerId) {
        Player player = players.get(playerId);
        if (player != null && player.isAlive()) {
            createBullet(playerId, player);
            
            if (player.hasDoubleFire()) {
                createBullet(playerId, player); // Fire second bullet
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

    private void createBullet(String playerId, Player player) {
        double rad = Math.toRadians(player.getAngle());
        int dx = (int) (BULLET_SPEED * Math.cos(rad));
        int dy = (int) (BULLET_SPEED * Math.sin(rad));

        String bulletId = UUID.randomUUID().toString();
        Bullet bullet = new Bullet(bulletId, playerId, player.getX(), player.getY(), dx, dy);
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
            }

            String hitMessage = JsonUtil.createHitMessage(player.getId(), bullet.getOwnerId());
            broadcastMessage(hitMessage);
        } else {
            player.setShield(false);
        }
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
