import { CONFIG } from './core/config.js';
import { UIManager } from './managers/ui-manager.js';
import { NetworkManager } from './managers/network-manager.js';
import { VoiceChatManager } from './managers/voice-chat-manager.js';
import { Renderer } from './core/renderer.js';
import { InputManager } from './core/input-manager.js';

class GameClient {
    constructor() {
        this.canvas = document.getElementById('game-canvas');
        this.minimapCanvas = document.getElementById('minimap');
        
        // Initialize managers
        this.uiManager = new UIManager(this);
        this.networkManager = new NetworkManager(this);
        this.voiceChatManager = new VoiceChatManager(this);
        this.renderer = new Renderer(this.canvas, this.minimapCanvas);
        this.inputManager = new InputManager(this);
        
        // Game state
        this.playerId = null;
        this.myPlayer = null;
        this.players = {};
        this.bullets = {};
        this.powerUps = {};
        
        // Player stats
        this.kills = 0;
        this.deaths = 0;
        this.health = 100;
        this.isAlive = true;
        
        // Weapon system
        this.lastFireTime = 0;
        this.heatLevel = 0;
        
        // Settings
        this.aimLineEnabled = false;
        this.playerName = null;
        this.serverAddress = null;
        this.soundEffectsEnabled = true;
        this.screenShakeEnabled = true;
        this.musicMuted = false;
        
        // FPS tracking
        this.fps = 0;
        this.frameCount = 0;
        this.lastFpsUpdate = Date.now();
        
        // Audio
        this.attackSound = new Audio('src/audio/attack.mp3');
        this.attackSound.volume = 0.4;
        this.battleMusic = new Audio('src/audio/battel.mp3');
        this.battleMusic.volume = 0.3;
        this.battleMusic.loop = true;
        this.deathSound = new Audio('src/audio/dead.mp3');
        this.deathSound.volume = 0.5;
        this.winSound = new Audio('src/audio/win.mp3');
        this.winSound.volume = 0.6;
        this.lossSound = new Audio('src/audio/loss.mp3');
        this.lossSound.volume = 0.6;
        this.clickSound = new Audio('src/audio/click.mp3');
        this.clickSound.volume = 0.3;
        
        this.init();
    }

    init() {
        this.inputManager.setupEventListeners(this.canvas);
        this.uiManager.setupJoinScreen();
        this.uiManager.setupLobbyScreen();
        this.voiceChatManager.init();
        
        // Start login music
        this.startLoginMusic();
    }

    startLoginMusic() {
        const loginMusic = document.getElementById('login-music');
        if (loginMusic) {
            loginMusic.volume = 0.3; // Set volume to 30%
            loginMusic.muted = this.musicMuted; // Apply current mute state
            loginMusic.play().catch(error => {
                console.log('Autoplay blocked by browser, music will play on user interaction');
                // Add click listener to play on first interaction
                const playOnInteraction = () => {
                    loginMusic.muted = this.musicMuted; // Apply mute state when user interaction starts music
                    loginMusic.play();
                    document.removeEventListener('click', playOnInteraction);
                    document.removeEventListener('keydown', playOnInteraction);
                };
                document.addEventListener('click', playOnInteraction);
                document.addEventListener('keydown', playOnInteraction);
            });
        }
    }

    stopLoginMusic() {
        const loginMusic = document.getElementById('login-music');
        if (loginMusic) {
            loginMusic.pause();
            loginMusic.currentTime = 0;
        }
    }

    startBattleMusic() {
        // Apply current sound effects mute state (battle music is part of sound effects)
        this.battleMusic.muted = !this.soundEffectsEnabled;
        this.battleMusic.play().catch(error => {
            console.log('Battle music autoplay failed:', error);
        });
    }

    toggleMusicMute() {
        this.musicMuted = !this.musicMuted;
        
        // Mute/unmute login music only (battle music is controlled by sound effects)
        const loginMusic = document.getElementById('login-music');
        if (loginMusic) {
            loginMusic.muted = this.musicMuted;
        }
        
        return this.musicMuted;
    }

    playClickSound() {
        if (!this.soundEffectsEnabled) return;
        this.clickSound.currentTime = 0;
        this.clickSound.play().catch(e => console.log('Click sound play failed'));
    }

    connectToLobby() {
        const serverAddress = this.serverAddress || 'localhost';
        this.networkManager.connectToLobby(serverAddress);
    }

    joinGame(name) {
        this.networkManager.closeLobby();
        
        const serverAddress = this.serverAddress || 'localhost';
        
        this.uiManager.showLoadingOverlay('Connecting to server...');
        
        this.networkManager.connectToGame(
            name,
            serverAddress,
            () => this.onGameConnected(name),
            (msg) => this.handleMessage(msg),
            () => this.onGameDisconnected(),
            (error) => this.onGameError(error)
        );
    }

