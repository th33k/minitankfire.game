# 🎮 Mini Tank Fire: Online

A **pure Java network programming** implementation of a top-view multiplayer shooter game demonstrating core networking concepts for the IN 3111 - Network Programming module.

## 🎓 Network Programming Concepts Demonstrated

This project showcases **pure Java network programming** using only core Java APIs:

- ✅ **Socket Programming** (ServerSocket, Socket)
- ✅ **TCP/IP Protocol** (Client-Server Communication)
- ✅ **Multi-threading** (ExecutorService, Thread Pool)
- ✅ **Concurrent Programming** (ConcurrentHashMap, Thread Safety)
- ✅ **WebSocket Protocol** (RFC 6455 Manual Implementation)
- ✅ **I/O Streams** (InputStream, OutputStream)
- ✅ **Binary Protocol Parsing** (Bit Manipulation)
- ✅ **HTTP Protocol** (WebSocket Handshake)
- ✅ **Cryptography** (SHA-1, Base64)
- ✅ **Real-time Systems** (Game Loop Threading)

**NO External Frameworks** - Only `java.net`, `java.io`, `java.util.concurrent` APIs used!

📖 **See detailed documentation**: [NETWORK_PROGRAMMING_CONCEPTS.md](docs/NETWORK_PROGRAMMING_CONCEPTS.md)

## ✨ Features

### 🎯 Core Gameplay

- **Real-time Multiplayer**: WebSocket-based synchronization at 20 FPS
- **Smooth Tank Movement**: WASD controls with responsive aiming
- **Combat System**: Click-to-fire with cooldown, collision detection
- **Power-ups**: Shield, Speed Boost, Double Fire with visual effects
- **Respawn Mechanics**: 3-second countdown with animated overlay

### 🎨 Professional UI/UX

- **Modern HUD**: Health, kills, deaths, power-up indicators
- **Live Leaderboard**: Real-time top 10 rankings
- **Kill Feed**: Kill notifications with fade-out animations
- **Minimap**: Real-time tactical overview
- **Responsive Design**: Clean, neon-themed interface
- **Visual Effects**: Particle explosions, screen shake, glowing elements
- **Smooth Animations**: CSS transitions, fade-ins, slide effects

### 🎤 Voice Chat

- **WebRTC P2P**: Peer-to-peer voice communication
- **Push-to-Talk**: Toggle microphone on/off
- **Auto Configuration**: Echo cancellation, noise suppression
- **Visual Indicators**: Microphone status icon
- **Low Latency**: Direct peer connections

### 💬 Enhanced Chat System

- **Text Chat**: Real-time messaging between players
- **Keyboard Shortcuts**: Enter to open, ESC to close
- **Message History**: Scrollable chat with 20-message limit
- **Sender Highlighting**: Color-coded player names
- **Collapsible Panel**: Toggle chat visibility

### 🎯 Game Features

- **Power-up System**: 3 types with rotating animations
- **Health Bars**: Visual health indicators above tanks
- **Shield Effects**: Glowing border around protected tanks
- **Bullet Trails**: Visual feedback for projectiles
- **Speed Indicators**: Visual effect for speed boost
- **Tank Customization**: Different colors for self/enemies

## 📚 Documentation

- **[Network Programming Concepts](docs/NETWORK_PROGRAMMING_CONCEPTS.md)** - 🎓 **Complete guide to Java networking concepts used**
- **[Quick Start Guide](docs/QUICKSTART.md)** - Step-by-step setup and first match checklist
- **[Gameplay Guide](docs/GAMEPLAY.md)** - Detailed tactics, strategies, and advanced gameplay
- **[Deployment Guide](docs/DEPLOYMENT.md)** - Production server setup and scaling

## �🚀 Quick Start

### Prerequisites

- **Java 11+** (for server)
- **Maven** (build tool)
- **Python 3** (for serving client)
- **Modern Browser** (Chrome, Firefox, Edge)

### Installation

#### Option 1: One-Click Launch (Recommended)

```bash
# Windows
scripts\run.bat

# Linux/Mac
./scripts/run.sh
```

✅ **Automatically starts both servers and opens browser!**

