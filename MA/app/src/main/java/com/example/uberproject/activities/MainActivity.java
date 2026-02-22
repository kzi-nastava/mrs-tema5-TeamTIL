package com.example.uberproject.activities;

import static com.example.uberproject.utils.NotificationHelper.CHANNEL_RIDES;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.example.uberproject.fragments.forms.LoginFragment;
import com.example.uberproject.fragments.forms.NewPasswordFragment;
import com.example.uberproject.fragments.forms.ProfileFragment;
import com.example.uberproject.fragments.forms.RegisterFragment;
import com.example.uberproject.fragments.forms.ResetPasswordFragment;
import com.example.uberproject.fragments.forms.RideBookingFragment;
import com.example.uberproject.fragments.home.HomeFragment;
import com.example.uberproject.fragments.support.SupportChatsFragment;
import com.example.uberproject.fragments.tracking.TrackRideFragment;
import com.example.uberproject.fragments.user.RideStatsFragment;
import com.example.uberproject.fragments.user.UserRideHistoryFragment;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.NotificationHelper;
import com.example.uberproject.utils.TokenManager;
import com.example.uberproject.websocket.PanicWebSocketManager;
import com.example.uberproject.websocket.RideWebSocketManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private PanicWebSocketManager panicWebSocketManager;
    private RideWebSocketManager rideWebSocketManager;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 200;

    private Integer activeRideId = null;

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

        // kreiraj notification kanale pre svake notif
        NotificationHelper.createNotificationChannels(this);

        // trazi POST_NOTIFICATIONS permisiju
        requestNotificationPermissionIfNeeded();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

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

            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (itemId == R.id.book_an_uber) {
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
            } else if (itemId == R.id.nav_register_driver) {
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
            } else if (itemId == R.id.nav_logout) {
                disconnectPanicWebSocket();
                disconnectRideWebSocket();
                AuthGuard.logout(this);
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                updateNavigationMenuVisibility(navigationView);
                showLoginFragment();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        handleDeepLink(getIntent());
        updateNavigationMenuVisibility(navigationView);
        connectPanicWebSocketIfAdmin();
        handleNavigationExtra(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
        handleNavigationExtra(intent);
    }

    private void handleNavigationExtra(Intent intent) {
        if (intent == null) return;
        String nav = intent.getStringExtra(NotificationHelper.EXTRA_NAVIGATE_TO);
        if (NotificationHelper.NAV_RIDE_HISTORY.equals(nav)) {
            loadFragment(new UserRideHistoryFragment());
            intent.removeExtra(NotificationHelper.EXTRA_NAVIGATE_TO); // da se ne ponovi
        }
    }

    // ======= RIDE WEBSOCKET =======

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    public void connectRideWebSocket() {
        String userEmail = TokenManager.getInstance(this).getUserEmail();
        if (userEmail == null) return;

        String wsBaseUrl = BuildConfig.WS_HOST;

        rideWebSocketManager = RideWebSocketManager.getInstance();
        rideWebSocketManager.setListener(new RideWebSocketManager.RideNotificationListener() {

            @Override
            public void onRideAccepted(RideWebSocketManager.RideAcceptedNotification n) {
                // message polje vec sadrzi razliku passenger vs coPassenger
                String body = n.message != null ? n.message :
                        "Your driver " + n.driverName + " is on the way.";
                NotificationHelper.show(MainActivity.this, CHANNEL_RIDES, 1001, "Ride Accepted!", body);
                runOnUiThread(() -> broadcastRideEvent("RIDE_ACCEPTED", n.rideId != null ? n.rideId : -1));
            }

            @Override
            public void onRideRejected(RideWebSocketManager.RideRejectedNotification n) {
                NotificationHelper.showRideRejected(MainActivity.this);
            }

            @Override
            public void onRideReminder(RideWebSocketManager.RideReminderNotification n) {
                long minutes = extractMinutesFromMessage(n.message);
                NotificationHelper.showRideReminder(
                        MainActivity.this,
                        minutes,
                        n.from != null ? n.from : "",
                        n.to   != null ? n.to   : ""
                );
            }

            @Override
            public void onNewRideAssigned(RideWebSocketManager.NewRideAssignedNotification n) {
                NotificationHelper.showNewRideAssigned(MainActivity.this,
                        n.passengerName != null ? n.passengerName : "",
                        n.from != null ? n.from : "",
                        n.to != null ? n.to : "");
                runOnUiThread(() -> broadcastRideEvent("NEW_RIDE_ASSIGNED", n.rideId != null ? n.rideId : -1));
            }

            @Override
            public void onRideFinished(RideWebSocketManager.RideFinishedNotification n) {
                NotificationHelper.showRideFinished(MainActivity.this,
                        n.from != null ? n.from : "",
                        n.to != null ? n.to : "",
                        n.price);
                runOnUiThread(() -> broadcastRideEvent("RIDE_FINISHED", n.rideId != null ? n.rideId : -1));
            }

            @Override
            public void onRideCancelled(RideWebSocketManager.RideCancelledNotification n) {
                NotificationHelper.showRideCancelled(MainActivity.this);
                runOnUiThread(() -> broadcastRideEvent("RIDE_CANCELLED", n.rideId != null ? n.rideId : -1));
            }

            @Override
            public void onRideStopped(RideWebSocketManager.RideStoppedNotification n) {
                NotificationHelper.showRideStopped(MainActivity.this);
                runOnUiThread(() -> broadcastRideEvent("RIDE_FINISHED", n.rideId != null ? n.rideId : -1));
            }

            @Override
            public void onConnectionEstablished() {
                android.util.Log.d("MainActivity", "Ride WebSocket connected");
            }

            @Override
            public void onConnectionLost() {
                android.util.Log.d("MainActivity", "Ride WebSocket disconnected");
            }
        });

        rideWebSocketManager.connect(wsBaseUrl, userEmail);
    }

    private long extractMinutesFromMessage(String message) {
        if (message == null) return 0;
        try {
            // Poruka: "Reminder: Your ride starts in 15 minutes!"
            String[] parts = message.split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i + 1].startsWith("minute")) {
                    return Long.parseLong(parts[i]);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void disconnectRideWebSocket() {
        if (rideWebSocketManager != null) {
            rideWebSocketManager.disconnect();
            rideWebSocketManager = null;
        }
    }

    // ======= END RIDE WEBSOCKET =======

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
        checkTokenExpiration();
        connectPanicWebSocketIfAdmin();
        checkForActiveRide();
        // Konektuj ride WebSocket ako je korisnik ulogovan i WebSocket nije aktivan
        if (TokenManager.getInstance(this).hasToken()) {
            if (rideWebSocketManager == null || !rideWebSocketManager.isConnected()) {
                connectRideWebSocket();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectPanicWebSocket();
        disconnectRideWebSocket();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        MenuItem registerItem = menu.findItem(R.id.nav_register);
        MenuItem profileItem = menu.findItem(R.id.nav_profile);

        boolean isLoggedIn = AuthGuard.isUserLoggedIn(this);

        if (loginItem != null) loginItem.setVisible(!isLoggedIn);
        if (registerItem != null) registerItem.setVisible(!isLoggedIn);
        if (profileItem != null) profileItem.setVisible(isLoggedIn);

        if (isLoggedIn && profileItem != null) {
            updateToolbarProfile(profileItem);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void updateNavigationMenuVisibility(NavigationView navigationView) {
        if (AuthGuard.isUserLoggedIn(this)) {
            TokenManager tokenManager = TokenManager.getInstance(this);
            if (tokenManager.isTokenExpired()) {
                AuthGuard.logout(this);
            }
        }

        boolean isLoggedIn = AuthGuard.isUserLoggedIn(this);
        String userRole = AuthGuard.getUserRole(this);

        Menu menu = navigationView.getMenu();
        hideAllMenuItems(menu);
        showItem(menu, R.id.nav_home);

        MenuItem logoutItem = menu.findItem(R.id.nav_logout);
        if (logoutItem != null) logoutItem.setVisible(isLoggedIn);

        if (!isLoggedIn) {
            showMenuGroup(menu, "REGISTERED_USER");
        } else if ("REGISTERED_USER".equalsIgnoreCase(userRole)) {
            showMenuGroup(menu, "REGISTERED_USER");
        } else if ("DRIVER".equalsIgnoreCase(userRole)) {
            showMenuGroup(menu, "DRIVER");
        } else if ("ADMIN".equalsIgnoreCase(userRole) || "ADMINISTRATOR".equalsIgnoreCase(userRole)) {
            showMenuGroup(menu, "ADMIN");
        } else {
            showMenuGroup(menu, "REGISTERED_USER");
        }
    }

    private void hideAllMenuItems(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
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
        if (item != null) item.setVisible(true);
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
            tvUsername.setText(email.split("@")[0]);
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
            loginSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)),
                    0, loginSpan.length(), 0);
            loginItem.setTitle(loginSpan);
        }

        MenuItem registerItem = menu.findItem(R.id.nav_register);
        if (registerItem != null) {
            SpannableString registerSpan = new SpannableString(registerItem.getTitle());
            registerSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)),
                    0, registerSpan.length(), 0);
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

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri data = intent.getData();
        String path = data.getPath();
        if (path == null) return;

        if (path.contains("/new-password")) {
            String token = data.getQueryParameter("token");
            if (token != null && !token.isEmpty()) openNewPasswordFragment(token);
        }

        if (path.contains("/reset-password")) {
            String token = data.getQueryParameter("token");
            if (token != null && !token.isEmpty()) openResetPasswordFragment(token);
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
        if (toolbar != null) toolbar.setVisibility(View.GONE);
    }

    public void showToolbar() {
        if (toolbar != null) toolbar.setVisibility(View.VISIBLE);
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
        if (navigationView != null) updateNavigationMenuVisibility(navigationView);
    }

    // ======= PANIC WEBSOCKET (ADMIN ONLY) =======

    public void connectPanicWebSocketIfAdmin() {
        String userRole = AuthGuard.getUserRole(this);
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"ADMINISTRATOR".equalsIgnoreCase(userRole)) return;

        String email = TokenManager.getInstance(this).getUserEmail();
        if (email == null || email.isEmpty()) return;

        if (panicWebSocketManager != null && panicWebSocketManager.isConnected()) return;

        String apiHost = BuildConfig.API_HOST;
        String baseWsUrl = apiHost
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                .replaceAll("/api/?$", "")
                .replaceAll("/$", "");

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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
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
                    if (panicId != null) handlePanicFromPopup(panicId);
                })
                .setNegativeButton("Dismiss", null)
                .setNeutralButton("View All Panics", (dialog, which) -> loadFragment(new AdminPanicListFragment()))
                .show();
    }

    private void handlePanicFromPopup(Integer panicId) {
        com.example.uberproject.api.PanicApi panicApi =
                RetrofitClient.getInstance(this).create(com.example.uberproject.api.PanicApi.class);
        panicApi.handlePanic(panicId).enqueue(new retrofit2.Callback<com.example.uberproject.dto.response.PanicResponseDTO>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.example.uberproject.dto.response.PanicResponseDTO> call,
                                   @NonNull retrofit2.Response<com.example.uberproject.dto.response.PanicResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Panic #" + panicId + " handled successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Could not mark as handled: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.example.uberproject.dto.response.PanicResponseDTO> call,
                                  @NonNull Throwable t) {
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

    private void checkTokenExpiration() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        if (AuthGuard.isUserLoggedIn(this)) {
            if (tokenManager.isTokenExpired()) {
                Toast.makeText(this, "Your session has expired. Please login again.", Toast.LENGTH_LONG).show();
                AuthGuard.logout(this);
                invalidateOptionsMenu();
                NavigationView navigationView = findViewById(R.id.nav_view);
                if (navigationView != null) updateNavigationMenuVisibility(navigationView);
                showLoginFragment();
            }
        }
    }

    // Poziva se nakon uspesnog logina
    public void onLoginSuccess() {
        invalidateOptionsMenu();
        NavigationView nav = findViewById(R.id.nav_view);
        updateNavigationMenuVisibility(nav);
        connectPanicWebSocketIfAdmin();
        connectRideWebSocket();
        checkForActiveRide();
    }

    private void broadcastRideEvent(String type, int rideId) {
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (current instanceof HomeFragment) {
            ((HomeFragment) current).onRideEventReceived(type, rideId);
        }
    }
}