    onGameConnected(name) {
        const joinScreen = document.getElementById('join-screen');
        const lobbyScreen = document.getElementById('lobby-screen');
        const gameHud = document.getElementById('game-hud');
        
        if (joinScreen) joinScreen.style.display = 'none';
        if (lobbyScreen) lobbyScreen.style.display = 'none';
        gameHud.style.display = 'block';
        gameHud.classList.add('active');
        
        this.uiManager.hideLoadingOverlay();
        this.uiManager.showNotification(`Welcome, ${name}!`, 'success');
        
        gameHud.setAttribute('aria-hidden', 'false');
        
        // Start ping monitoring
        this.networkManager.startPingMonitoring();
        
        // Clean up lobby event listeners
        if (this.uiManager.lobbyEnterHandler) {
            document.removeEventListener('keydown', this.uiManager.lobbyEnterHandler);
            this.uiManager.lobbyEnterHandler = null;
        }
        
        // Stop login music and start battle music
        this.stopLoginMusic();
        this.startBattleMusic();
        
        this.gameLoop();
    }

    onGameDisconnected() {
        this.networkManager.stopPingMonitoring();
        this.uiManager.hideLoadingOverlay();
        this.uiManager.showNotification('Connection lost. Refreshing in 3 seconds...', 'error');
        setTimeout(() => location.reload(), 3000);
    }

    onGameError(error) {
        this.uiManager.hideLoadingOverlay();
        this.uiManager.showNotification(error, 'error');
        
        const joinBtn = document.getElementById('join-btn');
        if (joinBtn) {
            joinBtn.classList.remove('loading');
            joinBtn.disabled = false;
        }
        
        const lobbyJoinBtn = document.getElementById('lobby-join-btn');
        if (lobbyJoinBtn) {
            lobbyJoinBtn.classList.remove('loading');
            lobbyJoinBtn.disabled = false;
        }
    }

    handleMessage(msg) {
        switch (msg.type) {
            case 'update':
                this.handleGameUpdate(msg);
                break;
            case 'chat':
                this.uiManager.addChatMessage(msg.msg);
                break;
            case 'respawn':
                if (msg.playerId === this.playerId) {
                    this.isAlive = true;
                    this.health = CONFIG.RESPAWN.HEALTH;
                    document.getElementById('respawn-overlay').style.display = 'none';
                }
                break;
            case 'hit':
                this.handleHit(msg);
                break;
            case 'game_over':
                this.handleGameOver(msg);
                break;
            case 'pong':
                this.networkManager.handlePong(msg.timestamp);
                break;
            case 'voice-offer':
                this.voiceChatManager.handleOffer(msg);
                break;
            case 'voice-answer':
                this.voiceChatManager.handleAnswer(msg);
                break;
            case 'voice-ice':
                this.voiceChatManager.handleIce(msg);
                break;
        }
    }

    handleGameUpdate(msg) {
        this.players = {};
        msg.players.forEach(p => {
            this.players[p.id] = p;
            if (p.id === this.playerId || (!this.playerId && p.name === this.playerName)) {
                this.playerId = p.id;
                this.myPlayer = p;
                this.kills = p.score || 0;
                this.health = p.health || 0;
                this.isAlive = p.alive;
                
                if (p.lastPowerUpCollectTime && (!this.myPlayer || 
                    this.myPlayer.lastPowerUpCollectTime !== p.lastPowerUpCollectTime)) {
                    this.handleBonusCollection(p);
                }
            }
        });
        
        this.bullets = {};
        msg.bullets.forEach(b => this.bullets[b.id] = b);
        
        this.powerUps = {};
        msg.powerUps.forEach(pu => this.powerUps[pu.id] = pu);
        
        this.updateHUD();
    }

    handleHit(msg) {
        if (msg.target === this.playerId) {
            // Only handle death (when health reaches 0)
            // Server manages health deduction
            this.deaths++;
            this.isAlive = false;
            this.uiManager.showRespawnScreen();
            this.renderer.createExplosion(this.myPlayer.x, this.myPlayer.y, CONFIG.EFFECTS.EXPLOSION_PARTICLES, '#ff0000');
            
            // Play death sound
            if (this.soundEffectsEnabled) {
                this.deathSound.currentTime = 0;
                this.deathSound.play().catch(e => console.log('Death sound play failed'));
            }
        }
        
        if (msg.shooter === this.playerId) {
            this.kills++;
            this.uiManager.showNotification('+1 KILL', 'success');
        }
        
        this.uiManager.addKillFeed(msg.shooter, msg.target, this.players);
    }

    handleGameOver(msg) {
        this.isAlive = false;
        
        // Play win or loss sound
        if (this.soundEffectsEnabled) {
            if (msg.winnerId === this.playerId) {
                this.winSound.currentTime = 0;
                this.winSound.play().catch(e => console.log('Win sound play failed'));
            } else {
                this.lossSound.currentTime = 0;
                this.lossSound.play().catch(e => console.log('Loss sound play failed'));
            }
        }
        
        this.uiManager.showGameOverOverlay(msg);
    }

