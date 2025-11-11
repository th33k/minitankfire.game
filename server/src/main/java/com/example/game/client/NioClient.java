package com.example.game.client;

import com.example.game.util.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO client for non-blocking communication with the server.
 */
public class NioClient {
    private SocketChannel socketChannel;
    private volatile boolean connected = false;

    public boolean connect(String host, int port) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));

            // Wait for connection to complete
            while (!socketChannel.finishConnect()) {
                // Could implement timeout here
            }

            connected = true;
            LoggerUtil.info("NIO Client connected to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            LoggerUtil.error("Failed to connect NIO client", e);
            return false;
        }
    }

    public void sendMessage(String message) {
        if (!connected) return;
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);
            LoggerUtil.debug("Sent NIO message: " + message);
        } catch (IOException e) {
            LoggerUtil.error("Failed to send NIO message", e);
        }
    }

    public String receiveMessage() {
        if (!connected) return null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead == -1) {
                disconnect();
                return null;
            }

            buffer.flip();
            String message = new String(buffer.array(), 0, buffer.limit());
            LoggerUtil.debug("Received NIO message: " + message);
            return message;
        } catch (IOException e) {
            LoggerUtil.error("Failed to receive NIO message", e);
            return null;
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
            LoggerUtil.info("NIO Client disconnected");
        } catch (IOException e) {
            LoggerUtil.error("Error disconnecting NIO client", e);
        }
    }

    public boolean isConnected() {
        return connected && socketChannel != null && socketChannel.isConnected();
    }
}