package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import jakarta.validation.constraints.*;

@Getter
@Setter
public class DriverRegistrationRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String address;
    private String profilePictureUrl;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Passenger capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity must be at most 10")
    private Integer passengerCapacity;

    @NotNull(message = "Baby friendly field is required")
    private Boolean babyFriendly;

    @NotNull(message = "Pet friendly field is required")
    private Boolean petFriendly;
}
