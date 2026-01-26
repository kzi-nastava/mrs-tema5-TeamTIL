package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;

@Getter
@Setter
public class DriverRegistrationRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String profilePictureUrl;

    private String vehicleModel;
    private VehicleType vehicleType;
    private String licensePlate;
    private Integer passengerCapacity;
    private Boolean babyFriendly;
    private Boolean petFriendly;
}