    handleBonusCollection(player) {
        let bonusName = 'BONUS';
        let bonusEmoji = '✨';
        
        if (player.lastPowerUpType === 'SHIELD') {
            bonusName = 'SHIELD ACTIVATED';
            bonusEmoji = '🛡️';
        } else if (player.lastPowerUpType === 'DOUBLE_FIRE') {
            bonusName = 'DOUBLE FIRE!';
            bonusEmoji = '🔥';
        } else if (player.lastPowerUpType === 'SPEED_BOOST') {
            bonusName = 'SPEED BOOST!';
            bonusEmoji = '⚡';
        }
        
        this.uiManager.showNotification(`${bonusEmoji} ${bonusName} ${bonusEmoji}`, 'success');
    }

    tryFire() {
        const now = Date.now();
        const heatPercentage = (this.heatLevel / CONFIG.WEAPON.MAX_HEAT) * 100;
        
        let dynamicFireRate = CONFIG.WEAPON.FIRE_RATE;
        if (heatPercentage >= 100) {
            return;
        } else if (heatPercentage >= 80) {
            dynamicFireRate = CONFIG.WEAPON.FIRE_RATES.HEAT_80;
        } else if (heatPercentage >= 60) {
            dynamicFireRate = CONFIG.WEAPON.FIRE_RATES.HEAT_60;
        } else if (heatPercentage >= 40) {
            dynamicFireRate = CONFIG.WEAPON.FIRE_RATES.HEAT_40;
        }
        
        if (now - this.lastFireTime >= dynamicFireRate && this.isAlive) {
            this.heatLevel = Math.min(CONFIG.WEAPON.MAX_HEAT, this.heatLevel + CONFIG.WEAPON.HEAT_INCREASE);
            
            const angle = this.inputManager.getAngle();
            
            this.networkManager.sendMessage({ 
                type: 'fire', 
                angle: Math.round(angle),
                mouseX: Math.round(this.inputManager.mouseX),
                mouseY: Math.round(this.inputManager.mouseY),
                heatLevel: Math.round(this.heatLevel)
            });
            
            // Play attack sound
            if (this.soundEffectsEnabled) {
                this.attackSound.currentTime = 0; // Reset to start for rapid fire
                this.attackSound.play().catch(e => console.log('Attack sound play failed'));
            }
            
            this.lastFireTime = now;
            if (this.screenShakeEnabled) {
                this.renderer.screenShake();
            }
        }
    }

    sendMove() {
        const movement = this.inputManager.getMovement();
        if (!movement) return;
        
        const angle = this.inputManager.getAngle();
        
        if (movement.x !== this.myPlayer.x || movement.y !== this.myPlayer.y || angle !== this.myPlayer.angle) {
            this.networkManager.sendMessage({ 
                type: 'move', 
                x: Math.round(movement.x), 
                y: Math.round(movement.y), 
                angle: Math.round(angle) 
            });
        }
        
        if (this.inputManager.isSpacePressed()) {
            this.tryFire();
        }
    }

    sendChat() {
        const input = document.getElementById('chat-input');
        const msg = input.value.trim();
        if (msg) {
            this.networkManager.sendMessage({ type: 'chat', msg: msg });
            input.value = '';
        }
    }

    updateHUD() {
        this.uiManager.updateHUD(
            this.health,
            this.kills,
            this.deaths,
            this.myPlayer,
            this.heatLevel,
            CONFIG.WEAPON.MAX_HEAT,
            this.playerId,
            this.players
        );
    }

    gameLoop() {
        this.sendMove();
        this.renderer.updateParticles();
        
        // Calculate FPS
        this.frameCount++;
        const now = Date.now();
        const elapsed = now - this.lastFpsUpdate;
        
        // Update FPS every second
        if (elapsed >= 1000) {
            this.fps = Math.round((this.frameCount * 1000) / elapsed);
            this.frameCount = 0;
            this.lastFpsUpdate = now;
        }
        
        // Decay heat level
        this.heatLevel = Math.max(0, this.heatLevel - CONFIG.WEAPON.HEAT_DECAY_RATE);
        
        const angle = this.inputManager.getAngle();
        
        this.renderer.render(
            this.players,
            this.bullets,
            this.powerUps,
            this.myPlayer,
            this.playerId,
            this.inputManager.mouseX,
            this.inputManager.mouseY,
            angle,
            this.aimLineEnabled,
            this.heatLevel,
            CONFIG.WEAPON.MAX_HEAT
        );
        
        this.renderer.renderMinimap(this.players, this.playerId);
        this.updateHUD();
        
        requestAnimationFrame(() => this.gameLoop());
    }
}

// Start the game
document.addEventListener('DOMContentLoaded', () => {
    window.gameClient = new GameClient();
});
