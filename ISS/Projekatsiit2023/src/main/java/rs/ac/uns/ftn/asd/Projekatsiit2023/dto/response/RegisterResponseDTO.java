package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}