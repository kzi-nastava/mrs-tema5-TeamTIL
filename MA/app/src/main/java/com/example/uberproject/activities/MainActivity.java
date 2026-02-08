package com.example.uberproject.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.fragments.driver.DriverRideHistoryFragment;
import com.example.uberproject.fragments.forms.ProfileFragment;
import com.example.uberproject.fragments.forms.LoginFragment;
import com.example.uberproject.fragments.forms.RegisterFragment;
import com.example.uberproject.fragments.forms.ResetPasswordFragment;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Obrada Deep Link-a za reset lozinke
        handleDeepLink(getIntent());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    showToolbar();
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        findViewById(R.id.ivToolbarLogo).setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.book_an_uber) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                    showLoginFragment();
                } else {
                    loadFragment(new ProfileFragment());
                }
            } else if (itemId == R.id.ride_history) {
                if (!AuthGuard.isDriver(this)) {
                    Toast.makeText(this, "Only drivers can access ride history", Toast.LENGTH_SHORT).show();
                } else {
                    loadFragment(new DriverRideHistoryFragment());
                }
            } else if (itemId == R.id.favorite_rides) {
                if (!AuthGuard.isDriver(this)) {
                    Toast.makeText(this, "Only drivers can access this page", Toast.LENGTH_SHORT).show();
                } else {
                    loadFragment(new ProfileFragment());
                }
            } else if (itemId == R.id.support) {
                if (!AuthGuard.isAdmin(this)) {
                    Toast.makeText(this, "Only admins can access this page", Toast.LENGTH_SHORT).show();
                } else {
                    loadFragment(new ProfileFragment());
                }
            } else if (itemId == R.id.nav_logout) {
                AuthGuard.logout(this);
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                showLoginFragment();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        MenuItem registerItem = menu.findItem(R.id.nav_register);
        MenuItem profileItem = menu.findItem(R.id.nav_profile);

        boolean isLoggedIn = AuthGuard.isUserLoggedIn(this);

        // Sakrij login/register ako je ulogovan
        if (loginItem != null) {
            loginItem.setVisible(!isLoggedIn);
        }
        if (registerItem != null) {
            registerItem.setVisible(!isLoggedIn);
        }

        // Prikaži/sakrij profile view
        if (profileItem != null) {
            profileItem.setVisible(isLoggedIn);
        }

        // Učitaj profile podatke ako je ulogovan
        if (isLoggedIn && profileItem != null) {
            updateToolbarProfile(profileItem);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void updateToolbarProfile(MenuItem profileItem) {
        View actionView = profileItem.getActionView();
        if (actionView == null) return;

        ImageView ivProfileImage = actionView.findViewById(R.id.ivProfileImage);
        TextView tvUsername = actionView.findViewById(R.id.tvUsername);

        TokenManager tokenManager = TokenManager.getInstance(this);
        String email = tokenManager.getUserEmail();
        String profilePictureUrl = tokenManager.getProfilePictureUrl();

        // Postavi username (email bez @domena)
        if (email != null && tvUsername != null) {
            String username = email.split("@")[0];
            tvUsername.setText(username);
        }

        // Učitaj sliku pomoću Glide
        if (ivProfileImage != null) {
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                Glide.with(this)
                        .load(profilePictureUrl)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .into(ivProfileImage);
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_person_placeholder);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem loginItem = menu.findItem(R.id.nav_login);
        if (loginItem != null) {
            SpannableString loginSpan = new SpannableString(loginItem.getTitle());
            loginSpan.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)),
                    0,
                    loginSpan.length(),
                    0
            );
            loginItem.setTitle(loginSpan);
        }

        MenuItem registerItem = menu.findItem(R.id.nav_register);
        if (registerItem != null) {
            SpannableString registerSpan = new SpannableString(registerItem.getTitle());
            registerSpan.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)),
                    0,
                    registerSpan.length(),
                    0
            );
            registerItem.setTitle(registerSpan);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_login) {
            showLoginFragment();
            return true;
        } else if (itemId == R.id.nav_register) {
            showRegisterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showLoginFragment() {
        hideToolbar();
        loadFragment(new LoginFragment());
    }

    public void showRegisterFragment() {
        hideToolbar();
        loadFragment(new RegisterFragment());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }

        Uri data = intent.getData();
        String scheme = data.getScheme();
        String host = data.getHost();
        String path = data.getPath();

        // Provera da li je ovo reset-password link (http://localhost ili https://uberproject.app)
        boolean isValidLink = false;

        if ("http".equals(scheme) && "localhost".equals(host) && "/reset-password".equals(path)) {
            isValidLink = true;  // http://localhost/reset-password
        } else if ("https".equals(scheme) && "uberproject.app".equals(host) && "/reset-password".equals(path)) {
            isValidLink = true;  // https://uberproject.app/reset-password
        }

        if (isValidLink) {
            // Izvuci reset token iz query parametara
            // URL format: http://localhost/reset-password?token=XYZ123
            String resetToken = data.getQueryParameter("token");

            if (resetToken != null && !resetToken.isEmpty()) {
                // Kreiraj Bundle sa reset tokenopm
                Bundle bundle = new Bundle();
                bundle.putString("resetToken", resetToken);

                // Kreiraj ResetPasswordFragment sa tokenopm
                ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
                resetPasswordFragment.setArguments(bundle);

                // Učitaj fragment
                hideToolbar();
                loadFragment(resetPasswordFragment);
            }
        }
    }

    private void hideToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(android.view.View.GONE);
        }
    }

    public void showToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(android.view.View.VISIBLE);
        }
    }
}