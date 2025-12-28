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

public class DriverProfileFragment extends Fragment {

    private ProfileInfoFragment infoFragment;
    private Button btnEdit;
    private Button btnStatus;
    private boolean isActive = true;

    private TextView tabInfo, tabVehicle, tabMyRides, tabHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);

        btnEdit = view.findViewById(R.id.btnEditProfile);
        btnStatus = view.findViewById(R.id.btnStatusActive);

        tabInfo = view.findViewById(R.id.tabInfo);
        tabVehicle = view.findViewById(R.id.tabVehicle);
        tabMyRides = view.findViewById(R.id.tabMyRides);
        tabHistory = view.findViewById(R.id.tabHistory);

        loadInfoFragment();
        updateTabStyles(tabInfo, tabVehicle, tabMyRides, tabHistory);

        btnEdit.setOnClickListener(v -> {
            if (!(infoFragment != null && infoFragment.isVisible())) {
                loadInfoFragment();
                updateTabStyles(tabInfo, tabVehicle, tabMyRides, tabHistory);
            }
            if (infoFragment != null) {
                infoFragment.toggleEditing(true);
                btnEdit.setVisibility(View.GONE);
            }
        });

        // STATUS BUTTON
        btnStatus.setOnClickListener(v -> {
            if (isActive) {
                btnStatus.setText(getString(R.string.inactive_title));
                btnStatus.setBackgroundResource(R.drawable.bg_button_red);
                isActive = false;
            } else {
                btnStatus.setText(getString(R.string.active_title));
                btnStatus.setBackgroundResource(R.drawable.bg_button_green);
                isActive = true;
            }
        });

        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            updateTabStyles(tabInfo, tabVehicle, tabMyRides, tabHistory);
        });

        tabVehicle.setOnClickListener(v -> {
            VehicleDetailsFragment vehicleFragment = new VehicleDetailsFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profileContentContainer, vehicleFragment)
                    .commit();

            btnEdit.setVisibility(View.GONE);
            updateTabStyles(tabVehicle, tabInfo, tabMyRides, tabHistory);
        });

        tabMyRides.setOnClickListener(v -> {
            Toast.makeText(getContext(), "My rides clicked!", Toast.LENGTH_SHORT).show();

            btnEdit.setVisibility(View.GONE);

            updateTabStyles(tabMyRides, tabInfo, tabVehicle, tabHistory);
        });

        tabHistory.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ride history clicked!", Toast.LENGTH_SHORT).show();

            btnEdit.setVisibility(View.GONE);

            updateTabStyles(tabHistory, tabInfo, tabVehicle, tabMyRides);
        });

        return view;
    }

    private void loadInfoFragment() {
        infoFragment = new ProfileInfoFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.profileContentContainer, infoFragment)
                .commit();
        btnEdit.setVisibility(View.VISIBLE);
    }

    private void updateTabStyles(TextView selectedTab, TextView... otherTabs) {
        selectedTab.setTypeface(null, Typeface.BOLD);
        for (TextView tab : otherTabs) {
            tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void showEditButton() {
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
    }
}