package com.example.uberproject.fragments.driver;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.AssignedRidesAdapter;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.response.AssignedRideDTO;
import com.example.uberproject.dto.request.RideCancelRequestDTO;
import com.example.uberproject.dto.request.RideStopRequestDTO;
import com.example.uberproject.dto.response.RideStopResponseDTO;
import com.example.uberproject.dto.response.RideCancelResponseDTO;
import com.example.uberproject.fragments.tracking.TrackRideFragment;
import com.example.uberproject.model.Location;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DriverAssignedRidesFragment extends Fragment implements AssignedRidesAdapter.OnRideActionListener {

    private static final String TAG = "AssignedRidesFragment";

    private RecyclerView rvAssignedRides;
    private AssignedRidesAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private Button btnFilterToday, btnFilterNext7Days, btnFilterAll;

    private List<AssignedRideDTO> allRides = new ArrayList<>();
    private String currentFilter = "TODAY";
    private RideApi rideApi;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_assigned_rides, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupFilterButtons();

        rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        tokenManager = TokenManager.getInstance(requireContext());

        loadAssignedRides();

        return view;
    }

    private void initializeViews(View view) {
        rvAssignedRides = view.findViewById(R.id.rvAssignedRides);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        btnFilterToday = view.findViewById(R.id.btnFilterToday);
        btnFilterNext7Days = view.findViewById(R.id.btnFilterNext7Days);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
    }

    private void setupRecyclerView() {
        adapter = new AssignedRidesAdapter(this);
        rvAssignedRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAssignedRides.setAdapter(adapter);
    }

    private void setupFilterButtons() {
        btnFilterToday.setOnClickListener(v -> {
            currentFilter = "TODAY";
            updateFilterButtonStyles();
            filterRides();
        });

        btnFilterNext7Days.setOnClickListener(v -> {
            currentFilter = "NEXT_7_DAYS";
            updateFilterButtonStyles();
            filterRides();
        });

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtonStyles();
            filterRides();
        });
    }

    private void updateFilterButtonStyles() {
        // Reset all buttons to purple
        btnFilterToday.setBackgroundResource(R.drawable.button_purple_rounded);
        btnFilterToday.setTextColor(getResources().getColor(android.R.color.white, null));

        btnFilterNext7Days.setBackgroundResource(R.drawable.button_purple_rounded);
        btnFilterNext7Days.setTextColor(getResources().getColor(android.R.color.white, null));

        btnFilterAll.setBackgroundResource(R.drawable.button_purple_rounded);
        btnFilterAll.setTextColor(getResources().getColor(android.R.color.white, null));

        // Set selected button to yellow
        switch (currentFilter) {
            case "TODAY":
                btnFilterToday.setBackgroundResource(R.drawable.button_yellow_rounded);
                btnFilterToday.setTextColor(getResources().getColor(android.R.color.black, null));
                break;
            case "NEXT_7_DAYS":
                btnFilterNext7Days.setBackgroundResource(R.drawable.button_yellow_rounded);
                btnFilterNext7Days.setTextColor(getResources().getColor(android.R.color.black, null));
                break;
            case "ALL":
                btnFilterAll.setBackgroundResource(R.drawable.button_yellow_rounded);
                btnFilterAll.setTextColor(getResources().getColor(android.R.color.black, null));
                break;
        }
    }

    private void loadAssignedRides() {
        progressBar.setVisibility(View.VISIBLE);
        rvAssignedRides.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        String driverEmail = tokenManager.getUserEmail();
        if (driverEmail == null || driverEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User email not found", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            return;
        }

        rideApi.getDriverAssignedRides(driverEmail).enqueue(new Callback<List<AssignedRideDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<AssignedRideDTO>> call, @NonNull Response<List<AssignedRideDTO>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allRides = response.body();
                    filterRides();
                } else {
                    Log.e(TAG, "Error loading rides: " + response.code());
                    showEmptyState();
                    Toast.makeText(requireContext(), "Failed to load rides", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AssignedRideDTO>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState();
                Log.e(TAG, "Network error", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRides() {
        List<AssignedRideDTO> filteredRides = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar endOfToday = Calendar.getInstance();
        endOfToday.set(Calendar.HOUR_OF_DAY, 23);
        endOfToday.set(Calendar.MINUTE, 59);
        endOfToday.set(Calendar.SECOND, 59);

        Calendar next7Days = Calendar.getInstance();
        next7Days.add(Calendar.DAY_OF_YEAR, 7);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (AssignedRideDTO ride : allRides) {
            // Always include IN_PROGRESS and REQUESTED rides
            if ("IN_PROGRESS".equals(ride.getStatus()) || "REQUESTED".equals(ride.getStatus())) {
                filteredRides.add(ride);
                continue;
            }

            try {
                Date rideDate = sdf.parse(ride.getStartTime());
                if (rideDate == null) continue;

                switch (currentFilter) {
                    case "TODAY":
                        if (rideDate.after(today.getTime()) && rideDate.before(endOfToday.getTime())) {
                            filteredRides.add(ride);
                        }
                        break;
                    case "NEXT_7_DAYS":
                        if (rideDate.after(today.getTime()) && rideDate.before(next7Days.getTime())) {
                            filteredRides.add(ride);
                        }
                        break;
                    case "ALL":
                        if (rideDate.after(today.getTime())) {
                            filteredRides.add(ride);
                        }
                        break;
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date", e);
            }
        }

        if (filteredRides.isEmpty()) {
            showEmptyState();
        } else {
            rvAssignedRides.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.setRides(filteredRides);
        }
    }

    private void showEmptyState() {
        rvAssignedRides.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOpenRide(AssignedRideDTO ride) {
        if (ride.getRideId() != null) {
            TrackRideFragment trackFragment =
                    TrackRideFragment.newInstance(ride.getRideId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, trackFragment)
                    .addToBackStack(null)
                    .commit();
        }
        // TODO: Navigate to map or ride details
    }

    @Override
    public void onStartRide(AssignedRideDTO ride) {
        rideApi.startRide(ride.getRideId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ride started!", Toast.LENGTH_SHORT).show();
                    TrackRideFragment trackFragment = TrackRideFragment.newInstance(ride.getRideId());
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, trackFragment)
                            .addToBackStack(null)
                            .commit();
                    //loadAssignedRides();
                } else {
                    Toast.makeText(requireContext(), "Failed to start ride: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStopRide(AssignedRideDTO ride) {
        // Zakucane koordinate centra Novog Sada (tracking nije implementiran)
        Location endLocation = new Location(45.2517, 19.8369, ride.getEndLocation());
        String nowStr = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        RideStopRequestDTO stopRequest = new RideStopRequestDTO(endLocation, nowStr);

        rideApi.stopRide(ride.getId(), stopRequest).enqueue(new Callback<RideStopResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RideStopResponseDTO> call, @NonNull Response<RideStopResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Ride ended successfully", Toast.LENGTH_SHORT).show();
                    loadAssignedRides(); // Refresh the list
                } else {
                    Log.e(TAG, "Error ending ride: " + response.code());
                    Toast.makeText(requireContext(), "Failed to end ride", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideStopResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error ending ride", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEndRide(AssignedRideDTO ride) {
        // TODO: implementirati end ride
    }

    @Override
    public void onCancelRide(AssignedRideDTO ride) {
        // Create and show dialog for cancellation reason
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_ride, null);

        TextInputEditText etReason = dialogView.findViewById(R.id.etCancelReason);
        androidx.appcompat.widget.AppCompatButton btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        androidx.appcompat.widget.AppCompatButton btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        final AlertDialog dialog = builder
                .setView(dialogView)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String reason = etReason.getText().toString().trim();

            if (reason.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide a reason for cancellation", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create request with reason and send to API
            RideCancelRequestDTO cancelRequest = new RideCancelRequestDTO(reason);

            rideApi.cancelRide(ride.getId(), cancelRequest).enqueue(new Callback<RideCancelResponseDTO>() {
                @Override
                public void onResponse(@NonNull Call<RideCancelResponseDTO> call, @NonNull Response<RideCancelResponseDTO> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Ride cancelled successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadAssignedRides();
                    } else {
                        Log.e(TAG, "Error cancelling ride: " + response.code());
                        Toast.makeText(requireContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RideCancelResponseDTO> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error cancelling ride", t);
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
