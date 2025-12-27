package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterResponseDTO {
    private String email;
    private String userType;
    private String message;

    public RegisterResponseDTO() {}

    public RegisterResponseDTO(String email, String userType, String message) {
        this.email = email;
        this.userType = userType;
        this.message = message;
    }
}