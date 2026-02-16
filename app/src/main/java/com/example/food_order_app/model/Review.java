package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private String id;
    @SerializedName("food_id")
    private String foodId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("rating")
    private int rating;
    @SerializedName("comment")
    private String comment;
    @SerializedName("created_at")
    private String createdAt;

    // Joined field
    private User user;

    public Review() {}
    public Review(String foodId, String userId, int rating, String comment) {
        this.foodId = foodId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getId() { return id; }
    public String getFoodId() { return foodId; }
    public String getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
