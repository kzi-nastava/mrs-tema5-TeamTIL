package rs.ac.uns.ftn.asd.Projekatsiit2023.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideHistoryDTO {
    private Integer id;
    private String passengerEmail;
    private String driverEmail;
    private String startLocation;
    private String endLocation;
    private String status;
    private Double price;
    private String createdAt;
}
