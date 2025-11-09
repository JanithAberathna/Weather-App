// package com.example.weather.core;

// import java.util.List;

// /**
//  * Periodically fetches weather for a list of cities and broadcasts to all connected clients.
//  * Pure Java fetch + server.broadcast.
//  */
// public class WeatherBroadcaster {
//     private final WeatherServer server;
//     private final List<String> cities;
//     private final long intervalMs;
//     private volatile boolean running = true;

//     /**
//      * @param server WeatherServer instance
//      * @param cities list of cities to fetch & broadcast
//      * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
//      */
//     public WeatherBroadcaster(WeatherServer server, List<String> cities, long intervalMs) {
//         this.server = server;
//         this.cities = cities;
//         this.intervalMs = intervalMs;
//     }

//     public void start() {
//         System.out.println("[WeatherBroadcaster] Started. Broadcasting every " + intervalMs + " ms for: " + cities);
//         while (running) {
//             try {
//                 for (String city : cities) {
//                     String weather = WeatherDataFetcher.fetchWeather(city);
//                     String payload = "[BROADCAST] " + city + " -> " + weather;
//                     server.broadcast(payload);
//                     System.out.println("[WeatherBroadcaster] Broadcasted for " + city);
//                 }
//                 Thread.sleep(intervalMs);
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//                 running = false;
//             } catch (Exception e) {
//                 NetworkMonitor.apiFailure("Broadcaster error: " + e.getMessage());
//             }
//         }
//     }

//     public void stop() {
//         running = false;
//     }
// }


// package com.example.weather.core;

// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;

// /**
//  * Dynamically broadcasts weather for cities that users have searched.
//  * Thread-safe using ConcurrentHashMap.
//  */
// public class WeatherBroadcaster {
//     private final WeatherServer server;
//     private final long intervalMs;
//     private volatile boolean running = true;
    
//     // Thread-safe set of cities to broadcast
//     private final Set<String> citiesToBroadcast = ConcurrentHashMap.newKeySet();
    
//     // Default cities to always broadcast
//     private final Set<String> defaultCities = Set.of("Colombo", "London", "Tokyo");

//     /**
//      * @param server WeatherServer instance
//      * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
//      */
//     public WeatherBroadcaster(WeatherServer server, long intervalMs) {
//         this.server = server;
//         this.intervalMs = intervalMs;
//         // Start with default cities
//         citiesToBroadcast.addAll(defaultCities);
//     }

//     /**
//      * Add a city to the broadcast list (called when user searches for a city)
//      * Thread-safe method
//      */
//     public void addCity(String city) {
//         if (city != null && !city.trim().isEmpty()) {
//             citiesToBroadcast.add(city.trim());
//             System.out.println("[WeatherBroadcaster] Added city to broadcast list: " + city);
//         }
//     }
    
//     /**
//      * Remove a city from broadcast list
//      */
//     public void removeCity(String city) {
//         citiesToBroadcast.remove(city);
//         System.out.println("[WeatherBroadcaster] Removed city from broadcast list: " + city);
//     }
    
//     /**
//      * Get current list of cities being broadcasted
//      */
//     public Set<String> getBroadcastCities() {
//         return Set.copyOf(citiesToBroadcast); // Return immutable copy
//     }

//     public void start() {
//         System.out.println("[WeatherBroadcaster] Started. Broadcasting every " + intervalMs + " ms");
//         System.out.println("[WeatherBroadcaster] Initial cities: " + citiesToBroadcast);
        
//         while (running) {
//             try {
//                 // Create a snapshot of cities to avoid concurrent modification
//                 Set<String> currentCities = Set.copyOf(citiesToBroadcast);
                
//                 System.out.println("[WeatherBroadcaster] Broadcasting for " + currentCities.size() + " cities: " + currentCities);
                
