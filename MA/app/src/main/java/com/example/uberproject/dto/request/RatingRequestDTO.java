package com.example.uberproject.dto.request;

public class RatingRequestDTO {
    private String userEmail;
    private int driverRating;
    private int vehicleRating;
    private String comment;

    public RatingRequestDTO(String userEmail, int driverRating, int vehicleRating, String comment) {
        this.userEmail = userEmail;
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

    public String getUserEmail() { return userEmail; }
    public int getDriverRating() { return driverRating; }
    public int getVehicleRating() { return vehicleRating; }
    public String getComment() { return comment; }
}