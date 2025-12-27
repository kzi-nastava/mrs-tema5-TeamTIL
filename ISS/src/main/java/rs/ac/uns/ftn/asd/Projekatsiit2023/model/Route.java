package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Setter
@Getter
@Data
public class Route {
    private Integer id;
    private Double distance;
    private Duration estimatedTime;
    private List<Location> locations;
}
