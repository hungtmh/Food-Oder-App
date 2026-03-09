package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class FoodSentimentStats {
    @SerializedName("food_id")
    private String foodId;

    @SerializedName("food_name")
    private String foodName;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("total_reviews")
    private int totalReviews;

    @SerializedName("positive_count")
    private int positiveCount;

    @SerializedName("negative_count")
    private int negativeCount;

    @SerializedName("neutral_count")
    private int neutralCount;

    @SerializedName("positive_percent")
    private double positivePercent;

    @SerializedName("negative_percent")
    private double negativePercent;

    @SerializedName("neutral_percent")
    private double neutralPercent;

    @SerializedName("avg_sentiment_score")
    private double avgSentimentScore;

    @SerializedName("avg_rating")
    private double avgRating;

    public FoodSentimentStats() {}

    // Getters
    public String getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public String getImageUrl() { return imageUrl; }
    public int getTotalReviews() { return totalReviews; }
    public int getPositiveCount() { return positiveCount; }
    public int getNegativeCount() { return negativeCount; }
    public int getNeutralCount() { return neutralCount; }
    public double getPositivePercent() { return positivePercent; }
    public double getNegativePercent() { return negativePercent; }
    public double getNeutralPercent() { return neutralPercent; }
    public double getAvgSentimentScore() { return avgSentimentScore; }
    public double getAvgRating() { return avgRating; }

    // Setters
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
    public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; }
    public void setNegativeCount(int negativeCount) { this.negativeCount = negativeCount; }
    public void setNeutralCount(int neutralCount) { this.neutralCount = neutralCount; }
    public void setPositivePercent(double positivePercent) { this.positivePercent = positivePercent; }
    public void setNegativePercent(double negativePercent) { this.negativePercent = negativePercent; }
    public void setNeutralPercent(double neutralPercent) { this.neutralPercent = neutralPercent; }
    public void setAvgSentimentScore(double avgSentimentScore) { this.avgSentimentScore = avgSentimentScore; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }

    // Helper method to get dominant sentiment
    public String getDominantSentiment() {
        if (positiveCount >= negativeCount && positiveCount >= neutralCount) {
            return "positive";
        } else if (negativeCount >= positiveCount && negativeCount >= neutralCount) {
            return "negative";
        } else {
            return "neutral";
        }
    }
}
