package com.example.uberproject.dto.response;

public class DriverRegistrationResponseDTO {
    private String message;

    // Konstruktori
    public DriverRegistrationResponseDTO() {
    }

    public DriverRegistrationResponseDTO(String message) {
        this.message = message;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }
}
