# Team Contributions - Tank Arena Multiplayer Game

## IN 3111 - Network Programming - Assignment 2

**Project:** Tank Arena - Real-time Multiplayer Battle Arena  
**Repository:** th33k/Tank-Game  
**Academic Year:** 2025

---

## Project Overview

Tank Arena is a real-time multiplayer tank battle game that demonstrates advanced Java Network Programming concepts. The application implements client-server architecture using WebSockets, multi-threaded game room management, and concurrent player handling to provide a seamless multiplayer gaming experience.

### Core Network Programming Concepts Implemented:

- WebSocket Protocol for real-time bidirectional communication
- Multi-threaded server architecture
- Concurrent client connection handling
- Game state synchronization across multiple clients
- Non-blocking I/O operations
- Thread-safe data structures for game state management

---

## Team Members & Individual Contributions

### Member 1: WebSocket Server & Client Connection Management

**Implemented Components:**

- `WebSocketHandler.java`
- `GameServer.java` (main server initialization)

**Network Programming Concepts:**

#### 1. WebSocket Server Implementation

- Established WebSocket server using Java WebSocket API (javax.websocket)
- Configured server endpoint to accept client connections on port 8080
- Implemented connection lifecycle management (@OnOpen, @OnClose, @OnError, @OnMessage)

```java
@ServerEndpoint("/game")
public class WebSocketHandler {
    @OnOpen
    public void onOpen(Session session) {
        // Handle new client connection
    }

    @OnClose
    public void onClose(Session session) {
        // Handle client disconnection
    }
}
```

#### 2. Session Management

- Managed individual WebSocket sessions for each connected client
- Implemented session tracking using thread-safe ConcurrentHashMap
- Handled graceful disconnection and connection timeout scenarios

#### 3. Server Bootstrap & Configuration

- Configured Tomcat embedded server for WebSocket support
- Set up server initialization parameters (port, context path, protocol)
- Implemented server startup and shutdown hooks

**Key Learning Outcomes:**

- Understanding WebSocket protocol vs traditional HTTP
- Managing persistent connections in web applications
- Server-side session lifecycle management
- Handling network errors and connection failures

---

### Member 2: Multi-threaded Client Handler & Request Processing

**Implemented Components:**

- `ClientHandler.java`
- Player authentication and lobby join logic

**Network Programming Concepts:**

#### 1. Multi-threaded Client Processing

- Created dedicated thread for each client connection
- Implemented thread pooling to manage concurrent client requests
- Used ExecutorService for efficient thread management

```java
ExecutorService clientThreadPool = Executors.newFixedThreadPool(50);
clientThreadPool.submit(new ClientHandler(session, player));
```

#### 2. Concurrent Message Processing

- Handled incoming client messages in separate threads
- Implemented message queue for processing player actions (MOVE, SHOOT, CHAT)
- Ensured thread-safety using synchronized methods and locks

#### 3. Player Authentication & Lobby Management

- Processed player join requests with username validation
- Implemented lobby system where players wait before joining active game
- Managed player state transitions (LOBBY → IN_GAME → DISCONNECTED)

#### 4. Request-Response Pattern

- Implemented message parsing and routing based on action types
- Created structured JSON message format for client-server communication
- Handled various message types: JOIN, MOVE, SHOOT, CHAT, DISCONNECT

**Key Learning Outcomes:**

- Multi-threading in network applications
- Thread synchronization and race condition prevention
- Concurrent data structure usage (ConcurrentHashMap, BlockingQueue)
- Scalable client request handling architecture

---

### Member 3: Real-time Game State Synchronization & Broadcasting

**Implemented Components:**

- `GameRoom.java` (game state broadcast logic)
- Game loop and state update mechanism

**Network Programming Concepts:**

#### 1. Real-time State Broadcasting

- Implemented server-side game loop running at 60 ticks per second
- Broadcast game state to all connected clients simultaneously
- Used ScheduledExecutorService for periodic state updates

```java
ScheduledExecutorService gameLoop = Executors.newScheduledThreadPool(1);
gameLoop.scheduleAtFixedRate(() -> {
    broadcastGameState();
}, 0, 16, TimeUnit.MILLISECONDS); // 60 FPS
```

#### 2. Multicast Communication Pattern

- Implemented one-to-many communication for game state updates
- Sent game state to all active players in the game room
- Optimized message size using delta compression for bandwidth efficiency

#### 3. Event-Driven Broadcasting

- Broadcast specific events (PLAYER_JOINED, PLAYER_KILLED, POWER_UP_SPAWNED)
- Implemented kill feed notifications sent to all clients
- Real-time leaderboard updates broadcasted on score changes

