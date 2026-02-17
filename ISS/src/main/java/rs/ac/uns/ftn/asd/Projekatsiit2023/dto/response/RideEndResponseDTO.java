package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RideEndResponseDTO {
    private Integer rideId;
    private String finalLocation;
    private Double finalPrice;
    private String duration;

    // Sledeća vožnja (null ako nema)
    private Integer nextRideId;
    private String nextRideFrom;
    private String nextRideTo;
    private String nextRideScheduledTime;
    private Boolean hasNextRide;
}
