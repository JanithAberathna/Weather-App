# 🌦️ Weather Broadcasting System
### A Network Programming Demonstration Project Using Pure Java

This project demonstrates **core network programming concepts** using pure Java networking APIs (`java.net`, `java.io`) combined with modern frameworks. It implements a real-time weather broadcasting system that showcases client-server architecture, socket programming, multi-threading, and protocol bridging.

---

## 📚 Network Programming Concepts Demonstrated

### 1. **TCP Socket Programming** (`WeatherServer.java`, `ClientHandler.java`)
- **ServerSocket**: Listening for incoming TCP connections on port 5000
- **Socket**: Accepting and managing client connections
- **Multi-threaded Server**: Each client handled by a separate thread using `ExecutorService`
- **Bidirectional Communication**: Reading from `BufferedReader` and writing to `PrintWriter`
- **Connection Lifecycle**: Managing client connect, message exchange, and disconnect

**Key APIs Used:**
```java
ServerSocket serverSocket = new ServerSocket(port);
Socket client = serverSocket.accept();
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
```

### 2. **HTTP Client Programming** (`WeatherDataFetcher.java`)
- **HttpURLConnection**: Making HTTP GET requests to REST APIs
- **URL Encoding**: Properly encoding query parameters
- **Response Handling**: Reading response streams and parsing JSON
- **Error Handling**: Managing connection timeouts and HTTP error codes
- **API Integration**: Connecting to Open-Meteo geocoding and weather APIs

**Key APIs Used:**
```java
URL url = new URL(urlString);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(10_000);
BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
```

### 3. **WebSocket Server Implementation** (`WebSocketBridge.java`)
- **Protocol Upgrade**: HTTP to WebSocket protocol upgrade
- **Bidirectional Real-time Communication**: Full-duplex communication channels
- **Broadcast Pattern**: Sending messages to multiple connected clients
- **Connection State Management**: Tracking active WebSocket connections
- **Browser Compatibility**: WebSocket support for web clients

**Key APIs Used:**
```java
public class WebSocketBridge extends WebSocketServer {
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) { }
    @Override
    public void onMessage(WebSocket conn, String message) { }
    public void broadcast(String message) { }
}
```

### 4. **Multi-Threading and Concurrency**
- **Thread Pools**: `ExecutorService` with cached thread pool for scalable client handling
- **Daemon Threads**: Background broadcaster thread that doesn't prevent JVM shutdown
- **Thread Safety**: `Collections.synchronizedList()` and `ConcurrentHashMap.newKeySet()`
- **Concurrent Client Management**: Safe iteration and removal of disconnected clients

**Demonstrated in:**
- `WeatherServer.java` - Thread pool for client handlers
- `ClientHandler.java` - Runnable implementation for each client
- `WeatherBroadcaster.java` - Daemon thread for periodic broadcasts

### 5. **Protocol Bridging** (`WebSocketBridge.java`)
- **Multi-Protocol Support**: Bridging between TCP sockets and WebSockets
- **Unified Broadcasting**: Single broadcast mechanism for multiple protocol types
- **Client Heterogeneity**: Supporting both pure Java TCP clients and browser WebSocket clients

### 6. **Network Monitoring and Observability** (`NetworkMonitor.java`)
- **Connection Events**: Tracking client connections and disconnections
- **Failure Detection**: Monitoring API failures and network errors
- **Logging**: Real-time activity logging with timestamps
- **Diagnostics**: System health monitoring and debugging

### 7. **RESTful API Integration** (`WeatherController.java`)
- **Spring REST Controller**: Exposing HTTP endpoints
- **Cross-Origin Resource Sharing (CORS)**: Enabling browser access
- **Request/Response Handling**: Query parameters and JSON responses
- **Exception Handling**: Graceful error handling with proper error responses

---

## 🏗️ Architecture Overview

