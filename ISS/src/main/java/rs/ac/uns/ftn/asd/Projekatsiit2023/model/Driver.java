package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Driver extends Account {
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
    private List<PanicNotification> panicNotifications;

    @OneToMany(mappedBy = "ratedDriver")
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "driver")
    private List<Ride> assignedRides = new ArrayList<>();

    public void assignVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle.getDriver() != this) {
            vehicle.setDriver(this);
        }
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double sum = ratings.stream().mapToDouble(Rating::getDriverRating).sum();
        return sum / ratings.size();
    }
}
