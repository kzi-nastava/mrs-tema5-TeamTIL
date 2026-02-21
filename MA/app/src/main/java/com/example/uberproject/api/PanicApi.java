package com.example.uberproject.api;

import com.example.uberproject.dto.response.PanicResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PanicApi {

    @GET("panic")
    Call<List<PanicResponseDTO>> getAllPanics();

    @PUT("panic/{id}/handle")
    Call<PanicResponseDTO> handlePanic(@Path("id") Integer id);
}

