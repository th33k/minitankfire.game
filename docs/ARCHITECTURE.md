# 🏗️ Project Architecture & Technical Design

## Table of Contents

1. [System Overview](#system-overview)
2. [Server Architecture](#server-architecture)
3. [Package Structure](#package-structure)
4. [Client Architecture](#client-architecture)
5. [Network Protocol](#network-protocol)
6. [Data Structures](#data-structures)
7. [Threading Model](#threading-model)
8. [Technology Stack](#technology-stack)

---

## System Overview

**Mini Tank Fire** is a real-time multiplayer online game built using a **Client-Server Architecture** with **WebSocket** communication.

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Browser Clients                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Client 1    │  │  Client 2    │  │  Client N    │      │
│  │ HTML5 Canvas │  │ HTML5 Canvas │  │ HTML5 Canvas │      │
│  │ WebSocket    │  │ WebSocket    │  │ WebSocket    │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼──────────────────┼──────────────────┼──────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │ WebSocket (TCP Port 8080)
                             ▼
         ┌────────────────────────────────────────┐
         │     Tank Game Server (Java)            │
         │                                        │
         │  ┌──────────────────────────────────┐ │
         │  │  GameServer                      │ │
         │  │  - ServerSocket (Port 8080)      │ │
         │  │  - Thread Pool (100 max clients) │ │
         │  └──────────────────────────────────┘ │
         │                                        │
         │  ┌──────────────────────────────────┐ │
         │  │  GameRoom                        │ │
         │  │  - Game Logic & State            │ │
         │  │  - Player Management             │ │
         │  │  - Physics & Collision Detection │ │
         │  │  - Power-up Management           │ │
         │  └──────────────────────────────────┘ │
         │                                        │
         │  ┌──────────────────────────────────┐ │
         │  │  ClientHandler Threads (N)       │ │
         │  │  - WebSocket Protocol Handling   │ │
         │  │  - Message Processing            │ │
         │  │  - Player Input Processing       │ │
         │  └──────────────────────────────────┘ │
         │                                        │
         └────────────────────────────────────────┘
```

---

## Server Architecture

### Package Organization

The server is organized into **5 focused packages** for clarity and maintainability:

```
com/minitankfire/
├── server/          🖥️ Server bootstrap & lifecycle
│   └── GameServer.java
├── network/         🌐 Network & WebSocket protocol
│   ├── WebSocketHandler.java
│   └── ClientHandler.java
├── game/            🎮 Game logic & state management
│   └── GameRoom.java
├── model/           📊 Game entity data structures
│   ├── Player.java
│   ├── Bullet.java
│   └── PowerUp.java
└── util/            🔧 Utility functions
    └── JsonUtil.java
```

**Benefits:**
- ✅ Clear separation of concerns
- ✅ Easy to locate specific functionality
- ✅ Independent testing per package
- ✅ Scalable for future enhancements
- ✅ Reduced code coupling

### Core Components

#### 1. **GameServer** (`server/GameServer.java`)

**Responsibility**: Bootstrap and manage the WebSocket server lifecycle

**Key Features**:
- Creates `ServerSocket` listening on port 8080
- Manages thread pool (ExecutorService) for concurrent clients
- Accepts incoming client connections
- Graceful shutdown handling with Ctrl+C support
- Configurable port via command-line arguments

**Package**: `com.minitankfire.server`

**Location**: `server/src/main/java/com/minitankfire/server/GameServer.java`

**Main Methods**:
- `main(String[] args)` - Entry point
- `start()` - Server loop (accepts connections)
- `shutdown()` - Graceful cleanup

**Threading**:
- Main thread accepts connections
- Thread pool runs ClientHandler tasks (max 100 concurrent)
- GameRoom runs separate dedicated game loop thread

---

#### 2. **ClientHandler** (`network/ClientHandler.java`)

**Responsibility**: Handle individual client connections

**Key Features**:
- Manages per-client WebSocket connection (runs in thread pool)
- Routes incoming messages to appropriate handlers
- Separates message handling logic into focused methods
- Cleans up resources on disconnection

**Package**: `com.minitankfire.network`

**Location**: `network/src/main/java/com/minitankfire/network/ClientHandler.java`

**Message Flow**:
```
1. Client connects (TCP connection)
2. WebSocket handshake performed
3. Client sends "join" message
4. Handler routes to GameRoom.addPlayer()
5. Handler enters receive loop:
   - Reads message from client
   - Parses JSON to identify type
   - Dispatches to specialized handler method
   - Repeats until disconnection
```

**Supported Message Types**:
- `join` - Player joins game
- `move` - Position and angle update
- `fire` - Fire weapon
- `chat` - Text message
- `voice-*` - WebRTC voice signals

**Thread Safety**:
- Each client runs in its own thread (from pool)
- Uses thread-safe GameRoom APIs
- No synchronization needed on per-client data

---

#### 3. **GameRoom** (`game/GameRoom.java`)

**Responsibility**: Core game logic and state management

**Key Features**:
- Manages game entities: Players, Bullets, PowerUps
- Runs game loop at 20 FPS in dedicated thread
- Physics engine (collision detection)
- Scoring system
- Power-up spawning and management
- Broadcasting game state to all clients
- Respawn logic and duration management

**Package**: `com.minitankfire.game`

**Location**: `game/src/main/java/com/minitankfire/game/GameRoom.java`

**Data Collections** (All Thread-Safe):
```java
private Map<String, Player> players = new ConcurrentHashMap<>();
private Map<String, Bullet> bullets = new ConcurrentHashMap<>();
private Map<String, PowerUp> powerUps = new ConcurrentHashMap<>();
private Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
```

**Game Constants** (Configurable):
```java
MAP_WIDTH = 800                           // Canvas width
MAP_HEIGHT = 600                          // Canvas height
PLAYER_SPEED = 3                          // pixels/frame
BULLET_SPEED = 8                          // pixels/frame
GAME_TICK_MS = 50                         // 20 FPS
RESPAWN_TIME_MS = 3000                    // 3 seconds
SHIELD_DURATION_MS = 5000                 // 5 seconds
SPEED_BOOST_DURATION_MS = 3000            // 3 seconds
DOUBLE_FIRE_DURATION_MS = 10000           // 10 seconds
```

**Game Loop** (20 FPS = 50ms per tick):
```
1. Update bullet positions
   ├─ Move each bullet by velocity
   ├─ Remove expired or out-of-bounds bullets
   └─ Check for 3-second lifetime

2. Check collisions
   ├─ Bullet vs Player (with shield detection)
   ├─ Power-up collection
   └─ Handle hits and deaths

3. Update power-ups
   ├─ Remove expired power-ups
   ├─ Randomly spawn new ones
   └─ Update on-map availability

4. Update player power-up durations
   ├─ Shield expiration
   ├─ Speed boost expiration
   └─ Double fire expiration

5. Respawn dead players
   ├─ Check respawn timer (3s cooldown)
   ├─ Reset health and position
   └─ Make player alive

6. Broadcast updated state to all clients
   └─ Send players, bullets, powerups data
```

**Main Methods**:
- `addPlayer(String, String, ClientHandler)` - Player joins
- `removePlayer(String)` - Player leaves
- `handleMove(String, int, int, int)` - Update position
- `handleFire(String)` - Fire bullet
- `handleChat(String, String)` - Chat message
- `updateGameState()` - Game loop tick
- `broadcastUpdate()` - Send state to clients

---

#### 4. **WebSocketHandler** (`network/WebSocketHandler.java`)

**Responsibility**: Implement WebSocket protocol (RFC 6455)

**Key Features**:
- WebSocket handshake negotiation
- Frame encoding/decoding (text frames, ping/pong, close)
- Payload masking/unmasking
- Binary protocol parsing
- Pure Java implementation (no external libraries)
- Connection lifecycle management

**Package**: `com.minitankfire.network`

**Location**: `network/src/main/java/com/minitankfire/network/WebSocketHandler.java`

**Handshake Process**:
```
Client HTTP Request:
  GET / HTTP/1.1
  Host: localhost:8080
  Upgrade: websocket
  Connection: Upgrade
  Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==

Server HTTP Response:
  HTTP/1.1 101 Switching Protocols
  Upgrade: websocket
  Connection: Upgrade
  Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
```

**Message Frame Format** (RFC 6455):
```
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
+-+-+-+-+-------+-+-------------+
|F|R|R|R| opcode|M| Payload len |
|I|S|S|S|(4)    |A|             |
|N|V|V|V|       |S|             |
+-+-+-+-+-------+-+-------------+

Frame types:
- 0x1: Text frame
- 0x8: Close frame
- 0x9: Ping frame
- 0xA: Pong frame
```

**Main Methods**:
- `performHandshake()` - HTTP upgrade negotiation
- `readMessage()` - Decode WebSocket frame
- `sendMessage(String)` - Encode and send frame
- `close()` - Close connection

---

#### 5. **Game Models** (`model/` package)

**Player.java** - Represents a tank in the game
```java
String id                      // Unique identifier (UUID)
String name                    // Player's callsign
int x, y                       // Position on map
int angle                      // Rotation (0-359°)
int score                      // Kills - Deaths
boolean alive                  // Death/alive status
long lastRespawnTime           // Last death timestamp
boolean hasShield              // Shield active flag
long shieldEndTime             // When shield expires
boolean speedBoost             // Speed boost active
long speedBoostEndTime         // When speed expires
boolean doubleFire             // Double fire active
long doubleFireEndTime         // When double fire expires
```

**Bullet.java** - Represents a projectile
```java
String id                      // Unique identifier (UUID)
String ownerId                 // Who fired it
int x, y                       // Position
int dx, dy                     // Velocity per frame
long creationTime              // Spawn timestamp (for expiration)
```

**PowerUp.java** - Represents collectible items
```java
String id                      // Unique identifier (UUID)
Type type                      // SHIELD, SPEED_BOOST, or DOUBLE_FIRE
int x, y                       // Position on map
long spawnTime                 // Spawn timestamp (for expiration)
```

**Package**: `com.minitankfire.model`

**Location**: `model/src/main/java/com/minitankfire/model/`

---

#### 6. **Utilities** (`util/JsonUtil.java`)

**Responsibility**: JSON serialization/deserialization (NO external libraries!)

**Features**:
- Serialize Java objects to JSON strings
- Deserialize JSON strings to Java objects
- Custom handlers for Player, Bullet, PowerUp
- String escaping for special characters
- Message factory methods

**Package**: `com.minitankfire.util`

**Location**: `util/src/main/java/com/minitankfire/util/JsonUtil.java`

**Methods**:
- `toJson(Object)` - Object → JSON string
- `parseJson(String)` - JSON string → Map
- `createUpdateMessage()` - Game state broadcast
- `createChatMessage()` - Chat message
- `createHitMessage()` - Collision notification

**Example**:
```java
String json = JsonUtil.toJson(player);
// {"id":"abc-123","name":"Player1","x":400,"y":300,"angle":45,"score":5,...}

Map<String, String> data = JsonUtil.parseJson(json);
// {id=abc-123, name=Player1, x=400, y=300, angle=45, score=5, ...}
```

---

## Client Architecture

### Frontend Stack

**HTML** (`index.html`):
- Canvas element (800×600) for game rendering
- HUD panels (stats, leaderboard, chat)
- Join screen overlay
- Responsive layout

**CSS** (`style.css`):
- Neon-themed styling
- Dark background with glowing accents
- Responsive grid system
- Smooth animations and transitions

**JavaScript** (`game.js`):

#### GameClient Class

**Main Controller** - orchestrates all client-side logic

**Key Methods**:
```javascript
constructor()          // Initialize game state
init()                // Setup event listeners and screens
setupEventListeners() // Keyboard, mouse, UI events
connect()             // WebSocket connection
handleMessage()       // Process server messages
update()              // Update game state
render()              // Draw to canvas
```

**Game State**:
```javascript
playerId: string (assigned by server)
myPlayer: object (own player data)
players: {} (other players)
bullets: {} (all bullets)
powerUps: {} (all power-ups)
particles: [] (visual effects)

// Input state
keys: {} (keyboard state)
mouseX, mouseY: int (cursor position)
angle: int (tank rotation)

// Game stats
kills: int
deaths: int
health: int
isAlive: boolean
```

#### Rendering Pipeline

```
60 FPS Animation Frame Loop
  1. Update player position (WASD)
  2. Update rotation (mouse angle)
  3. Render game background
  4. Render power-ups
  5. Render other players
  6. Render bullets
  7. Render particles (explosions)
  8. Render HUD (stats, leaderboard)
  9. Render minimap
```

#### Input Handling

**Keyboard** (WASD Movement):
```javascript
W: move up (y -= speed)
A: move left (x -= speed)
S: move down (y += speed)
D: move right (x += speed)
```

**Mouse** (Aiming):
```javascript
mousemove: calculate angle from player to cursor
click: fire bullet
```

#### WebSocket Communication

**Connection**:
```javascript
this.ws = new WebSocket("ws://hostname:8080");
```

**Message Types Sent**:
- `join`: Player joins game
- `move`: Player position and angle
- `fire`: Player fires bullet
- `chat`: Text message
- `voice-*`: WebRTC initialization

**Message Types Received**:
- `update`: Game state update
- `hit`: Collision occurred
- `chat`: Chat message
- `respawn`: Player respawned
- `voice-*`: WebRTC signals

---

## Network Protocol

### Message Format

All messages are JSON-encoded strings sent over WebSocket:

```json
{
  "type": "message_type",
  "data": { /* type-specific data */ }
}
```

### Message Specifications

#### JOIN (Client → Server)
```json
{
  "type": "join",
  "name": "PlayerName"
}
```

#### MOVE (Client → Server)
```json
{
  "type": "move",
  "x": 400,
  "y": 300,
  "angle": 45
}
```

#### FIRE (Client → Server)
```json
{
  "type": "fire"
}
```

#### UPDATE (Server → Broadcast)
```json
{
  "type": "update",
  "players": [
    {"id":"p1","name":"Player1","x":100,"y":200,"angle":45,"score":5,"alive":true,"hasShield":false,"speedBoost":false,"doubleFire":false}
  ],
  "bullets": [
    {"id":"b1","ownerId":"p1","x":150,"y":220,"dx":8,"dy":0}
  ],
  "powerUps": [
    {"id":"pu1","type":"SHIELD","x":400,"y":300}
  ]
}
```

#### CHAT (Both Directions)
```json
{
  "type": "chat",
  "msg": "Hello everyone!"
}
```

#### HIT (Server → Broadcast)
```json
{
  "type": "hit",
  "target": "player-id-2",
  "shooter": "player-id-1"
}
```

#### RESPAWN (Server → Client)
```json
{
  "type": "respawn",
  "playerId": "player-id",
  "x": 400,
  "y": 300
}
```

---

## Data Structures

### Server-Side Collections

#### ConcurrentHashMap Usage

Thread-safe maps used for concurrent access:

```java
// Players currently in game
Map<String, Player> players = new ConcurrentHashMap<>();

// Bullets in flight
Map<String, Bullet> bullets = new ConcurrentHashMap<>();

// Available power-ups on map
Map<String, PowerUp> powerUps = new ConcurrentHashMap<>();

// Connected client handlers
Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
```

**Why ConcurrentHashMap?**
- Multiple ClientHandler threads read/write simultaneously
- No need for external synchronization (faster than synchronized maps)
- Supports atomic operations like `putIfAbsent()`
- Iterators fail-safe (don't throw ConcurrentModificationException)

#### Game Loop State

```java
private long gameStartTime;        // Server start time
private volatile boolean gameRunning = true;  // Game state flag
private Thread gameLoopThread;     // Dedicated loop thread
```

---

## Threading Model

### Thread Organization

```
┌─────────────────────────────────────────────────────────────┐
│  Main Thread                                                 │
│  ├─ Start ServerSocket on port 8080                         │
│  ├─ Create ExecutorService (100 threads max)                │
│  └─ Accept loop (blocks on serverSocket.accept())           │
└─────────────┬───────────────────────────────────────────────┘
              │
    ┌─────────┴──────────────────────────────────┐
    ▼                                            ▼
Thread Pool (0-100 ClientHandler threads)   Game Loop Thread
├─ ClientHandler #1                         (Dedicated)
├─ ClientHandler #2                         - Runs every 50ms
├─ ClientHandler #N                         - Updates game state
└─ ...                                       - Broadcasts updates
```

**Key Points**:
- Main thread never blocks on client processing
- Each client has dedicated thread for I/O
- Game loop has dedicated thread (never blocks)
- No thread starvation

### Synchronization Strategy

**Per-Collection Synchronization** (ConcurrentHashMap):
```java
// Thread-safe operations
bullets.entrySet().removeIf(entry -> !entry.getValue().isActive());
players.get(playerId).setX(newX);
```

**Volatile Fields**:
```java
private volatile boolean gameRunning = true;  // Visibility across threads
```

**No Deadlocks**:
- Single lock per collection
- No nested locking
- No waiting on other threads

---

## Technology Stack

### Backend

| Component | Technology | Version | Why? |
|-----------|-----------|---------|------|
| Language | Java | 11+ | Core language |
| Networking | ServerSocket/Socket | Built-in | Pure Java requirement |
| Protocol | WebSocket | RFC 6455 | Real-time bidirectional |
| Threading | ExecutorService | Built-in | Thread pool management |
| Serialization | Custom JsonUtil | - | NO external dependencies |
| Build Tool | Maven | 3.x | Dependency management |

### Frontend

| Component | Technology | Version | Why? |
|-----------|-----------|---------|------|
| Markup | HTML5 | - | Canvas-based rendering |
| Styling | CSS3 | - | Modern design |
| Logic | JavaScript ES6 | - | Client game loop |
| Canvas API | HTML5 Canvas | - | 2D graphics |
| WebSocket | Native API | - | Real-time communication |
| Voice | WebRTC | - | P2P voice chat |
| Icons | Font Awesome | 6.4.0 | UI iconography |

**🎯 Assignment Compliance**: Server uses **ONLY** core Java APIs - no external networking frameworks!

---

## Performance Characteristics

### Server

| Metric | Value |
|--------|-------|
| Max Concurrent Clients | 100 |
| Game Update Rate | 20 FPS (50ms per tick) |
| WebSocket Protocol | RFC 6455 |
| Message Latency | <50ms (depends on network) |
| Memory per Client | ~5KB (base) + network buffers |
| Thread Pool Size | 100 threads (configurable) |

### Client

| Metric | Value |
|--------|-------|
| Render Rate | 60 FPS (requestAnimationFrame) |
| Canvas Size | 800×600 pixels |
| Update Frequency | 20 FPS from server |
| Minimap Update | Every 100ms |
| Particle Cap | 500 simultaneous |

---

## Conclusion

The architecture prioritizes:
- ✅ **Real-time responsiveness** (20 FPS game updates)
- ✅ **Scalability** (thread pool, concurrent collections)
- ✅ **Educational clarity** (pure Java, no frameworks)
- ✅ **Network efficiency** (binary frames, message batching)
- ✅ **Clear code organization** (package by responsibility)
- ✅ **Thread safety** (no race conditions)
