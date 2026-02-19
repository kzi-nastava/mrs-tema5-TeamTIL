package com.example.uberproject.dto.response;

public class RideCancelResponseDTO {
    private Integer rideId;
    private String status;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Constructor
    public RideCancelResponseDTO(Integer rideId, String status, String message) {
        this.rideId = rideId;
        this.status = status;
        this.message = message;
    }

    public RideCancelResponseDTO() {}
}

