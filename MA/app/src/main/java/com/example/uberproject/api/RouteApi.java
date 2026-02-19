package com.example.uberproject.api;

import com.example.uberproject.dto.response.AddToFavoritesResponseDTO;
import com.example.uberproject.dto.response.FavoriteRouteDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RouteApi {

    // GET /api/route/favorites - Lista omiljenih ruta ulogovanog korisnika
    @GET("route/favorites")
    Call<List<FavoriteRouteDTO>> getFavoriteRoutes();

    // POST /api/route/{routeId}/favorite - Dodaj u omiljene
    @POST("route/{routeId}/favorite")
    Call<AddToFavoritesResponseDTO> addToFavorites(@Path("routeId") Integer routeId);

    // DELETE /api/route/{routeId}/favorite - Ukloni iz omiljenih
    @DELETE("route/{routeId}/favorite")
    Call<AddToFavoritesResponseDTO> removeFromFavorites(@Path("routeId") Integer routeId);

    // GET /api/route/{routeId}/favorite/check - Provjeri je li omiljena
    @GET("route/{routeId}/favorite/check")
    Call<Boolean> isFavorite(@Path("routeId") Integer routeId);
}
