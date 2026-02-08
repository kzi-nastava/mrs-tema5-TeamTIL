package com.example.uberproject.fragments.forms;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.UserApi;
import com.example.uberproject.dto.response.UserResponseDTO;
import com.example.uberproject.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInfoFragment extends Fragment {

    private EditText name, surname, email, address, phone;
    private Button btnSave;
    private UserApi userApi;
    private UserResponseDTO currentUser;
    private String newBase64Photo = null;

    public void setNewProfilePicture(String base64) {
        this.newBase64Photo = base64;
    }

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

        userApi = RetrofitClient.getInstance(getContext()).create(UserApi.class);
        loadUserData();

        setupTextWatcher();

        btnSave.setOnClickListener(v -> updateUserData());

        return view;
    }

    private void setupTextWatcher() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Fragment parent = getParentFragment();
                if (parent instanceof ProfileFragment) {
                    ((ProfileFragment) parent).updateHeaderUI(name.getText().toString(), surname.getText().toString(), email.getText().toString(), null);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        name.addTextChangedListener(watcher);
        surname.addTextChangedListener(watcher);
    }

    private void loadUserData() {
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

                    Fragment parent = getParentFragment();
                    if (parent instanceof ProfileFragment) {
                        ((ProfileFragment) parent).updateHeaderUI(currentUser.getFirstName(), currentUser.getLastName(), currentUser.getEmail(), currentUser.getProfilePictureUrl());
                    }
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {}
        });
    }

    private void updateUserData() {
        if (currentUser == null) return;

        currentUser.setFirstName(name.getText().toString());
        currentUser.setLastName(surname.getText().toString());
        currentUser.setAddress(address.getText().toString());
        currentUser.setPhoneNumber(phone.getText().toString());

        if (newBase64Photo != null) {
            currentUser.setProfilePictureUrl(newBase64Photo);
        }

        userApi.updateMyProfile(currentUser).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();

                    TokenManager.getInstance(getContext()).saveProfilePictureUrl(currentUser.getProfilePictureUrl());

                    if (getActivity() != null) {
                        getActivity().invalidateOptionsMenu();
                    }

                    Fragment parent = getParentFragment();
                    if (parent instanceof ProfileFragment) {
                        ((ProfileFragment) parent).updateHeaderUI(
                                currentUser.getFirstName(),
                                currentUser.getLastName(),
                                currentUser.getEmail(),
                                currentUser.getProfilePictureUrl()
                        );
                        ((ProfileFragment) parent).showEditButton();
                    }

                    newBase64Photo = null;
                    toggleEditing(false);
                    hideKeyboard();
                    Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void toggleEditing(boolean enable) {
        name.setEnabled(enable);
        surname.setEnabled(enable);
        address.setEnabled(enable);
        phone.setEnabled(enable);
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        if (enable) {
            btnSave.setFocusableInTouchMode(false);
            btnSave.bringToFront();
        }

        Fragment parent = getParentFragment();
        if (parent instanceof ProfileFragment) {
            ((ProfileFragment) parent).setChangePhotoVisible(enable);
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}