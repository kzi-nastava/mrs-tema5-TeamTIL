package com.example.uberproject.fragments.user;

import android.app.DatePickerDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.RideAdapter;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.api.RouteApi;
import com.example.uberproject.dto.response.AddToFavoritesResponseDTO;
import com.example.uberproject.dto.response.FavoriteRouteDTO;
import com.example.uberproject.dto.response.RideHistoryResponseDTO;
import com.example.uberproject.model.Ride;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRideHistoryFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "UserRideHistoryFragment";

    // Shake detection constants
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final long SHAKE_COOLDOWN_MS = 1000;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private boolean sortAscending = false; // default: newest first (DESC)

    private TextView tvSortIndicator;

    private androidx.appcompat.widget.AppCompatButton btnApplyFilters;
    private android.widget.AutoCompleteTextView etFromDate, etToDate;
    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> allRides = new ArrayList<>();
    private List<RideHistoryResponseDTO> rideHistoryData = new ArrayList<>();
    private Set<Integer> favoriteRouteIds = new HashSet<>();
    private Chip chipLast7Days, chipLastMonth, chipCompletedOnly, chipCanceledOnly, chipAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_ride_history, container, false);

        // Sort indicator label
        tvSortIndicator = view.findViewById(R.id.tvSortIndicator);

        etFromDate = view.findViewById(R.id.etFromDate);
        etToDate   = view.findViewById(R.id.etToDate);
        setupDatePickers();

        view.findViewById(R.id.fromLayout).setOnClickListener(v -> etFromDate.performClick());
        view.findViewById(R.id.toLayout).setOnClickListener(v -> etToDate.performClick());

        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnApplyFilters.setOnClickListener(v -> applyFilters());

        view.findViewById(R.id.btnResetIcon).setOnClickListener(v -> resetFilters());

        chipLast7Days    = view.findViewById(R.id.chipLast7Days);
        chipLastMonth    = view.findViewById(R.id.chipLastMonth);
        chipCompletedOnly= view.findViewById(R.id.chipCompletedOnly);
        chipCanceledOnly = view.findViewById(R.id.chipCanceledOnly);
        chipAll          = view.findViewById(R.id.chipAll);
        setChipListeners();

        ridesRecyclerView = view.findViewById(R.id.ridesRecyclerView);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        rideAdapter = new RideAdapter(allRides, ride -> {
            UserRideDetailsFragment fragment = UserRideDetailsFragment.newInstance(ride);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // postavi lisener za zvezdice
        rideAdapter.setFavoriteListener(new RideAdapter.OnFavoriteClickListener() {
            @Override
            public void onAddToFavorites(Integer routeId) {
                addToFavorites(routeId);
            }

            @Override
            public void onRemoveFromFavorites(Integer routeId) {
                removeFromFavorites(routeId);
            }
        });

        ridesRecyclerView.setAdapter(rideAdapter);

        // Init shake sensor
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // ucitaj ride history i favorite paralelno
        loadRideHistory();
        loadFavoriteRouteIds();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // ========= SHAKE SENSOR =========

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Remove gravity component and compute total acceleration
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

        if (acceleration > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > SHAKE_COOLDOWN_MS) {
                lastShakeTime = now;
                toggleSortOrder();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void toggleSortOrder() {
        sortAscending = !sortAscending;

        // Simply reverse the list
        Collections.reverse(allRides);

        // Check if any filters are active
        String fromDateStr = etFromDate != null ? etFromDate.getText().toString().trim() : "";
        String toDateStr   = etToDate   != null ? etToDate.getText().toString().trim()   : "";
        boolean hasFilters = !fromDateStr.isEmpty() || !toDateStr.isEmpty()
                || chipCompletedOnly.isSelected() || chipCanceledOnly.isSelected()
                || chipLast7Days.isSelected() || chipLastMonth.isSelected();

        if (hasFilters) {
            // Re-apply filters on the reversed list
            applyFilters();
        } else {
            // No filters - show all reversed rides
            rideAdapter.setRidesSorted(new ArrayList<>(allRides));
        }

        String label = sortAscending ? "↑ Oldest first" : "↓ Newest first";
        if (tvSortIndicator != null) {
            tvSortIndicator.setText("📅 " + label);
        }

        // Haptic feedback
        try {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(80);
            }
        } catch (Exception ignored) {}

        Toast.makeText(requireContext(), "Sorted: " + label, Toast.LENGTH_SHORT).show();
    }

    private void sortRidesByDate(List<Ride> rides) {
        // No longer needed - we just reverse the list
    }

    // ================================

    // ride history
    private void loadRideHistory() {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        String userEmail = tokenManager.getUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        RideApi rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        rideApi.getUserRideHistory(userEmail).enqueue(new Callback<List<RideHistoryResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<RideHistoryResponseDTO>> call,
                                   @NonNull Response<List<RideHistoryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rideHistoryData = response.body();
                    convertToRideModel(rideHistoryData);
                    rideAdapter.setRides(allRides);
                    // primijeni favourite stanje ako je vec ucitano
                    rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                } else {
                    Log.e(TAG, "Error loading ride history: " + response.code());
                    Toast.makeText(getContext(), "Failed to load ride history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RideHistoryResponseDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // fav routes
    private void loadFavoriteRouteIds() {
        RouteApi routeApi = RetrofitClient.getInstance(requireContext()).create(RouteApi.class);
        routeApi.getFavoriteRoutes().enqueue(new Callback<List<FavoriteRouteDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoriteRouteDTO>> call,
                                   @NonNull Response<List<FavoriteRouteDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteRouteIds.clear();
                    for (FavoriteRouteDTO fav : response.body()) {
                        if (fav.getRouteId() != null) {
                            favoriteRouteIds.add(fav.getRouteId());
                        }
                    }
                    // obavesti adapter da osvezi zvezdice
                    rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                    Log.d(TAG, "Loaded " + favoriteRouteIds.size() + " favorite routes");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FavoriteRouteDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load favorites: " + t.getMessage());
            }
        });
    }

    // dodaj u omiljene
    private void addToFavorites(Integer routeId) {
        RouteApi routeApi = RetrofitClient.getInstance(requireContext()).create(RouteApi.class);
        routeApi.addToFavorites(routeId).enqueue(new Callback<AddToFavoritesResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<AddToFavoritesResponseDTO> call,
                                   @NonNull Response<AddToFavoritesResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Added to favorites ★", Toast.LENGTH_SHORT).show();
                    favoriteRouteIds.add(routeId);
                    rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                } else {
                    // rollback - ukloni iz lokalnog seta jer API nije uspeo
                    favoriteRouteIds.remove(routeId);
                    rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                    Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddToFavoritesResponseDTO> call, @NonNull Throwable t) {
                favoriteRouteIds.remove(routeId);
                rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ukloni iz omiljenih
    private void removeFromFavorites(Integer routeId) {
        RouteApi routeApi = RetrofitClient.getInstance(requireContext()).create(RouteApi.class);
        routeApi.removeFromFavorites(routeId).enqueue(new Callback<AddToFavoritesResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<AddToFavoritesResponseDTO> call,
                                   @NonNull Response<AddToFavoritesResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    favoriteRouteIds.remove(routeId);
                    rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                } else {
                    // rollback
                    favoriteRouteIds.add(routeId);
                    rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                    Toast.makeText(getContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddToFavoritesResponseDTO> call, @NonNull Throwable t) {
                favoriteRouteIds.add(routeId);
                rideAdapter.setFavoriteRouteIds(favoriteRouteIds);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertToRideModel(List<RideHistoryResponseDTO> list) {
        allRides.clear();
        for (RideHistoryResponseDTO dto : list) {
            String price = dto.getPrice() != null
                    ? String.format("%.0f RSD", dto.getPrice()) : "N/A";
            String status = mapStatus(dto.getStatus());
            String dateTime = formatDateTime(dto.getStartTime());

           Ride ride = new Ride(dto.getId(), dto.getRouteId(),
                    dto.getStartLocation(), dto.getEndLocation(),
                    price, status, dateTime, dto.getPanicSent());

            // Driver
            ride.setDriverFirstName(dto.getDriverFirstName());
            ride.setDriverLastName(dto.getDriverLastName());
            ride.setDriverPhoneNumber(dto.getDriverPhoneNumber());
            ride.setDriverProfilePictureUrl(dto.getDriverProfilePictureUrl());
            // Passenger
            ride.setPassengerFirstName(dto.getPassengerFirstName());
            ride.setPassengerLastName(dto.getPassengerLastName());
            ride.setPassengerPhoneNumber(dto.getPassengerPhoneNumber());
            ride.setPassengerProfilePictureUrl(dto.getPassengerProfilePictureUrl());
            ride.setPassengerEmail(dto.getPassengerEmail());
            // Times & details
            ride.setStartTime(dto.getStartTime());
            ride.setEstimatedEndTime(dto.getEstimatedEndTime());
            ride.setDistance(dto.getDistance());
            ride.setDuration(dto.getDuration());
            // Vehicle
            ride.setVehicleModel(dto.getVehicleModel());
            ride.setVehicleLicensePlate(dto.getVehicleLicensePlate());

            allRides.add(ride);
        }
    }

    private String mapStatus(String backendStatus) {
        if (backendStatus == null) return "Unknown";
        switch (backendStatus.toUpperCase()) {
            case "FINISHED": return "Finished";
            case "CANCELED": return "Canceled";
            default:         return backendStatus;
        }
    }

    private String formatDateTime(String startTime) {
        if (startTime == null || startTime.isEmpty()) return "N/A";
        try {
            return startTime;
        } catch (Exception e) {
            return startTime;
        }
    }

    private void setChipListeners() {
        View.OnClickListener chipClickListener = v -> {
            chipLast7Days.setSelected(false);
            chipLastMonth.setSelected(false);
            chipCompletedOnly.setSelected(false);
            chipCanceledOnly.setSelected(false);
            chipAll.setSelected(false);
            ((Chip) v).setSelected(true);

            SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            etFromDate.setText("");
            etToDate.setText("");

            if (v == chipLast7Days) {
                etToDate.setText(fmt.format(new Date()));
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                etFromDate.setText(fmt.format(calendar.getTime()));
                applyFilters();
            } else if (v == chipLastMonth) {
                etToDate.setText(fmt.format(new Date()));
                calendar.add(Calendar.MONTH, -1);
                etFromDate.setText(fmt.format(calendar.getTime()));
                applyFilters();
            } else if (v == chipCompletedOnly || v == chipCanceledOnly) {
                applyFilters();
            } else if (v == chipAll) {
                // Show all in current sort order
                rideAdapter.setRides(new ArrayList<>(allRides));
            }
        };

        chipLast7Days.setOnClickListener(chipClickListener);
        chipLastMonth.setOnClickListener(chipClickListener);
        chipCompletedOnly.setOnClickListener(chipClickListener);
        chipCanceledOnly.setOnClickListener(chipClickListener);
        chipAll.setOnClickListener(chipClickListener);
    }

    private void applyFilters() {
        String fromDateStr = etFromDate.getText().toString().trim();
        String toDateStr   = etToDate.getText().toString().trim();

        Date fromDate = fromDateStr.isEmpty() ? null : parsePickerDate(fromDateStr);
        Date toDate   = toDateStr.isEmpty()   ? null : parsePickerDate(toDateStr);

        // If toDate is set, include the whole day (end of day)
        if (toDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            toDate = cal.getTime();
        }

        List<Ride> filtered = new ArrayList<>();
        for (Ride ride : allRides) {
            boolean match = true;


            if (chipCompletedOnly.isSelected() && !"Finished".equalsIgnoreCase(ride.getStatus())) match = false;
            if (chipCanceledOnly.isSelected()  && !"Canceled".equalsIgnoreCase(ride.getStatus())) match = false;

            if (match) filtered.add(ride);
        }
        rideAdapter.setRides(filtered);
    }

    private void resetFilters() {
        etFromDate.setText("");
        etToDate.setText("");
        chipLast7Days.setSelected(false);
        chipLastMonth.setSelected(false);
        chipCompletedOnly.setSelected(false);
        chipCanceledOnly.setSelected(false);
        chipAll.setSelected(false);
        // Show all in current sort order
        rideAdapter.setRides(new ArrayList<>(allRides));
    }

    private void setupDatePickers() {
        android.app.DatePickerDialog.OnDateSetListener fromListener = (v, year, month, day) -> {
            etFromDate.setText(String.format(Locale.getDefault(), "%02d.%02d.\n%d", day, month + 1, year));
        };
        android.app.DatePickerDialog.OnDateSetListener toListener = (v, year, month, day) -> {
            etToDate.setText(String.format(Locale.getDefault(), "%02d.%02d.\n%d", day, month + 1, year));
        };

        etFromDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new DatePickerDialog(requireContext(), fromListener,
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        etToDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new DatePickerDialog(requireContext(), toListener,
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
    }


    private Date parsePickerDate(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(str.replace("\n", ""));
        } catch (ParseException e) {
            return null;
        }
    }
}
