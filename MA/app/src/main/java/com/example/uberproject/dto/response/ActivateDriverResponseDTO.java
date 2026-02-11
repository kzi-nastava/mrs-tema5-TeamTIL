package com.example.uberproject.dto.response;

public class ActivateDriverResponseDTO {
    private String message;

    // Konstruktori
    public ActivateDriverResponseDTO() {
    }

    public ActivateDriverResponseDTO(String message) {
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
