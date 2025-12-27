package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponseDTO {
    private String token;
    private String userType;
    private String email;
    private String message;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String userType, String email, String message) {
        this.token = token;
        this.userType = userType;
        this.email = email;
        this.message = message;
    }
}
