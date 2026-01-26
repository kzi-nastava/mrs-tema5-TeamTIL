package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideEstimationRequestDTO {
    private String pickupAddress;
    private String destinationAddress;
    private String vehicleType;
    private double pickupLat;
    private double pickupLon;
    private double destinationLat;
    private double destinationLon;
}
