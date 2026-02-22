package com.example.uberproject.fragments.tracking;

import android.content.Intent;
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

import com.example.uberproject.BuildConfig;
import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.response.RideTrackingResponseDTO;
import com.example.uberproject.fragments.user.ReportInconsistencyBottomSheet;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import com.example.uberproject.websocket.RideTrackingWebSocketClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackRideFragment extends Fragment {

    private static final String TAG = "TrackRideFragment";
    private static final String ARG_RIDE_ID = "rideId";

    // ---------- UI refs ----------
    private WebView webViewMap;
    private TextView tvRoute;
    private TextView tvStartTime;
    private TextView tvEta;
    private TextView tvCurrentPrice;
    private TextView tvPersonName;
    private TextView tvPersonPhone;
    private TextView tvPersonLabel;
    private LinearLayout layoutLiveIndicator;
    private LinearLayout layoutRideEnded;
    private LinearLayout layoutAccessDenied;
    private LinearLayout layoutLoading;
    private LinearLayout layoutContent;
    private Button btnReportInconsistency;
    private ImageView btnGoBack;
    private Button btnGoBackDenied;

    // ---------- State ----------
    private int rideId = -1;
    private RideTrackingResponseDTO trackingData;
    private RideTrackingWebSocketClient wsClient;
    private boolean mapReady = false;
    private double lastLat = 0;
    private double lastLng = 0;
    private boolean isPassenger = false;
    private boolean isDriver = false;

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static TrackRideFragment newInstance(int rideId) {
        TrackRideFragment f = new TrackRideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RIDE_ID, rideId);
        f.setArguments(args);
        return f;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_track_ride, container, false);

        // Parse arguments
        if (getArguments() != null) {
            rideId = getArguments().getInt(ARG_RIDE_ID, -1);
        }

        bindViews(view);
        determineUserRole();
        setupMap(view);
        setupButtonListeners();

        if (rideId <= 0) {
            showAccessDenied();
        } else {
            showLoading();
            loadTrackingData();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disconnectWebSocket();
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        webViewMap           = view.findViewById(R.id.webViewTrackMap);
        tvRoute              = view.findViewById(R.id.tvTrackRoute);
        tvStartTime          = view.findViewById(R.id.tvTrackStartTime);
        tvEta                = view.findViewById(R.id.tvTrackEta);
        tvCurrentPrice       = view.findViewById(R.id.tvTrackCurrentPrice);
        tvPersonName         = view.findViewById(R.id.tvTrackPersonName);
        tvPersonPhone        = view.findViewById(R.id.tvTrackPersonPhone);
        tvPersonLabel        = view.findViewById(R.id.tvTrackPersonLabel);
        layoutLiveIndicator  = view.findViewById(R.id.layoutLiveIndicator);
        layoutRideEnded      = view.findViewById(R.id.layoutRideEnded);
        layoutAccessDenied   = view.findViewById(R.id.layoutAccessDenied);
        layoutLoading        = view.findViewById(R.id.layoutTrackLoading);
        layoutContent        = view.findViewById(R.id.layoutTrackContent);
        btnReportInconsistency = view.findViewById(R.id.btnReportInconsistency);
        btnGoBack            = view.findViewById(R.id.btnTrackGoBack);
        btnGoBackDenied      = view.findViewById(R.id.btnTrackGoBackDenied);
    }

    private void determineUserRole() {
        String role = AuthGuard.getUserRole(requireContext());
        isPassenger = "REGISTERED_USER".equalsIgnoreCase(role);
        isDriver    = "DRIVER".equalsIgnoreCase(role);
    }

    private void setupMap(View view) {
        WebSettings settings = webViewMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView wv, String url) {
                mapReady = true;
                // Ako već imamo podatke, odmah ucrtaj rutu
                if (trackingData != null) {
                    drawRouteOnMap();
                    if (lastLat != 0) updateVehicleOnMap(lastLat, lastLng);
                }
            }
        });

        webViewMap.loadUrl("file:///android_asset/map.html");
    }

    private void setupButtonListeners() {
        btnGoBack.setOnClickListener(v -> navigateBack());
        btnGoBackDenied.setOnClickListener(v -> navigateBack());

        // Report inconsistency - samo za putnike
        btnReportInconsistency.setOnClickListener(v -> {
            if (rideId > 0) {
                openReportInconsistency();
            }
        });
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private void loadTrackingData() {
        RideApi api = RetrofitClient.getInstance(requireContext()).create(RideApi.class);

        api.getRideTracking(rideId).enqueue(new Callback<RideTrackingResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RideTrackingResponseDTO> call,
                                   @NonNull Response<RideTrackingResponseDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    trackingData = response.body();
                    populateStaticInfo();
                    showContent();

                    if (mapReady) drawRouteOnMap();

                    // WebSocket za live updates
                    connectWebSocket();

                } else if (response.code() == 403 || response.code() == 401) {
                    showAccessDenied();
                } else {
                    showAccessDenied();
                    Log.e(TAG, "Tracking error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideTrackingResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showAccessDenied();
                Log.e(TAG, "Tracking network error", t);
            }
        });
    }

    private void populateStaticInfo() {
        if (trackingData == null) return;

        // Ruta
        String from = shortenAddress(trackingData.getStartAddress());
        String to   = shortenAddress(trackingData.getEndAddress());
        tvRoute.setText(String.format("%s → %s", from, to));
        tvStartTime.setText(trackingData.getStartTime() != null ? trackingData.getStartTime() : "—");

        // Osoba (driver vidi putnika, putnik vidi vozača, admin vidi vozača)
        if (isDriver) {
            tvPersonLabel.setText(R.string.passenger);
            tvPersonName.setText(trackingData.getPassengerName() != null ? trackingData.getPassengerName() : "—");
            tvPersonPhone.setText(trackingData.getPassengerPhone() != null ? trackingData.getPassengerPhone() : "");
            btnReportInconsistency.setVisibility(View.GONE); // vozač ne može prijaviti
        } else {
            // Putnik ili admin
            tvPersonLabel.setText(R.string.driver);
            tvPersonName.setText(trackingData.getDriverName() != null ? trackingData.getDriverName() : "—");
            tvPersonPhone.setText(trackingData.getDriverPhone() != null ? trackingData.getDriverPhone() : "");
            btnReportInconsistency.setVisibility(isPassenger ? View.VISIBLE : View.GONE);
        }

        // Postavi početnu poziciju vozila (startLatitude iz REST odgovora)
        if (trackingData.getStartLatitude() != null && trackingData.getStartLongitude() != null) {
            lastLat = trackingData.getStartLatitude();
            lastLng = trackingData.getStartLongitude();
        }
    }

    // ─── WebSocket ────────────────────────────────────────────────────────────

    private void connectWebSocket() {
        String token = TokenManager.getInstance(requireContext()).getToken();
        if (token == null) {
            Log.e(TAG, "No token available for WebSocket");
            return;
        }


        String wsUrl = BuildConfig.WS_HOST + "/ws/ride-tracking";

        wsClient = new RideTrackingWebSocketClient(wsUrl, rideId);
        wsClient.setListener(new RideTrackingWebSocketClient.TrackingListener() {

            @Override
            public void onPositionUpdate(double lat, double lng, int remainingMin, int currentPrice) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    lastLat = lat;
                    lastLng = lng;

                    // Ažuriraj ETA i cenu
                    tvEta.setText(remainingMin + " min");
                    tvCurrentPrice.setText(currentPrice + " RSD");

                    // Ažuriraj poziciju vozila na mapi
                    if (mapReady) updateVehicleOnMap(lat, lng);
                });
            }

            @Override
            public void onRideEnded() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showRideEnded();
                });
            }

            @Override
            public void onRideNotActive() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showAccessDenied();
                });
            }

            @Override
            public void onConnected() {
                Log.d(TAG, "WebSocket connected for rideId=" + rideId);
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "WebSocket disconnected for rideId=" + rideId);
            }
        });

        wsClient.connect(token);
    }

    private void disconnectWebSocket() {
        if (wsClient != null) {
            wsClient.disconnect();
            wsClient = null;
        }
    }

    // ─── Map helpers ──────────────────────────────────────────────────────────

    /**
     * Ucrtaj polazište i odredište na mapi.
     * Koristimo geocoding jer imamo samo adrese (startLatitude/Longitude su tu kao backup).
     */
    private void drawRouteOnMap() {
        if (trackingData == null || webViewMap == null) return;

        Double startLat = trackingData.getStartLatitude();
        Double startLng = trackingData.getStartLongitude();
        Double endLat   = trackingData.getEndLatitude();
        Double endLng   = trackingData.getEndLongitude();

        if (startLat != null && startLng != null && endLat != null && endLng != null) {
            fetchAndDrawRoute(startLat, startLng, endLat, endLng);
        } else {
            Log.w(TAG, "No coordinates in tracking data, route won't be drawn");
        }
    }

    /**
     * Ažuriraj marker vozila na mapi.
     * Koristi clearMarkers + addMarker jer je map.html pisan za to.
     */
    private void updateVehicleOnMap(double lat, double lng) {
        if (webViewMap == null || !mapReady) return;
        webViewMap.post(() -> {
            String js = String.format("updateVehicleMarker(%s, %s)", lat, lng);
            webViewMap.evaluateJavascript(js, null);
        });
    }

    private void fetchAndDrawRoute(double startLat, double startLng, double endLat, double endLng) {
        new Thread(() -> {
            try {
                String url = String.format(
                        "http://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                        startLng, startLat, endLng, endLat
                );

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
                okhttp3.Response response = client.newCall(request).execute();

                if (response.body() == null) return;
                String json = response.body().string();

                org.json.JSONArray coords = new org.json.JSONObject(json)
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                // Napravi JS niz [[lat,lng], ...]
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < coords.length(); i++) {
                    org.json.JSONArray point = coords.getJSONArray(i);
                    double lng = point.getDouble(0);
                    double lat = point.getDouble(1);
                    if (i > 0) sb.append(",");
                    sb.append("[").append(lat).append(",").append(lng).append("]");
                }
                sb.append("]");

                String jsArray = sb.toString();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (mapReady) {
                            String js = "drawRoute(" + jsArray + ", '')";
                            webViewMap.evaluateJavascript(js, null);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching route from OSRM", e);
            }
        }).start();
    }

    // ─── UI State ─────────────────────────────────────────────────────────────

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
        layoutRideEnded.setVisibility(View.GONE);
        layoutAccessDenied.setVisibility(View.GONE);
    }

    private void showContent() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
        layoutRideEnded.setVisibility(View.GONE);
        layoutAccessDenied.setVisibility(View.GONE);
        layoutLiveIndicator.setVisibility(View.VISIBLE);
    }

    private void showRideEnded() {
        disconnectWebSocket();
        layoutLiveIndicator.setVisibility(View.GONE);
        layoutRideEnded.setVisibility(View.VISIBLE);
    }

    private void showAccessDenied() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.GONE);
        layoutRideEnded.setVisibility(View.GONE);
        layoutAccessDenied.setVisibility(View.VISIBLE);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private void openReportInconsistency() {
        String passengerEmail = TokenManager.getInstance(requireContext()).getUserEmail();
        ReportInconsistencyBottomSheet sheet =
                ReportInconsistencyBottomSheet.newInstance(rideId, passengerEmail);
        sheet.show(getChildFragmentManager(), "report_inconsistency");
    }

    private void navigateBack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // ─── Utils ────────────────────────────────────────────────────────────────

    private String shortenAddress(String address) {
        if (address == null || address.isEmpty()) return "";
        return address.split(",")[0];
    }
}