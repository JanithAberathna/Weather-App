// package com.example.weather.core;

// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;

// /**
//  * Periodically fetches weather for a list of cities and broadcasts to all connected clients.
//  * Works with both TCP server and WebSocket bridge.
//  */
// public class WeatherBroadcaster {
//     private final WeatherServer tcpServer;
//     private final WebSocketBridge wsBridge;
//     private final long intervalMs;
//     private volatile boolean running = true;
    
//     // Thread-safe set of cities to broadcast
//     private final Set<String> citiesToBroadcast = ConcurrentHashMap.newKeySet();
    
//     // Default cities to always broadcast
//     private final Set<String> defaultCities = Set.of("Colombo", "London", "Tokyo");

//     /**
//      * @param tcpServer TCP WeatherServer instance
//      * @param wsBridge WebSocket bridge instance
//      * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
//      */
//     public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs) {
//         this.tcpServer = tcpServer;
//         this.wsBridge = wsBridge;
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
                
//                 System.out.println("[WeatherBroadcaster] Broadcasting for " + currentCities.size() + 
//                                  " cities: " + currentCities);
                
//                 for (String city : currentCities) {
//                     try {
//                         String weather = WeatherDataFetcher.fetchWeather(city);
//                         String payload = "[BROADCAST] " + city + " -> " + weather;
                        
//                         // Broadcast to TCP clients
//                         tcpServer.broadcast(payload);
                        
//                         // Broadcast to WebSocket clients (browsers)
//                         wsBridge.broadcastToWeb(payload);
                        
//                         System.out.println("[WeatherBroadcaster] Broadcasted for " + city + 
//                                          " to " + tcpServer.getClients().size() + " TCP + " + 
//                                          wsBridge.getWebSocketClientCount() + " WebSocket clients");
//                     } catch (Exception e) {
//                         System.err.println("[WeatherBroadcaster] Failed to broadcast " + city + 
//                                          ": " + e.getMessage());
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

// package com.example.weather.core;

// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;

// /**
//  * Periodically fetches weather for a list of cities and broadcasts to all connected clients.
//  * Works with both TCP server and WebSocket bridge.
//  */
// public class WeatherBroadcaster {
//     private final WeatherServer tcpServer;
//     private final WebSocketBridge wsBridge;
//     private final long intervalMs;
//     private volatile boolean running = true;
    
//     // Thread-safe set of cities to broadcast
//     private final Set<String> citiesToBroadcast = ConcurrentHashMap.newKeySet();
    
//     // Default cities to always broadcast
//     private final Set<String> defaultCities = Set.of("Colombo", "London", "Tokyo");

//     /**
//      * @param tcpServer TCP WeatherServer instance
//      * @param wsBridge WebSocket bridge instance
//      * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
//      */
//     public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs) {
//         this.tcpServer = tcpServer;
//         this.wsBridge = wsBridge;
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
//      * Thread-safe method that prevents removal of default cities
//      * @param city The city name to remove
//      * @return true if city was removed, false if it was a default city or not found
//      */
//     public boolean removeCity(String city) {
//         if (city == null || city.trim().isEmpty()) {
//             return false;
//         }
        
//         String trimmedCity = city.trim();
        
//         // Prevent removal of default cities
//         if (defaultCities.contains(trimmedCity)) {
//             System.out.println("[WeatherBroadcaster] Cannot remove default city: " + trimmedCity);
//             return false;
//         }
        
//         boolean removed = citiesToBroadcast.remove(trimmedCity);
//         if (removed) {
//             System.out.println("[WeatherBroadcaster] Removed city from broadcast list: " + trimmedCity);
//         } else {
//             System.out.println("[WeatherBroadcaster] City not found in broadcast list: " + trimmedCity);
//         }
//         return removed;
//     }
    
//     /**
//      * Remove multiple cities at once
//      * @param cities Set of city names to remove
//      * @return Number of cities successfully removed
//      */
//     public int removeCities(Set<String> cities) {
//         int removedCount = 0;
//         for (String city : cities) {
//             if (removeCity(city)) {
//                 removedCount++;
//             }
//         }
//         return removedCount;
//     }
    
