package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;

@Setter
@Getter
@Data
public class PriceConfig {
    private VehicleType vehicleType;
    private Double basePrice;
    private Double pricePerKm;
}
