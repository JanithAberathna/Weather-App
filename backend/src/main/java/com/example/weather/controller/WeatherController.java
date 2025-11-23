package com.example.weather.controller;

import com.example.weather.core.WeatherDataFetcher;
import com.example.weather.core.WeatherBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "http://localhost:3000")
public class WeatherController {

    private WeatherBroadcaster broadcaster;

    // Inject the broadcaster via constructor (preferred way in Spring)
    @Autowired(required = false)
    public WeatherController(WeatherBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    /**
     * Search current weather by city name (uses Open-Meteo geocoding + forecast).
     * Example: GET /api/weather/search?city=Colombo
     */
    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam(name = "city") String city) {
        try {
            // Trim whitespace and newlines from the city parameter
            if (city != null) {
                city = city.trim();
            }
            
            // Validate city is not empty after trimming
            if (city == null || city.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"error\":\"City parameter cannot be empty.\"}");
            }
            
            String weatherJson = WeatherDataFetcher.fetchWeather(city);
            
            // Check if the response contains an error
            if (weatherJson.contains("\"error\"")) {
                return ResponseEntity.status(404).body(weatherJson);
            }
            
            // Add city to broadcaster if search was successful
            if (broadcaster != null) {
                broadcaster.addCity(city);
            }
            
            return ResponseEntity.ok(weatherJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body("{\"error\":\"Failed to fetch weather: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Get list of cities currently being broadcasted
     * Example: GET /api/weather/broadcast-cities
     */
    @GetMapping("/broadcast-cities")
    public ResponseEntity<?> getBroadcastCities() {
        if (broadcaster != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("cities", broadcaster.getBroadcastCities());
            response.put("defaultCities", broadcaster.getDefaultCities());
            response.put("count", broadcaster.getBroadcastCities().size());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(Map.of("cities", Set.of(), "defaultCities", Set.of(), "count", 0));
    }
    
    /**
     * Remove a city from broadcast list
     * Example: DELETE /api/weather/broadcast-cities?city=Paris
     */
    @DeleteMapping("/broadcast-cities")
    public ResponseEntity<Map<String, Object>> removeBroadcastCity(@RequestParam(name = "city") String city) {
        Map<String, Object> response = new HashMap<>();
        
        if (broadcaster == null) {
            response.put("success", false);
            response.put("message", "Broadcaster not available");
            return ResponseEntity.status(503).body(response);
        }
        
        if (city == null || city.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "City parameter is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        String trimmedCity = city.trim();
        
        // Check if it's a default city
        if (broadcaster.getDefaultCities().contains(trimmedCity)) {
            response.put("success", false);
            response.put("message", "Cannot remove default city: " + trimmedCity);
            response.put("city", trimmedCity);
            response.put("isDefault", true);
            return ResponseEntity.status(403).body(response);
        }
        
        boolean removed = broadcaster.removeCity(trimmedCity);
        
        if (removed) {
            response.put("success", true);
            response.put("message", "City removed from broadcast list");
            response.put("city", trimmedCity);
            response.put("remainingCities", broadcaster.getBroadcastCities());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "City not found in broadcast list");
            response.put("city", trimmedCity);
            return ResponseEntity.status(404).body(response);
        }
    }
    
    /**
     * Clear all non-default cities from broadcast list
     * Example: DELETE /api/weather/broadcast-cities/clear
     */
    @DeleteMapping("/broadcast-cities/clear")
    public ResponseEntity<Map<String, Object>> clearNonDefaultCities() {
        Map<String, Object> response = new HashMap<>();
        
        if (broadcaster == null) {
            response.put("success", false);
            response.put("message", "Broadcaster not available");
            return ResponseEntity.status(503).body(response);
        }
        
        int removedCount = broadcaster.clearNonDefaultCities();
        
        response.put("success", true);
        response.put("message", "Cleared non-default cities");
        response.put("removedCount", removedCount);
        response.put("remainingCities", broadcaster.getBroadcastCities());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if a city is in the broadcast list
     * Example: GET /api/weather/broadcast-cities/check?city=Paris
     */
    @GetMapping("/broadcast-cities/check")
    public ResponseEntity<Map<String, Object>> checkCity(@RequestParam(name = "city") String city) {
        Map<String, Object> response = new HashMap<>();
        
        if (broadcaster == null) {
            response.put("exists", false);
            response.put("message", "Broadcaster not available");
            return ResponseEntity.status(503).body(response);
        }
        
        if (city == null || city.trim().isEmpty()) {
            response.put("exists", false);
            response.put("message", "City parameter is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        String trimmedCity = city.trim();
        boolean exists = broadcaster.hasCity(trimmedCity);
        boolean isDefault = broadcaster.getDefaultCities().contains(trimmedCity);
        
        response.put("exists", exists);
        response.put("city", trimmedCity);
        response.put("isDefault", isDefault);
        
        return ResponseEntity.ok(response);
    }
}