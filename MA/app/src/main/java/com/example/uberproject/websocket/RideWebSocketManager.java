package com.example.uberproject.websocket;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class RideWebSocketManager {

    private static final String TAG = "RideWebSocket";

    private WebSocketClient webSocketClient;
    private RideNotificationListener listener;
    private final Gson gson = new Gson();

    // -------------------------------------------------------------------------
    // Data classes za svaki tip notifikacije
    // -------------------------------------------------------------------------
    public static class RideAcceptedNotification {
        public Integer rideId;
        public String message;
        public String driverName;
        public String vehicle;
    }

    public static class RideRejectedNotification {
        public String message;
    }

    public static class RideReminderNotification {
        public Integer rideId;
        public String message;
        public String from;
        public String to;
    }

    public static class NewRideAssignedNotification {
        public Integer rideId;
        public String message;
        public String from;
        public String to;
        public String passengerName;
    }

    public static class RideFinishedNotification {
        public Integer rideId;
        public String message;
        public String from;
        public String to;
        public Double price;
    }

    public static class RideCancelledNotification {
        public Integer rideId;
        public String message;
    }

    public static class RideStoppedNotification {
        public Integer rideId;
        public String message;
    }

    // -------------------------------------------------------------------------
    // Listener interface
    // -------------------------------------------------------------------------

    public interface RideNotificationListener {
        void onRideAccepted(RideAcceptedNotification notification);

        void onRideRejected(RideRejectedNotification notification);

        void onRideReminder(RideReminderNotification notification);

        void onNewRideAssigned(NewRideAssignedNotification notification);

        void onRideFinished(RideFinishedNotification notification);
        void onRideCancelled(RideCancelledNotification notification);
        void onRideStopped(RideStoppedNotification notification);

        void onConnectionEstablished();

        void onConnectionLost();
    }

    private static RideWebSocketManager instance;

    public static synchronized RideWebSocketManager getInstance() {
        if (instance == null) {
            instance = new RideWebSocketManager();
        }
        return instance;
    }

    private RideWebSocketManager() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void setListener(RideNotificationListener listener) {
        this.listener = listener;
    }

    public void connect(String baseWsUrl, String userEmail) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            Log.d(TAG, "Already connected, skipping connect()");
            return;
        }

        try {
            URI uri = new URI(baseWsUrl + "/ws/notifications?email=" + userEmail);
            Log.d(TAG, "Connecting to: " + uri);

            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connected as " + userEmail);
                    if (listener != null) listener.onConnectionEstablished();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "WS message received: " + message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed. Reason: " + reason);
                    if (listener != null) listener.onConnectionLost();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                }
            };

            webSocketClient.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URL: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
            Log.d(TAG, "WebSocket disconnected");
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    // -------------------------------------------------------------------------
    // Private - parsing i routing poruka
    // -------------------------------------------------------------------------

    private void handleMessage(String message) {
        try {
            // Izvuci "type" polje iz JSON-a
            RawMessage raw = gson.fromJson(message, RawMessage.class);
            if (raw == null || raw.type == null) {
                Log.d(TAG, "Message has no 'type' field, ignoring");
                return;
            }

            switch (raw.type) {
                case "RIDE_ACCEPTED":
                    if (listener != null) {
                        RideAcceptedNotification n = gson.fromJson(message, RideAcceptedNotification.class);
                        listener.onRideAccepted(n);
                    }
                    break;

                case "RIDE_REJECTED":
                    if (listener != null) {
                        RideRejectedNotification n = gson.fromJson(message, RideRejectedNotification.class);
                        listener.onRideRejected(n);
                    }
                    break;

                case "RIDE_REMINDER":
                    if (listener != null) {
                        RideReminderNotification n = gson.fromJson(message, RideReminderNotification.class);
                        listener.onRideReminder(n);
                    }
                    break;

                case "NEW_RIDE_ASSIGNED":
                    if (listener != null) {
                        NewRideAssignedNotification n = gson.fromJson(message, NewRideAssignedNotification.class);
                        listener.onNewRideAssigned(n);
                    }
                    break;

                case "RIDE_FINISHED":
                    if (listener != null) {
                        RideFinishedNotification n = gson.fromJson(message, RideFinishedNotification.class);
                        listener.onRideFinished(n);
                    }
                    break;

                case "PANIC_ALERT":
                    // Panic handleuje PanicWebSocketManager, ovde ignorisemo
                    Log.d(TAG, "PANIC_ALERT received, handled by PanicWebSocketManager");
                    break;

                case "RIDE_CANCELLED":
                    if (listener != null) {
                        RideCancelledNotification n = gson.fromJson(message, RideCancelledNotification.class);
                        listener.onRideCancelled(n);
                    }
                    break;

                case "RIDE_STOPPED":
                    if (listener != null) {
                        RideStoppedNotification n = gson.fromJson(message, RideStoppedNotification.class);
                        listener.onRideStopped(n);
                    }
                    break;

                default:
                    Log.d(TAG, "Unknown notification type: " + raw.type);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing WebSocket message: " + message, e);
        }
    }

    private static class RawMessage {
        public String type;
    }
}
