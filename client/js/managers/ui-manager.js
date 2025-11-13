// UI Manager - Handles all UI interactions and updates
export class UIManager {
    constructor(gameClient) {
        this.game = gameClient;
        this.chatOpen = true;
        this.chatHistory = [];
        this.killFeed = [];
    }

    setupJoinScreen() {
        const joinForm = document.getElementById('join-form');
        const joinBtn = document.getElementById('join-btn');
        const playerNameInput = document.getElementById('player-name');
        const serverAddressInput = document.getElementById('server-address');
        
        const validateForm = () => {
            const name = playerNameInput.value.trim();
            const server = serverAddressInput.value.trim();
            const isValid = name.length > 0 && server.length > 0;
            joinBtn.disabled = !isValid;
            return isValid;
        };
        
        playerNameInput.addEventListener('input', validateForm);
        serverAddressInput.addEventListener('input', validateForm);
        validateForm();
        
        const handleSubmit = (e) => {
            if (e) e.preventDefault();
            
            const name = playerNameInput.value.trim();
            const server = serverAddressInput.value.trim();
            
            if (!name) {
                this.showNotification('Please enter a callsign!', 'error');
                playerNameInput.focus();
                return;
            }
            
            if (!server) {
                this.showNotification('Please enter a server address!', 'error');
                serverAddressInput.focus();
                return;
            }
            
            this.game.playerName = name;
            this.game.serverAddress = server;
            
            joinBtn.classList.add('loading');
            joinBtn.disabled = true;
            
            this.showLobbyScreen();
        };
        
        joinForm.addEventListener('submit', handleSubmit);
        joinBtn.addEventListener('click', (e) => {
            e.preventDefault();
            this.game.playClickSound();
            handleSubmit();
        });
        
        serverAddressInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (validateForm()) {
                    // Trigger form submission
                    joinForm.dispatchEvent(new Event('submit'));
                } else {
                    // If form is invalid, focus on the first invalid field
                    if (!serverAddressInput.value.trim()) {
                        serverAddressInput.focus();
                    } else if (!playerNameInput.value.trim()) {
                        playerNameInput.focus();
                    }
                }
            }
        });
        
        playerNameInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (validateForm()) {
                    // Trigger form submission instead of calling handleSubmit directly
                    joinForm.dispatchEvent(new Event('submit'));
                }
            }
        });
    }

    setupLobbyScreen() {
        const joinBtn = document.getElementById('lobby-join-btn');
        joinBtn.addEventListener('click', () => {
            this.game.playClickSound();
            if (this.game.playerName && this.game.serverAddress) {
                this.game.joinGame(this.game.playerName);
            } else {
                this.showNotification('Please complete callsign entry first!', 'error');
            }
        });

        // Add Enter key support for joining from lobby
        const handleLobbyEnter = (e) => {
            if (e.key === 'Enter' && !e.target.matches('input, textarea, select')) {
                e.preventDefault();
                joinBtn.click();
            }
        };

        // Add listener when lobby is shown
        document.addEventListener('keydown', handleLobbyEnter);
        
        // Store reference to remove listener later
        this.lobbyEnterHandler = handleLobbyEnter;
    }

    showLobbyScreen() {
        document.getElementById('join-screen').style.display = 'none';
        document.getElementById('lobby-screen').style.display = 'flex';
        this.game.connectToLobby();
    }

    updateLobbyDisplay(msg) {
        if (msg.winningScore) {
            document.getElementById('lobby-winning-score').textContent = msg.winningScore;
        }
        
        document.getElementById('lobby-player-count').textContent = msg.playerCount || 0;
        
        const scoresDiv = document.getElementById('lobby-scores');
        
        if (!msg.players || msg.players.length === 0) {
            scoresDiv.innerHTML = '<p class="no-players">No players in the game lobby</p>';
        } else {
            scoresDiv.innerHTML = msg.players
                .sort((a, b) => b.score - a.score)
                .slice(0, 10)
                .map((p, index) => `
                    <div class="lobby-score-item">
                        <span class="lobby-score-rank">#${index + 1}</span>
                        <span class="lobby-score-name">${p.name}</span>
                        <span class="lobby-score-value">${p.score}</span>
                    </div>
                `).join('');
        }
    }

    updateHUD(health, kills, deaths, myPlayer, heatLevel, maxHeatLevel, playerId, players) {
        const healthText = document.getElementById('health-text');
        const killsText = document.getElementById('kills-text');
        const deathsText = document.getElementById('deaths-text');
        const pingText = document.getElementById('ping-text');
        const fpsText = document.getElementById('fps-text');
        
        healthText.textContent = health;
        healthText.setAttribute('aria-label', `Health: ${health}`);
        
        killsText.textContent = kills;
        killsText.setAttribute('aria-label', `Kills: ${kills}`);
        
        deathsText.textContent = deaths;
        deathsText.setAttribute('aria-label', `Deaths: ${deaths}`);
        
        // Update ping display
        const ping = this.game.networkManager.getPing();
        if (pingText) {
            // Show "..." if ping is exactly 0 (not yet measured)
            if (ping === 0 && this.game.networkManager.lastPingTimestamp !== null) {
                pingText.textContent = '...';
            } else {
                pingText.textContent = ping;
            }
            pingText.setAttribute('aria-label', `Ping: ${ping} milliseconds`);
            
            // Color code ping based on quality
            const pingContainer = pingText.parentElement;
            if (pingContainer) {
                pingContainer.classList.remove('ping-good', 'ping-medium', 'ping-bad');
                if (ping === 0) {
                    // Neutral color while waiting for first pong
                    pingContainer.classList.add('ping-medium');
                } else if (ping < 50) {
                    pingContainer.classList.add('ping-good');
                } else if (ping < 100) {
                    pingContainer.classList.add('ping-medium');
                } else {
                    pingContainer.classList.add('ping-bad');
                }
            }
        }
        
        // Update FPS display
        const fps = this.game.fps || 0;
        if (fpsText) {
            fpsText.textContent = fps;
            fpsText.setAttribute('aria-label', `FPS: ${fps}`);
            
            // Color code FPS based on performance
            const fpsContainer = fpsText.parentElement;
            if (fpsContainer) {
                fpsContainer.classList.remove('fps-good', 'fps-medium', 'fps-bad');
                if (fps >= 50) {
                    fpsContainer.classList.add('fps-good');
                } else if (fps >= 30) {
                    fpsContainer.classList.add('fps-medium');
                } else if (fps > 0) {
                    fpsContainer.classList.add('fps-bad');
                }
            }
        }
        
        this.updatePowerUpIndicator(myPlayer);
        this.updateHeatBar(heatLevel, maxHeatLevel);
        this.updateLeaderboard(players, playerId);
    }

    updatePowerUpIndicator(myPlayer) {
        const indicator = document.getElementById('power-up-indicator');
        if (!myPlayer) return;
        
        let powerUpHTML = '';
        let ariaLabel = '';
        
        if (myPlayer.shield) {
            powerUpHTML = '<div class="powerup-item shield-active"><i class="fas fa-shield-alt" aria-hidden="true"></i><div class="powerup-label">SHIELD</div></div>';
            ariaLabel = 'Shield active';
        } else if (myPlayer.speedBoost) {
            powerUpHTML = '<div class="powerup-item speed-boost-active"><i class="fas fa-bolt" aria-hidden="true"></i><div class="powerup-label">SPEED</div></div>';
            ariaLabel = 'Speed boost active';
        } else if (myPlayer.doubleFire) {
            powerUpHTML = '<div class="powerup-item double-fire-active"><i class="fas fa-fire" aria-hidden="true"></i><div class="powerup-label">DOUBLE FIRE</div></div>';
            ariaLabel = 'Double fire active';
        }
        
        if (powerUpHTML) {
            indicator.innerHTML = powerUpHTML;
            indicator.style.display = 'flex';
            indicator.setAttribute('aria-label', ariaLabel);
        } else {
            indicator.style.display = 'flex';
            indicator.setAttribute('aria-label', 'No active power-ups');
        }
    }

    updateHeatBar(heatLevel, maxHeatLevel) {
        const heatPercent = Math.round((heatLevel / maxHeatLevel) * 100);
        const heatColor = heatPercent < 30 ? '#00ff88' : heatPercent < 70 ? '#ffaa00' : '#ff4444';
        
        const heatBar = document.getElementById('heat-bar-vertical');
        if (!heatBar) return;
        
        heatBar.setAttribute('aria-valuenow', heatPercent);
        heatBar.setAttribute('aria-label', `Weapon heat level: ${heatPercent} percent`);
        
        const segments = 160;
        const filledSegments = Math.ceil((heatPercent / 100) * segments);
        let segmentsHTML = '';
        
        for (let i = 0; i < segments; i++) {
            const isActive = i < filledSegments;
            const segmentColor = isActive ? heatColor : 'rgba(255,255,255,0.05)';
            segmentsHTML += `<div class="heat-bar-segment" style="background-color: ${segmentColor};"></div>`;
        }
        
        heatBar.innerHTML = segmentsHTML;
        heatBar.style.borderColor = heatColor;
        
        const label = document.createElement('div');
        label.style.cssText = 'position: absolute; font-size: 0.8rem; color: ' + heatColor + '; top: 12px; text-align: center; width: 100%; font-weight: bold;';
        label.textContent = heatPercent + '%';
        label.setAttribute('aria-hidden', 'true');
        heatBar.appendChild(label);
    }

    updateLeaderboard(players, playerId) {
        const scoresDiv = document.getElementById('scores');
        const sortedPlayers = Object.values(players)
            .sort((a, b) => b.score - a.score)
            .slice(0, 10);
        
        scoresDiv.innerHTML = sortedPlayers.map((p, index) => {
            const isCurrentPlayer = p.id === playerId;
            const ariaLabel = `${index + 1}. ${p.name}: ${p.score} points${isCurrentPlayer ? ' (you)' : ''}`;
            
            return `
                <div class="score-item ${isCurrentPlayer ? 'self' : ''}" role="listitem" aria-label="${ariaLabel}">
                    <span class="score-rank" aria-hidden="true">#${index + 1}</span>
                    <span class="score-name">${p.name}</span>
                    <span class="score-value">${p.score}</span>
                </div>
            `;
        }).join('');
    }

    addChatMessage(msg) {
        this.chatHistory.push(msg);
        if (this.chatHistory.length > 50) this.chatHistory.shift();
        
        const messagesDiv = document.getElementById('chat-messages');
        const msgEl = document.createElement('div');
        msgEl.className = 'chat-message';
        
        const parts = msg.split(':');
        if (parts.length >= 2) {
            msgEl.innerHTML = `<span class="sender">${parts[0]}:</span><span class="text">${parts.slice(1).join(':')}</span>`;
        } else {
            msgEl.innerHTML = `<span class="text">${msg}</span>`;
        }
        
        messagesDiv.appendChild(msgEl);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
        
        while (messagesDiv.children.length > 20) {
            messagesDiv.removeChild(messagesDiv.firstChild);
        }
    }

    addKillFeed(shooterId, targetId, players) {
        const shooter = players[shooterId];
        const target = players[targetId];
        
        if (!shooter || !target) return;
        
        const feedEl = document.createElement('div');
        feedEl.className = 'kill-notification';
        feedEl.innerHTML = `
            <span class="killer">${shooter.name}</span>
            <i class="fas fa-skull"></i>
            <span class="victim">${target.name}</span>
        `;
        
        const feedDiv = document.getElementById('kill-feed');
        feedDiv.appendChild(feedEl);
        
        setTimeout(() => feedEl.remove(), 5000);
    }

    showRespawnScreen() {
        const overlay = document.getElementById('respawn-overlay');
        overlay.style.display = 'flex';
        
        let countdown = 3;
        const timerEl = document.getElementById('respawn-timer');
        timerEl.textContent = countdown;
        
        const interval = setInterval(() => {
            countdown--;
            if (countdown > 0) {
                timerEl.textContent = countdown;
            } else {
                clearInterval(interval);
                overlay.style.display = 'none';
            }
        }, 1000);
    }

    showGameOverOverlay(msg) {
        let existing = document.getElementById('game-over-overlay');
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = 'game-over-overlay';
        overlay.style.cssText = 'position: fixed; left: 0; top: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.75); z-index: 9999;';

        const card = document.createElement('div');
        card.style.cssText = 'background: #111; color: #fff; padding: 30px; border-radius: 8px; text-align: center; min-width: 320px;';

        const title = document.createElement('h2');
        title.style.marginTop = '0';
        if (msg.winnerId === this.game.playerId) {
            title.textContent = 'YOU WON!';
            title.style.color = '#00ff88';
        } else {
            title.textContent = (msg.winnerName ? msg.winnerName : 'A player') + ' won the game';
            title.style.color = '#ffcc00';
        }

        const subtitle = document.createElement('p');
        subtitle.textContent = 'Final leaderboard:';

        const list = document.createElement('div');
        list.style.cssText = 'text-align: left; max-height: 200px; overflow: auto; margin: 10px 0;';

        if (Array.isArray(msg.leaderboard)) {
            msg.leaderboard.forEach((entry, idx) => {
                const el = document.createElement('div');
                el.style.cssText = 'padding: 6px 0; border-bottom: 1px solid rgba(255,255,255,0.06);';
                el.innerHTML = `<strong>#${idx + 1}</strong> ${entry.name} <span style="float:right">${entry.score}</span>`;
                list.appendChild(el);
            });
        }

        const btn = document.createElement('button');
        btn.textContent = 'Return / Reload';
        btn.style.marginTop = '12px';
        btn.className = 'btn-primary';
        btn.addEventListener('click', () => location.reload());

        card.appendChild(title);
        card.appendChild(subtitle);
        card.appendChild(list);
        card.appendChild(btn);
        overlay.appendChild(card);
        document.body.appendChild(overlay);
    }

    showNotification(message, type = 'info') {
        const notifDiv = document.getElementById('notifications');
        const notif = document.createElement('div');
        notif.className = `notification ${type}`;
        notif.textContent = message;
        notif.setAttribute('role', 'alert');
        
        notifDiv.appendChild(notif);
        
        setTimeout(() => {
            notif.style.animation = 'fadeOut 0.5s ease forwards';
            setTimeout(() => notif.remove(), 500);
        }, 3000);
    }
    
    showLoadingOverlay(text = 'Loading...') {
        const overlay = document.getElementById('loading-overlay');
        const loadingText = document.getElementById('loading-text');
        
        if (overlay) {
            loadingText.textContent = text;
            overlay.classList.add('active');
            overlay.setAttribute('aria-busy', 'true');
        }
    }
    
    hideLoadingOverlay() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.classList.remove('active');
            overlay.setAttribute('aria-busy', 'false');
        }
    }

    toggleSettings(aimLineEnabled, onAimLineChange) {
        let settingsPanel = document.getElementById('settings-panel');
        
        if (!settingsPanel) {
            settingsPanel = document.createElement('div');
            settingsPanel.id = 'settings-panel';
            settingsPanel.innerHTML = `
                <div class="settings-header">
                    <h3>SETTINGS</h3>
                    <button id="close-settings" class="icon-btn-small" aria-label="Close settings">×</button>
                </div>
                <div class="settings-content">
                    <div class="setting-item">
                        <label for="aim-line-toggle" class="setting-label">
                            <i class="fas fa-crosshairs"></i> Aim Line
                        </label>
                        <label class="toggle-switch">
                            <input type="checkbox" id="aim-line-toggle" ${aimLineEnabled ? 'checked' : ''}>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>
                    <div class="setting-item">
                        <label for="sound-effects-toggle" class="setting-label">
                            <i class="fas fa-volume-up"></i> Sound Effects
                        </label>
                        <label class="toggle-switch">
                            <input type="checkbox" id="sound-effects-toggle" checked>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>
                    <div class="setting-item">
                        <label for="screen-shake-toggle" class="setting-label">
                            <i class="fas fa-bomb"></i> Screen Shake
                        </label>
                        <label class="toggle-switch">
                            <input type="checkbox" id="screen-shake-toggle" checked>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>
                </div>
            `;
            
            const gameHud = document.getElementById('game-hud');
            if (gameHud) {
                gameHud.appendChild(settingsPanel);
            } else {
                document.body.appendChild(settingsPanel);
            }
            
            document.getElementById('close-settings').addEventListener('click', (e) => {
                e.stopPropagation();
                settingsPanel.remove();
            });
            
            // Set initial checkbox states
            document.getElementById('aim-line-toggle').checked = this.game.aimLineEnabled;
            document.getElementById('sound-effects-toggle').checked = this.game.soundEffectsEnabled;
            document.getElementById('screen-shake-toggle').checked = this.game.screenShakeEnabled;
            
            document.getElementById('aim-line-toggle').addEventListener('change', (e) => {
                onAimLineChange(e.target.checked);
                this.showNotification(`Aim line ${e.target.checked ? 'enabled' : 'disabled'}`, 'info');
            });
            
            document.getElementById('sound-effects-toggle').addEventListener('change', (e) => {
                this.game.soundEffectsEnabled = e.target.checked;
                // Also control battle music as part of sound effects
                this.game.battleMusic.muted = !e.target.checked;
                this.game.inputManager.updateSoundToggleIcon();
                this.showNotification(`Sound effects ${e.target.checked ? 'enabled' : 'disabled'}`, 'info');
            });
            
            document.getElementById('screen-shake-toggle').addEventListener('change', (e) => {
                this.game.screenShakeEnabled = e.target.checked;
                this.showNotification(`Screen shake ${e.target.checked ? 'enabled' : 'disabled'}`, 'info');
            });
            
            setTimeout(() => {
                document.addEventListener('click', this.closeSettingsOutside = (e) => {
                    if (!settingsPanel.contains(e.target) && e.target.id !== 'settings-btn') {
                        settingsPanel.remove();
                        if (this.closeSettingsOutside) {
                            document.removeEventListener('click', this.closeSettingsOutside);
                        }
                    }
                });
            }, 0);
        } else {
            settingsPanel.remove();
            if (this.closeSettingsOutside) {
                document.removeEventListener('click', this.closeSettingsOutside);
            }
        }
    }

    updateSoundEffectsVolume() {
        // This method can be used to update volumes if needed in the future
        // For now, sounds are muted by not playing them when disabled
    }
}
