package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideCancelResponseDTO {
    private Integer rideId;
    private String status;
    private String cancellationReason;
    private String message;

    public RideCancelResponseDTO() {}

    public RideCancelResponseDTO(Integer rideId, String status, String cancellationReason, String message) {
        this.rideId = rideId;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.message = message;
    }
}