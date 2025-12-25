package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;

import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

@Data
public class User {
    private Integer id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Boolean isBlocked;
    private String blockReason;
    private UserType userType;
    private String profilePictureUrl;
}
