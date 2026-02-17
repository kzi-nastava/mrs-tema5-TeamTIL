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
public class RideStatsResponseDTO {
    private List<RideStatsDayDTO> days;
    private int totalRides;
    private double totalDistanceKm;
    private double totalMoney;
    private double avgRidesPerDay;
    private double avgDistancePerDay;
    private double avgMoneyPerDay;
}
