package com.example.uberproject.forms;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.uberproject.R;

public class ResetPasswordFragment extends Fragment {

    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonResetPassword;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        editTextNewPassword = view.findViewById(R.id.etNewPassword);
        editTextConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        buttonResetPassword = view.findViewById(R.id.btnResetPassword);
    }

    private void setupClickListeners() {
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetPassword();
            }
        });
    }

    private void handleResetPassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (validatePasswords(newPassword, confirmPassword)) {
            // TODO: Implement password reset logic
            Toast.makeText(getContext(), "Password reset successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("New password is required");
            return false;
        }

        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Please confirm your password");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }
}