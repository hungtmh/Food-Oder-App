package com.example.food_order_app.config;

import com.example.food_order_app.BuildConfig;

/**
 * Cấu hình Supabase - chứa URL và API Key
 * Dữ liệu bảo mật được lấy tự động từ local.properties thông qua BuildConfig
 */
public class SupabaseConfig {
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    // REST API base URL
    public static final String REST_URL = SUPABASE_URL + "/rest/v1/";

    // Auth API base URL
    public static final String AUTH_URL = SUPABASE_URL + "/auth/v1/";

    // Functions API base URL
    public static final String FUNCTIONS_URL = SUPABASE_URL + "/functions/v1/";

    // Storage API base URL
    public static final String STORAGE_URL = SUPABASE_URL + "/storage/v1/";
}
