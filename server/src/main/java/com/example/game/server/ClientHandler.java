package com.example.game.server;

import com.example.game.model.Bullet;
import com.example.game.model.Player;
import com.example.game.model.PowerUp;
import com.example.game.util.LoggerUtil;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Handles individual client connections for TCP server.
 * Can handle both Java object streams and HTTP requests.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running = true;
    private final GameStateManager gameStateManager;
    private String playerId;
    
    // For delta updates
    private Map<String, Player> lastSentPlayers = new HashMap<>();
    private Map<String, Bullet> lastSentBullets = new HashMap<>();
    private Map<String, PowerUp> lastSentPowerUps = new HashMap<>();
    private long lastUpdateTime = 0;

    public ClientHandler(Socket clientSocket, GameStateManager gameStateManager) {
        this.clientSocket = clientSocket;
        this.gameStateManager = gameStateManager;
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            LoggerUtil.error("Failed to initialize streams for client", e);
        }
    }

    @Override
    public void run() {
        try {
            LoggerUtil.info("Client handler started for " + clientSocket.getInetAddress());

            // Check if this is an HTTP request or Java object stream
            try {
                clientSocket.setSoTimeout(1000); // 1 second timeout to detect protocol
            } catch (Exception e) {
                LoggerUtil.error("Error setting socket timeout", e);
                closeConnection();
                return;
            }

            try {
                String firstLine = reader.readLine();
                if (firstLine != null && (firstLine.startsWith("GET") || firstLine.startsWith("POST"))) {
                    // This is an HTTP request
                    handleHttpRequest(firstLine);
                } else {
                    // Try to handle as Java object stream
                    handleJavaConnection();
                }
            } catch (Exception e) {
                // If reading fails, try Java connection
                handleJavaConnection();
            }

        } finally {
            closeConnection();
        }
    }

    private void handleHttpRequest(String firstLine) {
        try {
            LoggerUtil.debug("Handling HTTP request: " + firstLine);

            // Read headers to check for WebSocket upgrade
            StringBuilder headers = new StringBuilder();
            String line;
            boolean isWebSocket = false;
            String key = null;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headers.append(line).append("\n");
                if (line.toLowerCase().contains("upgrade: websocket")) {
                    isWebSocket = true;
                }
                if (line.toLowerCase().startsWith("sec-websocket-key:")) {
                    key = line.substring(line.indexOf(":") + 1).trim();
                }
            }

            // Parse the request line
            String[] parts = firstLine.split(" ");
            if (parts.length >= 2) {
                String method = parts[0];
                String path = parts[1];

                if ("GET".equals(method)) {
                    if (isWebSocket && ("/".equals(path) || "/game".equals(path))) {
                        handleWebSocketHandshake(key);
                    } else {
                        serveFile(path);
                    }
                } else {
                    sendHttpResponse(405, "Method Not Allowed", "text/plain", "Method not allowed");
                }
            }

        } catch (IOException e) {
            LoggerUtil.error("Error handling HTTP request", e);
        }
    }

    private void serveFile(String path) {
        try {
            // Default to index.html for root
            if ("/".equals(path)) {
                path = "/index.html";
            }

            // Construct file path
            String filePath = "client" + path.replace("/", File.separator);
            Path file = Paths.get(filePath);

            if (Files.exists(file) && !Files.isDirectory(file)) {
                byte[] content = Files.readAllBytes(file);
                String contentType = getContentType(path);
                sendHttpResponse(200, "OK", contentType, new String(content));
            } else {
                sendHttpResponse(404, "Not Found", "text/plain", "File not found: " + path);
            }

        } catch (IOException e) {
            LoggerUtil.error("Error serving file: " + path, e);
            sendHttpResponse(500, "Internal Server Error", "text/plain", "Server error");
        }
    }

    private void handleWebSocketHandshake(String key) {
        try {
            LoggerUtil.info("WebSocket handshake initiated");

            // Compute accept key
            String acceptKey = computeWebSocketAccept(key);

            // Send handshake response
            writer.println("HTTP/1.1 101 Switching Protocols");
            writer.println("Upgrade: websocket");
            writer.println("Connection: Upgrade");
            writer.println("Sec-WebSocket-Accept: " + acceptKey);
            writer.println();
            writer.flush();

            LoggerUtil.info("WebSocket handshake completed");

            // Small delay to allow browser to process the upgrade
            Thread.sleep(100);

            // Now handle WebSocket frames
            handleWebSocketConnection();

        } catch (Exception e) {
            LoggerUtil.error("Error during WebSocket handshake", e);
        }
    }

    private String computeWebSocketAccept(String key) throws NoSuchAlgorithmException {
        String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String combined = key + magic;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(combined.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    private void handleWebSocketConnection() {
        try {
            LoggerUtil.info("WebSocket connection established");

            // Register this client with the game state manager
            this.playerId = gameStateManager.addWebSocketPlayer(clientSocket.getInetAddress().toString());

            // Register for game state updates
            gameStateManager.addUpdateListener(gsm -> sendGameUpdate());

            // Don't send initial update - wait for client to join
            // sendGameUpdate();

            // Handle WebSocket frames
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            long lastUpdateTime = System.currentTimeMillis();

            while (running && !clientSocket.isClosed()) {
                try {
                    // Read WebSocket frame with timeout (non-blocking)
                    clientSocket.setSoTimeout(10); // 10ms timeout for reading
                    WebSocketFrame frame = readWebSocketFrame(inputStream);
                    if (frame != null) {
                        handleWebSocketMessage(frame);
                    }

                    // Send periodic updates (every 50ms for better responsiveness)
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= 50) {
                        sendGameUpdate();
                        lastUpdateTime = currentTime;
                    }

                } catch (IOException e) {
                    if (running) {
                        LoggerUtil.debug("WebSocket connection closed or timeout: " + e.getMessage());
                    }
                    break;
                }
            }

            // Remove player when connection closes
            gameStateManager.removePlayer(this.playerId);

        } catch (Exception e) {
            LoggerUtil.error("Error handling WebSocket connection", e);
        }
    }

    private void sendHttpResponse(int statusCode, String statusText, String contentType, String body) {
        writer.println("HTTP/1.1 " + statusCode + " " + statusText);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + body.length());
        writer.println("Connection: close");
        writer.println();
        writer.println(body);
        writer.flush();
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }

    private void handleJavaConnection() {
        // Try to handle as Java object stream
        try {
            // Reset timeout
            clientSocket.setSoTimeout(0);

            // Try to create object streams
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            while (running && !clientSocket.isClosed()) {
                try {
                    // Read object from client
                    Object message = inputStream.readObject();
                    LoggerUtil.debug("Received object from client: " + message);

                    // Process message (would integrate with game logic)
                    // For now, just echo back
                    outputStream.writeObject(message);
                    outputStream.flush();

                } catch (ClassNotFoundException e) {
                    LoggerUtil.error("Unknown object type received", e);
                } catch (EOFException e) {
                    // Client disconnected
                    LoggerUtil.info("Client disconnected");
                    break;
                } catch (IOException e) {
                    if (running) {
                        LoggerUtil.error("Error reading from client", e);
                    }
                    break;
                }
            }

            inputStream.close();
            outputStream.close();

        } catch (IOException e) {
            LoggerUtil.debug("Not a Java object stream connection, probably HTTP");
        }
    }

    private static class WebSocketFrame {
        boolean fin;
        int opcode;
        boolean masked;
        byte[] maskingKey;
        byte[] payload;
    }

    private WebSocketFrame readWebSocketFrame(DataInputStream inputStream) throws IOException {
        try {
            // Read first byte (FIN + opcode)
            int firstByte = inputStream.readUnsignedByte();
            boolean fin = (firstByte & 0x80) != 0;
            int opcode = firstByte & 0x0F;

            // Read second byte (mask + payload length)
            int secondByte = inputStream.readUnsignedByte();
            boolean masked = (secondByte & 0x80) != 0;
            int payloadLength = secondByte & 0x7F;

            // Extended payload length
            if (payloadLength == 126) {
                payloadLength = inputStream.readUnsignedShort();
            } else if (payloadLength == 127) {
                // For simplicity, we'll handle up to 64KB frames
                payloadLength = inputStream.readInt();
            }

            // Read masking key if present
            byte[] maskingKey = null;
            if (masked) {
                maskingKey = new byte[4];
                inputStream.readFully(maskingKey);
            }

            // Read payload
            byte[] payload = new byte[payloadLength];
            inputStream.readFully(payload);

            // Unmask payload if needed
            if (masked && maskingKey != null) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= maskingKey[i % 4];
                }
            }

            WebSocketFrame frame = new WebSocketFrame();
            frame.fin = fin;
            frame.opcode = opcode;
            frame.masked = masked;
            frame.maskingKey = maskingKey;
            frame.payload = payload;

            return frame;

        } catch (EOFException e) {
            return null; // Connection closed
        } catch (Exception e) {
            LoggerUtil.debug("Error reading WebSocket frame: " + e.getMessage());
            return null; // Invalid frame, ignore
        }
    }

    private void handleWebSocketMessage(WebSocketFrame frame) {
        try {
            if (frame.opcode == 1) { // Text frame
                String message = new String(frame.payload, "UTF-8");
                LoggerUtil.debug("Received WebSocket message: " + message);

                // Parse JSON message
                handleGameMessage(message);

            } else if (frame.opcode == 8) { // Close frame
                LoggerUtil.info("WebSocket close frame received");
                closeConnection();
            } else if (frame.opcode == 9) { // Ping frame
                LoggerUtil.debug("WebSocket ping received, sending pong");
                sendWebSocketFrame(10, frame.payload); // Send pong with same payload
            } else if (frame.opcode == 10) { // Pong frame
                LoggerUtil.debug("WebSocket pong received");
            } else {
                LoggerUtil.debug("Ignoring WebSocket frame with opcode: " + frame.opcode);
            }
            // Ignore other frame types for now

        } catch (Exception e) {
            LoggerUtil.error("Error handling WebSocket message", e);
        }
    }

    private void handleGameMessage(String jsonMessage) {
        try {
            LoggerUtil.debug("Received game message: " + jsonMessage);
            // Simple JSON parsing (in production, use a proper JSON library)
            if (jsonMessage.contains("\"type\":\"join\"")) {
                // Extract name from JSON
                String name = extractJsonValue(jsonMessage, "name");
                LoggerUtil.debug("Extracted name: '" + name + "' from JSON: " + jsonMessage);
                if (name != null) {
                    gameStateManager.setPlayerName(this.playerId, name);
                    LoggerUtil.info("Player " + this.playerId + " joined as " + name);
                    // Send immediate update after joining
                    sendGameUpdate();
                }
            } else if (jsonMessage.contains("\"type\":\"move\"")) {
                // Extract coordinates
                Double x = extractJsonDouble(jsonMessage, "x");
                Double y = extractJsonDouble(jsonMessage, "y");
                Double angle = extractJsonDouble(jsonMessage, "angle");

                if (x != null && y != null && angle != null) {
                    gameStateManager.updatePlayerPosition(this.playerId, x, y, angle);
                }
            } else if (jsonMessage.contains("\"type\":\"fire\"")) {
                gameStateManager.playerFire(this.playerId);
            } else if (jsonMessage.contains("\"type\":\"chat\"")) {
                String msg = extractJsonValue(jsonMessage, "msg");
                if (msg != null) {
                    gameStateManager.addChatMessage(this.playerId, msg);
                }
            }

        } catch (Exception e) {
            LoggerUtil.error("Error parsing game message: " + jsonMessage, e);
        }
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Double extractJsonDouble(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":([0-9.]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void sendGameUpdate() {
        try {
            // Get current game state
            var gameState = gameStateManager.getGameState();
            long currentTime = System.currentTimeMillis();
            
            // Send full update every 5 seconds or if it's been more than 2 seconds since last update
            boolean sendFullUpdate = (currentTime - lastUpdateTime > 5000) || (lastUpdateTime == 0);
            
            StringBuilder json = new StringBuilder();
            json.append("{\"type\":\"update\"");
            
            // Players - delta updates
            Map<String, Player> currentPlayers = new HashMap<>(gameState.players);
            Map<String, Player> addedPlayers = new HashMap<>();
            Map<String, Player> updatedPlayers = new HashMap<>();
            Map<String, Player> removedPlayers = new HashMap<>(lastSentPlayers);
            
            // Find added and updated players
            for (var player : currentPlayers.values()) {
                Player lastPlayer = lastSentPlayers.get(player.getId());
                if (lastPlayer == null) {
                    addedPlayers.put(player.getId(), player);
                } else if (!playersEqual(player, lastPlayer)) {
                    updatedPlayers.put(player.getId(), player);
                }
                removedPlayers.remove(player.getId());
            }
            
            // Build players JSON
            if (!addedPlayers.isEmpty() || !updatedPlayers.isEmpty() || !removedPlayers.isEmpty() || sendFullUpdate) {
                json.append(",\"players\":{");
                boolean first = true;
                
                if (sendFullUpdate) {
                    json.append("\"full\":true,\"data\":[");
                    for (var player : currentPlayers.values()) {
                        if (!first) json.append(",");
                        appendPlayerJson(json, player);
                        first = false;
                    }
                    json.append("]");
                } else {
                    if (!addedPlayers.isEmpty()) {
                        json.append("\"added\":[");
                        first = true;
                        for (var player : addedPlayers.values()) {
                            if (!first) json.append(",");
                            appendPlayerJson(json, player);
                            first = false;
                        }
                        json.append("]");
                        first = false;
                    }
                    
                    if (!updatedPlayers.isEmpty()) {
                        if (!first) json.append(",");
                        json.append("\"updated\":[");
                        first = true;
                        for (var player : updatedPlayers.values()) {
                            if (!first) json.append(",");
                            appendPlayerJson(json, player);
                            first = false;
                        }
                        json.append("]");
                        first = false;
                    }
                    
                    if (!removedPlayers.isEmpty()) {
                        if (!first) json.append(",");
                        json.append("\"removed\":[");
                        first = true;
                        for (var playerId : removedPlayers.keySet()) {
                            if (!first) json.append(",");
                            json.append("\"").append(playerId).append("\"");
                            first = false;
                        }
                        json.append("]");
                    }
                }
                json.append("}");
            }
            
            // Bullets - delta updates
            Map<String, Bullet> currentBullets = new HashMap<>();
            for (var bullet : gameState.bullets) {
                currentBullets.put(bullet.getId(), bullet);
            }
            
            Map<String, Bullet> addedBullets = new HashMap<>();
            Map<String, Bullet> removedBullets = new HashMap<>(lastSentBullets);
            
            // Find added bullets
            for (var bullet : currentBullets.values()) {
                if (!lastSentBullets.containsKey(bullet.getId())) {
                    addedBullets.put(bullet.getId(), bullet);
                }
                removedBullets.remove(bullet.getId());
            }
            
            if (!addedBullets.isEmpty() || !removedBullets.isEmpty() || sendFullUpdate) {
                json.append(",\"bullets\":{");
                boolean first = true;
                
                if (sendFullUpdate) {
                    json.append("\"full\":true,\"data\":[");
                    for (var bullet : currentBullets.values()) {
                        if (!first) json.append(",");
                        appendBulletJson(json, bullet);
                        first = false;
                    }
                    json.append("]");
                } else {
                    if (!addedBullets.isEmpty()) {
                        json.append("\"added\":[");
                        first = true;
                        for (var bullet : addedBullets.values()) {
                            if (!first) json.append(",");
                            appendBulletJson(json, bullet);
                            first = false;
                        }
                        json.append("]");
                        first = false;
                    }
                    
                    if (!removedBullets.isEmpty()) {
                        if (!first) json.append(",");
                        json.append("\"removed\":[");
                        first = true;
                        for (var bulletId : removedBullets.keySet()) {
                            if (!first) json.append(",");
                            json.append("\"").append(bulletId).append("\"");
                            first = false;
                        }
                        json.append("]");
                    }
                }
                json.append("}");
            }
            
            // Power-ups (simplified - send all for now)
            json.append(",\"powerUps\":[]");
            json.append("}");
            
            // Send as WebSocket text frame
            sendWebSocketFrame(1, json.toString().getBytes("UTF-8"));
            
            // Update last sent state
            lastSentPlayers = new HashMap<>(currentPlayers);
            lastSentBullets = new HashMap<>(currentBullets);
            lastUpdateTime = currentTime;

        } catch (Exception e) {
            LoggerUtil.error("Error sending game update", e);
        }
    }
    
    private boolean playersEqual(Player p1, Player p2) {
        return Math.abs(p1.getX() - p2.getX()) < 0.1 && 
               Math.abs(p1.getY() - p2.getY()) < 0.1 && 
               Math.abs(p1.getAngle() - p2.getAngle()) < 0.1 && 
               p1.isAlive() == p2.isAlive() &&
               p1.getName().equals(p2.getName());
    }
    
    private void appendPlayerJson(StringBuilder json, Player player) {
        json.append("{\"id\":\"").append(player.getId()).append("\",")
            .append("\"name\":\"").append(player.getName()).append("\",")
            .append("\"x\":").append(Math.round(player.getX())).append(",")
            .append("\"y\":").append(Math.round(player.getY())).append(",")
            .append("\"angle\":").append(Math.round(player.getAngle())).append(",")
            .append("\"alive\":").append(player.isAlive()).append("}");
    }
    
    private void appendBulletJson(StringBuilder json, Bullet bullet) {
        json.append("{\"id\":\"").append(bullet.getId()).append("\",")
            .append("\"x\":").append(Math.round(bullet.getX())).append(",")
            .append("\"y\":").append(Math.round(bullet.getY())).append(",")
            .append("\"angle\":").append(Math.round(Math.toDegrees(Math.atan2(bullet.getDy(), bullet.getDx())))).append("}");
    }    private void sendWebSocketFrame(int opcode, byte[] payload) throws IOException {
        ByteArrayOutputStream frame = new ByteArrayOutputStream();

        // First byte: FIN (1) + opcode
        frame.write(0x80 | opcode);

        // Second byte: mask flag (0 for server) + payload length
        if (payload.length <= 125) {
            frame.write(payload.length);
        } else if (payload.length <= 65535) {
            frame.write(126);
            frame.write((payload.length >> 8) & 0xFF);
            frame.write(payload.length & 0xFF);
        } else {
            frame.write(127);
            frame.write((payload.length >> 24) & 0xFF);
            frame.write((payload.length >> 16) & 0xFF);
            frame.write((payload.length >> 8) & 0xFF);
            frame.write(payload.length & 0xFF);
        }

        // Payload
        frame.write(payload);

        // Send frame using the underlying OutputStream
        clientSocket.getOutputStream().write(frame.toByteArray());
        clientSocket.getOutputStream().flush();
    }

    public void closeConnection() {
        running = false;
        try {
            // Remove update listener
            gameStateManager.removeUpdateListener(gsm -> sendGameUpdate());
            
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (!clientSocket.isClosed()) clientSocket.close();
            LoggerUtil.info("Client connection closed");
        } catch (IOException e) {
            LoggerUtil.error("Error closing client connection", e);
        }
    }
}