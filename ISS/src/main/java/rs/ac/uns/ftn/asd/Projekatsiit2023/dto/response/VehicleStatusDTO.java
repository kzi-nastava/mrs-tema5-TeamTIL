package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VehicleStatusDTO {
    private String name;
    private String type;
    private String licensePlate;
    private Boolean available;
    private Double latitude;
    private Double longitude;
}
