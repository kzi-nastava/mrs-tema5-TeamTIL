package rs.ac.uns.ftn.asd.Projekatsiit2023.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceConfigDTO {
    @NotNull(message = "Vehicle type must not be null")
    private VehicleType vehicleType;

    @NotNull(message = "Base price must not be null")
    @Positive(message = "Base price must be greater than 0")
    private Double basePrice;

    @NotNull(message = "Price per km must not be null")
    @Positive(message = "Price per km must be greater than 0")
    private Double pricePerKm;
}
