package com.example.uberproject.api;

import android.content.Context;

import com.example.uberproject.BuildConfig;
import com.example.uberproject.api.interceptor.TokenInterceptor;
import com.example.uberproject.utils.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = TokenManager.getInstance(context);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new TokenInterceptor(tokenManager))
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}


