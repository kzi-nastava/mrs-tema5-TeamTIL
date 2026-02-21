package com.example.uberproject.api;

import com.example.uberproject.dto.response.ActiveRideAdminDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ActiveRidesApi {

    @GET("rides/admin/active")
    Call<List<ActiveRideAdminDTO>> getActiveRides();
}