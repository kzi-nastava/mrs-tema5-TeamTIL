package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PanicRequestDTO {
    @NotNull(message = "Ride ID is required")
    private Integer rideId;

    @NotNull(message = "Location ID is required")
    private Integer locationId;
}


