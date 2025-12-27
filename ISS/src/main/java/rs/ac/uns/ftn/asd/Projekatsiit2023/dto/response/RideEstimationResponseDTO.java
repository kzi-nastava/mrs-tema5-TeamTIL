package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideEstimationResponseDTO {
    private String estimatedTime;
    private double estimatedDistance;
    private double estimatedPrice;
    private String vehicleType;

    public RideEstimationResponseDTO() {}

    public RideEstimationResponseDTO(String estimatedTime, double estimatedDistance,
                                     double estimatedPrice, String vehicleType) {
        this.estimatedTime = estimatedTime;
        this.estimatedDistance = estimatedDistance;
        this.estimatedPrice = estimatedPrice;
        this.vehicleType = vehicleType;
    }

}