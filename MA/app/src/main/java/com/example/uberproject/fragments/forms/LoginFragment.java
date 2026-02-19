package com.example.uberproject.fragments.forms;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.uberproject.R;
import com.example.uberproject.activities.MainActivity;
import com.example.uberproject.api.AuthApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.request.LoginRequestDTO;
import com.example.uberproject.dto.response.LoginResponseDTO;
import com.example.uberproject.fragments.home.HomeFragment;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);

        LinearLayout logoContainer = view.findViewById(R.id.logoContainer);

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            handleLogin();
        });

        tvForgotPassword.setOnClickListener(v -> {
            ForgotPasswordFragment fragment = new ForgotPasswordFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Logo click - navigate to home/main screen
        logoContainer.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

        return view;
    }

    private boolean validateLoginInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateLoginInputs(email, password)) {
            LoginRequestDTO request = new LoginRequestDTO(email, password);

            AuthApi authApi = RetrofitClient.getInstance(requireContext()).create(AuthApi.class);

            authApi.login(request).enqueue(new Callback<LoginResponseDTO>() {
                @Override
                public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String token = response.body().getToken();
                        String role = response.body().getUserType();
                        String userEmail = response.body().getEmail();
                        String profilePictureUrl = response.body().getProfilePictureUrl();
                        long expiresIn = response.body().getExpiresIn();

                        // Koristi saveTokenWithExpiration ako je expiresIn definisan
                        if (expiresIn > 0) {
                            saveTokenWithExpiration(token, role, userEmail, profilePictureUrl, expiresIn);
                        } else {
                            saveToken(token, role, userEmail, profilePictureUrl);
                        }

                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                        navigateToProfileByRole();
                    } else {
                        Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                    Toast.makeText(getContext(), "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveToken(String token, String role, String email, String profilePictureUrl) {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        tokenManager.saveToken(token, role, email, profilePictureUrl);
    }

    private void saveTokenWithExpiration(String token, String role, String email, String profilePictureUrl, long expiresInSeconds) {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        // Preračunaj trenutno vrijeme + expiresIn u milisekundama
        long expirationTimeMillis = System.currentTimeMillis() + (expiresInSeconds * 1000);
        tokenManager.saveTokenWithExpiration(token, role, email, profilePictureUrl, expirationTimeMillis);
    }

    private void navigateToProfileByRole() {
        Fragment targetFragment = new ProfileFragment();

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.showToolbar();
            mainActivity.invalidateOptionsMenu();
            mainActivity.refreshNavigationMenu();
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .commit();
    }
}
