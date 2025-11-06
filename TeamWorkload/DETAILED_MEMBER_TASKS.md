# Detailed member tasks — frontend & backend

This file lists, per team member (1–5), the backend and frontend items they should own, what in the project is already implemented, any gaps, and optional extras they can add under their feature.

Guidelines

- Each member should create a branch named `feature/<member>-<short-name>` for their work.
- Mark backend and frontend tasks done in the PR description and include a short demo/integration test or steps to exercise the feature.
- If a member's assigned feature is already present and marked completed below, they may extend it by adding one or more optional extras listed under "Optional additions".

---

## Member 1 — TCP (login, reliable messages)

Backend (server)

- Present: `TcpServer.java`, `ClientHandler.java` implement ServerSocket accept and per-client handling; object streams and HTTP handling exist.
- Gaps / To do: ensure login paths and chat broadcast are fully unit-tested; harden `ClientHandler` input validation and exception handling.

Frontend (client)

- Present: `client/index.html` and `client/js/game.js` exist but TCP login/chat UI wiring is likely missing.
- Tasks: implement login screen (username), TCP-based chat panel, UI connection status indicator, and a small test script that uses `TcpClient` to exercise login + chat.

Completed? (backend): Present (partial). (frontend): Not completed by default — implement as above.

Optional additions

- Add message history persistence (store recent chat messages in server memory or file).
- Add private messages (direct send to specific player IDs via TCP).

---

## Member 2 — UDP (real-time state updates / broadcasting)

Backend (server)

- Present: `UdpServer.java`, `BroadcastService.java` and `UdpClient.java` show UDP receive/send and per-client registrations.
- Gaps / To do: integrate BroadcastService with GameLoop to send delta updates each tick (if not already wired), add sequence IDs and occasional full-state snapshots, and ensure non-blocking hot path.

Frontend (client)

- Present: renderer files exist, but subscription to UDP updates and the Resync UI are not implemented by default.
- Tasks: implement UDP state receiver (or native demo) that applies delta updates; network-quality indicator (sequence id, packets missed) and a "Resync" button that requests full snapshot over TCP.

Completed? (backend): Present (simplified). (frontend): Not completed by default.

Optional additions

- Implement a basic FEC or retransmit request protocol for important state changes.
- Add a multicast fallback (see extras) for LAN play.

---

## Member 3 — Java NIO (scalable non-blocking server)

Backend (server)

- Present: `NioServer.java` demonstrates ServerSocketChannel, Selector and non-blocking read/write; currently echoes messages.
- Gaps / To do: parse the project message protocol (JSON or length-prefixed), route messages into `GameStateManager`, and add tests that simulate non-blocking clients.

Frontend (client / demo)

- Present: `NioClient.java` exists for a native Java client demo.
- Tasks: provide a simple native Java demo (or documentation) showing how to run the NIO client; add a settings toggle in the browser UI to document NIO mode (optional).

Completed? (backend): Present (partial echo). (frontend): Demo client present but not integrated with game logic.

Optional additions

- Add connection multiplexing and backpressure handling in the NIO path.
- Provide performance metrics (connections served, bytes/sec) exposed to the diagnostics UI.

---

## Member 4 — WebSocket & browser integration (move/fire/chat)

Backend (server)

- Present: Manual WebSocket handshake and frame parsing/sending implemented in `ClientHandler.java`.
- Gaps / To do: harden frame parsing (extended lengths, masking, fragmented frames), or optionally swap to a standard WebSocket library (Jetty / javax.websocket / Netty) for production robustness.

Frontend (client)

- Present: `client/js/game.js` (browser game UI) — verify and implement the WebSocket connect/send/receive paths.
- Tasks: implement UI controls for move/fire/chat; ensure incoming `update` messages are mapped into the renderer; implement reconnect and graceful close handling.

Completed? (backend): Present (manual WebSocket). (frontend): Browser UI present but may need wiring and testing.

Optional additions

- Replace manual WebSocket with Jetty/Netty for more reliable and maintainable code.
- Add WebSocket compression (permessage-deflate) if bandwidth is constrained.

---

## Member 5 — Concurrency, scheduling, performance

Backend (server)

- Present: `GameLoop.java` using ScheduledExecutorService; servers use ExecutorService; `GameStateManager.java` uses concurrent collections.
- Gaps / To do: audit for race conditions, make executors bounded, improve shutdown hooks, and run stress tests.

Frontend (client)

- Present: basic renderer; no performance overlay or simulated-client runner by default.
- Tasks: add performance overlay (tick rate, RTT, active players), debug toggles (collision boxes), and a simulated-client runner (Node script or in-browser simulated bots) for load testing.

Completed? (backend): Present (partial — requires tuning). (frontend): Not completed by default.

Optional additions

- Create a CI stress job or collection of JUnit/benchmarks to measure tick time under load.

---

## Extras not yet implemented in repo

- RMI AdminService (good fit for an admin role).
- MulticastBroadcastService (useful for LAN sessions).
- TLS-secured TCP (SSLServerSocket / keystore) for secure logins.

If a member's assigned feature is already implemented and the member wants to add more, they should pick from the optional additions above and note it in their PR as "extension work".

---

## How to mark work as complete

1. Create branch `feature/<member>-<short-task>`.
2. Implement backend changes and frontend wiring as listed above.
3. Add a short README or PR description showing how to test the feature locally (expected commands and demo steps).
4. Add at least one integration test or a small manual test script that exercises the end-to-end flow.

If you'd like, I can implement one small frontend demo (chat + connection-status + resync) and wire it to the existing TCP/UDP code, and add the test steps to the README.
