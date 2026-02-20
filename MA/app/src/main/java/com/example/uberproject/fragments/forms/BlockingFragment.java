package com.example.uberproject.fragments.forms;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.R;
import com.example.uberproject.adapters.UserBlockAdapter;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.api.UserApi;
import com.example.uberproject.dto.request.BlockUserRequestDTO;
import com.example.uberproject.dto.response.UserListItemDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockingFragment extends Fragment {

    private UserApi userApi;
    private UserBlockAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText etSearch;
    private TextView tabDrivers, tabUsers;
    private boolean showingDrivers = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blocking, container, false);

        userApi = RetrofitClient.getInstance(getContext()).create(UserApi.class);

        recyclerView = view.findViewById(R.id.rvUsers);
        progressBar = view.findViewById(R.id.progressBar);
        etSearch = view.findViewById(R.id.etSearch);
        tabDrivers = view.findViewById(R.id.tabDrivers);
        tabUsers = view.findViewById(R.id.tabUsers);

        adapter = new UserBlockAdapter(getContext(), this::onBlockAction);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
        });

        // Tab clicks
        tabDrivers.setOnClickListener(v -> {
            if (!showingDrivers) {
                showingDrivers = true;
                updateTabStyles();
                etSearch.setText("");
                loadDrivers();
            }
        });

        tabUsers.setOnClickListener(v -> {
            if (showingDrivers) {
                showingDrivers = false;
                updateTabStyles();
                etSearch.setText("");
                loadUsers();
            }
        });

        // Load drivers by default
        updateTabStyles();
        loadDrivers();

        return view;
    }

    private void loadDrivers() {
        showLoading(true);
        userApi.getAllDrivers().enqueue(new Callback<List<UserListItemDTO>>() {
            @Override
            public void onResponse(Call<List<UserListItemDTO>> call, Response<List<UserListItemDTO>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setUsers(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<UserListItemDTO>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Failed to load drivers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        showLoading(true);
        userApi.getAllUsers().enqueue(new Callback<List<UserListItemDTO>>() {
            @Override
            public void onResponse(Call<List<UserListItemDTO>> call, Response<List<UserListItemDTO>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setUsers(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<UserListItemDTO>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onBlockAction(UserListItemDTO user, boolean currentlyBlocked) {
        String fullName = user.getFirstName() + " " + user.getLastName();

        if (currentlyBlocked) {
            // prikazi confirmation dialog
            new AlertDialog.Builder(getContext())
                    .setTitle("Unblock " + fullName)
                    .setMessage("Are you sure you want to unblock this user?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Unblock", (dialog, which) -> {
                        sendBlockRequest(user, false, null);
                    })
                    .show();
        } else {
            // prikazi dialog sa razlogom
            View dialogView = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_block_reason, null);
            EditText etReason = dialogView.findViewById(R.id.etBlockReason);
            TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
            tvDialogTitle.setText("Block " + fullName);

            new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Block", (dialog, which) -> {
                        String reason = etReason.getText().toString().trim();
                        sendBlockRequest(user, true, reason);
                    })
                    .show();
        }
    }

    private void sendBlockRequest(UserListItemDTO user, boolean block, String reason) {
        BlockUserRequestDTO request = new BlockUserRequestDTO(user.getId(), block, reason);

        userApi.blockUser(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // azuriraj lokalno bez reloada
                    user.setIsBlocked(block);
                    user.setBlockReason(block ? reason : null);
                    adapter.notifyDataSetChanged();

                    String msg = block ? "User blocked successfully" : "User unblocked successfully";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Action failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void updateTabStyles() {
        if (showingDrivers) {
            tabDrivers.setTypeface(null, Typeface.BOLD);
            tabDrivers.setBackgroundResource(R.drawable.bg_button_yellow);
            tabUsers.setTypeface(null, Typeface.NORMAL);
            tabUsers.setBackgroundResource(android.R.color.transparent);
        } else {
            tabUsers.setTypeface(null, Typeface.BOLD);
            tabUsers.setBackgroundResource(R.drawable.bg_button_yellow);
            tabDrivers.setTypeface(null, Typeface.NORMAL);
            tabDrivers.setBackgroundResource(android.R.color.transparent);
        }
    }
}
