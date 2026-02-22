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

public class ReportInconsistencyBottomSheet extends BottomSheetDialogFragment {

    // callback

    public interface OnReportSubmittedListener {
        void onReportSubmitted();
    }

    private OnReportSubmittedListener reportListener;

    public void setOnReportSubmittedListener(OnReportSubmittedListener l) {
        this.reportListener = l;
    }

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

        btnSubmit.setOnClickListener(v -> {
            String description = etDescription.getText() != null
                    ? etDescription.getText().toString().trim() : "";

            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Please describe the issue", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReport(description, btnSubmit);
        });

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

    private void submitReport(String description, Button btnSubmit) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        InconsistencyReportRequestDTO request =
                new InconsistencyReportRequestDTO(passengerEmail, description, null);

        RetrofitClient.getInstance(requireContext()).create(RideApi.class)
                .reportInconsistency(rideId, request)
                .enqueue(new Callback<InconsistencyReportResponseDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<InconsistencyReportResponseDTO> call,
                                           @NonNull Response<InconsistencyReportResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Report submitted!", Toast.LENGTH_SHORT).show();
                            // Fire callback BEFORE dismiss so parent can refresh
                            if (reportListener != null) reportListener.onReportSubmitted();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                            resetButton(btnSubmit);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<InconsistencyReportResponseDTO> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        resetButton(btnSubmit);
                    }
                });
    }

    private void resetButton(Button btn) {
        btn.setEnabled(true);
        btn.setText("Submit Report");
    }
}