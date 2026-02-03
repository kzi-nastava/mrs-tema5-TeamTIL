package com.example.uberproject.api;

import com.example.uberproject.dto.request.LoginRequestDTO;
import com.example.uberproject.dto.request.ForgotPasswordRequestDTO;
import com.example.uberproject.dto.request.ResetPasswordRequestDTO;
import com.example.uberproject.dto.request.RegisterRequestDTO;
import com.example.uberproject.dto.response.LoginResponseDTO;
import com.example.uberproject.dto.response.ForgotPasswordResponseDTO;
import com.example.uberproject.dto.response.ResetPasswordResponseDTO;
import com.example.uberproject.dto.response.RegisterResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO request);

    @POST("api/auth/register")
    Call<RegisterResponseDTO> register(@Body RegisterRequestDTO request);

    @POST("api/auth/logout")
    Call<Void> logout();

    @POST("api/auth/forgot-password")
    Call<ForgotPasswordResponseDTO> forgotPassword(@Body ForgotPasswordRequestDTO request);

    @POST("api/auth/reset-password")
    Call<ResetPasswordResponseDTO> resetPassword(@Body ResetPasswordRequestDTO request);
}
