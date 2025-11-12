# 👨‍💻 Development Guide - Mini Tank Fire

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Build & Run](#build--run)
4. [Project Structure](#project-structure)
5. [Makefile Reference](#makefile-reference)
6. [Development Workflow](#development-workflow)
7. [Code Organization](#code-organization)
8. [Testing](#testing)
9. [Debugging](#debugging)
10. [Contributing Guidelines](#contributing-guidelines)
11. [Common Issues](#common-issues)

---

## Prerequisites

### Required Software

- **Java Development Kit (JDK)**
  - Version: 11 or higher
  - Download: https://adoptopenjdk.net/
  - Verify: `java -version`

- **Maven**
  - Version: 3.8.0 or higher
  - Download: https://maven.apache.org/download.cgi
  - Verify: `mvn -version`

- **Python**
  - Version: 3.6 or higher (for serving client)
  - Download: https://www.python.org/downloads/
  - Verify: `python --version`

- **Git**
  - Version: 2.0 or higher
  - Download: https://git-scm.com/
  - Verify: `git --version`

- **IDE (Recommended)**
  - VS Code (Free, lightweight)
  - Eclipse IDE (Free)
  - IntelliJ IDEA (Professional)

### IDE Setup (VS Code Example)

**Extensions**:
- Extension Pack for Java (Microsoft)
- WebSocket Extension (optional)
- REST Client (for testing APIs)

---

## Project Setup

### 1. Clone Repository

```bash
# Navigate to desired location
cd /path/to/projects

# Clone the repository
git clone https://github.com/th33k/minitankfire.game.git
cd minitankfire.game

# Switch to development branch (if not on it)
git checkout dev
```

### 2. Install Dependencies

**Server Dependencies**:
```bash
cd server
mvn clean install
```

This downloads all Maven dependencies and compiles the project.

**Client Dependencies**:
No installation needed! Pure HTML/CSS/JavaScript.

### 3. Verify Installation

```bash
# From server directory
mvn -version
java -version

# Test build
mvn clean compile
```

Expected output: `BUILD SUCCESS`

---

## Build & Run

### Option 1: Using Makefile (Recommended)

The Makefile automates common development tasks. See [Makefile Reference](#makefile-reference) below.

**Start everything**:
```bash
# From project root
make run
```

**Build only**:
```bash
make build
```

**Clean build artifacts**:
```bash
make clean
```

**Stop servers**:
```bash
make stop
```

### Option 2: Manual Execution

**Start Server**:
```bash
cd server
mvn clean compile exec:java
```

Expected output:
```
╔════════════════════════════════════════════════════════════╗
║      🎮 Tank Game Server - Pure Java Network Programming  ║
╠════════════════════════════════════════════════════════════╣
║  Architecture:                                             ║
║  ✓ Multi-threaded Client Handling                         ║
║  ✓ WebSocket Protocol (RFC 6455)                          ║
║  ✓ Real-time Game Loop (20 FPS)                           ║
║  ✓ Concurrent State Management                            ║
╠════════════════════════════════════════════════════════════╣
║  Server Address: 0.0.0.0:8080                             ║
║  WebSocket URI: ws://localhost:8080/game                  ║
║  Max Clients: 100                                         ║
╚════════════════════════════════════════════════════════════╝

[SERVER] Waiting for connections...
```

**Start Client** (new terminal):
```bash
cd client
python -m http.server 3000
```

Expected output:
```
Serving HTTP on 0.0.0.0 port 3000 (http://0.0.0.0:3000/) ...
```

**Open Browser**:
- Navigate to: `http://localhost:3000`
- Enter your player name
- Click "Deploy to Battle"
- Start playing!

### Option 3: Docker (Future)

```bash
docker build -t tank-game .
docker run -p 8080:8080 -p 3000:3000 tank-game
```

---

## Project Structure

### New Organized Structure

```
minitankfire.game/
├── Makefile                                    # Build automation
├── README.md                                   # Main documentation
├── .gitignore                                  # Git ignore rules
│
├── client/                                     # Frontend (HTML/CSS/JS)
│   ├── index.html                              # Main game UI
│   ├── css/
│   │   └── style.css                           # Game styling (neon theme)
│   └── js/
│       ├── game.js                             # Main game client logic
│       ├── game-client.js                      # Game client utilities
│       ├── core/                               # Core game systems
│       │   ├── config.js                       # Game configuration
│       │   ├── input-manager.js                # Input handling
│       │   └── renderer.js                     # Rendering system
│       └── managers/                           # Game managers
│           ├── network-manager.js              # Network communication
│           ├── ui-manager.js                   # UI management
│           └── voice-chat-manager.js           # Voice chat system
│
├── server/                                     # Backend (Java)
│   ├── pom.xml                                 # Maven configuration
│   ├── target/                                 # Build artifacts (generated)
│   └── src/main/java/com/minitankfire/
│       ├── server/                             # 🖥️ Server bootstrap
│       │   └── GameServer.java
│       ├── network/                            # 🌐 Network layer
│       │   ├── WebSocketHandler.java
│       │   └── ClientHandler.java
│       ├── game/                               # 🎮 Game logic
│       │   └── GameRoom.java
│       ├── model/                              # 📊 Data models
│       │   ├── Player.java
│       │   ├── Bullet.java
│       │   └── PowerUp.java
│       └── util/                               # 🔧 Utilities
│           └── JsonUtil.java
│
├── docs/                                       # Documentation
│   ├── ARCHITECTURE.md                         # Technical design
│   ├── GAMEPLAY.md                             # Game guide
│   ├── DEVELOPMENT.md                          # This file
│   └── report/                                 # Project reports
│       ├── PROJECT_REPORT.md                   # Project report
│       └── TEAM_CONTRIBUTIONS.md               # Team contributions
│
└── .github/
    └── workflows/                              # CI/CD pipelines
        └── deploy.yml                          # Deployment workflow
```

### Package Responsibilities

| Package | Responsibility |
|---------|-----------------|
| `server` | Server bootstrap and lifecycle management |
| `network` | WebSocket protocol and client connections |
| `game` | Game logic, physics, and state management |
| `model` | Game entity data structures (Player, Bullet, PowerUp) |
| `util` | Utilities (JSON serialization, message factories) |

---

## Makefile Reference

### Overview

The `Makefile` provides automated commands for common development tasks.

### Available Commands

#### `make build`
Compiles the server project.

```bash
make build
# Equivalent to: cd server && mvn clean compile
```

**Use When**:
- Making code changes
- Before committing
- Testing for syntax errors

---

#### `make run`
Starts both server and client simultaneously.

```bash
make run
# Starts:
# 1. Server on ws://localhost:8080
# 2. Client server on http://localhost:3000
```

**After running**:
1. Open browser: `http://localhost:3000`
2. Join the game
3. To stop: `make stop` or Ctrl+C

---

#### `make server`
Starts only the Java server.

```bash
make server
# Runs: cd server && mvn exec:java
```

**Use When**:
- Testing server changes
- Debugging server logic
- Client is already running

---

#### `make client`
Starts only the Python HTTP server for the client.

```bash
make client
# Runs: cd client && python3 -m http.server 3000
```

**Use When**:
- Testing client changes
- Server is already running
- Developing frontend independently

---

#### `make clean`
Removes build artifacts and cache files.

```bash
make clean
# Runs: cd server && mvn clean
```

**Use When**:
- Cleaning up before committing
- Fixing build issues
- Freeing up disk space

---

#### `make stop`
Terminates server and client processes.

```bash
make stop
# Kills: java (server) and python (client) processes
```

**Use When**:
- Stopping background processes
- Between server restarts
- Freeing up ports

**Note**: May not work on Windows. Use Ctrl+C in terminals instead.

---

### Makefile Usage Examples

**Complete workflow**:
```bash
# 1. Build
make build

# 2. Run
make run

# 3. Test in browser (http://localhost:3000)

# 4. Stop when done
make stop
```

**Development cycle**:
```bash
# Edit code...

# Rebuild
make build

# Restart
make stop
make run

# Test in browser...
```

**Server-only development**:
```bash
# Terminal 1: Start server
make server

# Terminal 2: Make changes, rebuild
make build

# Terminal 1: Restart server (Ctrl+C, then re-run)
make server
```

---

## Development Workflow

### 1. Feature Development

```bash
# 1. Create feature branch
git checkout -b feature/my-feature

# 2. Make changes in your editor
# ... edit files ...

# 3. Build and test
make clean
make build

# 4. Run locally to test
make run
# Test in http://localhost:3000

# 5. Stop servers
make stop

# 6. Commit changes
git add .
git commit -m "feat: add my feature"

# 7. Push to GitHub
git push origin feature/my-feature

# 8. Create Pull Request on GitHub
```

### 2. Bug Fixes

```bash
# 1. Create fix branch
git checkout -b fix/bug-name

# 2. Reproduce bug
make run
# ... verify bug exists ...

# 3. Make fix
# ... edit files ...

# 4. Verify fix
make clean
make build
make run
# ... test fix ...

# 5. Commit and push
git add .
git commit -m "fix: resolve bug-name"
git push origin fix/bug-name

# 6. Create PR with fix
```

### 3. Documentation Updates

```bash
# Edit relevant markdown files in docs/
# No build needed - just commit and push

git add docs/
git commit -m "docs: update architecture guide"
git push origin dev
```

---

## Code Organization

### Server Code Structure

#### GameServer.java (`server/` package)
```
Bootstrap layer:
  ├─ Accepts TCP connections
  ├─ Creates thread pool
  ├─ Delegates to ClientHandler
  └─ Manages lifecycle
```

#### ClientHandler.java (`network/` package)
```
Connection layer:
  ├─ Runs in thread pool (one per client)
  ├─ Performs WebSocket handshake
  ├─ Routes messages to GameRoom
  └─ Handles disconnection
```

#### GameRoom.java (`game/` package)
```
Logic layer:
  ├─ Manages game state
  ├─ Runs game loop (20 FPS)
  ├─ Physics & collisions
  ├─ Broadcasting to clients
  └─ Scoring system
```

#### WebSocketHandler.java (`network/` package)
```
Protocol layer:
  ├─ RFC 6455 handshake
  ├─ Frame encoding/decoding
  ├─ Masking/unmasking
  └─ Connection management
```

#### JsonUtil.java (`util/` package)
```
Serialization layer:
  ├─ Object → JSON
  ├─ JSON → Map
  ├─ Message factories
  └─ String escaping
```

### Client Code Structure

#### game.js (Main Controller)
```javascript
class GameClient {
  constructor()           // Initialize game state
  init()                  // Setup event listeners and screens
  connect()               // WebSocket connection
  handleMessage()         // Process server messages
  update()                // Update game state (60 FPS)
  render()                // Draw to canvas
  // ... more methods
}
```

#### game-client.js (Client Utilities)
- Helper functions for game client
- Utility methods and constants

#### core/ Directory
- **config.js**: Game configuration and constants
- **input-manager.js**: Keyboard and mouse input handling
- **renderer.js**: Rendering utilities and functions

#### managers/ Directory
- **network-manager.js**: WebSocket communication management
- **ui-manager.js**: User interface management
- **voice-chat-manager.js**: WebRTC voice chat functionality

---

## Testing

### Server Testing

#### Unit Tests

Location: `server/src/test/java/com/minitankfire/`

**Run Tests**:
```bash
cd server
mvn test
```

**Example Test** (JsonUtilTest.java):
```java
@Test
public void testPlayerSerialization() {
    Player p = new Player("id1", "TestPlayer");
    String json = JsonUtil.toJson(p);
    Map<String, String> data = JsonUtil.parseJson(json);
    assertEquals("id1", data.get("id"));
    assertEquals("TestPlayer", data.get("name"));
}
```

**Write New Tests**:
1. Create test file in `src/test/java/...`
2. Add `@Test` annotations
3. Run with Maven
4. Commit with code

#### Manual Testing

```bash
# Test WebSocket connection
1. Start server: make run
2. Open client: http://localhost:3000
3. Join game
4. Watch console for connection logs

# Test specific features
- Movement: Use WASD keys
- Firing: Click on game area
- Chat: Press Enter
- Power-ups: Collect them on map
```

### Client Testing

#### Browser Developer Tools

```bash
# Open DevTools
- Press F12 or Ctrl+Shift+I

# Check Console
- Look for errors/warnings
- Test message: console.log()

# Check Network Tab
- Monitor WebSocket traffic
- Verify message formats
- Check latency
```

#### Manual Testing Checklist

```
[ ] Start server (make server)
[ ] Open client (http://localhost:3000)
[ ] Join game with valid name
[ ] Move around (WASD)
[ ] Aim at enemies (mouse)
[ ] Fire bullets (click)
[ ] Collect power-ups
[ ] See health bar update
[ ] See leaderboard update
[ ] Chat messages appear
[ ] Minimap shows positions
[ ] Death/respawn works
```

---

## Debugging

### Server Debugging

#### Enable Debug Mode

**Option 1: Maven Debug**:
```bash
cd server
mvn clean compile exec:java@debug
```

**Option 2: IDE Debugging** (VS Code):
1. Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Java Server Debug",
      "type": "java",
      "name": "Launch Current File",
      "request": "launch"
    }
  ]
}
```
2. Press F5 to start debugging
3. Set breakpoints by clicking line numbers

#### Logging

**Add Debug Prints** (GameRoom.java):
```java
System.out.println("[DEBUG] Player " + playerId + " moved to " + x + "," + y);
```

**Structured Logging Format**:
```
[COMPONENT] Action/Status
[SERVER] Listening on port 8080
[ACCEPT] New client from 127.0.0.1:54321
[CONNECTED] Client: a1b2c3d4 from 127.0.0.1
[JOIN] Player 'John' joined. Total: 5
[GAME] Bullet fired by Player1
[HANDLER] Message received: {"type":"move"...}
[ERROR] WebSocket handshake failed
[DISCONNECTED] Client: a1b2c3d4
```

### Client Debugging

#### Browser Console

```javascript
// Log game state
console.log("Players:", this.players);
console.log("My position:", this.myPlayer);

// Test networking
console.log("WebSocket connected:", this.ws.readyState);

// Monitor messages
// Already logged in game.js - search for console.log()
```

#### Network Inspection

1. Open DevTools (F12)
2. Go to Network tab
3. Filter: WS (WebSocket)
4. Click on websocket connection
5. Watch Messages tab for real-time traffic

#### Common Issues

```
Issue: Connection refused
Debug: Check if server is running (make run)
       Check port 8080 is not in use
       Verify firewall settings

Issue: Blank canvas
Debug: Check canvas width/height (800x600)
       Check if render() is called
       Check console for errors

Issue: No players visible
Debug: Check websocket messages in DevTools
       Verify update broadcast
       Check client player data in console
```

---

## Contributing Guidelines

### Code Style

#### Java

```java
// Class names: PascalCase
public class GameServer { }

// Method names: camelCase
public void handleMessage() { }

// Constants: UPPER_SNAKE_CASE
private static final int MAX_CLIENTS = 100;

// Variables: camelCase
private int playerCount;

// Formatting: 4 spaces indentation
if (condition) {
    doSomething();
}

// Comments: Explain WHY, not WHAT
// Good: Thread pool handles concurrent clients without blocking
private ExecutorService threadPool;

// Bad: Create thread pool
```

#### JavaScript

```javascript
// Class names: PascalCase
class GameClient { }

// Method names: camelCase
render() { }

// Constants: UPPER_SNAKE_CASE
const MAX_SPEED = 100;

// Variables: camelCase
let playerCount;

// Formatting: 2 spaces indentation
if (condition) {
  doSomething();
}

// Comments: JSDoc style
/**
 * Render player on canvas
 * @param {Player} player - Player to render
 * @param {CanvasRenderingContext2D} ctx - Canvas context
 */
render(player, ctx) { }
```

### Git Commit Messages

**Format**:
```
<type>: <subject>

<body>

<footer>
```

**Types**:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `style:` Code style (formatting)
- `refactor:` Code refactoring
- `perf:` Performance improvements
- `test:` Tests
- `chore:` Build, dependencies

**Examples**:
```
feat: add double-fire power-up

- Create DoubleFire logic
- Add fire() method to handle 2 bullets
- Update GameRoom to spawn power-ups
- Test with manual gameplay

Closes #42

---

fix: prevent friendly fire damage

- Add owner check in collision detection
- Only damage opposite team players
- Update tests

---

docs: update architecture guide

- Add threading model section
- Add data structure explanation
```

### Pull Request Process

1. **Create PR with clear title**
   ```
   feat: add shield power-up mechanic
   fix: resolve bullet collision detection bug
   docs: update API documentation
   ```

2. **Write PR description**
   ```markdown
   ## Description
   Brief description of changes

   ## Changes
   - List of specific changes
   - Another change
   - One more change

   ## Testing
   How to test these changes

   ## Closes
   #123
   ```

3. **Link related issues**
   ```
   Closes #123
   Related to #456
   ```

4. **Await review**
   - Code review required
   - CI/CD checks passed
   - At least one approval

---

## Common Issues

### Build Issues

#### Issue: "Maven command not found"
**Solution**:
```bash
# Add Maven to PATH
# Windows: Add C:\Program Files\maven\bin to PATH
# macOS: brew install maven
# Linux: apt-get install maven

# Verify
mvn -version
```

#### Issue: "Java version mismatch"
**Solution**:
```bash
# Check current Java
java -version

# Set JAVA_HOME
# Windows: setx JAVA_HOME "C:\Program Files\Java\jdk-11"
# macOS/Linux: export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Verify
java -version  # Should be 11+
```

#### Issue: "Port 8080 already in use"
**Solution**:
```bash
# Find process using port
# Windows: netstat -ano | findstr :8080
# macOS/Linux: lsof -i :8080

# Kill process
# Windows: taskkill /PID <PID> /F
# macOS/Linux: kill <PID>
```

### Runtime Issues

#### Issue: "WebSocket connection failed"
**Solution**:
1. Verify server is running: Check `make run` output
2. Check firewall: Allow port 8080
3. Check browser console: DevTools → Console
4. Try localhost instead of 127.0.0.1

#### Issue: "Players not visible on canvas"
**Solution**:
1. Check DevTools → Network → WS messages
2. Verify update messages contain player data
3. Check console for render errors
4. Test with fewer players first

### Development Issues

#### Issue: "Changes not reflecting"
**Solution**:
1. Rebuild: `make clean && make build`
2. Clear browser cache: Ctrl+Shift+Delete
3. Hard refresh: Ctrl+F5
4. Restart servers: `make stop && make run`

#### Issue: "Git merge conflicts"
**Solution**:
```bash
# View conflicts
git status

# Open file and resolve manually
# Look for: <<<<<<< HEAD, ======, >>>>>>>

# After resolving:
git add .
git commit -m "resolve: merge conflicts"
git push
```

---

## Additional Resources

- **Java**: https://docs.oracle.com/javase/11/
- **WebSocket**: https://tools.ietf.org/html/rfc6455
- **Maven**: https://maven.apache.org/guides/
- **Git**: https://git-scm.com/doc
- **JavaScript**: https://developer.mozilla.org/en-US/docs/Web/JavaScript

---

**Happy coding!** 🚀
