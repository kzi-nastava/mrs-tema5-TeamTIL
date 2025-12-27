package rs.ac.uns.ftn.asd.Projekatsiit2023.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideHistoryDTO {
    // Getters and setters
    private Integer id;
    private String passengerEmail;
    private String driverEmail;
    private String startLocation;
    private String endLocation;
    private String status;
    private Double price;
    private String createdAt;

    public RideHistoryDTO() {}

    public RideHistoryDTO(Integer id, String passengerEmail, String driverEmail,
                          String startLocation, String endLocation, String status,
                          Double price, String createdAt) {
        this.id = id;
        this.passengerEmail = passengerEmail;
        this.driverEmail = driverEmail;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.status = status;
        this.price = price;
        this.createdAt = createdAt;
    }
}
