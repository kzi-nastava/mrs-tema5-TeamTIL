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

    public void sendRideCoPassengerAddedNotification(RegisteredUser coPassenger, Ride ride) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "RIDE_ACCEPTED",
                    "rideId", ride.getId(),
                    "message", "You've been added to a ride! Driver: " +
                            ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName(),
                    "driverName", ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName(),
                    "vehicle", ride.getDriver().getVehicle().getModel()
            );
            String json = objectMapper.writeValueAsString(payload);
            notificationHandler.sendToUser(coPassenger.getEmail(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRideAcceptedNotification(RegisteredUser passenger, Ride ride) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "RIDE_ACCEPTED",
                    "rideId", ride.getId(),
                    "message", "Your ride has been accepted! Driver: " +
                            ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName(),
                    "driverName", ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName(),
                    "vehicle", ride.getDriver().getVehicle().getModel()
            );
            String json = objectMapper.writeValueAsString(payload);
            notificationHandler.sendToUser(passenger.getEmail(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRideRejectedNotification(String passengerEmail) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "RIDE_REJECTED",
                    "message", "No available drivers at the moment. Please try again later."
            );
            String json = objectMapper.writeValueAsString(payload);
            notificationHandler.sendToUser(passengerEmail, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRideDriverNotification(Ride ride) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "NEW_RIDE_ASSIGNED",
                    "rideId", ride.getId(),
                    "message", "You have a new ride assigned!",
                    "from", ride.getStartLocation().getAddress(),
                    "to", ride.getEndLocation().getAddress(),
                    "passengerName", ride.getPassenger().getFirstName() + " " + ride.getPassenger().getLastName()
            );
            String json = objectMapper.writeValueAsString(payload);
            notificationHandler.sendToUser(ride.getDriver().getEmail(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRideReminderNotification(String passengerEmail, Integer rideId, long minutesBefore, String from, String to) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "RIDE_REMINDER",
                    "rideId", rideId,
                    "message", "Reminder: Your ride starts in " + minutesBefore + " minutes!",
                    "from", from,
                    "to", to
            );
            String json = objectMapper.writeValueAsString(payload);
            notificationHandler.sendToUser(passengerEmail, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPanicNotificationToAdmins(Integer panicId, String reportedBy, String location, Integer rideId, Double latitude, Double longitude) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "PANIC_ALERT",
                    "panicId", panicId,
                    "message", "PANIC ALERT! Reported by: " + reportedBy,
                    "reportedBy", reportedBy,
                    "location", location,
                    "latitude", latitude != null ? latitude : 0.0,
                    "longitude", longitude != null ? longitude : 0.0,
                    "rideId", rideId
            );
            String json = objectMapper.writeValueAsString(payload);
            // Send to all connected admin users
            notificationHandler.sendToAllAdmins(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}