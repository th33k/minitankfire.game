import { CONFIG } from '../core/config.js';

// Input Manager - Handles keyboard and mouse input
export class InputManager {
    constructor(gameClient) {
        this.game = gameClient;
        this.keys = {};
        this.mouseX = 0;
        this.mouseY = 0;
    }

    setupEventListeners(canvas) {
        this.setupKeyboardEvents();
        this.setupMouseEvents(canvas);
        this.setupUIButtonEvents();
    }

    setupKeyboardEvents() {
        document.addEventListener('keydown', (e) => {
            this.keys[e.code] = true;
            
            if (e.code === 'Enter') {
                this.handleEnterKey(e);
            }
            
            if (e.code === 'Escape') {
                this.handleEscapeKey();
            }
        });
        
        document.addEventListener('keyup', (e) => {
            this.keys[e.code] = false;
        });
    }

    handleEnterKey(e) {
        const joinScreen = document.getElementById('join-screen');
        const playerNameInput = document.getElementById('player-name');
        const serverAddressInput = document.getElementById('server-address');
        
        if (joinScreen && joinScreen.style.display !== 'none') {
            if (document.activeElement === playerNameInput || 
                document.activeElement === serverAddressInput) {
                return;
            }
        }
        
        const chatInput = document.getElementById('chat-input');
        if (document.activeElement === chatInput) {
            this.game.sendChat();
            chatInput.blur();
        } else if (chatInput) {
            chatInput.focus();
        }
        
        e.preventDefault();
    }

    handleEscapeKey() {
        const chatInput = document.getElementById('chat-input');
        if (chatInput) {
            chatInput.blur();
        }
    }

    setupMouseEvents(canvas) {
        canvas.addEventListener('mousemove', (e) => {
            const rect = canvas.getBoundingClientRect();
            const scaleX = canvas.width / rect.width;
            const scaleY = canvas.height / rect.height;
            
            this.mouseX = (e.clientX - rect.left) * scaleX;
            this.mouseY = (e.clientY - rect.top) * scaleY;
        });

        canvas.addEventListener('click', () => {
            this.game.tryFire();
        });
    }

    setupUIButtonEvents() {
        const voiceBtn = document.getElementById('voice-toggle');
        if (voiceBtn) {
            voiceBtn.addEventListener('click', () => {
                this.game.playClickSound();
                
                // Toggle music mute
                const musicMuted = this.game.toggleMusicMute();
                
                // Update voice button icon to reflect music mute state
                this.updateVoiceToggleIcon();
                
                // Toggle voice chat
                this.game.voiceChatManager.toggle((msg, type) => {
                    this.game.uiManager.showNotification(msg, type);
                });
                
                // Show notification about music state
                this.game.uiManager.showNotification(
                    `Music ${musicMuted ? 'muted' : 'unmuted'}`, 
                    'info'
                );
            });
            // Initialize icon state
            this.updateVoiceToggleIcon();
        }
        
        const sendBtn = document.getElementById('send-btn');
        if (sendBtn) {
            sendBtn.addEventListener('click', () => {
                this.game.playClickSound();
                this.game.sendChat();
            });
        }
        
        const chatToggleBtn = document.getElementById('chat-toggle');
        if (chatToggleBtn) {
            chatToggleBtn.addEventListener('click', () => {
                this.game.playClickSound();
                this.game.uiManager.chatOpen = !this.game.uiManager.chatOpen;
                const chatPanel = document.getElementById('chat-panel');
                chatPanel.style.height = this.game.uiManager.chatOpen ? 'auto' : '45px';
                document.getElementById('chat-messages').style.display = this.game.uiManager.chatOpen ? 'block' : 'none';
                document.querySelector('.chat-input-wrapper').style.display = this.game.uiManager.chatOpen ? 'flex' : 'none';
            });
        }
        
        const soundBtn = document.getElementById('sound-toggle');
        if (soundBtn) {
            soundBtn.addEventListener('click', () => {
                // Always play click sound for feedback, even when sound effects are disabled
                if (this.game.clickSound) {
                    this.game.clickSound.currentTime = 0;
                    this.game.clickSound.play().catch(e => console.log('Click sound play failed'));
                }
                
                this.game.soundEffectsEnabled = !this.game.soundEffectsEnabled;
                // Also control battle music as part of sound effects
                this.game.battleMusic.muted = !this.game.soundEffectsEnabled;
                this.updateSoundToggleIcon();
                this.game.uiManager.showNotification(
                    `Sound effects ${this.game.soundEffectsEnabled ? 'enabled' : 'disabled'}`, 
                    'info'
                );
            });
            // Initialize icon state
            this.updateSoundToggleIcon();
        }
        
        const settingsBtn = document.getElementById('settings-btn');
        if (settingsBtn) {
            settingsBtn.addEventListener('click', () => {
                this.game.playClickSound();
                this.game.uiManager.toggleSettings(
                    this.game.aimLineEnabled,
                    (enabled) => { this.game.aimLineEnabled = enabled; }
                );
            });
        }
    }

    updateSoundToggleIcon() {
        const soundBtn = document.getElementById('sound-toggle');
        if (soundBtn) {
            const icon = soundBtn.querySelector('i');
            if (icon) {
                if (this.game.soundEffectsEnabled) {
                    icon.className = 'fas fa-volume-up';
                    soundBtn.setAttribute('aria-pressed', 'true');
                } else {
                    icon.className = 'fas fa-volume-mute';
                    soundBtn.setAttribute('aria-pressed', 'false');
                }
            }
        }
    }

    updateVoiceToggleIcon() {
        const voiceBtn = document.getElementById('voice-toggle');
        if (voiceBtn) {
            const icon = voiceBtn.querySelector('i');
            if (icon) {
                if (this.game.musicMuted) {
                    icon.className = 'fas fa-microphone-slash';
                    voiceBtn.setAttribute('aria-pressed', 'true');
                } else {
                    icon.className = 'fas fa-microphone';
                    voiceBtn.setAttribute('aria-pressed', 'false');
                }
            }
        }
    }

    getAngle() {
        if (this.game.myPlayer) {
            return Math.atan2(
                this.mouseY - this.game.myPlayer.y,
                this.mouseX - this.game.myPlayer.x
            ) * 180 / Math.PI;
        }
        return 0;
    }

    getMovement() {
        if (!this.game.myPlayer || !this.game.isAlive) return null;
        
        let x = this.game.myPlayer.x;
        let y = this.game.myPlayer.y;
        const speed = this.game.myPlayer.speedBoost ? CONFIG.PLAYER.SPEED_BOOSTED : CONFIG.PLAYER.SPEED_NORMAL;

        if (this.keys['KeyW'] || this.keys['ArrowUp']) y -= speed;
        if (this.keys['KeyS'] || this.keys['ArrowDown']) y += speed;
        if (this.keys['KeyA'] || this.keys['ArrowLeft']) x -= speed;
        if (this.keys['KeyD'] || this.keys['ArrowRight']) x += speed;

        x = Math.max(CONFIG.PLAYER.MIN_X, Math.min(CONFIG.PLAYER.MAX_X, x));
        y = Math.max(CONFIG.PLAYER.MIN_Y, Math.min(CONFIG.PLAYER.MAX_Y, y));

        return { x, y };
    }

    isSpacePressed() {
        return this.keys['Space'];
    }
}
