class GameClient {
    constructor() {
        this.canvas = document.getElementById('game-canvas');
        this.ctx = this.canvas.getContext('2d');
        this.minimapCanvas = document.getElementById('minimap');
        this.minimapCtx = this.minimapCanvas ? this.minimapCanvas.getContext('2d') : null;
        
        this.ws = null;
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
        
        // Voice chat
        this.peerConnections = {};
        this.localStream = null;
        this.voiceEnabled = false;
        
        // UI state
        this.chatOpen = true;
        this.chatHistory = [];
        this.killFeed = [];
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupJoinScreen();
        this.initVoiceChat();
        this.handleResize();
    }

    handleResize() {
        // Handle window resize for responsive canvas
        const resizeCanvas = () => {
            const container = document.getElementById('game-container');
            const rect = container.getBoundingClientRect();
            
            // Maintain aspect ratio
            const aspectRatio = 1920 / 1080;
            let width = rect.width;
            let height = rect.height;
            
            if (width / height > aspectRatio) {
                width = height * aspectRatio;
            } else {
                height = width / aspectRatio;
            }
            
            // Update canvas size while maintaining logical size
            this.canvas.style.width = width + 'px';
            this.canvas.style.height = height + 'px';
        };
        
        window.addEventListener('resize', resizeCanvas);
        resizeCanvas();
    }

    setupEventListeners() {
        // Keyboard
        document.addEventListener('keydown', (e) => {
            this.keys[e.code] = true;
            
            // Chat toggle
            if (e.code === 'Enter') {
                const chatInput = document.getElementById('chat-input');
                if (document.activeElement === chatInput) {
                    this.sendChat();
                    chatInput.blur();
                } else {
                    chatInput.focus();
                }
                e.preventDefault();
            }
            
            // ESC to close chat input
            if (e.code === 'Escape') {
                document.getElementById('chat-input').blur();
            }
        });
        
        document.addEventListener('keyup', (e) => {
            this.keys[e.code] = false;
        });

        // Mouse - track globally for turret aiming
        document.addEventListener('mousemove', (e) => {
            const rect = this.canvas.getBoundingClientRect();
            
            // Scale mouse coordinates from display space to logical canvas space (1920x1080)
            const scaleX = 1920 / rect.width;
            const scaleY = 1080 / rect.height;
            
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
    }

    setupJoinScreen() {
        const joinBtn = document.getElementById('join-btn');
        const playerNameInput = document.getElementById('player-name');
        
        joinBtn.addEventListener('click', () => {
            const name = playerNameInput.value.trim();
            if (name) {
                // Add visual feedback
                joinBtn.classList.add('loading');
                joinBtn.disabled = true;
                joinBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Connecting...';
                this.joinGame(name);
            } else {
                this.showNotification('Please enter a callsign!', 'error');
                playerNameInput.focus();
            }
        });
        
        playerNameInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                joinBtn.click();
            }
        });
        
