package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideEndRequestDTO {
    @NotNull(message = "End location is required")
    @Valid
    private Location actualEndLocation;
}