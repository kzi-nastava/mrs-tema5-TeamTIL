package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @ManyToOne
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus rideStatus;

    @Enumerated(EnumType.STRING)
    private UserType canceledBy;

    private String cancellationReason;

    @Column(nullable = false)
    private Double totalPrice;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private RegisteredUser passenger;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToMany
    @JoinTable(
            name = "ride_co_passengers",
            joinColumns = @JoinColumn(name = "ride_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<RegisteredUser> coPassengers = new ArrayList<>();

    @OneToOne(mappedBy = "ride")
    private Rating rating;

//    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL)
//    private List<PanicNotification> panicNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL)
    private List<InconsistencyReport> inconsistencyReports = new ArrayList<>();

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();
}
