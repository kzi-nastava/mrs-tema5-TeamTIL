package com.example.uberproject.websocket;

import android.content.Context;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class PanicWebSocketManager {

    private static final String TAG = "PanicWebSocket";
    private WebSocketClient webSocketClient;
    private PanicListener panicListener;
    private Context context;
    private String token;

    public interface PanicListener {
        void onPanicAlertReceived(PanicAlert alert);
        void onConnectionEstablished();
        void onConnectionLost();
        void onError(String error);
    }

    public static class PanicAlert {
        public Integer panicId;
        public Integer rideId;
        public Integer driverId;
        public Integer passengerid;
        public String vehicleName;
        public String licensePlate;
        public String location;
        public Double latitude;
        public Double longitude;
        public String reportedBy;
        public String timestamp;

        @Override
        public String toString() {
            return "PanicAlert{" +
                    "panicId=" + panicId +
                    ", rideId=" + rideId +
                    ", vehicleName='" + vehicleName + '\'' +
                    ", licensePlate='" + licensePlate + '\'' +
                    ", location='" + location + '\'' +
                    ", reportedBy='" + reportedBy + '\'' +
                    '}';
        }
    }

    public PanicWebSocketManager(Context context, String token) {
        this.context = context;
        this.token = token;
    }

    public void setPanicListener(PanicListener listener) {
        this.panicListener = listener;
    }

    public void connect(String wsUrl) {
        try {
            URI uri = new URI(wsUrl + "?token=" + token);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connected");
                    if (panicListener != null) {
                        panicListener.onConnectionEstablished();
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "WebSocket message received: " + message);
                    parsePanicAlert(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed: " + reason);
                    if (panicListener != null) {
                        panicListener.onConnectionLost();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                    if (panicListener != null) {
                        panicListener.onError(ex.getMessage());
                    }
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URL", e);
            if (panicListener != null) {
                panicListener.onError("Invalid URL: " + e.getMessage());
            }
        }
    }

    private void parsePanicAlert(String message) {
        try {
            // Parse JSON message using org.json or gson
            // For now, using simple string parsing
            PanicAlert alert = new PanicAlert();

            // You would parse JSON here
            // Using JsonParser or similar
            if (panicListener != null) {
                panicListener.onPanicAlertReceived(alert);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing panic alert", e);
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}

