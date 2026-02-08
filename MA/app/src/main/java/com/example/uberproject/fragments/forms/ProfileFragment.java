package com.example.uberproject.fragments.forms;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.uberproject.R;
import com.example.uberproject.dto.response.DriverResponseDTO;
import com.example.uberproject.utils.TokenManager;

public class ProfileFragment extends Fragment {

    private ProfileInfoFragment infoFragment;
    private Button btnEdit;
    private String userRole;


    private boolean isDriverActive = true;
    private DriverResponseDTO currentDriverData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userRole = TokenManager.getInstance(getContext()).getUserRole();
        View view;

        // 1. ODREDJIVANJE LAYOUTA
        if ("ADMINISTRATOR".equalsIgnoreCase(userRole)) {
            view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
            setupAdminLogic(view);
        } else if ("DRIVER".equalsIgnoreCase(userRole)) {
            view = inflater.inflate(R.layout.fragment_driver_profile, container, false);
            setupDriverLogic(view);
        } else {
            view = inflater.inflate(R.layout.fragment_profile, container, false);
            setupPassengerLogic(view);
        }

        // 2. ZAJEDNICKI ELEMENTI
        btnEdit = view.findViewById(R.id.btnEditProfile);
        loadInfoFragment();

        btnEdit.setOnClickListener(v -> {
            if (infoFragment != null) {
                infoFragment.toggleEditing(true);
                btnEdit.setVisibility(View.GONE);
            }
        });

        return view;
    }

    // --- LOGIKA ZA ADMINA ---
    private void setupAdminLogic(View view) {
        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabUpdates = view.findViewById(R.id.tabDriverUpdates);
        TextView tabBlocking = view.findViewById(R.id.tabBlocking);
        TextView tabPanic = view.findViewById(R.id.tabPanic);

        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            updateTabStyles(tabInfo, tabUpdates, tabBlocking, tabPanic);
        });

        tabUpdates.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Driver Updates!", Toast.LENGTH_SHORT).show();
            updateTabStyles(tabUpdates, tabInfo, tabBlocking, tabPanic);
            btnEdit.setVisibility(View.GONE);
        });

        tabBlocking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Blocking!", Toast.LENGTH_SHORT).show();
            updateTabStyles(tabBlocking, tabInfo, tabUpdates, tabPanic);
            btnEdit.setVisibility(View.GONE);
        });

        tabPanic.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Panic notifications!", Toast.LENGTH_SHORT).show();
            updateTabStyles(tabPanic, tabInfo, tabUpdates, tabBlocking);
            btnEdit.setVisibility(View.GONE);
        });

        updateTabStyles(tabInfo, tabUpdates, tabBlocking, tabPanic);
    }

    // --- LOGIKA ZA VOZACA ---
    private void setupDriverLogic(View view) {
        Button btnStatus = view.findViewById(R.id.btnStatusActive);
        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabVehicle = view.findViewById(R.id.tabVehicle);

        com.example.uberproject.api.UserApi api = com.example.uberproject.api.RetrofitClient.getInstance(getContext()).create(com.example.uberproject.api.UserApi.class);

        // UCITAVANJE TRENUTNOG STATUSA IZ BAZE
        api.getDriverProfile().enqueue(new retrofit2.Callback<DriverResponseDTO>() {
            @Override
            public void onResponse(retrofit2.Call<DriverResponseDTO> call, retrofit2.Response<DriverResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentDriverData = response.body();
                    isDriverActive = currentDriverData.getIsActive();
                    updateStatusButtonUI(btnStatus); // podesi boju na osnovu baze
                }
            }
            @Override
            public void onFailure(retrofit2.Call<DriverResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading status", Toast.LENGTH_SHORT).show();
            }
        });

        // PROMENA STATUSA NA KLIK
        btnStatus.setOnClickListener(v -> {
            if (currentDriverData == null) return;

            boolean newStatus = !isDriverActive;
            currentDriverData.setIsActive(newStatus); // postavi novi status u objekat

            // salji ceo objekat na server
            api.updateDriverProfile(currentDriverData).enqueue(new retrofit2.Callback<DriverResponseDTO>() {
                @Override
                public void onResponse(retrofit2.Call<DriverResponseDTO> call, retrofit2.Response<DriverResponseDTO> response) {
                    if (response.isSuccessful()) {
                        isDriverActive = newStatus;
                        updateStatusButtonUI(btnStatus);
                        Toast.makeText(getContext(), "Status updated!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<DriverResponseDTO> call, Throwable t) {
                    Toast.makeText(getContext(), "Failed to update status on server", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // tabovi ostaju isti
        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            updateTabStyles(tabInfo, tabVehicle);
            if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
        });

        tabVehicle.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.profileContentContainer, new VehicleDetailsFragment())
                    .commit();
            updateTabStyles(tabVehicle, tabInfo);
            if (btnEdit != null) btnEdit.setVisibility(View.GONE);
        });

        updateTabStyles(tabInfo, tabVehicle);
    }

    private void updateStatusButtonUI(Button btnStatus) {
        btnStatus.setText(isDriverActive ? getString(R.string.active_title) : getString(R.string.inactive_title));
        btnStatus.setBackgroundResource(isDriverActive ? R.drawable.bg_button_green : R.drawable.bg_button_red);
    }

    // --- LOGIKA ZA KORISNIKA ---
    private void setupPassengerLogic(View view) {
        TextView tabInfo = view.findViewById(R.id.tabInfo);
        if (tabInfo != null) tabInfo.setTypeface(null, Typeface.BOLD);
    }

    // pomocne metode
    private void loadInfoFragment() {
        infoFragment = new ProfileInfoFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.profileContentContainer, infoFragment)
                .commit();
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
    }

    private void updateTabStyles(TextView selectedTab, TextView... otherTabs) {
        selectedTab.setTypeface(null, Typeface.BOLD);
        for (TextView tab : otherTabs) {
            if (tab != null) tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void showEditButton() {
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
    }
}