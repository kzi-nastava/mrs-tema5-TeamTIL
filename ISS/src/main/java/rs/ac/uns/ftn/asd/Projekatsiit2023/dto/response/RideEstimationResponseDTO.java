package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideEstimationResponseDTO {
    private String estimatedTime;
    private double estimatedDistance;
    private double estimatedPrice;
    private String vehicleType;
}