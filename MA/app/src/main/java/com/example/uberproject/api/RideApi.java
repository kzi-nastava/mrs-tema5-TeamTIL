package com.example.uberproject.api;

import com.example.uberproject.dto.response.AssignedRideDTO;
import com.example.uberproject.dto.request.RideCancelRequestDTO;
import com.example.uberproject.dto.request.RideRequestDTO;
import com.example.uberproject.dto.request.RideStopRequestDTO;
import com.example.uberproject.dto.response.DriverRideHistoryResponseDTO;
import com.example.uberproject.dto.response.RideCreatedResponseDTO;
import com.example.uberproject.dto.response.RideHistoryResponseDTO;
import com.example.uberproject.dto.response.RideCancelResponseDTO;
import com.example.uberproject.dto.response.RideStopResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface RideApi {
    @GET("rides/{userEmail}/history")
    Call<List<RideHistoryResponseDTO>> getUserRideHistory(@Path("userEmail") String userEmail);

    @GET("rides/admin/history")
    Call<List<RideHistoryResponseDTO>> getAdminRideHistory();

    @GET("rides/driver/{driverEmail}/history")
    Call<List<DriverRideHistoryResponseDTO>> getDriverRideHistory(@Path("driverEmail") String driverEmail);

    @GET("rides/assigned")
    Call<List<AssignedRideDTO>> getDriverAssignedRides(@Query("driverEmail") String driverEmail);

    @PUT("rides/{rideId}/cancel")
    Call<RideCancelResponseDTO> cancelRide(@Path("rideId") Integer rideId, @Body RideCancelRequestDTO request);

    @PUT("rides/{rideId}/stop")
    Call<RideStopResponseDTO> stopRide(@Path("rideId") Integer rideId, @Body RideStopRequestDTO request);

    @POST("rides")
    Call<RideCreatedResponseDTO> createRide(@Body RideRequestDTO request);

}



