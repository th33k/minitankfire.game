package com.example.game.client;

import com.example.game.util.LoggerUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP client for fast real-time communication with the server.
 */
public class UdpClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private volatile boolean connected = false;

    public boolean connect(String host, int port) {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(host);
            serverPort = port;
            connected = true;
            LoggerUtil.info("UDP Client connected to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            LoggerUtil.error("Failed to connect UDP client", e);
            return false;
        }
    }

    public void sendMessage(String message) {
        if (!connected) return;
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(packet);
            LoggerUtil.debug("Sent UDP message: " + message);
        } catch (IOException e) {
            LoggerUtil.error("Failed to send UDP message", e);
        }
    }

    public String receiveMessage() {
        if (!connected) return null;
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            LoggerUtil.debug("Received UDP message: " + message);
            return message;
        } catch (IOException e) {
            LoggerUtil.error("Failed to receive UDP message", e);
            return null;
        }
    }

    public void disconnect() {
        connected = false;
        if (socket != null) {
            socket.close();
        }
        LoggerUtil.info("UDP Client disconnected");
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}