#### 4. Network Synchronization

- Synchronized game clock across all clients
- Handled client-side prediction and server reconciliation
- Implemented lag compensation for fair gameplay

**Key Learning Outcomes:**

- Broadcasting strategies in multiplayer games
- Network bandwidth optimization techniques
- Clock synchronization in distributed systems
- Handling network latency and packet loss

---

### Member 4: Live Chat System & Message Broadcasting

**Implemented Components:**

- Chat message handling in `ClientHandler.java`
- Chat history and message filtering

**Network Programming Concepts:**

#### 1. Real-time Chat Communication

- Implemented bidirectional chat system using WebSocket
- Processed incoming chat messages from clients
- Broadcast chat messages to all players in the game room

```java
public void handleChatMessage(String message, Player sender) {
    ChatMessage chatMsg = new ChatMessage(sender.getName(), message, timestamp);
    broadcastToAll(chatMsg);
}
```

#### 2. Message Filtering & Validation

- Validated chat message length and content
- Implemented profanity filter and spam prevention
- Rate-limited chat messages to prevent flooding (max 5 messages/second)

#### 3. Chat Persistence & History

- Maintained chat history buffer (last 50 messages)
- Sent chat history to newly joined players
- Implemented message timestamping for chronological order

#### 4. Selective Broadcasting

- Team chat vs global chat routing logic
- Whisper/private messaging between players
- Admin broadcast messages with special formatting

**Key Learning Outcomes:**

- Real-time messaging protocols
- Message queuing and delivery guarantees
- Network security considerations (message validation, rate limiting)
- Selective message routing in multi-user environments

---

### Member 5: Leaderboard System & Score Synchronization

**Implemented Components:**

- Leaderboard tracking in `GameRoom.java`
- Score update and persistence logic
- Player statistics calculation

**Network Programming Concepts:**

#### 1. Distributed State Management

- Maintained centralized leaderboard on server
- Synchronized player scores across all clients in real-time
- Handled concurrent score updates using atomic operations

```java
AtomicInteger playerScore = new AtomicInteger(0);
public void updateScore(Player player, int points) {
    int newScore = player.getScore().addAndGet(points);
    broadcastLeaderboardUpdate();
}
```

#### 2. Real-time Leaderboard Broadcasting

- Sent leaderboard updates when any player's score changes
- Implemented delta updates to minimize network traffic
- Broadcast winning score threshold to all clients

#### 3. Player Statistics Tracking

- Tracked kills, deaths, K/D ratio per player
- Calculated and synchronized statistics across network
- Implemented session-based stats vs persistent global stats

#### 4. Event Notification System

- Broadcast player achievements (first blood, killing spree)
- Sent win/loss notifications with final leaderboard
- Implemented ranking system (1st, 2nd, 3rd place announcements)

#### 5. Lobby Status Updates

- Real-time player count updates in lobby
- Displayed current leaderboard in lobby screen before game join
- Synchronized winning score configuration across all clients

**Key Learning Outcomes:**

- Distributed state consistency in networked applications
- Atomic operations for concurrent data updates
- Efficient data serialization for network transmission
- Event-driven architecture in real-time systems

---

## Network Programming Concepts Summary

### 1. **Socket Programming & WebSocket Protocol**

- Established persistent bidirectional connections
- Full-duplex communication for real-time gameplay
- Handled connection lifecycle and error recovery

### 2. **Multi-threading & Concurrency**

- Implemented thread-per-client model
- Used thread pools for scalable connection handling
- Applied synchronization mechanisms (locks, atomic operations)
- Utilized concurrent data structures (ConcurrentHashMap, BlockingQueue)

### 3. **Client-Server Architecture**

- Authoritative server for game logic
- Client-side prediction for responsive gameplay
- Server reconciliation for consistency

### 4. **Message Serialization & Protocol Design**

- JSON-based message format for client-server communication
- Structured message types (JOIN, MOVE, SHOOT, CHAT, etc.)
- Efficient serialization/deserialization using JsonUtil

### 5. **Broadcasting & Multicast Patterns**

- One-to-many communication for game state updates
- Selective broadcasting (room-based, team-based)
- Event-driven notifications

### 6. **Network Optimization**

- Delta compression for state updates
- Message batching to reduce overhead
- Bandwidth management and throttling

### 7. **Scalability & Performance**

- Non-blocking I/O operations
- Efficient thread management
- Resource cleanup and memory management

---

## Technology Stack

### Backend (Server-Side)

