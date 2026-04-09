package com.example.food_order_app.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.food_order_app.R;
import com.example.food_order_app.controller.NotificationsActivity;

import java.util.Map;

public final class NotificationHelper {

    public static final String CHANNEL_ID = "order_updates";
    private static final String CHANNEL_NAME = "Order updates";
    private static final String CHANNEL_DESCRIPTION = "Order status and system updates";

    private NotificationHelper() {
        // Utility class
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        notificationManager.createNotificationChannel(channel);
    }

    public static void showPushNotification(
            Context context,
            String title,
            String message,
            Map<String, String> data
    ) {
        createNotificationChannel(context);

        String safeTitle = (title == null || title.trim().isEmpty()) ? "New notification" : title;
        String safeMessage = (message == null || message.trim().isEmpty())
                ? "You have a new update"
                : message;

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                buildOpenIntent(context, data),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(safeTitle)
                .setContentText(safeMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(safeMessage))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }

    private static Intent buildOpenIntent(Context context, Map<String, String> data) {
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (data != null) {
            String orderId = data.get("order_id");
            if (orderId != null && !orderId.trim().isEmpty()) {
                intent.putExtra("order_id", orderId);
            }
        }

        return intent;
    }
}
