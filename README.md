🌦️ Live Weather System

A real-time weather broadcasting system built with Java (Spring Boot), WebSocket, and Next.js. The system fetches weather data from Open-Meteo API and broadcasts updates to all connected clients via WebSocket.

📋 Features

- **Real-time Weather Updates**: Automatic weather broadcasts every 60 seconds
- **WebSocket Communication**: Instant updates to all connected clients
- **City Management**: Add, remove, and manage cities in the broadcast list
- **Multi-Protocol Support**: Both TCP server and WebSocket bridge for different client types
- **Activity Monitoring**: Real-time activity log tracking all system events
- **Network Monitoring**: Built-in monitoring for API calls and client connections

## 🏗️ Architecture

```
weather-system/
├── backend/                    # Java Spring Boot backend
│   └── src/main/java/com/example/weather/
│       ├── controller/
│       │   └── WeatherController.java       # REST API endpoints
│       ├── core/
│       │   ├── ClientHandler.java           # TCP client handler
│       │   ├── NetworkMonitor.java          # Network monitoring
│       │   ├── WeatherBroadcaster.java      # Broadcast manager
│       │   ├── WeatherClient.java           # TCP client
│       │   ├── WeatherDataFetcher.java      # API integration
│       │   ├── WeatherServer.java           # TCP server
│       │   └── WebSocketBridge.java         # WebSocket server
│       ├── model/
│       │   └── WeatherInfo.java             # Weather data model
│       └── WeatherSystemApplication.java    # Main application
└── frontend/                   # Next.js frontend
    └── src/app/page.tsx                     # Main UI component
```

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (for backend)
- **Maven** (for building backend)
- **Node.js 18+** (for frontend)
- **npm or yarn** (for frontend dependencies)

### Backend Setup

1. **Navigate to backend directory**:
   ```bash
   cd backend
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

   The backend will start:
   - **REST API**: `http://localhost:8080`
   - **TCP Server**: `localhost:8081`
   - **WebSocket Server**: `ws://localhost:5001`

### Frontend Setup

1. **Navigate to frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Run the development server**:
   ```bash
   npm run dev
   ```

4. **Open browser**:
   ```
   http://localhost:3000
   ```

## 📡 API Endpoints

### Weather Search
```http
GET /api/weather/search?city={cityName}
```
Searches for weather data and adds the city to broadcast list.

**Response**:
```json
{
  "city": "Colombo",
  "country": "LK",
  "temperature": 28.5,
  "temperature_max": 30.2,
  "temperature_min": 26.8,
  "windspeed": 5.3,
  "precipitation_sum": 0.0,
  "weathercode": 2,
  "sunrise": "2025-11-11T06:15:00Z",
  "sunset": "2025-11-11T18:30:00Z",
  "time": "2025-11-11T14:30:00Z"
}
```

### Get Broadcast Cities
```http
GET /api/weather/broadcast-cities
```
Returns the list of cities currently being broadcasted.

**Response**:
```json
{
  "cities": ["Paris", "New York", "Tokyo"],
  "defaultCities": [],
  "count": 3
}
```

### Remove City
```http
DELETE /api/weather/broadcast-cities?city={cityName}
```
Removes a city from the broadcast list.

**Response**:
```json
{
  "success": true,
  "message": "City removed from broadcast list",
  "city": "Paris",
  "remainingCities": ["New York", "Tokyo"]
}
```

### Clear All Cities
```http
DELETE /api/weather/broadcast-cities/clear
```
Removes all cities from the broadcast list.

**Response**:
```json
{
  "success": true,
  "message": "Cleared non-default cities",
  "removedCount": 5,
  "remainingCities": []
}
```

### Check City Existence
```http
GET /api/weather/broadcast-cities/check?city={cityName}
```
Checks if a city is in the broadcast list.

**Response**:
```json
{
  "exists": true,
  "city": "Paris",
  "isDefault": false
}
```

## 🔌 WebSocket Messages

### Weather Broadcast
```
[BROADCAST] CityName -> {"city":"Colombo","temperature":28.5,...}
```
Sent every 60 seconds for each city in the broadcast list.

### City List Update
```
[CITY_LIST_UPDATE] Paris,London,New York
```
Sent whenever the broadcast city list changes (add/remove/clear).

## 🎯 Key Components

### WeatherBroadcaster
- Manages the list of cities to broadcast
- Fetches weather data periodically (default: 60 seconds)
- Broadcasts to both TCP and WebSocket clients
- Thread-safe operations with `ConcurrentHashMap`
- Starts with an empty city list

### WebSocketBridge
- Wraps the TCP server with WebSocket support
- Allows browser clients to receive real-time updates
- Manages WebSocket connections
- Broadcasts messages to all connected web clients

### WeatherDataFetcher
- Integrates with Open-Meteo API
- Geocoding for city coordinates
- Weather forecast data retrieval
- Error handling for API failures

### NetworkMonitor
- Tracks API calls and failures
- Monitors client connections
- Console logging for debugging
- Can be extended for metrics collection

## 🛠️ Configuration

### Broadcast Interval
Change the interval (in milliseconds) in `WeatherSystemApplication.java`:
```java
WeatherBroadcaster broadcaster = new WeatherBroadcaster(
    weatherServer, 
    wsBridge, 
    30000  // 30 seconds instead of 60
);
```

### Port Configuration
Update `application.properties`:
```properties
server.port=8080           # Spring Boot REST API
weather.tcp.port=8081      # TCP Server
weather.websocket.port=5001 # WebSocket Server
```

## 🎨 Frontend Features

- **Live Weather Display**: Current weather with detailed metrics
- **Weather Cards**: Visual representation of broadcast data
- **City Manager**: Add/remove cities from broadcast list
- **Activity Log**: Real-time event tracking
- **Connection Status**: WebSocket connection indicator
- **Responsive Design**: Mobile-friendly interface
- **Auto-refresh**: WebSocket-based real-time updates

## 📊 Weather Codes

| Code | Description |
|------|-------------|
| 0 | Clear sky ☀️ |
| 1-3 | Partly cloudy 🌤️ |
| 45-48 | Foggy ☁️ |
| 51-67 | Rainy 🌧️ |
| 71-77 | Snowy 🌨️ |
| 80-82 | Rain showers ⛈️ |
| 95+ | Thunderstorm 🌩️ |

## 🔧 Troubleshooting

### WebSocket Connection Failed
- Ensure backend is running on port 5001
- Check firewall settings
- Verify `ws://localhost:5001` is accessible

### API Errors
- Check internet connectivity
- Verify Open-Meteo API is accessible
- Review Network Monitor logs in console

### City Not Found
- Ensure correct spelling
- Try with different formats (e.g., "New York" vs "New-York")
- Check API response in browser developer tools

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📝 License

This project is open source and available under the MIT License.

## 🌐 Data Source

Weather data provided by [Open-Meteo](https://open-meteo.com/) - Free Weather API.

## 📧 Contact

For questions or support, please open an issue on the repository.

---

**Built with ❤️ using Java, Spring Boot, WebSocket, and Next.js**
