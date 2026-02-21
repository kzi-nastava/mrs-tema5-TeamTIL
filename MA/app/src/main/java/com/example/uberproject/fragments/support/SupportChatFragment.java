package com.example.uberproject.fragments.support;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uberproject.BuildConfig;
import com.example.uberproject.R;
import com.example.uberproject.adapters.ChatMessagesAdapter;
import com.example.uberproject.api.ChatApi;
import com.example.uberproject.api.RetrofitClient;
import com.example.uberproject.dto.request.SendMessageRequest;
import com.example.uberproject.dto.response.ChatDTO;
import com.example.uberproject.dto.response.MessageDTO;
import com.example.uberproject.utils.AuthGuard;
import com.example.uberproject.utils.TokenManager;
import com.example.uberproject.websocket.ChatWebSocketClient;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportChatFragment extends Fragment {

    // ── Args keys ──────────────────────────────────────────────────────────────
    private static final String ARG_CHAT_ID    = "chat_id";
    private static final String ARG_USER_NAME  = "user_name";
    private static final String ARG_USER_ROLE  = "user_role";
    private static final String ARG_IS_ADMIN   = "is_admin";

    // ── State ──────────────────────────────────────────────────────────────────
    private boolean isAdmin;
    private String userEmail;
    private Integer chatId;           // null until resolved for user/driver
    private boolean chatExists;       // user/driver: has chat been created yet
    private boolean wsConnected;

    private ChatApi chatApi;
    private ChatWebSocketClient wsClient;
    private final Gson gson = new Gson();

    // ── Views ──────────────────────────────────────────────────────────────────
    private RecyclerView rvMessages;
    private EditText etMessage;
    private AppCompatButton btnSend;
    private ProgressBar pbLoading;
    private LinearLayout llNoChat;     // user empty state (no chat yet)
    private TextView tvHeaderName, tvHeaderRole;

    private ChatMessagesAdapter messagesAdapter;
    private LinearLayoutManager layoutManager;

    // ── Factories ──────────────────────────────────────────────────────────────

    /** Admin opens a specific chat by id. */
    public static SupportChatFragment newInstanceAdmin(int chatId, String userName, String userRole) {
        SupportChatFragment f = new SupportChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CHAT_ID, chatId);
        args.putString(ARG_USER_NAME, userName);
        args.putString(ARG_USER_ROLE, userRole);
        args.putBoolean(ARG_IS_ADMIN, true);
        f.setArguments(args);
        return f;
    }

    /** User/Driver opens their own chat (chatId unknown until loaded). */
    public static SupportChatFragment newInstanceUser() {
        SupportChatFragment f = new SupportChatFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, false);
        f.setArguments(args);
        return f;
    }

    // ══════════════════════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEmail = TokenManager.getInstance(requireContext()).getUserEmail();
        chatApi   = RetrofitClient.getInstance(requireContext()).create(ChatApi.class);
        wsClient  = new ChatWebSocketClient(BuildConfig.WS_HOST);

        isAdmin   = getArguments() != null && getArguments().getBoolean(ARG_IS_ADMIN, false);

        bindViews(view);
        setupRecyclerView();
        setupSendButton();
        setupHeader();

        if (isAdmin) {
            chatId = getArguments() != null ? getArguments().getInt(ARG_CHAT_ID, -1) : -1;
            loadChatById(chatId);
        } else {
            loadExistingChat();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (wsClient != null) wsClient.disconnectAll();
    }

    // ── Bind & setup ───────────────────────────────────────────────────────────

    private void bindViews(View v) {
        rvMessages   = v.findViewById(R.id.rvMessages);
        etMessage    = v.findViewById(R.id.etMessage);
        btnSend      = v.findViewById(R.id.btnSend);
        pbLoading    = v.findViewById(R.id.pbChatLoading);
        llNoChat     = v.findViewById(R.id.llNoChat);
        tvHeaderName = v.findViewById(R.id.tvChatHeaderName);
        tvHeaderRole = v.findViewById(R.id.tvChatHeaderRole);
    }

    private void setupRecyclerView() {
        messagesAdapter = new ChatMessagesAdapter(isAdmin);
        layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messagesAdapter);
    }

    private void setupHeader() {
        if (isAdmin && getArguments() != null) {
            tvHeaderName.setText(getArguments().getString(ARG_USER_NAME, ""));
            tvHeaderRole.setText(getArguments().getString(ARG_USER_ROLE, "user"));
        } else {
            tvHeaderName.setText("Support Team");
            tvHeaderRole.setText("admin");
        }
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOAD CHAT
    // ══════════════════════════════════════════════════════════════════════════

    private void loadChatById(int id) {
        pbLoading.setVisibility(View.VISIBLE);
        chatApi.getChatById(id).enqueue(new Callback<ChatDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChatDTO> call,
                                   @NonNull Response<ChatDTO> response) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    populateChat(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to load chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingChat() {
        pbLoading.setVisibility(View.VISIBLE);
        chatApi.getExistingChat(userEmail).enqueue(new Callback<ChatDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChatDTO> call,
                                   @NonNull Response<ChatDTO> response) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    chatExists = true;
                    llNoChat.setVisibility(View.GONE);
                    populateChat(response.body());
                } else {
                    // No chat yet
                    chatExists = false;
                    llNoChat.setVisibility(View.VISIBLE);
                    rvMessages.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
                chatExists = false;
                llNoChat.setVisibility(View.VISIBLE);
                rvMessages.setVisibility(View.GONE);
            }
        });
    }

    private void populateChat(ChatDTO chat) {
        chatId = chat.getId();
        chatExists = true;
        llNoChat.setVisibility(View.GONE);
        rvMessages.setVisibility(View.VISIBLE);
        messagesAdapter.setMessages(chat.getMessages());
        scrollToBottom();
        connectToChat(chatId);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WEBSOCKET — chat session
    // ══════════════════════════════════════════════════════════════════════════

    private void connectToChat(int id) {
        wsClient.disconnectChat(); // clean up any previous connection

        wsClient.setChatListener(new ChatWebSocketClient.ChatMessageListener() {
            @Override
            public void onMessageReceived(String json) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> handleIncomingMessage(json));
            }

            @Override
            public void onConnected() {
                wsConnected = true;
                android.util.Log.d("SupportChatFragment", "WS connected to chat " + id);
            }

            @Override
            public void onDisconnected() {
                wsConnected = false;
                android.util.Log.d("SupportChatFragment", "WS disconnected — attempting reconnect");
                // Reconnect after a short delay
                if (isAdded()) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> {
                                if (isAdded() && chatId != null) connectToChat(chatId);
                            }, 3000);
                }
            }
        });

        wsClient.connectToChat(id, userEmail);
    }

    private void handleIncomingMessage(String json) {
        try {
            MessageDTO msg = gson.fromJson(json, MessageDTO.class);
            if (msg == null || !chatId.equals(msg.getChatId())) return;
            messagesAdapter.addMessage(msg);
            scrollToBottom();
        } catch (Exception e) {
            android.util.Log.e("SupportChatFragment", "Message parse error", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SEND MESSAGE — ALWAYS HTTP, receive echo via WS broadcast from server
    // ══════════════════════════════════════════════════════════════════════════

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessage.setText("");

        // User/Driver: first-ever message → create chat via HTTP
        if (!isAdmin && !chatExists) {
            chatApi.startChatWithMessage(userEmail, new SendMessageRequest(userEmail, text))
                    .enqueue(new Callback<ChatDTO>() {
                        @Override
                        public void onResponse(@NonNull Call<ChatDTO> call,
                                               @NonNull Response<ChatDTO> response) {
                            if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                            populateChat(response.body());
                        }
                        @Override
                        public void onFailure(@NonNull Call<ChatDTO> c, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Failed to send", Toast.LENGTH_SHORT).show();
                        }
                    });
            return;
        }

        if (chatId == null) return;

        SendMessageRequest req = new SendMessageRequest(userEmail, text);

        // Always send via HTTP — the server will broadcastToChat() back to all WS sessions,
        // including ours, so the message will appear via handleIncomingMessage().
        // This avoids the race condition where WS isn't OPEN yet when sendMessage() is called.
        chatApi.sendMessageHttp(chatId, req).enqueue(new Callback<MessageDTO>() {
            @Override
            public void onResponse(@NonNull Call<MessageDTO> call,
                                   @NonNull Response<MessageDTO> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                }
                // Don't add message here — it will arrive via WS broadcast from server.
                // If WS is not connected for some reason, add it manually as fallback:
                if (!wsClient.isChatConnected() && response.body() != null) {
                    messagesAdapter.addMessage(response.body());
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageDTO> c, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void scrollToBottom() {
        int count = messagesAdapter.getItemCount();
        if (count > 0) rvMessages.smoothScrollToPosition(count - 1);
    }
}