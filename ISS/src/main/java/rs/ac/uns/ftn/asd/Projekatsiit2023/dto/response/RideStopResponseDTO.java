package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideStopResponseDTO {
    // Getters and setters
    private Long rideId;
    private String status;
    private String finalLocation;
    private Double finalPrice;
    private String duration;
    private String message;

    public RideStopResponseDTO() {}

    public RideStopResponseDTO(Long rideId, String status, String finalLocation,
                               Double finalPrice, String duration, String message) {
        this.rideId = rideId;
        this.status = status;
        this.finalLocation = finalLocation;
        this.finalPrice = finalPrice;
        this.duration = duration;
        this.message = message;
    }

}