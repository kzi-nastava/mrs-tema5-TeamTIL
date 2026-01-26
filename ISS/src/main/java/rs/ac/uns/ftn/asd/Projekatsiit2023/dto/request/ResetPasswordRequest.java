package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

public class ResetPasswordRequest {
    private String token;
    private String newPassword;

    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }
}