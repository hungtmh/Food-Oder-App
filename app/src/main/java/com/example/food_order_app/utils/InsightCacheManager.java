package com.example.food_order_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class InsightCacheManager {

    private static final String PREF_NAME = "ai_insight_cache";
    private static final String KEY_INSIGHTS = "insights_json";

    private InsightCacheManager() {
    }

    public static Map<String, String> loadInsights(Context context) {
        Map<String, String> result = new HashMap<>();
        if (context == null) {
            return result;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_INSIGHTS, "{}");

        try {
            JSONObject object = new JSONObject(json);
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = object.optString(key, "");
                if (value != null && !value.trim().isEmpty()) {
                    result.put(key, value);
                }
            }
        } catch (JSONException ignored) {
            // Return empty map when stored JSON is invalid.
        }

        return result;
    }

    public static void saveInsight(Context context, String foodId, String insight) {
        if (context == null || foodId == null || foodId.trim().isEmpty() || insight == null
                || insight.trim().isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_INSIGHTS, "{}");

        try {
            JSONObject object = new JSONObject(json);
            object.put(foodId, insight.trim());
            prefs.edit().putString(KEY_INSIGHTS, object.toString()).apply();
        } catch (JSONException ignored) {
            // Ignore if unable to serialize.
        }
    }
}
