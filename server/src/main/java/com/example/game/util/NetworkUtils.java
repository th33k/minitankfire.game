package com.example.game.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Utility class for network-related operations.
 */
public class NetworkUtils {

    /**
     * Gets the local IP address of the machine.
     * @return the local IP address as a string
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LoggerUtil.error("Error getting local IP address", e);
        }
        return "127.0.0.1"; // fallback
    }

    /**
     * Checks if a port is available.
     * @param port the port to check
     * @return true if available, false otherwise
     */
    public static boolean isPortAvailable(int port) {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds an available port starting from the given port.
     * @param startPort the port to start checking from
     * @return an available port
     */
    public static int findAvailablePort(int startPort) {
        int port = startPort;
        while (!isPortAvailable(port)) {
            port++;
        }
        return port;
    }
}