package com.minitankfire;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class GameServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("0.0.0.0");
        connector.setPort(8080);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(GameWebSocketServlet.class, "/game");

        server.start();
        server.join();
    }

    public static class GameWebSocketServlet extends WebSocketServlet {
        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.register(GameWebSocket.class);
        }
    }
}