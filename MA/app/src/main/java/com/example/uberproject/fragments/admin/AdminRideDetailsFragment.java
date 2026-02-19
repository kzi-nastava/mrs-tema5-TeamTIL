package com.example.uberproject.fragments.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.model.Ride;
import com.example.uberproject.utils.GeocodingService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminRideDetailsFragment extends Fragment {

    private static final String ARG_RIDE = "arg_ride";
    private static final String TAG = "AdminRideDetailsFragment";

    private Ride ride;
    private LinearLayout driversContainer, passengersContainer;
    private TextView addressText, tvStartTime, tvEndTime, tvDuration, tvDistance, tvPrice, tvVehicleInfo;
    private Button btnStatus;
    private WebView webViewRideMap;

    public static AdminRideDetailsFragment newInstance(Ride ride) {
        AdminRideDetailsFragment fragment = new AdminRideDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_ride_history_details, container, false);

        if (getArguments() != null) {
            ride = (Ride) getArguments().getSerializable(ARG_RIDE);
        }

        driversContainer    = view.findViewById(R.id.driversContainer);
        passengersContainer = view.findViewById(R.id.passengersContainer);
        addressText         = view.findViewById(R.id.tvRideAddress);
        tvStartTime         = view.findViewById(R.id.tvStartTime);
        tvEndTime           = view.findViewById(R.id.tvEndTime);
        tvDuration          = view.findViewById(R.id.tvDuration);
        tvDistance          = view.findViewById(R.id.tvDistance);
        tvPrice             = view.findViewById(R.id.tvPrice);
        tvVehicleInfo       = view.findViewById(R.id.tvVehicleInfo);
        btnStatus           = view.findViewById(R.id.btnRideStatus);
        webViewRideMap      = view.findViewById(R.id.webViewRideMap);

        setupMap();
        populateRideData(inflater);

        return view;
    }

    // ---- MAPA ----
    private void setupMap() {
        WebSettings settings = webViewRideMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webViewRideMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                drawRouteOnMap();
            }
        });
        webViewRideMap.loadUrl("file:///android_asset/map.html");
    }

    private void drawRouteOnMap() {
        if (ride == null || ride.getFrom() == null || ride.getTo() == null) return;

        String startQuery = ride.getFrom() + ", Novi Sad";
        String endQuery   = ride.getTo()   + ", Novi Sad";

        GeocodingService geo = new GeocodingService();
        geo.geocodeAddress(startQuery, new GeocodingService.OnGeocodeListener() {
            @Override
            public void onSuccess(double startLat, double startLon) {
                geo.geocodeAddress(endQuery, new GeocodingService.OnGeocodeListener() {
                    @Override
                    public void onSuccess(double endLat, double endLon) {
                        List<double[]> coords = Arrays.asList(
                                new double[]{startLat, startLon},
                                new double[]{endLat, endLon}
                        );
                        drawRoute(coords);
                    }
                    @Override
                    public void onError(String e) { Log.e(TAG, "Geocode end: " + e); }
                });
            }
            @Override
            public void onError(String e) { Log.e(TAG, "Geocode start: " + e); }
        });
    }

    private void drawRoute(List<double[]> coords) {
        if (webViewRideMap == null) return;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < coords.size(); i++) {
            double[] c = coords.get(i);
            sb.append("[").append(c[0]).append(",").append(c[1]).append("]");
            if (i < coords.size() - 1) sb.append(",");
        }
        sb.append("]");
        String js = "drawRoute(" + sb + ",'')";
        webViewRideMap.post(() -> webViewRideMap.evaluateJavascript(js, null));
    }

    // ---- PODACI ----
    private void populateRideData(LayoutInflater inflater) {
        if (ride == null) return;

        // Adresa
        addressText.setText(ride.getFrom() + " → " + ride.getTo());

        // Vozač
        String driverName  = buildName(ride.getDriverFirstName(), ride.getDriverLastName());
        String driverPhone = ride.getDriverPhoneNumber() != null ? ride.getDriverPhoneNumber() : "";
        if (!driverName.isEmpty() || !driverPhone.isEmpty()) {
            addPersonCard(inflater, driversContainer,
                    driverName.isEmpty() ? "Unknown Driver" : driverName,
                    driverPhone, ride.getDriverProfilePictureUrl());
        }

        // Putnik
        String passengerName  = buildName(ride.getPassengerFirstName(), ride.getPassengerLastName());
        String passengerPhone = ride.getPassengerPhoneNumber() != null ? ride.getPassengerPhoneNumber() : "";
        if (!passengerName.isEmpty() || !passengerPhone.isEmpty()) {
            addPersonCard(inflater, passengersContainer,
                    passengerName.isEmpty()
                            ? (ride.getPassengerEmail() != null ? ride.getPassengerEmail() : "Unknown")
                            : passengerName,
                    passengerPhone, ride.getPassengerProfilePictureUrl());
        }

        // Vozilo
        String vehicleInfo = "";
        if (ride.getVehicleModel() != null)        vehicleInfo += ride.getVehicleModel();
        if (ride.getVehicleLicensePlate() != null) {
            if (!vehicleInfo.isEmpty()) vehicleInfo += " • ";
            vehicleInfo += ride.getVehicleLicensePlate();
        }
        tvVehicleInfo.setText(vehicleInfo.isEmpty() ? "N/A" : vehicleInfo);

        // Vremena
        tvStartTime.setText(formatDateTime(ride.getStartTime()));
        tvEndTime.setText(formatTime(ride.getEstimatedEndTime()));

        // Trajanje
        if (ride.getDuration() != null) {
            int totalMin = (int) Math.round(ride.getDuration());
            tvDuration.setText(totalMin >= 60
                    ? String.format(Locale.getDefault(), "%dh %dmin", totalMin / 60, totalMin % 60)
                    : totalMin + " min");
        } else {
            tvDuration.setText("N/A");
        }

        // Distanca
        tvDistance.setText(ride.getDistance() != null
                ? String.format(Locale.getDefault(), "%.1f km", ride.getDistance()) : "N/A");

        // Cijena i status
        tvPrice.setText(ride.getPrice() != null ? ride.getPrice() : "N/A");
        btnStatus.setText(ride.getStatus());
        if ("Finished".equalsIgnoreCase(ride.getStatus())) {
            btnStatus.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_green, null));
        } else if ("Canceled".equalsIgnoreCase(ride.getStatus())) {
            btnStatus.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_red, null));
        }
    }

    private String buildName(String first, String last) {
        String name = "";
        if (first != null) name += first;
        if (last != null)  name += (name.isEmpty() ? "" : " ") + last;
        return name.trim();
    }

    private void addPersonCard(LayoutInflater inflater, LinearLayout container,
                               String name, String phone, String photoUrl) {
        View card = inflater.inflate(R.layout.item_driver, container, false);
        ((TextView) card.findViewById(R.id.tvDriverName)).setText(name);
        ((TextView) card.findViewById(R.id.tvDriverPhone)).setText(phone);

        ImageView ivPhoto = card.findViewById(R.id.ivDriverPhoto);
        if (ivPhoto != null) {
            ivPhoto.clearColorFilter();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this).load(photoUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop().into(ivPhoto);
            } else {
                ivPhoto.setImageResource(R.drawable.ic_user_placeholder);
            }
        }
        container.addView(card);
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return "N/A";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date d = in.parse(raw);
            return d != null ? out.format(d) : raw;
        } catch (Exception e) { return raw; }
    }

    private String formatTime(String raw) {
        if (raw == null || raw.isEmpty()) return "N/A";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date d = in.parse(raw);
            return d != null ? out.format(d) : raw;
        } catch (Exception e) { return raw; }
    }
}