#### Option 2: Manual Setup

1. **Clone or navigate to the game directory**

   ```bash
   cd Game
   ```

2. **Build the Server (Optional)**

   ```bash
   # Windows
   scripts\build.bat

   # Linux/Mac
   ./scripts/build.sh
   ```

3. **Start the Server**

   ```bash
   cd server
   mvn clean compile exec:java
   ```

   Server runs on `ws://localhost:8080/game`

4. **Start the Client Server**

   ```bash
   cd client
   python -m http.server 3000
   ```

   Open `http://localhost:3000` in your browser

5. **Play!**
   - Enter your callsign
   - Click "Deploy to Battle"
   - Use WASD to move, mouse to aim, click to fire
   - Press Enter to chat
   - Click microphone icon for voice chat

## 🎮 Controls

| Action    | Control                                 |
| --------- | --------------------------------------- |
| **Move**  | W/A/S/D or Arrow Keys                   |
| **Aim**   | Mouse Movement                          |
| **Fire**  | Left Click or Spacebar                  |
| **Chat**  | Enter (type), Enter (send), ESC (close) |
| **Voice** | Click microphone icon                   |

## 🏗️ Architecture

### Server (Pure Java Network Programming)

```
server/
├── pom.xml                      # NO DEPENDENCIES - Pure Java only!
└── src/main/java/com/minitankfire/
    ├── GameServer.java          # ServerSocket + Thread Pool
    ├── ClientHandler.java       # Per-client thread (Runnable)
    ├── WebSocketHandler.java    # WebSocket RFC 6455 implementation
    ├── GameRoom.java            # Thread-safe game logic
    ├── JsonUtil.java            # Custom JSON serialization
    ├── Player.java              # Player entity
    ├── Bullet.java              # Projectile entity
    └── PowerUp.java             # Power-up entity
```

**Network Programming Components:**

- **ServerSocket**: Accepts TCP connections on port 8080
- **Multi-threading**: ExecutorService with thread pool (100 max clients)
- **WebSocket Protocol**: Manual handshake, frame encoding/decoding
- **Concurrent Collections**: ConcurrentHashMap for thread safety
- **Game Loop Thread**: Separate thread for 20 FPS updates
- **I/O Streams**: Direct InputStream/OutputStream manipulation
- **Binary Parsing**: Bit manipulation for WebSocket frames

### Client (Web)

```
client/
├── index.html                   # Game UI structure
├── css/
│   └── style.css               # Professional styling
└── js/
    └── game.js                 # Game client logic
```

**Key Components:**

- **Canvas Rendering**: 1200×800 game area
- **WebSocket Client**: Real-time communication
- **WebRTC Manager**: P2P voice connections
- **Particle System**: Explosion effects
- **HUD Manager**: Stats, leaderboard, chat

## 📡 Network Protocol

### Message Types

| Type           | Direction       | Description                |
| -------------- | --------------- | -------------------------- |
| `join`         | Client → Server | Player joins with name     |
| `move`         | Client → Server | Position & angle update    |
| `fire`         | Client → Server | Fire weapon                |
| `chat`         | Client ↔ Server | Text message               |
| `update`       | Server → Client | Game state broadcast       |
| `hit`          | Server → Client | Collision notification     |
| `respawn`      | Server → Client | Player respawn             |
| `voice-offer`  | Client ↔ Client | WebRTC offer (via server)  |
| `voice-answer` | Client ↔ Client | WebRTC answer (via server) |
| `voice-ice`    | Client ↔ Client | ICE candidate (via server) |

### Example Messages

```json
// Join
{ "type": "join", "name": "Player1" }

// Move
{ "type": "move", "x": 400, "y": 300, "angle": 45 }

// Fire
{ "type": "fire" }

// Update
{
  "type": "update",
  "players": [{ "id": "...", "name": "...", "x": 400, "y": 300, ... }],
  "bullets": [{ "id": "...", "x": 450, "y": 320, ... }],
  "powerUps": [{ "id": "...", "type": "SHIELD", ... }]
}
```

## 🎨 Visual Design

### Color Palette

