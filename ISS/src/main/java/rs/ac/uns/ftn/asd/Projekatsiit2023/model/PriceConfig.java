package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class PriceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vehicle_type", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "price_per_km", nullable = false)
    private Double pricePerKm;
}
