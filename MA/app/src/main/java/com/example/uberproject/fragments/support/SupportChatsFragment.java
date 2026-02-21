package com.example.uberproject.fragments.support;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.BuildConfig;
import com.example.uberproject.R;
import com.example.uberproject.adapters.ChatListAdapter;
import com.example.uberproject.api.ChatApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.response.ChatDTO;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import com.example.uberproject.websocket.ChatWebSocketClient;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportChatsFragment extends Fragment {

    private String userEmail;
    private boolean isAdmin;
    private ChatApi chatApi;
    private ChatWebSocketClient wsClient;

    // Views
    private ProgressBar pbLoading;
    private RecyclerView rvChatList;
    private EditText etSearch;
    private LinearLayout llNoChats;

    private ChatListAdapter chatListAdapter;

    // ── Factory ────────────────────────────────────────────────────────────────
    public static SupportChatsFragment newInstance() {
        return new SupportChatsFragment();
    }

    // ══════════════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEmail = TokenManager.getInstance(requireContext()).getUserEmail();
        isAdmin   = AuthGuard.isAdmin(requireContext());
        chatApi   = RetrofitClient.getInstance(requireContext()).create(ChatApi.class);
        wsClient  = new ChatWebSocketClient(BuildConfig.WS_HOST);

        pbLoading    = view.findViewById(R.id.pbChatsLoading);
        rvChatList   = view.findViewById(R.id.rvChatList);
        etSearch     = view.findViewById(R.id.etChatsSearch);
        llNoChats    = view.findViewById(R.id.llNoChats);

        if (isAdmin) {
            setupAdminChatList();
            loadAllChats();
            connectAdminGlobal();
        } else {
            // User/Driver: skip the list, go straight to their chat
            navigateToChat(null, false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (wsClient != null) wsClient.disconnectAdmin();
    }

    // ── Admin setup ────────────────────────────────────────────────────────────

    private void setupAdminChatList() {
        chatListAdapter = new ChatListAdapter(chat -> navigateToChat(chat, true));
        rvChatList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChatList.setAdapter(chatListAdapter);
        rvChatList.setVisibility(View.VISIBLE);

        etSearch.setVisibility(View.VISIBLE);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                chatListAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllChats() {
        pbLoading.setVisibility(View.VISIBLE);
        chatApi.getAllChats().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatDTO>> call,
                                   @NonNull Response<List<ChatDTO>> response) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    chatListAdapter.setChats(response.body());
                    llNoChats.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    llNoChats.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                llNoChats.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Admin WebSocket (notifications only — no chat messages here) ───────────

    private void connectAdminGlobal() {
        wsClient.setAdminListener(new ChatWebSocketClient.AdminNotificationListener() {
            @Override
            public void onNotificationReceived(String json) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> handleAdminNotification(json));
            }
            @Override public void onConnected() {}
            @Override public void onDisconnected() {}
        });
        wsClient.connectAdminGlobal(userEmail);
    }

    private void handleAdminNotification(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (!"NEW_MESSAGE".equals(obj.optString("type"))) return;

            int chatId       = obj.getInt("chatId");
            String content   = obj.optString("content");
            String timestamp = obj.optString("timestamp");

            chatListAdapter.updateChatPreview(chatId, content, timestamp, "REGISTERED_USER");
            chatListAdapter.incrementUnread(chatId);
            llNoChats.setVisibility(View.GONE);

            // Brand-new chat not yet in list → fetch and add
            if (!chatListAdapter.containsChat(chatId)) {
                chatApi.getChatById(chatId).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatDTO> call,
                                           @NonNull Response<ChatDTO> res) {
                        if (!isAdded() || !res.isSuccessful() || res.body() == null) return;
                        chatListAdapter.addChatAtTop(res.body());
                        chatListAdapter.incrementUnread(chatId);
                        llNoChats.setVisibility(View.GONE);
                    }
                    @Override public void onFailure(@NonNull Call<ChatDTO> c, @NonNull Throwable t) {}
                });
            }
        } catch (Exception e) {
            android.util.Log.e("SupportChatsFragment", "Admin notification parse error", e);
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    private void navigateToChat(ChatDTO chat, boolean clearUnread) {
        if (chat != null && clearUnread) {
            chatListAdapter.clearUnread(chat.getId());
            chatListAdapter.setSelectedChatId(chat.getId());
        }

        SupportChatFragment chatFragment = chat != null
                ? SupportChatFragment.newInstanceAdmin(chat.getId(),
                chat.getUserFullName(), chat.getRoleLabel())
                : SupportChatFragment.newInstanceUser();

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }
}