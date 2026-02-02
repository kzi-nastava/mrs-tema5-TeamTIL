package com.example.uberproject.dto.response;

public class ForgotPasswordResponseDTO {
    private String message;

    public ForgotPasswordResponseDTO() {}

    public ForgotPasswordResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

