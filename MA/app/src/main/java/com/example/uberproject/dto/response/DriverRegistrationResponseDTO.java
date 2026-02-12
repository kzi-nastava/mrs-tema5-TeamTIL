package com.example.uberproject.dto.response;

public class DriverRegistrationResponseDTO {
    private String message;

    public DriverRegistrationResponseDTO() {
    }

    public DriverRegistrationResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
