package com.example.uberproject.dto.request;

public class LoginRequestDTO {
    private String email;
    private String password;

    public LoginRequestDTO () {}
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
