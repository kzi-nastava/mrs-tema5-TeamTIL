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

public class ProfileFragment extends Fragment {

    private ProfileInfoFragment infoFragment;
    private Button btnEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnEdit = view.findViewById(R.id.btnEditProfile);

        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabSecondary = view.findViewById(R.id.tabSecondary);
        TextView tabHistory = view.findViewById(R.id.tabHistory);

        // load the profile info
        loadInfoFragment();

        // EDIT
        btnEdit.setOnClickListener(v -> {
            if (infoFragment != null) {
                infoFragment.toggleEditing(true);
                btnEdit.setVisibility(View.GONE);
            }
        });

        // TABS
        tabSecondary.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Favorite rides clicked!", Toast.LENGTH_SHORT).show();
        });

        tabHistory.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ride history clicked!", Toast.LENGTH_SHORT).show();
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
        if (btnEdit != null) {
            btnEdit.setVisibility(View.VISIBLE);
        }
    }
}