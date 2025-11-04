package com.example.game.client;

import com.example.game.util.LoggerUtil;

import java.io.*;
import java.net.Socket;

/**
 * TCP client for reliable communication with the server.
 */
public class TcpClient {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private volatile boolean connected = false;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            connected = true;
            LoggerUtil.info("TCP Client connected to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            LoggerUtil.error("Failed to connect TCP client", e);
            return false;
        }
    }

    public void sendMessage(Object message) {
        if (!connected) return;
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            LoggerUtil.debug("Sent TCP message: " + message);
        } catch (IOException e) {
            LoggerUtil.error("Failed to send TCP message", e);
        }
    }

    public Object receiveMessage() {
        if (!connected) return null;
        try {
            Object message = inputStream.readObject();
            LoggerUtil.debug("Received TCP message: " + message);
            return message;
        } catch (IOException | ClassNotFoundException e) {
            LoggerUtil.error("Failed to receive TCP message", e);
            return null;
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            LoggerUtil.info("TCP Client disconnected");
        } catch (IOException e) {
            LoggerUtil.error("Error disconnecting TCP client", e);
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}