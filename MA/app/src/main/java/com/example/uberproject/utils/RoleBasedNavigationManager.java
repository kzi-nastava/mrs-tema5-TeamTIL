package com.example.uberproject.utils;

import android.content.Context;
import androidx.fragment.app.Fragment;

import com.example.uberproject.fragments.forms.AdminProfileFragment;
import com.example.uberproject.fragments.forms.DriverProfileFragment;
import com.example.uberproject.fragments.forms.ProfileFragment;
import com.example.uberproject.fragments.forms.LoginFragment;

public class RoleBasedNavigationManager {
    public static Fragment getProfileFragmentByRole(Context context) {
        // Proveri da li je korisnik ulogovan
        if (!AuthGuard.isUserAuthenticated(context)) {
            return new LoginFragment();
        }

        String userRole = AuthGuard.getUserRole(context);

        if (userRole == null) {
            return new LoginFragment();
        }
        switch (userRole.toUpperCase()) {
            case "DRIVER":
                return new DriverProfileFragment();
            case "ADMIN":
                return new AdminProfileFragment();
            case "USER":
            default:
                return new ProfileFragment();
        }
    }

    public static boolean canAccessFragment(Context context, String requiredRole) {
        if (requiredRole == null || requiredRole.isEmpty()) {
            return AuthGuard.isUserAuthenticated(context);
        }

        return AuthGuard.hasAccessByRole(context, requiredRole);
    }

    public static boolean canAccessRideHistory(Context context) {
        return AuthGuard.isDriver(context);
    }

    public static boolean canAccessAdminPanel(Context context) {
        return AuthGuard.isAdmin(context);
    }

    public static boolean canAccessDriverProfile(Context context) {
        return AuthGuard.isDriver(context);
    }

    public static boolean canAccessUserProfile(Context context) {
        return AuthGuard.isUser(context);
    }
}
