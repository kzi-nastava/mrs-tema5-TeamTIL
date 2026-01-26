package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
public class DriverResponseDTO extends UserResponseDTO {
    private String vehicleModel;
    private String vehicleType;
    private String licensePlate;
    private Integer passengerCapacity;
    private Boolean babyFriendly;
    private Boolean petFriendly;

    private Boolean isActive;

    public DriverResponseDTO(Integer id, String firstName, String lastName, String email,
                             String phoneNumber, String address, String profilePictureUrl,
                             String vehicleModel, String vehicleType, String licensePlate,
                             Integer passengerCapacity, Boolean babyFriendly, Boolean petFriendly,
                             Boolean isActive) {
        super(id, firstName, lastName, email, phoneNumber, address, profilePictureUrl);
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
        this.passengerCapacity = passengerCapacity;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
        this.isActive = isActive;
    }
}