package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    @NotNull
    private String name;
    @NotNull
    private String surname;

    @NotNull
    private String email;
    @NotNull
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    private String password;

    @NotNull
    private String phoneNumber;
    @NotNull
    private String city;

    private String profilePictureUrl;
    @NotNull
    private String userType; // "REGISTERED_USER"
}