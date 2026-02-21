package com.example.uberproject.websocket;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class PanicWebSocketManager {

    private static final String TAG = "PanicWebSocket";
    private WebSocketClient webSocketClient;
    private PanicListener panicListener;
    private final Gson gson = new Gson();

    public interface PanicListener {
        void onPanicAlertReceived(PanicAlert alert);
        void onConnectionEstablished();
        void onConnectionLost();
        void onError(String error);
    }

    public static class PanicAlert {
        @SerializedName(value = "panicId", alternate = {"id"})
        public Integer panicId;
        public Integer rideId;
        public Integer driverId;
        public Integer registeredUserId;
        public String vehicleName;
        public String vehicleLicensePlate;
        public String locationAddress;
        public Double latitude;
        public Double longitude;
        public String reportedBy;
        public String timestamp;

        @Override
        public String toString() {
            return "PanicAlert{panicId=" + panicId +
                    ", rideId=" + rideId +
                    ", vehicleName='" + vehicleName + '\'' +
                    ", vehicleLicensePlate='" + vehicleLicensePlate + '\'' +
                    ", locationAddress='" + locationAddress + '\'' +
                    ", reportedBy='" + reportedBy + '\'' + '}';
        }
    }

    public PanicWebSocketManager() {}

    public void setPanicListener(PanicListener listener) {
        this.panicListener = listener;
    }

    /**
     * Connect to /ws/notifications?email=<adminEmail>
     * The backend NotificationHandler uses email as the session key.
     */
    public void connect(String baseWsUrl, String adminEmail) {
        try {
            // baseWsUrl e.g. ws://192.168.1.23:8080
            URI uri = new URI(baseWsUrl + "/ws/notifications?email=" + adminEmail);
            Log.d(TAG, "Connecting to: " + uri);

            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connected as " + adminEmail);
                    if (panicListener != null) panicListener.onConnectionEstablished();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "WS message: " + message);
                    parsePanicAlert(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed: " + reason);
                    if (panicListener != null) panicListener.onConnectionLost();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    if (panicListener != null) panicListener.onError(ex != null ? ex.getMessage() : "Unknown error");
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URL", e);
            if (panicListener != null) panicListener.onError("Invalid URL: " + e.getMessage());
        }
    }

    private void parsePanicAlert(String message) {
        try {
            Log.d(TAG, "Parsing WS message: " + message);
            PanicAlert alert = gson.fromJson(message, PanicAlert.class);
            if (alert != null && (alert.panicId != null || alert.rideId != null)) {
                Log.d(TAG, "Parsed panic alert: " + alert);
                if (panicListener != null) panicListener.onPanicAlertReceived(alert);
            } else {
                Log.d(TAG, "Message is not a panic alert, ignoring: " + message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing panic alert: " + message, e);
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}