```
┌─────────────────┐         HTTP          ┌──────────────────┐
│   Next.js Web   │◄─────────────────────►│ Spring Boot REST │
│   Frontend      │    (Port 8080)        │   Controller     │
│  (Port 3000)    │                       │                  │
└────────┬────────┘                       └────────┬─────────┘
         │                                         │
         │ WebSocket                               │
         │ (Port 5001)                             │
         ▼                                         ▼
┌─────────────────┐                       ┌──────────────────┐
│  WebSocket      │◄──────────────────────┤  Weather Data    │
│  Bridge         │   Broadcast Thread    │  Fetcher         │
│                 │                       │  (HTTP Client)   │
└────────┬────────┘                       └──────────────────┘
         │                                         │
         │ Broadcast                               │ HTTP GET
         │                                         ▼
         ▼                                ┌──────────────────┐
┌─────────────────┐      Broadcast       │   Open-Meteo     │
│  TCP Socket     │◄─────────────────────┤   Weather API    │
│  Server         │                      └──────────────────┘
│  (Port 5000)    │
└────────┬────────┘
         │
         │ TCP Socket
         ▼
┌─────────────────┐
│  Client Handler │  (Multi-threaded, one per client)
│  (Runnable)     │
└─────────────────┘
```

### Component Interactions:

1. **TCP Server (Port 5000)**: Pure Java `ServerSocket` accepting TCP connections
2. **WebSocket Bridge (Port 5001)**: WebSocket server for browser clients
3. **Weather Broadcaster**: Periodic thread fetching and broadcasting weather updates
4. **HTTP Client**: `HttpURLConnection` for REST API calls to Open-Meteo
5. **REST Controller (Port 8080)**: Spring Boot HTTP endpoints for web frontend
6. **Network Monitor**: Observability layer tracking all network events

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (JDK with full `java.net` and `java.io` support)
- **Maven 3.6+** (for dependency management and building)
- **Node.js 18+** (for frontend development)
- **npm or yarn** (frontend package manager)

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

## 🎯 Network Programming Components Deep Dive

### 1. **WeatherServer.java** - TCP Socket Server
**Network Concepts:**
- `ServerSocket.accept()` - Blocking I/O waiting for client connections
- Thread pool pattern using `Executors.newCachedThreadPool()`
- Synchronized collections for thread-safe client list management
- Graceful shutdown handling

**Code Highlights:**
```java
ServerSocket serverSocket = new ServerSocket(port);
while (running) {
    Socket client = serverSocket.accept();  // Blocking call
    ClientHandler handler = new ClientHandler(client, this);
    clients.add(handler);
    pool.execute(handler);  // Execute in thread pool
}
```

### 2. **ClientHandler.java** - TCP Client Handler (Runnable)
**Network Concepts:**
- Implementing `Runnable` for concurrent client handling
- Buffered I/O streams for efficient network communication
- Request-response pattern over TCP
- Connection cleanup and resource management

**Code Highlights:**
```java
public class ClientHandler implements Runnable {
    private BufferedReader in = new BufferedReader(
        new InputStreamReader(socket.getInputStream()));
    private PrintWriter out = new PrintWriter(
        socket.getOutputStream(), true);
    
    public void run() {
        while ((line = in.readLine()) != null) {
            // Process client requests
        }
    }
}
```

### 3. **WeatherDataFetcher.java** - HTTP Client
**Network Concepts:**
- HTTP GET requests using `HttpURLConnection`
- URL encoding for query parameters
- Connection timeouts and error handling
- JSON response parsing

**Code Highlights:**
```java
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(10_000);
conn.setReadTimeout(10_000);
int code = conn.getResponseCode();
```

### 4. **WebSocketBridge.java** - WebSocket Server
**Network Concepts:**
- WebSocket protocol implementation (RFC 6455)
- Event-driven connection lifecycle (onOpen, onMessage, onClose, onError)
- Broadcasting to multiple clients
- Protocol upgrade from HTTP to WebSocket

**Code Highlights:**
```java
public class WebSocketBridge extends WebSocketServer {
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wsConnections.add(conn);
    }
    
    public void broadcastToWeb(String message) {
        for (WebSocket conn : wsConnections) {
            if (conn.isOpen()) conn.send(message);
        }
    }
}
```

### 5. **WeatherBroadcaster.java** - Periodic Broadcasting
**Network Concepts:**
- Daemon threads for background tasks
- Periodic task execution with `Thread.sleep()`
- Broadcasting to heterogeneous clients (TCP + WebSocket)
- Thread interruption and graceful shutdown

