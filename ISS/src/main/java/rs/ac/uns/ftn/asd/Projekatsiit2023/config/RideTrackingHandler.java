package rs.ac.uns.ftn.asd.Projekatsiit2023.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.ride_simulation.RideSimulationService;

import java.io.IOException;

@Component
public class RideTrackingHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private RideSimulationService rideSimulationService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        int rideId = objectMapper.readTree(message.getPayload()).get("rideId").asInt();
        rideSimulationService.connectClient(rideId, session);
    }
}
