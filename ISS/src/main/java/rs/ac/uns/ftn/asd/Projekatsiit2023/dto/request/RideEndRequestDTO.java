package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideEndRequestDTO {
    private Location actualEndLocation;
    private LocalDateTime actualEndTime;
}