# Network Programming Concepts Implementation

## Overview

This Tank Game server has been refactored to use **pure Java Network Programming concepts** without any external frameworks or libraries. This aligns with the IN 3111 - Network Programming module assignment requirements.

## Key Network Programming Concepts Demonstrated

### 1. **Socket Programming (TCP/IP)**

- **File**: `GameServer.java`
- **Concept**: Uses `java.net.ServerSocket` to create a TCP server
- **Implementation**:
  ```java
  ServerSocket serverSocket = new ServerSocket(port);
  Socket clientSocket = serverSocket.accept(); // Blocking I/O
  ```
- **Learning**: Server sockets listen for incoming connections, accept() blocks until a client connects

### 2. **Client-Server Architecture**

- **Files**: `GameServer.java`, `ClientHandler.java`
- **Concept**: Multiple clients connect to a single centralized server
- **Implementation**: Server accepts connections and delegates each client to a handler
- **Learning**: Understanding the request-response and event-driven communication patterns

### 3. **Multi-threading**

- **File**: `GameServer.java`
- **Concept**: Uses `ExecutorService` thread pool to handle concurrent clients
- **Implementation**:
  ```java
  ExecutorService clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
  clientThreadPool.execute(new ClientHandler(socket, gameRoom));
  ```
- **Learning**: Each client runs in its own thread, allowing concurrent connections

### 4. **Thread-Safe Concurrent Programming**

- **File**: `GameRoom.java`
- **Concept**: Uses `ConcurrentHashMap` for thread-safe data structures
- **Implementation**:
  ```java
  private Map<String, Player> players = new ConcurrentHashMap<>();
  ```
- **Learning**: Multiple threads can safely access shared game state without race conditions

### 5. **WebSocket Protocol Implementation (RFC 6455)**

- **File**: `WebSocketHandler.java`
- **Concept**: Manual implementation of WebSocket handshake and frame encoding/decoding
- **Key Components**:
  - **HTTP Handshake**: Parses HTTP headers, validates WebSocket upgrade request
  - **SHA-1 Hashing**: Generates Sec-WebSocket-Accept key
  - **Base64 Encoding**: Encodes the accept key
  - **Frame Parsing**: Reads WebSocket frames with masking/unmasking
  - **Binary Protocol**: Handles bit manipulation for frame structure
- **Learning**: Understanding low-level protocol implementation, not just using libraries

### 6. **I/O Streams**

- **File**: `WebSocketHandler.java`
- **Concept**: Uses `InputStream` and `OutputStream` for network data transfer
- **Implementation**:
  ```java
  InputStream input = socket.getInputStream();
  OutputStream output = socket.getOutputStream();
  ```
- **Learning**: Reading and writing bytes over network connections

### 7. **Binary Protocol Parsing**

- **File**: `WebSocketHandler.java`
- **Concept**: Parsing binary data, bit manipulation
- **Implementation**:
  ```java
  boolean fin = (firstByte & 0x80) != 0;
  int opcode = firstByte & 0x0F;
  ```
- **Learning**: Working with binary protocols, bitwise operations

### 8. **Custom JSON Serialization**

- **File**: `JsonUtil.java`
- **Concept**: String manipulation and data serialization without external libraries
- **Implementation**: Manual JSON string construction and parsing
- **Learning**: Understanding data serialization formats

### 9. **Game Loop Threading**

- **File**: `GameRoom.java`
- **Concept**: Separate thread for game state updates
- **Implementation**:
  ```java
  Thread gameLoopThread = new Thread(() -> {
      while (gameRunning) {
          updateGameState();
          Thread.sleep(50); // 20 FPS
      }
  });
  ```
- **Learning**: Real-time systems, thread lifecycle management

### 10. **Socket Configuration**

- **File**: `GameServer.java`
- **Concept**: TCP socket options for performance
- **Implementation**:
  ```java
  clientSocket.setTcpNoDelay(true); // Disable Nagle's algorithm
  clientSocket.setSoTimeout(0); // No read timeout
  ```
- **Learning**: Understanding TCP optimizations for real-time applications

## No External Dependencies

The `pom.xml` file contains **NO dependencies**. All functionality is implemented using only:

