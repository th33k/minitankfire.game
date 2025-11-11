# Java Networking Concepts in this Project

This document explains which Java networking concepts the Tank-Game project uses, where they appear in the codebase, and suggestions for adding any missing concepts.

## Summary of the concepts

- Sockets (TCP/UDP): Present
- Java NIO (non-blocking I/O): Present
- Multithreading / Concurrency: Present
- Client-Server Communication: Present (TCP, UDP, WebSocket-like frames)
- RMI (Remote Method Invocation): Not present
- Multicast: Not present (unicast UDP broadcasts used instead)

## Where to find each concept in the code

- TCP (blocking sockets)

  - Files: `server/src/main/java/com/example/game/server/TcpServer.java`, `server/src/main/java/com/example/game/server/ClientHandler.java`
  - What it shows: `ServerSocket` accepts client `Socket` connections; each client is handled by `ClientHandler` executed on an `ExecutorService`. The code also supports both HTTP and Java object-stream protocols in `ClientHandler`.

- UDP (DatagramSocket)

  - Files: `server/src/main/java/com/example/game/server/UdpServer.java`, `server/src/main/java/com/example/game/client/UdpClient.java`, `server/src/main/java/com/example/game/server/BroadcastService.java`
  - What it shows: `DatagramSocket` and `DatagramPacket` are used for connectionless communication and broadcasting. `BroadcastService` serializes messages and sends UDP packets to registered clients. `UdpServer` receives packets and dispatches them to handlers via an `ExecutorService`.

- Java NIO (non-blocking channels, selectors)

  - Files: `server/src/main/java/com/example/game/server/NioServer.java`, `server/src/main/java/com/example/game/client/NioClient.java`
  - What it shows: `ServerSocketChannel`, `SocketChannel`, `Selector`, `SelectionKey` and `ByteBuffer` are used for scalable non-blocking I/O. The server registers channels with a `Selector` and processes `OP_ACCEPT` and `OP_READ` events.

- WebSocket-like handling (manual handshake)

  - File: `server/src/main/java/com/example/game/server/ClientHandler.java`
  - What it shows: The `ClientHandler` contains manual WebSocket handshake logic (Sec-WebSocket-Accept calculation) and frame parsing/sending over an underlying `Socket`. This is a custom implementation rather than using the Java WebSocket API.

- Multithreading and concurrency
  - Files: `server/src/main/java/com/example/game/server/TcpServer.java`, `UdpServer.java`, `NioServer.java`, `GameLoop.java`, `GameStateManager.java`
  - What it shows: The project uses `ExecutorService` (cached thread pool) for handling connections and tasks, `ScheduledExecutorService` for the main `GameLoop`, and concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) in `GameStateManager` to coordinate state safely across threads.

## Concepts not present (and how to add them)

- RMI (Remote Method Invocation)

  - Current status: Not implemented anywhere in the codebase.
  - Where it could fit: Add a server-side administrative interface (for example, `AdminService`) that exposes management operations (kick player, list players, server metrics). Implement a remote interface in `server/src/main/java/com/example/game/server/rmi/` and register it with an RMI registry on startup (or use an RMI over SSL transport for production).
  - Suggested files to add:
    - `server/src/main/java/com/example/game/server/rmi/AdminService.java` (interface extends `java.rmi.Remote`)
    - `server/src/main/java/com/example/game/server/rmi/AdminServiceImpl.java` (implements `AdminService` and calls into `GameStateManager`)
    - Start/stop code in `Main.java` to create and bind the RMI object to the registry.
  - Notes & security: RMI exposes management operations; secure it with authentication and (preferably) use SSL or run it on an admin-only network.

- Multicast (MulticastSocket)
  - Current status: Not used. The project uses `BroadcastService` which sends individual UDP packets to each known client (unicast). This is fine for small-to-medium scale but not ideal if you want true multicast.
  - Where it could fit: Replace or supplement `BroadcastService` with a `MulticastBroadcastService` that uses `MulticastSocket` to send a single packet to a multicast group address. Clients would join the multicast group to receive these packets.
  - Suggested files to add/modify:
    - New class `server/src/main/java/com/example/game/server/MulticastBroadcastService.java`.
    - Client-side: extend `UdpClient` or add `MulticastClient` functionality to join a multicast group and listen for packets.
  - Tradeoffs: Multicast reduces sender bandwidth but requires network support for IP multicast and can be more complex with NATs and cloud deployments.

## Practical suggestions & where to implement each enhancement

- Implement an RMI admin interface (quick plan)

  1. Add `AdminService` interface (extends `java.rmi.Remote`) with methods like `List<String> listPlayers() throws RemoteException`, `void kickPlayer(String playerId) throws RemoteException`, `ServerStats getStats() throws RemoteException`.
  2. Implement `AdminServiceImpl` that delegates to `GameStateManager` and registers itself with `java.rmi.registry.LocateRegistry.createRegistry(adminPort)` on server startup (e.g., in `Main.java`).
  3. Add basic auth or restrict access via firewall; consider using Java RMI over SSL (SSL socket factories) for secure transport.

- Add multicast-based broadcasting (quick plan)
  1. Create `MulticastBroadcastService` that constructs a `MulticastSocket` bound to an interface and sends packets to a group address (e.g., 239.0.0.1:4000).
  2. Modify server bootstrap to choose broadcast mode via `config.properties` (existing `config.properties` already has a `nio.port`).
  3. Create a small `MulticastClient` for clients to join the multicast group and listen for game state packets — keep UDP unicast for critical control messages (login/reliable data).

## Notes on design trade-offs already in the project

- Manual WebSocket vs library: The project implements the WebSocket handshake and frame parsing manually in `ClientHandler`. This works but can be error prone — using a mature library (javax.websocket / Jetty / Netty) would simplify things and be more robust.

- UDP broadcasting vs multicast: `BroadcastService` keeps a per-client address list and sends a unicast `DatagramPacket` to each client. This affords more control (per-client retries or different ports) but consumes more outbound bandwidth than multicast. Choose multicast only if your deployment network supports it.

- Using NIO server: `NioServer` demonstrates non-blocking I/O with `Selector` which scales well with many connections. It currently does simple echoing; integrate it into the `GameStateManager` if you want an NIO-based path for real-time messages.

## Quick mapping table (concept → example file)

- Socket / ServerSocket: `NetworkUtils.isPortAvailable()`, `TcpServer.java`, `ClientHandler.java`
- DatagramSocket (UDP): `UdpServer.java`, `UdpClient.java`, `BroadcastService.java`
- NIO (Selector/Channel): `NioServer.java`, `NioClient.java`
- Threads / ExecutorService: `TcpServer.java` (executor), `UdpServer.java` (executor), `NioServer.java` (executor), `GameLoop.java` (ScheduledExecutorService)
- WebSocket (manual): `ClientHandler.java`
- Concurrent collections/thread-safety: `GameStateManager.java` (ConcurrentHashMap, CopyOnWriteArrayList)

## References and next steps

- If you'd like, I can:
  - Add a small `MulticastBroadcastService` implementation and a `MulticastClient` example.
  - Add a simple RMI `AdminService` example and wire it into `Main.java` for admin tasks.
  - Replace the manual WebSocket code with a Jetty/Javax WebSocket implementation.

Pick one and I'll implement it as a small, self-contained change (with tests where appropriate).

---

Generated by code inspection on the repository; file map accurate as of the last scan. If you want the doc expanded with code snippets or an example implementation (multicast or RMI), tell me which and I'll add it.
