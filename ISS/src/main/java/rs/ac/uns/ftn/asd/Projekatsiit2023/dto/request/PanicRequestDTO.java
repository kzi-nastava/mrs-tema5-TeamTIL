package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PanicRequestDTO {
    @NotNull
    private Integer rideId;
    @NotNull
    private Integer locationId;
    @NotNull
    private UserType userType;
    @NotNull
    private String accountEmail;
}


