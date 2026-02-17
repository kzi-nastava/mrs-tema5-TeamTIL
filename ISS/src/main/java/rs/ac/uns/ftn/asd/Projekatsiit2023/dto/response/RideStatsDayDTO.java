package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideStatsDayDTO {
    private String date;
    private int ridesCount;
    private double distanceKm;
    private double moneyAmount;  // potroseno za korisnika i zaradjeno za vozaca
}