### 6. **NetworkMonitor.java** - Observability Layer
**Network Concepts:**
- Connection state tracking
- Network event logging
- Failure detection and diagnostics
- Timestamps and remote address tracking

---

## 🧪 Testing Network Components

### 1. Test TCP Server with Pure Java Client
Run the provided TCP client:
```bash
cd backend/src/main/java
javac com/example/weather/core/WeatherClient.java
java com.example.weather.core.WeatherClient
```

Expected output:
```
Connected to WeatherServer at localhost:5000
Type city names to request immediate weather (e.g., Colombo). Type 'exit' to quit.
> Colombo
SERVER: {"city":"Colombo","temperature":28.5,...}
```

### 2. Test WebSocket with Browser Console
Open browser console and run:
```javascript
const ws = new WebSocket('ws://localhost:5001');
ws.onopen = () => console.log('WebSocket connected');
ws.onmessage = (e) => console.log('Received:', e.data);
ws.onerror = (e) => console.error('WebSocket error:', e);
```

### 3. Test HTTP REST API with curl
```bash
# Search weather and add to broadcast list
curl "http://localhost:8080/api/weather/search?city=London"

# Get broadcast cities
curl "http://localhost:8080/api/weather/broadcast-cities"

# Remove a city
curl -X DELETE "http://localhost:8080/api/weather/broadcast-cities?city=London"
```

### 4. Monitor Network Activity
Watch the backend console for network events:
```
[WeatherServer] Starting on port 5000
[WebSocketBridge] Bridge started successfully
[MONITOR] 2025-11-23T14:30:00 - Client connected: /127.0.0.1:51234
[WeatherBroadcaster] Broadcasted for Colombo
[WebSocketBridge] Sent to 2 WebSocket client(s)
```

---

## 🛠️ Configuration

### Ports Used
| Service | Port | Protocol | Purpose |
|---------|------|----------|---------|
| Spring Boot REST | 8080 | HTTP | Web API endpoints |
| TCP Socket Server | 5000 | TCP | Pure Java client connections |
| WebSocket Server | 5001 | WebSocket | Browser client connections |

### Broadcast Interval
Change the interval in `WeatherSystemApplication.java`:
```java
WeatherBroadcaster broadcaster = new WeatherBroadcaster(
    tcpServer, 
    wsBridge, 
    30_000,  // 30 seconds instead of 60
    Set.of()  // Start with empty city list
);
```

