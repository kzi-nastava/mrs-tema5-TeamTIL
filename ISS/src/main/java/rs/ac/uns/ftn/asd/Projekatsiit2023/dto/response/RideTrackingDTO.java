package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RideTrackingDTO {
    private Long rideId;
    private String vehicleLocation;
    private String estimatedArrivalTime;
    private String status;
}
