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

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        EditText name = view.findViewById(R.id.valueName);
        EditText surname = view.findViewById(R.id.valueSurname);
        EditText email = view.findViewById(R.id.valueEmail);
        EditText address = view.findViewById(R.id.valueAddress);
        EditText phone = view.findViewById(R.id.valuePhone);
        Button btnEdit = view.findViewById(R.id.btnEditProfile);
        Button btnSave = view.findViewById(R.id.btnSaveChanges);

        btnEdit.setOnClickListener(v -> {
            name.setEnabled(true);
            surname.setEnabled(true);
            email.setEnabled(true);
            address.setEnabled(true);
            phone.setEnabled(true);
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        });

        btnSave.setOnClickListener(v -> {
            name.setEnabled(false);
            surname.setEnabled(false);
            email.setEnabled(false);
            address.setEnabled(false);
            phone.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
