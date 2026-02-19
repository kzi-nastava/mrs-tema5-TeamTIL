package com.example.uberproject.api;

import com.example.uberproject.dto.response.DriverRideHistoryResponseDTO;
import com.example.uberproject.dto.response.RideHistoryResponseDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import com.example.uberproject.dto.request.RideRequestDTO;
import com.example.uberproject.dto.response.RideCreatedResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface RideApi {
    @GET("rides/{userEmail}/history")
    Call<List<RideHistoryResponseDTO>> getUserRideHistory(@Path("userEmail") String userEmail);

    @GET("rides/admin/history")
    Call<List<RideHistoryResponseDTO>> getAdminRideHistory();

    @GET("rides/driver/{driverEmail}/history")
    Call<List<DriverRideHistoryResponseDTO>> getDriverRideHistory(@Path("driverEmail") String driverEmail);

    @POST("rides")
    Call<RideCreatedResponseDTO> createRide(@Body RideRequestDTO request);

}



