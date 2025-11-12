# Tank Arena - Network Programming Project Report

## IN 3111 - Network Programming - Assignment 2

**Project Name:** Mini Tank Fire: Online  
**Academic Year:** 2025  
**Repository:** [https://github.com/th33k/Tank-Game](https://github.com/th33k/Tank-Game)

---

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Network Programming Concepts Used](#network-programming-concepts-used)
3. [Challenges Faced and Solutions](#challenges-faced-and-solutions)
4. [Conclusion](#conclusion)

---

## 1. System Overview

### Project Description

**Mini Tank Fire: Online** is a real-time multiplayer battle arena game that demonstrates advanced network programming concepts using pure Java implementation. The project showcases a client-server architecture where multiple players can connect simultaneously, control tanks, engage in combat, collect power-ups, and communicate through text and voice chat—all synchronized in real-time across the network.

The game implements a complete WebSocket-based communication system from scratch, adhering to RFC 6455 specifications without relying on external networking frameworks. This educational project emphasizes fundamental networking principles including socket programming, multi-threading, concurrent state management, and real-time data synchronization.

### Main Features

Key features (high-level):

- Real-time multiplayer combat with authoritative Java server and up to 100 concurrent players
- Responsive click-to-fire weapons, bullet physics and collision handling
- Power-ups (shield, speed boost, double fire) and automatic respawn mechanics
- Real-time state synchronization at 20 FPS with low-latency TCP/WebSocket transport
- Built-in text chat and optional peer-to-peer voice chat (WebRTC)
- Modern HUD, live leaderboard and lobby system for pre-game setup
- Pure-Java server implementation with custom JSON serialization and thread-safe state management
- Graceful shutdown and resource cleanup

---

## 2. Network Programming Concepts Used

### 2.1 Socket Programming (TCP)

TCP/IP sockets establish reliable, connection-oriented communication between clients and servers with guaranteed delivery and packet ordering. The server creates a `ServerSocket` listening on port 8080 with TCP NoDelay enabled for low-latency real-time game data transmission.

**Files:** `GameServer.java`, `WebSocketHandler.java`

### 2.2 WebSocket Protocol (RFC 6455)

Custom implementation of WebSocket protocol providing full-duplex bidirectional communication over a single TCP connection. Handles HTTP upgrade handshakes, frame encoding/decoding with masking, and multiple frame types (text, ping, pong, close).

**Files:** `WebSocketHandler.java`

### 2.3 Multi-threading and Concurrency

ExecutorService thread pool manages up to 100 concurrent client connections, each with dedicated threads for independent message processing. Thread-safe `ConcurrentHashMap` stores shared game state with a dedicated 20 FPS game loop thread.

**Files:** `GameServer.java`, `ClientHandler.java`, `GameRoom.java`

### 2.4 Message Broadcasting

Server broadcasts complete game state (players, bullets, power-ups) to all connected clients every 50ms (20 FPS). Iterates through all client handlers to send synchronized updates and event notifications.

**Files:** `GameRoom.java`

### 2.5 Client-Server Architecture

Authoritative server model where server maintains single source of truth for all game state. Clients handle only rendering and input, sending player actions to server for validation and processing.

**Files:** `GameRoom.java`, `game-client.js`, `managers/network-manager.js`

### 2.6 Protocol Design and Serialization

Custom JSON-based protocol with message type routing system. Hand-coded JSON parser/serializer without external libraries handles game object serialization.

**Files:** `JsonUtil.java`

### 2.7 Session Management

Tracks client connection lifecycle with unique UUID identification per client. Manages player registration, connection state tracking, and graceful disconnection with proper resource cleanup.

**Files:** `ClientHandler.java`, `WebSocketHandler.java`, `GameRoom.java`

### 2.8 Real-time State Synchronization

Fixed 20 FPS tick rate provides consistent update intervals. Each tick performs physics updates, collision detection, power-up management, and broadcasts complete game state to all clients.

**Files:** `GameRoom.java`

### 2.9 Thread Safety

`ConcurrentHashMap` provides lock-free thread-safe access to shared state. Synchronized methods prevent interleaved socket writes, volatile variables ensure cross-thread visibility.

**Files:** `GameRoom.java`, `WebSocketHandler.java`

### 2.10 Error Handling and Recovery

Try-catch blocks wrap all network operations to prevent server crashes. Validates connections before sending, implements graceful shutdown with cleanup hooks, handles broadcast errors individually.

**Files:** `ClientHandler.java`, `GameServer.java`, `GameRoom.java`

---

## 3. Challenges Faced and Solutions

### Challenge 1: WebSocket Protocol Implementation

Implementing RFC 6455 step-by-step with proper handshake validation and XOR unmasking to handle frame parsing and masked frames correctly.

### Challenge 2: Race Conditions in Multi-threaded State

Used `ConcurrentHashMap`, `synchronized` methods, and `volatile` flags to prevent concurrent access issues and crashes.

### Challenge 3: Network Latency and Desynchronization

Enabled TCP NoDelay, implemented client-side prediction, and optimized message sizes to reduce lag from 200ms to under 50ms.

### Challenge 4: Memory Leaks and Resource Management

Implemented time-based expiration, proper thread pool shutdown, and cleanup hooks to prevent OutOfMemoryError crashes.

---

## 4. Conclusion

The **Mini Tank Fire: Online** project successfully demonstrates comprehensive network programming principles through the development of a fully functional real-time multiplayer game. By implementing a pure Java server with custom WebSocket protocol handling, multi-threaded client management, and distributed state synchronization, the team gained hands-on experience with the fundamental concepts required for building scalable networked applications.

The project showcases practical applications of critical networking concepts including socket programming with TCP/IP, protocol design and implementation (RFC 6455 WebSocket), concurrent programming with thread pools and synchronization, real-time message broadcasting and multicasting, and client-server architecture with authoritative server design. Through overcoming challenges such as protocol implementation complexity, race conditions in concurrent environments, network latency effects on gameplay, and resource management in long-running servers, the team developed a deep understanding of both the theoretical foundations and practical considerations of network programming.

The final system successfully supports up to 100 concurrent players with smooth 20 FPS game state synchronization, sub-50ms latency, stable memory usage under extended operation, and robust error handling and graceful shutdown. This project serves as a practical demonstration of modern multiplayer game networking while strictly adhering to pure Java implementation without external networking frameworks.

### Tools and Technologies Used

#### Backend Technologies

| Technology                | Version  | Purpose                                                                        |
| ------------------------- | -------- | ------------------------------------------------------------------------------ |
| **Java**                  | 11+      | Primary programming language for server implementation                         |
| **Maven**                 | 3.8.0+   | Build automation and dependency management                                     |
| **java.net.ServerSocket** | Built-in | TCP server socket for accepting client connections                             |
| **java.net.Socket**       | Built-in | TCP client socket for individual connections                                   |
| **java.util.concurrent**  | Built-in | Thread pool management (ExecutorService) and concurrent collections            |
| **java.io Streams**       | Built-in | Input/output stream handling for network communication                         |
| **WebSocket Protocol**    | RFC 6455 | Custom manual implementation for real-time bidirectional communication         |
| **JSON**                  | Custom   | Hand-coded serialization/deserialization (JsonUtil) without external libraries |

#### Frontend Technologies

| Technology         | Version | Purpose                                                          |
| ------------------ | ------- | ---------------------------------------------------------------- |
| **HTML5**          | -       | Markup for game interface and canvas element                     |
| **CSS3**           | -       | Styling with modern design (neon theme, animations, transitions) |
| **JavaScript ES6** | -       | Client-side game logic, rendering, and input handling            |
| **Canvas API**     | HTML5   | 2D graphics rendering for game visualization                     |
| **WebSocket API**  | Native  | Browser-side WebSocket client for server communication           |
| **WebRTC**         | -       | Peer-to-peer voice chat implementation                           |
| **Font Awesome**   | 6.4.0   | Icon library for UI elements                                     |

#### Development Tools

| Tool                        | Version | Purpose                                                |
| --------------------------- | ------- | ------------------------------------------------------ |
| **Git**                     | -       | Version control and collaboration                      |
| **GitHub**                  | -       | Repository hosting and team coordination               |
| **Makefile**                | -       | Build automation for quick compilation and execution   |
| **Python HTTP Server**      | 3.6+    | Serving static client files during development         |
| **VS Code / IntelliJ IDEA** | -       | Integrated development environments                    |
| **Browser DevTools**        | -       | Debugging client-side JavaScript and WebSocket traffic |

#### Key Architectural Decisions

- **Pure Java Implementation**: No external networking frameworks (no Spring, no Netty) to demonstrate core networking APIs
- **No External JSON Libraries**: Custom JSON parser/serializer to avoid dependencies like Gson or Jackson
- **Thread Pool Design**: Fixed thread pool (100 threads) balancing scalability with resource limits
- **Concurrent Collections**: ConcurrentHashMap for lock-free thread-safe access to shared state
- **20 FPS Tick Rate**: Balances responsiveness, server CPU usage, and network bandwidth
- **Authoritative Server Model**: Server maintains single source of truth, preventing client-side cheating

### Learning Outcomes

Through this project, team members gained practical experience in:

- ✅ **Socket Programming**: Creating TCP servers, accepting connections, configuring socket options
- ✅ **Protocol Implementation**: Understanding and implementing WebSocket (RFC 6455) from specification
- ✅ **Multi-threading**: Designing thread pool architectures and managing concurrent client connections
- ✅ **Synchronization**: Preventing race conditions using concurrent collections, locks, and atomic operations
- ✅ **Real-time Systems**: Implementing fixed tick rate game loops and state broadcasting
- ✅ **Network Optimization**: Reducing latency through TCP NoDelay, message optimization, and client prediction
- ✅ **Resource Management**: Preventing memory leaks through proper cleanup and lifecycle management
- ✅ **Error Handling**: Building robust systems that gracefully handle network failures
- ✅ **Distributed Systems**: Maintaining consistency across multiple clients with authoritative server design
- ✅ **Software Architecture**: Organizing code into modular packages with clear separation of concerns

---

**Project Repository:** [https://github.com/th33k/Tank-Game](https://github.com/th33k/Tank-Game)  
**Branch:** feature/winning-score-logic2  
**Date:** November 12, 2025

---

**© 2025 Tank Arena Development Team**  
**IN 3111 - Network Programming Module**
