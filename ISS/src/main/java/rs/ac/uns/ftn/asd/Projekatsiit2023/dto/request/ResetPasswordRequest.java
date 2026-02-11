package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotNull
    private String token;
    @NotNull
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    private String newPassword;
}