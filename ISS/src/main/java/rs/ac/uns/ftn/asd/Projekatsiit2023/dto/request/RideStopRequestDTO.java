package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideStopRequestDTO {
    @NotNull(message = "End location cannot be null")
    private Location actualEndLocation;
    @NotNull(message = "End time cannot be null")
    private LocalDateTime actualEndTime;
}
