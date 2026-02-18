package com.example.uberproject.api;

import com.example.uberproject.dto.request.RideEstimationRequestDTO;
import com.example.uberproject.dto.response.RideEstimationResponseDTO;
import com.example.uberproject.dto.response.VehicleStatusResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PublicApi {
    @GET("public/vehicles")
    Call<List<VehicleStatusResponseDTO>> getActiveVehicles();

    @POST("route/estimate")
    Call<RideEstimationResponseDTO> estimateRide(@Body RideEstimationRequestDTO request);
}
