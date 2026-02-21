package com.example.uberproject.api.interceptor;

import android.util.Log;

import com.example.uberproject.utils.TokenManager;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private static final String TAG = "TokenInterceptor";
    private final TokenManager tokenManager;

    public TokenInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws java.io.IOException {
        Request originalRequest = chain.request();
        String token = tokenManager.getToken();

        Log.d(TAG, "Request to: " + originalRequest.url());

        // Ako nema tokena, pošalji zahtev kako jeste
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "No token available, sending request without Authorization header");
            return chain.proceed(originalRequest);
        }

        Log.d(TAG, "Token found, adding Authorization header. Token length: " + token.length());
        Request newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(newRequest);
    }
}
