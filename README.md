#  Mini Tank Fire: Online

A **pure Java network programming** implementation of a real-time multiplayer shooter game demonstrating core networking concepts for the IN 3111 - Network Programming module.

---

##  Table of Contents

- [Features](#-features)
- [Quick Start](#-quick-start)
- [Documentation](#-documentation)
- [Technology Stack](#-technology-stack)
- [Architecture Overview](#-architecture-overview)
- [Network Protocol](#-network-protocol)
- [Game Controls](#-game-controls)
- [Gameplay Tips](#-gameplay-tips)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

---

##  Features

###  Core Gameplay

- **Real-time Multiplayer**: WebSocket-based synchronization at 20 FPS
- **Smooth Tank Movement**: WASD controls with responsive aiming
- **Combat System**: Click-to-fire with cooldown, collision detection
- **Power-ups**: Shield, Speed Boost, Double Fire with visual effects
- **Respawn Mechanics**: 3-second countdown with animated overlay

###  Professional UI/UX

- **Modern HUD**: Health, kills, deaths, power-up indicators
- **Live Leaderboard**: Real-time top 10 rankings
- **Kill Feed**: Kill notifications with fade-out animations
- **Minimap**: Real-time tactical overview
- **Responsive Design**: Clean, neon-themed interface
- **Visual Effects**: Particle explosions, screen shake, glowing elements
- **Smooth Animations**: CSS transitions, fade-ins, slide effects

###  Voice Chat

- **WebRTC P2P**: Peer-to-peer voice communication
- **Push-to-Talk**: Toggle microphone on/off
- **Auto Configuration**: Echo cancellation, noise suppression
- **Visual Indicators**: Microphone status icon
- **Low Latency**: Direct peer connections

###  Enhanced Chat System

- **Text Chat**: Real-time messaging between players
- **Keyboard Shortcuts**: Enter to open, ESC to close
- **Message History**: Scrollable chat with 20-message limit
- **Sender Highlighting**: Color-coded player names
- **Collapsible Panel**: Toggle chat visibility

---

##  Quick Start

### Using Makefile (Recommended)

```bash
# Build the project
make build

# Run both server and client
make run

# Stop all servers
make stop

# Clean build artifacts
make clean
```

**For detailed Makefile reference, see [DEVELOPMENT.md](docs/DEVELOPMENT.md#makefile-reference)**

### Manual Start

### Manual Start

**Start the Server**:
```bash
cd server
mvn clean compile exec:java
```

**Start Client** (new terminal):
```bash
cd client
python -m http.server 3000
```

**Open Browser**: `http://localhost:3000`

---

##  Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| Java | 11+ | Server runtime |
| Maven | 3.8.0+ | Build tool |
| Python | 3.6+ | Client server |
| Browser | Modern | Game client |

---

##  Documentation

| Document | Purpose |
|----------|---------|
| **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** | Original technical design |
| **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** |  **Updated** - New package structure |
| **[GAMEPLAY.md](docs/GAMEPLAY.md)** | Game guide & strategies |
| **[DEVELOPMENT.md](docs/DEVELOPMENT.md)** | Original dev setup |
| **[DEVELOPMENT.md](docs/DEVELOPMENT.md)** |  **Updated** - Makefile reference & new structure |
| **[SERVER_STRUCTURE.md](SERVER_STRUCTURE.md)** | Detailed server package guide |
| **[RESTRUCTURING_SUMMARY.md](RESTRUCTURING_SUMMARY.md)** | Project restructuring overview |

------

##  Game Controls

### Movement & Combat

| Control | Action |
|---------|--------|
| **W/A/S/D** | Move tank (up/left/down/right) |
| **Arrow Keys** | Alternative movement |
| **Mouse Move** | Aim turret |
| **Left Click** | Fire bullet |
| **Spacebar** | Alternative fire |

### Communication

| Control | Action |
|---------|--------|
| **Enter** | Open chat input |
| **Type & Enter** | Send message |
| **Escape** | Close chat |
| **Microphone Icon** | Toggle voice chat |

 **[See GAMEPLAY.md for complete guide](docs/GAMEPLAY.md)**

---

##  Architecture Overview

### System Diagram

```
Browser Clients (WebSocket TCP 8080)
            ↓
         GameServer (Java)
            ├─ GameRoom (Game Logic)
            ├─ ClientHandler (Per-client)
            └─ WebSocketHandler (RFC 6455)
```

### Server Components

The server is organized into **5 focused packages**:

```
com/minitankfire/
├── server/        GameServer (bootstrap & lifecycle)
├── network/       WebSocket protocol & client connections
├── game/          GameRoom (game logic & physics)
├── model/         Player, Bullet, PowerUp entities
└── util/          JsonUtil (serialization)
```

- **GameServer.java** - ServerSocket + Thread Pool
- **ClientHandler.java** - Per-client thread (WebSocket handling)
- **WebSocketHandler.java** - RFC 6455 protocol
- **GameRoom.java** - Game logic & state
- **Player.java, Bullet.java, PowerUp.java** - Game entities
- **JsonUtil.java** - Custom JSON (no dependencies!)

For detailed architecture, see **[ARCHITECTURE.md](docs/ARCHITECTURE.md)**

---

##  Network Protocol

### Main Messages

| Type | Direction | Purpose |
|------|-----------|---------|
| `join` | C→S | Player joins |
| `move` | C→S | Position update |
| `fire` | C→S | Fire weapon |
| `update` | S→C | Broadcast state |
| `chat` | Both | Text messages |

### Example

```json
{"type": "move", "x": 400, "y": 300, "angle": 45}
```

 **[See ARCHITECTURE.md for protocol details](docs/ARCHITECTURE.md#network-protocol)**

---

### UI Elements

- **Panels**: Semi-transparent black with neon borders
- **Buttons**: Gradient fills with hover animations
- **Text**: White with neon shadows
- **Icons**: Font Awesome 6.4.0

##  Configuration

### Server Settings

```java
// GameRoom.java
private static final int MAP_WIDTH = 1920;
private static final int MAP_HEIGHT = 1080;
private static final int PLAYER_SPEED = 3;
private static final int BULLET_SPEED = 8;
```

### Client Settings

```javascript
// game.js
this.fireRate = 500; // ms between shots
this.canvas.width = 1920;
this.canvas.height = 1080;
```

##  Power-ups

| Type            | Effect              | Duration   | Color   |
| --------------- | ------------------- | ---------- | ------- |
| **Shield**      | Ignore next hit     | 5 seconds  | Cyan    |
| **Speed Boost** | +50% movement speed | 3 seconds  | Yellow  |
| **Double Fire** | Fire 2 bullets      | 10 seconds | Magenta |

##  Scoring System

| Event            | Points |
| ---------------- | ------ |
| Kill Enemy       | +1     |
| Death            | −1     |
| Collect Power-up | +0.5   |

##  Future Enhancements

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

##  Tech Stack

| Layer          | Technology                         | Version  |
| -------------- | ---------------------------------- | -------- |
| **Server**     | Pure Java                          | 11+      |
| **Networking** | java.net.ServerSocket              | Built-in |
| **WebSocket**  | **Manual RFC 6455 Implementation** |        |
| **JSON**       | **Custom JsonUtil** (no Gson!)     |        |
| **Threading**  | java.util.concurrent               | Built-in |
| **I/O**        | java.io Streams                    | Built-in |
| **Build Tool** | Maven                              | 3.x      |
| **Client**     | HTML5 Canvas                       | -        |
| **JavaScript** | Vanilla ES6                        | -        |
| **Voice**      | WebRTC                             | -        |
| **Icons**      | Font Awesome                       | 6.4.0    |

** Assignment Compliance**: Server uses **ONLY** core Java APIs - no external networking frameworks!

##  Development

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

---

##  Troubleshooting

### Server Issues
- Check if port 8080 is available
- Ensure Java 11+ is installed  
- Run `mvn clean compile` first

### Client Issues
- Clear browser cache (Ctrl+F5)
- Check console for errors (F12)
- Verify server is running on port 8080

### Voice Chat
- Grant microphone permissions
- Check browser console for WebRTC errors
- Ensure both players click voice icon

� **[See DEVELOPMENT.md for more](docs/DEVELOPMENT.md#common-issues)**

---

##  Contributing

### Quick Start for Developers

```bash
git clone https://github.com/th33k/minitankfire.game.git
cd Tank-Game && git checkout dev
cd server && mvn clean install
make run
```

### Code Standards
-  Pure Java only
-  Follow style guidelines
-  Test locally first
-  Meaningful commit messages

 **[See DEVELOPMENT.md for details](docs/DEVELOPMENT.md)**

---

##  Project Structure

```
Tank-Game/
├── README.md          ← Main documentation
├── Makefile           ← Build automation
├── client/            ← Frontend (HTML/CSS/JS)
├── server/            ← Backend (Pure Java)
└── docs/              ← Guides & references
    ├── ARCHITECTURE.md
    ├── GAMEPLAY.md
    ├── DEVELOPMENT.md
```

---

##  License

Open source for educational purposes.

##  Credits

**Developed for**: IN 3111 - Network Programming Module

**100% Pure Java** - No external frameworks, educational compliance guaranteed!

---

##  Quick Links

-  [Play Guide](docs/GAMEPLAY.md)
-  [Dev Setup](docs/DEVELOPMENT.md)  
-  [Architecture](docs/ARCHITECTURE.md)

---

**Ready to play?** → `make run` → Open `http://localhost:3000`

**Have fun!** 
