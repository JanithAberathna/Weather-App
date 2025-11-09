// package com.example.weather;

// import com.example.weather.core.WeatherServer;
// import com.example.weather.core.WeatherBroadcaster;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

// import java.util.Arrays;

// @SpringBootApplication
// public class WeatherSystemApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(WeatherSystemApplication.class, args);

//         // Start socket server on a separate thread (pure Java)
//         int socketPort = 5000;
//         WeatherServer server = new WeatherServer(socketPort);

//         Thread serverThread = new Thread(server::startServer, "WeatherServer-Thread");
//         serverThread.setDaemon(true);
//         serverThread.start();

//         // Start broadcaster that broadcasts for a fixed set of cities every 60s
//         // You can change the list below or adapt to dynamic behavior.
//         String[] citiesToBroadcast = {"Colombo", "London", "Tokyo"};
//         WeatherBroadcaster broadcaster = new WeatherBroadcaster(server, Arrays.asList(citiesToBroadcast), 60_000);
//         Thread broadcasterThread = new Thread(broadcaster::start, "WeatherBroadcaster-Thread");
//         broadcasterThread.setDaemon(true);
//         broadcasterThread.start();
//     }
// }


// package com.example.weather;

// import com.example.weather.core.WeatherBroadcaster;
// import com.example.weather.core.WeatherServer;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;

// @SpringBootApplication
// public class WeatherSystemApplication {

//     public static void main(String[] args) {
//         SpringApplication.run(WeatherSystemApplication.class, args);
//     }

//     /**
//      * If you already have WeatherServer created somewhere,
//      * just get a reference to it and make it a bean.
//      * 
//      * Option 1: If you have a static instance or singleton
//      */
//     @Bean
//     public WeatherServer weatherServer() {
//         // If your WeatherServer is already started elsewhere,
//         // you might need to get the existing instance
//         // For example: return WeatherServer.getInstance();
        
//         // OR create a new instance (but don't start it if already started)
//         return new WeatherServer(5000);
//     }

//     /**
//      * Create WeatherBroadcaster as a Spring bean
//      * This makes it injectable into the controller
//      */
//     @Bean
//     public WeatherBroadcaster weatherBroadcaster(WeatherServer weatherServer) {
//         WeatherBroadcaster broadcaster = new WeatherBroadcaster(
//             weatherServer,
//             60_000 // Broadcast every 60 seconds
//         );
        
//         // Start broadcaster in a separate thread
//         Thread broadcasterThread = new Thread(broadcaster::start);
//         broadcasterThread.setName("WeatherBroadcaster-Thread");
//         broadcasterThread.setDaemon(true); // Daemon thread will exit when main app exits
//         broadcasterThread.start();
        
//         return broadcaster;
//     }
// }


package com.example.weather;

import com.example.weather.core.WeatherBroadcaster;
import com.example.weather.core.WeatherServer;
import com.example.weather.core.WebSocketBridge;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeatherSystemApplication {

    private static WeatherServer tcpServer;
    private static WebSocketBridge wsBridge;
    private static WeatherBroadcaster broadcaster;

    public static void main(String[] args) {
        // 1. Start TCP server on port 5000 (existing clients)
        tcpServer = new WeatherServer(5000);
        Thread tcpThread = new Thread(() -> {
            tcpServer.startServer();
        });
        tcpThread.setName("TCP-Server-Thread");
        tcpThread.start();

        // Small delay to ensure TCP server is ready
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2. Start WebSocket bridge on port 5001 (browser clients)
        wsBridge = new WebSocketBridge(5001, tcpServer);
        Thread wsThread = new Thread(() -> {
            wsBridge.start();
        });
        wsThread.setName("WebSocket-Bridge-Thread");
        wsThread.start();

        // Small delay to ensure WebSocket bridge is ready
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Start broadcaster
        broadcaster = new WeatherBroadcaster(tcpServer, wsBridge, 60_000);
        Thread broadcasterThread = new Thread(() -> {
            broadcaster.start();
        });
        broadcasterThread.setName("WeatherBroadcaster-Thread");
        broadcasterThread.setDaemon(true);
        broadcasterThread.start();

        // 4. Start Spring Boot
        SpringApplication.run(WeatherSystemApplication.class, args);
    }

    /**
     * Make TCP server available as a Spring bean
     */
    @Bean
    public WeatherServer weatherServer() {
        return tcpServer;
    }

    /**
     * Make WebSocket bridge available as a Spring bean
     */
    @Bean
    public WebSocketBridge webSocketBridge() {
        return wsBridge;
    }

    /**
     * Make broadcaster available as a Spring bean for injection into controller
     */
    @Bean
    public WeatherBroadcaster weatherBroadcaster() {
        return broadcaster;
    }
}