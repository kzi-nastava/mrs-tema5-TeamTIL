package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRideResponseDTO {
    private Integer id;
    private String passengerEmail;
    private String driverEmail;
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
