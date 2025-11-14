# 🎮 Tank Arena: Online

A real-time multiplayer tank battle arena game featuring pure Java WebSocket server implementation and modern HTML5/JavaScript client. Experience fast-paced tank combat with power-ups, leaderboards, and voice chat capabilities.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java: 11+](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![WebSocket: RFC 6455](https://img.shields.io/badge/WebSocket-RFC%206455-blue.svg)](https://tools.ietf.org/html/rfc6455)

## 🌟 Features

### Gameplay
- **Real-time Multiplayer**: Smooth 20 FPS server-side game loop with low latency
- **Tank Combat**: Classic top-down tank warfare with mouse aiming and WASD movement
- **Power-up System**: Collect shields, speed boosts, and double-fire upgrades
- **Heat Management**: Strategic weapon cooldown system prevents spam
- **Leaderboard**: Track kills, deaths, and K/D ratios in real-time
- **Respawn System**: 3-second respawn timer with invincibility period

### Technical Features
- **Pure Java Server**: Zero external dependencies - built with core Java APIs only
- **Custom WebSocket**: Hand-coded RFC 6455 compliant WebSocket implementation
- **Thread-safe Architecture**: Concurrent data structures and multi-threaded client handling
- **Minimap**: Real-time battlefield overview with player tracking
- **Voice Chat**: Integrated WebRTC voice communication (optional)
- **Responsive UI**: Modern, accessible interface with screen shake and visual effects
- **Network Stats**: Real-time ping display and connection monitoring

## 🚀 Quick Start

### Prerequisites
- **Java 11+** (for server)
- **Maven 3.6+** (for building)
- **Python 3.x** (for serving client files)
- Modern web browser with WebSocket support

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone https://github.com/th33k/minitankfire.game.git
   cd minitankfire.game
   ```

2. **Build the project**
   ```bash
   make build
   ```

3. **Start the server**
   ```bash
   make server
   ```
   Server will start on `ws://localhost:8080/game`

4. **Start the client (in a new terminal)**
   ```bash
   make client
   ```
   Client will be available at `http://localhost:3000`

5. **Play the game**
   - Open browser to `http://localhost:3000`
   - Enter server address (e.g., `localhost` or your IP)
   - Enter your callsign
   - Click "Deploy to Battle"

### Manual Commands

**Server (Java):**
```bash
cd server
mvn clean compile
mvn exec:java
```

**Client (Python HTTP Server):**
```bash
cd client
python -m http.server 3000
```

## 🎯 Game Controls

| Action | Control |
|--------|---------|
| Move | `W` `A` `S` `D` |
| Aim | Mouse |
| Fire | Left Click |
| Chat | `Enter` |
| Toggle Aim Line | Settings Menu |
| Toggle Minimap | Settings Menu |
| Voice Chat | Settings Menu |

## 🏗️ Architecture

### Server (Java)
- **Pure Java Implementation**: No external libraries or frameworks
- **WebSocket Protocol**: RFC 6455 compliant handshake and framing
- **Multi-threaded**: ExecutorService for concurrent client handling
- **Game Loop**: 50ms tick rate (20 FPS) with ConcurrentHashMap state management
- **TCP Sockets**: java.net.ServerSocket for low-level networking

### Client (JavaScript)
- **ES6 Modules**: Clean, modular architecture with import/export
- **Canvas Rendering**: Hardware-accelerated 2D graphics
- **WebSocket Client**: Native browser WebSocket API
- **Manager Pattern**: Separate managers for UI, network, input, and voice chat
- **Responsive Design**: Scales to different screen sizes

### Communication Protocol
- **JSON-based messaging**: Structured data exchange
- **Message types**: join, game_state, input, fire, chat, powerup, respawn
- **Ping/Pong**: Network latency monitoring
- **Binary WebSocket frames**: Efficient data transmission

## 📁 Project Structure

```
minitankfire.game/
├── client/                    # HTML5 Client
│   ├── index.html            # Main game page
│   ├── css/
│   │   └── style.css         # Game styling
│   ├── js/
│   │   ├── game-client.js    # Main client logic
│   │   ├── core/             # Core game systems
│   │   │   ├── config.js     # Game constants
│   │   │   ├── input-manager.js
│   │   │   └── renderer.js   # Canvas rendering
│   │   └── managers/         # Feature managers
│   │       ├── network-manager.js
│   │       ├── ui-manager.js
│   │       └── voice-chat-manager.js
│   └── src/audio/            # Sound effects & music
├── server/                    # Java Server
│   ├── pom.xml               # Maven configuration
│   └── src/main/java/com/minitankfire/
│       ├── server/
│       │   └── GameServer.java      # Main server entry
│       ├── game/
│       │   └── GameRoom.java        # Game logic & physics
│       ├── network/
│       │   ├── ClientHandler.java   # Client connection handler
│       │   └── WebSocketHandler.java # WebSocket protocol
│       ├── model/
│       │   ├── Player.java          # Tank model
│       │   ├── Bullet.java          # Projectile model
│       │   └── PowerUp.java         # Power-up model
│       └── util/
│           └── JsonUtil.java        # JSON serialization
├── docs/                      # Documentation
│   ├── ARCHITECTURE.md       # System design
│   ├── DEVELOPMENT.md        # Developer guide
│   └── GAMEPLAY.md           # Game mechanics
├── Makefile                  # Build automation
└── README.md                 # This file
```

## 🔧 Configuration

### Server Configuration
Edit `GameServer.java` constants:
```java
private static final int DEFAULT_PORT = 8080;
private static final int MAX_CLIENTS = 100;
```

Edit `GameRoom.java` for game mechanics:
```java
private static final int MAP_WIDTH = 1920;
private static final int MAP_HEIGHT = 1080;
private static final int GAME_TICK_MS = 50;        // 20 FPS
private static final int RESPAWN_TIME_MS = 3000;
private static final int BULLET_DAMAGE = 20;
```

### Client Configuration
Edit `client/js/core/config.js`:
```javascript
export const CONFIG = {
    CANVAS: { WIDTH: 1920, HEIGHT: 1080 },
    PLAYER: { SPEED_NORMAL: 12, SPEED_BOOSTED: 20 },
    WEAPON: { FIRE_RATE: 500, BASE_DAMAGE: 25 }
};
```

## 🧪 Testing

```bash
# Clean and rebuild
make clean build

# Run server in test mode
cd server && mvn test

# Check for compilation errors
mvn compile
```

## 📊 Performance Metrics

- **Server Tick Rate**: 20 FPS (50ms per tick)
- **Network Protocol**: WebSocket (low latency)
- **Max Players**: 100 concurrent connections
- **Memory**: ~50MB server heap (typical)
- **Client FPS**: 60+ FPS (browser dependent)

## 🛠️ Technology Stack

### Server
- **Java 11+**: Core language
- **Maven**: Build tool
- **Pure Java Networking**: java.net, java.nio, java.util.concurrent
- **No external dependencies**: 100% pure Java

### Client
- **HTML5**: Structure
- **CSS3**: Styling with animations
- **JavaScript ES6+**: Logic and rendering
- **Canvas API**: 2D graphics
- **WebSocket API**: Real-time communication
- **WebRTC**: Peer-to-peer voice chat

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **th33k** - Initial work - [th33k](https://github.com/th33k)

## 🙏 Acknowledgments

- WebSocket RFC 6455 specification
- HTML5 Canvas API documentation
- Java networking community

## 📮 Support

For issues, questions, or contributions, please visit:
- **Issues**: https://github.com/th33k/minitankfire.game/issues
- **Discussions**: https://github.com/th33k/minitankfire.game/discussions

---

**Built with ❤️ using pure Java and modern web technologies**
