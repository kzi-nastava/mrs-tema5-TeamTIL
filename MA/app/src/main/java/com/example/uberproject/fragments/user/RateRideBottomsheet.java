package com.example.uberproject.fragments.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.request.RatingRequestDTO;
import com.example.uberproject.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RateRideBottomsheet extends BottomSheetDialogFragment {

    public interface OnRatingSubmittedListener {
        void onRatingSubmitted();
    }

    private static final String ARG_RIDE_ID       = "rideId";
    private static final String ARG_DRIVER_NAME   = "driverName";
    private static final String ARG_VEHICLE_MODEL = "vehicleModel";
    private static final String ARG_VEHICLE_PLATE = "vehiclePlate";
    private static final String ARG_EXISTING_COMMENT = "existingComment";

    private int rideId;
    private String driverName, vehicleModel, vehiclePlate, existingComment;

    private int driverRating  = 0;
    private int vehicleRating = 0;

    private ImageView[] driverStars  = new ImageView[5];
    private ImageView[] vehicleStars = new ImageView[5];
    private EditText   etComment;
    private Button     btnSubmit;

    private OnRatingSubmittedListener listener;

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static RateRideBottomsheet newInstance(int rideId, String driverName,
                                                  String vehicleModel, String vehiclePlate,
                                                  String existingComment) {
        RateRideBottomsheet sheet = new RateRideBottomsheet();
        Bundle args = new Bundle();
        args.putInt(ARG_RIDE_ID, rideId);
        args.putString(ARG_DRIVER_NAME,   driverName   != null ? driverName   : "");
        args.putString(ARG_VEHICLE_MODEL, vehicleModel != null ? vehicleModel : "");
        args.putString(ARG_VEHICLE_PLATE, vehiclePlate != null ? vehiclePlate : "");
        args.putString(ARG_EXISTING_COMMENT, existingComment != null ? existingComment : "");
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnRatingSubmittedListener(OnRatingSubmittedListener l) {
        this.listener = l;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rideId          = getArguments().getInt(ARG_RIDE_ID, -1);
            driverName      = getArguments().getString(ARG_DRIVER_NAME, "");
            vehicleModel    = getArguments().getString(ARG_VEHICLE_MODEL, "");
            vehiclePlate    = getArguments().getString(ARG_VEHICLE_PLATE, "");
            existingComment = getArguments().getString(ARG_EXISTING_COMMENT, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rate_ride, container, false);

        // Driver section
        TextView tvDriverName = view.findViewById(R.id.tvRatingDriverName);
        tvDriverName.setText(driverName.isEmpty() ? "Driver" : driverName);

        driverStars[0] = view.findViewById(R.id.driverStar1);
        driverStars[1] = view.findViewById(R.id.driverStar2);
        driverStars[2] = view.findViewById(R.id.driverStar3);
        driverStars[3] = view.findViewById(R.id.driverStar4);
        driverStars[4] = view.findViewById(R.id.driverStar5);

        // Vehicle section
        StringBuilder vehicleLabel = new StringBuilder();
        if (!vehicleModel.isEmpty()) vehicleLabel.append(vehicleModel);
        if (!vehiclePlate.isEmpty()) {
            if (vehicleLabel.length() > 0) vehicleLabel.append("  •  ");
            vehicleLabel.append(vehiclePlate);
        }
        TextView tvVehicleName = view.findViewById(R.id.tvRatingVehicleName);
        tvVehicleName.setText(vehicleLabel.length() > 0 ? vehicleLabel.toString() : "Vehicle");

        vehicleStars[0] = view.findViewById(R.id.vehicleStar1);
        vehicleStars[1] = view.findViewById(R.id.vehicleStar2);
        vehicleStars[2] = view.findViewById(R.id.vehicleStar3);
        vehicleStars[3] = view.findViewById(R.id.vehicleStar4);
        vehicleStars[4] = view.findViewById(R.id.vehicleStar5);

        etComment = view.findViewById(R.id.etRatingComment);
        btnSubmit = view.findViewById(R.id.btnSubmitRating);

        // Pre-fill comment if editing
        if (!existingComment.isEmpty()) {
            etComment.setText(existingComment);
        }

        // Star click listeners
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            driverStars[i].setOnClickListener(v -> setDriverRating(rating));
            vehicleStars[i].setOnClickListener(v -> setVehicleRating(rating));
        }

        btnSubmit.setOnClickListener(v -> submitRating());

        return view;
    }

    // ─── Star Helpers ─────────────────────────────────────────────────────────

    private void setDriverRating(int value) {
        driverRating = value;
        updateStars(driverStars, value);
    }

    private void setVehicleRating(int value) {
        vehicleRating = value;
        updateStars(vehicleStars, value);
    }

    private void updateStars(ImageView[] stars, int value) {
        for (int i = 0; i < 5; i++) {
            stars[i].setImageResource(i < value
                    ? R.drawable.ic_star_filled2
                    : R.drawable.ic_star_empty);
        }
    }

    // ─── Submit ───────────────────────────────────────────────────────────────

    private void submitRating() {
        if (driverRating == 0 || vehicleRating == 0) {
            Toast.makeText(getContext(),
                    "Please rate both driver and vehicle", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = TokenManager.getInstance(requireContext()).getUserEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        RatingRequestDTO request = new RatingRequestDTO(email, driverRating, vehicleRating, comment);

        RideApi api = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        api.rateRide(rideId, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onRatingSubmitted();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to submit rating", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void resetButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit Rating");
    }
}