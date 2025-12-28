package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InconsistencyReportResponseDTO {
    private Long rideId;
    private String passengerEmail;
    private String location;
    private String message;
    private String statusMessage;
}
