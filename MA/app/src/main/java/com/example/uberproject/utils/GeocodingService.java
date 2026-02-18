package com.example.uberproject.utils;

import android.util.Log;

import com.example.uberproject.dto.response.GeocodingResponseDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class GeocodingService {

    public interface NominatimApi {
        @GET("search")
        Call<GeocodingResponseDTO[]> geocode(
                @Query("q") String address,
                @Query("format") String format,
                @Query("limit") int limit,
                @Query("bounded") int bounded,
                @Query("viewbox") String viewbox
        );
    }

    private NominatimApi nominatimApi;

    public GeocodingService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    okhttp3.Request newRequest = request.newBuilder()
                            .header("User-Agent", "tilTaxi/1.0 (Android)")
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();

        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        nominatimApi = retrofit.create(NominatimApi.class);
    }

    public void geocodeAddress(String address, OnGeocodeListener listener) {
        // Viewbox for Novi Sad area (SW to NE corners)
        // 19.7, 45.1, 20.0, 45.3
        String viewbox = "19.7,45.1,20.0,45.3";

        Call<GeocodingResponseDTO[]> call = nominatimApi.geocode(
                address + ", Serbia",
                "json",
                1,
                1,
                viewbox
        );

        call.enqueue(new Callback<GeocodingResponseDTO[]>() {
            @Override
            public void onResponse(Call<GeocodingResponseDTO[]> call, Response<GeocodingResponseDTO[]> response) {
                Log.d("Geocoding", "=== GEOCODING RESPONSE ===");
                Log.d("Geocoding", "Address: " + address);
                Log.d("Geocoding", "Response code: " + response.code());
                Log.d("Geocoding", "Body is null: " + (response.body() == null));

                if (response.body() != null) {
                    Log.d("Geocoding", "Body length: " + response.body().length);

                    try {
                        String rawJson = new Gson().toJson(response.body());
                        Log.d("Geocoding", "Raw JSON: " + rawJson);
                    } catch (Exception e) {
                        Log.e("Geocoding", "Error logging JSON: " + e.getMessage());
                    }
                }

                if (response.isSuccessful() && response.body() != null && response.body().length > 0) {
                    GeocodingResponseDTO result = response.body()[0];
                    Log.d("Geocoding", "Result 0 - Lat: " + result.getLat() + ", Lon: " + result.getLon());

                    try {
                        double lat = result.getLat();
                        double lon = result.getLon();

                        Log.d("Geocoding", "Extracted - Lat: " + lat + ", Lon: " + lon);

                        // Sanity check - must be in Novi Sad area
                        if (lat > 45.0 && lat < 45.4 && lon > 19.6 && lon < 20.1) {
                            Log.d("Geocoding", "Coordinates are valid, calling listener");
                            listener.onSuccess(lat, lon);
                        } else {
                            Log.d("Geocoding", "Coordinates out of bounds: " + lat + ", " + lon);
                            listener.onError("Address not in Novi Sad area");
                        }
                    } catch (Exception e) {
                        Log.e("Geocoding", "Error processing coordinates: " + e.getMessage());
                        listener.onError("Error processing coordinates: " + e.getMessage());
                    }
                } else {
                    Log.d("Geocoding", "No results found or response not successful");
                    listener.onError("Address not found");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponseDTO[]> call, Throwable t) {
                Log.e("Geocoding", "Request failed: " + t.getMessage());
                listener.onError(t.getMessage());
            }
        });
    }

    public interface OnGeocodeListener {
        void onSuccess(double latitude, double longitude);
        void onError(String errorMessage);
    }
}

