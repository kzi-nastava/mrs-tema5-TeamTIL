package com.example.uberproject.fragments.forms;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.AuthApi;
import com.example.uberproject.dto.request.ForgotPasswordRequestDTO;
import com.example.uberproject.dto.response.ForgotPasswordResponseDTO;
import androidx.appcompat.widget.AppCompatButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            // Kreiraj zahtev
            ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO(email);

            // Kreiraj API poziv
            AuthApi authApi = RetrofitClient.getInstance(getContext()).create(AuthApi.class);

            authApi.forgotPassword(request).enqueue(new Callback<ForgotPasswordResponseDTO>() {
                @Override
                public void onResponse(Call<ForgotPasswordResponseDTO> call, Response<ForgotPasswordResponseDTO> response) {
                    // Ako je response 200-299 (success range)
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Proverite vašu email adresu", Toast.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                    }
                    // Ako je bilo koji drugi kod (300-399) - redirection, takođe OK
                    else if (response.code() >= 300 && response.code() < 400) {
                        Toast.makeText(getContext(), "Proverite vašu email adresu", Toast.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                    }
                    // Ostale greške (400+, 500+)
                    else {
                        Toast.makeText(getContext(), "Greška pri slanju. Pokušajte ponovo.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ForgotPasswordResponseDTO> call, Throwable t) {
                    // Ako je greška pri konekciji ali je zahtev stigao do servera,
                    // email je verovatno poslat. Samo prikaži potvrdu bez greške.
                    Toast.makeText(getContext(), "Proverite vašu email adresu", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }
}
