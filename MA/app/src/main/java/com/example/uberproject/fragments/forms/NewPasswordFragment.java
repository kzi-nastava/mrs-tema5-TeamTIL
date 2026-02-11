package com.example.uberproject.fragments.forms;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.DriverApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.request.ActivateDriverRequestDTO;
import com.example.uberproject.dto.response.ActivateDriverResponseDTO;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPasswordFragment extends Fragment {

    private EditText etNewPassword, etConfirmPassword;
    private AppCompatButton btnActivate;
    private String activationToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_password, container, false);

        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnActivate = view.findViewById(R.id.btnActivate);

        // Uzmi token iz bundle-a
        if (getArguments() != null) {
            activationToken = getArguments().getString("token");
        }

        btnActivate.setOnClickListener(v -> handleActivation());

        return view;
    }

    private boolean validatePasswords() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Password is required");
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm password");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void handleActivation() {
        if (!validatePasswords()) {
            return;
        }

        if (activationToken == null || activationToken.isEmpty()) {
            Toast.makeText(getContext(), "Invalid activation link", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassword = etNewPassword.getText().toString().trim();

        ActivateDriverRequestDTO request = new ActivateDriverRequestDTO();
        request.setToken(activationToken);
        request.setNewPassword(newPassword);

        DriverApi driverApi = RetrofitClient.getInstance(getContext()).create(DriverApi.class);

        driverApi.activateDriver(request).enqueue(new Callback<ActivateDriverResponseDTO>() {
            @Override
            public void onResponse(Call<ActivateDriverResponseDTO> call, Response<ActivateDriverResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Account activated successfully! You can now log in.", Toast.LENGTH_LONG).show();
                    // Vrati se na login
                    getParentFragmentManager().popBackStack();
                } else {

                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("BACKEND_ERROR", errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Activation failed. Token may be invalid or expired.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ActivateDriverResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
