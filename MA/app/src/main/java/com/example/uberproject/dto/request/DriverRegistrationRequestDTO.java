package com.example.uberproject.dto.request;

public class DriverRegistrationRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String profilePictureUrl;

    private String vehicleModel;
    private String vehicleType; // "STANDARD", "LUXURY", "VAN"
    private String licensePlate;
    private Integer passengerCapacity;
    private Boolean babyFriendly;
    private Boolean petFriendly;

    // Konstruktori
    public DriverRegistrationRequestDTO() {
    }

    public DriverRegistrationRequestDTO(String firstName, String lastName, String email,
                                        String phoneNumber, String address, String profilePictureUrl,
                                        String vehicleModel, String vehicleType, String licensePlate,
                                        Integer passengerCapacity, Boolean babyFriendly, Boolean petFriendly) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profilePictureUrl = profilePictureUrl;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
        this.passengerCapacity = passengerCapacity;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public Integer getPassengerCapacity() {
        return passengerCapacity;
    }

    public Boolean getBabyFriendly() {
        return babyFriendly;
    }

    public Boolean getPetFriendly() {
        return petFriendly;
    }

    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setPassengerCapacity(Integer passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    public void setBabyFriendly(Boolean babyFriendly) {
        this.babyFriendly = babyFriendly;
    }

    public void setPetFriendly(Boolean petFriendly) {
        this.petFriendly = petFriendly;
    }
}
