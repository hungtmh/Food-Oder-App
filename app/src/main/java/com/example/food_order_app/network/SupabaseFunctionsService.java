package com.example.food_order_app.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Service API cho Supabase Edge Functions
 */
public interface SupabaseFunctionsService {

    /**
     * Gọi function analyze-reviews để phân tích cảm xúc bằng Gemini
     */
    @POST("analyze-reviews")
    Call<Map<String, Object>> analyzeReview(@Body Map<String, Object> payload);
}
