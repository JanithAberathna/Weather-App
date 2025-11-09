package com.example.weather.core;

import java.net.Socket;
import java.time.LocalDateTime;

public class NetworkMonitor {

    public static void clientConnected(Socket socket) {
        System.out.println("[MONITOR] " + LocalDateTime.now() + " - Client connected: " + socket.getRemoteSocketAddress());
    }

    public static void clientDisconnected(Socket socket) {
        System.out.println("[MONITOR] " + LocalDateTime.now() + " - Client disconnected: " + socket.getRemoteSocketAddress());
    }

    public static void apiFailure(String reason) {
        System.err.println("[MONITOR] " + LocalDateTime.now() + " - API/Server Failure: " + reason);
    }
}
