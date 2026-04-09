package com.example.food_order_app.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SupabaseFunctionsService {
    
    @POST("analyze-reviews")
    Call<Map<String, Object>> analyzeReview(@Body Map<String, Object> payload);

    @Headers("Content-Type: application/json")
    @POST("send-push")
    Call<Map<String, Object>> sendPush(@Body Map<String, Object> payload);
}
