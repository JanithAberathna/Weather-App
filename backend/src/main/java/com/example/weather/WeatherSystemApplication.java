// package com.example.weather;

// import com.example.weather.core.WeatherBroadcaster;
// import com.example.weather.core.WeatherServer;
// import com.example.weather.core.WebSocketBridge;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;

// @SpringBootApplication
// public class WeatherSystemApplication {

//     private static WeatherServer tcpServer;
//     private static WebSocketBridge wsBridge;
//     private static WeatherBroadcaster broadcaster;

//     public static void main(String[] args) {
//         // 1. Start TCP server on port 5000 (existing clients)
//         tcpServer = new WeatherServer(5000);
//         Thread tcpThread = new Thread(() -> {
//             tcpServer.startServer();
//         });
//         tcpThread.setName("TCP-Server-Thread");
//         tcpThread.start();

//         // Small delay to ensure TCP server is ready
//         try {
//             Thread.sleep(500);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }

//         // 2. Start WebSocket bridge on port 5001 (browser clients)
//         wsBridge = new WebSocketBridge(5001, tcpServer);
//         Thread wsThread = new Thread(() -> {
//             wsBridge.start();
//         });
//         wsThread.setName("WebSocket-Bridge-Thread");
//         wsThread.start();

//         // Small delay to ensure WebSocket bridge is ready
//         try {
//             Thread.sleep(500);
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }

//         // 3. Start broadcaster
//         broadcaster = new WeatherBroadcaster(tcpServer, wsBridge, 60_000);
//         Thread broadcasterThread = new Thread(() -> {
//             broadcaster.start();
//         });
//         broadcasterThread.setName("WeatherBroadcaster-Thread");
//         broadcasterThread.setDaemon(true);
//         broadcasterThread.start();

//         // 4. Start Spring Boot
//         SpringApplication.run(WeatherSystemApplication.class, args);
//     }

//     /**
//      * Make TCP server available as a Spring bean
//      */
//     @Bean
//     public WeatherServer weatherServer() {
//         return tcpServer;
//     }

//     /**
//      * Make WebSocket bridge available as a Spring bean
//      */
//     @Bean
//     public WebSocketBridge webSocketBridge() {
//         return wsBridge;
//     }

//     /**
//      * Make broadcaster available as a Spring bean for injection into controller
//      */
//     @Bean
//     public WeatherBroadcaster weatherBroadcaster() {
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

import java.util.Set;

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
        // Option 1: No default cities - empty set
        broadcaster = new WeatherBroadcaster(tcpServer, wsBridge, 60_000, Set.of());
        
        // Option 2: If you want specific default cities, uncomment this instead:
        // broadcaster = new WeatherBroadcaster(tcpServer, wsBridge, 60_000, 
        //     Set.of("Colombo", "London", "Tokyo"));
        
        // Option 3: Use the no-arg constructor (defaults to empty set):
        // broadcaster = new WeatherBroadcaster(tcpServer, wsBridge, 60_000);
        
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