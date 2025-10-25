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

        // Mouse
        this.canvas.addEventListener('mousemove', (e) => {
            const rect = this.canvas.getBoundingClientRect();
            this.mouseX = e.clientX - rect.left;
            this.mouseY = e.clientY - rect.top;
            
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
        document.getElementById('join-btn').addEventListener('click', () => {
            const name = document.getElementById('player-name').value.trim();
            if (name) {
                this.joinGame(name);
            } else {
                this.showNotification('Please enter a callsign!', 'error');
            }
        });
        
        document.getElementById('player-name').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                document.getElementById('join-btn').click();
            }
        });
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
            btn.innerHTML = '<i class="fas fa-microphone-slash"></i>';
            this.showNotification('Voice chat enabled', 'success');
            this.broadcastVoiceOffer();
        } else {
            btn.classList.remove('active');
            btn.innerHTML = '<i class="fas fa-microphone"></i>';
            this.showNotification('Voice chat muted', 'info');
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

    joinGame(name) {
        this.playerName = name;
        this.ws = new WebSocket('ws://localhost:8080/game');
        
        this.ws.onopen = () => {
            this.sendMessage({ type: 'join', name: name });
            document.getElementById('join-screen').style.display = 'none';
            document.getElementById('game-hud').style.display = 'block';
            this.showNotification(`Welcome, ${name}!`, 'success');
            this.gameLoop();
        };
        
        this.ws.onmessage = (event) => {
            this.handleMessage(JSON.parse(event.data));
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
        if (now - this.lastFireTime >= this.fireRate && this.isAlive) {
            this.sendMessage({ type: 'fire' });
            this.lastFireTime = now;
            this.screenShake();
        }
    }

    sendMove() {
        if (!this.myPlayer || !this.isAlive) return;
        
        let x = this.myPlayer.x;
        let y = this.myPlayer.y;
        const speed = this.myPlayer.speedBoost ? 5 : 3;

        if (this.keys['KeyW'] || this.keys['ArrowUp']) y -= speed;
        if (this.keys['KeyS'] || this.keys['ArrowDown']) y += speed;
        if (this.keys['KeyA'] || this.keys['ArrowLeft']) x -= speed;
        if (this.keys['KeyD'] || this.keys['ArrowRight']) x += speed;

        x = Math.max(15, Math.min(1185, x));
        y = Math.max(15, Math.min(785, y));

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

    updateHUD() {
        // Update stats
        document.getElementById('health-text').textContent = this.health;
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
        ctx.clearRect(0, 0, 1200, 800);

        // Background grid
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.1)';
        ctx.lineWidth = 1;
        for (let x = 0; x <= 1200; x += 50) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, 800);
            ctx.stroke();
        }
        for (let y = 0; y <= 800; y += 50) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(1200, y);
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
            
            const mx = (p.x / 1200) * 200;
            const my = (p.y / 800) * 150;
            
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