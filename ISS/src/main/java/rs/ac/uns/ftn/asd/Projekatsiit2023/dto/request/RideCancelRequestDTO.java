package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideCancelRequestDTO {
    private String cancellationReason;

    public RideCancelRequestDTO() {}

    public RideCancelRequestDTO(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}

