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
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.response.RideDetailsResponseDTO;
import com.example.uberproject.model.Ride;
import com.example.uberproject.utils.GeocodingService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRideDetailsFragment extends Fragment {

    private static final String ARG_RIDE = "arg_ride";
    private static final String TAG = "AdminRideDetailsFragment";

    private Ride ride;

    // Details card views
    private LinearLayout driversContainer, passengersContainer;
    private TextView addressText, tvStartTime, tvEndTime, tvDuration, tvDistance, tvPrice, tvVehicleInfo;
    private Button btnStatus;
    private WebView webViewRideMap;

    // Rating section views (read-only for admin)
    private LinearLayout ratingSection;
    private TextView tvRatingStatus, tvRatingComment;
    private ImageView[] ratingStars = new ImageView[5];

    // Issues section views (read-only for admin)
    private LinearLayout issuesSection, issuesContainer;
    private TextView tvNoIssues;

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static AdminRideDetailsFragment newInstance(Ride ride) {
        AdminRideDetailsFragment fragment = new AdminRideDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
        fragment.setArguments(args);
        return fragment;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_ride_history_details, container, false);

        if (getArguments() != null) {
            ride = (Ride) getArguments().getSerializable(ARG_RIDE);
        }

        bindViews(view);

        // Admin never sees action buttons
        view.findViewById(R.id.btnRateRide).setVisibility(View.GONE);
        view.findViewById(R.id.btnReportInconsistency).setVisibility(View.GONE);

        setupMap();
        populateRideData(inflater);
        fetchDetailsFromApi(inflater); // load real rating + issues

        return view;
    }

    // ─── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
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

        ratingSection   = view.findViewById(R.id.ratingSection);
        tvRatingStatus  = view.findViewById(R.id.tvRatingStatus);
        tvRatingComment = view.findViewById(R.id.tvRatingComment);
        ratingStars[0]  = view.findViewById(R.id.ratingStar1);
        ratingStars[1]  = view.findViewById(R.id.ratingStar2);
        ratingStars[2]  = view.findViewById(R.id.ratingStar3);
        ratingStars[3]  = view.findViewById(R.id.ratingStar4);
        ratingStars[4]  = view.findViewById(R.id.ratingStar5);

        issuesSection   = view.findViewById(R.id.issuesSection);
        issuesContainer = view.findViewById(R.id.issuesContainer);
        tvNoIssues      = view.findViewById(R.id.tvNoIssues);
    }

    // ─── Map ──────────────────────────────────────────────────────────────────

    private void setupMap() {
        WebSettings s = webViewRideMap.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webViewRideMap.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) { drawRouteOnMap(); }
        });
        webViewRideMap.loadUrl("file:///android_asset/map.html");
    }

    private void drawRouteOnMap() {
        if (ride == null || ride.getFrom() == null || ride.getTo() == null) return;
        GeocodingService geo = new GeocodingService();
        geo.geocodeAddress(ride.getFrom() + ", Novi Sad", new GeocodingService.OnGeocodeListener() {
            @Override public void onSuccess(double sLat, double sLon) {
                geo.geocodeAddress(ride.getTo() + ", Novi Sad", new GeocodingService.OnGeocodeListener() {
                    @Override public void onSuccess(double eLat, double eLon) {
                        drawRoute(Arrays.asList(new double[]{sLat, sLon}, new double[]{eLat, eLon}));
                    }
                    @Override public void onError(String e) { Log.e(TAG, "Geocode end: " + e); }
                });
            }
            @Override public void onError(String e) { Log.e(TAG, "Geocode start: " + e); }
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

    // ─── Populate base ride data ──────────────────────────────────────────────

    private void populateRideData(LayoutInflater inflater) {
        if (ride == null) return;

        addressText.setText(ride.getFrom() + " → " + ride.getTo());

        String driverName  = buildName(ride.getDriverFirstName(), ride.getDriverLastName());
        String driverPhone = ride.getDriverPhoneNumber() != null ? ride.getDriverPhoneNumber() : "";
        if (!driverName.isEmpty() || !driverPhone.isEmpty()) {
            addPersonCard(inflater, driversContainer,
                    driverName.isEmpty() ? "Unknown Driver" : driverName,
                    driverPhone, ride.getDriverProfilePictureUrl());
        }

        String passengerName  = buildName(ride.getPassengerFirstName(), ride.getPassengerLastName());
        String passengerPhone = ride.getPassengerPhoneNumber() != null ? ride.getPassengerPhoneNumber() : "";
        if (!passengerName.isEmpty() || !passengerPhone.isEmpty()) {
            addPersonCard(inflater, passengersContainer,
                    passengerName.isEmpty()
                            ? (ride.getPassengerEmail() != null ? ride.getPassengerEmail() : "Unknown")
                            : passengerName,
                    passengerPhone, ride.getPassengerProfilePictureUrl());
        }

        String vi = "";
        if (ride.getVehicleModel() != null)        vi += ride.getVehicleModel();
        if (ride.getVehicleLicensePlate() != null) vi += (vi.isEmpty() ? "" : "  •  ") + ride.getVehicleLicensePlate();
        tvVehicleInfo.setText(vi.isEmpty() ? "N/A" : vi);

        tvStartTime.setText(formatDateTime(ride.getStartTime()));
        tvEndTime.setText(formatTime(ride.getEstimatedEndTime()));

        if (ride.getDuration() != null) {
            int m = (int) Math.round(ride.getDuration());
            tvDuration.setText(m >= 60
                    ? String.format(Locale.getDefault(), "%dh %dmin", m / 60, m % 60)
                    : m + " min");
        } else {
            tvDuration.setText("N/A");
        }

        tvDistance.setText(ride.getDistance() != null
                ? String.format(Locale.getDefault(), "%.1f km", ride.getDistance()) : "N/A");

        tvPrice.setText(ride.getPrice() != null ? ride.getPrice() : "N/A");
        btnStatus.setText(ride.getStatus());
        applyStatusColor();
    }

    // ─── Fetch rating + issues from API ──────────────────────────────────────

    private void fetchDetailsFromApi(LayoutInflater inflater) {
        if (ride == null) return;
        RetrofitClient.getInstance(requireContext()).create(RideApi.class)
                .getRideDetails(ride.getId())
                .enqueue(new Callback<RideDetailsResponseDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<RideDetailsResponseDTO> call,
                                           @NonNull Response<RideDetailsResponseDTO> response) {
                        if (!isAdded() || response.body() == null) return;
                        RideDetailsResponseDTO dto = response.body();
                        requireActivity().runOnUiThread(() -> {
                            renderRatingReadOnly(dto.getRideRating(), dto.getRideComment());
                            renderIssuesReadOnly(inflater, dto.getReportedIssues());
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Call<RideDetailsResponseDTO> call, @NonNull Throwable t) {
                        Log.e(TAG, "fetchDetails failed: " + t.getMessage());
                        // Still show empty issues section gracefully
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                renderRatingReadOnly(null, null);
                                renderIssuesReadOnly(inflater, null);
                            });
                        }
                    }
                });
    }

    // ─── Rating (read-only) ───────────────────────────────────────────────────

    private void renderRatingReadOnly(Double rideRating, String rideComment) {
        ratingSection.setVisibility(View.VISIBLE);

        boolean hasRating = rideRating != null && rideRating > 0;

        tvRatingStatus.setText(hasRating ? "Rated" : "Unrated");
        tvRatingStatus.setBackgroundResource(hasRating ? R.drawable.badge_green : R.drawable.badge_grey);

        int filled = hasRating ? (int) Math.round(rideRating) : 0;
        for (int i = 0; i < 5; i++) {
            ratingStars[i].setImageResource(i < filled ? R.drawable.ic_star_filled2 : R.drawable.ic_star_empty);
        }

        if (hasRating && rideComment != null && !rideComment.isEmpty()) {
            tvRatingComment.setVisibility(View.VISIBLE);
            tvRatingComment.setText("\"" + rideComment + "\"");
        } else {
            tvRatingComment.setVisibility(View.GONE);
        }
    }

    // ─── Issues (read-only) ───────────────────────────────────────────────────

    private void renderIssuesReadOnly(LayoutInflater inflater, List<String> issues) {
        issuesSection.setVisibility(View.VISIBLE);
        issuesContainer.removeAllViews();

        if (issues != null && !issues.isEmpty()) {
            tvNoIssues.setVisibility(View.GONE);
            for (String issue : issues) {
                View iv = inflater.inflate(R.layout.item_issue, issuesContainer, false);
                ((TextView) iv.findViewById(R.id.tvIssueText)).setText(issue);
                issuesContainer.addView(iv);
            }
        } else {
            tvNoIssues.setVisibility(View.VISIBLE);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void applyStatusColor() {
        String s = ride.getStatus() != null ? ride.getStatus().toLowerCase() : "";
        int color = s.contains("cancel") ? R.color.button_red : R.color.button_green;
        btnStatus.setBackgroundTintList(getResources().getColorStateList(color, null));
    }

    private String buildName(String first, String last) {
        String n = first != null ? first : "";
        if (last != null) n += (n.isEmpty() ? "" : " ") + last;
        return n.trim();
    }

    private void addPersonCard(LayoutInflater inflater, LinearLayout container,
                               String name, String phone, String photoUrl) {
        View card = inflater.inflate(R.layout.item_driver, container, false);
        ((TextView) card.findViewById(R.id.tvDriverName)).setText(name);
        ((TextView) card.findViewById(R.id.tvDriverPhone)).setText(phone);
        ImageView iv = card.findViewById(R.id.ivDriverPhoto);
        if (iv != null) {
            iv.clearColorFilter();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this).load(photoUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop().into(iv);
            } else {
                iv.setImageResource(R.drawable.ic_user_placeholder);
            }
        }
        container.addView(card);
    }

    private String formatDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return "N/A";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(raw);
            return d != null ? new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(d) : raw;
        } catch (Exception e) { return raw; }
    }

    private String formatTime(String raw) {
        if (raw == null || raw.isEmpty()) return "N/A";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(raw);
            return d != null ? new SimpleDateFormat("HH:mm", Locale.getDefault()).format(d) : raw;
        } catch (Exception e) { return raw; }
    }
}