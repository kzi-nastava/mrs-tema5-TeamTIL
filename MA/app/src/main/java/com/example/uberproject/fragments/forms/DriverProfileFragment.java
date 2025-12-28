package com.example.uberproject.fragments.forms;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);

        btnEdit = view.findViewById(R.id.btnEditProfile);
        btnStatus = view.findViewById(R.id.btnStatusActive);

        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabVehicle = view.findViewById(R.id.tabVehicle);
        TextView tabMyRides = view.findViewById(R.id.tabMyRides);
        TextView tabHistory = view.findViewById(R.id.tabHistory);

        loadInfoFragment();

        btnEdit.setOnClickListener(v -> {
            if (!(infoFragment != null && infoFragment.isVisible())) {
                loadInfoFragment();
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
                Toast.makeText(getContext(), "Status changed!", Toast.LENGTH_SHORT).show();
            } else {
                btnStatus.setText(getString(R.string.active_title));
                btnStatus.setBackgroundResource(R.drawable.bg_button_green);
                isActive = true;
                Toast.makeText(getContext(), "Status changed!", Toast.LENGTH_SHORT).show();
            }
        });

        tabInfo.setOnClickListener(v -> loadInfoFragment());

        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            Toast.makeText(getContext(), "Profile info", Toast.LENGTH_SHORT).show();
        });

        tabVehicle.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Vehicle detalis clicked!", Toast.LENGTH_SHORT).show();
            if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
        });

        tabMyRides.setOnClickListener(v -> {
            Toast.makeText(getContext(), "My rides clicked!", Toast.LENGTH_SHORT).show();
            if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
        });

        tabHistory.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ride history clicked!", Toast.LENGTH_SHORT).show();
            if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void loadInfoFragment() {
        infoFragment = new ProfileInfoFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.profileContentContainer, infoFragment)
                .commit();
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
    }

    public void showEditButton() {
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
    }
}
