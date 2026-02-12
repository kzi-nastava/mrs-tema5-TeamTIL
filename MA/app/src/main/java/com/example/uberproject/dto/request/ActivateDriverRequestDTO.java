package com.example.uberproject.dto.request;

public class ActivateDriverRequestDTO {
    private String token;
    private String newPassword;

    public ActivateDriverRequestDTO() {
    }

    public ActivateDriverRequestDTO(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}