- `java.net.*` - Network programming APIs
- `java.io.*` - Input/output streams
- `java.util.concurrent.*` - Thread management
- `java.security.*` - Cryptographic functions (SHA-1)
- `java.util.*` - Core utilities

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      GameServer                             │
│  - ServerSocket (port 8080)                                 │
│  - ExecutorService (Thread Pool)                            │
│  - Accepts incoming connections                             │
└──────────────┬──────────────────────────────────────────────┘
               │
               │ Creates ClientHandler for each connection
               │
       ┌───────┴───────┬─────────────┬─────────────┐
       │               │             │             │
       ▼               ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ClientHandler│ │ClientHandler│ │ClientHandler│ │ClientHandler│
│  (Thread 1) │ │  (Thread 2) │ │  (Thread 3) │ │  (Thread N) │
│             │ │             │ │             │ │             │
│ WebSocket   │ │ WebSocket   │ │ WebSocket   │ │ WebSocket   │
│ Handler     │ │ Handler     │ │ Handler     │ │ Handler     │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │               │                │                │
       └───────────────┴────────────────┴────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │    GameRoom      │
                    │ (Shared State)   │
                    │ - ConcurrentMaps │
                    │ - Game Loop      │
                    └──────────────────┘
```

## How to Run

1. **Compile the server**:

   ```bash
   cd server
   mvn clean compile
   ```

2. **Run the server**:

   ```bash
   mvn exec:java
   ```

   Or with custom port:

   ```bash
   mvn exec:java -Dexec.args="9090"
   ```

3. **Open the client**:
   - Open `client/index.html` in a web browser
   - Connect to `ws://localhost:8080/game`

## Message Flow

```
Client                    Server
  │                          │
  │──── TCP Connection ─────▶│
  │                          │
  │◀──── WebSocket Handshake │
  │      (HTTP Upgrade)      │
  │                          │
  │──── join message ───────▶│
  │                          │
  │◀──── update message ─────│ (Game state)
  │                          │
  │──── move message ───────▶│
  │──── fire message ───────▶│
  │                          │
  │◀──── broadcast updates ──│ (To all clients)
  │                          │
```

## Network Programming Learning Outcomes

By studying this codebase, students will understand:

1. ✅ **Socket Programming**: Creating servers, accepting connections
2. ✅ **TCP/IP Protocol**: How data flows over TCP
3. ✅ **Multi-threading**: Concurrent client handling
4. ✅ **Thread Safety**: Synchronization and concurrent data structures
5. ✅ **Protocol Implementation**: WebSocket RFC 6455 from scratch
6. ✅ **Binary Data Handling**: Parsing frames, bit manipulation
7. ✅ **HTTP Protocol**: Understanding HTTP upgrade mechanism
8. ✅ **Cryptography**: SHA-1 hashing, Base64 encoding
9. ✅ **Real-time Systems**: Game loops, state synchronization
10. ✅ **Network Performance**: TCP_NODELAY, buffering considerations

## Comparison: Before vs After

### Before (Using Jetty Framework)

- ❌ Used `org.eclipse.jetty` for WebSocket server
- ❌ Used `com.google.gson` for JSON parsing
- ❌ High-level abstractions hid network details
- ❌ Did not demonstrate core networking concepts

### After (Pure Java)

- ✅ Manual `ServerSocket` implementation
- ✅ Custom WebSocket protocol handling
- ✅ Custom JSON serialization
- ✅ Direct I/O stream manipulation
- ✅ Explicit multi-threading with thread pools
- ✅ **Full demonstration of network programming concepts**

## File Structure

```
server/src/main/java/com/minitankfire/
├── GameServer.java           # Main server with ServerSocket
├── ClientHandler.java        # Per-client thread handler
├── WebSocketHandler.java     # WebSocket protocol implementation
├── GameRoom.java            # Game logic with thread safety
├── JsonUtil.java            # Custom JSON serialization
├── Player.java              # Game entity
├── Bullet.java              # Game entity
└── PowerUp.java             # Game entity
```

## Conclusion

This implementation fully aligns with the assignment guidelines by demonstrating:

- ✅ Pure Java Network Programming
- ✅ Socket programming (ServerSocket, Socket)
- ✅ Multi-threading and concurrency
- ✅ Client-server communication
- ✅ Protocol implementation (WebSocket)
- ✅ NO external frameworks for networking
- ✅ Real-world application of network concepts

The frontend uses React (as permitted in guidelines), but the backend is **100% pure Java** network programming.
