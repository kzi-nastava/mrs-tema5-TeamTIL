package com.example.uberproject.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.uberproject.R;
import com.example.uberproject.activities.MainActivity;

public class NotificationHelper {

    // Notification Channel ID-jevi
    public static final String CHANNEL_RIDES    = "ride_notifications";
    public static final String CHANNEL_REMINDER = "ride_reminders";

    // Notification ID-jevi
    private static final int ID_RIDE_ACCEPTED    = 1001;
    private static final int ID_RIDE_REJECTED    = 1002;
    private static final int ID_RIDE_REMINDER    = 1003;
    private static final int ID_NEW_RIDE_ASSIGNED = 1004;
    private static final int ID_RIDE_FINISHED    = 1005;

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            // Kanal za obicne ride notifikacije
            NotificationChannel ridesChannel = new NotificationChannel(
                    CHANNEL_RIDES,
                    "Ride Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            ridesChannel.setDescription("Notifications for ride status updates");
            ridesChannel.enableVibration(true);
            manager.createNotificationChannel(ridesChannel);

            // Kanal za podsetnike
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Ride Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Reminders before your ride starts");
            reminderChannel.enableVibration(true);
            manager.createNotificationChannel(reminderChannel);
        }
    }

    // -------------------------------------------------------------------------
    // Metode za svaki tip notifikacije
    // -------------------------------------------------------------------------

    public static void showRideAccepted(Context context, String driverName, String vehicle) {
        String title = "Ride Accepted!";
        String body  = "Your driver " + driverName + " is on the way. Vehicle: " + vehicle;
        show(context, CHANNEL_RIDES, ID_RIDE_ACCEPTED, title, body);
    }


    public static void showRideRejected(Context context) {
        String title = "No Drivers Available";
        String body  = "No available drivers at the moment. Please try again later.";
        show(context, CHANNEL_RIDES, ID_RIDE_REJECTED, title, body);
    }


    public static void showRideReminder(Context context, long minutesBefore, String from, String to) {
        String title = "Ride Reminder - " + minutesBefore + " min";
        String body  = "Your ride from " + from + " to " + to +
                       " starts in " + minutesBefore + " minutes!";
        show(context, CHANNEL_REMINDER, ID_RIDE_REMINDER, title, body);
    }


    public static void showNewRideAssigned(Context context, String passengerName, String from, String to) {
        String title = "New Ride Assigned!";
        String body  = "Passenger: " + passengerName + "\n" + from + " → " + to;
        show(context, CHANNEL_RIDES, ID_NEW_RIDE_ASSIGNED, title, body);
    }


    public static void showRideFinished(Context context, String from, String to, Double price) {
        String title = "Ride Completed!";
        String priceStr = price != null ? String.format("%.0f RSD", price) : "";
        String body  = "From " + from + " to " + to +
                       (priceStr.isEmpty() ? "" : ". Total: " + priceStr);
        show(context, CHANNEL_RIDES, ID_RIDE_FINISHED, title, body);
    }

    // -------------------------------------------------------------------------
    // Interna helper metoda
    // -------------------------------------------------------------------------

    private static void show(Context context, String channelId, int notifId,
                              String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.tiltaxi_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS permisija nije dodeljena
            android.util.Log.w("NotificationHelper",
                    "POST_NOTIFICATIONS permission not granted: " + e.getMessage());
        }
    }
}
