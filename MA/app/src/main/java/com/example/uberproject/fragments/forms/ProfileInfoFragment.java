package com.example.uberproject.fragments.forms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.UserApi;
import com.example.uberproject.dto.response.UserResponseDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInfoFragment extends Fragment {

    private EditText name, surname, email, address, phone;
    private Button btnSave;
    private UserApi userApi;
    private UserResponseDTO currentUser;

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

        android.text.TextWatcher headerUpdater = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Fragment parent = getParentFragment();
                if (parent != null && parent.getView() != null) {
                    TextView headerName = parent.getView().findViewById(R.id.headerName);
                    if (headerName != null) {
                        String fullName = name.getText().toString() + " " + surname.getText().toString();
                        headerName.setText(fullName);
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        name.addTextChangedListener(headerUpdater);
        surname.addTextChangedListener(headerUpdater);

        userApi = RetrofitClient.getInstance(getContext()).create(UserApi.class);
        loadUserDataFromServer();

        btnSave.setOnClickListener(v -> {
            updateUserDataOnServer();
        });

        return view;
    }

    private void loadUserDataFromServer() {
        userApi.getMyProfile().enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {

                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();

                    name.setText(currentUser.getFirstName());
                    surname.setText(currentUser.getLastName());
                    email.setText(currentUser.getEmail());
                    address.setText(currentUser.getAddress());
                    phone.setText(currentUser.getPhoneNumber());

                    // odmah saljemo podatke u ProfileFragment zaglavlje
                    Fragment parent = getParentFragment();
                    if (parent != null && parent.getView() != null) {
                        TextView headerName = parent.getView().findViewById(R.id.headerName);
                        TextView headerEmail = parent.getView().findViewById(R.id.headerEmail);

                        if (headerName != null) {
                            headerName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                        }
                        if (headerEmail != null) {
                            headerEmail.setText(currentUser.getEmail());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserDataOnServer() {
        if (currentUser == null) return;

        // uzmi šta je korisnik upisao u polja
        currentUser.setFirstName(name.getText().toString());
        currentUser.setLastName(surname.getText().toString());
        currentUser.setAddress(address.getText().toString());
        currentUser.setPhoneNumber(phone.getText().toString());

        // posalji na backend - put
        userApi.updateMyProfile(currentUser).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    toggleEditing(false);
                    Toast.makeText(getContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show();

                    // dinamicka izmena
                    Fragment parent = getParentFragment();
                    if (parent != null && parent.getView() != null) {
                        TextView headerName = parent.getView().findViewById(R.id.headerName);
                        TextView headerEmail = parent.getView().findViewById(R.id.headerEmail);

                        if (headerName != null) {
                            headerName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                        }
                        if (headerEmail != null) {
                            headerEmail.setText(currentUser.getEmail());
                        }

                        // ponovo vidljivo edit dugme
                        if (parent instanceof ProfileFragment) {
                            ((ProfileFragment) parent).showEditButton();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to save changes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Network issue occurred while saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void toggleEditing(boolean enable) {
        name.setEnabled(enable);
        surname.setEnabled(enable);
        email.setEnabled(false);
        address.setEnabled(enable);
        phone.setEnabled(enable);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
    }
}