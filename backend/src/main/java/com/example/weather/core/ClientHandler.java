package com.example.weather.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles a single connected client socket. Reads simple text commands (city names) and replies
 * with JSON payloads fetched by WeatherDataFetcher. Also exposes sendMessage for server broadcasts.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final WeatherServer server;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, WeatherServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            out.println("Welcome to WeatherServer. Send a city name to fetch current weather. Type 'exit' to quit.");
            String line;
            while (running && (line = in.readLine()) != null) {
                String cmd = line.trim();
                if (cmd.isEmpty()) continue;
                if ("exit".equalsIgnoreCase(cmd)) {
                    break;
                }
                // Fetch weather and reply
                String weatherJson = WeatherDataFetcher.fetchWeather(cmd);
                out.println(weatherJson);
            }
        } catch (IOException e) {
            // connection error
        } finally {
            cleanup();
        }
    }

    /**
     * Send a message (broadcast) to this client. Returns true if successful, false if the socket is closed.
     */
    public boolean sendMessage(String message) {
        if (socket.isClosed() || out.checkError()) return false;
        out.println(message);
        return !out.checkError();
    }

    public Socket getSocket() {
        return socket;
    }

    private void cleanup() {
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        server.removeClient(this);
    }
}
