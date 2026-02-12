package com.example.uberproject.dto.response;

public class ActivateDriverResponseDTO {
    private String message;

    public ActivateDriverResponseDTO() {
    }

    public ActivateDriverResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
