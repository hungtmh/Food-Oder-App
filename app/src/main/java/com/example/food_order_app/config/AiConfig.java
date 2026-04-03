package com.example.food_order_app.config;

import com.example.food_order_app.BuildConfig;

/**
 * Cấu hình AI tokens.
 * Dữ liệu bảo mật được lấy tự động từ local.properties thông qua BuildConfig.
 */
public class AiConfig {
    public static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    public static final String HF_TOKEN = BuildConfig.HF_TOKEN;
}
