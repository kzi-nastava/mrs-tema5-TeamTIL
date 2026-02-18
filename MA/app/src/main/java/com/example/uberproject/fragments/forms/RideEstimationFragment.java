package com.example.uberproject.fragments.forms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.uberproject.utils.GeocodingService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideEstimationFragment extends Fragment {

    private EditText etPickupAddress, etDestinationAddress;
    private Button btnEstimate, btnRequestRide;
    private OnRideEstimatedListener listener;

    public interface OnRideEstimatedListener {
        void onRideEstimated(RideEstimationResponseDTO estimation);
    }

    public RideEstimationFragment() {
    }

    public void setOnRideEstimatedListener(OnRideEstimatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ride_estimation, container, false);

        etPickupAddress = view.findViewById(R.id.etPickupAddress);
        etDestinationAddress = view.findViewById(R.id.etDestinationAddress);
        btnEstimate = view.findViewById(R.id.btnEstimate);
        btnRequestRide = view.findViewById(R.id.btnRequestRide);

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

        btnEstimate.setOnClickListener(v -> handleEstimateRide());
        btnRequestRide.setOnClickListener(v -> handleRequestRide());

        return view;
    }

    private void handleEstimateRide() {
        String pickupAddress = etPickupAddress.getText().toString().trim();
        String destinationAddress = etDestinationAddress.getText().toString().trim();
        String vehicleType = "Standard";

        if (pickupAddress.isEmpty() || destinationAddress.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Calculating route...", Toast.LENGTH_SHORT).show();

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
                        estimateRide(request);
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
    }

    private void handleRequestRide() {
        Toast.makeText(getContext(), "Request ride feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void estimateRide(RideEstimationRequestDTO request) {
        PublicApi publicApi = RetrofitClient.getInstance(requireContext()).create(PublicApi.class);
        Call<RideEstimationResponseDTO> call = publicApi.estimateRide(request);

        call.enqueue(new Callback<RideEstimationResponseDTO>() {
            @Override
            public void onResponse(Call<RideEstimationResponseDTO> call, Response<RideEstimationResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RideEstimationResponseDTO estimation = response.body();
                    if (listener != null) {
                        listener.onRideEstimated(estimation);
                    }
                    showEstimationDetails(estimation);
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

    private void showEstimationDetails(RideEstimationResponseDTO estimation) {
        String details = String.format(
                "Estimated Time: %s\nDistance: %.2f km\nPrice: $%.2f\nVehicle: %s",
                estimation.getEstimatedTime(),
                estimation.getEstimatedDistance(),
                estimation.getEstimatedPrice(),
                estimation.getVehicleType()
        );
        Toast.makeText(getContext(), details, Toast.LENGTH_LONG).show();
    }
}



