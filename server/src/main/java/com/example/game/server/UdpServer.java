package com.example.game.server;

import com.example.game.util.LoggerUtil;
import com.example.game.util.NetworkUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP server for fast real-time game updates (player movement, actions).
 * Uses DatagramSocket and DatagramPacket for connectionless communication.
 */
public class UdpServer implements Runnable {
    private final int port;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public UdpServer(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
        LoggerUtil.info("UDP Server initialized on port " + port);
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            LoggerUtil.info("UDP Server started on " + NetworkUtils.getLocalIpAddress() + ":" + port);

            byte[] buffer = new byte[1024];

            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // Handle packet in a separate thread
                    executor.submit(new UdpPacketHandler(socket, packet));
                } catch (IOException e) {
                    if (running) {
                        LoggerUtil.error("Error receiving UDP packet", e);
                    }
                }
            }
        } catch (IOException e) {
            LoggerUtil.error("Failed to start UDP server", e);
        } finally {
            executor.shutdown();
            LoggerUtil.info("UDP Server stopped");
        }
    }

    public void stop() {
        running = false;
        executor.shutdownNow();
    }

    /**
     * Handler for UDP packets.
     */
    private static class UdpPacketHandler implements Runnable {
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        public UdpPacketHandler(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                String message = new String(packet.getData(), 0, packet.getLength());

                LoggerUtil.debug("Received UDP packet from " + clientAddress + ":" + clientPort + " - " + message);

                // Process the message (this would integrate with game logic)
                // For now, just echo back
                String response = "ECHO: " + message;
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                socket.send(responsePacket);

            } catch (IOException e) {
                LoggerUtil.error("Error handling UDP packet", e);
            }
        }
    }
}