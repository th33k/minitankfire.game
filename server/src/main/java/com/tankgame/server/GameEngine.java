package com.tankgame.server;

import com.tankgame.common.*;
import com.tankgame.model.*;
import com.tankgame.network.*;

import java.util.*;

/**
 * Core game engine that handles game logic and physics
 */
public class GameEngine implements Runnable {
    private GameState gameState;
    private NetworkManager networkManager;
    private boolean running;
    private Thread gameThread;
    
    public GameEngine(NetworkManager networkManager) {
        this.gameState = new GameState();
        this.networkManager = networkManager;
        this.running = false;
    }
    
    /**
     * Start the game engine
     */
    public void start() {
        if (!running) {
            running = true;
            gameThread = new Thread(this, "GameEngine");
            gameThread.start();
            System.out.println("[GameEngine] Started");
        }
    }
    
    /**
     * Stop the game engine
     */
    public void stop() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[GameEngine] Stopped");
    }
    
    /**
     * Main game loop
     */
    @Override
    public void run() {
        long lastUpdate = System.currentTimeMillis();
        
        while (running) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastUpdate;
            
            if (deltaTime >= Constants.GAME_TICK_RATE) {
                update();
                lastUpdate = currentTime;
            }
            
            try {
                Thread.sleep(5); // Small sleep to prevent CPU spinning
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Update game state
     */
    private void update() {
        // Update bullets
        gameState.updateBullets();
        
        // Check collisions
        checkCollisions();
        
        // Update power-ups
        gameState.updatePowerUps();
        
        // Spawn new power-ups
        spawnPowerUps();
        
        // Check for respawns
        checkRespawns();
        
        // Broadcast game state to all clients
        broadcastGameState();
    }
    
    /**
     * Check all collision types
     */
    private void checkCollisions() {
        checkBulletTankCollisions();
        checkPowerUpCollisions();
    }
    
    /**
     * Check bullet-tank collisions
     */
    private void checkBulletTankCollisions() {
        List<String> bulletsToRemove = new ArrayList<>();
        
        for (Bullet bullet : gameState.getAllBullets()) {
            for (Tank tank : gameState.getAllTanks()) {
                // Skip if bullet owner or tank is dead
                if (bullet.getOwnerId().equals(tank.getId()) || !tank.isAlive()) {
                    continue;
                }
                
                // Check collision
                if (Utils.circleCollision(
                    bullet.getPosition().getX(), bullet.getPosition().getY(), 
                    Constants.COLLISION_RADIUS_BULLET,
                    tank.getPosition().getX(), tank.getPosition().getY(), 
                    Constants.COLLISION_RADIUS_TANK)) {
                    
                    // Apply damage
                    int healthBefore = tank.getHealth();
                    tank.takeDamage(Constants.BULLET_DAMAGE);
                    
                    // Notify hit
                    networkManager.broadcast(MessageBuilder.buildHit(
                        tank.getId(), bullet.getOwnerId(), Constants.BULLET_DAMAGE));
                    
                    // Check for kill
                    if (!tank.isAlive() && healthBefore > 0) {
                        Tank shooter = gameState.getTank(bullet.getOwnerId());
                        if (shooter != null) {
                            shooter.kill();
                        }
                        networkManager.broadcast(MessageBuilder.buildKill(
                            bullet.getOwnerId(), tank.getId()));
                    }
                    
                    bulletsToRemove.add(bullet.getId());
                    break;
                }
            }
        }
        
        // Remove collided bullets
        for (String bulletId : bulletsToRemove) {
            gameState.removeBullet(bulletId);
        }
    }
    
    /**
     * Check power-up collisions with tanks
     */
    private void checkPowerUpCollisions() {
        List<String> powerUpsToRemove = new ArrayList<>();
        
        for (PowerUp powerUp : gameState.getAllPowerUps()) {
            for (Tank tank : gameState.getAllTanks()) {
                if (!tank.isAlive()) continue;
                
                // Check collision
                if (Utils.circleCollision(
                    powerUp.getPosition().getX(), powerUp.getPosition().getY(), 
                    Constants.COLLISION_RADIUS_POWERUP,
                    tank.getPosition().getX(), tank.getPosition().getY(), 
                    Constants.COLLISION_RADIUS_TANK)) {
                    
                    // Apply power-up effect
                    applyPowerUp(tank, powerUp);
                    
                    // Notify collection
                    networkManager.broadcast(MessageBuilder.buildPowerUpCollect(
                        powerUp.getId(), tank.getId(), powerUp.getType()));
                    
                    powerUpsToRemove.add(powerUp.getId());
                    break;
                }
            }
        }
        
        // Remove collected power-ups
        for (String powerUpId : powerUpsToRemove) {
            gameState.removePowerUp(powerUpId);
        }
    }
    
    /**
     * Apply power-up effect to tank
     */
    private void applyPowerUp(Tank tank, PowerUp powerUp) {
        switch (powerUp.getType()) {
            case SHIELD:
                tank.activateShield();
                break;
            case SPEED_BOOST:
                tank.activateSpeedBoost();
                break;
            case DOUBLE_FIRE:
                tank.activateDoubleFire();
                break;
        }
    }
    
    /**
     * Spawn power-ups randomly
     */
    private void spawnPowerUps() {
        if (Utils.randomDouble() < Constants.POWERUP_SPAWN_CHANCE) {
            int x = Utils.randomInt(50, Constants.MAP_WIDTH - 50);
            int y = Utils.randomInt(50, Constants.MAP_HEIGHT - 50);
            
            PowerUp.Type[] types = PowerUp.Type.values();
            PowerUp.Type type = types[Utils.randomInt(0, types.length)];
            
            PowerUp powerUp = new PowerUp(Utils.generateId(), type, x, y);
            gameState.addPowerUp(powerUp);
        }
    }
    
    /**
     * Check and handle tank respawns
     */
    private void checkRespawns() {
        long currentTime = System.currentTimeMillis();
        
        for (Tank tank : gameState.getAllTanks()) {
            if (!tank.isAlive() && 
                (currentTime - tank.getLastRespawnTime()) >= Constants.TANK_RESPAWN_TIME) {
                
                // Find spawn position
                int x = Utils.randomInt(50, Constants.MAP_WIDTH - 50);
                int y = Utils.randomInt(50, Constants.MAP_HEIGHT - 50);
                
                // Respawn tank
                tank.respawn(x, y);
                
                // Notify clients
                networkManager.broadcast(MessageBuilder.buildRespawn(
                    tank.getId(), x, y));
            }
        }
    }
    
    /**
     * Broadcast current game state to all clients
     */
    private void broadcastGameState() {
        String updateMessage = MessageBuilder.buildUpdate(
            gameState.getAllTanks(),
            gameState.getAllBullets(),
            gameState.getAllPowerUps()
        );
        networkManager.broadcast(updateMessage);
    }
    
    // Public methods for handling player actions
    
    public String addPlayer(String playerId, String playerName) {
        int x = Utils.randomInt(50, Constants.MAP_WIDTH - 50);
        int y = Utils.randomInt(50, Constants.MAP_HEIGHT - 50);
        
        Tank tank = new Tank(playerId, playerName, x, y);
        gameState.addTank(tank);
        
        System.out.println("[GameEngine] Player joined: " + playerName + " (" + playerId + ")");
        return playerId;
    }
    
    public void removePlayer(String playerId) {
        gameState.removeTank(playerId);
        System.out.println("[GameEngine] Player left: " + playerId);
    }
    
    public void handleMove(String playerId, int x, int y, int angle) {
        Tank tank = gameState.getTank(playerId);
        if (tank != null && tank.isAlive()) {
            // Clamp position to map bounds
            x = Utils.clamp(x, 0, Constants.MAP_WIDTH);
            y = Utils.clamp(y, 0, Constants.MAP_HEIGHT);
            
            tank.getPosition().setX(x);
            tank.getPosition().setY(y);
            tank.getPosition().setAngle(angle);
        }
    }
    
    public void handleFire(String playerId, int angle) {
        Tank tank = gameState.getTank(playerId);
        if (tank != null && tank.canFire()) {
            tank.setLastFireTime(System.currentTimeMillis());
            
            // Create bullet
            Bullet bullet = new Bullet(
                Utils.generateId(),
                playerId,
                tank.getPosition().getX(),
                tank.getPosition().getY(),
                angle
            );
            gameState.addBullet(bullet);
            
            // If double fire is active, create second bullet
            if (tank.hasDoubleFire()) {
                Bullet bullet2 = new Bullet(
                    Utils.generateId(),
                    playerId,
                    tank.getPosition().getX(),
                    tank.getPosition().getY(),
                    angle + 10 // Slight angle offset
                );
                gameState.addBullet(bullet2);
            }
        }
    }
    
    public void handleChat(String playerId, String message) {
        Tank tank = gameState.getTank(playerId);
        if (tank != null) {
            String chatMessage = tank.getPlayerName() + ": " + message;
            networkManager.broadcast(MessageBuilder.buildChat(chatMessage));
        }
    }
    
    public GameState getGameState() {
        return gameState;
    }
}
