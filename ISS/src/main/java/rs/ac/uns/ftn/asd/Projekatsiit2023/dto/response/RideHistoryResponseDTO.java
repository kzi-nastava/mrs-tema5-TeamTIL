package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryResponseDTO {
    private Integer id;
    private Integer routeId;
    private String passengerEmail;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerProfilePictureUrl;
    private String passengerPhoneNumber;

    private String driverEmail;
    private String driverFirstName;
    private String driverLastName;
    private String driverProfilePictureUrl;
    private String driverPhoneNumber;

    private String startLocation;
    private String endLocation;
    private String status;

    private String startTime;
    private String estimatedEndTime;

    private Double price;
    private Double distance;
    private Double duration;
    private Boolean panicSent;
}
