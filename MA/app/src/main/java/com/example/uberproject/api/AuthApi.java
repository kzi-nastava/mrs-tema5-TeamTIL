package com.example.uberproject.api;

import com.example.uberproject.dto.request.LoginRequestDTO;
import com.example.uberproject.dto.response.LoginResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO request);

    @POST("api/auth/logout")
    Call<Void> logout();
}
