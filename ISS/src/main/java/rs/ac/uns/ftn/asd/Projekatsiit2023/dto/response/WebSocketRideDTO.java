package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketRideDTO {
    private double latitude;
    private double longitude;
    private int remainingDurationInMinutes;
    private int currentPrice;
}
