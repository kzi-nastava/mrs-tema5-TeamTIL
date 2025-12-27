package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

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

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
