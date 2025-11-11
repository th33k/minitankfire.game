package com.example.game.server;

import com.example.game.util.LoggerUtil;
import com.example.game.util.NetworkUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO server for non-blocking I/O scalability with many clients.
 * Uses channels, buffers, and selectors.
 */
public class NioServer implements Runnable {
    private final int port;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public NioServer(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
        LoggerUtil.info("NIO Server initialized on port " + port);
    }

    @Override
    public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            LoggerUtil.info("NIO Server started on " + NetworkUtils.getLocalIpAddress() + ":" + port);

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (running) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        handleAccept(serverChannel, selector);
                    } else if (key.isReadable()) {
                        handleRead(key, buffer);
                    }
                }
            }
        } catch (IOException e) {
            LoggerUtil.error("Failed to start NIO server", e);
        } finally {
            executor.shutdown();
            LoggerUtil.info("NIO Server stopped");
        }
    }

    private void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        LoggerUtil.info("New NIO connection from " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key, ByteBuffer buffer) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            buffer.clear();
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                // Client disconnected
                clientChannel.close();
                LoggerUtil.info("NIO client disconnected");
                return;
            }

            buffer.flip();
            String message = new String(buffer.array(), 0, buffer.limit());
            LoggerUtil.debug("Received NIO message: " + message);

            // Process message (would integrate with game logic)
            String response = "NIO ECHO: " + message;
            buffer.clear();
            buffer.put(response.getBytes());
            buffer.flip();
            clientChannel.write(buffer);

        } catch (IOException e) {
            LoggerUtil.error("Error handling NIO read", e);
            try {
                clientChannel.close();
            } catch (IOException closeException) {
                LoggerUtil.error("Error closing NIO channel", closeException);
            }
        }
    }

    public void stop() {
        running = false;
        executor.shutdownNow();
    }
}