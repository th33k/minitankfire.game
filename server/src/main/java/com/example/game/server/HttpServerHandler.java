package com.example.game.server;

import com.example.game.util.LoggerUtil;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple HTTP server to serve the client files and handle basic requests.
 * Uses com.sun.net.httpserver.HttpServer for serving static files.
 */
public class HttpServerHandler implements Runnable {
    private final int port;
    private HttpServer server;
    private volatile boolean running = true;

    public HttpServerHandler(int port) {
        this.port = port;
        LoggerUtil.info("HTTP Server initialized on port " + port);
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/status", new StatusHandler());
            server.setExecutor(null); // Use default executor
            server.start();

            LoggerUtil.info("HTTP Server started on port " + port);
        } catch (IOException e) {
            LoggerUtil.error("Failed to start HTTP server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            LoggerUtil.info("HTTP Server stopped");
        }
        running = false;
    }

    /**
     * Handler for serving static files from the client directory.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();

            // Default to index.html for root requests
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            // Remove leading slash and construct file path
            String filePath = "client" + requestPath.replace("/", "\\");

            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    byte[] fileBytes = Files.readAllBytes(path);

                    // Determine content type
                    String contentType = getContentType(requestPath);
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, fileBytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(fileBytes);
                    }
                } else {
                    // File not found
                    String response = "File not found: " + requestPath;
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } catch (Exception e) {
                LoggerUtil.error("Error serving file: " + requestPath, e);
                String response = "Internal server error";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }

    /**
     * Handler for API status requests.
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"running\",\"server\":\"Tank Game Server\",\"version\":\"1.0\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}