package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RideCancelResponseDTO {
    private Integer rideId;
    private String status;
    private String cancellationReason;
    private String message;
    private String cancelledBy; // "PASSENGER" or "DRIVER"
}