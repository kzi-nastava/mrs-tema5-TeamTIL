package com.example.uberproject.fragments.forms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.uberproject.R;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.UserApi;
import com.example.uberproject.dto.response.DriverResponseDTO;
import com.example.uberproject.dto.response.UserResponseDTO;
import com.example.uberproject.utils.TokenManager;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ProfileInfoFragment infoFragment;
    private Button btnEdit;
    private String userRole;
    private View layoutChangePhoto;
    private ImageView imgProfile;

    private static final int PICK_IMAGE_REQUEST = 1;

    private boolean isDriverActive = true;
    private DriverResponseDTO currentDriverData;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private UserApi userApi;
    private TextView tvHoursActive;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        userRole = TokenManager.getInstance(getContext()).getUserRole();
        View view;

        if ("ADMINISTRATOR".equalsIgnoreCase(userRole)) {
            view = inflater.inflate(R.layout.fragment_admin_profile, container, false);
            setupAdminLogic(view);
        } else if ("DRIVER".equalsIgnoreCase(userRole)) {
            view = inflater.inflate(R.layout.fragment_driver_profile, container, false);
            setupDriverLogic(view);
        } else {
            view = inflater.inflate(R.layout.fragment_profile, container, false);
            setupPassengerLogic(view);
        }

        btnEdit = view.findViewById(R.id.btnEditProfile);
        imgProfile = view.findViewById(R.id.imgProfile);
        layoutChangePhoto = view.findViewById(R.id.layoutChangePhoto);

        loadInfoFragment();

        if (layoutChangePhoto != null) {
            layoutChangePhoto.setOnClickListener(v -> openGallery());
            setChangePhotoVisible(false); // default sakriveno
        }

        btnEdit.setOnClickListener(v -> {
            if (infoFragment != null) {
                infoFragment.toggleEditing(true);
                btnEdit.setVisibility(View.GONE);
                setChangePhotoVisible(true);
            }
        });

        return view;
    }

    public void setChangePhotoVisible(boolean visible) {
        if (layoutChangePhoto != null) {
            layoutChangePhoto.setVisibility(visible ? View.VISIBLE : View.GONE);
            layoutChangePhoto.setClickable(visible);
            if (visible) {
                layoutChangePhoto.bringToFront();
            }
        }
    }

    public void updateHeaderUI(String name, String surname, String email, String photoUrl) {
        if (getView() == null) return;

        TextView headerName = getView().findViewById(R.id.headerName);
        TextView headerEmail = getView().findViewById(R.id.headerEmail);

        if (headerName != null) headerName.setText(name);
        if (headerEmail != null) headerEmail.setText(email);

        if (photoUrl != null && !photoUrl.isEmpty() && imgProfile != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(imgProfile);

            imgProfile.setPadding(0, 0, 0, 0);
            imgProfile.setImageTintList(null);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri imageUri = data.getData();

            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().getContentResolver(),
                        imageUri
                );

                int maxSize = 400;
                float ratio = Math.min(
                        (float) maxSize / originalBitmap.getWidth(),
                        (float) maxSize / originalBitmap.getHeight()
                );

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        Math.round(ratio * originalBitmap.getWidth()),
                        Math.round(ratio * originalBitmap.getHeight()),
                        true
                );

                imgProfile.setImageBitmap(scaledBitmap);
                imgProfile.setPadding(0, 0, 0, 0);
                imgProfile.setImageTintList(null);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

                if (infoFragment != null) {
                    infoFragment.setNewProfilePicture(base64);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupDriverLogic(View view) {
        Button btnStatus = view.findViewById(R.id.btnStatusActive);
        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabVehicle = view.findViewById(R.id.tabVehicle);
        tvHoursActive = view.findViewById(R.id.tvHoursActive);

        userApi = RetrofitClient.getInstance(getContext()).create(UserApi.class);

        userApi.getDriverProfile().enqueue(new Callback<DriverResponseDTO>() {
            @Override
            public void onResponse(Call<DriverResponseDTO> call, Response<DriverResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentDriverData = response.body();
                    updateHeaderUI(
                            currentDriverData.getFirstName(),
                            currentDriverData.getLastName(),
                            currentDriverData.getEmail(),
                            currentDriverData.getProfilePictureUrl()
                    );
                    isDriverActive = currentDriverData.getIsActive();
                    updateStatusButtonUI(btnStatus);

                    TextView tvBlockedBanner = view.findViewById(R.id.tvBlockedBanner);
                    if (tvBlockedBanner != null) {
                        if (Boolean.TRUE.equals(currentDriverData.getIsBlocked())) {
                            String reason = currentDriverData.getBlockReason();
                            tvBlockedBanner.setText("Account Blocked:  " + (reason != null ? reason : ""));
                            tvBlockedBanner.setVisibility(View.VISIBLE);
                        } else {
                            tvBlockedBanner.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DriverResponseDTO> call, Throwable t) {}
        });

        // Učitaj aktivne sate
        loadActiveHours(userApi, tvHoursActive);

        // Pokreni Handler za osvježavanje aktivnih sati svakog minuta
        startRefreshActiveHoursTimer();

        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            updateTabStyles(tabInfo, tabVehicle);
            btnEdit.setVisibility(View.VISIBLE);
        });

        tabVehicle.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.profileContentContainer, new VehicleDetailsFragment())
                    .commit();

            updateTabStyles(tabVehicle, tabInfo);
            btnEdit.setVisibility(View.GONE);
            setChangePhotoVisible(false);
        });

        btnStatus.setOnClickListener(v -> toggleDriverStatus(userApi, btnStatus));
    }

    private void loadActiveHours(UserApi api, TextView tvHoursActive) {
        api.getActiveHours().enqueue(new Callback<com.example.uberproject.dto.response.ActiveHoursResponseDTO>() {
            @Override
            public void onResponse(Call<com.example.uberproject.dto.response.ActiveHoursResponseDTO> call, Response<com.example.uberproject.dto.response.ActiveHoursResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Double hours = response.body().getActiveHoursLast24h();
                    if (hours != null) {
                        android.util.Log.d("ActiveHours", "Success: " + hours);
                        String displayText = formatActiveHours(hours);
                        tvHoursActive.setText("Hours Active (last 24h): " + displayText);
                    }
                } else {
                    android.util.Log.e("ActiveHours", "Error: " + response.code() + " - " + response.message());
                    tvHoursActive.setText("Hours Active (last 24h): Unable to load");
                }
            }

            @Override
            public void onFailure(Call<com.example.uberproject.dto.response.ActiveHoursResponseDTO> call, Throwable t) {
                android.util.Log.e("ActiveHours", "Request failed", t);
                tvHoursActive.setText("Hours Active (last 24h): Unable to load");
            }
        });
    }

    private String formatActiveHours(Double totalHours) {
        if (totalHours == null || totalHours <= 0) {
            return "0 min";
        }

        int hours = (int) Math.floor(totalHours);
        int minutes = (int) Math.round((totalHours - hours) * 60);

        // Ako je 60 minuta, konvertuj u sat
        if (minutes >= 60) {
            hours++;
            minutes = 0;
        }

        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "min";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "min";
        }
    }

    private void toggleDriverStatus(UserApi api, Button btnStatus) {
        if (currentDriverData == null) return;

        boolean newStatus = !isDriverActive;
        currentDriverData.setIsActive(newStatus);

        api.updateDriverProfile(currentDriverData).enqueue(new Callback<DriverResponseDTO>() {
            @Override
            public void onResponse(Call<DriverResponseDTO> call, Response<DriverResponseDTO> response) {
                if (response.isSuccessful()) {
                    isDriverActive = newStatus;
                    updateStatusButtonUI(btnStatus);
                }
            }

            @Override
            public void onFailure(Call<DriverResponseDTO> call, Throwable t) {}
        });
    }

    private void updateStatusButtonUI(Button btnStatus) {
        btnStatus.setText(
                isDriverActive ? getString(R.string.active_title) : getString(R.string.inactive_title)
        );
        btnStatus.setBackgroundResource(
                isDriverActive ? R.drawable.bg_button_green : R.drawable.bg_button_red
        );
    }

    private void setupAdminLogic(View view) {
        TextView tabInfo = view.findViewById(R.id.tabInfo);
        TextView tabBlocking = view.findViewById(R.id.tabBlocking);
        TextView tabPanic = view.findViewById(R.id.tabPanic);

        tabInfo.setOnClickListener(v -> {
            loadInfoFragment();
            updateTabStyles(tabInfo, tabBlocking, tabPanic);
            btnEdit.setVisibility(View.VISIBLE);
        });

        tabBlocking.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.profileContentContainer, new BlockingFragment())
                    .commit();
            updateTabStyles(tabBlocking, tabInfo, tabPanic);
            btnEdit.setVisibility(View.GONE);
            setChangePhotoVisible(false);
        });

        tabPanic.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Panic notifications", Toast.LENGTH_SHORT).show();
            updateTabStyles(tabPanic, tabInfo, tabBlocking);
            btnEdit.setVisibility(View.GONE);
            setChangePhotoVisible(false);
        });

        updateTabStyles(tabInfo, tabBlocking, tabPanic);
    }

    private void setupPassengerLogic(View view) {
        UserApi api = RetrofitClient.getInstance(getContext()).create(UserApi.class);

        api.getMyProfile().enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();
                    updateHeaderUI(
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getProfilePictureUrl()
                    );
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {}
        });
    }

    private void loadInfoFragment() {
        infoFragment = new ProfileInfoFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.profileContentContainer, infoFragment)
                .commit();
    }

    private void startRefreshActiveHoursTimer() {
        // ...existing code...
        if (refreshHandler == null) {
            refreshHandler = new Handler(Looper.getMainLooper());
        }

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Osvježi aktivne sate
                if (userApi != null && tvHoursActive != null) {
                    loadActiveHours(userApi, tvHoursActive);
                }
                // Ponovi nakon 60 sekundi (1 minut)
                refreshHandler.postDelayed(this, 60000);
            }
        };

        // Pokreni prvi refresh nakon 60 sekundi
        refreshHandler.postDelayed(refreshRunnable, 60000);
    }

    private void stopRefreshActiveHoursTimer() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Zaustavi timer kada se fragment uništi
        stopRefreshActiveHoursTimer();
    }

    private void updateTabStyles(TextView selectedTab, TextView... otherTabs) {
        selectedTab.setTypeface(null, Typeface.BOLD);
        for (TextView tab : otherTabs) {
            if (tab != null) tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void showEditButton() {
        if (btnEdit != null) btnEdit.setVisibility(View.VISIBLE);
    }
}