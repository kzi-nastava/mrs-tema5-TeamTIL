package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideRatingRequestDTO {
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;
}
