package com.example.uberproject.api;

import com.example.uberproject.dto.request.DriverRegistrationRequestDTO;
import com.example.uberproject.dto.request.ActivateDriverRequestDTO;
import com.example.uberproject.dto.response.DriverRegistrationResponseDTO;
import com.example.uberproject.dto.response.ActivateDriverResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DriverApi {

    @POST("drivers")
    Call<DriverRegistrationResponseDTO> registerDriver(@Body DriverRegistrationRequestDTO request);

    @POST("drivers/activate")
    Call<ActivateDriverResponseDTO> activateDriver(@Body ActivateDriverRequestDTO request);
}
