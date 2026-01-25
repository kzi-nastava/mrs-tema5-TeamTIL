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
@Entity
@NoArgsConstructor
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "distance", nullable = false)
    private Double distance;

    /**
     * Estimated time in seconds
     */
    @Column(name = "estimated_time", nullable = false)
    private Long estimatedTime = 0L;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "location_order")
    private List<Location> locations = new ArrayList<>();

    @OneToMany(mappedBy = "route")
    private List<Ride> rides = new ArrayList<>();
}
