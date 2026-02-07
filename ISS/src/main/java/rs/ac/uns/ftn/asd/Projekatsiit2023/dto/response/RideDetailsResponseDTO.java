package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.LinkdPassengerDTO;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideDetailsResponseDTO {
    private Integer id;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerProfilePictureUrl;
    private String passengerPhoneNumber;

    private String driverFirstName;
    private String driverLastName;
    private String driverProfilePictureUrl;
    private String driverPhoneNumber;
    private Double driverRating;

    private List<LocationResponseDTO> route;
    private List<LinkdPassengerDTO> linkedPassengers;

    private String status;
    private String startTime;
    private String estimatedEndTime;

    private Double price;
    private Double distance;
    private Double duration;

    private Double rideRating;
    private String rideComment;

    private Boolean panicSent;
    private List<String> reportedIssues;
}
