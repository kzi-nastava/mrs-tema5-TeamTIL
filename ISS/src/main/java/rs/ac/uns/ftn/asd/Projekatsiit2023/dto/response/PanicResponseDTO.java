package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PanicResponseDTO {
    private Integer id;
    private Integer rideId;
    private Integer locationId;
    private Integer registeredUserId;
    private Integer driverId;
    private Boolean handled;
    private String timestamp;  // Changed from LocalTime to String
    private UserType reportedBy;
    
    // Vehicle information
    private String vehicleName;
    private String vehicleLicensePlate;
    
    // Location information
    private String locationAddress;
    private Double latitude;
    private Double longitude;
    
    // Alias for frontend compatibility (frontend expects panicId, backend uses id)
    @JsonProperty("panicId")
    public Integer getPanicId() {
        return this.id;
    }
}
