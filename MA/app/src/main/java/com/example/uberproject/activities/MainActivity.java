package com.example.uberproject.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.uberproject.BuildConfig;
import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.response.AssignedRideDTO;
import com.example.uberproject.fragments.admin.ActiveRidesFragment;
import com.example.uberproject.fragments.admin.AdminPanicListFragment;
import com.example.uberproject.fragments.admin.AdminRideHistoryFragment;
import com.example.uberproject.fragments.admin.AdminRideStatsFragment;
import com.example.uberproject.fragments.admin.PriceConfigFragment;
import com.example.uberproject.fragments.driver.DriverAssignedRidesFragment;
import com.example.uberproject.fragments.driver.DriverRideHistoryFragment;
import com.example.uberproject.fragments.driver.DriverRideStatsFragment;
import com.example.uberproject.fragments.forms.DriverRegisterFragment;
import com.example.uberproject.fragments.home.HomeFragment;
import com.example.uberproject.fragments.support.SupportChatsFragment;
import com.example.uberproject.fragments.tracking.TrackRideFragment;
import com.example.uberproject.fragments.user.RideStatsFragment;
import com.example.uberproject.fragments.user.UserRideHistoryFragment;
import com.example.uberproject.fragments.forms.ProfileFragment;
import com.example.uberproject.fragments.forms.LoginFragment;
import com.example.uberproject.fragments.forms.RegisterFragment;
import com.example.uberproject.fragments.forms.ResetPasswordFragment;
import com.example.uberproject.fragments.forms.NewPasswordFragment;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.NotificationHelper;
import com.example.uberproject.utils.TokenManager;
import com.example.uberproject.websocket.NotificationWebSocketClient;
import com.example.uberproject.websocket.PanicWebSocketManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.example.uberproject.fragments.forms.RideBookingFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private PanicWebSocketManager panicWebSocketManager;

    private Integer activeRideId = null;

    private NotificationWebSocketClient notificationWsClient;

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "ride_notifications",
                "Ride Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for ride status updates");
        channel.enableVibration(true);
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
        createNotificationChannel();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        // Provera je li token istekao
        checkTokenExpiration();

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
            String userRole = AuthGuard.getUserRole(this);

            // HOME - COMMON FOR ALL USERS
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
            }
            // REGISTERED USER ITEMS
            else if (itemId == R.id.book_an_uber) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                    showLoginFragment();
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    RideBookingFragment bookingSheet = RideBookingFragment.newInstance();
                    bookingSheet.show(getSupportFragmentManager(), "RideBooking");
                    return true;
                }
            } else if (itemId == R.id.track_ride) {
                if (!AuthGuard.isUserLoggedIn(this)) { showLoginFragment(); return true; }
                if (activeRideId != null) {
                    loadFragment(TrackRideFragment.newInstance(activeRideId));
                }
            } else if (itemId == R.id.ride_history) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                }
                loadFragment(new UserRideHistoryFragment());
            } else if (itemId == R.id.user_reports) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new RideStatsFragment());
                }
            } else if (itemId == R.id.support) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new SupportChatsFragment());
                }
            // DRIVER ITEMS
            } else if (itemId == R.id.my_rides) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                    return true;
                }
                loadFragment(new DriverAssignedRidesFragment());
            } else if (itemId == R.id.driver_ride_history) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new DriverRideHistoryFragment());
                }
            } else if (itemId == R.id.driver_reports) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new DriverRideStatsFragment());
                }
            } else if (itemId == R.id.driver_support) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new SupportChatsFragment());
                }
            }
            // ADMIN ITEMS
            else if (itemId == R.id.nav_register_driver) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    hideToolbar();
                    loadFragment(new DriverRegisterFragment());
                }
            } else if (itemId == R.id.admin_price_config) {
                if (!AuthGuard.isUserLoggedIn(this)) { showLoginFragment(); return true; }
                loadFragment(new PriceConfigFragment());
            } else if (itemId == R.id.admin_ride_history) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new AdminRideHistoryFragment());
                }
            } else if (itemId == R.id.admin_active_rides) {
                if (!AuthGuard.isUserLoggedIn(this)) { showLoginFragment(); return true; }
                loadFragment(new ActiveRidesFragment());
            } else if (itemId == R.id.admin_reports) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new AdminRideStatsFragment());
                }
            } else if (itemId == R.id.admin_panic_notifications) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new AdminPanicListFragment());
                }
            } else if (itemId == R.id.admin_support) {
                if (!AuthGuard.isUserLoggedIn(this)) {
                    showLoginFragment();
                } else {
                    loadFragment(new SupportChatsFragment());
                }
            // LOGOUT
            } else if (itemId == R.id.nav_logout) {
                disconnectPanicWebSocket();
                if (notificationWsClient != null) notificationWsClient.disconnect();
                notificationWsClient = null;
                AuthGuard.logout(this);
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                updateNavigationMenuVisibility(navigationView);
                showLoginFragment();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Handle deep link kada se aplikacija prvi put otvori
        handleDeepLink(getIntent());
        // Postavi inicijalni prikaz menu items-a
        updateNavigationMenuVisibility(navigationView);
        // Connect panic WebSocket for admin
        connectPanicWebSocketIfAdmin();
        connectNotificationSocket();
    }

    private void checkForActiveRide() {
        String userRole = AuthGuard.getUserRole(this);
        if (!"REGISTERED_USER".equalsIgnoreCase(userRole)) return;

        String email = TokenManager.getInstance(this).getUserEmail();
        if (email == null) return;

        RideApi rideApi = RetrofitClient.getInstance(this).create(RideApi.class);
        rideApi.getUserActiveRides(email, "IN_PROGRESS").enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<AssignedRideDTO>> call,
                                   @NonNull Response<List<AssignedRideDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    activeRideId = response.body().get(0).getRideId();
                } else {
                    activeRideId = null;
                }
                NavigationView nav = findViewById(R.id.nav_view);
                updateNavigationMenuVisibility(nav);
            }
            @Override
            public void onFailure(@NonNull Call<List<AssignedRideDTO>> call, @NonNull Throwable t) {
                activeRideId = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Provera je li token istekao svaki put kada se aplikacija vrati u foreground
        checkTokenExpiration();
        // Reconnect WebSocket for admin if needed
        connectPanicWebSocketIfAdmin();
        // Provera da li postoji aktivna voznja
        checkForActiveRide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectPanicWebSocket();
        if (notificationWsClient != null) notificationWsClient.disconnect();
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

    private void updateNavigationMenuVisibility(NavigationView navigationView) {
        // Prvo proveravamo da li je token istekao
        if (AuthGuard.isUserLoggedIn(this)) {
            TokenManager tokenManager = TokenManager.getInstance(this);
            if (tokenManager.isTokenExpired()) {
                // Ako je token istekao, logout automatski
                AuthGuard.logout(this);
            }
        }

        boolean isLoggedIn = AuthGuard.isUserLoggedIn(this);
        String userRole = AuthGuard.getUserRole(this);

        Menu menu = navigationView.getMenu();

        // Prvo sakrij sve items
        hideAllMenuItems(menu);

        // Prikaži Home i Logout za sve
        showItem(menu, R.id.nav_home);
        MenuItem logoutItem = menu.findItem(R.id.nav_logout);
        if (logoutItem != null) {
            logoutItem.setVisible(isLoggedIn);
        }

        if (!isLoggedIn) {
            // Ako korisnik nije ulogovan, prikaži samo REGISTERED_USER items
            showMenuGroup(menu, "REGISTERED_USER");
        } else if ("REGISTERED_USER".equalsIgnoreCase(userRole)) {
            // Prikaži samo REGISTERED_USER grupu
            showMenuGroup(menu, "REGISTERED_USER");
        } else if ("DRIVER".equalsIgnoreCase(userRole)) {
            // Prikaži samo DRIVER grupu
            showMenuGroup(menu, "DRIVER");
        } else if ("ADMIN".equalsIgnoreCase(userRole) || "ADMINISTRATOR".equalsIgnoreCase(userRole)) {
            // Prikaži samo ADMIN grupu
            showMenuGroup(menu, "ADMIN");
        } else {
            // Defaultna gruupa za neregistrovane korisnike
            showMenuGroup(menu, "REGISTERED_USER");
        }
    }

    private void hideAllMenuItems(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setVisible(false);
        }
    }

    private void showMenuGroup(Menu menu, String groupType) {
        if ("REGISTERED_USER".equalsIgnoreCase(groupType)) {
            if (activeRideId != null) {
                showItem(menu, R.id.track_ride);
            } else {
                showItem(menu, R.id.book_an_uber);
            }
            showItem(menu, R.id.ride_history);
            showItem(menu, R.id.user_reports);
            showItem(menu, R.id.support);
        } else if ("DRIVER".equalsIgnoreCase(groupType)) {
            showItem(menu, R.id.my_rides);
            showItem(menu, R.id.driver_ride_history);
            showItem(menu, R.id.driver_reports);
            showItem(menu, R.id.driver_support);
        } else if ("ADMIN".equalsIgnoreCase(groupType)) {
            showItem(menu, R.id.nav_register_driver);
            showItem(menu, R.id.admin_price_config);
            showItem(menu, R.id.admin_ride_history);
            showItem(menu, R.id.admin_active_rides);
            showItem(menu, R.id.admin_reports);
            showItem(menu, R.id.admin_panic_notifications);
            showItem(menu, R.id.admin_support);
        }
    }

    private void showItem(Menu menu, int itemId) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(true);
        }
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

            // Add click listener to open ProfileFragment
            ivProfileImage.setOnClickListener(v -> {
                if (AuthGuard.isUserLoggedIn(this)) {
                    loadFragment(new ProfileFragment());
                }
            });
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

        // Handle driver activation - /new-password
        if (path.contains("/new-password")) {
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

    public void refreshNavigationMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            updateNavigationMenuVisibility(navigationView);
        }
    }

    // ======= PANIC WEBSOCKET (ADMIN ONLY) =======

    public void connectPanicWebSocketIfAdmin() {
        String userRole = AuthGuard.getUserRole(this);
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"ADMINISTRATOR".equalsIgnoreCase(userRole)) {
            return;
        }

        TokenManager tokenManager = TokenManager.getInstance(this);
        String email = tokenManager.getUserEmail();
        if (email == null || email.isEmpty()) return;

        if (panicWebSocketManager != null && panicWebSocketManager.isConnected()) return;

        // Build base WebSocket URL from API_HOST
        // API_HOST is e.g. "http://192.168.1.23:8080/api/"
        // We need "ws://192.168.1.23:8080"
        String apiHost = com.example.uberproject.BuildConfig.API_HOST;
        String baseWsUrl = apiHost
                .replace("https://", "wss://")
                .replace("http://", "ws://");
        // Remove trailing /api/ or /api
        baseWsUrl = baseWsUrl.replaceAll("/api/?$", "").replaceAll("/$", "");

        android.util.Log.d("MainActivity", "Connecting admin panic WebSocket: " + baseWsUrl + " as " + email);

        panicWebSocketManager = new PanicWebSocketManager();
        panicWebSocketManager.setPanicListener(new PanicWebSocketManager.PanicListener() {
            @Override
            public void onPanicAlertReceived(PanicWebSocketManager.PanicAlert alert) {
                runOnUiThread(() -> {
                    playAdminPanicSound();
                    showPanicPopup(alert);
                });
            }

            @Override
            public void onConnectionEstablished() {
                android.util.Log.d("MainActivity", "Admin panic WebSocket connected");
            }

            @Override
            public void onConnectionLost() {
                android.util.Log.d("MainActivity", "Admin panic WebSocket disconnected");
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("MainActivity", "Admin panic WebSocket error: " + error);
            }
        });
        panicWebSocketManager.connect(baseWsUrl, email);
    }

    private void disconnectPanicWebSocket() {
        if (panicWebSocketManager != null) {
            panicWebSocketManager.disconnect();
            panicWebSocketManager = null;
        }
    }

    private void playAdminPanicSound() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            MediaPlayer mediaPlayer = MediaPlayer.create(this, alarmUri);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.start();
                // Stop after 2.5 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.release();
                    } catch (Exception ignored) {}
                }, 2500);
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error playing panic sound", e);
        }
    }

    private void showPanicPopup(PanicWebSocketManager.PanicAlert alert) {
        if (isFinishing() || isDestroyed()) return;

        // Mark vehicle on map if coordinates are available
        if (alert.latitude != null && alert.longitude != null) {
            markPanicVehicleOnMap(alert.latitude, alert.longitude,
                    alert.vehicleName != null ? alert.vehicleName : "PANIC");
        }

        String message = "🚨 PANIC ALERT RECEIVED!\n\n";
        if (alert.vehicleName != null) message += "Vehicle: " + alert.vehicleName + "\n";
        if (alert.vehicleLicensePlate != null) message += "Plate: " + alert.vehicleLicensePlate + "\n";
        if (alert.locationAddress != null) message += "Location: " + alert.locationAddress + "\n";
        if (alert.reportedBy != null) message += "Reported by: " + alert.reportedBy + "\n";
        if (alert.rideId != null) message += "Ride ID: " + alert.rideId + "\n";
        if (alert.timestamp != null) message += "Time: " + alert.timestamp;

        final String finalMessage = message;
        final Integer panicId = alert.panicId;

        new AlertDialog.Builder(this)
                .setTitle("🚨 EMERGENCY ALERT")
                .setMessage(finalMessage)
                .setCancelable(false)
                .setPositiveButton("Mark as Handled", (dialog, which) -> {
                    if (panicId != null) {
                        handlePanicFromPopup(panicId);
                    }
                })
                .setNegativeButton("Dismiss", null)
                .setNeutralButton("View All Panics", (dialog, which) -> loadFragment(new AdminPanicListFragment()))
                .show();
    }

    private void handlePanicFromPopup(Integer panicId) {
        com.example.uberproject.api.PanicApi panicApi =
                com.example.uberproject.api.RetrofitClient.getInstance(this)
                        .create(com.example.uberproject.api.PanicApi.class);
        panicApi.handlePanic(panicId).enqueue(new retrofit2.Callback<com.example.uberproject.dto.response.PanicResponseDTO>() {
            @Override
            public void onResponse(@androidx.annotation.NonNull retrofit2.Call<com.example.uberproject.dto.response.PanicResponseDTO> call,
                                   @androidx.annotation.NonNull retrofit2.Response<com.example.uberproject.dto.response.PanicResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Panic #" + panicId + " handled successfully", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e("MainActivity", "Handle panic failed: " + response.code());
                    Toast.makeText(MainActivity.this, "Could not mark as handled: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull retrofit2.Call<com.example.uberproject.dto.response.PanicResponseDTO> call,
                                  @androidx.annotation.NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markPanicVehicleOnMap(double lat, double lon, String title) {
        try {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (current instanceof HomeFragment) {
                Fragment mapFrag = current.getChildFragmentManager()
                        .findFragmentById(R.id.map_fragment_container);
                if (mapFrag instanceof com.example.uberproject.fragments.map.MapFragment) {
                    ((com.example.uberproject.fragments.map.MapFragment) mapFrag)
                            .markVehiclePanic(lat, lon, title);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Could not mark panic on map", e);
        }
    }

    // ======= END PANIC =======

    // Novo: Proverava je li token istekao
    private void checkTokenExpiration() {
        TokenManager tokenManager = TokenManager.getInstance(this);

        // Proverava samo ako je korisnik ulogovan
        if (AuthGuard.isUserLoggedIn(this)) {
            if (tokenManager.isTokenExpired()) {
                // Token je istekao - odjavi korisnika
                Toast.makeText(this, "Your session has expired. Please login again.", Toast.LENGTH_LONG).show();
                AuthGuard.logout(this);
                invalidateOptionsMenu();
                NavigationView navigationView = findViewById(R.id.nav_view);
                if (navigationView != null) {
                    updateNavigationMenuVisibility(navigationView);
                }
                showLoginFragment();
            }
        }
    }

    // poziva se nakon uspešnog Logina
    public void onLoginSuccess() {
        invalidateOptionsMenu();
        NavigationView nav = findViewById(R.id.nav_view);
        updateNavigationMenuVisibility(nav);
        connectPanicWebSocketIfAdmin();
        checkForActiveRide();
        connectNotificationSocket();
    }

    private void connectNotificationSocket() {
        if (notificationWsClient != null && notificationWsClient.isConnected()) return;

        String email = TokenManager.getInstance(this).getUserEmail();
        if (email == null || email.isEmpty()) return;

        notificationWsClient = new NotificationWebSocketClient(BuildConfig.WS_HOST);
        notificationWsClient.setListener(new NotificationWebSocketClient.NotificationListener() {

            @Override
            public void onRideFinished(int rideId, String message) {
                showRideNotification("🏁 Ride Finished", message);
                broadcastRideEvent("RIDE_FINISHED", rideId);
            }

            @Override
            public void onRideAccepted(int rideId, String driverName, String vehicle, String message) {
                // message vec sadrzi kontekst - da li je obicni passenger ili coPassenger
                // "Your ride has been accepted! Driver: ..." ili "You've been added to a ride! Driver: ..."
                showRideNotification("✅ Ride Accepted", message);
                broadcastRideEvent("RIDE_ACCEPTED", rideId);
            }

            @Override
            public void onRideRejected(String message) {
                showRideNotification("❌ No Drivers Available", message);
            }

            @Override
            public void onNewRideAssigned(int rideId, String from, String to, String passengerName, String message) {
                showRideNotification("🚗 New Ride Assigned", message);
                broadcastRideEvent("NEW_RIDE_ASSIGNED", rideId);
            }

            @Override
            public void onRideCancelled(int rideId, String message) {
                showRideNotification("❌ Ride Cancelled", message);
                broadcastRideEvent("RIDE_CANCELLED", rideId);
            }

            @Override
            public void onRideReminder(int rideId, String message) {
                showRideNotification("⏰ Ride Reminder", message);
            }

            @Override
            public void onConnected() {
                Log.d("MainActivity", "Notification WS connected");
            }

            @Override
            public void onDisconnected() {
                Log.d("MainActivity", "Notification WS disconnected");
            }
        });
        notificationWsClient.connect(email);
    }

    private void showRideNotification(String title, String message) {
        // Toast za kada je app u fokusu (foreground)
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
        // Sistemska notifikacija - vidljiva i kad je app u pozadini
        NotificationHelper.show(this, title, message);
    }

    // Broadcast prema trenutno aktivnom fragmentu
    private void broadcastRideEvent(String type, int rideId) {
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (current instanceof HomeFragment) {
            ((HomeFragment) current).onRideEventReceived(type, rideId);
        }
    }
}