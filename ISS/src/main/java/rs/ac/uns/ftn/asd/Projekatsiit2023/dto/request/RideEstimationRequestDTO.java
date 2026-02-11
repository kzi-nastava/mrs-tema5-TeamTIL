package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideEstimationRequestDTO {
    @NotNull()
    private String pickupAddress;
    @NotNull
    private String destinationAddress;
    @NotNull
    private String vehicleType;
    @NotNull
    private double pickupLat;
    @NotNull
    private double pickupLon;
    @NotNull
    private double destinationLat;
    @NotNull
    private double destinationLon;
}
