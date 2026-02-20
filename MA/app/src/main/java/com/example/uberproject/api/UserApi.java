package com.example.uberproject.api;

import com.example.uberproject.dto.response.ActiveHoursResponseDTO;
import com.example.uberproject.dto.response.DriverResponseDTO;
import com.example.uberproject.dto.response.UserResponseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import com.example.uberproject.dto.request.BlockUserRequestDTO;
import com.example.uberproject.dto.response.UserListItemDTO;
import retrofit2.http.POST;
import java.util.List;

public interface UserApi {
    @GET("users/my-profile")
    Call<UserResponseDTO> getMyProfile();

    @PUT("users/my-profile")
    Call<UserResponseDTO> updateMyProfile(@Body UserResponseDTO updatedData);

    @GET("drivers/my-profile")
    Call<DriverResponseDTO> getDriverProfile();

    @PUT("drivers/my-profile")
    Call<DriverResponseDTO> updateDriverProfile(@Body DriverResponseDTO updatedData);

    @GET("drivers/active-hours")
    Call<ActiveHoursResponseDTO> getActiveHours();

    @GET("accounts/drivers")
    Call<List<UserListItemDTO>> getAllDrivers();

    @GET("accounts/users")
    Call<List<UserListItemDTO>> getAllUsers();

    @POST("accounts/block")
    Call<Void> blockUser(@Body BlockUserRequestDTO request);
}
