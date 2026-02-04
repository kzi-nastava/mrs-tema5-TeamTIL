package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RideTrackingDTO {
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private String driverName;
    private String driverPhone;
    private String startTime;
}
