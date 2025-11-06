# 🎮 Mini Tank Fire: Pure Java Multiplayer Server

A **web-based multiplayer tank game** with a **pure Java 21 server backend** demonstrating advanced networking concepts including TCP, UDP, and NIO for real-time multiplayer gameplay.

## ✨ Features

### 🧠 Core Networking Concepts Demonstrated
- **TCP Server**: Reliable data transfer for login, chat, and game setup
- **UDP Server**: Fast real-time updates for player movement and actions
- **NIO Server**: Non-blocking I/O for scalable client connections
- **Multithreading**: Concurrent client handling with thread pools
- **Game Loop**: Fixed tick rate (60 TPS) using ScheduledExecutorService
- **Serialization**: Object serialization for game state transfer
- **Broadcasting**: Real-time state updates to all connected players

### 🎯 Game Features
- **Real-time Multiplayer**: Multiple players in shared game world
- **Tank Movement**: Smooth position updates and aiming
- **Combat System**: Bullet firing with collision detection
- **Power-ups**: Shield, Speed Boost, Double Fire
- **Respawn Mechanics**: Automatic respawn after death
- **Game State Management**: Centralized server-side game logic

### �️ Architecture
- **Pure Java 21**: No external frameworks or libraries
- **Modular Design**: Separate packages for server, client, model, and utilities
- **Thread Safety**: Concurrent collections and synchronized access
- **Configurable**: Properties file for ports and game settings
- **Logging**: Built-in logging utility for debugging and monitoring

## 📁 Project Structure

```
tank-game/
│
├── server/src/main/
│   ├── java/com/example/game/
│   │   ├── server/           # Server implementations
│   │   ├── client/           # Client implementations
│   │   ├── model/            # Game data models
│   │   └── util/             # Utilities
│   └── resources/
│       └── config.properties # Configuration
│
├── client/                   # Web frontend
│   ├── index.html
│   ├── css/style.css
│   └── js/game.js
│
├── docs/                     # Documentation
├── scripts/                  # Build and run scripts
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- **Java 21** (required for server)
- **Maven 3.6+** (build tool)
- **Modern Browser** (for client)

### Installation

1. **Clone or navigate to the game directory**
   ```bash
   cd tank-game
   ```

2. **Build the Server**
   ```bash
   cd server
   mvn clean compile
   ```

3. **Stop the Game**
   ```bash
   # Windows
   scripts\stop.bat

   # Linux/Mac
   ./scripts/stop.sh
   ```

### Manual Setup

**Start the Server**
```bash
cd server
mvn exec:java
```
Server starts on TCP:8080, UDP:8081, NIO:8082

**Start the Client Web Server**
```bash
cd client
python -m http.server 3000
```
Open `http://localhost:3000` in your browser

## 🧩 Networking Architecture

### TCP Server (Port 8080)
- **Purpose**: Reliable communication
- **Use Cases**: Player login, chat messages, game setup
- **Implementation**: ServerSocket with thread-per-client model

### UDP Server (Port 8081)
- **Purpose**: Fast real-time updates
- **Use Cases**: Player movement, bullet positions, game actions
- **Implementation**: DatagramSocket with connectionless packets

### NIO Server (Port 8082)
- **Purpose**: Scalable non-blocking I/O
- **Use Cases**: High-concurrency scenarios
- **Implementation**: Channels, Buffers, Selectors

### Game Loop
- **Tick Rate**: 60 updates per second
- **Responsibilities**:
  - Update game state
  - Check collisions
  - Broadcast updates
  - Manage power-ups

## 🛠️ Development

### Building
```bash
cd server
mvn clean compile
```

### Running
```bash
cd server
mvn exec:java
```

### Testing
```bash
cd server
mvn test
```

### Configuration
Edit `server/src/main/resources/config.properties`:
```properties
tcp.port=8080
udp.port=8081
nio.port=8082
game.tick.rate=60
max.players=100
```

