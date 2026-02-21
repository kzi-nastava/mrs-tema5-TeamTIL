package com.example.uberproject.api;

import android.content.Context;
import android.util.Log;

import com.example.uberproject.BuildConfig;
import com.example.uberproject.api.interceptor.TokenInterceptor;
import com.example.uberproject.utils.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = TokenManager.getInstance(context);

            // HTTP Logging za debug
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d(TAG, message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new TokenInterceptor(tokenManager))
                    .build();

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.IDENTITY)
                    .setLenient()  // Tolerantni parsing za unexpected JSON strukture
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();

            Log.d(TAG, "Retrofit initialized with base URL: " + BuildConfig.API_HOST);
        }
        return retrofit;
    }
}


