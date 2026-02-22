package com.example.uberproject.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class NotificationWebSocketClient {

    private static final String TAG = "NotificationWS";

    public interface NotificationListener {
        void onRideFinished(int rideId, String message);
        void onRideAccepted(int rideId, String driverName, String vehicle, String message);
        void onRideRejected(String message);
        void onNewRideAssigned(int rideId, String from, String to, String passengerName, String message);
        void onRideCancelled(int rideId, String message);
        void onRideReminder(int rideId, String message);
        void onConnected();
        void onDisconnected();
    }

    private WebSocketClient client;
    private NotificationListener listener;
    private final String wsBaseUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NotificationWebSocketClient(String wsBaseUrl) {
        this.wsBaseUrl = wsBaseUrl;
    }

    public void setListener(NotificationListener listener) {
        this.listener = listener;
    }

    public void connect(String email) {
        try {
            String url = wsBaseUrl + "/ws/notifications?email=" + encode(email);
            client = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake h) {
                    Log.d(TAG, "Connected");
                    mainHandler.post(() -> { if (listener != null) listener.onConnected(); });
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Notification: " + message);
                    mainHandler.post(() -> parseAndDispatch(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "Disconnected: " + reason);
                    mainHandler.post(() -> { if (listener != null) listener.onDisconnected(); });
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WS error", ex);
                }
            };
            client.connect();
        } catch (Exception e) {
            Log.e(TAG, "Connection error", e);
        }
    }

    private void parseAndDispatch(String raw) {
        if (listener == null) return;
        try {
            JSONObject json = new JSONObject(raw);
            String type = json.optString("type", "");
            String message = json.optString("message", "");

            switch (type) {
                case "RIDE_FINISHED":
                    listener.onRideFinished(
                            json.optInt("rideId", -1),
                            message.isEmpty() ? "Your ride has been completed!" : message
                    );
                    break;
                case "RIDE_ACCEPTED":
                    listener.onRideAccepted(
                            json.optInt("rideId", -1),
                            json.optString("driverName", ""),
                            json.optString("vehicle", ""),
                            message.isEmpty() ? "Your ride has been accepted!" : message
                    );
                    break;
                case "RIDE_REJECTED":
                    listener.onRideRejected(
                            message.isEmpty() ? "No available drivers at the moment." : message
                    );
                    break;
                case "NEW_RIDE_ASSIGNED":
                    listener.onNewRideAssigned(
                            json.optInt("rideId", -1),
                            json.optString("from", ""),
                            json.optString("to", ""),
                            json.optString("passengerName", ""),
                            message.isEmpty() ? "You have a new ride assigned!" : message
                    );
                    break;
                case "RIDE_CANCELLED":
                    listener.onRideCancelled(
                            json.optInt("rideId", -1),
                            message.isEmpty() ? "The ride has been cancelled." : message
                    );
                    break;
                case "RIDE_REMINDER":
                    listener.onRideReminder(
                            json.optInt("rideId", -1),
                            message.isEmpty() ? "Your ride is starting soon!" : message
                    );
                    break;
                default:
                    Log.d(TAG, "Unhandled type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
        }
    }

    public void disconnect() {
        if (client != null) { client.close(); client = null; }
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }

    private String encode(String v) {
        try { return java.net.URLEncoder.encode(v, "UTF-8"); }
        catch (Exception e) { return v; }
    }
}