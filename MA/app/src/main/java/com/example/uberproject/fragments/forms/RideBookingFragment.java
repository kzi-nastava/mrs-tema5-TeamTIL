package com.example.uberproject.fragments.forms;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.api.RouteApi;
import com.example.uberproject.dto.request.RideRequestDTO;
import com.example.uberproject.dto.response.FavoriteRouteDTO;
import com.example.uberproject.dto.response.RideCreatedResponseDTO;
import com.example.uberproject.utils.GeocodingService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideBookingFragment extends BottomSheetDialogFragment {

    private static final String TAG = "RideBookingFragment";

    private EditText etStartLocation, etEndLocation;
    private LinearLayout llStopsContainer;
    private TextView tvCurrentMonth, tvHour, tvMinute;
    private GridLayout calendarGrid;
    private int selectedDay, selectedMonth, selectedYear;
    private int currentHour = 12, currentMinute = 0;
    private CheckBox cbBabyFriendly, cbPetFriendly;
    private TextView btnVehicleStandard, btnVehicleLuxury, btnVehicleVan;
    private String selectedVehicleType = "STANDARD";
    private EditText etPassengerEmail;
    private LinearLayout llPassengersList;
    private final List<String> addedPassengerEmails = new ArrayList<>();
    private LinearLayout llFavoritesContainer;
    private TextView tvFavoritesArrow;
    private boolean favoritesExpanded = false;
    private List<FavoriteRouteDTO> loadedFavorites = new ArrayList<>();
    private final List<EditText> stopFields = new ArrayList<>();
    private int stopCount = 0;
    private final List<double[]> stopCoords = new ArrayList<>();
    private Double startLat, startLon, endLat, endLon;
    private final GeocodingService geocodingService = new GeocodingService();

    public RideBookingFragment() {}
    public static RideBookingFragment newInstance() { return new RideBookingFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        initCalendar();
        setupTimeControls(view);
        setupVehicleTypeButtons();
        setupFavoriteRoutes();
        setupPassengers();
        setupAddStop(view);
        setupRequestRide(view);
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
        loadFavoritesFromApi();
    }

    private void bindViews(View view) {
        etStartLocation    = view.findViewById(R.id.etStartLocation);
        etEndLocation      = view.findViewById(R.id.etEndLocation);
        llStopsContainer   = view.findViewById(R.id.llStopsContainer);
        tvCurrentMonth     = view.findViewById(R.id.tvCurrentMonth);
        calendarGrid       = view.findViewById(R.id.calendarGrid);
        tvHour             = view.findViewById(R.id.tvHour);
        tvMinute           = view.findViewById(R.id.tvMinute);
        cbBabyFriendly     = view.findViewById(R.id.cbBabyFriendly);
        cbPetFriendly      = view.findViewById(R.id.cbPetFriendly);
        btnVehicleStandard = view.findViewById(R.id.btnVehicleStandard);
        btnVehicleLuxury   = view.findViewById(R.id.btnVehicleLuxury);
        btnVehicleVan      = view.findViewById(R.id.btnVehicleVan);
        etPassengerEmail   = view.findViewById(R.id.etPassengerEmail);
        llPassengersList   = view.findViewById(R.id.llPassengersList);
        llFavoritesContainer = view.findViewById(R.id.llFavoritesContainer);
        tvFavoritesArrow   = view.findViewById(R.id.tvFavoritesArrow);
    }

    // ---- API: Load favorites ----
    private void loadFavoritesFromApi() {
        RouteApi routeApi = RetrofitClient.getInstance(requireContext()).create(RouteApi.class);
        routeApi.getFavoriteRoutes().enqueue(new Callback<List<FavoriteRouteDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoriteRouteDTO>> call,
                                   @NonNull Response<List<FavoriteRouteDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadedFavorites = response.body();
                    Log.d(TAG, "Loaded " + loadedFavorites.size() + " favorites");
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<FavoriteRouteDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Favorites load failed: " + t.getMessage());
            }
        });
    }

    // ---- CALENDAR ----
    private void initCalendar() {
        Calendar cal = Calendar.getInstance();
        selectedDay = cal.get(Calendar.DAY_OF_MONTH);
        selectedMonth = cal.get(Calendar.MONTH);
        selectedYear = cal.get(Calendar.YEAR);
        renderCalendar();
    }

    private void renderCalendar() {
        String[] months = {"January","February","March","April","May","June",
                "July","August","September","October","November","December"};
        tvCurrentMonth.setText(months[selectedMonth] + " " + selectedYear);
        calendarGrid.removeAllViews();
        calendarGrid.setColumnCount(7);
        String[] days = {"Mo","Tu","We","Th","Fr","Sa","Su"};
        for (String d : days) {
            TextView h = makeCalendarCell(d, false, false);
            h.setTextColor(0xFF9E9E9E); h.setTypeface(null, Typeface.BOLD);
            calendarGrid.addView(h);
        }
        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, selectedMonth, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDow == Calendar.SUNDAY) ? 6 : firstDow - 2;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < offset; i++) calendarGrid.addView(makeCalendarCell("", false, false));
        for (int day = 1; day <= daysInMonth; day++) {
            boolean isToday = day == today.get(Calendar.DAY_OF_MONTH)
                    && selectedMonth == today.get(Calendar.MONTH)
                    && selectedYear == today.get(Calendar.YEAR);
            TextView cell = makeCalendarCell(String.valueOf(day), isToday, day == selectedDay);
            final int d = day;
            cell.setOnClickListener(v -> { selectedDay = d; renderCalendar(); });
            calendarGrid.addView(cell);
        }
    }

    private TextView makeCalendarCell(String text, boolean isToday, boolean isSelected) {
        TextView tv = new TextView(requireContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0; lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER); tv.setText(text);
        tv.setTextSize(12f); tv.setPadding(4, 6, 4, 6);
        if (isSelected) {
            tv.setBackgroundResource(R.drawable.confirm_button_background);
            tv.setTextColor(0xFF1A0A2E); tv.setTypeface(null, Typeface.BOLD);
        } else if (isToday) {
            tv.setTextColor(0xFFFFB800); tv.setTypeface(null, Typeface.BOLD);
        } else { tv.setTextColor(0xFFFFFFFF); }
        return tv;
    }

    // ---- TIME ----
    private void setupTimeControls(View view) {
        Calendar cal = Calendar.getInstance();
        currentHour = cal.get(Calendar.HOUR_OF_DAY);
        currentMinute = (cal.get(Calendar.MINUTE) / 5) * 5;
        updateTimeDisplay();
        view.findViewById(R.id.btnHourPlus).setOnClickListener(v -> { currentHour = (currentHour+1)%24; updateTimeDisplay(); });
        view.findViewById(R.id.btnHourMinus).setOnClickListener(v -> { currentHour = (currentHour+23)%24; updateTimeDisplay(); });
        view.findViewById(R.id.btnMinutePlus).setOnClickListener(v -> { currentMinute = (currentMinute+5)%60; updateTimeDisplay(); });
        view.findViewById(R.id.btnMinuteMinus).setOnClickListener(v -> { currentMinute = (currentMinute+55)%60; updateTimeDisplay(); });
        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            selectedMonth--; if (selectedMonth<0){selectedMonth=11;selectedYear--;} selectedDay=1; renderCalendar();});
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedMonth++; if (selectedMonth>11){selectedMonth=0;selectedYear++;} selectedDay=1; renderCalendar();});
    }

    private void updateTimeDisplay() {
        tvHour.setText(String.format("%02d", currentHour));
        tvMinute.setText(String.format("%02d", currentMinute));
    }

    // ---- VEHICLE ----
    private void setupVehicleTypeButtons() {
        btnVehicleStandard.setOnClickListener(v -> selectVehicle("STANDARD"));
        btnVehicleLuxury.setOnClickListener(v -> selectVehicle("LUXURY"));
        btnVehicleVan.setOnClickListener(v -> selectVehicle("VAN"));
    }

    private void selectVehicle(String type) {
        selectedVehicleType = type;
        setVehicleButtonState(btnVehicleStandard, type.equals("STANDARD"));
        setVehicleButtonState(btnVehicleLuxury, type.equals("LUXURY"));
        setVehicleButtonState(btnVehicleVan, type.equals("VAN"));
    }

    private void setVehicleButtonState(TextView btn, boolean selected) {
        if (selected) {
            btn.setBackgroundResource(R.drawable.bg_vehicle_type_selected);
            btn.setTextColor(0xFF1A0A2E); btn.setTypeface(null, Typeface.BOLD);
        } else {
            btn.setBackgroundResource(R.drawable.bg_vehicle_type_unselected);
            btn.setTextColor(0xFFFFFFFF); btn.setTypeface(null, Typeface.NORMAL);
        }
    }

    // ---- FAVORITES ----
    private void setupFavoriteRoutes() {
        requireView().findViewById(R.id.btnToggleFavorites).setOnClickListener(v -> {
            favoritesExpanded = !favoritesExpanded;
            llFavoritesContainer.setVisibility(favoritesExpanded ? View.VISIBLE : View.GONE);
            tvFavoritesArrow.setText(favoritesExpanded ? "▲" : "▼");
            if (favoritesExpanded) renderFavoriteRoutes();
        });
    }

    private void renderFavoriteRoutes() {
        llFavoritesContainer.removeAllViews();
        if (loadedFavorites == null || loadedFavorites.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("No favorite routes yet.");
            tv.setTextColor(0xFF9E9E9E); tv.setTextSize(13f); tv.setPadding(8, 12, 8, 12);
            llFavoritesContainer.addView(tv);
            return;
        }
        for (FavoriteRouteDTO fav : loadedFavorites) {
            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_favorite_route, llFavoritesContainer, false);

            String info = String.format("Distance %.1fkm · %.0f min",
                    fav.getDistanceKm() != null ? fav.getDistanceKm() : 0.0,
                    fav.getEstimatedTimeMin() != null ? fav.getEstimatedTimeMin() : 0.0);
            ((TextView) card.findViewById(R.id.tvRouteInfo)).setText(info);

            // Putanja sa eventualnim stopovima
            String path;
            if (fav.getIntermediateStops() != null && !fav.getIntermediateStops().isEmpty()) {
                path = fav.getStartLocation() + " → "
                        + String.join(" → ", fav.getIntermediateStops())
                        + " → " + fav.getEndLocation();
            } else {
                path = fav.getStartLocation() + " → " + fav.getEndLocation();
            }
            ((TextView) card.findViewById(R.id.tvRoutePath)).setText(path);

            card.findViewById(R.id.btnChooseRoute).setOnClickListener(vv -> {
                // Popuni polja u formi
                etStartLocation.setText(fav.getStartLocation());
                etEndLocation.setText(fav.getEndLocation());

                // Popuni stopove
                llStopsContainer.removeAllViews();
                stopFields.clear(); stopCoords.clear(); stopCount = 0;
                if (fav.getIntermediateStops() != null) {
                    for (String stop : fav.getIntermediateStops()) {
                        addStopField();
                        if (!stopFields.isEmpty()) {
                            stopFields.get(stopFields.size() - 1).setText(stop);
                        }
                    }
                }
                // Reset geocoded coords
                startLat = startLon = endLat = endLon = null;
                // Zatvori panel
                favoritesExpanded = false;
                llFavoritesContainer.setVisibility(View.GONE);
                tvFavoritesArrow.setText("▼");
                Toast.makeText(getContext(), "Route selected!", Toast.LENGTH_SHORT).show();
            });

            llFavoritesContainer.addView(card);
        }
    }

    // ---- PASSENGERS ----
    private void setupPassengers() {
        requireView().findViewById(R.id.btnAddPassenger).setOnClickListener(v -> {
            String email = etPassengerEmail.getText().toString().trim();
            if (email.isEmpty()) { Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show(); return; }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show(); return; }
            if (addedPassengerEmails.contains(email)) {
                Toast.makeText(getContext(), "Already added", Toast.LENGTH_SHORT).show(); return; }
            addedPassengerEmails.add(email);
            addPassengerChip(email);
            etPassengerEmail.setText("");
        });
    }

    private void addPassengerChip(String email) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 8);
        row.setLayoutParams(lp);
        row.setBackgroundResource(R.drawable.bg_purple_tint);
        row.setPadding(24, 14, 24, 14);
        TextView tvEmail = new TextView(requireContext());
        tvEmail.setText(email); tvEmail.setTextColor(0xFFFFFFFF); tvEmail.setTextSize(13f);
        tvEmail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvEmail);
        TextView btnRemove = new TextView(requireContext());
        btnRemove.setText("✕"); btnRemove.setTextColor(0xFFFF4444); btnRemove.setTextSize(16f);
        btnRemove.setClickable(true); btnRemove.setFocusable(true);
        btnRemove.setOnClickListener(v -> { addedPassengerEmails.remove(email); llPassengersList.removeView(row); });
        row.addView(btnRemove);
        llPassengersList.addView(row);
    }

    // ---- STOPS ----
    private void setupAddStop(View view) {
        view.findViewById(R.id.btnAddStop).setOnClickListener(v -> addStopField());
    }

    private void addStopField() {
        stopCount++;
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(52));
        lp.setMargins(0, 0, 0, dpToPx(8));
        row.setLayoutParams(lp);
        row.setBackgroundResource(R.drawable.bg_purple_tint);
        row.setPadding(dpToPx(12), 0, dpToPx(12), 0);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText("S" + stopCount); tvLabel.setTextColor(0xFFFFFFFF);
        tvLabel.setTextSize(12f); tvLabel.setGravity(Gravity.CENTER);
        tvLabel.setBackgroundResource(R.drawable.bg_location_circle);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
        row.addView(tvLabel);

        EditText et = new EditText(requireContext());
        et.setHint("Add stop"); et.setTextColor(0xFFFFFFFF);
        et.setHintTextColor(0xFF9E9E9E); et.setTextSize(14f);
        et.setBackground(null); et.setSingleLine(true);
        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        etLp.setMarginStart(dpToPx(10));
        et.setLayoutParams(etLp);
        row.addView(et);

        TextView btnRemove = new TextView(requireContext());
        btnRemove.setText("✕"); btnRemove.setTextColor(0xFFFF4444);
        btnRemove.setTextSize(16f); btnRemove.setClickable(true); btnRemove.setFocusable(true);
        btnRemove.setOnClickListener(v -> { stopFields.remove(et); llStopsContainer.removeView(row); });
        row.addView(btnRemove);

        stopFields.add(et);
        stopCoords.add(new double[]{0, 0});
        llStopsContainer.addView(row);
    }

    // ---- REQUEST RIDE ----
    private void setupRequestRide(View view) {
        view.findViewById(R.id.btnRequestRide).setOnClickListener(v -> {
            String startAddr = etStartLocation.getText().toString().trim();
            String endAddr   = etEndLocation.getText().toString().trim();
            if (startAddr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter starting location", Toast.LENGTH_SHORT).show(); return; }
            if (endAddr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter ending location", Toast.LENGTH_SHORT).show(); return; }

            // Validacija: max 5 sati unapred
            Calendar now = Calendar.getInstance();
            Calendar scheduled = Calendar.getInstance();
            scheduled.set(selectedYear, selectedMonth, selectedDay, currentHour, currentMinute, 0);
            long diffMinutes = (scheduled.getTimeInMillis() - now.getTimeInMillis()) / 60000;
            if (diffMinutes > 5 * 60) {
                Toast.makeText(getContext(), "You can schedule a ride at most 5 hours in advance", Toast.LENGTH_LONG).show();
                return;
            }

            setButtonLoading(true);
            geocodeAndSubmit(startAddr, endAddr);
        });
    }

    private void geocodeAndSubmit(String startAddr, String endAddr) {
        geocodingService.geocodeAddress(startAddr, new GeocodingService.OnGeocodeListener() {
            @Override public void onSuccess(double lat, double lon) {
                startLat = lat; startLon = lon;
                geocodingService.geocodeAddress(endAddr, new GeocodingService.OnGeocodeListener() {
                    @Override public void onSuccess(double lat2, double lon2) {
                        endLat = lat2; endLon = lon2;
                        if (stopFields.isEmpty()) buildAndSendRequest(startAddr, endAddr);
                        else geocodeStopsThenSubmit(startAddr, endAddr, 0);
                    }
                    @Override public void onError(String e) {
                        setButtonLoading(false);
                        Toast.makeText(getContext(), "Could not find destination: " + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override public void onError(String e) {
                setButtonLoading(false);
                Toast.makeText(getContext(), "Could not find start: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void geocodeStopsThenSubmit(String startAddr, String endAddr, int idx) {
        if (idx >= stopFields.size()) { buildAndSendRequest(startAddr, endAddr); return; }
        String addr = stopFields.get(idx).getText().toString().trim();
        if (addr.isEmpty()) { geocodeStopsThenSubmit(startAddr, endAddr, idx + 1); return; }
        geocodingService.geocodeAddress(addr, new GeocodingService.OnGeocodeListener() {
            @Override public void onSuccess(double lat, double lon) {
                if (idx < stopCoords.size()) stopCoords.set(idx, new double[]{lat, lon});
                geocodeStopsThenSubmit(startAddr, endAddr, idx + 1);
            }
            @Override public void onError(String e) {
                setButtonLoading(false);
                Toast.makeText(getContext(), "Stop " + (idx+1) + " not found: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildAndSendRequest(String startAddr, String endAddr) {
        List<RideRequestDTO.LocationDTO> locations = new ArrayList<>();
        locations.add(new RideRequestDTO.LocationDTO(startAddr, startLat, startLon));
        for (int i = 0; i < stopFields.size(); i++) {
            String addr = stopFields.get(i).getText().toString().trim();
            if (!addr.isEmpty() && i < stopCoords.size()) {
                double[] c = stopCoords.get(i);
                locations.add(new RideRequestDTO.LocationDTO(addr, c[0], c[1]));
            }
        }
        locations.add(new RideRequestDTO.LocationDTO(endAddr, endLat, endLon));

        String scheduledTime = String.format("%04d-%02d-%02dT%02d:%02d:00",
                selectedYear, selectedMonth + 1, selectedDay, currentHour, currentMinute);

        RideRequestDTO request = new RideRequestDTO(
                locations, new ArrayList<>(addedPassengerEmails),
                selectedVehicleType, cbBabyFriendly.isChecked(), cbPetFriendly.isChecked(), scheduledTime);
        submitRide(request);
    }

    private void submitRide(RideRequestDTO request) {
        RideApi rideApi = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        rideApi.createRide(request).enqueue(new Callback<RideCreatedResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RideCreatedResponseDTO> call,
                                   @NonNull Response<RideCreatedResponseDTO> response) {
                setButtonLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    RideCreatedResponseDTO result = response.body();
                    Toast.makeText(getContext(),
                            "Ride booked! Driver: " + result.getDriverName()
                                    + "\nPrice: " + String.format("%.0f RSD", result.getPrice()),
                            Toast.LENGTH_LONG).show();
                    dismiss();
                    if (getActivity() instanceof OnRideBookedListener) {
                        ((OnRideBookedListener) getActivity()).onRideBooked(result);
                    }
                } else {
                    String msg = "Failed to book ride";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (body.contains("\"error\""))
                                msg = body.replaceAll(".*\"error\":\"([^\"]+)\".*", "$1");
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<RideCreatedResponseDTO> call, @NonNull Throwable t) {
                setButtonLoading(false);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setButtonLoading(boolean loading) {
        if (getView() == null) return;
        MaterialButton btn = getView().findViewById(R.id.btnRequestRide);
        btn.setEnabled(!loading);
        btn.setText(loading ? "Please wait..." : "Request ride");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    public interface OnRideBookedListener {
        void onRideBooked(RideCreatedResponseDTO ride);
    }
}
