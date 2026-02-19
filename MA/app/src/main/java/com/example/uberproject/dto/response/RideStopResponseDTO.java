package com.example.uberproject.dto.response;
public class RideStopResponseDTO {
    private Integer rideId;
    private String status;
    private Double finalPrice;
    private Double finalDistance;
    private Double finalDuration;
    private String message;

    // Getters and Setters
    public Integer getRideId() {
        return rideId;
    }

    public void setRideId(Integer rideId) {
        this.rideId = rideId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getFinalDistance() {
        return finalDistance;
    }

    public void setFinalDistance(Double finalDistance) {
        this.finalDistance = finalDistance;
    }

    public Double getFinalDuration() {
        return finalDuration;
    }

    public void setFinalDuration(Double finalDuration) {
        this.finalDuration = finalDuration;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Constructor
    public RideStopResponseDTO(Integer rideId, String status, Double finalPrice, Double finalDistance, Double finalDuration, String message) {
        this.rideId = rideId;
        this.status = status;
        this.finalPrice = finalPrice;
        this.finalDistance = finalDistance;
        this.finalDuration = finalDuration;
        this.message = message;
    }

    public RideStopResponseDTO() {}
}

