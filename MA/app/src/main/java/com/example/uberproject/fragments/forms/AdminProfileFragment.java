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

public class AdminProfileFragment extends Fragment {

    private ProfileInfoFragment infoFragment;
    private Button btnEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // load the ADMIN layout
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        btnEdit = view.findViewById(R.id.btnEditProfile);

        // tabs
        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabUpdates = view.findViewById(R.id.tabDriverUpdates);
        TextView tabBlocking = view.findViewById(R.id.tabBlocking);
        TextView tabPanic = view.findViewById(R.id.tabPanic);

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
        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            Toast.makeText(getContext(), "Profile info", Toast.LENGTH_SHORT).show();
        });

        tabUpdates.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Driver Updates clicked!", Toast.LENGTH_SHORT).show();
        });

        tabBlocking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Blocking clicked!", Toast.LENGTH_SHORT).show();
        });

        tabPanic.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Panic notifications clicked!", Toast.LENGTH_SHORT).show();
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
