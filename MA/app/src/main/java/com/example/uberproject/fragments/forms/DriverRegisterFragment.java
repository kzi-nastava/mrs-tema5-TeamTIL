package com.example.uberproject.fragments.forms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.DriverApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.request.DriverRegistrationRequestDTO;
import com.example.uberproject.dto.response.DriverRegistrationResponseDTO;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRegisterFragment extends Fragment {

    private EditText etName, etSurname, etEmail, etPhoneNumber, etAddress;
    private EditText etVehicleModel, etLicensePlate, etSeatCount;
    private Spinner spinnerVehicleType;
    private CheckBox cbBabyFriendly, cbPetFriendly;
    private ImageView ivProfilePhoto;
    private AppCompatButton btnInsertPhoto, btnRemovePhoto, btnSubmitDriver;

    private String base64ProfileImage = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            // Compress i konvertuj u Base64
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                            base64ProfileImage = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);

                            // Prikazi sliku
                            ivProfilePhoto.setImageBitmap(bitmap);
                            btnRemovePhoto.setEnabled(true);
                            btnRemovePhoto.setAlpha(1.0f);

                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_register, container, false);

        etName = view.findViewById(R.id.etName);
        etSurname = view.findViewById(R.id.etSurname);
        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etAddress = view.findViewById(R.id.etAddress);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        btnInsertPhoto = view.findViewById(R.id.btnInsertPhoto);
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto);

        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        etLicensePlate = view.findViewById(R.id.etLicensePlate);
        etSeatCount = view.findViewById(R.id.etSeatCount);
        spinnerVehicleType = view.findViewById(R.id.spinnerVehicleType);
        cbBabyFriendly = view.findViewById(R.id.cbBabyFriendly);
        cbPetFriendly = view.findViewById(R.id.cbPetFriendly);
        btnSubmitDriver = view.findViewById(R.id.btnSubmitDriver);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.vehicle_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);

        btnInsertPhoto.setOnClickListener(v -> openImagePicker());
        btnRemovePhoto.setOnClickListener(v -> removePhoto());
        btnSubmitDriver.setOnClickListener(v -> submitDriverRegistration());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void removePhoto() {
        base64ProfileImage = null;
        ivProfilePhoto.setImageResource(R.drawable.ic_person_placeholder);
        btnRemovePhoto.setEnabled(false);
        btnRemovePhoto.setAlpha(0.5f);
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(etSurname.getText().toString().trim())) {
            etSurname.setError("Surname is required");
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.setError("Email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            etEmail.setError("Invalid email format");
            return false;
        }
        if (TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())) {
            etPhoneNumber.setError("Phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            etAddress.setError("Address is required");
            return false;
        }
        if (TextUtils.isEmpty(etVehicleModel.getText().toString().trim())) {
            etVehicleModel.setError("Vehicle model is required");
            return false;
        }
        if (TextUtils.isEmpty(etLicensePlate.getText().toString().trim())) {
            etLicensePlate.setError("License plate is required");
            return false;
        }
        if (TextUtils.isEmpty(etSeatCount.getText().toString().trim())) {
            etSeatCount.setError("Seat count is required");
            return false;
        }
        return true;
    }

    private void submitDriverRegistration() {
        if (!validateInputs()) {
            return;
        }

        DriverRegistrationRequestDTO request = new DriverRegistrationRequestDTO();
        request.setFirstName(etName.getText().toString().trim());
        request.setLastName(etSurname.getText().toString().trim());
        request.setEmail(etEmail.getText().toString().trim());
        request.setPhoneNumber(etPhoneNumber.getText().toString().trim());
        request.setAddress(etAddress.getText().toString().trim());
        request.setProfilePictureUrl(base64ProfileImage);

        request.setVehicleModel(etVehicleModel.getText().toString().trim());
        request.setVehicleType(spinnerVehicleType.getSelectedItem().toString().toUpperCase());
        request.setLicensePlate(etLicensePlate.getText().toString().trim());
        request.setPassengerCapacity(Integer.parseInt(etSeatCount.getText().toString().trim()));
        request.setBabyFriendly(cbBabyFriendly.isChecked());
        request.setPetFriendly(cbPetFriendly.isChecked());

        DriverApi driverApi = RetrofitClient.getInstance(getContext()).create(DriverApi.class);

        driverApi.registerDriver(request).enqueue(new Callback<DriverRegistrationResponseDTO>() {
            @Override
            public void onResponse(Call<DriverRegistrationResponseDTO> call, Response<DriverRegistrationResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Driver registered successfully! Activation email sent.", Toast.LENGTH_LONG).show();
                    clearForm();
                    getView().scrollTo(0, 0);
                } else {
                    Toast.makeText(getContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DriverRegistrationResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etName.setText("");
        etSurname.setText("");
        etEmail.setText("");
        etPhoneNumber.setText("");
        etAddress.setText("");
        etVehicleModel.setText("");
        etLicensePlate.setText("");
        etSeatCount.setText("1");
        spinnerVehicleType.setSelection(0);
        cbBabyFriendly.setChecked(false);
        cbPetFriendly.setChecked(false);
        removePhoto();
    }
}