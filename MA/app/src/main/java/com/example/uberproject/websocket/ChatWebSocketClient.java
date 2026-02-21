package com.example.uberproject.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatWebSocketClient {

    private static final String TAG = "ChatWS";

    // ── Listeners ──────────────────────────────────────────────────────────────

    public interface ChatMessageListener {
        void onMessageReceived(String messageJson);
        void onConnected();
        void onDisconnected();
    }

    public interface AdminNotificationListener {
        void onNotificationReceived(String notificationJson);
        void onConnected();
        void onDisconnected();
    }

    // ── State ──────────────────────────────────────────────────────────────────

    private WebSocketClient chatClient;
    private WebSocketClient adminClient;

    private ChatMessageListener chatListener;
    private AdminNotificationListener adminListener;

    private final String wsBaseUrl; // e.g. "ws://192.168.1.x:8080"

    public ChatWebSocketClient(String wsBaseUrl) {
        this.wsBaseUrl = wsBaseUrl;
    }

    // ── Public setters ─────────────────────────────────────────────────────────

    public void setChatListener(ChatMessageListener listener) {
        this.chatListener = listener;
    }

    public void setAdminListener(AdminNotificationListener listener) {
        this.adminListener = listener;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHAT SESSION  (/ws/chat?chatId=X&email=Y)
    // ══════════════════════════════════════════════════════════════════════════

    public void connectToChat(int chatId, String email) {
        disconnectChat();
        try {
            String url = wsBaseUrl + "/ws/chat?chatId=" + chatId
                    + "&email=" + encode(email);
            URI uri = new URI(url);

            chatClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "Chat WS connected. chatId=" + chatId);
                    if (chatListener != null) chatListener.onConnected();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Chat message: " + message);
                    if (chatListener != null) chatListener.onMessageReceived(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "Chat WS closed. reason=" + reason);
                    if (chatListener != null) chatListener.onDisconnected();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "Chat WS error", ex);
                }
            };
            chatClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid chat WS URL", e);
        }
    }

    /**
     * Send a JSON message through the chat WebSocket.
     * Falls back silently if the socket is not open — callers should use HTTP fallback.
     *
     * @return true if sent via WebSocket, false if not connected
     */
    public boolean sendChatMessage(String jsonPayload) {
        if (chatClient != null && chatClient.isOpen()) {
            chatClient.send(jsonPayload);
            return true;
        }
        return false;
    }

    public void disconnectChat() {
        if (chatClient != null) {
            chatClient.close();
            chatClient = null;
        }
    }

    public boolean isChatConnected() {
        return chatClient != null && chatClient.isOpen();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ADMIN GLOBAL  (/ws/chat/admin?email=Y)
    // ══════════════════════════════════════════════════════════════════════════

    public void connectAdminGlobal(String email) {
        disconnectAdmin();
        try {
            String url = wsBaseUrl + "/ws/chat/admin?email=" + encode(email);
            URI uri = new URI(url);

            adminClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "Admin global WS connected");
                    if (adminListener != null) adminListener.onConnected();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Admin notification: " + message);
                    if (adminListener != null) adminListener.onNotificationReceived(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "Admin WS closed. reason=" + reason);
                    if (adminListener != null) adminListener.onDisconnected();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "Admin WS error", ex);
                }
            };
            adminClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid admin WS URL", e);
        }
    }

    public void disconnectAdmin() {
        if (adminClient != null) {
            adminClient.close();
            adminClient = null;
        }
    }

    public boolean isAdminConnected() {
        return adminClient != null && adminClient.isOpen();
    }

    // ── Disconnect all ─────────────────────────────────────────────────────────

    public void disconnectAll() {
        disconnectChat();
        disconnectAdmin();
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}