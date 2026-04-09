package com.example.food_order_app.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.food_order_app.utils.NotificationHelper;
import com.example.food_order_app.utils.PushRegistrationManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token received");
        PushRegistrationManager.syncToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        Map<String, String> data = remoteMessage.getData();
        if ((title == null || title.trim().isEmpty()) && data != null) {
            title = data.get("title");
        }
        if ((body == null || body.trim().isEmpty()) && data != null) {
            body = data.get("body");
        }

        NotificationHelper.showPushNotification(getApplicationContext(), title, body, data);
    }
}