- **Primary**: `#00ff88` (Neon Green)
- **Secondary**: `#00ffcc` (Cyan)
- **Accent**: `#ffaa00` (Gold)
- **Danger**: `#ff4444` (Red)
- **Background**: `#0a0a0a` - `#1a1a2e` (Dark gradient)

### UI Elements

- **Panels**: Semi-transparent black with neon borders
- **Buttons**: Gradient fills with hover animations
- **Text**: White with neon shadows
- **Icons**: Font Awesome 6.4.0

## 🔧 Configuration

### Server Settings

```java
// GameRoom.java
private static final int MAP_WIDTH = 1200;
private static final int MAP_HEIGHT = 800;
private static final int PLAYER_SPEED = 3;
private static final int BULLET_SPEED = 8;
```

### Client Settings

```javascript
// game.js
this.fireRate = 500; // ms between shots
this.canvas.width = 1200;
this.canvas.height = 800;
```

## 🎯 Power-ups

| Type            | Effect              | Duration   | Color   |
| --------------- | ------------------- | ---------- | ------- |
| **Shield**      | Ignore next hit     | 5 seconds  | Cyan    |
| **Speed Boost** | +50% movement speed | 3 seconds  | Yellow  |
| **Double Fire** | Fire 2 bullets      | 10 seconds | Magenta |

## 📊 Scoring System

| Event            | Points |
| ---------------- | ------ |
| Kill Enemy       | +1     |
| Death            | −1     |
| Collect Power-up | +0.5   |

## 🔮 Future Enhancements

- [ ] **Team Mode**: Red vs Blue teams
- [ ] **Multiple Maps**: Different arena layouts
- [ ] **AI Bots**: Fill empty slots
- [ ] **Tank Upgrades**: Damage, armor, speed tiers
- [ ] **Custom Rooms**: Private lobbies
- [ ] **Spectator Mode**: Watch ongoing matches
- [ ] **Match History**: Stats tracking
- [ ] **Sound Effects**: Weapon fire, explosions
- [ ] **Background Music**: Ambient tracks
- [ ] **Mobile Support**: Touch controls

## 🛠️ Tech Stack

| Layer          | Technology                         | Version  |
| -------------- | ---------------------------------- | -------- |
| **Server**     | Pure Java                          | 11+      |
| **Networking** | java.net.ServerSocket              | Built-in |
| **WebSocket**  | **Manual RFC 6455 Implementation** | ✅       |
| **JSON**       | **Custom JsonUtil** (no Gson!)     | ✅       |
| **Threading**  | java.util.concurrent               | Built-in |
| **I/O**        | java.io Streams                    | Built-in |
| **Build Tool** | Maven                              | 3.x      |
| **Client**     | HTML5 Canvas                       | -        |
| **JavaScript** | Vanilla ES6                        | -        |
| **Voice**      | WebRTC                             | -        |
| **Icons**      | Font Awesome                       | 6.4.0    |

**🎯 Assignment Compliance**: Server uses **ONLY** core Java APIs - no external networking frameworks!

## 📝 Development

### Building

```bash
cd server
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Packaging

```bash
mvn package
java -jar target/minitankfire-server-1.0-SNAPSHOT.jar
```

## 🐛 Troubleshooting

### Server won't start

- Check if port 8080 is available
- Ensure Java 11+ is installed
- Run `mvn clean compile` first

### Client issues

- Clear browser cache (Ctrl+F5)
- Check console for errors (F12)
- Verify server is running on port 8080

### Voice chat not working

- Grant microphone permissions
- Check browser console for WebRTC errors
- Ensure both players click voice icon

## 📄 License

This project is open source and available for educational purposes.

## 👥 Credits

Developed for **IN 3111 - Network Programming** module to demonstrate:

- ✅ Pure Java socket programming (no frameworks)
- ✅ Multi-threading and concurrent client handling
- ✅ WebSocket protocol implementation from scratch
- ✅ Client-server architecture
- ✅ Real-time game state synchronization
- ✅ Thread-safe concurrent programming

---

**🎓 Educational Project - 100% Assignment Compliant**

This implementation uses **ZERO external networking libraries** and demonstrates core Java network programming concepts required for academic evaluation.
