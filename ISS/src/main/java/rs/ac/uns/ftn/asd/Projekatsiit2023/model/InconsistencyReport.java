package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class InconsistencyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "time_reported", nullable = false)
    private LocalTime timeReported;

    @ManyToOne
    @JoinColumn(name = "reported_by_id")
    private RegisteredUser reportedBy;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;
}
