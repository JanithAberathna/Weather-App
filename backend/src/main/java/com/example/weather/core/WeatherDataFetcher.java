package com.example.weather.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherDataFetcher {

    public static String fetchWeather(String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                return "{\"error\":\"City cannot be empty.\"}";
            }

            // 1) Geocoding
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" 
                + URLEncoder.encode(city, "UTF-8") + "&count=1&language=en&format=json";
            
            System.out.println("Geocoding URL: " + geoUrl); // debug
            
            String geoJson = getResponse(geoUrl);
            System.out.println("Geo API Response: " + geoJson); // debug

            // Check if response is valid JSON
            if (geoJson == null || geoJson.trim().isEmpty()) {
                return "{\"error\":\"Empty response from geocoding service.\"}";
            }

            JSONObject geo;
            try {
                geo = new JSONObject(geoJson);
            } catch (Exception e) {
                System.err.println("Failed to parse geocoding response: " + geoJson);
                return "{\"error\":\"Invalid response from geocoding service.\"}";
            }

            // Check if 'results' field exists and is not empty
            if (!geo.has("results")) {
                System.err.println("No 'results' field in response: " + geoJson);
                return "{\"error\":\"City '" + city + "' not found. Please check the spelling and try again.\"}";
            }

            JSONArray results = geo.getJSONArray("results");
            
            if (results.length() == 0) {
                return "{\"error\":\"City '" + city + "' not found. Please check the spelling and try again.\"}";
            }

            JSONObject loc = results.getJSONObject(0);
            double lat = loc.getDouble("latitude");
            double lon = loc.getDouble("longitude");
            String name = loc.getString("name");
            String country = loc.optString("country", "Unknown");

            System.out.println("Found location: " + name + ", " + country + " (" + lat + ", " + lon + ")");

            // 2) Fetch detailed weather
            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                    "&longitude=" + lon +
                    "&current_weather=true" +
                    "&hourly=temperature_2m,relative_humidity_2m,precipitation,windspeed_10m,weathercode" +
                    "&daily=sunrise,sunset,temperature_2m_max,temperature_2m_min,precipitation_sum" +
                    "&timezone=auto";

            System.out.println("Weather URL: " + weatherUrl); // debug
            
            String weatherJson = getResponse(weatherUrl);
            JSONObject weather = new JSONObject(weatherJson);

            JSONObject current = weather.getJSONObject("current_weather");
            JSONObject daily = weather.getJSONObject("daily");

            // Build response JSON
            JSONObject result = new JSONObject();
            result.put("city", name);
            result.put("country", country);
            result.put("latitude", lat);
            result.put("longitude", lon);
            result.put("temperature", current.getDouble("temperature"));
            result.put("windspeed", current.getDouble("windspeed"));
            result.put("weathercode", current.getInt("weathercode"));
            result.put("time", current.getString("time"));

            // Daily summary
            result.put("temperature_max", daily.getJSONArray("temperature_2m_max").optDouble(0));
            result.put("temperature_min", daily.getJSONArray("temperature_2m_min").optDouble(0));
            result.put("precipitation_sum", daily.getJSONArray("precipitation_sum").optDouble(0));
            result.put("sunrise", daily.getJSONArray("sunrise").optString(0));
            result.put("sunset", daily.getJSONArray("sunset").optString(0));

            return result.toString(2);
        } catch (Exception e) {
            e.printStackTrace();
            NetworkMonitor.apiFailure(e.getMessage());
            return "{\"error\":\"Failed to fetch weather: " + e.getMessage() + "\"}";
        }
    }

    private static String getResponse(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        conn.setRequestProperty("User-Agent", "WeatherApp/1.0");

        int code = conn.getResponseCode();
        System.out.println("HTTP Response Code: " + code); // debug
        
        BufferedReader in;
        if (code >= 200 && code < 400) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();
        conn.disconnect();
        
        return sb.toString();
    }
}