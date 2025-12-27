package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}