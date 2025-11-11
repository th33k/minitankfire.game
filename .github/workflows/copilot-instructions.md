
---

## 🧠 `copilot-instructions.md`

### 🎯 Project Goal

Develop a **web-based multiplayer game backend** using **Core Java 21 network programming concepts**.
The server will handle **real-time communication** between multiple players over the network using **TCP, UDP, and NIO**, while the currently implemented frontend.
The focus is entirely on the **server-side networking, concurrency, and data handling** using **pure Java** — no frameworks.

---

### 🕹️ Game Context

The Currently implemented multiplayer game:

* Real-time position updates and actions from multiple players.
* Fast and reliable message delivery (TCP for reliability, UDP for speed).
* Concurrent player connections and synchronization.
* A server that manages player sessions, game state, and broadcasts updates.

---

### 🧩 Java Network Programming Concepts to Demonstrate

| Concept                                            | Purpose in Game                                                        | Java Classes/Packages                                       |
| -------------------------------------------------- | ---------------------------------------------------------------------- | ----------------------------------------------------------- |
| **TCP (ServerSocket, Socket)**                     | Reliable data transfer (e.g., login, game setup, chat).                | `java.net.ServerSocket`, `java.net.Socket`                  |
| **UDP (DatagramSocket, DatagramPacket)**           | Fast real-time game updates (e.g., player movement, actions).          | `java.net.DatagramSocket`, `java.net.DatagramPacket`        |
| **NIO (Channels, Buffers, Selectors)**             | Non-blocking I/O for scalability with many clients.                    | `java.nio.channels.*`, `java.nio.ByteBuffer`                |
| **Multithreading (Thread, ExecutorService)**       | Handle multiple player connections concurrently.                       | `java.util.concurrent.*`, `Thread`                          |
| **Serialization (Object Streams)**                 | Transfer game state objects (e.g., player info, bullets, map updates). | `ObjectInputStream`, `ObjectOutputStream`                   |
| **Networking Utilities (InetAddress, HttpServer)** | IP handling and optional HTTP communication (e.g., leaderboard).       | `java.net.InetAddress`, `com.sun.net.httpserver.HttpServer` |
| **Game Loop Timing (ScheduledExecutorService)**    | Maintain consistent tick rate (e.g., 60 updates/sec).                  | `ScheduledExecutorService`, `Runnable`                      |

> ✅ Use **Core Java 21 only** — no Spring Boot, Netty, or external libraries.

---

### 📁 Folder Structure

```
java-multiplayer-game/
│
├── server/src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/example/game/
│   │   │   │   ├── server/
│   │   │   │   │   ├── GameServer.java           # Main entry for server
│   │   │   │   │   ├── TcpServer.java            # Handles reliable data
│   │   │   │   │   ├── UdpServer.java            # Handles fast real-time updates
│   │   │   │   │   ├── NioServer.java            # Non-blocking I/O demo
│   │   │   │   │   ├── ClientHandler.java        # One thread per client
│   │   │   │   │   ├── GameLoop.java             # Game tick controller
│   │   │   │   │   ├── GameStateManager.java     # Tracks all players
│   │   │   │   │   └── BroadcastService.java     # Sends updates to all clients
│   │   │   │   │
│   │   │   │   ├── client/
│   │   │   │   │   ├── TcpClient.java
│   │   │   │   │   ├── UdpClient.java
│   │   │   │   │   ├── NioClient.java
│   │   │   │   │   └── ClientInputHandler.java
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── Player.java
│   │   │   │   │   ├── GameObject.java
│   │   │   │   │   ├── GameAction.java
│   │   │   │   │   └── Message.java
│   │   │   │   │
│   │   │   │   ├── util/
│   │   │   │   │   ├── LoggerUtil.java
│   │   │   │   │   └── NetworkUtils.java
│   │   │   │   │
│   │   │   │   └── Main.java
│   │   │
│   │   └── resources/
│   │       └── config.properties
│   │
│   └── test/
│       └── GameServerTest.java
│
├── client/ 
│   ├── index.html
│   ├── css/style.css
│   └── js/app.js
│
├── README.md
└── .github/
    └── copilot-instructions.md
    
```

---

### ⚙️ Implementation Guidelines for Copilot

#### 1. Programming Rules

* Only **Core Java 21** standard APIs.
* No external dependencies or frameworks.
* Use modular code and packages as shown above.

#### 2. Networking Requirements

* Implement both **TCP** (for login, chat, lobby setup) and **UDP** (for in-game position updates).
* Use **multithreading** to manage each client connection.
* Use **ExecutorService** or **ThreadPoolExecutor** for efficiency.
* Implement **broadcasting** to send updates to all connected players.
* Add **basic serialization** for transferring object data.

#### 3. Game Loop

* Create a **GameLoop.java** that runs at a fixed tick rate (e.g., 60 FPS).
* Periodically updates positions, handles collisions, and sends state updates.

#### 4. Synchronization

* Use synchronized blocks or concurrent collections for shared data (e.g., `ConcurrentHashMap` for player sessions).
* Prevent race conditions when multiple threads modify the game state.

#### 5. NIO Integration

* Implement a simple **NioServer.java** version that uses non-blocking channels and selectors.
* Compare it with the blocking I/O TCP version.

#### 6. Optional HTTP Features

* Create a small `HttpServerExample.java` using `com.sun.net.httpserver.HttpServer` to display online players or leaderboard data in a browser.

#### 7. Logging and Error Handling

* Use `LoggerUtil.java` for standardized logs (connections, disconnections, errors).
* Handle `IOException` and `SocketTimeoutException` gracefully.
* Print player events and server info to console.

---

### 🧠 Concept Integration in Game Flow

1. **Player joins** via TCP connection → Server authenticates player.
2. **UDP socket** established for real-time gameplay updates.
3. **Multithreaded handlers** manage client sessions and game logic concurrently.
4. **Game loop** updates positions and broadcasts state changes.
5. **NIO server** demo shows how to scale non-blocking player communication.
6. **Serialization** used for compact game state updates.
7. **Optional HTTP server** provides game stats (leaderboard, player count).

---

### ✅ Deliverables

* Full Java source under `server/src/main/java/com/example/game/`
* production level frontend under `/client/`
* Configuration file (`config.properties`) for ports and tick rates.
* This `copilot-instructions.md` and `README.md` with explanations.
* updated GitHub repository with all code and instructions.
---

### 💡 Best Practices for Copilot

* Use meaningful class and method names.
* Write small, single-responsibility methods.
* Use clear comments and JavaDoc explaining network logic.
* Print server startup info (port, protocol, player connections).
* Keep data formats (JSON/String/Object) consistent for client-server sync.

---

### 🔍 Example Scenarios

| Scenario         | Java Concept      | Description                                      |
| ---------------- | ----------------- | ------------------------------------------------ |
| Player connects  | TCP               | ServerSocket accepts client connection.          |
| Player moves     | UDP               | Fast broadcast of position update.               |
| Game tick        | Multithreading    | ScheduledExecutorService updates all entities.   |
| Multiple clients | Thread per client | Each client runs independently.                  |
| Leaderboard      | HTTP Server       | Displays game info via simple GET endpoint.      |
| Scaling          | NIO               | Non-blocking selector handles multiple channels. |

---