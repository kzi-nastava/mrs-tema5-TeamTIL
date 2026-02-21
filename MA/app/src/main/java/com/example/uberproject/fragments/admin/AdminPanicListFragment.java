package com.example.uberproject.fragments.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.PanicNotificationAdapter;
import com.example.uberproject.api.PanicApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.response.PanicResponseDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanicListFragment extends Fragment {

    private static final String TAG = "AdminPanicListFragment";

    private RecyclerView rvPanicList;
    private ProgressBar loadingIndicator;
    private TextView tvNoPanics;
    private PanicNotificationAdapter adapter;
    private List<PanicResponseDTO> panicList = new ArrayList<>();
    private PanicApi panicApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_panic_list, container, false);

        rvPanicList = view.findViewById(R.id.rvPanicList);
        loadingIndicator = view.findViewById(R.id.panicLoadingIndicator);
        tvNoPanics = view.findViewById(R.id.tvNoPanics);

        panicApi = RetrofitClient.getInstance(requireContext()).create(PanicApi.class);

        adapter = new PanicNotificationAdapter(panicList, this::handlePanic);
        rvPanicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPanicList.setAdapter(adapter);

        loadPanics();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPanics();
    }

    private void loadPanics() {
        loadingIndicator.setVisibility(View.VISIBLE);
        tvNoPanics.setVisibility(View.GONE);

        Log.d(TAG, "Calling GET /api/panic endpoint...");
        panicApi.getAllPanics().enqueue(new Callback<List<PanicResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<PanicResponseDTO>> call,
                                   @NonNull Response<List<PanicResponseDTO>> response) {
                if (!isAdded()) return;
                loadingIndicator.setVisibility(View.GONE);

                Log.d(TAG, "Response code: " + response.code() + ", isSuccessful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully loaded " + response.body().size() + " panics");
                    panicList.clear();
                    panicList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    tvNoPanics.setVisibility(panicList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "(no error body)";
                    } catch (Exception e) {
                        errorBody = "Could not read error body: " + e.getMessage();
                    }
                    Log.e(TAG, "Failed to load panics: HTTP " + response.code());
                    Log.e(TAG, "Error response body: " + errorBody);
                    Log.e(TAG, "Response message: " + response.message());

                    tvNoPanics.setVisibility(View.VISIBLE);
                    tvNoPanics.setText("Failed to load panic notifications (HTTP " + response.code() + ")\nCheck logs for details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PanicResponseDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                loadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "Network error loading panics: " + t.getMessage());
                Log.e(TAG, "Error type: " + t.getClass().getName());
                t.printStackTrace();
                tvNoPanics.setVisibility(View.VISIBLE);
                tvNoPanics.setText("Network error: " + t.getMessage());
            }
        });
    }

    private void handlePanic(PanicResponseDTO panic) {
        if (panic == null || panic.getId() == null) return;

        panicApi.handlePanic(panic.getId()).enqueue(new Callback<PanicResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<PanicResponseDTO> call,
                                   @NonNull Response<PanicResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Panic #" + panic.getId() + " marked as handled", Toast.LENGTH_SHORT).show();
                    // Refresh the list
                    loadPanics();
                } else {
                    Toast.makeText(requireContext(), "Failed to handle panic: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PanicResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

