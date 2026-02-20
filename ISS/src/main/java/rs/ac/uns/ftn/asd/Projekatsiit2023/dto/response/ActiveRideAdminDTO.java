package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActiveRideAdminDTO {
    private Integer rideId;

    // Driver info
    private String driverFirstName;
    private String driverLastName;
    private String driverEmail;
    private String driverPhone;
    private String driverProfilePicture;
    private Double driverRating;

    // Vehicle info
    private String vehicleModel;
    private String vehicleType;
    private String licensePlate;

    // Passenger info
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerPhone;
    private String passengerProfilePicture;

    // Ride info
    private String startAddress;
    private String endAddress;
    private String rideStatus;
    private String startTime;
    private String estimatedEndTime;
    private Double price;
    private Double distanceKm;

    // Vehicle location (for potential map use)
    private Double vehicleLat;
    private Double vehicleLon;
}