## 📚 Documentation

- **[Quick Start Guide](docs/QUICKSTART.md)** - Setup and first game
- **[Gameplay Guide](docs/GAMEPLAY.md)** - Game mechanics and strategies
- **[Deployment Guide](docs/DEPLOYMENT.md)** - Production deployment

## 🎯 Learning Objectives

This project demonstrates:
- **Core Java Networking**: TCP, UDP, NIO implementations
- **Multithreading**: Thread pools, executors, synchronization
- **Game Architecture**: Client-server model, game loops, state management
- **Serialization**: Object transfer over networks
- **Concurrent Programming**: Thread-safe data structures
- **Real-time Systems**: Fixed update rates, broadcasting

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is open source and available under the MIT License.

---

**Built with Pure Java 21 - No External Dependencies**

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
scripts\game.bat run

# Linux/Mac
./scripts/game.sh run
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
   scripts\game.bat build

   # Linux/Mac
   ./scripts/game.sh build
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

| Action | Control |
|--------|---------|
| **Move** | W/A/S/D or Arrow Keys |
| **Aim** | Mouse Movement |
| **Fire** | Left Click or Spacebar |
| **Chat** | Enter (type), Enter (send), ESC (close) |
| **Voice** | Click microphone icon |

## 🏗️ Architecture

### Server (Java)
```
server/
├── pom.xml                      # Maven configuration
└── src/main/java/com/minitankfire/
    ├── GameServer.java          # Jetty WebSocket server
    ├── GameWebSocket.java       # Connection handler
    ├── GameRoom.java            # Game logic & state
    ├── Player.java              # Player entity
    ├── Bullet.java              # Projectile entity
    ├── PowerUp.java             # Power-up entity
    └── Message.java             # Message protocols
```

**Key Components:**
- **Game Loop**: 50ms tick rate (20 FPS)
- **Collision Detection**: AABB bounding boxes
- **State Sync**: Broadcasts game state every frame
- **Voice Signaling**: WebRTC offer/answer/ICE forwarding

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

| Type | Direction | Description |
|------|-----------|-------------|
| `join` | Client → Server | Player joins with name |
| `move` | Client → Server | Position & angle update |
| `fire` | Client → Server | Fire weapon |
| `chat` | Client ↔ Server | Text message |
| `update` | Server → Client | Game state broadcast |
| `hit` | Server → Client | Collision notification |
| `respawn` | Server → Client | Player respawn |
| `voice-offer` | Client ↔ Client | WebRTC offer (via server) |
| `voice-answer` | Client ↔ Client | WebRTC answer (via server) |
| `voice-ice` | Client ↔ Client | ICE candidate (via server) |

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

| Type | Effect | Duration | Color |
|------|--------|----------|-------|
| **Shield** | Ignore next hit | 5 seconds | Cyan |
| **Speed Boost** | +50% movement speed | 3 seconds | Yellow |
| **Double Fire** | Fire 2 bullets | 10 seconds | Magenta |

## 📊 Scoring System

| Event | Points |
|-------|--------|
| Kill Enemy | +1 |
| Death | −1 |
| Collect Power-up | +0.5 |

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

| Layer | Technology | Version |
|-------|-----------|---------|
| **Server** | Java | 11+ |
| **Server Framework** | Eclipse Jetty | 9.4.51 |
| **WebSocket** | Jetty WebSocket API | 9.4.51 |
| **JSON** | Gson | 2.10.1 |
| **Build Tool** | Maven | 3.x |
| **Client** | HTML5 Canvas | - |
| **JavaScript** | Vanilla ES6 | - |
| **Voice** | WebRTC | - |
| **Icons** | Font Awesome | 6.4.0 |

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

Developed as a production-grade multiplayer game demonstration with:
- Professional UI/UX design
- Real-time WebSocket communication
- WebRTC voice chat integration
- Particle effects and animations
- Industrial-level code organization

---

**Ready to deploy and scale!** 🚀