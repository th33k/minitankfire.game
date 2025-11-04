# Java Backend Documentation

## Overview

The Java backend for the Tank Game is a WebSocket-based server that manages real-time multiplayer game sessions. It handles client connections, processes game actions, maintains game state, and broadcasts updates to all connected players. The server is built using Jetty for HTTP/WebSocket support and Gson for JSON message handling.

## Architecture

### High-Level Design
- **Client-Server Model**: HTML/JS clients connect to Java server
- **Communication Protocol**: JSON messages over WebSocket
- **Concurrency Model**: Asynchronous event-driven processing
- **State Management**: Thread-safe in-memory game state

### Data Flow
1. Client establishes WebSocket connection to `/game` endpoint
2. Server assigns unique player ID and adds to game room
3. Client sends action messages (move, fire, chat)
4. Server processes actions and updates game state
5. Server broadcasts state updates to all connected clients
6. Loop continues until client disconnects

## Package Structure

The Java backend is now organized into the following packages for better maintainability:

### `com.minitankfire.server`
- **GameServer.java**: Main server entry point and Jetty configuration

### `com.minitankfire.websocket` 
- **GameWebSocket.java**: WebSocket connection handling and message routing

### `com.minitankfire.game`
- **GameRoom.java**: Game state management and logic coordination

### `com.minitankfire.model`
- **Player.java**: Player entity representation
- **Bullet.java**: Projectile entity representation  
- **PowerUp.java**: Collectible item representation

### `com.minitankfire.protocol`
- **Message.java**: Message structures and JSON serialization utilities

#### Purpose
Sets up and configures the Jetty HTTP server with WebSocket capabilities.

#### Key Methods
- `main(String[] args)`: Entry point, initializes and starts server
- `GameWebSocketServlet.configure()`: Registers WebSocket handler

#### Implementation Details
- Binds to port 8080 on all interfaces (0.0.0.0)
- Uses ServletContextHandler for HTTP request routing
- Registers WebSocket servlet at `/game` path
- Starts server in blocking mode with `server.join()`

#### Dependencies
- `org.eclipse.jetty.server.Server`
- `org.eclipse.jetty.server.ServerConnector`
- `org.eclipse.jetty.servlet.ServletContextHandler`

### 2. GameWebSocket.java - WebSocket Connection Handler

#### Purpose
Manages individual WebSocket connections and routes incoming messages to game logic.

#### Key Methods
- `onConnect(Session session)`: Handles new connections, assigns player IDs
- `onMessage(Session session, String message)`: Parses and processes JSON messages
- `onClose(Session session, int statusCode, String reason)`: Cleans up disconnected players
- `onError(Session session, Throwable error)`: Handles connection errors

#### Message Routing
- `join`: Player registration with name
- `move`: Position and angle updates
- `fire`: Bullet creation
- `chat`: Text message broadcasting
- `voice-offer/answer/ice`: WebRTC signaling

#### Implementation Details
- Uses Jetty WebSocket annotations for event handling
- Parses JSON with `JsonParser.parseString()`
- Delegates game logic to `GameRoom` instance
- Generates UUIDs for player identification

#### Dependencies
- `org.eclipse.jetty.websocket.api.Session`
- `org.eclipse.jetty.websocket.api.annotations.*`
- `com.google.gson.JsonObject`
- `com.google.gson.JsonParser`

### 3. Message.java - Message Protocol Definitions

#### Purpose
Defines all network message structures and provides JSON serialization utilities.

#### Message Types

##### Client-to-Server Messages
- `JoinMessage`: `{type: "join", name: string}`
- `MoveMessage`: `{type: "move", x: int, y: int, angle: int}`
- `FireMessage`: `{type: "fire"}`
- `ChatMessage`: `{type: "chat", msg: string}`

##### Server-to-Client Messages
- `UpdateMessage`: `{type: "update", players: Player[], bullets: Bullet[], powerUps: PowerUp[]}`
- `HitMessage`: `{type: "hit", target: string, shooter: string}`
- `ScoreMessage`: `{type: "score", scores: JsonObject}`
- `RespawnMessage`: `{type: "respawn", playerId: string, x: int, y: int}`

#### Key Methods
- `toJson(Object obj)`: Serializes objects to JSON string
- `fromJson(String json, Class<T> clazz)`: Deserializes JSON to objects

#### Implementation Details
- Static inner classes for type safety
- Gson-based serialization with custom field mapping
- Consistent `type` field for message identification

#### Dependencies
- `com.google.code.gson.Gson`

### 4. GameRoom.java - Game State Manager

#### Purpose
Maintains centralized game state and coordinates all game logic and broadcasting.

#### Key Data Structures
- `players`: Map<String, Player> - Active players by ID
- `bullets`: Map<String, Bullet> - Active projectiles by ID
- `powerUps`: Map<String, PowerUp> - Collectible items by ID
- `sessions`: Map<String, Session> - WebSocket sessions by player ID

