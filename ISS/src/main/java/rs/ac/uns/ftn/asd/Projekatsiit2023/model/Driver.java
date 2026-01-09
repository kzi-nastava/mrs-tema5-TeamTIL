package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Driver extends User {
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    /**
     * Active working time in seconds
     */
    @Column(name = "active_hours", nullable = false)
    private Long activeHours = 0L;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<InconsistencyReport> reports = new ArrayList<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<PanicNotification> panicNotifications;

    @OneToMany(mappedBy = "ratedDriver")
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "driver")
    private List<Ride> assignedRides = new ArrayList<>();
}
