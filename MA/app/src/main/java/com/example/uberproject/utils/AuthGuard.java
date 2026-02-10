package com.example.uberproject.utils;

import android.content.Context;

public class AuthGuard {
    public static boolean isUserLoggedIn(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        return tokenManager.hasToken();
    }

    public static boolean isUserAuthenticated(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        if (!tokenManager.hasToken()) {
            return false;
        }
        String token = tokenManager.getToken();
        return JwtTokenUtil.isTokenValid(token);
    }

    public static boolean hasAccessByRole(Context context, String requiredRole) {
        if (!isUserLoggedIn(context)) {
            return false;
        }

        TokenManager tokenManager = TokenManager.getInstance(context);
        String userRole = tokenManager.getUserRole();

        if (userRole == null) {
            return false;
        }

        if (requiredRole == null || requiredRole.isEmpty()) {
            return true;
        }

        return userRole.equalsIgnoreCase(requiredRole);
    }

    public static boolean isDriver(Context context) {
        return hasAccessByRole(context, "DRIVER");
    }

    public static boolean isAdmin(Context context) {
        return hasAccessByRole(context, "ADMIN");
    }

    public static boolean isUser(Context context) {
        return hasAccessByRole(context, "REGISTERED_USER");
    }

    public static boolean isRegisteredUser(Context context) {
        return hasAccessByRole(context, "REGISTERED_USER");
    }

    public static String getUserRole(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        return tokenManager.getUserRole();
    }

    public static String getUserEmail(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        return tokenManager.getUserEmail();
    }

    public static String getUserToken(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        return tokenManager.getToken();
    }

    public static void logout(Context context) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        tokenManager.clearToken();
    }
}
