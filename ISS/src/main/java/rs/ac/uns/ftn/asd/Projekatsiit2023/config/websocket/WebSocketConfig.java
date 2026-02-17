package rs.ac.uns.ftn.asd.Projekatsiit2023.config.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RideTrackingHandler rideTrackingHandler;
    private final NotificationHandler notificationHandler;

    @Autowired
    public WebSocketConfig(RideTrackingHandler rideTrackingHandler, NotificationHandler notificationHandler) {
        this.rideTrackingHandler = rideTrackingHandler;
        this.notificationHandler = notificationHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rideTrackingHandler, "/ws/ride-tracking")
                .setAllowedOrigins("*");

        registry.addHandler(notificationHandler, "/ws/notifications")
                .setAllowedOrigins("*");
    }
}