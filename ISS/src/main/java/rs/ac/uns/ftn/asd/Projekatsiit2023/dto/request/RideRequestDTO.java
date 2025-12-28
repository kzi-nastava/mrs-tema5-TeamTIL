package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import java.util.List;

@Getter @Setter
public class RideRequestDTO {
    private List<String> locations;
    private List<String> passengerEmails;
    private VehicleType vehicleType;
    private Boolean babyFriendly;
    private Boolean petFriendly;
    private String scheduledTime;
}
