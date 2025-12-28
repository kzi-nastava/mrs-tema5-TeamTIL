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

public class ProfileInfoFragment extends Fragment {

    private EditText name, surname, email, address, phone;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_profile_info, container, false);

        name = view.findViewById(R.id.valueName);
        surname = view.findViewById(R.id.valueSurname);
        email = view.findViewById(R.id.valueEmail);
        address = view.findViewById(R.id.valueAddress);
        phone = view.findViewById(R.id.valuePhone);
        btnSave = view.findViewById(R.id.btnSaveChanges);

        btnSave.setOnClickListener(v -> {
            toggleEditing(false);
            Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();

            if (getParentFragment() instanceof ProfileFragment) {
                ((ProfileFragment) getParentFragment()).showEditButton();
            }

            else if (getParentFragment() instanceof AdminProfileFragment) {
                ((AdminProfileFragment) getParentFragment()).showEditButton();
            }

            else if (getParentFragment() instanceof DriverProfileFragment) {
                ((DriverProfileFragment) getParentFragment()).showEditButton();
            }
        });

        return view;
    }

    public void toggleEditing(boolean enable) {
        name.setEnabled(enable);
        surname.setEnabled(enable);
        email.setEnabled(enable);
        address.setEnabled(enable);
        phone.setEnabled(enable);

        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
    }
}