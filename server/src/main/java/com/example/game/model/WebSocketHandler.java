package com.minitankfire;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure Java implementation of WebSocket protocol (RFC 6455)
 * Demonstrates low-level socket programming and protocol implementation
 * No external dependencies - only core Java networking APIs
 */
public class WebSocketHandler {
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private boolean connected;
    private String clientId;

    private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public WebSocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.connected = true;
        this.clientId = java.util.UUID.randomUUID().toString();
    }

    /**
     * Performs WebSocket handshake according to RFC 6455
     * Demonstrates HTTP protocol parsing and header manipulation
     */
    public boolean performHandshake() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        // Read HTTP request
        String line = reader.readLine();
        if (line == null || !line.contains("GET")) {
            return false;
        }

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonPos = line.indexOf(':');
            if (colonPos > 0) {
                String key = line.substring(0, colonPos).trim();
                String value = line.substring(colonPos + 1).trim();
                headers.put(key.toLowerCase(), value);
            }
        }

        // Verify WebSocket upgrade request
        String upgrade = headers.get("upgrade");
        String connection = headers.get("connection");
        String key = headers.get("sec-websocket-key");

        if (upgrade == null || !upgrade.equalsIgnoreCase("websocket") ||
                connection == null || !connection.toLowerCase().contains("upgrade") ||
                key == null) {
            return false;
        }

        // Generate accept key (SHA-1 hash + Base64 encoding)
        String acceptKey = generateAcceptKey(key);

        // Send handshake response
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
        writer.print("HTTP/1.1 101 Switching Protocols\r\n");
        writer.print("Upgrade: websocket\r\n");
        writer.print("Connection: Upgrade\r\n");
        writer.print("Sec-WebSocket-Accept: " + acceptKey + "\r\n");
        writer.print("\r\n");
        writer.flush();

        return true;
    }

    /**
     * Generates WebSocket accept key using SHA-1 and Base64
     */
    private String generateAcceptKey(String key) throws Exception {
        String combined = key + WEBSOCKET_GUID;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Reads a WebSocket frame and decodes the message
     * Demonstrates binary protocol parsing and bit manipulation
     */
    public String readMessage() throws IOException {
        if (!connected)
            return null;

        // Read first byte (FIN, RSV, opcode)
        int firstByte = input.read();
        if (firstByte == -1) {
            connected = false;
            return null;
        }

        // Parse frame header (FIN flag not currently used, but part of protocol)
        // boolean fin = (firstByte & 0x80) != 0;
        int opcode = firstByte & 0x0F;

        // Handle close frame
        if (opcode == 0x8) {
            connected = false;
            return null;
        }

        // Handle ping frame
        if (opcode == 0x9) {
            sendPong();
            return readMessage(); // Continue reading next frame
        }

        // Only handle text frames (0x1)
        if (opcode != 0x1) {
            return readMessage(); // Skip other frame types
        }

        // Read second byte (MASK, payload length)
        int secondByte = input.read();
        if (secondByte == -1) {
            connected = false;
            return null;
        }

        boolean masked = (secondByte & 0x80) != 0;
        long payloadLength = secondByte & 0x7F;

        // Read extended payload length if needed
        if (payloadLength == 126) {
            payloadLength = (input.read() << 8) | input.read();
        } else if (payloadLength == 127) {
            payloadLength = 0;
            for (int i = 0; i < 8; i++) {
                payloadLength = (payloadLength << 8) | input.read();
            }
        }

        // Read masking key
        byte[] maskingKey = new byte[4];
        if (masked) {
            input.read(maskingKey);
        }

        // Read payload data
        byte[] payload = new byte[(int) payloadLength];
        int totalRead = 0;
        while (totalRead < payloadLength) {
            int read = input.read(payload, totalRead, (int) payloadLength - totalRead);
            if (read == -1) {
                connected = false;
                return null;
            }
            totalRead += read;
        }

        // Unmask payload
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] ^= maskingKey[i % 4];
            }
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    /**
     * Sends a WebSocket text frame
     * Demonstrates frame construction and binary encoding
     */
    public synchronized void sendMessage(String message) throws IOException {
        if (!connected)
            return;

        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        // First byte: FIN=1, RSV=0, opcode=1 (text frame)
        output.write(0x81);

        // Second byte: MASK=0, payload length
        if (payload.length <= 125) {
            output.write(payload.length);
        } else if (payload.length <= 65535) {
            output.write(126);
            output.write((payload.length >> 8) & 0xFF);
            output.write(payload.length & 0xFF);
        } else {
            output.write(127);
            for (int i = 7; i >= 0; i--) {
                output.write((payload.length >> (i * 8)) & 0xFF);
            }
        }

        // Payload data
        output.write(payload);
        output.flush();
    }

    /**
     * Sends a pong frame in response to ping
     */
    private void sendPong() throws IOException {
        output.write(0x8A); // FIN=1, opcode=10 (pong)
        output.write(0x00); // No payload
        output.flush();
    }

    /**
     * Sends a close frame
     */
    public void close() {
        try {
            if (connected) {
                output.write(0x88); // FIN=1, opcode=8 (close)
                output.write(0x00); // No payload
                output.flush();
                connected = false;
            }
            socket.close();
        } catch (IOException e) {
            // Ignore errors during close
        }
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }

    public String getClientId() {
        return clientId;
    }

    public Socket getSocket() {
        return socket;
    }
}
