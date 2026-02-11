package com.example.uberproject.api;

import com.example.uberproject.dto.response.RideHistoryResponseDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface RideApi {
    @GET("rides/{userEmail}/history")
    Call<List<RideHistoryResponseDTO>> getUserRideHistory(@Path("userEmail") String userEmail);
}



