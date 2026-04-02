package com.example.food_order_app.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SupabaseEdgeService {
    @POST("create-sepay-payment")
    Call<Map<String, Object>> createSepayPayment(@Body Map<String, Object> payload);
}
