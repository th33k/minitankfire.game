# Development Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Development Environment Setup](#development-environment-setup)
3. [Project Structure](#project-structure)
4. [Building the Project](#building-the-project)
5. [Running Locally](#running-locally)
6. [Development Workflow](#development-workflow)
7. [Code Standards](#code-standards)
8. [Testing](#testing)
9. [Debugging](#debugging)
10. [Common Issues](#common-issues)
11. [Contributing](#contributing)

## Getting Started

### Prerequisites

Before you begin development, ensure you have the following installed:

#### Required Tools
- **Java Development Kit (JDK) 11 or higher**
  ```bash
  java -version
  # Should output: java version "11" or higher
  ```

- **Apache Maven 3.6+**
  ```bash
  mvn -version
  # Should output: Apache Maven 3.6.x or higher
  ```

- **Python 3.x** (for serving client files)
  ```bash
  python --version
  # Should output: Python 3.x
  ```

- **Git** (for version control)
  ```bash
  git --version
  ```

#### Recommended Tools
- **Visual Studio Code** with extensions:
  - Java Extension Pack
  - JavaScript (ES6) code snippets
  - Live Server
  - GitLens
  
- **IntelliJ IDEA Community Edition** (alternative to VS Code)

- **Modern Web Browser** with DevTools:
  - Chrome/Edge (recommended for debugging)
  - Firefox Developer Edition

## Development Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/th33k/minitankfire.game.git
cd minitankfire.game
```

### 2. Configure Java Environment

Ensure `JAVA_HOME` is set correctly:

**Windows:**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-11
set PATH=%JAVA_HOME%\bin;%PATH%
```

**Linux/Mac:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### 3. Verify Maven Configuration

Check that Maven can access JDK:
```bash
cd server
mvn -version
```

### 4. Install Dependencies (if any are added)

Currently, the project has zero external dependencies. If you add any:
```bash
cd server
mvn clean install
```

## Project Structure

```
minitankfire.game/
│
├── client/                          # Frontend (HTML5/JavaScript)
│   ├── index.html                   # Main game page
│   ├── css/
│   │   └── style.css               # Game styling & animations
│   ├── js/
│   │   ├── game-client.js          # Main client entry point
│   │   ├── core/                   # Core game systems
│   │   │   ├── config.js           # Game constants & configuration
│   │   │   ├── input-manager.js    # Keyboard & mouse input handling
│   │   │   └── renderer.js         # Canvas rendering engine
│   │   └── managers/               # Feature-specific managers
│   │       ├── network-manager.js  # WebSocket communication
│   │       ├── ui-manager.js       # UI updates & interactions
│   │       └── voice-chat-manager.js # WebRTC voice chat
│   └── src/
│       └── audio/                  # Sound effects & music
│
├── server/                          # Backend (Java)
│   ├── pom.xml                     # Maven build configuration
│   └── src/main/java/com/minitankfire/
│       ├── server/
│       │   └── GameServer.java     # Main server entry & TCP socket
│       ├── game/
│       │   └── GameRoom.java       # Game logic, physics, state
│       ├── network/
│       │   ├── ClientHandler.java  # Per-client connection handler
│       │   └── WebSocketHandler.java # WebSocket protocol implementation
│       ├── model/
│       │   ├── Player.java         # Player/tank data model
│       │   ├── Bullet.java         # Projectile data model
│       │   └── PowerUp.java        # Power-up data model
│       └── util/
│           └── JsonUtil.java       # JSON serialization utilities
│
├── docs/                            # Documentation
│   ├── ARCHITECTURE.md             # System architecture & design
│   ├── DEVELOPMENT.md              # This file - developer guide
│   └── GAMEPLAY.md                 # Game mechanics & features
│
├── Makefile                        # Build automation (Unix/Linux)
└── README.md                       # Project overview
```

## Building the Project

### Using Make (Recommended)

```bash
# Clean and build everything
make clean build

# Just build
make build
```

### Manual Build

```bash
# Navigate to server directory
cd server

# Clean previous builds
mvn clean

# Compile source code
mvn compile

# Create executable JAR (optional)
mvn package
```

### Build Output

Compiled classes are located at:
```
server/target/classes/com/minitankfire/
```

## Running Locally

### Method 1: Using Make (Recommended)

```bash
# Start both server and client
make run

# Or start individually:
make server    # Start game server (port 8080)
make client    # Start client server (port 3000)
```

### Method 2: Manual Start

**Terminal 1 - Start Server:**
```bash
cd server
mvn exec:java -Dexec.mainClass="com.minitankfire.server.GameServer"
```

**Terminal 2 - Start Client:**
```bash
cd client
python -m http.server 3000
```

### Method 3: IDE Run Configurations

**IntelliJ IDEA / VS Code:**
1. Open `server/src/main/java/com/minitankfire/server/GameServer.java`
2. Right-click on `main()` method
3. Select "Run GameServer.main()"

### Accessing the Game

1. **Server**: Running on `ws://localhost:8080/game`
2. **Client**: Open browser to `http://localhost:3000`
3. **Join**: Enter server address (`localhost`) and your name

### Network Configuration

To allow connections from other devices:

1. Find your local IP:
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   ```

2. Clients connect using your IP:
   - Server address: `192.168.x.x` (your IP)
   - Client URL: `http://192.168.x.x:3000`

## Development Workflow

### Making Changes to Server Code

1. **Edit Java files** in `server/src/main/java/`

2. **Recompile:**
   ```bash
   cd server
   mvn compile
   ```

3. **Restart server:**
   - Stop current server (Ctrl+C)
   - Run: `mvn exec:java`

4. **Hot reload** (optional - not built-in):
   - Use JRebel or Spring DevTools
   - Or: Create a file watcher script

### Making Changes to Client Code

1. **Edit JavaScript files** in `client/js/`

2. **Refresh browser** - No build step needed!
   - JavaScript modules reload automatically
   - Use hard refresh (Ctrl+F5) to clear cache

3. **CSS changes:**
   - Edit `client/css/style.css`
   - Refresh browser to see changes

### Adding New Features

#### Server-Side Feature

1. **Create/modify model** in `server/src/main/java/com/minitankfire/model/`
   ```java
   public class NewFeature {
       private String id;
       // ... fields, getters, setters
   }
   ```

2. **Add game logic** in `GameRoom.java`:
   ```java
   private Map<String, NewFeature> features = new ConcurrentHashMap<>();
   
   private void updateFeatures() {
       // Update logic
   }
   ```

3. **Update protocol** in `ClientHandler.java`:
   ```java
   if (msg.get("type").equals("new_action")) {
       handleNewAction(msg);
   }
   ```

4. **Broadcast to clients**:
   ```java
   broadcast(JsonUtil.createMessage("new_state", features));
   ```

#### Client-Side Feature

1. **Update config** in `client/js/core/config.js`:
   ```javascript
   export const CONFIG = {
       NEW_FEATURE: {
           PARAM1: value1,
           PARAM2: value2
       }
   };
   ```

2. **Handle server messages** in `game-client.js`:
   ```javascript
   handleServerMessage(msg) {
       switch(msg.type) {
           case 'new_state':
               this.updateNewFeature(msg.data);
               break;
       }
   }
   ```

3. **Add rendering** in `renderer.js`:
   ```javascript
   drawNewFeature() {
       // Canvas drawing code
   }
   ```

4. **Update UI** in `ui-manager.js`:
   ```javascript
   updateNewFeatureUI(data) {
       // DOM manipulation
   }
   ```

## Code Standards

### Java Code Style

#### Naming Conventions
```java
// Classes: PascalCase
public class GameServer { }

// Methods: camelCase
public void handleClientMessage() { }

// Constants: UPPER_SNAKE_CASE
private static final int MAX_PLAYERS = 100;

// Variables: camelCase
private Map<String, Player> activePlayers;
```

#### Documentation
```java
/**
 * Brief description of class/method.
 * 
 * Detailed explanation if needed.
 * Can span multiple lines.
 * 
 * @param paramName Description of parameter
 * @return Description of return value
 */
public ReturnType methodName(ParamType paramName) {
    // Implementation
}
```

#### Best Practices
- Use meaningful variable names
- Keep methods short (< 50 lines)
- One responsibility per method
- Use thread-safe collections for shared state
- Always close resources (try-with-resources)
- Handle exceptions appropriately

### JavaScript Code Style

#### Naming Conventions
```javascript
// Classes: PascalCase
class GameClient { }

// Functions: camelCase
function handleInput() { }

// Constants: UPPER_SNAKE_CASE (in CONFIG)
const MAX_SPEED = 20;

// Variables: camelCase
let playerPosition = { x: 0, y: 0 };
```

#### ES6 Features
```javascript
// Use const/let, not var
const config = { ... };
let state = { ... };

// Arrow functions for callbacks
players.forEach(player => {
    this.drawPlayer(player);
});

// Template literals
console.log(`Player ${name} joined at position (${x}, ${y})`);

// Destructuring
const { x, y, angle } = player.position;

// Spread operator
const newState = { ...oldState, health: 100 };
```

#### Module Exports
```javascript
// Export at definition
export class GameClient { }
export const CONFIG = { };

// Or export at end
class Renderer { }
export { Renderer };
```

### JSON Message Format

```javascript
// Consistent structure
{
    "type": "message_type",      // Required: Message identifier
    "timestamp": 1234567890,     // Optional: For timing
    "data": {                    // Optional: Payload
        "key": "value"
    }
}
```

## Testing

### Manual Testing

1. **Start server** with test configuration:
   ```bash
   cd server
   mvn exec:java -Dconfig.test=true
   ```

2. **Open multiple browser tabs** to simulate multiple players

3. **Test scenarios:**
   - Player connection/disconnection
   - Movement and collision
   - Bullet firing and hits
   - Power-up collection
   - Respawn system
   - Chat functionality

### Browser DevTools Testing

**Console Commands:**
```javascript
// Access game client instance
window.game

// Check connection status
window.game.networkManager.ws.readyState

// Force disconnect
window.game.networkManager.disconnect()

// Change settings
window.game.aimLineEnabled = true
```

### Network Testing

**Test different network conditions:**

1. Open Chrome DevTools → Network tab
2. Select throttling preset:
   - Slow 3G
   - Fast 3G
   - Custom (set latency, bandwidth)

**Monitor WebSocket frames:**
1. DevTools → Network → WS filter
2. Click on WebSocket connection
3. View Messages tab

### Load Testing

Test with multiple simultaneous connections:

```javascript
// Create multiple bot clients
for (let i = 0; i < 50; i++) {
    const ws = new WebSocket('ws://localhost:8080/game');
    ws.onopen = () => {
        ws.send(JSON.stringify({ 
            type: 'join', 
            name: `Bot${i}` 
        }));
    };
}
```

### Unit Testing (Future Implementation)

Currently no unit tests. To add:

```bash
# Add JUnit to pom.xml
cd server
mvn test
```

## Debugging

### Server Debugging

#### Console Logging
```java
// Add debug statements
System.out.println("[DEBUG] Player count: " + players.size());
System.out.println("[DEBUG] Bullet position: (" + bullet.getX() + ", " + bullet.getY() + ")");
```

#### IDE Debugging (IntelliJ/VS Code)

1. Set breakpoints in Java code
2. Run in debug mode
3. Use step-through debugging
4. Inspect variables and call stack

**Key breakpoints:**
- `GameServer.start()` - Connection handling
- `ClientHandler.handleMessage()` - Message processing
- `GameRoom.update()` - Game logic
- `WebSocketHandler.readFrame()` - Protocol issues

#### Network Debugging

Monitor all WebSocket traffic:
```java
// In WebSocketHandler.java
public void sendMessage(String message) {
    System.out.println("[WS-SEND] " + message);
    // ... actual send code
}

private String readMessage() {
    String message = // ... read code
    System.out.println("[WS-RECV] " + message);
    return message;
}
```

### Client Debugging

#### Browser Console

```javascript
// Enable verbose logging in game-client.js
const DEBUG = true;

if (DEBUG) {
    console.log('[RENDER] Drawing', Object.keys(this.players).length, 'players');
    console.log('[INPUT] Mouse angle:', this.inputManager.getMouseAngle());
    console.log('[NETWORK] Received game_state', msg);
}
```

#### Performance Monitoring

```javascript
// Add to game-client.js
console.time('frame');
this.update();
this.render();
console.timeEnd('frame');

// Monitor FPS
let lastFrameTime = performance.now();
function updateFPS() {
    const now = performance.now();
    const fps = 1000 / (now - lastFrameTime);
    console.log('FPS:', fps.toFixed(1));
    lastFrameTime = now;
}
```

#### Canvas Debugging

```javascript
// Draw debug info on canvas
ctx.fillStyle = 'yellow';
ctx.font = '12px monospace';
ctx.fillText(`Players: ${Object.keys(this.players).length}`, 10, 20);
ctx.fillText(`Bullets: ${Object.keys(this.bullets).length}`, 10, 35);
ctx.fillText(`FPS: ${this.fps}`, 10, 50);
```

## Common Issues

### Issue: "Connection refused" / Cannot connect to server

**Cause**: Server not running or firewall blocking

**Solution:**
1. Verify server is running: `netstat -an | grep 8080`
2. Check firewall settings
3. Try: `telnet localhost 8080`
4. Ensure no other process using port 8080

### Issue: WebSocket handshake fails

**Cause**: Protocol mismatch or invalid upgrade request

**Solution:**
1. Check browser console for errors
2. Verify WebSocket URL format: `ws://host:port/path`
3. Enable server debug logging in `WebSocketHandler.java`
4. Check HTTP headers in browser Network tab

### Issue: Game lag / high latency

**Cause**: Network issues or server overload

**Solution:**
1. Check ping display in-game
2. Monitor server CPU usage
3. Reduce game tick rate for testing
4. Check network throttling in DevTools

### Issue: Players not visible / state desync

**Cause**: Client not processing game_state messages

**Solution:**
1. Check console for JavaScript errors
2. Verify `handleGameState()` is called
3. Check if player IDs match
4. Inspect game_state message structure

### Issue: Build fails with "package does not exist"

**Cause**: Wrong directory or Maven not configured

**Solution:**
```bash
cd server
mvn clean install -U
mvn dependency:resolve
```

### Issue: Client can't load ES6 modules

**Cause**: Not serving with HTTP server or CORS issues

**Solution:**
1. Always use HTTP server (not file://)
2. Check browser supports ES6 modules
3. Verify `type="module"` in HTML script tags

## Contributing

### Contribution Workflow

1. **Fork** the repository
2. **Create feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make changes** following code standards
4. **Test thoroughly** 
5. **Commit with descriptive messages:**
   ```bash
   git commit -m "Add: New power-up system with time limits"
   ```
6. **Push to your fork:**
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create Pull Request** on GitHub

### Commit Message Format

```
Type: Brief description (50 chars max)

Detailed explanation of changes if needed.
Can span multiple lines.

- Bullet points for specific changes
- Reference issue numbers: #123
```

**Types:**
- `Add:` New feature
- `Fix:` Bug fix
- `Update:` Modify existing feature
- `Refactor:` Code restructuring
- `Docs:` Documentation changes
- `Test:` Add or update tests
- `Style:` Formatting, no code change

### Code Review Checklist

Before submitting:
- [ ] Code compiles without errors
- [ ] No console errors in browser
- [ ] Follows code style guidelines
- [ ] Added comments for complex logic
- [ ] Tested with multiple players
- [ ] No performance degradation
- [ ] Documentation updated if needed

## Additional Resources

### Documentation
- [WebSocket RFC 6455](https://tools.ietf.org/html/rfc6455)
- [Canvas API MDN](https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API)
- [Java Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)

### Tools
- [Maven Documentation](https://maven.apache.org/guides/)
- [Chrome DevTools](https://developers.google.com/web/tools/chrome-devtools)
- [Git Documentation](https://git-scm.com/doc)

---

**Last Updated**: November 14, 2025

For questions or issues, please open an issue on GitHub or contact the development team.
