package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDTO {
    private List<LocationDTO> locations; // Lista lokacija sa koordinatama
    private List<String> passengerEmails;
    private String vehicleType;
    private Boolean babyFriendly;
    private Boolean petFriendly;
    private String scheduledTime;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDTO {
        private String address;
        private Double latitude;
        private Double longitude;
    }
}
