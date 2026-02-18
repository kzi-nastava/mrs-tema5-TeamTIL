package com.example.uberproject.fragments.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.PublicApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.request.RideEstimationRequestDTO;
import com.example.uberproject.dto.response.RideEstimationResponseDTO;
import com.example.uberproject.dto.response.VehicleStatusResponseDTO;
import com.example.uberproject.utils.GeocodingService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.osmdroid.config.Configuration;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {

    private WebView webView;
    private Button btnEstimateRide;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        webView = view.findViewById(R.id.webViewMap);
        btnEstimateRide = view.findViewById(R.id.btnEstimateRide);

        File cachePath = new File(requireContext().getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidBasePath(cachePath);
        Configuration.getInstance().setOsmdroidTileCache(cachePath);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.loadUrl("file:///android_asset/map.html");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loadActiveVehicles();
            }
        });

        btnEstimateRide.setOnClickListener(v -> showRideEstimationForm());

        return view;
    }

    private void showRideEstimationForm() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        View bottomSheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_ride_estimation, null);

        EditText etPickupAddress = bottomSheetView.findViewById(R.id.etPickupAddress);
        EditText etDestinationAddress = bottomSheetView.findViewById(R.id.etDestinationAddress);
        Button btnEstimate = bottomSheetView.findViewById(R.id.btnEstimate);
        Button btnRequestRide = bottomSheetView.findViewById(R.id.btnRequestRide);

        // Add text change listeners to show/hide estimate button
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pickup = etPickupAddress.getText().toString().trim();
                String destination = etDestinationAddress.getText().toString().trim();

                if (!pickup.isEmpty() && !destination.isEmpty()) {
                    btnEstimate.setVisibility(View.VISIBLE);
                } else {
                    btnEstimate.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etPickupAddress.addTextChangedListener(textWatcher);
        etDestinationAddress.addTextChangedListener(textWatcher);

        btnEstimate.setOnClickListener(v -> {
            String pickupAddress = etPickupAddress.getText().toString().trim();
            String destinationAddress = etDestinationAddress.getText().toString().trim();
            String vehicleType = "Standard";

            // Show loading indicator
            Toast.makeText(getContext(), "Calculating route...", Toast.LENGTH_SHORT).show();

            // Use geocoding service to get coordinates
            GeocodingService geocodingService = new GeocodingService();

            // Geocode pickup address
            geocodingService.geocodeAddress(pickupAddress, new GeocodingService.OnGeocodeListener() {
                @Override
                public void onSuccess(double pickupLat, double pickupLon) {
                    // Geocode destination address
                    geocodingService.geocodeAddress(destinationAddress, new GeocodingService.OnGeocodeListener() {
                        @Override
                        public void onSuccess(double destinationLat, double destinationLon) {
                            // Both geocoding successful, create request
                            RideEstimationRequestDTO request = new RideEstimationRequestDTO(
                                    pickupAddress,
                                    destinationAddress,
                                    vehicleType,
                                    pickupLat,
                                    pickupLon,
                                    destinationLat,
                                    destinationLon
                            );
                            estimateRide(request, bottomSheetDialog);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(getContext(),
                                    "Could not find destination: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(getContext(),
                            "Could not find pickup location: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnRequestRide.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Request ride feature coming soon", Toast.LENGTH_SHORT).show();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void estimateRide(RideEstimationRequestDTO request, BottomSheetDialog dialog) {
        PublicApi publicApi = RetrofitClient.getInstance(requireContext()).create(PublicApi.class);
        Call<RideEstimationResponseDTO> call = publicApi.estimateRide(request);

        call.enqueue(new Callback<RideEstimationResponseDTO>() {
            @Override
            public void onResponse(Call<RideEstimationResponseDTO> call, Response<RideEstimationResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    drawRideEstimation(response.body());
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to estimate ride", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RideEstimationResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRideEstimation(RideEstimationResponseDTO estimation) {
        if (estimation.getRouteCoordinates() != null && !estimation.getRouteCoordinates().isEmpty()) {
            List<double[]> coords = new java.util.ArrayList<>();
            for (List<Double> coord : estimation.getRouteCoordinates()) {
                if (coord.size() >= 2) {
                    coords.add(new double[]{coord.get(0), coord.get(1)});
                }
            }
            drawRoute(coords, estimation.getEstimatedTime());

            String details = String.format(
                    "Estimated: %s | Distance: %.1f km | Price: $%.2f",
                    estimation.getEstimatedTime(),
                    estimation.getEstimatedDistance(),
                    estimation.getEstimatedPrice()
            );
            Toast.makeText(getContext(), details, Toast.LENGTH_LONG).show();
        }
    }

    public void loadActiveVehicles(){
        PublicApi publicApi = RetrofitClient.getInstance(requireContext()).create(PublicApi.class);
        Call<List<VehicleStatusResponseDTO>> call = publicApi.getActiveVehicles();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<VehicleStatusResponseDTO>> call, Response<List<VehicleStatusResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VehicleStatusResponseDTO> vehicles = response.body();

                    clearMarkers();

                    for (VehicleStatusResponseDTO v : vehicles) {

                        if (v.getLatitude() != null &&
                                v.getLongitude() != null) {
                            addMarker(
                                    v.getLatitude(),
                                    v.getLongitude(),
                                    v.getName() + " (" + v.getLicensePlate() + ")",
                                    v.getAvailable()
                            );
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load active vehicles: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VehicleStatusResponseDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearMarkers() {
        if (webView != null) {
            webView.post(() ->
                    webView.evaluateJavascript("clearMarkers()", null)
            );
        }
    }

    public void addMarker(double lat, double lng, String title, boolean available) {
        String safeTitle = title.replace("'", "\\'");
        String js = "addMarker(" + lat + "," + lng + ",'" + safeTitle + "'," + available + ")";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }

    public void drawRoute(List<double[]> coords, String estimatedTime){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0; i<coords.size(); i++){
            double[] c = coords.get(i);
            sb.append("[").append(c[0]).append(",").append(c[1]).append("]");
            if(i<coords.size()-1) sb.append(",");
        }
        sb.append("]");
        String js = "drawRoute(" + sb.toString() + ",'" + estimatedTime + "')";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }
}

