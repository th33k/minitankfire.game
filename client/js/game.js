class GameClient {
    constructor() {
        this.canvas = document.getElementById('game-canvas');
        this.ctx = this.canvas.getContext('2d');
        this.minimapCanvas = document.getElementById('minimap');
        this.minimapCtx = this.minimapCanvas ? this.minimapCanvas.getContext('2d') : null;
        
        this.ws = null;
        this.lobbyWs = null;
        this.playerId = null;
        this.myPlayer = null;
        this.players = {};
        this.bullets = {};
        this.powerUps = {};
        this.particles = [];
        this.keys = {};
        this.mouseX = 0;
        this.mouseY = 0;
        this.angle = 0;
        this.lastFireTime = 0;
        this.fireRate = 500; // ms between shots
        
        // Stats
        this.kills = 0;
        this.deaths = 0;
        this.health = 100;
        this.isAlive = true;
        this.respawnTime = 0;
        
        // Heat level system (for damage scaling)
        this.heatLevel = 0; // 0-100, increases with each shot
        this.maxHeatLevel = 100;
        this.heatDecayRate = 0.2; // per frame when not firing (reduced from 0.5 for harder gameplay)
        this.baseDamage = 25; // Damage per bullet
        
        // Voice chat
        this.peerConnections = {};
        this.localStream = null;
        this.voiceEnabled = false;
        
        // UI state
        this.chatOpen = true;
        this.chatHistory = [];
        this.killFeed = [];
        
        // Settings
        this.aimLineEnabled = false; // Default off as requested
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupCallsignScreen();
        this.setupLobbyScreen();
        this.initVoiceChat();
    }

    setupEventListeners() {
        // Keyboard
        document.addEventListener('keydown', (e) => {
            this.keys[e.code] = true;
            
            // Chat toggle (only when in game, not on join screen)
            if (e.code === 'Enter') {
                const joinScreen = document.getElementById('join-screen');
                const playerNameInput = document.getElementById('player-name');
                const serverAddressInput = document.getElementById('server-address');
                
                // If on join screen, let the join screen handle Enter key
                if (joinScreen && joinScreen.style.display !== 'none') {
                    // If focused on player name or server address, let their keypress handlers work
                    if (document.activeElement === playerNameInput || 
                        document.activeElement === serverAddressInput) {
                        return;
                    }
                }
                
                const chatInput = document.getElementById('chat-input');
                if (document.activeElement === chatInput) {
                    this.sendChat();
                    chatInput.blur();
                } else if (chatInput) {
                    chatInput.focus();
                }
                e.preventDefault();
            }
            
            // ESC to close chat input
            if (e.code === 'Escape') {
                const chatInput = document.getElementById('chat-input');
                if (chatInput) {
                    chatInput.blur();
                }
            }
        });
        
        document.addEventListener('keyup', (e) => {
            this.keys[e.code] = false;
        });

        // Mouse
        this.canvas.addEventListener('mousemove', (e) => {
            const rect = this.canvas.getBoundingClientRect();
            // Account for canvas scaling due to CSS or browser zoom
            const scaleX = this.canvas.width / rect.width;
            const scaleY = this.canvas.height / rect.height;
            
            this.mouseX = (e.clientX - rect.left) * scaleX;
            this.mouseY = (e.clientY - rect.top) * scaleY;
            
            if (this.myPlayer) {
                this.angle = Math.atan2(
                    this.mouseY - this.myPlayer.y,
                    this.mouseX - this.myPlayer.x
                ) * 180 / Math.PI;
            }
        });

        // Mouse click to fire
        this.canvas.addEventListener('click', () => {
            this.tryFire();
        });
        
        // Voice toggle
        const voiceBtn = document.getElementById('voice-toggle');
        if (voiceBtn) {
            voiceBtn.addEventListener('click', () => {
                this.toggleVoice();
            });
        }
        
        // Send chat button
        const sendBtn = document.getElementById('send-btn');
        if (sendBtn) {
            sendBtn.addEventListener('click', () => {
                this.sendChat();
            });
        }
        
        // Chat toggle button
        const chatToggleBtn = document.getElementById('chat-toggle');
        if (chatToggleBtn) {
            chatToggleBtn.addEventListener('click', () => {
                this.chatOpen = !this.chatOpen;
                const chatPanel = document.getElementById('chat-panel');
                chatPanel.style.height = this.chatOpen ? 'auto' : '45px';
                document.getElementById('chat-messages').style.display = this.chatOpen ? 'block' : 'none';
                document.querySelector('.chat-input-wrapper').style.display = this.chatOpen ? 'flex' : 'none';
            });
        }
        
        // Settings button
        const settingsBtn = document.getElementById('settings-btn');
        if (settingsBtn) {
            settingsBtn.addEventListener('click', () => {
                this.toggleSettings();
            });
        }
    }

    setupJoinScreen() {
        const joinForm = document.getElementById('join-form');
        const joinBtn = document.getElementById('join-btn');
        const playerNameInput = document.getElementById('player-name');
        const serverAddressInput = document.getElementById('server-address');
        
        // Form validation
        const validateForm = () => {
            const name = playerNameInput.value.trim();
            const server = serverAddressInput.value.trim();
            const isValid = name.length > 0 && server.length > 0;
            
            joinBtn.disabled = !isValid;
            return isValid;
        };
        
        // Real-time validation
        playerNameInput.addEventListener('input', validateForm);
        serverAddressInput.addEventListener('input', validateForm);
        
        // Initial validation
        validateForm();
        
        // Form submit handler
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
            
            // Visual feedback
            joinBtn.classList.add('loading');
            joinBtn.disabled = true;
            
            this.joinGame(name, server);
        };
        
        joinForm.addEventListener('submit', handleSubmit);
        joinBtn.addEventListener('click', (e) => {
            e.preventDefault();
            handleSubmit();
        });
        
        // Enter key navigation
        serverAddressInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                playerNameInput.focus();
            }
            
            if (!serverAddress) {
                this.showNotification('Please enter a server address!', 'error');
                return;
            }
            
            // Save the name and server address
            this.playerName = name;
            this.serverAddress = serverAddress;
            
            // Show lobby screen
            this.showLobbyScreen();
        });
        
        playerNameInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (validateForm()) {
                    handleSubmit();
                }
            }
        });
    }

    setupLobbyScreen() {
        document.getElementById('lobby-join-btn').addEventListener('click', () => {
            if (this.playerName && this.serverAddress) {
                this.joinGame(this.playerName);
            } else {
                this.showNotification('Please complete callsign entry first!', 'error');
            }
        });
    }

    showLobbyScreen() {
        // Hide callsign screen
        document.getElementById('callsign-screen').style.display = 'none';
        
        // Show lobby screen
        document.getElementById('lobby-screen').style.display = 'flex';
        
        // Connect to server to get lobby info
        this.connectToLobby();
    }

    connectToLobby() {
        const serverAddress = this.serverAddress || 'localhost';
        
        try {
            this.lobbyWs = new WebSocket(`ws://${serverAddress}:8080/game`);
            
            this.lobbyWs.onopen = () => {
                // Request lobby information
                this.sendLobbyMessage({ type: 'lobby_info' });
            };
            
            this.lobbyWs.onmessage = (event) => {
                const msg = JSON.parse(event.data);
                if (msg.type === 'lobby_info') {
                    this.updateLobbyDisplay(msg);
                }
            };
            
            this.lobbyWs.onerror = (error) => {
                console.log('Lobby connection error:', error);
                document.getElementById('lobby-player-count').textContent = '?';
            };
            
            this.lobbyWs.onclose = () => {
                // Retry connection after 3 seconds
                setTimeout(() => {
                    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
                        this.connectToLobby();
                    }
                }, 3000);
            };
        } catch (error) {
            console.log('Could not connect to lobby:', error);
        }
    }

    sendLobbyMessage(msg) {
        if (this.lobbyWs && this.lobbyWs.readyState === WebSocket.OPEN) {
            this.lobbyWs.send(JSON.stringify(msg));
        }
    }

    updateLobbyDisplay(msg) {
        // Update winning score
        if (msg.winningScore) {
            document.getElementById('lobby-winning-score').textContent = msg.winningScore;
        }
        
        // Update player count
        document.getElementById('lobby-player-count').textContent = msg.playerCount || 0;
        
        // Update leaderboard
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

    async initVoiceChat() {
        try {
            this.localStream = await navigator.mediaDevices.getUserMedia({ 
                audio: {
                    echoCancellation: true,
                    noiseSuppression: true,
                    autoGainControl: true
                }
            });
            // Mute by default
            this.localStream.getAudioTracks().forEach(track => track.enabled = false);
        } catch (err) {
            console.log('Microphone access denied:', err);
        }
    }

    toggleVoice() {
        if (!this.localStream) {
            this.showNotification('Microphone not available', 'error');
            return;
        }
        
        this.voiceEnabled = !this.voiceEnabled;
        const btn = document.getElementById('voice-toggle');
        
        this.localStream.getAudioTracks().forEach(track => {
            track.enabled = this.voiceEnabled;
        });
        
        if (this.voiceEnabled) {
            btn.classList.add('active');
            btn.innerHTML = '<i class="fas fa-microphone-slash" aria-hidden="true"></i>';
            btn.setAttribute('aria-pressed', 'true');
            btn.setAttribute('aria-label', 'Mute voice chat');
            this.showNotification('Voice chat enabled', 'success');
            this.broadcastVoiceOffer();
        } else {
            btn.classList.remove('active');
            btn.innerHTML = '<i class="fas fa-microphone" aria-hidden="true"></i>';
            btn.setAttribute('aria-pressed', 'false');
            btn.setAttribute('aria-label', 'Enable voice chat');
            this.showNotification('Voice chat muted', 'info');
        }
    }

    toggleSettings() {
        let settingsPanel = document.getElementById('settings-panel');
        
        if (!settingsPanel) {
            // Create settings panel if it doesn't exist
            settingsPanel = document.createElement('div');
            settingsPanel.id = 'settings-panel';
            settingsPanel.className = 'settings-panel';
            settingsPanel.innerHTML = `
                <div class="settings-header">
                    <h3>SETTINGS</h3>
                    <button id="close-settings" class="icon-btn-small">×</button>
                </div>
                <div class="settings-content">
                    <div class="setting-item">
                        <label for="aim-line-toggle" class="setting-label">
                            <i class="fas fa-crosshairs"></i> Aim Line
                        </label>
                        <label class="toggle-switch">
                            <input type="checkbox" id="aim-line-toggle" ${this.aimLineEnabled ? 'checked' : ''}>
                            <span class="toggle-slider"></span>
                        </label>
                    </div>
                </div>
            `;
            
            document.body.appendChild(settingsPanel);
            
            // Add event listeners
            document.getElementById('close-settings').addEventListener('click', () => {
                settingsPanel.remove();
            });
            
            document.getElementById('aim-line-toggle').addEventListener('change', (e) => {
                this.aimLineEnabled = e.target.checked;
                this.showNotification(`Aim line ${this.aimLineEnabled ? 'enabled' : 'disabled'}`, 'info');
            });
            
            // Close on click outside
            settingsPanel.addEventListener('click', (e) => {
                if (e.target === settingsPanel) {
                    settingsPanel.remove();
                }
            });
        } else {
            settingsPanel.remove();
        }
    }

    async broadcastVoiceOffer() {
        // Create peer connections for each player
        for (const playerId in this.players) {
            if (playerId !== this.playerId && !this.peerConnections[playerId]) {
                await this.createPeerConnection(playerId);
            }
        }
    }

    async createPeerConnection(remotePlayerId) {
        const pc = new RTCPeerConnection({
            iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
        });
        
        this.peerConnections[remotePlayerId] = pc;
        
        // Add local stream
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => {
                pc.addTrack(track, this.localStream);
            });
        }
        
        // Handle remote stream
        pc.ontrack = (event) => {
            const audio = new Audio();
            audio.srcObject = event.streams[0];
            audio.play();
        };
        
        // Handle ICE candidates
        pc.onicecandidate = (event) => {
            if (event.candidate) {
                this.sendMessage({
                    type: 'voice-ice',
                    target: remotePlayerId,
                    candidate: event.candidate
                });
            }
        };
        
        // Create and send offer
        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);
        
        this.sendMessage({
            type: 'voice-offer',
            target: remotePlayerId,
            offer: offer
        });
    }

    async handleVoiceOffer(data) {
        const pc = new RTCPeerConnection({
            iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
        });
        
        this.peerConnections[data.from] = pc;
        
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => {
                pc.addTrack(track, this.localStream);
            });
        }
        
        pc.ontrack = (event) => {
            const audio = new Audio();
            audio.srcObject = event.streams[0];
            audio.play();
        };
        
        pc.onicecandidate = (event) => {
            if (event.candidate) {
                this.sendMessage({
                    type: 'voice-ice',
                    target: data.from,
                    candidate: event.candidate
                });
            }
        };
        
        await pc.setRemoteDescription(new RTCSessionDescription(data.offer));
        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);
        
        this.sendMessage({
            type: 'voice-answer',
            target: data.from,
            answer: answer
        });
    }

    async handleVoiceAnswer(data) {
        const pc = this.peerConnections[data.from];
        if (pc) {
            await pc.setRemoteDescription(new RTCSessionDescription(data.answer));
        }
    }

    async handleVoiceIce(data) {
        const pc = this.peerConnections[data.from];
        if (pc) {
            await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
        }
    }

    joinGame(name, server = 'localhost') {
        this.playerName = name;
        const serverAddress = server || 'localhost';
        const wsUrl = `ws://${serverAddress}:8080/game`;
        
        console.log('Attempting to connect to:', wsUrl);
        
        // Show loading overlay
        this.showLoadingOverlay('Connecting to server...');
        
        this.ws = new WebSocket(wsUrl);
        
        // Connection timeout
        const connectionTimeout = setTimeout(() => {
            if (this.ws.readyState !== WebSocket.OPEN) {
                this.ws.close();
                this.hideLoadingOverlay();
                this.showNotification('Connection timeout. Please check server address.', 'error');
                const joinBtn = document.getElementById('join-btn');
                joinBtn.classList.remove('loading');
                joinBtn.disabled = false;
            }
        }, 10000); // 10 second timeout
        
        this.ws.onopen = () => {
            clearTimeout(connectionTimeout);
            console.log('WebSocket connected');
            this.sendMessage({ type: 'join', name: name });
            
            // Hide join screen and show HUD
            const joinScreen = document.getElementById('join-screen');
            const gameHud = document.getElementById('game-hud');
            
            joinScreen.style.display = 'none';
            gameHud.style.display = 'block';
            gameHud.classList.add('active');
            
            this.hideLoadingOverlay();
            this.showNotification(`Welcome, ${name}!`, 'success');
            
            // Update ARIA attributes
            gameHud.setAttribute('aria-hidden', 'false');
            
            this.gameLoop();
        };
        
        this.ws.onmessage = (event) => {
            try {
                this.handleMessage(JSON.parse(event.data));
            } catch (e) {
                console.error('Error parsing message:', e);
            }
        };
        
        this.ws.onclose = () => {
            clearTimeout(connectionTimeout);
            console.log('WebSocket closed');
            this.hideLoadingOverlay();
            this.showNotification('Connection lost. Refreshing in 3 seconds...', 'error');
            setTimeout(() => location.reload(), 3000);
        };
        
        this.ws.onerror = (error) => {
            clearTimeout(connectionTimeout);
            console.error('WebSocket error:', error);
            this.hideLoadingOverlay();
            this.showNotification('Failed to connect to server. Please verify the server address.', 'error');
            
            // Re-enable join button
            const joinBtn = document.getElementById('join-btn');
            joinBtn.classList.remove('loading');
            joinBtn.disabled = false;
        };
    }

    sendMessage(msg) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(msg));
        }
    }

    handleMessage(msg) {
        switch (msg.type) {
            case 'update':
                this.players = {};
                msg.players.forEach(p => {
                    this.players[p.id] = p;
                    if (p.id === this.playerId || (!this.playerId && p.name === this.playerName)) {
                        // Check for bonus collection
                        if (this.myPlayer && p.lastPowerUpCollectTime && 
                            p.lastPowerUpCollectTime !== this.myPlayer.lastPowerUpCollectTime) {
                            // Bonus was just collected!
                            this.handleBonusCollection(p);
                        }
                        
                        this.playerId = p.id;
                        this.myPlayer = p;
                        this.health = p.alive ? 100 : 0;
                        this.isAlive = p.alive;
                    }
                });
                
                this.bullets = {};
                msg.bullets.forEach(b => this.bullets[b.id] = b);
                
                this.powerUps = {};
                msg.powerUps.forEach(pu => this.powerUps[pu.id] = pu);
                
                this.updateHUD();
                break;
                
            case 'chat':
                this.addChatMessage(msg.msg);
                break;
                
            case 'respawn':
                if (msg.playerId === this.playerId) {
                    this.isAlive = true;
                    this.health = 100;
                    document.getElementById('respawn-overlay').style.display = 'none';
                }
                break;
                
            case 'hit':
                if (msg.target === this.playerId) {
                    this.health -= 100;
                    this.deaths++;
                    this.showRespawnScreen();
                    this.createExplosion(this.myPlayer.x, this.myPlayer.y, 30, '#ff0000');
                }
                if (msg.shooter === this.playerId) {
                    this.kills++;
                    this.showNotification('+1 KILL', 'success');
                }
                this.addKillFeed(msg.shooter, msg.target);
                break;

            case 'game_over':
                // msg: { type: 'game_over', winnerId: '...', winnerName: '...', leaderboard: [ {name, score}, ... ] }
                this.handleGameOver(msg);
                break;
                
            case 'voice-offer':
                this.handleVoiceOffer(msg);
                break;
            case 'voice-answer':
                this.handleVoiceAnswer(msg);
                break;
            case 'voice-ice':
                this.handleVoiceIce(msg);
                break;
        }
    }

    tryFire() {
        const now = Date.now();
        
        // Heat-based firing limitation for increased difficulty
        // At 100% heat, cannot fire; gradually reduce fire rate as heat increases
        const heatPercentage = (this.heatLevel / this.maxHeatLevel) * 100;
        
        // Calculate dynamic fire rate based on heat
        // Base: 500ms, increases to 2000ms at 80% heat, blocked at 100% heat
        let dynamicFireRate = this.fireRate;
        if (heatPercentage >= 100) {
            // Cannot fire at max heat
            return;
        } else if (heatPercentage >= 80) {
            // Severely restricted: 2000ms fire rate
            dynamicFireRate = 2000;
        } else if (heatPercentage >= 60) {
            // Restricted: 1200ms fire rate
            dynamicFireRate = 1200;
        } else if (heatPercentage >= 40) {
            // Moderate restriction: 800ms fire rate
            dynamicFireRate = 800;
        }
        
        if (now - this.lastFireTime >= dynamicFireRate && this.isAlive) {
            // Increase heat level with each shot
            this.heatLevel = Math.min(this.maxHeatLevel, this.heatLevel + 20);
            
            this.sendMessage({ 
                type: 'fire', 
                angle: Math.round(this.angle),
                mouseX: Math.round(this.mouseX),
                mouseY: Math.round(this.mouseY),
                heatLevel: Math.round(this.heatLevel)
            });
            this.lastFireTime = now;
            this.screenShake();
        }
    }

    sendMove() {
        if (!this.myPlayer || !this.isAlive) return;
        
        let x = this.myPlayer.x;
        let y = this.myPlayer.y;
        const speed = this.myPlayer.speedBoost ? 20 : 12;

        if (this.keys['KeyW'] || this.keys['ArrowUp']) y -= speed;
        if (this.keys['KeyS'] || this.keys['ArrowDown']) y += speed;
        if (this.keys['KeyA'] || this.keys['ArrowLeft']) x -= speed;
        if (this.keys['KeyD'] || this.keys['ArrowRight']) x += speed;

        x = Math.max(15, Math.min(1905, x));
        y = Math.max(15, Math.min(1065, y));

        if (x !== this.myPlayer.x || y !== this.myPlayer.y || this.angle !== this.myPlayer.angle) {
            this.sendMessage({ 
                type: 'move', 
                x: Math.round(x), 
                y: Math.round(y), 
                angle: Math.round(this.angle) 
            });
        }
        
        // Auto-fire on space
        if (this.keys['Space']) {
            this.tryFire();
        }
    }

    sendChat() {
        const input = document.getElementById('chat-input');
        const msg = input.value.trim();
        if (msg) {
            this.sendMessage({ type: 'chat', msg: msg });
            input.value = '';
        }
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
        
        // Remove old messages
        while (messagesDiv.children.length > 20) {
            messagesDiv.removeChild(messagesDiv.firstChild);
        }
    }

    addKillFeed(shooterId, targetId) {
        const shooter = this.players[shooterId];
        const target = this.players[targetId];
        
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
        this.isAlive = false;
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

    handleGameOver(msg) {
        // stop input/updates
        this.isAlive = false;
        this.showGameOverOverlay(msg);
    }

    showGameOverOverlay(msg) {
        // Remove existing overlay if present
        let existing = document.getElementById('game-over-overlay');
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = 'game-over-overlay';
        overlay.style.position = 'fixed';
        overlay.style.left = '0';
        overlay.style.top = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.display = 'flex';
        overlay.style.alignItems = 'center';
        overlay.style.justifyContent = 'center';
        overlay.style.background = 'rgba(0,0,0,0.75)';
        overlay.style.zIndex = '9999';

        const card = document.createElement('div');
        card.style.background = '#111';
        card.style.color = '#fff';
        card.style.padding = '30px';
        card.style.borderRadius = '8px';
        card.style.textAlign = 'center';
        card.style.minWidth = '320px';

        const title = document.createElement('h2');
        title.style.marginTop = '0';
        if (msg.winnerId === this.playerId) {
            title.textContent = 'YOU WON!';
            title.style.color = '#00ff88';
        } else {
            title.textContent = (msg.winnerName ? msg.winnerName : 'A player') + ' won the game';
            title.style.color = '#ffcc00';
        }

        const subtitle = document.createElement('p');
        subtitle.textContent = 'Final leaderboard:';

        const list = document.createElement('div');
        list.style.textAlign = 'left';
        list.style.maxHeight = '200px';
        list.style.overflow = 'auto';
        list.style.margin = '10px 0';

        // msg.leaderboard is expected to be an array of {name, score}
        if (Array.isArray(msg.leaderboard)) {
            msg.leaderboard.forEach((entry, idx) => {
                const el = document.createElement('div');
                el.style.padding = '6px 0';
                el.style.borderBottom = '1px solid rgba(255,255,255,0.06)';
                el.innerHTML = `<strong>#${idx + 1}</strong> ${entry.name} <span style="float:right">${entry.score}</span>`;
                list.appendChild(el);
            });
        }

        const btn = document.createElement('button');
        btn.textContent = 'Return / Reload';
        btn.style.marginTop = '12px';
        btn.className = 'btn-primary';
        btn.addEventListener('click', () => {
            location.reload();
        });

        card.appendChild(title);
        card.appendChild(subtitle);
        card.appendChild(list);
        card.appendChild(btn);
        overlay.appendChild(card);
        document.body.appendChild(overlay);
    }

    updateHUD() {
        // Update stats with ARIA labels
        const healthText = document.getElementById('health-text');
        const killsText = document.getElementById('kills-text');
        const deathsText = document.getElementById('deaths-text');
        
        healthText.textContent = this.health;
        healthText.setAttribute('aria-label', `Health: ${this.health}`);
        
        killsText.textContent = this.kills;
        killsText.setAttribute('aria-label', `Kills: ${this.kills}`);
        
        deathsText.textContent = this.deaths;
        deathsText.setAttribute('aria-label', `Deaths: ${this.deaths}`);
        
        // Update power-up indicator with enhanced visualization
        const indicator = document.getElementById('power-up-indicator');
        if (this.myPlayer) {
            let powerUpHTML = '';
            let ariaLabel = '';
            
            if (this.myPlayer.shield) {
                powerUpHTML = '<div class="powerup-item shield-active"><i class="fas fa-shield-alt" aria-hidden="true"></i><div class="powerup-label">SHIELD</div></div>';
                ariaLabel = 'Shield active';
            } else if (this.myPlayer.speedBoost) {
                powerUpHTML = '<div class="powerup-item speed-boost-active"><i class="fas fa-bolt" aria-hidden="true"></i><div class="powerup-label">SPEED</div></div>';
                ariaLabel = 'Speed boost active';
            } else if (this.myPlayer.doubleFire) {
                powerUpHTML = '<div class="powerup-item double-fire-active"><i class="fas fa-fire" aria-hidden="true"></i><div class="powerup-label">DOUBLE</div></div>';
                ariaLabel = 'Double fire active';
            }
            
            // Update heat level indicator (vertical bar - full map height)
            const heatPercent = Math.round((this.heatLevel / this.maxHeatLevel) * 100);
            const heatColor = heatPercent < 30 ? '#00ff88' : heatPercent < 70 ? '#ffaa00' : '#ff4444';
            
            // Update vertical heat bar with ARIA
            const heatBar = document.getElementById('heat-bar-vertical');
            if (heatBar) {
                heatBar.setAttribute('aria-valuenow', heatPercent);
                heatBar.setAttribute('aria-label', `Weapon heat level: ${heatPercent} percent`);
                
                const segments = 160; // Total segments to fill full map height
                const filledSegments = Math.ceil((heatPercent / 100) * segments);
                let segmentsHTML = '';
                for (let i = 0; i < segments; i++) {
                    const isActive = i < filledSegments;
                    const segmentColor = isActive ? heatColor : 'rgba(255,255,255,0.05)';
                    segmentsHTML += `<div class="heat-bar-segment" style="background-color: ${segmentColor};"></div>`;
                }
                heatBar.innerHTML = segmentsHTML;
                heatBar.style.borderColor = heatColor;
                
                // Update label to show percentage
                const label = document.createElement('div');
                label.style.cssText = 'position: absolute; font-size: 0.8rem; color: ' + heatColor + '; top: 12px; text-align: center; width: 100%; font-weight: bold;';
                label.textContent = heatPercent + '%';
                label.setAttribute('aria-hidden', 'true');
                heatBar.appendChild(label);
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
        
        // Update leaderboard
        this.updateLeaderboard();
    }

    updateLeaderboard() {
        const scoresDiv = document.getElementById('scores');
        const sortedPlayers = Object.values(this.players)
            .sort((a, b) => b.score - a.score)
            .slice(0, 10);
        
        scoresDiv.innerHTML = sortedPlayers.map((p, index) => {
            const isCurrentPlayer = p.id === this.playerId;
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

    showNotification(message, type = 'info') {
        const notifDiv = document.getElementById('notifications');
        const notif = document.createElement('div');
        notif.className = `notification ${type}`;
        notif.textContent = message;
        notif.setAttribute('role', 'alert');
        
        notifDiv.appendChild(notif);
        
        // Auto-remove after 3 seconds
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

    handleBonusCollection(player) {
        // Determine bonus type and message
        let bonusName = 'BONUS';
        let bonusEmoji = '✨';
        let bonusColor = '#ffff00';
        
        if (player.lastPowerUpType === 'SHIELD') {
            bonusName = 'SHIELD ACTIVATED';
            bonusEmoji = '🛡️';
            bonusColor = '#00ffff';
        } else if (player.lastPowerUpType === 'DOUBLE_FIRE') {
            bonusName = 'DOUBLE FIRE!';
            bonusEmoji = '🔥';
            bonusColor = '#ff6600';
        } else if (player.lastPowerUpType === 'SPEED_BOOST') {
            bonusName = 'SPEED BOOST!';
            bonusEmoji = '⚡';
            bonusColor = '#ffff00';
        }
        
        // Show notification
        this.showNotification(`${bonusEmoji} ${bonusName} ${bonusEmoji}`, 'success');
        
        // Create floating bonus text
        this.createBonusFloatingText(player, bonusName, bonusColor);
    }

    createBonusFloatingText(player, bonusName, bonusColor) {
        const canvas = this.canvas;
        const screenX = player.x;
        const screenY = player.y - 60;
        
        // Create floating text element
        const floatDiv = document.createElement('div');
        floatDiv.style.cssText = `
            position: fixed;
            left: ${canvas.getBoundingClientRect().left + screenX}px;
            top: ${canvas.getBoundingClientRect().top + screenY}px;
            font-size: 2rem;
            font-weight: bold;
            color: ${bonusColor};
            pointer-events: none;
            text-shadow: 0 0 10px ${bonusColor};
            z-index: 100;
            animation: bonusFloatUp 1.5s ease-out forwards;
            transform: translate(-50%, -50%);
        `;
        floatDiv.textContent = bonusName;
        document.body.appendChild(floatDiv);
        
        setTimeout(() => floatDiv.remove(), 1500);
    }

    screenShake() {
        this.canvas.style.transform = 'translate(' + (Math.random() * 4 - 2) + 'px, ' + (Math.random() * 4 - 2) + 'px)';
        setTimeout(() => {
            this.canvas.style.transform = '';
        }, 50);
    }

    createExplosion(x, y, count, color) {
        for (let i = 0; i < count; i++) {
            this.particles.push({
                x, y,
                vx: (Math.random() - 0.5) * 10,
                vy: (Math.random() - 0.5) * 10,
                life: 1.0,
                color
            });
        }
    }

    updateParticles() {
        this.particles = this.particles.filter(p => {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.3; // gravity
            p.life -= 0.02;
            return p.life > 0;
        });
    }

    gameLoop() {
        this.sendMove();
        this.updateParticles();
        
        // Update and decay heat level
        this.heatLevel = Math.max(0, this.heatLevel - this.heatDecayRate);
        
        // Update angle to always point to mouse pointer
        if (this.myPlayer) {
            this.angle = Math.atan2(
                this.mouseY - this.myPlayer.y,
                this.mouseX - this.myPlayer.x
            ) * 180 / Math.PI;
        }
        
        this.render();
        this.renderMinimap();
        this.updateHUD();
        requestAnimationFrame(() => this.gameLoop());
    }

    render() {
        const ctx = this.ctx;
        ctx.clearRect(0, 0, 1920, 1080);

        // Background grid
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.1)';
        ctx.lineWidth = 1;
        for (let x = 0; x <= 1920; x += 50) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, 1080);
            ctx.stroke();
        }
        for (let y = 0; y <= 1080; y += 50) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(1920, y);
            ctx.stroke();
        }

        // Power-ups
        Object.values(this.powerUps).forEach(pu => {
            const colors = { SHIELD: '#00ffff', SPEED_BOOST: '#ffff00', DOUBLE_FIRE: '#ff6600' };
            ctx.fillStyle = colors[pu.type] || '#ffffff';
            ctx.shadowBlur = 20;
            ctx.shadowColor = ctx.fillStyle;
            
            ctx.save();
            ctx.translate(pu.x, pu.y);
            
            const rotation = (Date.now() / 500) % (2 * Math.PI);
            ctx.rotate(rotation);
            
            // Different visuals based on power-up type
            if (pu.type === 'SHIELD') {
                // Shield - circular with pulsing glow
                const pulseSize = 14 + Math.sin(Date.now() / 200) * 3;
                ctx.strokeStyle = colors.SHIELD;
                ctx.lineWidth = 3;
                ctx.beginPath();
                ctx.arc(0, 0, pulseSize, 0, Math.PI * 2);
                ctx.stroke();
                
                // Inner shield circle
                ctx.fillStyle = colors.SHIELD;
                ctx.globalAlpha = 0.3;
                ctx.beginPath();
                ctx.arc(0, 0, pulseSize - 3, 0, Math.PI * 2);
                ctx.fill();
                ctx.globalAlpha = 1;
                
                // Shield symbol
                ctx.fillStyle = colors.SHIELD;
                ctx.font = 'bold 16px Arial';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('⬢', 0, 0);
                
            } else if (pu.type === 'DOUBLE_FIRE') {
                // Double bullets - show two bullet symbols
                ctx.fillStyle = colors.DOUBLE_FIRE;
                
                // Left bullet
                ctx.save();
                ctx.translate(-8, -2);
                ctx.rotate(-rotation);
                ctx.beginPath();
                ctx.arc(0, 0, 5, 0, Math.PI * 2);
                ctx.fill();
                ctx.restore();
                
                // Right bullet
                ctx.save();
                ctx.translate(8, 2);
                ctx.rotate(-rotation);
                ctx.beginPath();
                ctx.arc(0, 0, 5, 0, Math.PI * 2);
                ctx.fill();
                ctx.restore();
                
                // Center spark effect
                const sparkCount = 4;
                for (let i = 0; i < sparkCount; i++) {
                    const angle = (rotation + (i / sparkCount) * Math.PI * 2);
                    const x = Math.cos(angle) * 12;
                    const y = Math.sin(angle) * 12;
                    ctx.fillStyle = colors.DOUBLE_FIRE;
                    ctx.globalAlpha = 0.6;
                    ctx.beginPath();
                    ctx.arc(x, y, 2, 0, Math.PI * 2);
                    ctx.fill();
                    ctx.globalAlpha = 1;
                }
                
            } else if (pu.type === 'SPEED_BOOST') {
                // Speed boost - rotating star/diamond
                const size = 12 + Math.sin(Date.now() / 150) * 2;
                ctx.fillStyle = colors.SPEED_BOOST;
                ctx.globalAlpha = 0.8;
                ctx.fillRect(-size, -size, size * 2, size * 2);
                ctx.globalAlpha = 1;
                
                // Speed lines
                ctx.strokeStyle = colors.SPEED_BOOST;
                ctx.lineWidth = 2;
                ctx.globalAlpha = 0.6;
                for (let i = -1; i <= 1; i++) {
                    ctx.beginPath();
                    ctx.moveTo(-16, i * 4);
                    ctx.lineTo(-10, i * 4);
                    ctx.stroke();
                }
                ctx.globalAlpha = 1;
            } else {
                // Default square
                ctx.fillRect(-12, -12, 24, 24);
            }
            
            ctx.restore();
            ctx.shadowBlur = 0;
        });

        // Bullets
        ctx.fillStyle = '#ff0000';
        ctx.shadowBlur = 10;
        ctx.shadowColor = '#ff0000';
        Object.values(this.bullets).forEach(b => {
            ctx.beginPath();
            ctx.arc(b.x, b.y, 4, 0, Math.PI * 2);
            ctx.fill();
            
            // Bullet trail
            ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(b.x, b.y);
            ctx.lineTo(b.x - b.dx * 2, b.y - b.dy * 2);
            ctx.stroke();
        });
        ctx.shadowBlur = 0;

        // Particles
        this.particles.forEach(p => {
            ctx.fillStyle = p.color;
            ctx.globalAlpha = p.life;
            ctx.fillRect(p.x - 2, p.y - 2, 4, 4);
        });
        ctx.globalAlpha = 1;

        // Players
        Object.values(this.players).forEach(p => {
            if (!p.alive) return;
            
            const isMe = p.id === this.playerId;
            ctx.fillStyle = isMe ? '#00ff88' : '#ff4444';
            
            // Enhanced Shield effect
            if (p.shield) {
                const pulseAmount = Math.sin(Date.now() / 200) * 0.5 + 1.5;
                const shieldRadius = 30 + pulseAmount;
                
                // Pulsing shield circle
                ctx.strokeStyle = '#00ffff';
                ctx.lineWidth = 2 + pulseAmount * 0.5;
                ctx.shadowBlur = 20;
                ctx.shadowColor = '#00ffff';
                ctx.globalAlpha = 0.6 + Math.sin(Date.now() / 200) * 0.2;
                ctx.beginPath();
                ctx.arc(p.x, p.y, shieldRadius, 0, Math.PI * 2);
                ctx.stroke();
                ctx.globalAlpha = 1;
                
                // Inner shield ring
                ctx.strokeStyle = '#00ffff';
                ctx.lineWidth = 1;
                ctx.globalAlpha = 0.4;
                ctx.beginPath();
                ctx.arc(p.x, p.y, shieldRadius - 5, 0, Math.PI * 2);
                ctx.stroke();
                ctx.globalAlpha = 1;
                
                // Shield corners (square shield corners)
                ctx.strokeStyle = '#00ffff';
                ctx.lineWidth = 2.5;
                const cornerSize = 22 + pulseAmount * 0.3;
                ctx.globalAlpha = 0.7;
                ctx.strokeRect(p.x - cornerSize, p.y - cornerSize, cornerSize * 2, cornerSize * 2);
                ctx.globalAlpha = 1;
                
                ctx.shadowBlur = 0;
            }
            
            // Bonus collection animation
            if (p.lastPowerUpCollectTime) {
                const timeSinceCollect = Date.now() - p.lastPowerUpCollectTime;
                const animationDuration = 1000; // 1 second animation
                
                if (timeSinceCollect < animationDuration) {
                    const progress = timeSinceCollect / animationDuration;
                    const expandRadius = 40 + (progress * 60);
                    const fadeOut = 1 - progress;
                    
                    ctx.save();
                    
                    // Get bonus type color
                    let bonusColor = '#ffff00';
                    let bonusSymbol = '★';
                    if (p.lastPowerUpType === 'SHIELD') {
                        bonusColor = '#00ffff';
                        bonusSymbol = '⬢';
                    } else if (p.lastPowerUpType === 'DOUBLE_FIRE') {
                        bonusColor = '#ff6600';
                        bonusSymbol = '⚡';
                    } else if (p.lastPowerUpType === 'SPEED_BOOST') {
                        bonusColor = '#ffff00';
                        bonusSymbol = '→';
                    }
                    
                    // Expanding ring effect
                    ctx.strokeStyle = bonusColor;
                    ctx.globalAlpha = fadeOut;
                    ctx.lineWidth = 3;
                    ctx.shadowBlur = 20;
                    ctx.shadowColor = bonusColor;
                    ctx.beginPath();
                    ctx.arc(p.x, p.y, expandRadius, 0, Math.PI * 2);
                    ctx.stroke();
                    
                    // Expanding particles around tank
                    for (let i = 0; i < 8; i++) {
                        const angle = (i / 8) * Math.PI * 2;
                        const distance = 30 + (progress * 70);
                        const px = p.x + Math.cos(angle) * distance;
                        const py = p.y + Math.sin(angle) * distance;
                        
                        ctx.fillStyle = bonusColor;
                        ctx.globalAlpha = fadeOut * 0.7;
                        ctx.beginPath();
                        ctx.arc(px, py, 4 - progress * 3, 0, Math.PI * 2);
                        ctx.fill();
                    }
                    
                    // Center glow burst
                    ctx.fillStyle = bonusColor;
                    ctx.globalAlpha = fadeOut * 0.5;
                    ctx.beginPath();
                    ctx.arc(p.x, p.y, 25, 0, Math.PI * 2);
                    ctx.fill();
                    
                    ctx.restore();
                    ctx.globalAlpha = 1;
                    ctx.shadowBlur = 0;
                }
            }
            
            // Tank body
            ctx.shadowBlur = 10;
            ctx.shadowColor = ctx.fillStyle;
            ctx.fillRect(p.x - 18, p.y - 18, 36, 36);
            ctx.shadowBlur = 0;
            
            // Active Power-Up Highlights/Glows
            if (p.shield || p.speedBoost || p.doubleFire) {
                // Determine glow color and intensity based on active power-ups
                let glowColor = '#ffffff';
                let glowIntensity = 0;
                
                if (p.shield) {
                    glowColor = '#00ffff';
                    glowIntensity = 1;
                } else if (p.doubleFire) {
                    glowColor = '#ff6600';
                    glowIntensity = 0.8;
                } else if (p.speedBoost) {
                    glowColor = '#ffff00';
                    glowIntensity = 0.6;
                }
                
                // Pulsing tank glow
                const glowPulse = Math.sin(Date.now() / 150) * 0.3 + 0.7;
                ctx.strokeStyle = glowColor;
                ctx.lineWidth = 3 * glowIntensity;
                ctx.globalAlpha = glowPulse * glowIntensity;
                ctx.shadowBlur = 15 + (glowPulse * 10);
                ctx.shadowColor = glowColor;
                ctx.strokeRect(p.x - 20, p.y - 20, 40, 40);
                ctx.shadowBlur = 0;
                ctx.globalAlpha = 1;
            }
            
            // Turret - tracks mouse for player, angle for others
            const turretAngle = isMe ? this.angle : p.angle;
            const rad = turretAngle * Math.PI / 180;
            
            // Double Fire Visualization - Two barrel effect
            if (p.doubleFire) {
                // Draw two offset barrels for double fire visualization
                const barrelOffset = 8;
                const barrelLength = 25;
                
                // Top barrel
                ctx.strokeStyle = '#ff6600';
                ctx.lineWidth = 4;
                ctx.globalAlpha = 0.8;
                ctx.shadowBlur = 15;
                ctx.shadowColor = '#ff6600';
                ctx.beginPath();
                ctx.moveTo(p.x - barrelOffset * Math.sin(rad), p.y + barrelOffset * Math.cos(rad));
                ctx.lineTo(p.x - barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength, 
                           p.y + barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength);
                ctx.stroke();
                
                // Bottom barrel
                ctx.beginPath();
                ctx.moveTo(p.x + barrelOffset * Math.sin(rad), p.y - barrelOffset * Math.cos(rad));
                ctx.lineTo(p.x + barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength,
                           p.y - barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength);
                ctx.stroke();
                
                ctx.globalAlpha = 1;
                ctx.shadowBlur = 0;
                
                // Muzzle flare effect
                const flareTime = Date.now() % 200; // 200ms cycle
                if (flareTime < 100) { // First half of cycle for flare effect
                    const flareIntensity = 1 - (flareTime / 100);
                    
                    // Top muzzle flare
                    ctx.fillStyle = '#ffaa00';
                    ctx.globalAlpha = flareIntensity * 0.6;
                    ctx.beginPath();
                    ctx.arc(p.x - barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength, 
                            p.y + barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength,
                            8 + flareIntensity * 4, 0, Math.PI * 2);
                    ctx.fill();
                    
                    // Bottom muzzle flare
                    ctx.beginPath();
                    ctx.arc(p.x + barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength,
                            p.y - barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength,
                            8 + flareIntensity * 4, 0, Math.PI * 2);
                    ctx.fill();
                    
                    ctx.globalAlpha = 1;
                }
            } else {
                // Normal single barrel
                ctx.strokeStyle = '#ffffff';
                ctx.lineWidth = 4;
                ctx.beginPath();
                ctx.moveTo(p.x, p.y);
                ctx.lineTo(p.x + Math.cos(rad) * 25, p.y + Math.sin(rad) * 25);
                ctx.stroke();
            }
            
            // Draw laser sight line for player's tank
            if (isMe && this.aimLineEnabled) {
                ctx.strokeStyle = 'rgba(255, 100, 100, 0.6)';
                ctx.lineWidth = 1;
                ctx.setLineDash([5, 5]);
                ctx.beginPath();
                ctx.moveTo(p.x, p.y);
                ctx.lineTo(this.mouseX, this.mouseY);
                ctx.stroke();
                ctx.setLineDash([]);
            }
            
            // Draw heat glow around barrel (always visible)
            if (isMe) {
                const heatPercent = this.heatLevel / this.maxHeatLevel;
                if (heatPercent > 0) {
                    ctx.shadowBlur = 5 + heatPercent * 15;
                    ctx.shadowColor = heatPercent < 0.4 ? '#00ff88' : heatPercent < 0.7 ? '#ffaa00' : '#ff4444';
                    ctx.strokeStyle = ctx.shadowColor;
                    ctx.globalAlpha = 0.6 + heatPercent * 0.4;
                    ctx.lineWidth = 2 + heatPercent * 3;
                    ctx.beginPath();
                    ctx.moveTo(p.x, p.y);
                    ctx.lineTo(p.x + Math.cos(rad) * 25, p.y + Math.sin(rad) * 25);
                    ctx.stroke();
                    ctx.globalAlpha = 1;
                    ctx.shadowBlur = 0;
                }
            }
            
            // Health bar
            const healthPercent = 100;
            const healthWidth = 36 * (healthPercent / 100);
            ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
            ctx.fillRect(p.x - 18, p.y - 30, 36, 4);
            ctx.fillStyle = healthPercent > 50 ? '#00ff88' : healthPercent > 25 ? '#ffaa00' : '#ff0000';
            ctx.fillRect(p.x - 18, p.y - 30, healthWidth, 4);
            
            // Name tag
            ctx.fillStyle = '#ffffff';
            ctx.font = 'bold 12px Arial';
            ctx.textAlign = 'center';
            ctx.shadowBlur = 3;
            ctx.shadowColor = '#000000';
            ctx.fillText(p.name, p.x, p.y - 35);
            ctx.shadowBlur = 0;
            
            // Speed boost indicator
            if (p.speedBoost) {
                ctx.strokeStyle = '#ffff00';
                ctx.lineWidth = 2;
                for (let i = 0; i < 3; i++) {
                    ctx.strokeRect(p.x - 20 + i * 2, p.y + 25, 8, 2);
                }
            }
        });
    }

    renderMinimap() {
        if (!this.minimapCtx) return;
        
        const ctx = this.minimapCtx;
        ctx.clearRect(0, 0, 200, 150);
        
        // Background
        ctx.fillStyle = 'rgba(0, 20, 10, 0.8)';
        ctx.fillRect(0, 0, 200, 150);
        
        // Grid
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.2)';
        ctx.lineWidth = 1;
        for (let x = 0; x <= 200; x += 40) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, 150);
            ctx.stroke();
        }
        for (let y = 0; y <= 150; y += 30) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(200, y);
            ctx.stroke();
        }
        
        // Players
        Object.values(this.players).forEach(p => {
            if (!p.alive) return;
            
            const mx = (p.x / 1920) * 200;
            const my = (p.y / 1080) * 150;
            
            ctx.fillStyle = p.id === this.playerId ? '#00ff88' : '#ff4444';
            ctx.beginPath();
            ctx.arc(mx, my, 4, 0, Math.PI * 2);
            ctx.fill();
        });
    }
}

// Start the game
document.addEventListener('DOMContentLoaded', () => {
    new GameClient();
});