package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RideStopResponseDTO {
    private Integer rideId;
    private String status;
    private String finalLocation;
    private Double finalPrice;
    private String duration;
    private String message;
}