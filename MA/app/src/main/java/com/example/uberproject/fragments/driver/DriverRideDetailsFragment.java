package com.example.uberproject.fragments.driver;

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

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.response.DriverRideHistoryResponseDTO;
import com.example.uberproject.dto.response.RideDetailsResponseDTO;
import com.example.uberproject.utils.GeocodingService;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideDetailsFragment extends Fragment {

    private static final String ARG_RIDE = "arg_ride";
    private static final String TAG = "DriverRideDetailsFragment";

    private DriverRideHistoryResponseDTO rideDTO;

    // Details views
    private LinearLayout passengersContainer;
    private TextView addressText, tvStartTime, tvEndTime, tvDuration, tvDistance, tvPrice;
    private Button btnStatus;
    private WebView webViewRideMap;

    // Rating section views (read-only)
    private LinearLayout ratingSection;
    private TextView tvRatingStatus, tvRatingComment;
    private ImageView[] ratingStars = new ImageView[5];

    // Issues section views (read-only)
    private LinearLayout issuesSection, issuesContainer;
    private TextView tvNoIssues;

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static DriverRideDetailsFragment newInstance(DriverRideHistoryResponseDTO rideDTO) {
        DriverRideDetailsFragment fragment = new DriverRideDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, rideDTO);
        fragment.setArguments(args);
        return fragment;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_ride_history_details, container, false);

        if (getArguments() != null) {
            rideDTO = (DriverRideHistoryResponseDTO) getArguments().getSerializable(ARG_RIDE);
        }

        bindViews(view);
        setupMap();
        populateRideData(inflater);
        fetchDetailsFromApi(inflater);

        return view;
    }

    // ─── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        passengersContainer = view.findViewById(R.id.passengersContainer);
        addressText         = view.findViewById(R.id.tvRideAddress);
        tvStartTime         = view.findViewById(R.id.tvStartTime);
        tvEndTime           = view.findViewById(R.id.tvEndTime);
        tvDuration          = view.findViewById(R.id.tvDuration);
        tvDistance          = view.findViewById(R.id.tvDistance);
        tvPrice             = view.findViewById(R.id.tvPrice);
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
            @Override
            public void onPageFinished(WebView v, String url) { drawRouteOnMap(); }
        });
        webViewRideMap.loadUrl("file:///android_asset/map.html");
    }

    private void drawRouteOnMap() {
        if (rideDTO == null) return;
        String from = rideDTO.getFrom();
        String to   = rideDTO.getTo();
        if (from == null || to == null) return;

        GeocodingService geo = new GeocodingService();
        geo.geocodeAddress(from + ", Novi Sad", new GeocodingService.OnGeocodeListener() {
            @Override public void onSuccess(double sLat, double sLon) {
                geo.geocodeAddress(to + ", Novi Sad", new GeocodingService.OnGeocodeListener() {
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

    // ─── Populate base data ───────────────────────────────────────────────────

    private void populateRideData(LayoutInflater inflater) {
        if (rideDTO == null) return;

        String from = rideDTO.getFrom() != null ? rideDTO.getFrom().split(",")[0] : "";
        String to   = rideDTO.getTo()   != null ? rideDTO.getTo().split(",")[0]   : "";
        addressText.setText(from + " → " + to);

        if (rideDTO.getPassengers() != null) {
            for (DriverRideHistoryResponseDTO.PassengerDTO p : rideDTO.getPassengers()) {
                addPassenger(inflater, p.getName(), p.getPhone());
            }
        }

        tvStartTime.setText(rideDTO.getDate() != null && rideDTO.getStartTime() != null
                ? rideDTO.getDate() + ", " + rideDTO.getStartTime() : "N/A");
        tvEndTime.setText(rideDTO.getEndTime() != null ? rideDTO.getEndTime() : "N/A");
        tvDuration.setText(rideDTO.getDuration() != null ? rideDTO.getDuration() : "N/A");
        tvDistance.setText(rideDTO.getDistance() != null ? rideDTO.getDistance() : "N/A");
        tvPrice.setText(rideDTO.getPrice() != null ? rideDTO.getPrice() : "N/A");

        btnStatus.setText(rideDTO.getStatus());
        if ("Completed".equalsIgnoreCase(rideDTO.getStatus())) {
            btnStatus.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_green, null));
        } else if ("Canceled".equalsIgnoreCase(rideDTO.getStatus())) {
            btnStatus.setBackgroundTintList(
                    getResources().getColorStateList(R.color.button_red, null));
        }
    }

    // ─── Fetch rating + issues from API ──────────────────────────────────────

    private void fetchDetailsFromApi(LayoutInflater inflater) {
        if (rideDTO == null || rideDTO.getId() <= 0) return;

        RetrofitClient.getInstance(requireContext()).create(RideApi.class)
                .getRideDetails(rideDTO.getId())
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

    private void addPassenger(LayoutInflater inflater, String name, String phone) {
        View v = inflater.inflate(R.layout.item_passenger, passengersContainer, false);
        ((TextView) v.findViewById(R.id.tvPassengerName)).setText(name != null ? name : "");
        ((TextView) v.findViewById(R.id.tvPassengerPhone)).setText(phone != null ? phone : "");
        passengersContainer.addView(v);
    }
}