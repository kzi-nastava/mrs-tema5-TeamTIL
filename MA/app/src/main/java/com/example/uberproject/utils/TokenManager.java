package com.example.uberproject.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenManager {

    private static final String PREF_NAME = "app_secure_prefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USER_ROLE_KEY = "user_role";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_PROFILE_PICTURE_KEY = "user_profile_picture";

    private static TokenManager instance;
    private final SharedPreferences encryptedPreferences;

    private TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            this.encryptedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
        }
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveToken(String token, String role, String email) {
        encryptedPreferences.edit()
                .putString(TOKEN_KEY, token)
                .putString(USER_ROLE_KEY, role)
                .putString(USER_EMAIL_KEY, email)
                .apply();
    }

    public void saveToken(String token, String role, String email, String profilePictureUrl) {
        encryptedPreferences.edit()
                .putString(TOKEN_KEY, token)
                .putString(USER_ROLE_KEY, role)
                .putString(USER_EMAIL_KEY, email)
                .putString(USER_PROFILE_PICTURE_KEY, profilePictureUrl)
                .apply();
    }

    public String getToken() {
        return encryptedPreferences.getString(TOKEN_KEY, null);
    }

    public String getUserRole() {
        return encryptedPreferences.getString(USER_ROLE_KEY, null);
    }

    public String getUserEmail() {
        return encryptedPreferences.getString(USER_EMAIL_KEY, null);
    }

    public String getProfilePictureUrl() {
        return encryptedPreferences.getString(USER_PROFILE_PICTURE_KEY, null);
    }

    public boolean hasToken() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void clearToken() {
        encryptedPreferences.edit()
                .remove(TOKEN_KEY)
                .remove(USER_ROLE_KEY)
                .remove(USER_EMAIL_KEY)
                .remove(USER_PROFILE_PICTURE_KEY)
                .apply();
    }

    public void clearAll() {
        encryptedPreferences.edit().clear().apply();
    }

    public void saveProfilePictureUrl(String url) {
        encryptedPreferences.edit()
                .putString(USER_PROFILE_PICTURE_KEY, url)
                .apply();
    }
}
