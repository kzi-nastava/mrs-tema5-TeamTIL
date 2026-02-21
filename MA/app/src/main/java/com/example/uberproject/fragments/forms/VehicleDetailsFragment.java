package com.example.uberproject.fragments.forms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.UserApi;
import com.example.uberproject.dto.response.DriverResponseDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleDetailsFragment extends Fragment {
    private TextView tvModel, tvType, tvPlate, tvCapacity, tvBabyValue, tvPetValue;
    private ImageView ivBabyIcon, ivPetIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_vehicle_details, container, false);

        tvModel = view.findViewById(R.id.tvModelValue);
        tvType = view.findViewById(R.id.tvTypeValue);
        tvPlate = view.findViewById(R.id.tvPlateValue);
        tvCapacity = view.findViewById(R.id.tvCapacityValue);

        tvBabyValue = view.findViewById(R.id.tvBabyValue);
        tvPetValue = view.findViewById(R.id.tvPetValue);
        ivBabyIcon = view.findViewById(R.id.ivBabyIcon);
        ivPetIcon = view.findViewById(R.id.ivPetIcon);

        loadVehicleData();

        return view;
    }

    private void loadVehicleData() {
        UserApi api = RetrofitClient.getInstance(getContext()).create(UserApi.class);

        api.getDriverProfile().enqueue(new Callback<DriverResponseDTO>() {
            @Override
            public void onResponse(Call<DriverResponseDTO> call, Response<DriverResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    DriverResponseDTO driver = response.body();

                    tvModel.setText(driver.getVehicleModel());
                    tvType.setText(driver.getVehicleType());
                    tvPlate.setText(driver.getLicensePlate());
                    tvCapacity.setText(String.valueOf(driver.getPassengerCapacity()));

                    // dinamicka logika za Baby Friendly
                    boolean isBaby = driver.getBabyFriendly() != null && driver.getBabyFriendly();
                    if (isBaby) {
                        tvBabyValue.setText(getString(R.string.yes_title));
                        ivBabyIcon.setImageResource(R.drawable.ic_check);
                        ivBabyIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        tvBabyValue.setText(getString(R.string.no_title));
                        ivBabyIcon.setImageResource(R.drawable.ic_close_ic);
                        ivBabyIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
                    }

                    // dinamicka logika za Pet Friendly
                    boolean isPet = driver.getPetFriendly() != null && driver.getPetFriendly();
                    if (isPet) {
                        tvPetValue.setText(getString(R.string.yes_title));
                        ivPetIcon.setImageResource(R.drawable.ic_check);
                        ivPetIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        tvPetValue.setText(getString(R.string.no_title));
                        ivPetIcon.setImageResource(R.drawable.ic_close_ic);
                        ivPetIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
                    }
                }
            }

            @Override
            public void onFailure(Call<DriverResponseDTO> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading vehicle info", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}