package com.example.uberproject.fragments.forms;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;

import java.io.IOException;

import android.Manifest;

public class RegisterFragment extends Fragment {

    private EditText etName, etSurname, etEmail, etPassword, etRepeatPassword, etAddress, etPhoneNumber;
    private ImageView ivProfilePhoto;
    private Button btnInsertPhoto, btnRemovePhoto;
    private AppCompatButton btnSubmit;
    private Uri selectedImageUri;
    private boolean isPhotoSelected = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                        ivProfilePhoto.setImageBitmap(bitmap);
                        enableRemoveButton();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        initializeViews(view);
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        etName = view.findViewById(R.id.etName);
        etSurname = view.findViewById(R.id.etSurname);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etRepeatPassword = view.findViewById(R.id.etRepeatPassword);
        etAddress = view.findViewById(R.id.etAddress);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        btnInsertPhoto = view.findViewById(R.id.btnInsertPhoto);
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        // Inicijalno postaviti Remove dugme kao onemogućeno
        btnRemovePhoto.setEnabled(false);
        btnRemovePhoto.setAlpha(0.5f);
    }

    private void setupClickListeners() {
        btnInsertPhoto.setOnClickListener(v -> checkPermissionAndOpenPicker());
        btnRemovePhoto.setOnClickListener(v -> {
            if (isPhotoSelected) {
                removePhoto();
            }
        });
        btnSubmit.setOnClickListener(v -> handleRegistration());
    }

    private void checkPermissionAndOpenPicker() {
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void enableRemoveButton() {
        btnRemovePhoto.setEnabled(true);
        btnRemovePhoto.setAlpha(1.0f);
        isPhotoSelected = true;
    }

    private void removePhoto() {
        ivProfilePhoto.setImageResource(R.drawable.ic_person_placeholder);
        selectedImageUri = null;
        btnRemovePhoto.setEnabled(false);
        btnRemovePhoto.setAlpha(0.5f);
        isPhotoSelected = false;
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etRepeatPassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        // Clear previous errors
        clearErrors();

        boolean isValid = true;

        // Name validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            isValid = false;
        }

        // Surname validation
        if (TextUtils.isEmpty(surname)) {
            etSurname.setError("Surname is required");
            isValid = false;
        } else if (surname.length() < 2) {
            etSurname.setError("Surname must be at least 2 characters");
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Repeat password validation
        if (TextUtils.isEmpty(repeatPassword)) {
            etRepeatPassword.setError("Please repeat password");
            isValid = false;
        } else if (!password.equals(repeatPassword)) {
            etRepeatPassword.setError("Passwords do not match");
            isValid = false;
        }

        // Address validation
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            isValid = false;
        }

        // Phone number validation
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            isValid = false;
        } else if (!Patterns.PHONE.matcher(phoneNumber).matches() || phoneNumber.length() < 8) {
            etPhoneNumber.setError("Please enter a valid phone number");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        etName.setError(null);
        etSurname.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);
        etAddress.setError(null);
        etPhoneNumber.setError(null);
    }

    private void handleRegistration() {
        if (validateInputs()) {
            // TODO: Implement actual registration logic
            Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_LONG).show();

            // Navigate back or to main activity
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}