package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InconsistencyReportRequestDTO {
    private String passengerEmail;
    private String location;
    private String message;
    private String timestamp;
}