- **Language:** Java 17
- **Framework:** Java WebSocket API (javax.websocket)
- **Server:** Apache Tomcat Embedded
- **Build Tool:** Maven
- **Concurrency:** java.util.concurrent package

### Frontend (Client-Side)

- **HTML5 Canvas** for game rendering
- **JavaScript** for client-side game logic
- **WebSocket API** for server communication
- **CSS3** for UI styling

---

## Project Structure

```
Tank-Game/
├── server/
│   └── src/main/java/com/minitankfire/
│       ├── server/
│       │   └── GameServer.java          [Member 1]
│       ├── network/
│       │   ├── WebSocketHandler.java    [Member 1]
│       │   └── ClientHandler.java       [Member 2]
│       ├── game/
│       │   └── GameRoom.java            [Member 3 & 5]
│       ├── model/
│       │   ├── Player.java
│       │   ├── Bullet.java
│       │   └── PowerUp.java
│       └── util/
│           └── JsonUtil.java
├── client/
│   ├── index.html
│   ├── css/style.css
│   └── js/game.js
└── docs/
    ├── ARCHITECTURE.md
    └── GAMEPLAY.md
```

---

## Key Features Demonstrating Network Programming

### 1. Real-time Multiplayer Gameplay

- Up to 50 concurrent players per game room
- 60 Hz server tick rate for smooth gameplay
- Sub-100ms latency for responsive controls

### 2. Lobby System

- Pre-game lobby with player list
- Configurable winning score
- Real-time player count updates

### 3. Live Chat System

- Global team chat
- Message history and persistence
- Spam prevention and rate limiting

### 4. Leaderboard & Statistics

- Real-time score updates
- Kill/death tracking
- Ranking and achievement notifications

### 5. Game State Synchronization

- Player positions and movements
- Bullet trajectories and collisions
- Power-up spawns and pickups
- Health and damage synchronization

---

## Testing & Validation

### Network Testing Performed:

1. **Load Testing:** Simulated 50+ concurrent connections
2. **Latency Testing:** Measured round-trip time for various actions
3. **Packet Loss Simulation:** Tested game behavior under poor network conditions
4. **Disconnection Handling:** Verified graceful handling of sudden disconnects
5. **Race Condition Testing:** Stress-tested concurrent score updates and chat messages

---

## Challenges & Solutions

### Challenge 1: Thread Safety in Game State

**Problem:** Race conditions when multiple players update game state simultaneously  
**Solution:** Implemented ConcurrentHashMap and atomic operations for score updates

### Challenge 2: Network Latency Compensation

**Problem:** Player actions felt delayed due to network latency  
**Solution:** Implemented client-side prediction with server reconciliation

### Challenge 3: Bandwidth Optimization

**Problem:** Full game state broadcasts consumed excessive bandwidth  
**Solution:** Implemented delta compression, sending only changed data

### Challenge 4: Scalability

**Problem:** Performance degradation with many concurrent players  
**Solution:** Optimized thread pool size and implemented efficient broadcasting

### Challenge 5: Connection Stability

**Problem:** Players disconnecting caused game state inconsistencies  
**Solution:** Implemented robust error handling and automatic cleanup

---

## Learning Outcomes

Through this project, the team gained hands-on experience with:

1. **WebSocket Protocol:** Real-time bidirectional communication
2. **Multi-threading:** Concurrent programming and synchronization
3. **Network Architecture:** Client-server design patterns
4. **State Management:** Distributed state consistency
5. **Performance Optimization:** Network bandwidth and latency optimization
6. **Error Handling:** Robust network error recovery
7. **Scalability:** Designing for multiple concurrent users

---

## Conclusion

Tank Arena successfully demonstrates core Network Programming concepts required for modern multiplayer applications. Each team member contributed a distinct networking feature, collectively creating a fully functional real-time multiplayer game. The project showcases practical applications of Java socket programming, multi-threading, concurrent data management, and efficient network communication protocols.

The implementation follows industry best practices for scalable networked applications and provides a solid foundation for understanding distributed systems and real-time communication in software development.

---

## References

1. Java WebSocket API Documentation - Oracle
2. "Java Network Programming" by Elliotte Rusty Harold
3. "Multiplayer Game Programming" by Joshua Glazer & Sanjay Madhav
4. WebSocket RFC 6455 Specification
5. Java Concurrency in Practice - Brian Goetz

---

**Project Repository:** [https://github.com/th33k/Tank-Game](https://github.com/th33k/Tank-Game)  
**Branch:** feature/winning-score-logic2  
**Date:** November 2025
