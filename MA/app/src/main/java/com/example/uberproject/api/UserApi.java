package com.example.uberproject.api;

import com.example.uberproject.dto.response.DriverResponseDTO;
import com.example.uberproject.dto.response.UserResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserApi {
    @GET("users/my-profile")
    Call<UserResponseDTO> getMyProfile();

    @PUT("users/my-profile")
    Call<UserResponseDTO> updateMyProfile(@Body UserResponseDTO updatedData);

    @GET("drivers/my-profile")
    Call<DriverResponseDTO> getDriverProfile();

    @PUT("drivers/my-profile")
    Call<DriverResponseDTO> updateDriverProfile(@Body DriverResponseDTO updatedData);
}
