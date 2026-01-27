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
    private String date;
    private String startTime;
    private String endTime;
    private String from;
    private String to;
    private String price;
    private String status; // "Completed" | "Canceled"
    private String canceledBy; // "Driver" | "Passenger"
    private Boolean panicActivated;
    private String duration;
    private String distance;
    private List<PassengerDTO> passengers;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PassengerDTO {
        private String name;
        private String phone;
    }
}