//     /**
//      * Clear all non-default cities from broadcast list
//      * @return Number of cities removed
//      */
//     public int clearNonDefaultCities() {
//         Set<String> citiesToRemove = Set.copyOf(citiesToBroadcast);
//         int removedCount = 0;
        
//         for (String city : citiesToRemove) {
//             if (!defaultCities.contains(city)) {
//                 if (citiesToBroadcast.remove(city)) {
//                     removedCount++;
//                 }
//             }
//         }
        
//         System.out.println("[WeatherBroadcaster] Cleared " + removedCount + " non-default cities");
//         return removedCount;
//     }
    
//     /**
//      * Get current list of cities being broadcasted
//      */
//     public Set<String> getBroadcastCities() {
//         return Set.copyOf(citiesToBroadcast); // Return immutable copy
//     }
    
//     /**
//      * Get the default cities that cannot be removed
//      */
//     public Set<String> getDefaultCities() {
//         return defaultCities;
//     }
    
//     /**
//      * Check if a city is in the broadcast list
//      */
//     public boolean hasCity(String city) {
//         return city != null && citiesToBroadcast.contains(city.trim());
//     }

//     public void start() {
//         System.out.println("[WeatherBroadcaster] Started. Broadcasting every " + intervalMs + " ms");
//         System.out.println("[WeatherBroadcaster] Initial cities: " + citiesToBroadcast);
        
//         while (running) {
//             try {
//                 // Create a snapshot of cities to avoid concurrent modification
//                 Set<String> currentCities = Set.copyOf(citiesToBroadcast);
                
//                 System.out.println("[WeatherBroadcaster] Broadcasting for " + currentCities.size() + 
//                                  " cities: " + currentCities);
                
//                 for (String city : currentCities) {
//                     try {
//                         String weather = WeatherDataFetcher.fetchWeather(city);
//                         String payload = "[BROADCAST] " + city + " -> " + weather;
                        
//                         // Broadcast to TCP clients
//                         tcpServer.broadcast(payload);
                        
//                         // Broadcast to WebSocket clients (browsers)
//                         wsBridge.broadcastToWeb(payload);
                        
//                         System.out.println("[WeatherBroadcaster] Broadcasted for " + city + 
//                                          " to " + tcpServer.getClients().size() + " TCP + " + 
//                                          wsBridge.getWebSocketClientCount() + " WebSocket clients");
//                     } catch (Exception e) {
//                         System.err.println("[WeatherBroadcaster] Failed to broadcast " + city + 
//                                          ": " + e.getMessage());
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


// package com.example.weather.core;

// import java.util.Set;
// import java.util.concurrent.ConcurrentHashMap;

// /**
//  * Periodically fetches weather for a list of cities and broadcasts to all connected clients.
//  * Works with both TCP server and WebSocket bridge.
//  */
// public class WeatherBroadcaster {
//     private final WeatherServer tcpServer;
//     private final WebSocketBridge wsBridge;
//     private final long intervalMs;
//     private volatile boolean running = true;
    
//     // Thread-safe set of cities to broadcast
//     private final Set<String> citiesToBroadcast = ConcurrentHashMap.newKeySet();
    
//     // Default cities to always broadcast (configurable)
//     private final Set<String> defaultCities;

//     /**
//      * @param tcpServer TCP WeatherServer instance
//      * @param wsBridge WebSocket bridge instance
//      * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
//      */
//     public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs) {
//         this(tcpServer, wsBridge, intervalMs, Set.of());
//     }

//     /**
//      * @param tcpServer TCP WeatherServer instance
//      * @param wsBridge WebSocket bridge instance
//      * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
//      * @param defaultCities Set of cities that cannot be removed (can be empty)
//      */
//     public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs, Set<String> defaultCities) {
//         this.tcpServer = tcpServer;
//         this.wsBridge = wsBridge;
//         this.intervalMs = intervalMs;
//         this.defaultCities = Set.copyOf(defaultCities); // Immutable copy
//         // Start with default cities
//         citiesToBroadcast.addAll(this.defaultCities);
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
//      * Thread-safe method that prevents removal of default cities
//      * @param city The city name to remove
//      * @return true if city was removed, false if it was a default city or not found
//      */
//     public boolean removeCity(String city) {
//         if (city == null || city.trim().isEmpty()) {
//             return false;
//         }
        
