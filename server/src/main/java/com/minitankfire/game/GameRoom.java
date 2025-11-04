package com.minitankfire.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.minitankfire.model.Player;
import com.minitankfire.model.Bullet;
import com.minitankfire.model.PowerUp;
import com.minitankfire.protocol.Message;

public class GameRoom {
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final int PLAYER_SPEED = 3;
    private static final int BULLET_SPEED = 8;

    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, Bullet> bullets = new ConcurrentHashMap<>();
    private Map<String, PowerUp> powerUps = new ConcurrentHashMap<>();
    private Map<String, org.eclipse.jetty.websocket.api.Session> sessions = new ConcurrentHashMap<>();
    private Random random = new Random();
    private long gameStartTime;
    private boolean gameRunning = false;

    public GameRoom() {
        gameStartTime = System.currentTimeMillis();
        gameRunning = true;
        startGameLoop();
    }

    public void addPlayer(String playerId, String name, org.eclipse.jetty.websocket.api.Session session) {
        Player player = new Player(playerId, name);
        player.setX(random.nextInt(MAP_WIDTH));
        player.setY(random.nextInt(MAP_HEIGHT));
        player.setAngle(0);
        players.put(playerId, player);
        sessions.put(playerId, session);
        broadcastUpdate();
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        sessions.remove(playerId);
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
            Message.ChatMessage chatMsg = new Message.ChatMessage();
            chatMsg.msg = player.getName() + ": " + msg;
            broadcastMessage(Message.toJson(chatMsg));
        }
    }

    private void updateGameState() {
        // Update bullets
        bullets.entrySet().removeIf(entry -> {
            Bullet bullet = entry.getValue();
            bullet.updatePosition();
            if (bullet.isExpired() || bullet.getX() < 0 || bullet.getX() > MAP_WIDTH || bullet.getY() < 0 || bullet.getY() > MAP_HEIGHT) {
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
                            Message.HitMessage hitMsg = new Message.HitMessage();
                            hitMsg.target = player.getId();
                            hitMsg.shooter = bullet.getOwnerId();
                            broadcastMessage(Message.toJson(hitMsg));
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
                    if (Math.abs(powerUp.getX() - player.getX()) < 20 && Math.abs(powerUp.getY() - player.getY()) < 20) {
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
        Message.RespawnMessage respawnMsg = new Message.RespawnMessage();
        respawnMsg.playerId = player.getId();
        respawnMsg.x = player.getX();
        respawnMsg.y = player.getY();
        broadcastMessage(Message.toJson(respawnMsg));
    }

    private void broadcastUpdate() {
        Message.UpdateMessage updateMsg = new Message.UpdateMessage();
        updateMsg.players = players.values().toArray(new Player[0]);
        updateMsg.bullets = bullets.values().toArray(new Bullet[0]);
        updateMsg.powerUps = powerUps.values().toArray(new PowerUp[0]);
        broadcastMessage(Message.toJson(updateMsg));
    }

    private void broadcastMessage(String message) {
        for (org.eclipse.jetty.websocket.api.Session session : sessions.values()) {
            try {
                session.getRemote().sendString(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void sendToPlayer(String playerId, String message) {
        org.eclipse.jetty.websocket.api.Session session = sessions.get(playerId);
        if (session != null) {
            try {
                session.getRemote().sendString(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startGameLoop() {
        Thread gameLoop = new Thread(() -> {
            while (gameRunning) {
                updateGameState();
                try {
                    Thread.sleep(50); // 20 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        gameLoop.start();
    }

    public void stop() {
        gameRunning = false;
    }
}