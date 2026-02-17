package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.config.websocket.NotificationHandler;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PanicNotification;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PanicNotificationRepository;

import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private PanicNotificationRepository panicNotificationRepository;
    @Autowired
    private NotificationHandler notificationHandler;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void savePanicNotification(PanicNotification panicNotification) {
        panicNotificationRepository.save(panicNotification);
    }
    public void sendRideFinishedNotification(RegisteredUser passenger, Ride ride) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "RIDE_FINISHED",
                    "rideId", ride.getId(),
                    "message", "Your ride has been completed!",
                    "from", ride.getStartLocation().getAddress(),
                    "to", ride.getEndLocation().getAddress(),
                    "price", ride.getTotalPrice()
            );
            String json = objectMapper.writeValueAsString(payload);
            notificationHandler.sendToUser(passenger.getEmail(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}