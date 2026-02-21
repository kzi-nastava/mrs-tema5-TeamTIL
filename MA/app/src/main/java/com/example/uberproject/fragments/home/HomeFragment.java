package com.example.uberproject.fragments.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.request.RideEndRequestDTO;
import com.example.uberproject.dto.response.AssignedRideDTO;
import com.example.uberproject.dto.request.RideCancelRequestDTO;
import com.example.uberproject.dto.request.RideStopRequestDTO;
import com.example.uberproject.dto.response.RideEndResponseDTO;
import com.example.uberproject.dto.response.RideStopResponseDTO;
import com.example.uberproject.dto.response.RideCancelResponseDTO;
import com.example.uberproject.fragments.driver.DriverAssignedRidesFragment;
import com.example.uberproject.fragments.map.MapFragment;
import com.example.uberproject.fragments.tracking.TrackRideFragment;
import com.example.uberproject.model.Location;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private View activeRideCardView;
    private TextView tvActiveRideStatus;
    private TextView tvActiveRideTime;
    private TextView tvActiveRideLocations;
    private TextView tvActiveRidePrice;
    private TextView tvActiveRideDistance;
    private LinearLayout expandedDetailsLayout;
    private ImageView ivPassengerPhoto;
    private TextView tvPersonLabel;
    private TextView tvPassengerEmail;
    private TextView tvEstimatedEndTime;
    private TextView tvDuration;
    private LinearLayout actionButtonsLayout;
    private LinearLayout upcomingButtonsLayout;
    private Button btnOpenRide;
    private Button btnStopRide;
    private Button btnStartRide;
    private Button btnCancelRide;
    private Button btnEndRide;

    private boolean isExpanded = false;
    private AssignedRideDTO currentRide;
    private RideApi rideApi;
    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        activeRideCardView = view.findViewById(R.id.activeRideCardInclude);
        tvActiveRideStatus = view.findViewById(R.id.tvActiveRideStatus);
        tvActiveRideTime = view.findViewById(R.id.tvActiveRideTime);
        tvActiveRideLocations = view.findViewById(R.id.tvActiveRideLocations);
        tvActiveRidePrice = view.findViewById(R.id.tvActiveRidePrice);
        tvActiveRideDistance = view.findViewById(R.id.tvActiveRideDistance);
        expandedDetailsLayout = view.findViewById(R.id.expandedDetailsLayout);
        ivPassengerPhoto = view.findViewById(R.id.ivPassengerPhoto);
        tvPersonLabel = view.findViewById(R.id.tvPersonLabel);
        tvPassengerEmail = view.findViewById(R.id.tvPassengerEmail);
        tvEstimatedEndTime = view.findViewById(R.id.tvEstimatedEndTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        actionButtonsLayout = view.findViewById(R.id.actionButtonsLayout);
        upcomingButtonsLayout = view.findViewById(R.id.upcomingButtonsLayout);
        btnOpenRide = view.findViewById(R.id.btnOpenRide);
        btnStopRide = view.findViewById(R.id.btnStopRide);
        btnStartRide = view.findViewById(R.id.btnStartRide);
        btnCancelRide = view.findViewById(R.id.btnCancelRide);
        btnEndRide = view.findViewById(R.id.btnEndRide);

        rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);

        // Handle card click to expand/collapse
        activeRideCardView.setOnClickListener(v -> toggleExpanded());

        // Setup button listeners
        setupButtonListeners();

        loadActiveRideIfNeeded();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActiveRideIfNeeded();
    }

    private void loadActiveRideIfNeeded() {
        if (!AuthGuard.isUserLoggedIn(requireContext())) {
            hideCard();
            return;
        }

        String userRole = AuthGuard.getUserRole(requireContext());
        if (userRole == null) {
            hideCard();
            return;
        }

        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        String email = tokenManager.getUserEmail();
        RideApi rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);

        if ("DRIVER".equalsIgnoreCase(userRole)) {
            // For drivers, use the assigned rides endpoint
            if (email == null || email.isEmpty()) {
                hideCard();
                return;
            }
            rideApi.getDriverAssignedRides(email).enqueue(new Callback<List<AssignedRideDTO>>() {
                @Override
                public void onResponse(@NonNull Call<List<AssignedRideDTO>> call,
                                       @NonNull Response<List<AssignedRideDTO>> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        showActiveDriverRide(response.body());
                    } else {
                        hideCard();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<AssignedRideDTO>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    Log.e(TAG, "Failed to load driver assigned rides", t);
                    hideCard();
                }
            });

        } else if ("REGISTERED_USER".equalsIgnoreCase(userRole)) {
            // For users, use the dedicated active rides endpoint (IN_PROGRESS and REQUESTED)
            if (email == null || email.isEmpty()) {
                hideCard();
                return;
            }
            rideApi.getUserActiveRides(email, "IN_PROGRESS,REQUESTED").enqueue(new Callback<List<AssignedRideDTO>>() {
                @Override
                public void onResponse(@NonNull Call<List<AssignedRideDTO>> call,
                                       @NonNull Response<List<AssignedRideDTO>> response) {
                    if (!isAdded()) return;
                    Log.d(TAG, "User active rides response - Code: " + response.code() + ", Body: " + (response.body() != null ? response.body().size() : "null"));
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Successfully loaded " + response.body().size() + " user active rides");
                        showActiveDriverRide(response.body());
                    } else {
                        Log.e(TAG, "Failed to load user active rides - Code: " + response.code());
                        hideCard();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<AssignedRideDTO>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    Log.e(TAG, "Failed to load user active rides", t);
                    hideCard();
                }
            });

        } else {
            hideCard();
        }
    }

    private void showActiveDriverRide(List<AssignedRideDTO> rides) {
        // Find first IN_PROGRESS ride, or first REQUESTED if no in-progress
        AssignedRideDTO activeRide = null;
        AssignedRideDTO requestedRide = null;

        Log.d(TAG, "showActiveDriverRide: Received " + rides.size() + " rides");
        for (int i = 0; i < rides.size(); i++) {
            AssignedRideDTO ride = rides.get(i);
            Log.d(TAG, "Ride " + i + ": ID=" + ride.getRideId() + ", Status=" + ride.getStatus());
            Log.d(TAG, "  - passengerFirstName=" + ride.getPassengerFirstName());
            Log.d(TAG, "  - passengerEmail=" + ride.getPassengerEmail());
            Log.d(TAG, "  - driverFirstName=" + ride.getDriverFirstName());
            Log.d(TAG, "  - driverEmail=" + ride.getDriverEmail());
            Log.d(TAG, "  - accountEmail=" + ride.getAccountEmail());
        }

        for (AssignedRideDTO ride : rides) {
            if ("IN_PROGRESS".equalsIgnoreCase(ride.getStatus())) {
                activeRide = ride;
                break;
            }
            if ("REQUESTED".equalsIgnoreCase(ride.getStatus()) && requestedRide == null) {
                requestedRide = ride;
            }
        }

        if (activeRide == null) {
            activeRide = requestedRide;
        }

        if (activeRide == null) {
            Log.d(TAG, "No active or requested ride found");
            hideCard();
            hidePanicButton();
            return;
        }

        Log.d(TAG, "Using ride: ID=" + activeRide.getRideId() + ", Status=" + activeRide.getStatus());
        currentRide = activeRide;
        isExpanded = false;
        expandedDetailsLayout.setVisibility(View.GONE);

        populateCard(
                activeRide.getStatus(),
                activeRide.getStartTime(),
                activeRide.getStartLocation(),
                activeRide.getEndLocation(),
                activeRide.getPrice(),
                activeRide.getDistance()
        );

        // Show panic button immediately if ride is IN_PROGRESS (don't wait for expansion)
        if ("IN_PROGRESS".equalsIgnoreCase(activeRide.getStatus())) {
            showMapFragmentPanicButton();
        } else {
            hidePanicButton();
        }
    }

    private void populateCard(String status, String startTime,
                               String startLocation, String endLocation,
                               Double price, Double distance) {
        if (activeRideCardView == null) return;

        // Status label + badge color
        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            tvActiveRideStatus.setText("In Progress");
            tvActiveRideStatus.setBackgroundResource(R.drawable.status_in_progress_bg);
        } else if ("REQUESTED".equalsIgnoreCase(status)) {
            tvActiveRideStatus.setText("Requested");
            tvActiveRideStatus.setBackgroundResource(R.drawable.status_upcoming_bg);
        } else {
            tvActiveRideStatus.setText(status != null ? status : "");
            tvActiveRideStatus.setBackgroundResource(R.drawable.status_upcoming_bg);
        }

        // Time
        if (startTime != null && !startTime.isEmpty()) {
            try {
                SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                Date date = inputSdf.parse(startTime);
                tvActiveRideTime.setText(date != null ? outputSdf.format(date) : startTime);
            } catch (ParseException e) {
                tvActiveRideTime.setText(startTime);
            }
        } else {
            tvActiveRideTime.setText("");
        }

        // Locations
        String from = startLocation != null ? startLocation : "—";
        String to = endLocation != null ? endLocation : "—";
        tvActiveRideLocations.setText(from + " → " + to);

        // Price
        if (price != null) {
            tvActiveRidePrice.setText(Math.round(price) + " RSD");
        } else {
            tvActiveRidePrice.setText("—");
        }

        // Distance
        if (distance != null) {
            tvActiveRideDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        } else {
            tvActiveRideDistance.setText("");
        }

        activeRideCardView.setVisibility(View.VISIBLE);
    }

    private void hideCard() {
        if (activeRideCardView != null) {
            activeRideCardView.setVisibility(View.GONE);
        }
    }

    private void toggleExpanded() {
        if (currentRide == null) return;

        isExpanded = !isExpanded;
        if (isExpanded) {
            expandedDetailsLayout.setVisibility(View.VISIBLE);
            populateExpandedDetails();
        } else {
            expandedDetailsLayout.setVisibility(View.GONE);
        }
    }

    private void populateExpandedDetails() {
        if (currentRide == null) return;

        userRole = AuthGuard.getUserRole(requireContext());

        if ("DRIVER".equalsIgnoreCase(userRole)) {
            // Za drivera - prikazuj podatke o putniku
            showPassengerInfo();
            showDriverActionButtons();
        } else if ("REGISTERED_USER".equalsIgnoreCase(userRole)) {
            // Za običnog korisnika - prikazuj podatke o vozaču
            showDriverInfo();
            showUserActionButtons();
        }

        // Ride details (ista za obe uloge)
        tvEstimatedEndTime.setText(formatTime(currentRide.getEstimatedEndTime()));
        tvDuration.setText(String.format(Locale.getDefault(), "%.0f min", currentRide.getDuration() != null ? currentRide.getDuration() : 0));

        // Show panic button only for IN_PROGRESS rides
        if ("IN_PROGRESS".equalsIgnoreCase(currentRide.getStatus())) {
            showMapFragmentPanicButton();
        } else {
            hideMapFragmentPanicButton();
        }
    }

    private void showMapFragmentPanicButton() {
        try {
            // MapFragment je child fragment od HomeFragment, koristi getChildFragmentManager
            Fragment fragment = getChildFragmentManager().findFragmentById(R.id.map_fragment_container);
            if (fragment instanceof MapFragment && currentRide != null && currentRide.getRideId() != null) {
                MapFragment mapFragment = (MapFragment) fragment;
                // Use fixed coordinates for now - in real app, use actual location
                mapFragment.showPanicButton(currentRide.getRideId(), 45.2517, 19.8369);
                Log.d(TAG, "Panic button shown for ride: " + currentRide.getRideId());
            }
        } catch (Exception e) {
            Log.d(TAG, "Could not show panic button: " + e.getMessage());
        }
    }

    private void hideMapFragmentPanicButton() {
        try {
            Fragment fragment = getChildFragmentManager().findFragmentById(R.id.map_fragment_container);
            if (fragment instanceof MapFragment) {
                MapFragment mapFragment = (MapFragment) fragment;
                mapFragment.hidePanicButton();
                Log.d(TAG, "Panic button hidden");
            }
        } catch (Exception e) {
            Log.d(TAG, "Could not hide panic button: " + e.getMessage());
        }
    }

    private void hidePanicButton() {
        hideMapFragmentPanicButton();
    }

    private void showPassengerInfo() {
        // Za drivera - prikazuj podatke o putniku
        if (tvPersonLabel != null) tvPersonLabel.setText("Passenger");
        String passengerName = (currentRide.getPassengerFirstName() != null ? currentRide.getPassengerFirstName() : "")
                + " " + (currentRide.getPassengerLastName() != null ? currentRide.getPassengerLastName() : "");

        Log.d(TAG, "showPassengerInfo - passengerFirstName: " + currentRide.getPassengerFirstName());
        Log.d(TAG, "showPassengerInfo - passengerLastName: " + currentRide.getPassengerLastName());
        Log.d(TAG, "showPassengerInfo - passengerEmail: " + currentRide.getPassengerEmail());
        Log.d(TAG, "showPassengerInfo - accountEmail: " + currentRide.getAccountEmail());

        if (passengerName.trim().isEmpty()) {
            tvPassengerEmail.setText(currentRide.getPassengerEmail() != null ? currentRide.getPassengerEmail() : currentRide.getAccountEmail());
        } else {
            tvPassengerEmail.setText(passengerName.trim());
        }

        // Load passenger profile picture
        if (currentRide.getPassengerProfilePictureUrl() != null && !currentRide.getPassengerProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentRide.getPassengerProfilePictureUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(ivPassengerPhoto);
        } else {
            ivPassengerPhoto.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    private void showDriverInfo() {
        // Za korisnika - prikazuj podatke o vozaču
        if (tvPersonLabel != null) tvPersonLabel.setText("Driver");
        String driverName = (currentRide.getDriverFirstName() != null ? currentRide.getDriverFirstName() : "")
                + " " + (currentRide.getDriverLastName() != null ? currentRide.getDriverLastName() : "");

        Log.d(TAG, "showDriverInfo - driverFirstName: " + currentRide.getDriverFirstName());
        Log.d(TAG, "showDriverInfo - driverLastName: " + currentRide.getDriverLastName());
        Log.d(TAG, "showDriverInfo - driverEmail: " + currentRide.getDriverEmail());
        Log.d(TAG, "showDriverInfo - accountEmail: " + currentRide.getAccountEmail());

        if (driverName.trim().isEmpty()) {
            tvPassengerEmail.setText(currentRide.getDriverEmail() != null ? currentRide.getDriverEmail() : "Driver");
        } else {
            tvPassengerEmail.setText(driverName.trim());
        }

        // Load driver profile picture
        if (currentRide.getDriverProfilePictureUrl() != null && !currentRide.getDriverProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentRide.getDriverProfilePictureUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(ivPassengerPhoto);
        } else {
            ivPassengerPhoto.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    private void showDriverActionButtons() {
        // Za drivera - prikazuj driver buttons ovisno od statusa
        btnStartRide.setText("Start ride");
        btnStartRide.setVisibility(View.VISIBLE);
        // Sakrij Open Ride jer je za korisnika
        btnOpenRide.setVisibility(View.GONE);

        if ("IN_PROGRESS".equalsIgnoreCase(currentRide.getStatus())) {
            actionButtonsLayout.setVisibility(View.VISIBLE);
            upcomingButtonsLayout.setVisibility(View.GONE);
        } else if ("REQUESTED".equalsIgnoreCase(currentRide.getStatus())) {
            actionButtonsLayout.setVisibility(View.GONE);
            upcomingButtonsLayout.setVisibility(View.VISIBLE);
            btnCancelRide.setVisibility(View.VISIBLE);
        }
    }

    private void showUserActionButtons() {
        // Za korisnika - prikazuj samo Open Ride i/ili Cancel Ride
        actionButtonsLayout.setVisibility(View.GONE);
        upcomingButtonsLayout.setVisibility(View.VISIBLE);

        if ("IN_PROGRESS".equalsIgnoreCase(currentRide.getStatus())) {
            // Prikaži Open Ride i Cancel Ride
            btnStartRide.setVisibility(View.VISIBLE);
            btnStartRide.setText("Open ride");
            btnCancelRide.setVisibility(View.VISIBLE);
        } else {
            // REQUESTED - samo Cancel Ride
            btnStartRide.setVisibility(View.GONE);
            btnCancelRide.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtonListeners() {
        btnOpenRide.setOnClickListener(v -> {
            if (currentRide != null) {
                Toast.makeText(requireContext(), "Opening ride details...", Toast.LENGTH_SHORT).show();
                // Implementacija za otvaranje ride detalja
            }
        });

        btnStopRide.setOnClickListener(v -> {
            if (currentRide != null) {
                showStopRideDialog();
            }
        });

        btnStartRide.setOnClickListener(v -> {
            if (currentRide == null) return;
            userRole = AuthGuard.getUserRole(requireContext());

            if ("DRIVER".equalsIgnoreCase(userRole)) {
                // Pozovi backend da startuje vožnju (ovo pokreće simulaciju!)
                rideApi.startRide(currentRide.getRideId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Ride started!", Toast.LENGTH_SHORT).show();
                            TrackRideFragment trackFragment = TrackRideFragment.newInstance(currentRide.getRideId());
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, trackFragment)
                                    .addToBackStack(null)
                                    .commit();
                            //loadActiveRideIfNeeded(); // Refresh kartice
                        } else {
                            Toast.makeText(requireContext(), "Failed to start ride (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else if ("REGISTERED_USER".equalsIgnoreCase(userRole)) {
                // Korisnik otvara TrackRide
                if (currentRide.getRideId() != null) {
                    TrackRideFragment trackFragment = TrackRideFragment.newInstance(currentRide.getRideId());
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, trackFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        btnCancelRide.setOnClickListener(v -> {
            if (currentRide != null) {
                showCancelRideDialog();
            }
        });

        btnEndRide.setOnClickListener( v -> {
            if(currentRide != null){
                RideEndRequestDTO request = new RideEndRequestDTO();
                request.setActualEndLocation(new Location(45.2671, 19.8335, "Trg slobode 1, Novi Sad"));

                RideApi api = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
                api.endRide(currentRide.getId(), request).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RideEndResponseDTO> call, @NonNull Response<RideEndResponseDTO> response) {
                        RideEndResponseDTO body = response.body();

                        if (body != null) {
                            hideCard();
                            hidePanicButton();
                            if (body.getHasNextRide()) {
                                Toast.makeText(getContext(), "Ride ended successfully! Next ride: " + body.getNextRideFrom()
                                        + " -> " + body.getNextRideTo() + " at " + body.getNextRideScheduledTime(), Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new DriverAssignedRidesFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                Toast.makeText(getContext(), "Ride ended successfully! Price: " + body.getFinalPrice()
                                        + " RSD, Duration: " + body.getDuration() + " at " + body.getNextRideScheduledTime(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error ending ride: " + response.code());
                            Toast.makeText(requireContext(), "Failed to end ride", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RideEndResponseDTO> call, Throwable throwable) {
                        Log.e(TAG, "Report network error", throwable);
                        Toast.makeText(getContext(), "Network error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showStopRideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Stop Ride");
        builder.setMessage("Are you sure you want to stop this ride?");
        builder.setPositiveButton("Yes", (dialog, which) -> stopRide());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void showCancelRideDialog() {
        userRole = AuthGuard.getUserRole(requireContext());

        if ("DRIVER".equalsIgnoreCase(userRole)) {
            // Driver mora unijeti razlog - prikaži dialog s poljem za unos
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_ride, null);

            TextInputEditText etReason = dialogView.findViewById(R.id.etCancelReason);
            androidx.appcompat.widget.AppCompatButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
            androidx.appcompat.widget.AppCompatButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

            final AlertDialog dialog = builder
                    .setView(dialogView)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            btnDialogCancel.setOnClickListener(v -> dialog.dismiss());

            btnDialogConfirm.setOnClickListener(v -> {
                String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
                if (reason.isEmpty()) {
                    Toast.makeText(requireContext(), "Please provide a reason for cancellation", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                cancelRideWithReason(reason);
            });

            dialog.show();
        } else {
            // Korisnik - direktno otkazivanje bez unosa razloga
            cancelRideWithReason("User cancelled");
        }
    }

    private void stopRide() {
        if (currentRide == null) return;

        // Fixed coordinates for Novi Sad center (tracking not implemented)
        Location endLocation = new Location(45.2517, 19.8369, currentRide.getEndLocation());
        String nowStr = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
        RideStopRequestDTO request = new RideStopRequestDTO(endLocation, nowStr);

        rideApi.stopRide(currentRide.getRideId(), request).enqueue(new Callback<RideStopResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RideStopResponseDTO> call, @NonNull Response<RideStopResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ride stopped successfully", Toast.LENGTH_SHORT).show();
                    loadActiveRideIfNeeded();
                } else {
                    Toast.makeText(requireContext(), "Failed to stop ride", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideStopResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Failed to stop ride", t);
                Toast.makeText(requireContext(), "Error stopping ride", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRideWithReason(String reason) {
        if (currentRide == null) {
            Toast.makeText(requireContext(), "No ride selected", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ERROR: currentRide is null!");
            return;
        }

        if (currentRide.getRideId() == null) {
            Toast.makeText(requireContext(), "Ride ID is null", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ERROR: currentRide.getRideId() is null!");
            return;
        }

        RideCancelRequestDTO request = new RideCancelRequestDTO(reason);

        Log.d(TAG, "========================================");
        Log.d(TAG, "CANCEL RIDE REQUEST DETAILS:");
        Log.d(TAG, "Ride ID: " + currentRide.getRideId());
        Log.d(TAG, "Reason: " + reason);
        Log.d(TAG, "Request.CancellationReason: " + request.getCancellationReason());
        Log.d(TAG, "Ride Status: " + currentRide.getStatus());
        Log.d(TAG, "Passenger Email: " + currentRide.getPassengerEmail());
        Log.d(TAG, "Account Email: " + currentRide.getAccountEmail());
        Log.d(TAG, "========================================");

        rideApi.cancelRide(currentRide.getRideId(), request).enqueue(new Callback<RideCancelResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RideCancelResponseDTO> call, @NonNull Response<RideCancelResponseDTO> response) {
                if (!isAdded()) return;
                Log.d(TAG, "Cancel ride response - Code: " + response.code() + ", IsSuccessful: " + response.isSuccessful());
                if (response.isSuccessful()) {
                    RideCancelResponseDTO body = response.body();
                    Log.d(TAG, "Cancel successful - Response: " + (body != null ? body.getMessage() : "null body"));
                    Toast.makeText(requireContext(), "Ride cancelled successfully", Toast.LENGTH_SHORT).show();
                    loadActiveRideIfNeeded();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Cancel ride failed - Code: " + response.code() + ", Error: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(requireContext(), "Failed to cancel ride (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideCancelResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Failed to cancel ride", t);
                Toast.makeText(requireContext(), "Error cancelling ride: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTime(String time) {
        if (time == null || time.isEmpty()) {
            return "—";
        }
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputSdf.parse(time);
            return date != null ? outputSdf.format(date) : time;
        } catch (ParseException e) {
            return time;
        }
    }
}
