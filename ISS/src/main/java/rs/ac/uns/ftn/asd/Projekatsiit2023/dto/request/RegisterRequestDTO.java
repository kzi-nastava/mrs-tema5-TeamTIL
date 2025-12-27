package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequestDTO {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String phoneNumber;
    private String city;
    private String userType; // "REGISTERED_USER"

    public RegisterRequestDTO() {}

    public RegisterRequestDTO(String name, String surname, String email, String password,
                              String phoneNumber, String city, String userType) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.userType = userType;
    }
}