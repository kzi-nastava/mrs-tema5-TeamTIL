package com.example.uberproject.fragments.user;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRideHistoryFragment extends Fragment {

    private static final String TAG = "UserRideHistoryFragment";

    private androidx.appcompat.widget.AppCompatButton btnApplyFilters;
    private android.widget.AutoCompleteTextView etFromDate, etToDate;
    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> allRides = new ArrayList<>();
    private List<RideHistoryResponseDTO> rideHistoryData = new ArrayList<>();

    // Favorites - čuvamo routeId-eve koji su omiljeni
    private Set<Integer> favoriteRouteIds = new HashSet<>();

    private Chip chipLast7Days, chipLastMonth, chipCompletedOnly, chipCanceledOnly, chipAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_ride_history, container, false);

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

        // Postavi listener za zvezdice
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

        // Učitaj ride history i favorite paralelno
        loadRideHistory();
        loadFavoriteRouteIds();

        return view;
    }

    // ---- API: Učitaj ride history ----
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
                    // Primijeni favourite stanje ako je već učitano
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

    // ---- API: Učitaj koje rute su omiljene ----
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
                    // Obavijesti adapter da osvježi zvezdice
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

    // ---- API: Dodaj u omiljene ----
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
                    // Rollback - ukloni iz lokalnog seta jer API nije uspio
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

    // ---- API: Ukloni iz omiljenih ----
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
                    // Rollback
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

    // ---- Konverzija DTO -> model ----
    private void convertToRideModel(List<RideHistoryResponseDTO> list) {
        allRides.clear();
        for (RideHistoryResponseDTO dto : list) {
            String price = dto.getPrice() != null
                    ? String.format("%.0f RSD", dto.getPrice()) : "N/A";
            String status = mapStatus(dto.getStatus());
            String dateTime = formatDateTime(dto.getStartTime());

            // Koristi novi konstruktor sa routeId
            Ride ride = new Ride(
                    dto.getId(),
                    dto.getRouteId(),   // ← routeId za zvezdicu
                    dto.getStartLocation(),
                    dto.getEndLocation(),
                    price,
                    status,
                    dateTime,
                    dto.getPanicSent()
            );
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
            // Backend šalje "dd MMM yyyy, HH:mm" format
            return startTime;
        } catch (Exception e) {
            return startTime;
        }
    }

    // ---- FILTERI I CHIPOVI (nepromijenjeno) ----
    private void setChipListeners() {
        View.OnClickListener chipClickListener = v -> {
            chipLast7Days.setSelected(false);
            chipLastMonth.setSelected(false);
            chipCompletedOnly.setSelected(false);
            chipCanceledOnly.setSelected(false);
            chipAll.setSelected(false);
            ((Chip) v).setSelected(true);

            Calendar calendar = java.util.Calendar.getInstance();
            etFromDate.setText("");
            etToDate.setText("");

            if (v == chipLast7Days) {
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                Date from = calendar.getTime();
                Date to = new Date();
                etFromDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(from));
                etToDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(to));
                applyFilters();
            } else if (v == chipLastMonth) {
                calendar.add(Calendar.MONTH, -1);
                Date from = calendar.getTime();
                Date to = new Date();
                etFromDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(from));
                etToDate.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(to));
                applyFilters();
            } else if (v == chipCompletedOnly || v == chipCanceledOnly) {
                applyFilters();
            } else if (v == chipAll) {
                rideAdapter.setRides(allRides);
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

        List<Ride> filtered = new ArrayList<>();
        for (Ride ride : allRides) {
            boolean match = true;

            Date rideDate = parseRideDate(ride.getDateTime());
            if (rideDate != null) {
                if (fromDate != null && rideDate.before(fromDate)) match = false;
                if (toDate   != null && rideDate.after(toDate))   match = false;
            } else {
                match = false;
            }

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
        rideAdapter.setRides(allRides);
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

    private Date parseRideDate(String rideDateStr) {
        if (rideDateStr == null || rideDateStr.isEmpty()) return null;
        try {
            // Backend format: "dd MMM yyyy, HH:mm"
            return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).parse(rideDateStr);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(rideDateStr.split(",")[0]);
            } catch (ParseException ex) {
                return null;
            }
        }
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
