package rs.ac.uns.ftn.asd.Projekatsiit2023.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DriverRideDTO{
    private Integer id;
    private List<PassengerDTO> passengers;

    private String from;
    private String to;
    private String status; // "Completed" | "Canceled"
    private String canceledBy; // "Driver" | "Passenger"

    private String date;
    private String startTime;
    private String endTime;

    private String price;
    private String duration;
    private String distance;

    private Boolean panicActivated;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PassengerDTO {
        private String name;
        private String phone;
    }
}
