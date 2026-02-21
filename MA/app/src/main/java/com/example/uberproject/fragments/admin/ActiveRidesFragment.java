package com.example.uberproject.fragments.admin;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.adapters.ActiveRidesAdapter;
import com.example.uberproject.api.ActiveRidesApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.response.ActiveRideAdminDTO;
import com.example.uberproject.fragments.tracking.TrackRideFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveRidesFragment extends Fragment {

    private static final int REFRESH_INTERVAL_MS = 30_000;

    // Views
    private RecyclerView rvActiveRides;
    private LinearLayout llEmptyState, layoutDetailPanel;
    private ProgressBar pbInitialLoad, pbLoading;
    private TextView tvRidesCount, tvEmptyMessage;
    private EditText etSearch;
    private ImageView ivClearSearch, ivClosePanel, ivRefreshIcon;
    private TextView tvRefreshState;
    private AppCompatButton btnViewFullDetails;
    private LinearLayout llRefreshToggle;

    // Panel views
    private ImageView ivPanelDriverAvatar, ivPanelPassengerAvatar;
    private TextView tvPanelDriverName, tvPanelDriverPhone, tvPanelRating;
    private TextView tvPanelVehicle, tvPanelLicensePlate, tvPanelDistance, tvPanelEta;
    private TextView tvPanelPassengerName, tvPanelPassengerPhone;

    // Data
    private ActiveRidesAdapter adapter;
    private List<ActiveRideAdminDTO> allRides = new ArrayList<>();
    private ActiveRideAdminDTO selectedRide = null;
    private boolean autoRefreshEnabled = true;
    private boolean initialLoadDone = false;

    // Auto-refresh
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (autoRefreshEnabled) {
                fetchRides(false);
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecyclerView();
        setupSearch();
        setupRefreshToggle();
        setupPanelClose();
        setupViewDetailsButton();

        fetchRides(true);
        startAutoRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
    }

    // ---- SETUP ----

    private void bindViews(View v) {
        rvActiveRides       = v.findViewById(R.id.rvActiveRides);
        llEmptyState        = v.findViewById(R.id.llEmptyState);
        layoutDetailPanel   = v.findViewById(R.id.layoutDetailPanel);
        pbInitialLoad       = v.findViewById(R.id.pbInitialLoad);
        pbLoading           = v.findViewById(R.id.pbLoading);
        tvRidesCount        = v.findViewById(R.id.tvRidesCount);
        tvEmptyMessage      = v.findViewById(R.id.tvEmptyMessage);
        etSearch            = v.findViewById(R.id.etSearch);
        ivClearSearch       = v.findViewById(R.id.ivClearSearch);
        ivClosePanel        = v.findViewById(R.id.ivClosePanel);
        ivRefreshIcon       = v.findViewById(R.id.ivRefreshIcon);
        tvRefreshState      = v.findViewById(R.id.tvRefreshState);
        llRefreshToggle     = v.findViewById(R.id.llRefreshToggle);
        btnViewFullDetails  = v.findViewById(R.id.btnViewFullDetails);

        ivPanelDriverAvatar    = v.findViewById(R.id.ivPanelDriverAvatar);
        ivPanelPassengerAvatar = v.findViewById(R.id.ivPanelPassengerAvatar);
        tvPanelDriverName      = v.findViewById(R.id.tvPanelDriverName);
        tvPanelDriverPhone     = v.findViewById(R.id.tvPanelDriverPhone);
        tvPanelRating          = v.findViewById(R.id.tvPanelRating);
        tvPanelVehicle         = v.findViewById(R.id.tvPanelVehicle);
        tvPanelLicensePlate    = v.findViewById(R.id.tvPanelLicensePlate);
        tvPanelDistance        = v.findViewById(R.id.tvPanelDistance);
        tvPanelEta             = v.findViewById(R.id.tvPanelEta);
        tvPanelPassengerName   = v.findViewById(R.id.tvPanelPassengerName);
        tvPanelPassengerPhone  = v.findViewById(R.id.tvPanelPassengerPhone);
    }

    private void setupRecyclerView() {
        adapter = new ActiveRidesAdapter(ride -> {
            if (selectedRide != null && selectedRide.getRideId().equals(ride.getRideId())) {
                // Tap same ride = deselect
                selectedRide = null;
                hideDetailPanel();
            } else {
                selectedRide = ride;
                showDetailPanel(ride);
            }
        });
        rvActiveRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActiveRides.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            applyFilter("");
        });
    }

    private void setupRefreshToggle() {
        llRefreshToggle.setOnClickListener(v -> {
            autoRefreshEnabled = !autoRefreshEnabled;
            tvRefreshState.setText(autoRefreshEnabled ? "ON" : "OFF");
            float alpha = autoRefreshEnabled ? 1f : 0.5f;
            ivRefreshIcon.setAlpha(alpha);
            tvRefreshState.setAlpha(alpha);
            if (autoRefreshEnabled) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        });
    }

    private void setupPanelClose() {
        ivClosePanel.setOnClickListener(v -> {
            selectedRide = null;
            hideDetailPanel();
        });
    }

    private void setupViewDetailsButton() {
        btnViewFullDetails.setOnClickListener(v -> {
            if (selectedRide == null) return;
            TrackRideFragment fragment = TrackRideFragment.newInstance(selectedRide.getRideId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    // ---- DATA ----

    private void fetchRides(boolean isInitial) {
        if (isInitial) {
            pbInitialLoad.setVisibility(View.VISIBLE);
            rvActiveRides.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.GONE);
        } else {
            pbLoading.setVisibility(View.VISIBLE);
        }

        ActiveRidesApi api = RetrofitClient.getInstance(requireContext()).create(ActiveRidesApi.class);
        api.getActiveRides().enqueue(new Callback<List<ActiveRideAdminDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ActiveRideAdminDTO>> call,
                                   @NonNull Response<List<ActiveRideAdminDTO>> response) {
                if (!isAdded()) return;
                pbInitialLoad.setVisibility(View.GONE);
                pbLoading.setVisibility(View.GONE);
                initialLoadDone = true;

                if (response.isSuccessful() && response.body() != null) {
                    allRides = response.body();

                    // Keep selected ride in sync after refresh
                    if (selectedRide != null) {
                        boolean found = false;
                        for (ActiveRideAdminDTO r : allRides) {
                            if (r.getRideId().equals(selectedRide.getRideId())) {
                                selectedRide = r;
                                showDetailPanel(r);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            selectedRide = null;
                            hideDetailPanel();
                        }
                    }

                    applyFilter(etSearch.getText().toString());
                } else {
                    showEmpty("No active rides at the moment.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ActiveRideAdminDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                pbInitialLoad.setVisibility(View.GONE);
                pbLoading.setVisibility(View.GONE);
                if (!initialLoadDone) {
                    showEmpty("Failed to load rides. Check connection.");
                }
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<ActiveRideAdminDTO> filtered;

        if (q.isEmpty()) {
            filtered = new ArrayList<>(allRides);
        } else {
            filtered = allRides.stream().filter(r -> {
                String name = (r.getDriverFirstName() + " " + r.getDriverLastName()).toLowerCase(Locale.ROOT);
                String email = r.getDriverEmail() != null ? r.getDriverEmail().toLowerCase(Locale.ROOT) : "";
                String plate = r.getLicensePlate() != null ? r.getLicensePlate().toLowerCase(Locale.ROOT) : "";
                return name.contains(q) || email.contains(q) || plate.contains(q);
            }).collect(Collectors.toList());
        }

        updateCountLabel(filtered.size());

        if (filtered.isEmpty()) {
            showEmpty(q.isEmpty() ? "No active rides at the moment." : "No rides match your search.");
        } else {
            rvActiveRides.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            adapter.setRides(filtered);
        }
    }

    private void updateCountLabel(int count) {
        tvRidesCount.setText("ACTIVE RIDES (" + count + ")");
    }

    private void showEmpty(String message) {
        rvActiveRides.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    // ---- DETAIL PANEL ----

    private void showDetailPanel(ActiveRideAdminDTO ride) {
        layoutDetailPanel.setVisibility(View.VISIBLE);

        tvPanelDriverName.setText(ride.getDriverFullName().trim());
        tvPanelDriverPhone.setText(ride.getDriverPhone() != null ? ride.getDriverPhone() : "");
        tvPanelRating.setText(ride.getFormattedRating());

        String vehicle = (ride.getVehicleModel() != null ? ride.getVehicleModel() : "")
                + (ride.getVehicleType() != null ? " · " + capitalize(ride.getVehicleType()) : "");
        tvPanelVehicle.setText(vehicle);
        tvPanelLicensePlate.setText(ride.getLicensePlate() != null ? ride.getLicensePlate() : "");

        if (ride.getDistanceKm() != null) {
            tvPanelDistance.setText(String.format(Locale.ROOT, "%.1f km", ride.getDistanceKm()));
        } else {
            tvPanelDistance.setText("N/A");
        }

        tvPanelEta.setText(extractTime(ride.getEstimatedEndTime()));
        tvPanelPassengerName.setText(ride.getPassengerFullName().trim());
        tvPanelPassengerPhone.setText(ride.getPassengerPhone() != null ? ride.getPassengerPhone() : "");

        loadAvatar(ivPanelDriverAvatar, ride.getDriverProfilePicture());
        loadAvatar(ivPanelPassengerAvatar, ride.getPassengerProfilePicture());

        // Show "View Full Ride Details" only for IN_PROGRESS
        btnViewFullDetails.setVisibility(ride.isInProgress() ? View.VISIBLE : View.GONE);
    }

    private void hideDetailPanel() {
        layoutDetailPanel.setVisibility(View.GONE);
    }

    // ---- AUTO-REFRESH ----

    private void startAutoRefresh() {
        stopAutoRefresh();
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    private void stopAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    // ---- HELPERS ----

    private String extractTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "N/A";
        int commaIdx = dateTime.lastIndexOf(", ");
        if (commaIdx >= 0 && commaIdx + 2 < dateTime.length()) {
            return dateTime.substring(commaIdx + 2);
        }
        return dateTime;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private void loadAvatar(ImageView imageView, String picData) {
        if (!isAdded()) return;
        if (picData == null || picData.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_person_placeholder);
            return;
        }
        if (picData.startsWith("http")) {
            Glide.with(this)
                    .load(picData)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(imageView);
        } else {
            try {
                byte[] bytes = Base64.decode(picData, Base64.DEFAULT);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_person_placeholder);
            }
        }
    }
}