package com.example.game.client;

import com.example.game.util.LoggerUtil;

import java.util.Scanner;

/**
 * Handles client input from console and sends commands to server.
 */
public class ClientInputHandler implements Runnable {
    private final TcpClient tcpClient;
    private final UdpClient udpClient;
    private volatile boolean running = true;

    public ClientInputHandler(TcpClient tcpClient, UdpClient udpClient) {
        this.tcpClient = tcpClient;
        this.udpClient = udpClient;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        LoggerUtil.info("Client input handler started. Type 'quit' to exit.");

        while (running) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine();

                if ("quit".equalsIgnoreCase(input)) {
                    running = false;
                    break;
                }

                // Process input command
                processCommand(input);

            } catch (Exception e) {
                LoggerUtil.error("Error processing input", e);
            }
        }

        scanner.close();
        LoggerUtil.info("Client input handler stopped");
    }

    private void processCommand(String command) {
        String[] parts = command.split(" ");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "tcp":
                if (parts.length > 1) {
                    tcpClient.sendMessage(parts[1]);
                }
                break;
            case "udp":
                if (parts.length > 1) {
                    udpClient.sendMessage(parts[1]);
                }
                break;
            case "move":
                if (parts.length >= 3) {
                    // Simulate move command: move x y
                    String moveCmd = "MOVE " + parts[1] + " " + parts[2];
                    udpClient.sendMessage(moveCmd);
                }
                break;
            case "chat":
                if (parts.length > 1) {
                    String message = command.substring(5); // Remove "chat "
                    tcpClient.sendMessage("CHAT " + message);
                }
                break;
            default:
                System.out.println("Unknown command. Try: tcp <msg>, udp <msg>, move <x> <y>, chat <msg>, quit");
        }
    }

    public void stop() {
        running = false;
    }
}