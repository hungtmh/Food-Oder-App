package com.example.food_order_app.config;

/**
 * Cấu hình Supabase - chứa URL và API Key
 * TODO: Thay thế bằng thông tin Supabase project thực tế của bạn
 */
public class SupabaseConfig {
    // Thay YOUR_SUPABASE_URL bằng URL project Supabase của bạn
    // Ví dụ: "https://abcdefghijk.supabase.co"
    public static final String SUPABASE_URL = "https://qqmqucmebzvcwcgjnbph.supabase.co";

    // Thay YOUR_ANON_KEY bằng anon/public key của Supabase project
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFxbXF1Y21lYnp2Y3djZ2puYnBoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzExMzQxNjMsImV4cCI6MjA4NjcxMDE2M30.7CjVxCwuTzi4LQL7Q_G9Z2VD_0x6-f6R7Z3cJlSoZBA";

    // REST API base URL
    public static final String REST_URL = SUPABASE_URL + "/rest/v1/";

    // Auth API base URL
    public static final String AUTH_URL = SUPABASE_URL + "/auth/v1/";
}
