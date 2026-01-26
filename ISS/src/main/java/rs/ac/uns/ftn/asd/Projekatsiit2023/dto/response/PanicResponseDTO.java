package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

import java.time.LocalTime;

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
    private LocalTime timestamp;
    private UserType reportedBy;
}
