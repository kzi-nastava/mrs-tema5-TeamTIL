package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class PanicNotification extends Notification {
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "handled", nullable = false)
    private Boolean handled;

    @ManyToOne
    @JoinColumn(name = "registered_user_id")
    private RegisteredUser registeredUser;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;
}
