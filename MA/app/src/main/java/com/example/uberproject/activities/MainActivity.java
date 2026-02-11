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
import com.example.uberproject.fragments.forms.DriverRegisterFragment;
import com.example.uberproject.fragments.forms.ProfileFragment;
import com.example.uberproject.fragments.forms.LoginFragment;
import com.example.uberproject.fragments.forms.RegisterFragment;
import com.example.uberproject.fragments.forms.ResetPasswordFragment;
import com.example.uberproject.fragments.forms.NewPasswordFragment;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
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
            } /*else if (itemId == R.id.nav_register_driver) {
                if (!AuthGuard.isAdmin(this)) {
                    Toast.makeText(this, "Only admins can access this page", Toast.LENGTH_SHORT).show();
                } else {
                    hideToolbar();
                    loadFragment(new DriverRegisterFragment());
                }
            } */
            else if (itemId == R.id.nav_register_driver) {
                // PRIVREMENO - proveri ulogu
                String currentRole = AuthGuard.getUserRole(this);
                Toast.makeText(this, "Your role is: " + currentRole, Toast.LENGTH_LONG).show();

                if (!AuthGuard.isAdmin(this)) {
                    Toast.makeText(this, "Only admins can access this page", Toast.LENGTH_SHORT).show();
                } else {
                    hideToolbar();
                    loadFragment(new DriverRegisterFragment());
                }
            }else if (itemId == R.id.nav_logout) {
                AuthGuard.logout(this);
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                showLoginFragment();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Handle deep link kada se aplikacija prvi put otvori
        handleDeepLink(getIntent());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        MenuItem registerItem = menu.findItem(R.id.nav_register);
        MenuItem profileItem = menu.findItem(R.id.nav_profile);

        boolean isLoggedIn = AuthGuard.isUserLoggedIn(this);

        if (loginItem != null) {
            loginItem.setVisible(!isLoggedIn);
        }
        if (registerItem != null) {
            registerItem.setVisible(!isLoggedIn);
        }
        if (profileItem != null) {
            profileItem.setVisible(isLoggedIn);
        }

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

        if (email != null && tvUsername != null) {
            String username = email.split("@")[0];
            tvUsername.setText(username);
        }

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
        String path = data.getPath();

        if (path == null) {
            return;
        }

        // Handle driver activation - /activate-driver
        if (path.contains("/activate-driver")) {
            String token = data.getQueryParameter("token");
            if (token != null && !token.isEmpty()) {
                openNewPasswordFragment(token);
            }
        }

        // Handle reset password - /reset-password
        if (path.contains("/reset-password")) {
            String token = data.getQueryParameter("token");
            if (token != null && !token.isEmpty()) {
                openResetPasswordFragment(token);
            }
        }
    }

    private void openNewPasswordFragment(String token) {
        Bundle bundle = new Bundle();
        bundle.putString("token", token);

        NewPasswordFragment fragment = new NewPasswordFragment();
        fragment.setArguments(bundle);

        hideToolbar();
        loadFragment(fragment);
    }

    private void openResetPasswordFragment(String token) {
        Bundle bundle = new Bundle();
        bundle.putString("resetToken", token);

        ResetPasswordFragment fragment = new ResetPasswordFragment();
        fragment.setArguments(bundle);

        hideToolbar();
        loadFragment(fragment);
    }

    private void hideToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
    }

    public void showToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    public void refreshToolbarImage(String photoUrlOrBase64) {
        if (toolbar != null && toolbar.getMenu() != null) {
            MenuItem profileItem = toolbar.getMenu().findItem(R.id.nav_profile);
            if (profileItem != null) {
                View actionView = profileItem.getActionView();
                if (actionView == null) return;

                ImageView ivProfileImage = actionView.findViewById(R.id.ivProfileImage);
                if (ivProfileImage != null) {
                    Glide.with(this)
                            .load(photoUrlOrBase64)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .circleCrop()
                            .signature(new com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis()))
                            .into(ivProfileImage);
                }
            }
        }
    }
}