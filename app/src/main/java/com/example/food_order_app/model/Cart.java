package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Cart {
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("created_at")
    private String createdAt;

    public Cart() {}
    public Cart(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
}
