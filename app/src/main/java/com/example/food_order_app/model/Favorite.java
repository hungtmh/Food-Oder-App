package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Favorite {
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("food_id")
    private String foodId;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("foods")
    private Food food;

    public Favorite() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public String getCreatedAt() { return createdAt; }
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
}
