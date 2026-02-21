package com.example.uberproject.api;

import com.example.uberproject.dto.PriceConfigDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PriceConfigApi {

    @GET("price-config/{vehicleType}")
    Call<PriceConfigDTO> getPriceConfig(@Path("vehicleType") String vehicleType);

    @PUT("price-config/{vehicleType}")
    Call<PriceConfigDTO> updatePriceConfig(
            @Path("vehicleType") String vehicleType,
            @Body PriceConfigDTO dto
    );
}