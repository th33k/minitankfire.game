# Contribution workload — Networking features (5 members)

Project goal: Develop a web application that demonstrates Java Network Programming concepts — sockets, NIO, multithreading, client-server communication, RMI, multicast — by splitting implementation responsibilities across 5 team members.

Each member must implement at least one distinct feature that exercises a networking concept. Below is a suggested workload split based on the current project codebase and a short plan for each feature (files to edit, tasks, acceptance criteria, and estimated effort).

## Current distinct features in the codebase (available to assign)

These features are already present in the repository and are suitable for assignment:

1. TCP (blocking sockets) — `TcpServer`, `ClientHandler`, `TcpClient` (reliable client-server messages, HTTP and object streams)
2. UDP (DatagramSocket) — `UdpServer`, `UdpClient`, `BroadcastService` (fast, connectionless state updates and broadcasting)
3. Java NIO (non-blocking) — `NioServer`, `NioClient` (Selector/Channel based non-blocking I/O)
4. Manual WebSocket handling — WebSocket handshake and frames implemented in `ClientHandler` (HTTP upgrade + frame parsing)
5. Multithreading & concurrency — `ExecutorService` usage in servers, `ScheduledExecutorService` in `GameLoop`, and concurrent collections in `GameStateManager`

These provide five distinct networking concepts and therefore cover five member roles. RMI and Multicast are not currently implemented — see the "Extras / future features" section below for recommended additions.

---

## Proposed 5‑member workload

Below each member is assigned a primary networking feature to implement or improve. Each assignment includes concrete tasks, files to edit, acceptance criteria, and estimated effort (small/medium/large).

- Member 1 — TCP feature (login, reliable game control)

  - Concept: Blocking TCP sockets (ServerSocket / Socket)
  - Files to work on: `server/src/main/java/com/example/game/server/TcpServer.java`, `server/src/main/java/com/example/game/server/ClientHandler.java`, `server/src/main/java/com/example/game/client/TcpClient.java`
  - Tasks:
    1. Harden the TCP login and object-stream handlers in `ClientHandler` (validate inputs, handle malformed objects safely).
    2. Add a reliable chat feature over TCP (server-side broadcast through `GameStateManager`).
    3. Write a small integration test that connects a `TcpClient`, performs login, sends a chat message, and expects the broadcast.
  - Acceptance criteria:
    - Client can login via TCP and receive a confirmation message.
    - Chat messages sent via TCP are broadcast to all other connected TCP clients (or WebSocket clients via message broadcaster).
    - No uncaught exceptions on malformed messages.
  - Effort: medium

- Member 2 — UDP feature (real-time state updates / broadcasting)

  - Concept: DatagramSocket for fast, connectionless messaging
  - Files to work on: `server/src/main/java/com/example/game/server/UdpServer.java`, `server/src/main/java/com/example/game/server/BroadcastService.java`, `server/src/main/java/com/example/game/client/UdpClient.java`
  - Tasks:
    1. Integrate `BroadcastService` more tightly with `GameStateManager` so that game-state delta updates are serialized and sent over UDP every tick.
    2. Implement a basic reliability layer (e.g., sequence IDs + occasional full-state snapshots) to handle missed UDP packets.
    3. Add a lightweight receiver in `UdpClient` to validate sequence numbers and request a full-state refresh via TCP if resync is needed.
  - Acceptance criteria:
    - Clients receive regular UDP state updates and can detect/recover from lost packets.
    - Minimal CPU/memory overhead added; no blocking calls in the UDP hot path.
  - Effort: medium

- Member 3 — Java NIO feature (scalable non-blocking server)

  - Concept: NIO channels, selector and non-blocking sockets
  - Files to work on: `server/src/main/java/com/example/game/server/NioServer.java`, `server/src/main/java/com/example/game/client/NioClient.java`
  - Tasks:
    1. Extend `NioServer` to parse a simple game protocol (JSON or length-prefixed messages) instead of echoing.
    2. Hook `NioServer` message handling into `GameStateManager` to process moves and chat from NIO clients.
    3. Add unit tests for non-blocking read/write using `SocketChannel` in test environment.
  - Acceptance criteria:
    - NIO clients can connect and send game messages; server correctly processes them and responds.
    - Selector loop remains non-blocking and robust to client disconnects.
  - Effort: medium/large

- Member 4 — WebSocket & browser integration

  - Concept: WebSocket protocol (HTTP upgrade + frames) — currently implemented manually, can be improved
  - Files to work on: `server/src/main/java/com/example/game/server/ClientHandler.java`, `client/js/game.js`, and optionally introduce a WebSocket library
  - Tasks:
    1. Clean up the manual WebSocket implementation in `ClientHandler` (frame handling, masking, and close/ping/pong handling).
    2. Alternatively, replace the manual implementation by integrating a Java WebSocket implementation (e.g., Jetty or Javax.websocket) and adjust `ClientHandler` to use it.
    3. Ensure browser clients (the `client/` folder) can connect and receive `update` messages at ~20 TPS and send `move`/`fire` inputs.
  - Acceptance criteria:
    - Browser client connects via WebSocket and plays a short local game scenario (move and fire, receive updates).
    - No frame-parsing bugs; clean closure and ping/pong behavior.
  - Effort: medium