//                 for (String city : currentCities) {
//                     try {
//                         String weather = WeatherDataFetcher.fetchWeather(city);
//                         String payload = "[BROADCAST] " + city + " -> " + weather;
//                         server.broadcast(payload);
//                         System.out.println("[WeatherBroadcaster] Broadcasted for " + city);
//                     } catch (Exception e) {
//                         System.err.println("[WeatherBroadcaster] Failed to broadcast " + city + ": " + e.getMessage());
//                         NetworkMonitor.apiFailure("Broadcaster error for " + city + ": " + e.getMessage());
//                     }
//                 }
                
//                 Thread.sleep(intervalMs);
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//                 running = false;
//             } catch (Exception e) {
//                 NetworkMonitor.apiFailure("Broadcaster error: " + e.getMessage());
//             }
//         }
        
//         System.out.println("[WeatherBroadcaster] Stopped.");
//     }

//     public void stop() {
//         running = false;
//     }
// }

package com.example.weather.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Periodically fetches weather for a list of cities and broadcasts to all connected clients.
 * Works with both TCP server and WebSocket bridge.
 */
public class WeatherBroadcaster {
    private final WeatherServer tcpServer;
    private final WebSocketBridge wsBridge;
    private final long intervalMs;
    private volatile boolean running = true;
    
    // Thread-safe set of cities to broadcast
    private final Set<String> citiesToBroadcast = ConcurrentHashMap.newKeySet();
    
    // Default cities to always broadcast
    private final Set<String> defaultCities = Set.of("Colombo", "London", "Tokyo");

    /**
     * @param tcpServer TCP WeatherServer instance
     * @param wsBridge WebSocket bridge instance
     * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
     */
    public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs) {
        this.tcpServer = tcpServer;
        this.wsBridge = wsBridge;
        this.intervalMs = intervalMs;
        // Start with default cities
        citiesToBroadcast.addAll(defaultCities);
    }

    /**
     * Add a city to the broadcast list (called when user searches for a city)
     * Thread-safe method
     */
    public void addCity(String city) {
        if (city != null && !city.trim().isEmpty()) {
            citiesToBroadcast.add(city.trim());
            System.out.println("[WeatherBroadcaster] Added city to broadcast list: " + city);
        }
    }
    
    /**
     * Remove a city from broadcast list
     */
    public void removeCity(String city) {
        citiesToBroadcast.remove(city);
        System.out.println("[WeatherBroadcaster] Removed city from broadcast list: " + city);
    }
    
    /**
     * Get current list of cities being broadcasted
     */
    public Set<String> getBroadcastCities() {
        return Set.copyOf(citiesToBroadcast); // Return immutable copy
    }

    public void start() {
        System.out.println("[WeatherBroadcaster] Started. Broadcasting every " + intervalMs + " ms");
        System.out.println("[WeatherBroadcaster] Initial cities: " + citiesToBroadcast);
        
        while (running) {
            try {
                // Create a snapshot of cities to avoid concurrent modification
                Set<String> currentCities = Set.copyOf(citiesToBroadcast);
                
                System.out.println("[WeatherBroadcaster] Broadcasting for " + currentCities.size() + 
                                 " cities: " + currentCities);
                
                for (String city : currentCities) {
                    try {
                        String weather = WeatherDataFetcher.fetchWeather(city);
                        String payload = "[BROADCAST] " + city + " -> " + weather;
                        
                        // Broadcast to TCP clients
                        tcpServer.broadcast(payload);
                        
                        // Broadcast to WebSocket clients (browsers)
                        wsBridge.broadcastToWeb(payload);
                        
                        System.out.println("[WeatherBroadcaster] Broadcasted for " + city + 
                                         " to " + tcpServer.getClients().size() + " TCP + " + 
                                         wsBridge.getWebSocketClientCount() + " WebSocket clients");
                    } catch (Exception e) {
                        System.err.println("[WeatherBroadcaster] Failed to broadcast " + city + 
                                         ": " + e.getMessage());
                        NetworkMonitor.apiFailure("Broadcaster error for " + city + ": " + e.getMessage());
                    }
                }
                
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                NetworkMonitor.apiFailure("Broadcaster error: " + e.getMessage());
            }
        }
        
        System.out.println("[WeatherBroadcaster] Stopped.");
    }

    public void stop() {
        running = false;
    }
}