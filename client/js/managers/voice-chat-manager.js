// Voice Chat Manager - Handles WebRTC voice communication
export class VoiceChatManager {
    constructor(gameClient) {
        this.game = gameClient;
        this.peerConnections = {};
        this.localStream = null;
        this.voiceEnabled = false;
    }

    async init() {
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

    toggle(showNotification) {
        if (!this.localStream) {
            showNotification('Microphone not available', 'error');
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
            showNotification('Voice chat enabled', 'success');
            this.broadcastOffer();
        } else {
            btn.classList.remove('active');
            btn.innerHTML = '<i class="fas fa-microphone" aria-hidden="true"></i>';
            btn.setAttribute('aria-pressed', 'false');
            btn.setAttribute('aria-label', 'Enable voice chat');
            showNotification('Voice chat muted', 'info');
        }
    }

    async broadcastOffer() {
        for (const playerId in this.game.players) {
            if (playerId !== this.game.playerId && !this.peerConnections[playerId]) {
                await this.createPeerConnection(playerId);
            }
        }
    }

    async createPeerConnection(remotePlayerId) {
        const pc = new RTCPeerConnection({
            iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
        });
        
        this.peerConnections[remotePlayerId] = pc;
        
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
                this.game.networkManager.sendMessage({
                    type: 'voice-ice',
                    target: remotePlayerId,
                    candidate: event.candidate
                });
            }
        };
        
        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);
        
        this.game.networkManager.sendMessage({
            type: 'voice-offer',
            target: remotePlayerId,
            offer: offer
        });
    }

    async handleOffer(data) {
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
                this.game.networkManager.sendMessage({
                    type: 'voice-ice',
                    target: data.from,
                    candidate: event.candidate
                });
            }
        };
        
        await pc.setRemoteDescription(new RTCSessionDescription(data.offer));
        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);
        
        this.game.networkManager.sendMessage({
            type: 'voice-answer',
            target: data.from,
            answer: answer
        });
    }

    async handleAnswer(data) {
        const pc = this.peerConnections[data.from];
        if (pc) {
            await pc.setRemoteDescription(new RTCSessionDescription(data.answer));
        }
    }

    async handleIce(data) {
        const pc = this.peerConnections[data.from];
        if (pc) {
            await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
        }
    }
}
