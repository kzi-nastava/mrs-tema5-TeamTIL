package rs.ac.uns.ftn.asd.Projekatsiit2023.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignedRideDTO {
    private Integer id;
    private String passengerEmail;
    private String startLocation;
    private String endLocation;
    private String status;
}