package rs.ac.uns.ftn.asd.Projekatsiit2023.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RideTrackingHandler rideTrackingHandler;

    @Autowired
    public WebSocketConfig(RideTrackingHandler rideTrackingHandler) {
        this.rideTrackingHandler = rideTrackingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rideTrackingHandler, "/ws/ride-tracking")
                .setAllowedOrigins("*"); // za test, kasnije stavi frontend origin
    }
}