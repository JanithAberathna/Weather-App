package com.example.weather.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * Pure Java ServerSocket that accepts clients and assigns each to a ClientHandler thread.
 */
public class WeatherServer {
    private final int port;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    public WeatherServer(int port) {
        this.port = port;
    }

    public void startServer() {
        System.out.println("[WeatherServer] Starting on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client, this);
                clients.add(handler);
                pool.execute(handler);
                NetworkMonitor.clientConnected(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
            NetworkMonitor.apiFailure("Server socket error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void broadcast(String message) {
        synchronized (clients) {
            Iterator<ClientHandler> it = clients.iterator();
            while (it.hasNext()) {
                ClientHandler ch = it.next();
                boolean ok = ch.sendMessage(message);
                if (!ok) { // remove closed clients
                    it.remove();
                }
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        NetworkMonitor.clientDisconnected(handler.getSocket());
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void shutdown() {
        running = false;
        pool.shutdownNow();
    }
}