- Member 5 — Concurrency, scheduling, and performance
  - Concept: Multithreading, thread pools, scheduled tasks, and thread-safe data structures
  - Files to work on: `server/src/main/java/com/example/game/server/GameLoop.java`, `server/src/main/java/com/example/game/server/GameStateManager.java`, `server/src/main/java/com/example/game/server/TcpServer.java`, `server/src/main/java/com/example/game/server/UdpServer.java`
  - Tasks:
    1. Audit and improve thread-safety and concurrency in `GameStateManager` (ensure iteration/updates are safe and efficient).
    2. Tune executor pools (avoid thread explosion) and ensure clean shutdown on server stop.
    3. Add a stress test (simulated clients) to measure tick processing and CPU usage; fix any bottlenecks found.
  - Acceptance criteria:
    - No race conditions under moderate load; game loop stays within expected tick timing.
    - Executors are bounded and properly shut down.
  - Effort: medium/large

---

## If the team prefers only already-existing distinct features

Because the repository already exposes five distinct networking-related features (TCP, UDP, NIO, WebSocket, Multithreading), you can assign those to five members as shown above.

If you want to replace any assignment with missing/extra networking concepts, consider these additions (detailed plans below):

### Suggested extra features to add (if you prefer different assignments)

1. RMI Admin Service

   - Concept: Java RMI for remote management
   - What to add: `server/src/main/java/com/example/game/server/rmi/AdminService.java` (remote interface), `AdminServiceImpl.java` (implementation delegating to `GameStateManager`), and registry bootstrap in `Main.java`.
   - Use case: remote admin can list players, kick players, change server settings.

2. Multicast-based broadcasting

   - Concept: `MulticastSocket` to send a single packet to many clients
   - What to add: `MulticastBroadcastService.java` and client-side `MulticastClient` that joins group
   - Use case: lower server bandwidth for large LAN-based sessions (requires network support for multicast)

3. TLS / Secure Sockets for TCP (SSLServerSocket)

   - Concept: Secure transport for login and control messages
   - What to add: configure `SSLServerSocket` for `TcpServer` and `SSLSocket` on clients, generate keystore for tests

4. NAT traversal / UDP hole punching

   - Concept: help P2P clients connect across NATs (advanced)
   - What to add: a simple STUN-like service or use an existing STUN server integration; more advanced and optional.

5. Replace manual WebSocket with Netty or Jetty implementation
   - Concept: robust production-ready async I/O framework
   - What to add: remove or deprecate manual handshake; add Netty/Jetty server and map messages into `GameStateManager`.

---

## Notes for team leads

- Each member should create a small branch `feature/<member>-<short-feature-name>` and open a PR against the main branch.
- Require at least one automated or manual test per feature. For network features, supply a small integration script that simulates a client and validates expected behavior.
- Keep changes minimal per PR: one feature, focused tests, and small refactors only as needed.

If you'd like, I can implement one of the extra features (RMI admin or Multicast) as a demo and add tests and README snippets. Tell me which one and I'll scaffold it in a small PR-style change.

---

## Frontend responsibilities (what each member should implement in the game UI)

Each team member should also implement or update parts of the web frontend (`client/` folder) that align with their networking feature. Below are concrete, testable frontend tasks per member so both client and server work together end-to-end.

- Member 1 — TCP (login & reliable messages)

  - UI responsibilities:
    - Implement a login screen (username input, connect button) that uses a TCP endpoint to authenticate or register the player.
    - Add a chat panel (text input + message list) that sends chat via TCP and displays incoming chat messages from the message broadcaster.
    - Show connection state (connected/connecting/disconnected) and error messages on failed TCP actions.

- Member 2 — UDP (real-time state updates)

  - UI responsibilities:
    - Implement the real-time game renderer (player sprites, bullets, power-ups) that subscribes to UDP state updates.
    - Add an indicator for network quality (last-seen sequence id, packets missed counter) and a "Resync" button that requests a full state via TCP when desynced.
    - Ensure rendering runs at a stable frame rate and can apply delta updates (added/updated/removed lists) to the in-memory game state.

- Member 3 — Java NIO (alternative client path)

  - UI responsibilities:
    - Add a client option (settings modal) to choose the connection mode: WebSocket (browser), TCP, UDP, or NIO (for native Java client demo).
    - Provide a small diagnostics panel that shows NIO connection state, bytes sent/received, and a sample request/response exchange (for the NIO demo client).

- Member 4 — WebSocket & browser integration

  - UI responsibilities:
    - Ensure the main browser game UI in `client/js/game.js` connects via WebSocket and maps incoming `update` messages into the renderer.
    - Implement UI controls for `move`, `fire`, and `chat` that send JSON WebSocket frames to the server.
    - Add graceful reconnect logic and show a popup if the WebSocket is closed unexpectedly.

- Member 5 — Concurrency / performance (client-side UX & monitoring)
  - UI responsibilities:
    - Add a lightweight performance overlay showing tick rate, update latency (round-trip time), and active players count.
    - Provide a UI for toggling debug overlays (collision boxes, velocity vectors) to help testing and balancing.
    - Add a small simulated-client runner (in `client/` or as a Node script) to generate load for stress testing and visualize its effect in the overlay.

## Frontend acceptance criteria (team-wide)

- The browser client must be able to connect to the server using the WebSocket path and play a short local session: join, move, fire, see other players and bullets.
- Chat messages must be visible in the chat panel within 1 second of sending for reliable channels (TCP/WebSocket) and visible within a few hundred ms for UDP-broadcasted events when applicable.
- The UI should show connection status and a means to recover from missed UDP packets (Resync via TCP).

If you want, I can implement one small frontend example (chat + connection-status + resync button wired to the existing TCP/UDP server code) and add the client-side changes into `client/js/game.js` and a short README on how to test it locally.