//         String trimmedCity = city.trim();
        
//         // Prevent removal of default cities
//         if (defaultCities.contains(trimmedCity)) {
//             System.out.println("[WeatherBroadcaster] Cannot remove default city: " + trimmedCity);
//             return false;
//         }
        
//         boolean removed = citiesToBroadcast.remove(trimmedCity);
//         if (removed) {
//             System.out.println("[WeatherBroadcaster] Removed city from broadcast list: " + trimmedCity);
//         } else {
//             System.out.println("[WeatherBroadcaster] City not found in broadcast list: " + trimmedCity);
//         }
//         return removed;
//     }
    
//     /**
//      * Remove multiple cities at once
//      * @param cities Set of city names to remove
//      * @return Number of cities successfully removed
//      */
//     public int removeCities(Set<String> cities) {
//         int removedCount = 0;
//         for (String city : cities) {
//             if (removeCity(city)) {
//                 removedCount++;
//             }
//         }
//         return removedCount;
//     }
    
//     /**
//      * Clear all non-default cities from broadcast list
//      * @return Number of cities removed
//      */
//     public int clearNonDefaultCities() {
//         Set<String> citiesToRemove = Set.copyOf(citiesToBroadcast);
//         int removedCount = 0;
        
//         for (String city : citiesToRemove) {
//             if (!defaultCities.contains(city)) {
//                 if (citiesToBroadcast.remove(city)) {
//                     removedCount++;
//                 }
//             }
//         }
        
//         System.out.println("[WeatherBroadcaster] Cleared " + removedCount + " non-default cities");
//         return removedCount;
//     }
    
//     /**
//      * Get current list of cities being broadcasted
//      */
//     public Set<String> getBroadcastCities() {
//         return Set.copyOf(citiesToBroadcast); // Return immutable copy
//     }
    
//     /**
//      * Get the default cities that cannot be removed
//      */
//     public Set<String> getDefaultCities() {
//         return defaultCities;
//     }
    
//     /**
//      * Check if a city is in the broadcast list
//      */
//     public boolean hasCity(String city) {
//         return city != null && citiesToBroadcast.contains(city.trim());
//     }

//     public void start() {
//         System.out.println("[WeatherBroadcaster] Started. Broadcasting every " + intervalMs + " ms");
//         System.out.println("[WeatherBroadcaster] Initial cities: " + citiesToBroadcast);
        
//         while (running) {
//             try {
//                 // Create a snapshot of cities to avoid concurrent modification
//                 Set<String> currentCities = Set.copyOf(citiesToBroadcast);
                
//                 System.out.println("[WeatherBroadcaster] Broadcasting for " + currentCities.size() + 
//                                  " cities: " + currentCities);
                
//                 for (String city : currentCities) {
//                     try {
//                         String weather = WeatherDataFetcher.fetchWeather(city);
//                         String payload = "[BROADCAST] " + city + " -> " + weather;
                        
//                         // Broadcast to TCP clients
//                         tcpServer.broadcast(payload);
                        
//                         // Broadcast to WebSocket clients (browsers)
//                         wsBridge.broadcastToWeb(payload);
                        
