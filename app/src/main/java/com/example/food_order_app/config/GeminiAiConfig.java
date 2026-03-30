package com.example.food_order_app.config;

import com.example.food_order_app.BuildConfig;

/**
 * Cấu hình Gemini AI API Key
 * Dữ liệu bảo mật được lấy tự động từ local.properties thông qua BuildConfig
 */
public class GeminiAiConfig {
    public static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
}
