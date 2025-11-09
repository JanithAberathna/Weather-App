// package com.example.weather.controller;

// import com.example.weather.core.WeatherDataFetcher;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/weather")
// @CrossOrigin(origins = "http://localhost:3000")
// public class WeatherController {

//     /**
//      * Search current weather by city name (uses Open-Meteo geocoding + forecast).
//      * Example: GET /api/weather/search?city=Colombo
//      */
//     @GetMapping("/search")
//     public ResponseEntity<String> search(@RequestParam(name = "city") String city) {
//         try {
//             // Trim whitespace and newlines from the city parameter
//             if (city != null) {
//                 city = city.trim();
//             }
            
//             // Validate city is not empty after trimming
//             if (city == null || city.isEmpty()) {
//                 return ResponseEntity.badRequest()
//                     .body("{\"error\":\"City parameter cannot be empty.\"}");
//             }
            
//             String weatherJson = WeatherDataFetcher.fetchWeather(city);
            
//             // Check if the response contains an error
//             if (weatherJson.contains("\"error\"")) {
//                 return ResponseEntity.status(404).body(weatherJson);
//             }
            
//             return ResponseEntity.ok(weatherJson);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.status(500)
//                 .body("{\"error\":\"Failed to fetch weather: " + e.getMessage() + "\"}");
//         }
//     }
// }

package com.example.weather.controller;

import com.example.weather.core.WeatherDataFetcher;
import com.example.weather.core.WeatherBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.ok(broadcaster.getBroadcastCities());
        }
        return ResponseEntity.ok("[]");
    }
    
    /**
     * Remove a city from broadcast list
     * Example: DELETE /api/weather/broadcast-cities?city=Paris
     */
    @DeleteMapping("/broadcast-cities")
    public ResponseEntity<String> removeBroadcastCity(@RequestParam(name = "city") String city) {
        if (broadcaster != null && city != null) {
            broadcaster.removeCity(city.trim());
            return ResponseEntity.ok("{\"message\":\"City removed from broadcast list\"}");
        }
        return ResponseEntity.badRequest().body("{\"error\":\"Invalid request\"}");
    }
}