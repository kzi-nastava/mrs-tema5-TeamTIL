package com.example.uberproject.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class RideTrackingWebSocketClient {

    private static final String TAG = "RideTrackingWS";

    public interface TrackingListener {
        /** Poziva se svaki put kad stigne nova pozicija vozila */
        void onPositionUpdate(double lat, double lng, int remainingMin, int currentPrice);
        /** Vozač je završio vožnju */
        void onRideEnded();
        /** Vožnja nije aktivna (WebSocket ne može da prati) */
        void onRideNotActive();
        /** WS konekcija uspostavljena */
        void onConnected();
        /** WS konekcija prekinuta */
        void onDisconnected();
    }

    private WebSocketClient webSocketClient;
    private TrackingListener listener;
    private final int rideId;
    private final String wsBaseUrl;

    public RideTrackingWebSocketClient(String wsBaseUrl, int rideId) {
        this.wsBaseUrl = wsBaseUrl;
        this.rideId = rideId;
    }

    public void setListener(TrackingListener listener) {
        this.listener = listener;
    }

    /**
     * @param token JWT token za autentifikaciju (dodaje se kao query param)
     */
    public void connect(String token) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            Log.w(TAG, "Already connected, disconnecting first.");
            webSocketClient.close();
        }

        try {
            // Backend WebSocket endpoint: /ws/ride-tracking?token=...
            String url = wsBaseUrl + "?token=" + token;
            URI uri = new URI(url);

            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connected, subscribing to rideId=" + rideId);
                    try {
                        // Šalji rideId da se server zna na koju vožnju da se pretplati
                        JSONObject subscribeMsg = new JSONObject();
                        subscribeMsg.put("rideId", rideId);
                        send(subscribeMsg.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending subscribe message", e);
                    }
                    if (listener != null) listener.onConnected();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);
                    parseMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket closed. Code=" + code + " Reason=" + reason);
                    if (listener != null) listener.onDisconnected();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error", ex);
                }
            };

            webSocketClient.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid WebSocket URL: " + wsBaseUrl, e);
        }
    }

    private void parseMessage(String raw) {
        try {
            JSONObject json = new JSONObject(raw);

            // Provjeri da li je specijalna poruka (type field)
            if (json.has("type")) {
                String type = json.getString("type");
                switch (type) {
                    case "RIDE_ENDED":
                        if (listener != null) listener.onRideEnded();
                        break;
                    case "RIDE_NOT_ACTIVE":
                        if (listener != null) listener.onRideNotActive();
                        break;
                    default:
                        Log.w(TAG, "Unknown message type: " + type);
                }
                return;
            }

            // Pozicija vozila (WebSocketRideDTO sa backend-a)
            double lat = json.getDouble("latitude");
            double lng = json.getDouble("longitude");
            int remainingMin = json.getInt("remainingDurationInMinutes");
            int currentPrice = json.getInt("currentPrice");

            if (listener != null) {
                listener.onPositionUpdate(lat, lng, remainingMin, currentPrice);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing tracking message: " + raw, e);
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