package com.example.uberproject.api;

import com.example.uberproject.dto.response.VehicleStatusResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PublicApi {
    @GET("public/vehicles")
    Call<List<VehicleStatusResponseDTO>> getActiveVehicles();
}
