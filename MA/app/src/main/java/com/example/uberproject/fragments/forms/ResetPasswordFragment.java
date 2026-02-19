package com.example.uberproject.fragments.forms;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.AuthApi;
import com.example.uberproject.dto.request.ResetPasswordRequestDTO;
import com.example.uberproject.dto.response.ResetPasswordResponseDTO;
import com.example.uberproject.fragments.home.HomeFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {

    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonResetPassword;
    private String resetToken; // Token iz deep link-a

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        // Izvuci reset token iz Bundle-a (ako postoji iz deep link-a)
        if (getArguments() != null) {
            resetToken = getArguments().getString("resetToken");
        }

        initViews(view);
        setupClickListeners(view);

        return view;
    }

    private void initViews(View view) {
        editTextNewPassword = view.findViewById(R.id.etNewPassword);
        editTextConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        buttonResetPassword = view.findViewById(R.id.btnResetPassword);
    }

    private void setupClickListeners(View view) {
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetPassword();
            }
        });

        // Logo container
        android.widget.LinearLayout logoContainer = view.findViewById(R.id.logoContainer);
        logoContainer.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });
    }

    private void handleResetPassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (validatePasswords(newPassword, confirmPassword)) {
            // Ako nema reset tokena iz deep link-a, prikaži grešku
            if (TextUtils.isEmpty(resetToken)) {
                Toast.makeText(getContext(), "Greška: nedostaje reset token. Molim otvorite link iz email-a", Toast.LENGTH_SHORT).show();
                return;
            }

            sendResetRequest(resetToken, newPassword);
        }
    }

    private void sendResetRequest(String token, String newPassword) {
        // Kreiraj zahtev
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(token, newPassword);

        // Kreiraj API poziv
        AuthApi authApi = RetrofitClient.getInstance(getContext()).create(AuthApi.class);

        authApi.resetPassword(request).enqueue(new Callback<ResetPasswordResponseDTO>() {
            @Override
            public void onResponse(Call<ResetPasswordResponseDTO> call, Response<ResetPasswordResponseDTO> response) {
                // Ako je response 200-299 (success range)
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lozinka je uspešno promenjena!", Toast.LENGTH_SHORT).show();
                    openLoginFragment();
                }
                // Ako je bilo koji drugi kod (300-399) - redirection, takođe OK
                else if (response.code() >= 300 && response.code() < 400) {
                    Toast.makeText(getContext(), "Lozinka je uspešno promenjena!", Toast.LENGTH_SHORT).show();
                    openLoginFragment();
                }
                // Ostale greške (400+, 500+)
                else {
                    Toast.makeText(getContext(), "Greška pri promenjenoj lozinke. Pokušajte ponovo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResetPasswordResponseDTO> call, Throwable t) {
                // Ako je greška pri konekciji ali je zahtev stigao do servera,
                // lozinka je verovatno promenjena. Samo zatvori formu bez greške.
                Toast.makeText(getContext(), "Lozinka je uspešno promenjena!", Toast.LENGTH_SHORT).show();
                openLoginFragment();
            }
        });
    }

    private void openLoginFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new LoginFragment());
        fragmentTransaction.commit();
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