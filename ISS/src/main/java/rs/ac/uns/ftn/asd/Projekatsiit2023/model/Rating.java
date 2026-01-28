package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RatingType;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Double vehicleRating;

    @Column(nullable = false)
    private Double driverRating;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver ratedDriver;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle ratedVehicle;

    @ManyToOne
    @JoinColumn(name = "rater_id", nullable = false)
    private RegisteredUser rater;

    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;
}
