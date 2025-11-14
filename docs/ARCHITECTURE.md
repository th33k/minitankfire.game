# Architecture Documentation

## System Overview

Tank Arena is a real-time multiplayer game built with a client-server architecture using WebSocket communication. The system is designed with separation of concerns, where the server handles authoritative game state and physics, while clients render the game and handle user input.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT TIER                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │   HTML5    │  │    CSS3    │  │ JavaScript │            │
│  │   Canvas   │  │  Styling   │  │   ES6+     │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│         │                │                │                  │
│         └────────────────┴────────────────┘                  │
│                          │                                   │
│            ┌─────────────┴─────────────┐                    │
│            │    Game Client Manager     │                    │
│            │  - Renderer                │                    │
│            │  - Input Manager           │                    │
│            │  - Network Manager         │                    │
│            │  - UI Manager              │                    │
│            │  - Voice Chat Manager      │                    │
│            └─────────────┬─────────────┘                    │
└──────────────────────────┼──────────────────────────────────┘
                           │ WebSocket (ws://)
                           │ JSON Messages
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                SERVER TIER│                                  │
│            ┌─────────────┴─────────────┐                    │
│            │    GameServer (TCP)        │                    │
│            │  - ServerSocket (8080)     │                    │
│            │  - ExecutorService         │                    │
│            │  - Client Thread Pool      │                    │
│            └─────────────┬─────────────┘                    │
│                          │                                   │
│         ┌────────────────┼────────────────┐                 │
│         │                │                │                 │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐        │
│  │  WebSocket  │  │   Client    │  │   Game      │        │
│  │   Handler   │  │   Handler   │  │   Room      │        │
│  │             │  │             │  │             │        │
│  │ - Handshake │  │ - Message   │  │ - Game Loop │        │
│  │ - Framing   │  │ - Protocol  │  │ - Physics   │        │
│  │ - Encoding  │  │ - State     │  │ - Collision │        │
│  └─────────────┘  └─────────────┘  └──────┬──────┘        │
│                                            │                 │
│                    ┌───────────────────────┴──┐             │
│                    │     Game Models          │             │
│                    │  - Player                │             │
│                    │  - Bullet                │             │
│                    │  - PowerUp               │             │
│                    └──────────────────────────┘             │
└─────────────────────────────────────────────────────────────┘
```

## Server Architecture

### Core Components

#### 1. GameServer (Main Entry Point)
**Location**: `server/src/main/java/com/minitankfire/server/GameServer.java`

**Responsibilities:**
- Initialize TCP ServerSocket on port 8080
- Accept incoming client connections
- Manage thread pool for concurrent client handling
- Initialize and maintain GameRoom instance
- Handle graceful shutdown

**Key Design Decisions:**
- Uses `ExecutorService` with fixed thread pool (100 threads max)
- Each client connection runs in separate thread
- TCP socket options: `setTcpNoDelay(true)` for low latency

```java
ServerSocket serverSocket = new ServerSocket(8080);
ExecutorService clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
```

#### 2. WebSocketHandler (Protocol Layer)
**Location**: `server/src/main/java/com/minitankfire/network/WebSocketHandler.java`

**Responsibilities:**
- Implement RFC 6455 WebSocket protocol
- Perform HTTP upgrade handshake
- Encode/decode WebSocket frames
- Handle control frames (ping, pong, close)
- Manage binary and text messages

**Protocol Implementation:**
```java
// Handshake - Compute WebSocket accept key
String key = headers.get("sec-websocket-key");
String accept = Base64.getEncoder().encodeToString(
    MessageDigest.getInstance("SHA-1").digest(
        (key + WEBSOCKET_GUID).getBytes()
    )
);
```

**Frame Structure:**
```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-------+-+-------------+-------------------------------+
|F|R|R|R| opcode|M| Payload len |    Extended payload length    |
|I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
|N|V|V|V|       |S|             |   (if payload len==126/127)   |
| |1|2|3|       |K|             |                               |
+-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
|     Extended payload length continued, if payload len == 127  |
+ - - - - - - - - - - - - - - - +-------------------------------+
|                               |Masking-key, if MASK set to 1  |
+-------------------------------+-------------------------------+
| Masking-key (continued)       |          Payload Data         |
+-------------------------------- - - - - - - - - - - - - - - - +
:                     Payload Data continued ...                :
+ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
|                     Payload Data continued ...                |
+---------------------------------------------------------------+
```

#### 3. ClientHandler (Connection Manager)
**Location**: `server/src/main/java/com/minitankfire/network/ClientHandler.java`

**Responsibilities:**
- Manage individual client connection lifecycle
- Parse incoming messages
- Route messages to appropriate handlers
- Broadcast game state updates
- Handle disconnections gracefully

**Message Flow:**
```
Client → WebSocket Frame → ClientHandler → GameRoom
                                              ↓
                                         Process Logic
                                              ↓
GameRoom → ClientHandler → WebSocket Frame → Client
```

#### 4. GameRoom (Game Logic Core)
**Location**: `server/src/main/java/com/minitankfire/game/GameRoom.java`

**Responsibilities:**
- Main game loop (20 FPS / 50ms tick)
- Player state management
- Bullet physics and collision detection
- Power-up spawning and collection
- Score tracking and leaderboard
- Respawn system

**Thread Safety:**
```java
// All game state uses thread-safe collections
private Map<String, Player> players = new ConcurrentHashMap<>();
private Map<String, Bullet> bullets = new ConcurrentHashMap<>();
private Map<String, PowerUp> powerUps = new ConcurrentHashMap<>();
```

**Game Loop Architecture:**
```java
while (gameRunning) {
    long startTime = System.currentTimeMillis();
    
    // 1. Update player power-ups
    updatePowerUpTimers();
    
    // 2. Update bullet positions
    updateBullets();
    
    // 3. Check collisions
    checkCollisions();
    
    // 4. Spawn power-ups
    spawnPowerUps();
    
    // 5. Broadcast state
    broadcastGameState();
    
    // 6. Maintain 50ms tick rate
    long elapsed = System.currentTimeMillis() - startTime;
    Thread.sleep(Math.max(0, GAME_TICK_MS - elapsed));
}
```

### Data Models

#### Player Model
```java
public class Player {
    private String id;           // Unique identifier
    private String name;         // Display name
    private int x, y;           // Position (pixels)
    private int angle;          // Rotation (degrees)
    private int score;          // Kill count
    private int health;         // 0-100
    private boolean alive;      // Death state
    private boolean hasShield;  // Shield power-up
    private boolean speedBoost; // Speed power-up
    private boolean doubleFire; // Double fire power-up
}
```

#### Bullet Model
```java
public class Bullet {
    private String id;          // Unique identifier
    private String playerId;    // Owner
    private int x, y;          // Position
    private double vx, vy;     // Velocity vector
    private long spawnTime;    // For lifetime tracking
}
```

#### PowerUp Model
```java
public class PowerUp {
    private String id;          // Unique identifier
    private String type;        // shield, speed, doubleFire
    private int x, y;          // Position
    private long spawnTime;    // For lifetime tracking
}
```

## Client Architecture

### Core Components

#### 1. GameClient (Main Controller)
**Location**: `client/js/game-client.js`

**Responsibilities:**
- Initialize all managers
- Maintain local game state
- Handle game loop and rendering
- Process server messages
- Manage settings and preferences

**Manager Instances:**
```javascript
class GameClient {
    constructor() {
        this.uiManager = new UIManager(this);
        this.networkManager = new NetworkManager(this);
        this.voiceChatManager = new VoiceChatManager(this);
        this.renderer = new Renderer(this.canvas, this.minimapCanvas, this);
        this.inputManager = new InputManager(this);
    }
}
```

#### 2. Renderer (Graphics Engine)
**Location**: `client/js/core/renderer.js`

**Responsibilities:**
- Canvas 2D rendering
- Player tank visualization
- Bullet trails and effects
- Power-up animations
- Minimap rendering
- Screen shake effects

**Rendering Pipeline:**
```javascript
render() {
    // 1. Clear canvas
    this.clearCanvas();
    
    // 2. Draw background grid
    this.drawGrid();
    
    // 3. Draw power-ups
    this.drawPowerUps();
    
    // 4. Draw bullets with trails
    this.drawBullets();
    
    // 5. Draw players (tanks)
    this.drawPlayers();
    
    // 6. Draw UI overlays
    this.drawUI();
    
    // 7. Update minimap
    this.updateMinimap();
}
```

**Tank Rendering:**
```
    Turret (rotating)
         │
    ┌────┴────┐
    │  Tank   │  ← Main body (square)
    │  Body   │  ← Rotates with movement
    └─────────┘
    ┌─┐ ┌─┐
    └─┘ └─┘     ← Tracks (decorative)
```

#### 3. InputManager (User Input)
**Location**: `client/js/core/input-manager.js`

**Responsibilities:**
- Keyboard input (WASD)
- Mouse position and clicks
- Input state tracking
- Send input to server

**Input State:**
```javascript
{
    keys: {
        w: false, a: false, s: false, d: false
    },
    mouse: {
        x: 0, y: 0, angle: 0, down: false
    }
}
```

#### 4. NetworkManager (Communication)
**Location**: `client/js/managers/network-manager.js`

**Responsibilities:**
- WebSocket connection management
- Message serialization/deserialization
- Ping/pong latency tracking
- Reconnection logic
- Protocol handling

**Message Types:**
```javascript
// Client → Server
{ type: 'join', name: 'Player' }
{ type: 'input', keys: {...}, angle: 45 }
{ type: 'fire', angle: 45 }
{ type: 'chat', message: 'Hello' }
{ type: 'ping', timestamp: 1234567890 }

// Server → Client
{ type: 'init', playerId: 'uuid', x: 100, y: 100 }
{ type: 'game_state', players: [...], bullets: [...], powerUps: [...] }
{ type: 'pong', timestamp: 1234567890 }
{ type: 'player_died', killerId: 'uuid', victimId: 'uuid' }
```

#### 5. UIManager (User Interface)
**Location**: `client/js/managers/ui-manager.js`

**Responsibilities:**
- Join screen management
- Settings panel
- Leaderboard updates
- Kill feed notifications
- Chat system
- Network stats display

#### 6. VoiceChatManager (Voice Communication)
**Location**: `client/js/managers/voice-chat-manager.js`

**Responsibilities:**
- WebRTC peer connections
- Audio stream management
- Mute/unmute controls
- Peer discovery

## Communication Protocol

### Message Flow Diagram

```
┌─────────┐                                    ┌─────────┐
│ Client  │                                    │ Server  │
└────┬────┘                                    └────┬────┘
     │                                              │
     │  1. WebSocket Handshake (HTTP Upgrade)      │
     ├─────────────────────────────────────────────>│
     │                                              │
     │  2. 101 Switching Protocols                 │
     │<─────────────────────────────────────────────┤
     │                                              │
     │  3. join message (name)                     │
     ├─────────────────────────────────────────────>│
     │                                              │
     │  4. init message (playerId, spawn pos)      │
     │<─────────────────────────────────────────────┤
     │                                              │
     │  5. input messages (continuous)             │
     ├─────────────────────────────────────────────>│
     │                                              │
     │  6. game_state broadcasts (20 FPS)          │
     │<─────────────────────────────────────────────┤
     │                                              │
     │  7. fire message (on click)                 │
     ├─────────────────────────────────────────────>│
     │                                              │
     │  8. ping (every 2s)                         │
     ├─────────────────────────────────────────────>│
     │                                              │
     │  9. pong (immediate)                        │
     │<─────────────────────────────────────────────┤
     │                                              │
```

### State Synchronization

**Server as Authority:**
- Server maintains authoritative game state
- Client sends inputs, not positions
- Server validates all actions
- Server broadcasts confirmed state

**Client Prediction:**
- Client immediately moves locally
- Reconciled with server state on update
- Reduces perceived latency

## Performance Considerations

### Server Optimizations

1. **Thread Pool**: Fixed-size thread pool prevents resource exhaustion
2. **ConcurrentHashMap**: Lock-free reads for game state
3. **TCP_NODELAY**: Disabled Nagle's algorithm for low latency
4. **Fixed Tick Rate**: Predictable CPU usage (50ms)
5. **Spatial Indexing**: Simple grid-based collision detection

### Client Optimizations

1. **RequestAnimationFrame**: Synced with display refresh
2. **Canvas Double Buffering**: Smooth rendering
3. **Object Pooling**: Reuse particle objects
4. **Culling**: Only render visible entities
5. **Delta Time**: Frame-rate independent movement

## Security Considerations

### Server-Side Validation

```java
// Validate all client inputs
if (x < MIN_X || x > MAX_X || y < MIN_Y || y > MAX_Y) {
    // Reject invalid position
    return;
}

// Rate limiting for fire command
long now = System.currentTimeMillis();
if (now - player.getLastFireTime() < FIRE_RATE_MS) {
    // Reject rapid fire
    return;
}
```

### Connection Security

- WebSocket origin checking (configurable)
- Input sanitization for player names
- Chat message filtering
- Connection rate limiting
- Max payload size enforcement

## Scalability

### Current Limitations

- Single game room (all players in one world)
- Single server instance
- In-memory state (no persistence)
- Max 100 concurrent players

### Future Scaling Options

1. **Multiple Rooms**: Separate GameRoom instances
2. **Load Balancing**: Multiple server instances
3. **Redis**: Shared state across servers
4. **Database**: Persistent player accounts
5. **CDN**: Static client asset delivery

## Technology Stack Summary

### Server
- **Language**: Java 11+
- **Build Tool**: Maven 3.6+
- **Networking**: java.net (ServerSocket, Socket)
- **Concurrency**: java.util.concurrent (ExecutorService, ConcurrentHashMap)
- **Protocol**: Custom WebSocket RFC 6455 implementation
- **Serialization**: Manual JSON (no libraries)

### Client
- **Languages**: HTML5, CSS3, JavaScript ES6+
- **Graphics**: Canvas 2D API
- **Networking**: Browser WebSocket API
- **Audio**: HTML5 Audio API
- **Voice**: WebRTC API
- **Build**: None (vanilla JavaScript modules)

## Deployment Architecture

```
┌──────────────────────────────────────────────┐
│           Client Deployment                   │
│  ┌────────────────────────────────────────┐  │
│  │   Static File Server (Python/Nginx)    │  │
│  │   Port: 3000                           │  │
│  │   Files: HTML, CSS, JS, Audio         │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
                     │
                     │ HTTP/HTTPS
                     │
┌──────────────────────────────────────────────┐
│           Server Deployment                   │
│  ┌────────────────────────────────────────┐  │
│  │   Java GameServer                      │  │
│  │   Port: 8080                           │  │
│  │   Protocol: WebSocket (ws://)          │  │
│  │   JVM: Java 11+                        │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

## Monitoring & Debugging

### Server Logs
```
[SERVER] Waiting for connections...
[CONNECTION] New client connected: 192.168.1.100:52341
[HANDSHAKE] WebSocket handshake successful
[GAME] Player 'Tank123' joined (ID: abc-123)
[GAME] Player 'Tank123' fired bullet
[GAME] Bullet hit! Player 'Tank456' health: 80
[GAME] Player 'Tank456' collected power-up: shield
```

### Client Debug Console
```javascript
// Network stats
console.log('Ping:', this.currentPing, 'ms');
console.log('Players:', Object.keys(this.players).length);

// Performance metrics
console.log('FPS:', this.fps);
console.log('Frame time:', this.frameTime, 'ms');
```

---

**Last Updated**: November 14, 2025
