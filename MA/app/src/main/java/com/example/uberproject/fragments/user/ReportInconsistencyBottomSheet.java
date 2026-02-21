package com.example.uberproject.fragments.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.RideApi;
import com.example.uberproject.dto.request.InconsistencyReportRequestDTO;
import com.example.uberproject.dto.response.InconsistencyReportResponseDTO;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BottomSheet za prijavu nekonzistentnosti vožnje.
 * Identično Angular ReportDriver dijalogu.
 *
 * Koristiti:
 *  ReportInconsistencyBottomSheet.newInstance(rideId, passengerEmail)
 *      .show(getChildFragmentManager(), "report_inconsistency");
 */
public class ReportInconsistencyBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "ReportSheet";
    private static final String ARG_RIDE_ID = "rideId";
    private static final String ARG_EMAIL   = "passengerEmail";

    private int rideId;
    private String passengerEmail;

    private EditText etDescription;
    private Button btnUploadAttachment;
    private Button btnSubmit;
    private TextView tvAttachmentName;
    private ImageView ivAttachmentPreview;

    private String attachmentBase64 = null;

    // ActivityResultLauncher za odabir slike iz galerije
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static ReportInconsistencyBottomSheet newInstance(int rideId, String passengerEmail) {
        ReportInconsistencyBottomSheet sheet = new ReportInconsistencyBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_RIDE_ID, rideId);
        args.putString(ARG_EMAIL, passengerEmail);
        sheet.setArguments(args);
        return sheet;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            rideId = getArguments().getInt(ARG_RIDE_ID, -1);
            passengerEmail = getArguments().getString(ARG_EMAIL, "");
        }

        // Registruj image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelected(imageUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_inconsistency, container, false);

        etDescription       = view.findViewById(R.id.etReportDescription);
        btnUploadAttachment = view.findViewById(R.id.btnUploadAttachment);
        btnSubmit           = view.findViewById(R.id.btnSubmitReport);
        tvAttachmentName    = view.findViewById(R.id.tvAttachmentName);
        ivAttachmentPreview = view.findViewById(R.id.ivAttachmentPreview);

        btnUploadAttachment.setOnClickListener(v -> openImagePicker());
        btnSubmit.setOnClickListener(v -> submitReport());

        return view;
    }

    // ─── Image Picker ─────────────────────────────────────────────────────────

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelected(Uri imageUri) {
        try {
            // Prikaži preview
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), imageUri);
            ivAttachmentPreview.setImageBitmap(bitmap);
            ivAttachmentPreview.setVisibility(View.VISIBLE);

            // Konvertuj u Base64
            attachmentBase64 = uriToBase64(imageUri);

            // Prikaži naziv fajla
            tvAttachmentName.setText("Image attached ✓");
            tvAttachmentName.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Konvertuje URI slike u Base64 string.
     */
    private String uriToBase64(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    // ─── Submit ───────────────────────────────────────────────────────────────

    private void submitReport() {
        String description = etDescription.getText() != null
                ? etDescription.getText().toString().trim() : "";

        if (description.isEmpty()) {
            etDescription.setError("Please describe the issue");
            return;
        }

        if (rideId <= 0 || passengerEmail == null || passengerEmail.isEmpty()) {
            Toast.makeText(getContext(), "Invalid ride or user data", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        InconsistencyReportRequestDTO request = new InconsistencyReportRequestDTO(
                passengerEmail,
                description,
                attachmentBase64  // može biti null ako nema slike
        );

        RideApi api = RetrofitClient.getInstance(requireContext()).create(RideApi.class);
        api.reportInconsistency(rideId, request).enqueue(new Callback<InconsistencyReportResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<InconsistencyReportResponseDTO> call,
                                   @NonNull Response<InconsistencyReportResponseDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Log.e(TAG, "Report failed: " + response.code());
                    Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                    resetSubmitButton();
                }
            }

            @Override
            public void onFailure(@NonNull Call<InconsistencyReportResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Report network error", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetSubmitButton();
            }
        });
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit Report");
    }
}