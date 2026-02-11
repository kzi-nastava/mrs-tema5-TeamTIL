package com.example.uberproject.dto.request;

public class ActivateDriverRequestDTO {
    private String token;
    private String newPassword;

    // Konstruktori
    public ActivateDriverRequestDTO() {
    }

    public ActivateDriverRequestDTO(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}