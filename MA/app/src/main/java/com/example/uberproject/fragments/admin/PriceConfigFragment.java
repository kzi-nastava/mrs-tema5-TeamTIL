package com.example.uberproject.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.uberproject.R;
import com.example.uberproject.api.PriceConfigApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.PriceConfigDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceConfigFragment extends Fragment {

    private static final String TAG = "PriceConfigFragment";

    private static final String[] VEHICLE_TYPES = {"STANDARD", "LUXURY", "VAN"};
    private static final String[] TAB_LABELS    = {"Standard", "Luxury", "Van"};

    // Tabs
    private AppCompatButton btnTabStandard, btnTabLuxury, btnTabVan;

    // Form fields
    private EditText etBasePrice, etPricePerKm;
    private TextView tvFormulaExample;
    private Button btnSave;

    // States
    private ProgressBar progressBar;
    private LinearLayout layoutContent;
    private TextView tvSuccessMessage, tvErrorMessage;

    // State
    private String activeTab = "STANDARD";
    private final PriceConfigDTO[] configs = new PriceConfigDTO[3]; // 0=STANDARD, 1=LUXURY, 2=VAN
    private PriceConfigApi api;
    private int loadedCount = 0;

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_price_config, container, false);
        bindViews(view);
        api = RetrofitClient.getInstance(requireContext()).create(PriceConfigApi.class);

        setupTabListeners();
        setupInputListeners();
        setupSaveButton();

        loadAllConfigs();

        return view;
    }

    // ─── Bind views ───────────────────────────────────────────────────────────

    private void bindViews(View view) {
        btnTabStandard   = view.findViewById(R.id.btnTabStandard);
        btnTabLuxury     = view.findViewById(R.id.btnTabLuxury);
        btnTabVan        = view.findViewById(R.id.btnTabVan);
        etBasePrice      = view.findViewById(R.id.etBasePrice);
        etPricePerKm     = view.findViewById(R.id.etPricePerKm);
        tvFormulaExample = view.findViewById(R.id.tvFormulaExample);
        btnSave          = view.findViewById(R.id.btnSaveConfig);
        progressBar      = view.findViewById(R.id.progressBarPriceConfig);
        layoutContent    = view.findViewById(R.id.layoutPriceConfigContent);
        tvSuccessMessage = view.findViewById(R.id.tvSuccessMessage);
        tvErrorMessage   = view.findViewById(R.id.tvErrorMessage);
    }

    // ─── Tab setup ────────────────────────────────────────────────────────────

    private void setupTabListeners() {
        btnTabStandard.setOnClickListener(v -> switchTab("STANDARD"));
        btnTabLuxury.setOnClickListener(v -> switchTab("LUXURY"));
        btnTabVan.setOnClickListener(v -> switchTab("VAN"));
    }

    private void switchTab(String type) {
        activeTab = type;
        updateTabStyles();
        populateForm();
        clearMessages();
    }

    private void updateTabStyles() {
        // Reset sve na transparentno
        ViewCompat.setBackgroundTintList(btnTabStandard, null);
        ViewCompat.setBackgroundTintList(btnTabLuxury, null);
        ViewCompat.setBackgroundTintList(btnTabVan, null);
        btnTabStandard.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnTabLuxury.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnTabVan.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnTabStandard.setTextColor(getResources().getColor(android.R.color.white, null));
        btnTabLuxury.setTextColor(getResources().getColor(android.R.color.white, null));
        btnTabVan.setTextColor(getResources().getColor(android.R.color.white, null));

        // Tek onda postavi aktivni
        androidx.appcompat.widget.AppCompatButton active = getActiveTabButton();
        if (active != null) {
            ViewCompat.setBackgroundTintList(active, null);
            active.setBackgroundResource(R.drawable.button_yellow_rounded);
            active.setTextColor(getResources().getColor(android.R.color.black, null));
        }
    }

    private AppCompatButton getActiveTabButton() {
        switch (activeTab) {
            case "STANDARD": return btnTabStandard;
            case "LUXURY":   return btnTabLuxury;
            case "VAN":      return btnTabVan;
            default:         return null;
        }
    }

    // ─── Form population ──────────────────────────────────────────────────────

    private void populateForm() {
        PriceConfigDTO config = getCurrentConfig();
        if (config == null) return;

        etBasePrice.setText(String.valueOf((int) config.getBasePrice()));
        etPricePerKm.setText(String.valueOf((int) config.getPricePerKm()));
        updateFormulaExample(config.getBasePrice(), config.getPricePerKm());
    }

    private void setupInputListeners() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFormulaFromInputs();
            }
        };
        etBasePrice.addTextChangedListener(watcher);
        etPricePerKm.addTextChangedListener(watcher);
    }

    private void updateFormulaFromInputs() {
        try {
            double base = parseDouble(etBasePrice.getText().toString());
            double perKm = parseDouble(etPricePerKm.getText().toString());
            updateFormulaExample(base, perKm);
        } catch (Exception ignored) {}
    }

    private void updateFormulaExample(double base, double perKm) {
        if (tvFormulaExample == null) return;
        int example = (int) (base + 10 * perKm);
        tvFormulaExample.setText("e.g. 10 km → " + example + " RSD");
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadAllConfigs() {
        showLoading();
        loadedCount = 0;

        for (int i = 0; i < VEHICLE_TYPES.length; i++) {
            final int idx = i;
            api.getPriceConfig(VEHICLE_TYPES[i]).enqueue(new Callback<PriceConfigDTO>() {
                @Override
                public void onResponse(@NonNull Call<PriceConfigDTO> call,
                                       @NonNull Response<PriceConfigDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        configs[idx] = response.body();
                    } else {
                        configs[idx] = new PriceConfigDTO(VEHICLE_TYPES[idx], 0, 0);
                    }
                    onConfigLoaded();
                }

                @Override
                public void onFailure(@NonNull Call<PriceConfigDTO> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    configs[idx] = new PriceConfigDTO(VEHICLE_TYPES[idx], 0, 0);
                    onConfigLoaded();
                }
            });
        }
    }

    private synchronized void onConfigLoaded() {
        loadedCount++;
        if (loadedCount == VEHICLE_TYPES.length) {
            requireActivity().runOnUiThread(() -> {
                showContent();
                updateTabStyles();
                populateForm();
            });
        }
    }

    // ─── Save ─────────────────────────────────────────────────────────────────

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        clearMessages();

        double base, perKm;
        try {
            base  = parseDouble(etBasePrice.getText().toString());
            perKm = parseDouble(etPricePerKm.getText().toString());
        } catch (NumberFormatException e) {
            tvErrorMessage.setText("Please enter valid numbers.");
            tvErrorMessage.setVisibility(View.VISIBLE);
            return;
        }

        PriceConfigDTO dto = new PriceConfigDTO(activeTab, base, perKm);
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        api.updatePriceConfig(activeTab, dto).enqueue(new Callback<PriceConfigDTO>() {
            @Override
            public void onResponse(@NonNull Call<PriceConfigDTO> call,
                                   @NonNull Response<PriceConfigDTO> response) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    if (response.isSuccessful() && response.body() != null) {
                        setCurrentConfig(response.body());
                        tvSuccessMessage.setText("✓ Pricing updated successfully!");
                        tvSuccessMessage.setVisibility(View.VISIBLE);
                        tvSuccessMessage.postDelayed(() -> tvSuccessMessage.setVisibility(View.GONE), 3000);
                    } else {
                        tvErrorMessage.setText("✕ Failed to save changes. Please try again.");
                        tvErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<PriceConfigDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    tvErrorMessage.setText("✕ Network error. Please try again.");
                    tvErrorMessage.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private PriceConfigDTO getCurrentConfig() {
        for (int i = 0; i < VEHICLE_TYPES.length; i++) {
            if (VEHICLE_TYPES[i].equals(activeTab)) return configs[i];
        }
        return null;
    }

    private void setCurrentConfig(PriceConfigDTO dto) {
        for (int i = 0; i < VEHICLE_TYPES.length; i++) {
            if (VEHICLE_TYPES[i].equals(activeTab)) { configs[i] = dto; return; }
        }
    }

    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return Double.parseDouble(s.trim());
    }

    private void clearMessages() {
        tvSuccessMessage.setVisibility(View.GONE);
        tvErrorMessage.setVisibility(View.GONE);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
    }
}