#### Key Methods
- `addPlayer(String playerId, String name, Session session)`: Registers new player
- `removePlayer(String playerId)`: Cleans up disconnected player
- `handleMove(String playerId, int x, int y, int angle)`: Updates player position
- `handleFire(String playerId)`: Creates new bullet
- `handleChat(String playerId, String msg)`: Broadcasts chat message
- `broadcastUpdate()`: Sends game state to all clients
- `startGameLoop()`: Initiates periodic update thread

#### Game Constants
- `MAP_WIDTH = 800`, `MAP_HEIGHT = 600`
- `PLAYER_SPEED = 3`, `BULLET_SPEED = 8`

#### Implementation Details
- Uses `ConcurrentHashMap` for thread-safe operations
- Runs game loop in separate thread at ~20 FPS
- Handles collision detection and scoring
- Manages power-up spawning and collection

#### Dependencies
- `java.util.concurrent.ConcurrentHashMap`
- `java.util.Random`
- `org.eclipse.jetty.websocket.api.Session`

### 5. Supporting Model Classes

#### Player.java
- **Fields**: id, name, x, y, angle, health, score, alive status
- **Methods**: getters/setters, damage handling
- **Purpose**: Represents player entities with game statistics

#### Bullet.java
- **Fields**: id, ownerId, x, y, velocityX, velocityY
- **Methods**: position updates, collision detection
- **Purpose**: Represents projectiles with physics

#### PowerUp.java
- **Fields**: id, x, y, type, duration
- **Methods**: effect application, expiration handling
- **Purpose**: Represents temporary player enhancements

## Dependencies

### Maven Dependencies
```xml
<!-- Jetty WebSocket Server -->
<dependency>
    <groupId>org.eclipse.jetty</groupId>
    <artifactId>jetty-server</artifactId>
    <version>9.4.51.v20230217</version>
</dependency>

<dependency>
    <groupId>org.eclipse.jetty.websocket</groupId>
    <artifactId>websocket-server</artifactId>
    <version>9.4.51.v20230217</version>
</dependency>

<dependency>
    <groupId>org.eclipse.jetty.websocket</groupId>
    <artifactId>websocket-api</artifactId>
    <version>9.4.51.v20230217</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### Java Version
- **Minimum**: Java 11
- **Build Tool**: Maven 3.6+

## Building and Running

### Build Process
```bash
cd server
mvn clean compile
```

### Run Server
```bash
mvn exec:java
```

### Alternative Scripts
- Windows: `run.bat`
- Unix/Linux: `run.sh`

### Server Configuration
- **Port**: 8080 (configurable in GameServer.java)
- **Host**: 0.0.0.0 (all interfaces)
- **WebSocket Path**: `/game`

## Communication Protocol

### Connection Establishment
1. Client connects to `ws://server:8080/game`
2. Server assigns UUID and acknowledges connection
3. Client sends `join` message with player name
4. Server adds player to game room and starts broadcasting updates

### Message Format
All messages are JSON objects with a `type` field:

```json
{
  "type": "message_type",
  // type-specific fields
}
```

### Error Handling
- Invalid JSON: Connection closed with error
- Unknown message type: Ignored
- Network errors: Automatic reconnection handled by client

## Game Logic Components

### Movement System
- Players move at constant speed (3 pixels/update)
- Boundary checking prevents leaving map area
- Angle determines movement direction

### Combat System
- Bullets travel at 8 pixels/update
- Collision detection between bullets and players
- Hit players lose health and respawn
- Score tracking for kills

### Power-up System
- Random spawning on map
- Types: Shield, Speed Boost, Double Fire
- Temporary effects with duration
- Visual indicators in client

### Broadcasting System
- Full state updates every ~50ms
- Selective messaging for chat/private communications
- Efficient JSON serialization

## Threading and Concurrency

### WebSocket Threads
- Jetty manages connection threads automatically
- Each WebSocket event handled in separate thread
- Non-blocking message processing

### Game Loop Thread
- Dedicated thread for periodic updates
- Runs at 20 FPS for smooth gameplay
- Independent of WebSocket event timing

### Thread Safety
- `ConcurrentHashMap` for all shared state
- Atomic operations for score updates
- No locks required due to map design

## Configuration and Constants

### Game Settings (GameRoom.java)
```java
private static final int MAP_WIDTH = 800;
private static final int MAP_HEIGHT = 600;
private static final int PLAYER_SPEED = 3;
private static final int BULLET_SPEED = 8;
```

### Server Settings (GameServer.java)
```java
connector.setHost("0.0.0.0");
connector.setPort(8080);
```

## Development and Extension

### Adding New Message Types
1. Define new static class in `Message.java`
2. Add handling logic in `GameWebSocket.onMessage()`
3. Update client-side message processing

### Adding New Game Features
1. Extend model classes (Player, Bullet, PowerUp)
2. Add logic to `GameRoom.java` methods
3. Update message protocol if needed
4. Modify client rendering and input

### Performance Considerations
- WebSocket provides low-latency communication
- ConcurrentHashMap scales well for multiple players
- JSON serialization is lightweight
- Game loop runs efficiently for real-time updates

### Testing
- Unit tests for individual components
- Integration tests for WebSocket communication
- Load testing for concurrent players
- Client-server protocol validation</content>
<parameter name="filePath">d:\GitHub\Tank-Game\docs\JAVA_BACKEND.md