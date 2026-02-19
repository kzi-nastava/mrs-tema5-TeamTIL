package com.example.uberproject.dto.response;

public class AssignedRideDTO {
    private Integer id;
    private String accountEmail;
    private String passengerEmail;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerProfilePictureUrl;
    private String passengerPhoneNumber;
    private String startLocation;
    private String endLocation;
    private String status;
    private String startTime;
    private String estimatedEndTime;
    private Double price;
    private Double distance;
    private Double duration;

    public AssignedRideDTO() {}

    public AssignedRideDTO(Integer id, String accountEmail, String startLocation, String endLocation,
                          String status, String startTime, String estimatedEndTime,
                          Double price, Double distance, Double duration) {
        this.id = id;
        this.accountEmail = accountEmail;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.status = status;
        this.startTime = startTime;
        this.estimatedEndTime = estimatedEndTime;
        this.price = price;
        this.distance = distance;
        this.duration = duration;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerFirstName() {
        return passengerFirstName;
    }

    public void setPassengerFirstName(String passengerFirstName) {
        this.passengerFirstName = passengerFirstName;
    }

    public String getPassengerLastName() {
        return passengerLastName;
    }

    public void setPassengerLastName(String passengerLastName) {
        this.passengerLastName = passengerLastName;
    }

    public String getPassengerProfilePictureUrl() {
        return passengerProfilePictureUrl;
    }

    public void setPassengerProfilePictureUrl(String passengerProfilePictureUrl) {
        this.passengerProfilePictureUrl = passengerProfilePictureUrl;
    }

    public String getPassengerPhoneNumber() {
        return passengerPhoneNumber;
    }

    public void setPassengerPhoneNumber(String passengerPhoneNumber) {
        this.passengerPhoneNumber = passengerPhoneNumber;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEstimatedEndTime() {
        return estimatedEndTime;
    }

    public void setEstimatedEndTime(String estimatedEndTime) {
        this.estimatedEndTime = estimatedEndTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}



