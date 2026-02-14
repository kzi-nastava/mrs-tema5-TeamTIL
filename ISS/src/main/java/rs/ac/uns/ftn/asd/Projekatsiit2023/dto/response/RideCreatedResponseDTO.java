package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideCreatedResponseDTO {
    private Integer rideId;
    private String status;
    private Double estimatedPrice;
    private String driverName;
    private String driverEmail;
    private String vehicleInfo;
    private String message;
    private String startTime;
    private String estimatedEndTime;
    private Double distanceKm;
    private Double durationMin;
}
