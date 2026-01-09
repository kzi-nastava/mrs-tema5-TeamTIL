package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RegisteredUser extends Account {
    @ManyToMany
    @JoinTable(
            name = "user_favorite_routes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "route_id")
    )
    private List<Route> favoriteRoutes = new ArrayList<>();

    @OneToMany(mappedBy = "reportedBy", cascade = CascadeType.ALL)
    private List<InconsistencyReport> reports = new ArrayList<>();

    @OneToMany(mappedBy = "registeredUser", cascade = CascadeType.ALL)
    private List<PanicNotification> panicNotifications;

    @OneToMany(mappedBy = "rater")
    private List<Rating> givenRatings = new ArrayList<>();

    @ManyToMany(mappedBy = "coPassengers")
    private List<Ride> ridesAsCoPassenger = new ArrayList<>();

    @OneToMany(mappedBy = "passenger")
    private List<Ride> ridesAsPassenger = new ArrayList<>();
}
