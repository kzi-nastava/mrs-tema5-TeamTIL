package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@Data
public class Ride {
    private Integer id;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime scheduledTime;
    private Location startLocation;
    private Location endLocation;
    private RideStatus rideStatus;
    private UserType canceledBy;
    private String cancellationReason;
    private Double totalPrice;

    private RegisteredUser passenger;
    private Driver driver;
    private Route route;
    private List<RegisteredUser> coPassengers;
    private Rating rating;
    private List<PanicNotification> panicNotifications;
    private List<InconsistencyReport> inconsistencyReports;
    private List<Notification> notifications;
}
