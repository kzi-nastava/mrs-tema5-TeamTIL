package com.example.uberproject.api.interceptor;

import com.example.uberproject.utils.TokenManager;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public TokenInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws java.io.IOException {
        Request originalRequest = chain.request();

        // Ako nema tokena, pošalji zahtev kako jeste
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(newRequest);
    }
}
