package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteDTO {
    private Integer routeId;
    private String startLocation;
    private String endLocation;
    private List<String> intermediateStops;
    private Double distanceKm;
    private Double estimatedTimeMin;
}