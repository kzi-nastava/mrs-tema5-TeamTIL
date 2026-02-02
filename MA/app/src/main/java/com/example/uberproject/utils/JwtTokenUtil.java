package com.example.uberproject.utils;

import com.auth0.android.jwt.JWT;

import java.util.Date;

public class JwtTokenUtil {
    public static boolean isTokenExpired(String token) {
        try {
            JWT jwt = new JWT(token);
            Date expiresAt = jwt.getExpiresAt();
            return expiresAt != null && expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public static String getTokenSubject(String token) {
        try {
            JWT jwt = new JWT(token);
            return jwt.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTokenClaim(String token, String claimName) {
        try {
            JWT jwt = new JWT(token);
            return jwt.getClaim(claimName).asString();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return !isTokenExpired(token);
    }

    public static String getUserRoleFromToken(String token) {
        return getTokenClaim(token, "role");
    }

    public static String getUserIdFromToken(String token) {
        return getTokenSubject(token);
    }

    public static long getTokenExpirationTime(String token) {
        try {
            JWT jwt = new JWT(token);
            Date expiresAt = jwt.getExpiresAt();
            if (expiresAt != null) {
                return expiresAt.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getTimeUntilExpiration(String token) {
        try {
            JWT jwt = new JWT(token);
            Date expiresAt = jwt.getExpiresAt();
            if (expiresAt != null) {
                long timeLeft = expiresAt.getTime() - System.currentTimeMillis();
                return timeLeft / 1000;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
