package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivationRequestDTO {
    private String token;
    private String newPassword;
}