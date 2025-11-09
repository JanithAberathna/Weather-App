package com.example.weather.model;

public class WeatherInfo {
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private double temperature;
    private double windspeed;
    private String time;

    // getters & setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public double getWindspeed() { return windspeed; }
    public void setWindspeed(double windspeed) { this.windspeed = windspeed; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
