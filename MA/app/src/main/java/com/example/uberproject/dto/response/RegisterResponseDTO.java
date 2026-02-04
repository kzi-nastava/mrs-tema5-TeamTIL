package com.example.uberproject.dto.response;

import com.google.gson.annotations.SerializedName;

public class RegisterResponseDTO {
    private String email;
    private String userType;
    private String message;

    @SerializedName("token")
    private String token;

    public RegisterResponseDTO() {}

    public RegisterResponseDTO(String email, String userType, String message) {
        this.email = email;
        this.userType = userType;
        this.message = message;
    }

    public RegisterResponseDTO(String email, String userType, String message, String token) {
        this.email = email;
        this.userType = userType;
        this.message = message;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

