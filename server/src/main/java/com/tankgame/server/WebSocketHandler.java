package com.tankgame.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket protocol handler for browser clients
 * Wraps raw socket communication with WebSocket framing
 */
public class WebSocketHandler {
    private static final String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    
    /**
     * Perform WebSocket handshake
     * @return true if handshake successful
     */
    public static boolean performHandshake(BufferedReader reader, PrintWriter writer) throws IOException {
        String line;
        String webSocketKey = null;
        
        // Read HTTP headers
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Sec-WebSocket-Key:")) {
                webSocketKey = line.substring("Sec-WebSocket-Key:".length()).trim();
            }
        }
        
        if (webSocketKey == null) {
            return false;
        }
        
        // Generate accept key
        String acceptKey = generateAcceptKey(webSocketKey);
        
        // Send handshake response
        writer.print("HTTP/1.1 101 Switching Protocols\r\n");
        writer.print("Upgrade: websocket\r\n");
        writer.print("Connection: Upgrade\r\n");
        writer.print("Sec-WebSocket-Accept: " + acceptKey + "\r\n");
        writer.print("\r\n");
        writer.flush();
        
        return true;
    }
    
    /**
     * Generate WebSocket accept key
     */
    private static String generateAcceptKey(String webSocketKey) {
        try {
            String combined = webSocketKey + MAGIC_STRING;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate WebSocket accept key", e);
        }
    }
    
    /**
     * Read a WebSocket frame and decode message
     */
    public static String readFrame(InputStream in) throws IOException {
        // Read first byte (FIN + opcode)
        int firstByte = in.read();
        if (firstByte == -1) return null;
        
        boolean fin = (firstByte & 0x80) != 0;
        int opcode = firstByte & 0x0F;
        
        // Opcode 8 = connection close
        if (opcode == 8) return null;
        
        // Opcode 1 = text frame, 2 = binary, 9 = ping, 10 = pong
        if (opcode != 1 && opcode != 2) {
            if (opcode == 9) { // Ping
                // Send pong back
                return ""; // Empty string to indicate ping (handle in caller)
            }
            return ""; // Skip control frames
        }
        
        // Read second byte (MASK + payload length)
        int secondByte = in.read();
        if (secondByte == -1) return null;
        
        boolean masked = (secondByte & 0x80) != 0;
        long payloadLength = secondByte & 0x7F;
        
        // Extended payload length
        if (payloadLength == 126) {
            payloadLength = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
        } else if (payloadLength == 127) {
            payloadLength = 0;
            for (int i = 0; i < 8; i++) {
                payloadLength = (payloadLength << 8) | (in.read() & 0xFF);
            }
        }
        
        // Read masking key (if masked)
        byte[] maskingKey = new byte[4];
        if (masked) {
            for (int i = 0; i < 4; i++) {
                maskingKey[i] = (byte) in.read();
            }
        }
        
        // Read payload
        byte[] payload = new byte[(int) payloadLength];
        int totalRead = 0;
        while (totalRead < payloadLength) {
            int read = in.read(payload, totalRead, (int) payloadLength - totalRead);
            if (read == -1) return null;
            totalRead += read;
        }
        
        // Unmask payload
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
            }
        }
        
        return new String(payload, StandardCharsets.UTF_8);
    }
    
    /**
     * Write a WebSocket text frame
     */
    public static void writeFrame(OutputStream out, String message) throws IOException {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        
        // First byte: FIN=1, RSV=0, Opcode=1 (text)
        out.write(0x81);
        
        // Second byte: MASK=0, payload length
        if (payload.length <= 125) {
            out.write(payload.length);
        } else if (payload.length <= 65535) {
            out.write(126);
            out.write((payload.length >> 8) & 0xFF);
            out.write(payload.length & 0xFF);
        } else {
            out.write(127);
            for (int i = 7; i >= 0; i--) {
                out.write((int) ((payload.length >> (8 * i)) & 0xFF));
            }
        }
        
        // Write payload
        out.write(payload);
        out.flush();
    }
    
    /**
     * Send a pong frame in response to ping
     */
    public static void writePong(OutputStream out) throws IOException {
        out.write(0x8A); // FIN=1, Opcode=10 (pong)
        out.write(0x00); // No payload
        out.flush();
    }
}
