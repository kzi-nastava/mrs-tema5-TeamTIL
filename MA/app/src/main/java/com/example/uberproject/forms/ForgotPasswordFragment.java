package com.example.uberproject.forms;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uberproject.R;
import androidx.appcompat.widget.AppCompatButton;

public class ForgotPasswordFragment extends Fragment {

    private EditText editTextEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        editTextEmail = view.findViewById(R.id.etEmail);
        AppCompatButton btnCancel = view.findViewById(R.id.btnCancel);
        AppCompatButton btnResetPassword = view.findViewById(R.id.btnResetPassword);

        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        btnResetPassword.setOnClickListener(v -> {
            handleForgotPassword();
        });

        return view;
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private void handleForgotPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (validateEmail(email)) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ResetPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}
