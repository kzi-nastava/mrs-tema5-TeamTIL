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

    private final ChatHandler chatHandler;

    @Autowired
    public WebSocketConfig(RideTrackingHandler rideTrackingHandler, NotificationHandler notificationHandler, ChatHandler chatHandler) {
        this.rideTrackingHandler = rideTrackingHandler;
        this.notificationHandler = notificationHandler;
        this.chatHandler = chatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rideTrackingHandler, "/ws/ride-tracking")
                .setAllowedOrigins("*");

        registry.addHandler(notificationHandler, "/ws/notifications")
                .setAllowedOrigins("*");

        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*");

        registry.addHandler(chatHandler, "/ws/chat/admin")
                .setAllowedOrigins("*");
    }
}