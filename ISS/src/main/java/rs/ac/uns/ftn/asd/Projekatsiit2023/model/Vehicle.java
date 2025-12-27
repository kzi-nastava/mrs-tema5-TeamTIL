package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;

@Setter
@Getter
@Data
public class Vehicle {
    private Integer id;
    private String model;
    private VehicleType type;
    private String licensePlate;
    private Integer capacity;
    private Boolean babyFriendly;
    private Boolean petFriendly;
}