//                         System.out.println("[WeatherBroadcaster] Broadcasted for " + city + 
//                                          " to " + tcpServer.getClients().size() + " TCP + " + 
//                                          wsBridge.getWebSocketClientCount() + " WebSocket clients");
//                     } catch (Exception e) {
//                         System.err.println("[WeatherBroadcaster] Failed to broadcast " + city + 
//                                          ": " + e.getMessage());
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
    
    // Default cities to always broadcast (configurable)
    private final Set<String> defaultCities;

    /**
     * @param tcpServer TCP WeatherServer instance
     * @param wsBridge WebSocket bridge instance
     * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
     */
    public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs) {
        this(tcpServer, wsBridge, intervalMs, Set.of());
    }

    /**
     * @param tcpServer TCP WeatherServer instance
     * @param wsBridge WebSocket bridge instance
     * @param intervalMs interval in milliseconds (e.g., 60000 for 1 minute)
     * @param defaultCities Set of cities that cannot be removed (can be empty)
     */
    public WeatherBroadcaster(WeatherServer tcpServer, WebSocketBridge wsBridge, long intervalMs, Set<String> defaultCities) {
        this.tcpServer = tcpServer;
        this.wsBridge = wsBridge;
        this.intervalMs = intervalMs;
        this.defaultCities = Set.copyOf(defaultCities); // Immutable copy
        // Start with default cities
        citiesToBroadcast.addAll(this.defaultCities);
    }

    /**
     * Broadcast city list update to all connected clients
     */
    private void broadcastCityListUpdate() {
        try {
            String message = "[CITY_LIST_UPDATE] " + String.join(",", citiesToBroadcast);
            wsBridge.broadcastToWeb(message);
            System.out.println("[WeatherBroadcaster] Broadcasted city list update: " + citiesToBroadcast.size() + " cities");
        } catch (Exception e) {
            System.err.println("[WeatherBroadcaster] Failed to broadcast city list: " + e.getMessage());
        }
    }

    /**
     * Add a city to the broadcast list (called when user searches for a city)
     * Thread-safe method
     */
    public void addCity(String city) {
        if (city != null && !city.trim().isEmpty()) {
            boolean added = citiesToBroadcast.add(city.trim());
            if (added) {
                System.out.println("[WeatherBroadcaster] Added city to broadcast list: " + city);
                broadcastCityListUpdate();
            }
        }
    }
    
    /**
     * Remove a city from broadcast list
     * Thread-safe method that prevents removal of default cities
     * @param city The city name to remove
     * @return true if city was removed, false if it was a default city or not found
     */
    public boolean removeCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        
        String trimmedCity = city.trim();
        
        // Prevent removal of default cities
        if (defaultCities.contains(trimmedCity)) {
            System.out.println("[WeatherBroadcaster] Cannot remove default city: " + trimmedCity);
            return false;
        }
        
        boolean removed = citiesToBroadcast.remove(trimmedCity);
        if (removed) {
            System.out.println("[WeatherBroadcaster] Removed city from broadcast list: " + trimmedCity);
            broadcastCityListUpdate();
        } else {
            System.out.println("[WeatherBroadcaster] City not found in broadcast list: " + trimmedCity);
        }
        return removed;
    }
    
    /**
     * Remove multiple cities at once
     * @param cities Set of city names to remove
     * @return Number of cities successfully removed
     */
    public int removeCities(Set<String> cities) {
        int removedCount = 0;
        for (String city : cities) {
            if (removeCity(city)) {
                removedCount++;
            }
        }
        return removedCount;
    }
    
    /**
     * Clear all non-default cities from broadcast list
     * @return Number of cities removed
     */
    public int clearNonDefaultCities() {
        Set<String> citiesToRemove = Set.copyOf(citiesToBroadcast);
        int removedCount = 0;
        
        for (String city : citiesToRemove) {
            if (!defaultCities.contains(city)) {
                if (citiesToBroadcast.remove(city)) {
                    removedCount++;
                }
            }
        }
        
        System.out.println("[WeatherBroadcaster] Cleared " + removedCount + " non-default cities");
        
        if (removedCount > 0) {
            broadcastCityListUpdate();
        }
        
        return removedCount;
    }
    
    /**
     * Get current list of cities being broadcasted
     */
    public Set<String> getBroadcastCities() {
        return Set.copyOf(citiesToBroadcast); // Return immutable copy
    }
    
    /**
     * Get the default cities that cannot be removed
     */
    public Set<String> getDefaultCities() {
        return defaultCities;
    }
    
    /**
     * Check if a city is in the broadcast list
     */
    public boolean hasCity(String city) {
        return city != null && citiesToBroadcast.contains(city.trim());
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