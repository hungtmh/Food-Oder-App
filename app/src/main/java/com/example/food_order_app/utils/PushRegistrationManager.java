package com.example.food_order_app.utils;

import android.content.Context;
import android.util.Log;

import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PushRegistrationManager {

    private static final String TAG = "PushRegistration";

    private PushRegistrationManager() {
        // Utility class
    }

    public static void requestCurrentTokenAndSync(Context context) {
        final Context appContext = context.getApplicationContext();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Failed to get FCM token", task.getException());
                return;
            }

            String token = task.getResult();
            if (token == null || token.trim().isEmpty()) {
                Log.w(TAG, "FCM returned empty token");
                return;
            }

            Log.d(TAG, "FCM token obtained: " + maskToken(token));
            syncToken(appContext, token);
        });
    }

    public static void syncToken(Context context, String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Log.d(TAG, "Skip token sync because user is not logged in. token=" + maskToken(token));
            return;
        }

        Log.d(TAG, "Syncing token for userId=" + userId + " token=" + maskToken(token));

        SupabaseDbService dbService = RetrofitClient.getDbService();
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("fcm_token", token);
        payload.put("platform", "android");
        payload.put("is_active", true);
        payload.put("last_seen_at", nowIsoString());

        dbService.upsertDeviceToken("resolution=merge-duplicates,return=representation", "fcm_token", payload)
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(
                            Call<List<Map<String, Object>>> call,
                            Response<List<Map<String, Object>>> response
                    ) {
                        if (!response.isSuccessful()) {
                            Log.w(TAG, "Token upsert failed with HTTP " + response.code()
                                    + " body=" + readErrorBody(response));
                            return;
                        }
                        Log.d(TAG, "FCM token synced successfully");
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        Log.w(TAG, "Token upsert failed", t);
                    }
                });
    }

    private static String nowIsoString() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(new Date());
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }

    private static String readErrorBody(Response<List<Map<String, Object>>> response) {
        try {
            if (response.errorBody() == null) {
                return "null";
            }
            return response.errorBody().string();
        } catch (IOException e) {
            return "<unreadable error body>";
        }
    }
}
