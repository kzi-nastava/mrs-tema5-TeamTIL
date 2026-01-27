package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PanicRequestDTO {
    private Integer rideId;
    private Integer locationId;
    private UserType userType;
    private String accountEmail;
}


