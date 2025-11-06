## Java network programming concepts — explanation and how this project uses them

This document explains several common Java network programming concepts and shows whether and how the Tank-Game project uses them. For each concept I include a short definition, the typical Java APIs you would use, and concrete evidence from the repository.

---

### 1) Sockets (TCP/UDP)

- What it is: Low-level point-to-point network communication using TCP (`Socket`/`ServerSocket`) or UDP (`DatagramSocket`/`DatagramPacket`).
- Typical Java APIs: `java.net.Socket`, `java.net.ServerSocket`, `java.net.DatagramSocket`, `java.net.DatagramPacket`.
- When to use: Implement custom protocols, small servers/clients, or when you need fine control over transport.

Project usage: NOT used directly.

Evidence:

- The server code uses Jetty and the Jetty WebSocket API rather than raw `Socket`/`ServerSocket`. See `server/src/main/java/com/minitankfire/GameServer.java` which creates an embedded Jetty `Server` and registers a `WebSocketServlet`.

Relevant file(s):

- `server/src/main/java/com/minitankfire/GameServer.java` (Jetty `Server`, `ServerConnector`, `WebSocketServlet`)

---

### 2) Java NIO (non-blocking I/O)

- What it is: New I/O APIs that support non-blocking channels, selectors, and buffers. Typical packages: `java.nio`, `java.nio.channels`.
- Typical Java APIs: `SocketChannel`, `ServerSocketChannel`, `Selector`, `ByteBuffer`.
- When to use: High-performance servers that handle many concurrent connections with few threads.

Project usage: NOT used directly in application code.

Evidence:

- There are no imports or usages of `java.nio.*` in the server sources. The project relies on Jetty which internally may use NIO under the hood depending on configuration, but the application code itself uses the Jetty WebSocket API.

---

### 3) Multithreading / Concurrency

- What it is: Running multiple threads in the JVM to perform tasks concurrently. Common concurrency utilities: `Thread`, `ExecutorService`, `synchronized`, `ConcurrentHashMap`, `volatile`.
- Typical Java APIs: `java.lang.Thread`, `java.util.concurrent.*` (Executors, Locks, Concurrent collections).
- When to use: Game loops, background tasks, handling multiple client connections, or any parallel work.

Project usage: YES — the server uses a dedicated game loop thread and concurrent data structures.

Evidence and examples:

- `GameRoom` starts the game loop on a dedicated thread:

  - File: `server/src/main/java/com/minitankfire/GameRoom.java`
  - Method: `startGameLoop()` creates `new Thread(() -> { ... }).start();` and uses `Thread.sleep(50)` to run the loop at ~20 FPS.

- Thread-safe collections are used to share state between the WebSocket thread(s) and the game loop thread:

  - `private Map<String, Player> players = new ConcurrentHashMap<>();`
  - `private Map<String, Bullet> bullets = new ConcurrentHashMap<>();`
  - `private Map<String, org.eclipse.jetty.websocket.api.Session> sessions = new ConcurrentHashMap<>();`

- The design uses these concurrent maps to avoid data races when WebSocket handlers (invoked by Jetty's threads) mutate game state while the game loop thread iterates/updates it.

Notes and potential edge cases:

- Using `ConcurrentHashMap` avoids many race conditions but be mindful of compound operations (read-modify-write) which may still need additional synchronization or atomic operations.

---

### 4) Client–Server communication (application-layer protocols)

- What it is: Communication between a client and server at the application level. For web games, common choices are HTTP, WebSocket, or WebRTC (data/voice) signaling.
- Typical Java APIs: `javax.websocket` (Java EE WebSocket), Jetty's WebSocket API (`org.eclipse.jetty.websocket.*`), or plain HTTP servlets.
- When to use: Real-time two-way comms (WebSocket) or request/response (HTTP).

Project usage: YES — this project uses WebSockets (Jetty) for real-time client-server communication.

Evidence and examples:

- `GameServer.java` sets up an embedded Jetty server and registers a `WebSocketServlet` at endpoint `/game`.

  - File: `server/src/main/java/com/minitankfire/GameServer.java`
  - Code: `context.addServlet(GameWebSocketServlet.class, "/game");` and `factory.register(GameWebSocket.class);`

- `GameWebSocket.java` is annotated with `@WebSocket` and defines handlers:

  - `@OnWebSocketConnect` — assigns a unique player id
  - `@OnWebSocketMessage` — receives JSON messages and dispatches to `GameRoom` (join, move, fire, chat, voice signaling)
  - `@OnWebSocketClose` / `@OnWebSocketError`

- Messages are JSON (Gson) and the server sends updates with `session.getRemote().sendString(message)`.

Also:

- The WebSocket handlers forward special messages used for voice-chat signaling (`voice-offer`, `voice-answer`, `voice-ice`). The actual voice media likely uses WebRTC peer connections handled in the browser; the server only relays signaling.

---

### 5) RMI (Remote Method Invocation)

- What it is: Java's built-in remote invocation mechanism allowing calling methods on remote Java objects (`java.rmi.*`).
- Typical Java APIs: `java.rmi.Remote`, `Naming.rebind`, `UnicastRemoteObject`.

Project usage: NOT used.

Evidence:

- No imports or usages of `java.rmi` appear in the server sources.

---

### 6) Multicast (UDP multicast)

- What it is: Sending UDP packets to a multicast group so multiple listeners can receive the same packet, typically via `MulticastSocket`.
- Typical Java APIs: `java.net.MulticastSocket`, `InetAddress.getByName("224.x.x.x")`.

Project usage: NOT used.

Evidence:

- No `MulticastSocket` or `DatagramSocket` usage in the server code.

---

## Quick summary mapping

- Sockets (raw): Not used directly by app code — Jetty handles socket details internally.
- NIO: Not used directly in app code (Jetty may use NIO internally).
- Multithreading/Concurrency: Used (game loop thread and `ConcurrentHashMap`).
- Client–Server (WebSocket): Used — Jetty WebSocket API (`GameWebSocket`, JSON messages, sendString).
- RMI: Not used.
- Multicast: Not used.

## Concrete places to inspect in repo

- `server/src/main/java/com/minitankfire/GameServer.java` — Jetty server setup and WebSocket servlet registration.
- `server/src/main/java/com/minitankfire/GameWebSocket.java` — WebSocket event handlers and JSON message parsing/dispatch.
- `server/src/main/java/com/minitankfire/GameRoom.java` — game state, concurrent maps, game loop thread, broadcasting via `session.getRemote().sendString()`.

## Notes and suggested next steps (optional)

- If you want to add explicit socket-level code (e.g., a UDP lobby broadcast or custom TCP server), we can add a small module using `ServerSocket`/`Socket` or `DatagramSocket` and show how it coexists with Jetty.
- If you want higher performance and to handle extremely many connections with fewer threads, consider configuring Jetty's connector to use NIO (it already supports it) or migrating heavy CPU work off the game loop into worker threads or an `ExecutorService`.

---

If you'd like, I can (pick one):

- add small inline code examples showing how to implement a raw TCP `ServerSocket` echo server in this project; or
- add instrumentation to the server (logging or comments) to make the network flow clearer; or
- create a short diagram linking client (browser) -> WebSocket -> GameRoom -> game loop.

Tell me which follow-up you'd like and I'll implement it.
