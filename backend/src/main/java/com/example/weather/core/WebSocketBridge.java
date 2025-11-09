package com.example.weather.core;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket bridge that wraps the existing TCP WeatherServer.
 * Allows browsers to connect via WebSocket while keeping the TCP server intact.
 * 
 * Location: backend/src/main/java/com/example/weather/core/WebSocketBridge.java
 */
public class WebSocketBridge extends WebSocketServer {
    private final Set<WebSocket> wsConnections = ConcurrentHashMap.newKeySet();
    private final WeatherServer tcpServer;

    public WebSocketBridge(int wsPort, WeatherServer tcpServer) {
        super(new InetSocketAddress(wsPort));
        this.tcpServer = tcpServer;
        System.out.println("[WebSocketBridge] Initializing on port " + wsPort);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wsConnections.add(conn);
        System.out.println("[WebSocketBridge] Browser client connected: " + conn.getRemoteSocketAddress());
        System.out.println("[WebSocketBridge] Total WebSocket clients: " + wsConnections.size());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        wsConnections.remove(conn);
        System.out.println("[WebSocketBridge] Browser client disconnected: " + conn.getRemoteSocketAddress());
        System.out.println("[WebSocketBridge] Total WebSocket clients: " + wsConnections.size());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[WebSocketBridge] Received from browser: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WebSocketBridge] Error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("[WebSocketBridge] Bridge started successfully");
        System.out.println("[WebSocketBridge] Browsers can connect to: ws://localhost:" + getPort());
    }

    /**
     * Broadcast to all WebSocket clients (browsers)
     */
    public void broadcastToWeb(String message) {
        int sent = 0;
        for (WebSocket conn : wsConnections) {
            if (conn.isOpen()) {
                try {
                    conn.send(message);
                    sent++;
                } catch (Exception e) {
                    System.err.println("[WebSocketBridge] Failed to send to " + 
                                      conn.getRemoteSocketAddress() + ": " + e.getMessage());
                }
            }
        }
        if (sent > 0) {
            System.out.println("[WebSocketBridge] Sent to " + sent + " WebSocket client(s)");
        }
    }

    /**
     * Broadcast to both TCP clients and WebSocket clients
     */
    public void broadcastToAll(String message) {
        // Broadcast to TCP clients
        tcpServer.broadcast(message);
        
        // Broadcast to WebSocket clients
        broadcastToWeb(message);
        
        System.out.println("[WebSocketBridge] Broadcasted to TCP: " + 
                          tcpServer.getClients().size() + 
                          ", WebSocket: " + wsConnections.size());
    }

    public int getWebSocketClientCount() {
        return wsConnections.size();
    }
    
    public void shutdown() {
        System.out.println("[WebSocketBridge] Shutting down...");
        try {
            stop(1000);
        } catch (InterruptedException e) {
            System.err.println("[WebSocketBridge] Shutdown interrupted");
        }
    }
}