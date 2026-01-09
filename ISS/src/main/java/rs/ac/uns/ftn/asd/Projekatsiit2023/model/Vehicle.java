package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "baby_friendly", nullable = false)
    private Boolean babyFriendly;

    @Column(name = "pet_friendly", nullable = false)
    private Boolean petFriendly;

    @OneToOne(mappedBy = "vehicle", fetch = FetchType.LAZY)
    private Driver driver;

    @OneToMany(mappedBy = "ratedVehicle")
    private List<Rating> ratings = new ArrayList<>();
}
