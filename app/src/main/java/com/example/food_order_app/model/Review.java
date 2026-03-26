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
    @SerializedName("title")
    private String title;
    @SerializedName("comment")
    private String comment;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("sentiment")
    private String sentiment; // positive, negative, neutral
    @SerializedName("sentiment_score")
    private double sentimentScore;

    // Joined field (Supabase returns with table name "users")
    @SerializedName("users")
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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getComment() { return comment; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }
}
