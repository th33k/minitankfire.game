// Network Manager - Handles WebSocket communication
export class NetworkManager {
    constructor(gameClient) {
        this.game = gameClient;
        this.ws = null;
        this.lobbyWs = null;
    }

    connectToLobby(serverAddress) {
        try {
            this.lobbyWs = new WebSocket(`ws://${serverAddress}:8080/game`);
            
            this.lobbyWs.onopen = () => {
                this.sendLobbyMessage({ type: 'lobby_info' });
            };
            
            this.lobbyWs.onmessage = (event) => {
                const msg = JSON.parse(event.data);
                if (msg.type === 'lobby_info') {
                    this.game.uiManager.updateLobbyDisplay(msg);
                }
            };
            
            this.lobbyWs.onerror = (error) => {
                console.log('Lobby connection error:', error);
                document.getElementById('lobby-player-count').textContent = '?';
            };
            
            this.lobbyWs.onclose = () => {
                setTimeout(() => {
                    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
                        this.connectToLobby(serverAddress);
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

    connectToGame(name, serverAddress, onOpen, onMessage, onClose, onError) {
        const wsUrl = `ws://${serverAddress}:8080/game`;
        console.log('Attempting to connect to:', wsUrl);
        
        this.ws = new WebSocket(wsUrl);
        
        const connectionTimeout = setTimeout(() => {
            if (this.ws.readyState !== WebSocket.OPEN) {
                this.ws.close();
                onError('Connection timeout. Please check server address.');
            }
        }, 10000);
        
        this.ws.onopen = () => {
            clearTimeout(connectionTimeout);
            console.log('WebSocket connected');
            this.sendMessage({ type: 'join', name: name });
            onOpen();
        };
        
        this.ws.onmessage = (event) => {
            try {
                onMessage(JSON.parse(event.data));
            } catch (e) {
                console.error('Error parsing message:', e);
            }
        };
        
        this.ws.onclose = () => {
            clearTimeout(connectionTimeout);
            console.log('WebSocket closed');
            onClose();
        };
        
        this.ws.onerror = (error) => {
            clearTimeout(connectionTimeout);
            console.error('WebSocket error:', error);
            onError('Failed to connect to server. Please verify the server address.');
        };
    }

    sendMessage(msg) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(msg));
        }
    }

    closeLobby() {
        if (this.lobbyWs) {
            this.lobbyWs.close();
            this.lobbyWs = null;
        }
    }
}