        document.getElementById('server-address').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                playerNameInput.focus();
            }
        });
    }

    async initVoiceChat() {
        try {
            console.log('Requesting microphone access...');
            this.localStream = await navigator.mediaDevices.getUserMedia({ 
                audio: {
                    echoCancellation: true,
                    noiseSuppression: true,
                    autoGainControl: true
                }
            });
            // Mute by default
            this.localStream.getAudioTracks().forEach(track => track.enabled = false);
            console.log('Microphone access granted');
            this.showNotification('Microphone ready - Click mic button to enable', 'success');
        } catch (err) {
            console.log('Microphone access denied:', err);
            this.showNotification('Microphone not available - voice chat disabled', 'error');
            // Disable voice button if no mic
            const voiceBtn = document.getElementById('voice-toggle');
            if (voiceBtn) {
                voiceBtn.disabled = true;
                voiceBtn.style.opacity = '0.5';
                voiceBtn.title = 'Microphone not available';
            }
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
            btn.innerHTML = '<i class="fas fa-microphone"></i>';
            btn.title = 'Voice chat enabled - Click to mute';
            this.showNotification('Voice chat enabled - connecting to players...', 'success');
            this.broadcastVoiceOffer();
        } else {
            btn.classList.remove('active');
            btn.innerHTML = '<i class="fas fa-microphone-slash"></i>';
            btn.title = 'Voice chat muted - Click to enable';
            this.showNotification('Voice chat muted', 'info');
            
            // Close all peer connections
            for (const playerId in this.peerConnections) {
                this.peerConnections[playerId].close();
            }
            this.peerConnections = {};
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
        console.log('Creating peer connection to:', remotePlayerId);
        
        try {
            const pc = new RTCPeerConnection({
                iceServers: [
                    { urls: 'stun:stun.l.google.com:19302' },
                    { urls: 'stun:stun1.l.google.com:19302' }
                ]
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
                console.log('Received remote track from:', remotePlayerId);
                const audio = new Audio();
                audio.srcObject = event.streams[0];
                audio.play().catch(e => console.log('Audio autoplay blocked:', e));
            };
            
            // Handle ICE candidates
            pc.onicecandidate = (event) => {
                if (event.candidate) {
                    // Send VOICE_ICE message: VOICE_ICE|fromId|targetId|candidateJson
                    this.sendMessage('VOICE_ICE|' + this.playerId + '|' + remotePlayerId + '|' + JSON.stringify(event.candidate));
                }
            };
            
            pc.oniceconnectionstatechange = () => {
                console.log('ICE connection state with', remotePlayerId, ':', pc.iceConnectionState);
                if (pc.iceConnectionState === 'connected') {
                    this.showNotification(`Voice connected to ${this.players[remotePlayerId]?.name || 'player'}`, 'success');
                } else if (pc.iceConnectionState === 'disconnected' || pc.iceConnectionState === 'failed') {
                    this.showNotification(`Voice disconnected from ${this.players[remotePlayerId]?.name || 'player'}`, 'error');
                }
            };
            
            // Create and send offer
            const offer = await pc.createOffer();
            await pc.setLocalDescription(offer);
            
            // Send VOICE_OFFER message: VOICE_OFFER|fromId|targetId|offerJson
            this.sendMessage('VOICE_OFFER|' + this.playerId + '|' + remotePlayerId + '|' + JSON.stringify(offer));
            console.log('Sent voice offer to:', remotePlayerId);
        } catch (err) {
            console.error('Error creating peer connection:', err);
        }
    }

    async handleVoiceOffer(data) {
        console.log('Received voice offer from:', data.from);
        
        try {
            const pc = new RTCPeerConnection({
                iceServers: [
                    { urls: 'stun:stun.l.google.com:19302' },
                    { urls: 'stun:stun1.l.google.com:19302' }
                ]
            });
            
            this.peerConnections[data.from] = pc;
            
            if (this.localStream) {
                this.localStream.getTracks().forEach(track => {
                    pc.addTrack(track, this.localStream);
                });
            }
            
            pc.ontrack = (event) => {
                console.log('Received remote track from:', data.from);
                const audio = new Audio();
                audio.srcObject = event.streams[0];
                audio.play().catch(e => console.log('Audio autoplay blocked:', e));
            };
            
            pc.onicecandidate = (event) => {
                if (event.candidate) {
                    // Send VOICE_ICE message: VOICE_ICE|fromId|targetId|candidateJson
                    this.sendMessage('VOICE_ICE|' + this.playerId + '|' + data.from + '|' + JSON.stringify(event.candidate));
                }
            };
            
            pc.oniceconnectionstatechange = () => {
                console.log('ICE connection state with', data.from, ':', pc.iceConnectionState);
            };
            
            const offer = JSON.parse(data.offer);
            await pc.setRemoteDescription(new RTCSessionDescription(offer));
            const answer = await pc.createAnswer();
            await pc.setLocalDescription(answer);
            
            // Send VOICE_ANSWER message: VOICE_ANSWER|fromId|targetId|answerJson
            this.sendMessage('VOICE_ANSWER|' + this.playerId + '|' + data.from + '|' + JSON.stringify(answer));
            console.log('Sent voice answer to:', data.from);
        } catch (err) {
            console.error('Error in handleVoiceOffer:', err);
        }
    }

    async handleVoiceAnswer(data) {
        console.log('Received voice answer from:', data.from);
        
        try {
            const pc = this.peerConnections[data.from];
            if (pc) {
                await pc.setRemoteDescription(new RTCSessionDescription(data.answer));
                console.log('Voice answer processed for:', data.from);
            } else {
                console.error('No peer connection found for:', data.from);
            }
        } catch (err) {
            console.error('Error in handleVoiceAnswer:', err);
        }
    }

    async handleVoiceIce(data) {
        console.log('Received ICE candidate from:', data.from);
        
        try {
            const pc = this.peerConnections[data.from];
            if (pc && data.candidate) {
                await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
                console.log('ICE candidate added for:', data.from);
            }
        } catch (err) {
            console.error('Error in handleVoiceIce:', err);
        }
    }

    // Text protocol voice message handlers
    async handleVoiceOfferText(parts) {
        // VOICE_OFFER|fromId|targetId|offerJson
        if (parts.length >= 4 && parts[2] === this.playerId) {
            try {
                // Reconstruct JSON in case it contained | characters
                const offerJson = parts.slice(3).join('|');
                const data = {
                    from: parts[1],
                    offer: offerJson
                };
                await this.handleVoiceOffer(data);
            } catch (err) {
                console.error('Error handling voice offer:', err);
            }
        }
    }

    async handleVoiceAnswerText(parts) {
        // VOICE_ANSWER|fromId|targetId|answerJson
        if (parts.length >= 4 && parts[2] === this.playerId) {
            try {
                // Reconstruct JSON in case it contained | characters
                const answerJson = parts.slice(3).join('|');
                const data = {
                    from: parts[1],
                    answer: JSON.parse(answerJson)
                };
                await this.handleVoiceAnswer(data);
            } catch (err) {
                console.error('Error handling voice answer:', err);
            }
        }
    }

    async handleVoiceIceText(parts) {
        // VOICE_ICE|fromId|targetId|candidateJson
        if (parts.length >= 4 && parts[2] === this.playerId) {
            try {
                // Reconstruct JSON in case it contained | characters
                const candidateJson = parts.slice(3).join('|');
                const data = {
                    from: parts[1],
                    candidate: JSON.parse(candidateJson)
                };
                await this.handleVoiceIce(data);
            } catch (err) {
                console.error('Error handling voice ICE:', err);
            }
        }
    }

    joinGame(name) {
        this.playerName = name;
        const serverInput = document.getElementById('server-address').value.trim() || 'localhost';
        
        // Parse server:port format, default to 8080 if no port specified
        let serverAddress = 'localhost';
        let port = 8080;
        
        if (serverInput.includes(':')) {
            const parts = serverInput.split(':');
            serverAddress = parts[0];
            port = parseInt(parts[1]) || 8080;
        } else {
            serverAddress = serverInput;
        }
        
        this.ws = new WebSocket(`ws://${serverAddress}:${port}/game`);
        
        this.ws.onopen = () => {
            // Send JOIN message in text protocol format: JOIN|playerName
            this.ws.send('JOIN|' + name);
            document.getElementById('join-screen').style.display = 'none';
            document.getElementById('game-hud').style.display = 'block';
            this.showNotification(`Welcome, ${name}!`, 'success');
            this.gameLoop();
        };
        
        this.ws.onmessage = (event) => {
            this.handleMessage(event.data);
        };
        
        this.ws.onclose = () => {
            this.showNotification('Connection lost. Refreshing...', 'error');
            setTimeout(() => location.reload(), 3000);
        };
        
        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            this.showNotification('Connection error', 'error');
        };
    }

    sendMessage(msg) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(msg);
        }
    }

    handleMessage(data) {
        // Parse text protocol: TYPE|field1|field2|...
        const parts = data.split('|');
        const msgType = parts[0];
        
        switch (msgType) {
            case 'UPDATE':
                this.handleUpdate(parts);
                break;
                
            case 'HIT':
                this.handleHit(parts);
                break;
                
            case 'KILL':
                this.handleKill(parts);
                break;
                
            case 'RESPAWN':
                this.handleRespawn(parts);
                break;
                
            case 'POWERUP_COLLECT':
                this.handlePowerUpCollect(parts);
                break;
                
            case 'CHAT':
                this.handleChat(parts);
                break;
                
            case 'VOICE_OFFER':
                this.handleVoiceOfferText(parts);
                break;
                
            case 'VOICE_ANSWER':
                this.handleVoiceAnswerText(parts);
                break;
                
            case 'VOICE_ICE':
                this.handleVoiceIceText(parts);
                break;
        }
    }
    
    handleUpdate(parts) {
        // Parse UPDATE message: UPDATE|tanksData|bulletsData|powerUpsData
        const tanksData = parts[1] || '';
        const bulletsData = parts[2] || '';
        const powerUpsData = parts[3] || '';
        
        // Track previous players to detect new ones
        const previousPlayers = Object.keys(this.players);
        
        // Parse tanks: id:name:x:y:angle:health:kills:deaths:alive:shield:speed:double;...
        this.players = {};
        if (tanksData) {
            tanksData.split(';').forEach(tankStr => {
                if (!tankStr) return;
                const fields = tankStr.split(':');
                const tank = {
                    id: fields[0],
                    name: fields[1],
                    x: parseInt(fields[2]),
                    y: parseInt(fields[3]),
                    angle: parseInt(fields[4]),
                    health: parseInt(fields[5]),
                    kills: parseInt(fields[6]),
                    deaths: parseInt(fields[7]),
                    alive: fields[8] === '1',
                    shield: fields[9] === '1',
                    speedBoost: fields[10] === '1',
                    doubleFire: fields[11] === '1'
                };
                this.players[tank.id] = tank;
                
                if (tank.id === this.playerId || (!this.playerId && tank.name === this.playerName)) {
                    this.playerId = tank.id;
                    this.myPlayer = tank;
                    if (this.health >= 25 && tank.health < 25) {
                        this.showNotification("Low Health!", "error");
                    }
                    this.health = tank.health;
                    this.isAlive = tank.alive;
                    this.kills = tank.kills;
                    this.deaths = tank.deaths;
                }
            });
        }
        
        // Check for new players and create voice connections if enabled
        if (this.voiceEnabled && this.playerId) {
            for (const playerId in this.players) {
                if (playerId !== this.playerId && 
                    !previousPlayers.includes(playerId) && 
                    !this.peerConnections[playerId]) {
                    console.log('New player detected, creating voice connection:', playerId);
                    this.createPeerConnection(playerId);
                }
            }
        }
        
        // Parse bullets: id:ownerId:x:y:vx:vy;...
        this.bullets = {};
        if (bulletsData) {
            bulletsData.split(';').forEach(bulletStr => {
                if (!bulletStr) return;
                const fields = bulletStr.split(':');
                const bullet = {
                    id: fields[0],
                    ownerId: fields[1],
                    x: parseInt(fields[2]),
                    y: parseInt(fields[3]),
                    dx: parseInt(fields[4]),
                    dy: parseInt(fields[5])
                };
                this.bullets[bullet.id] = bullet;
            });
        }
        
        // Parse power-ups: id:type:x:y;...
        this.powerUps = {};
        if (powerUpsData) {
            powerUpsData.split(';').forEach(puStr => {
                if (!puStr) return;
                const fields = puStr.split(':');
                const powerUp = {
                    id: fields[0],
                    type: parseInt(fields[1]),
                    x: parseInt(fields[2]),
                    y: parseInt(fields[3])
                };
                this.powerUps[powerUp.id] = powerUp;
            });
        }
        
        this.updateHUD();
    }
    
    handleHit(parts) {
        // HIT|targetId|shooterId|damage
        const targetId = parts[1];
        const shooterId = parts[2];
        const damage = parseInt(parts[3] || 20);
        
        if (targetId === this.playerId) {
            this.health -= damage;
            if (this.health <= 0) {
                this.deaths++;
                this.showRespawnScreen();
                this.createExplosion(this.myPlayer.x, this.myPlayer.y, 30, '#ff0000');
            }
            this.showNotification('You were hit!', 'damage');
        }
    }
    
    handleKill(parts) {
        // KILL|killerId|victimId
        const killerId = parts[1];
        const victimId = parts[2];
        
        const killer = this.players[killerId];
        const victim = this.players[victimId];
        
        if (killer && victim) {
            this.addKillFeed(killer.name, victim.name);
        }
        
        if (victimId === this.playerId) {
            this.showNotification('You were killed!', 'error');
            this.isAlive = false;
        } else if (killerId === this.playerId) {
            this.showNotification('Kill!', 'success');
        }
    }
    
    handleRespawn(parts) {
        // RESPAWN|tankId|x|y
        const tankId = parts[1];
        if (tankId === this.playerId) {
            this.showNotification('Respawned!', 'success');
            this.isAlive = true;
        }
    }
    
    handlePowerUpCollect(parts) {
        // POWERUP_COLLECT|powerUpId|tankId|type
        const tankId = parts[2];
        const type = parseInt(parts[3]);
        
        if (tankId === this.playerId) {
            const typeName = ['Shield', 'Speed Boost', 'Double Fire'][type];
            this.showNotification(`Collected ${typeName}!`, 'success');
        }
    }
    
    handleChat(parts) {
        // CHAT|message
        const message = parts[1] || '';
        this.addChatMessage(message);
    }

    tryFire() {
        const now = Date.now();
        if (now - this.lastFireTime >= this.fireRate && this.isAlive) {
            // Send FIRE message: FIRE|angle
            this.sendMessage('FIRE|' + Math.round(this.angle));
            this.lastFireTime = now;
            this.screenShake();
        }
    }

    sendMove() {
        if (!this.myPlayer || !this.isAlive) return;
        
        let x = this.myPlayer.x;
        let y = this.myPlayer.y;
        const speed = this.myPlayer.speedBoost ? 15 : 10;

        if (this.keys['KeyW'] || this.keys['ArrowUp']) y -= speed;
        if (this.keys['KeyS'] || this.keys['ArrowDown']) y += speed;
        if (this.keys['KeyA'] || this.keys['ArrowLeft']) x -= speed;
        if (this.keys['KeyD'] || this.keys['ArrowRight']) x += speed;

        x = Math.max(15, Math.min(1185, x));
        y = Math.max(15, Math.min(785, y));

        if (x !== this.myPlayer.x || y !== this.myPlayer.y || this.angle !== this.myPlayer.angle) {
            // Send MOVE message: MOVE|x|y|angle
            this.sendMessage('MOVE|' + Math.round(x) + '|' + Math.round(y) + '|' + Math.round(this.angle));
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
            // Send CHAT message: CHAT|message
            this.sendMessage('CHAT|' + msg);
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

    updateHUD() {
        // Update stats with color coding
        const healthText = document.getElementById('health-text');
        healthText.textContent = this.health;
        
        // Add health color classes
        healthText.classList.remove('health-critical', 'health-warning', 'health-good');
        if (this.health < 25) {
            healthText.classList.add('health-critical');
        } else if (this.health < 50) {
            healthText.classList.add('health-warning');
        } else {
            healthText.classList.add('health-good');
        }
        
        document.getElementById('kills-text').textContent = this.kills;
        document.getElementById('deaths-text').textContent = this.deaths;
        
        // Update power-up indicator
        const indicator = document.getElementById('power-up-indicator');
        if (this.myPlayer) {
            let powerUpText = '';
            if (this.myPlayer.shield) powerUpText = '<i class="fas fa-shield-alt"></i> SHIELD ACTIVE';
            else if (this.myPlayer.speedBoost) powerUpText = '<i class="fas fa-bolt"></i> SPEED BOOST';
            else if (this.myPlayer.doubleFire) powerUpText = '<i class="fas fa-fire"></i> DOUBLE FIRE';
            
            if (powerUpText) {
                indicator.innerHTML = powerUpText;
                indicator.style.display = 'block';
            } else {
                indicator.style.display = 'none';
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
        
        scoresDiv.innerHTML = sortedPlayers.map((p, index) => `
            <div class="score-item ${p.id === this.playerId ? 'self' : ''}">
                <span class="score-rank">#${index + 1}</span>
                <span class="score-name">${p.name}</span>
                <span class="score-value">${p.score}</span>
            </div>
        `).join('');
    }

    showNotification(message, type = 'info') {
        const notifDiv = document.getElementById('notifications');
        const notif = document.createElement('div');
        notif.className = 'notification';
        notif.style.borderColor = type === 'error' ? '#ff4444' : type === 'success' ? '#00ff88' : '#ffaa00';
        notif.textContent = message;
        
        notifDiv.appendChild(notif);
        setTimeout(() => notif.remove(), 3000);
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
        this.render();
        this.renderMinimap();
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
            const colors = { SHIELD: '#00ffff', SPEED_BOOST: '#ffff00', DOUBLE_FIRE: '#ff00ff' };
            ctx.fillStyle = colors[pu.type] || '#ffffff';
            ctx.shadowBlur = 20;
            ctx.shadowColor = ctx.fillStyle;
            
            // Rotating effect
            ctx.save();
            ctx.translate(pu.x, pu.y);
            ctx.rotate((Date.now() / 500) % (2 * Math.PI));
            ctx.fillRect(-12, -12, 24, 24);
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
            
            // Shield effect
            if (p.shield) {
                ctx.strokeStyle = '#00ffff';
                ctx.lineWidth = 3;
                ctx.shadowBlur = 15;
                ctx.shadowColor = '#00ffff';
                ctx.strokeRect(p.x - 22, p.y - 22, 44, 44);
                ctx.shadowBlur = 0;
            }
            
            // Tank body
            ctx.shadowBlur = 10;
            ctx.shadowColor = ctx.fillStyle;
            ctx.fillRect(p.x - 18, p.y - 18, 36, 36);
            ctx.shadowBlur = 0;
            
            // Turret
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 4;
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            const rad = p.angle * Math.PI / 180;
            ctx.lineTo(p.x + Math.cos(rad) * 25, p.y + Math.sin(rad) * 25);
            ctx.stroke();
            
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
            
            // Voice indicator - show mic icon if peer connection exists
            let nameText = p.name;
            if (this.peerConnections[p.id] && this.peerConnections[p.id].iceConnectionState === 'connected') {
                nameText = '🎤 ' + p.name;
            }
            
            ctx.fillText(nameText, p.x, p.y - 35);
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