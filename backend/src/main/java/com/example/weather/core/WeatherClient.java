package com.example.weather.core;

import java.io.*;
import java.net.Socket;

/**
 * Simple console client to connect to the WeatherServer (plain TCP).
 * Usage: run this as a Java application. It connects to localhost:5000 by default.
 */
public class WeatherClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            // Thread to read server messages
            Thread reader = new Thread(() -> {
                String s;
                try {
                    while ((s = serverIn.readLine()) != null) {
                        System.out.println("SERVER: " + s);
                    }
                } catch (IOException ignored) {}
            });
            reader.setDaemon(true);
            reader.start();

            System.out.println("Connected to WeatherServer at " + host + ":" + port);
            System.out.println("Type city names to request immediate weather (e.g., Colombo). Type 'exit' to quit.");

            String line;
            while ((line = userIn.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("exit")) {
                    out.println("exit");
                    break;
                }
                out.println(line.trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Client terminated.");
    }
}