### Thread Pool Configuration
Modify `WeatherServer.java` for fixed thread pool:
```java
// Change from cached to fixed-size thread pool
private final ExecutorService pool = Executors.newFixedThreadPool(10);
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

---

## 🎓 Learning Objectives & Educational Value

This project demonstrates:

### Core Java Networking (`java.net` package)
- ✅ ServerSocket and Socket programming
- ✅ HttpURLConnection for REST API consumption
- ✅ InetSocketAddress and remote socket address handling
- ✅ Connection lifecycle management

### I/O Streams (`java.io` package)
- ✅ BufferedReader and InputStreamReader for reading
- ✅ PrintWriter and OutputStreamWriter for writing
- ✅ Stream chaining and buffering strategies
- ✅ Resource management with try-with-resources

### Concurrency (`java.util.concurrent`)
- ✅ ExecutorService and thread pools
- ✅ Runnable interface implementation
- ✅ Thread-safe collections (synchronized, concurrent)
- ✅ Daemon threads and background tasks

### Design Patterns
- ✅ **Client-Server Architecture**: Traditional request-response model
- ✅ **Publisher-Subscriber**: Broadcasting to multiple clients
- ✅ **Observer Pattern**: Network event monitoring
- ✅ **Bridge Pattern**: Protocol bridging (TCP ↔ WebSocket)
- ✅ **Thread-per-Request**: Scalable client handling
- ✅ **Resource Pool**: Connection and thread pooling

### Protocol Implementation
- ✅ **TCP**: Reliable, connection-oriented byte stream
- ✅ **HTTP**: Request-response with headers and status codes
- ✅ **WebSocket**: Full-duplex communication over single TCP connection
- ✅ **JSON**: Data interchange format

---

## 📚 Additional Network Programming Exercises

Want to extend this project? Try:

1. **Add UDP Support**: Implement a UDP server for fire-and-forget weather updates
2. **Connection Pooling**: Reuse HttpURLConnection objects for API calls
3. **Rate Limiting**: Implement token bucket for API request throttling
4. **Load Balancing**: Distribute client connections across multiple server threads
5. **Heartbeat Mechanism**: Detect and remove stale client connections
6. **SSL/TLS Support**: Secure communication with SSLSocket
7. **Binary Protocol**: Replace JSON with a custom binary protocol (e.g., Protocol Buffers)
8. **Multicast Broadcasting**: Use MulticastSocket for efficient broadcasting
9. **Connection Metrics**: Track bandwidth, latency, and throughput
10. **Client Authentication**: Implement a simple authentication mechanism

---

## 🔍 Code Organization

```
backend/src/main/java/com/example/weather/
│
├── core/                           # Pure Java Networking Components
│   ├── WeatherServer.java          # TCP ServerSocket + thread pool
│   ├── ClientHandler.java          # Runnable for each TCP client
│   ├── WeatherClient.java          # TCP Socket client (for testing)
│   ├── WeatherDataFetcher.java     # HttpURLConnection API client
│   ├── WebSocketBridge.java        # WebSocket server implementation
│   ├── WeatherBroadcaster.java     # Periodic broadcasting thread
│   └── NetworkMonitor.java         # Network event observer
│
├── controller/                     # Spring Boot REST Layer
│   └── WeatherController.java      # HTTP endpoints (@RestController)
│
├── model/                          # Data Models
│   └── WeatherInfo.java            # Weather data POJO
│
└── WeatherSystemApplication.java   # Main entry point
```

---

## 🛠️ Technologies & Dependencies

### Pure Java (JDK 17+)
- `java.net.*` - Socket, ServerSocket, HttpURLConnection, URL
- `java.io.*` - BufferedReader, PrintWriter, InputStreamReader
- `java.util.concurrent.*` - ExecutorService, ConcurrentHashMap
- `java.time.*` - LocalDateTime for timestamps

### External Libraries (Maven)
- **Spring Boot 3.2.0** - REST API framework and dependency injection
- **org.json** - JSON parsing and generation
- **Java-WebSocket 1.5.3** - WebSocket protocol implementation
- **Lombok** (optional) - Boilerplate code reduction

### Frontend
- **Next.js 14** - React framework with TypeScript
- **Tailwind CSS** - Utility-first CSS framework

---

## 🐛 Common Issues & Solutions

### Issue: Port Already in Use
**Solution**: Find and kill the process using the port
```powershell
# Windows PowerShell
netstat -ano | Select-String ":8080"
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Issue: WebSocket Connection Refused
**Solution**: Ensure backend is running and WebSocket server started
```
Check console output for:
[WebSocketBridge] Bridge started successfully
[WebSocketBridge] Browsers can connect to: ws://localhost:5001
```

### Issue: HTTP 500 Internal Server Error
**Solution**: Check backend console for stack traces. Common causes:
- Network connectivity issues
- Open-Meteo API rate limiting
- Invalid city names
- JSON parsing errors

### Issue: No Weather Broadcasts
**Solution**: Add cities to the broadcast list
```bash
curl "http://localhost:8080/api/weather/search?city=London"
```

---

## 📖 References & Further Reading

### Java Networking
- [Oracle Java Networking Tutorial](https://docs.oracle.com/javase/tutorial/networking/)
- [Java Socket Programming](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html)
- [HttpURLConnection Guide](https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html)

### WebSocket Protocol
- [RFC 6455 - The WebSocket Protocol](https://datatracker.ietf.org/doc/html/rfc6455)
- [Java-WebSocket Library](https://github.com/TooTallNate/Java-WebSocket)

### Concurrency
- [Java Concurrency in Practice](https://jcip.net/)
- [ExecutorService Tutorial](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)

### Weather API
- [Open-Meteo API Documentation](https://open-meteo.com/en/docs)

---

## 👨‍💻 Author

This project was created as an educational demonstration of network programming concepts using pure Java networking APIs.

## 📝 License

This project is open source and available for educational purposes.

---

**🎯 Built to demonstrate network programming concepts with Java Socket API, HttpURLConnection, WebSocket, and multi-threading**
