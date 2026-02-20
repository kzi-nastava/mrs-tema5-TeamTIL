package com.example.uberproject.dto.response;

public class UserListItemDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String userType;
    private Boolean isBlocked;
    private String blockReason;
    private String profilePictureUrl;

    public Integer getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getUserType() { return userType; }
    public Boolean getIsBlocked() { return isBlocked; }
    public String getBlockReason() { return blockReason; }
    public String getProfilePictureUrl() { return profilePictureUrl; }

    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
}
