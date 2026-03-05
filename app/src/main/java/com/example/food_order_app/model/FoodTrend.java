package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class FoodTrend {
    @SerializedName("id")
    private String id;

    @SerializedName("food_id")
    private String foodId;

    @SerializedName("trend_type")
    private String trendType; // hot_seller, declining, at_risk, stable

    @SerializedName("confidence_score")
    private double confidenceScore;

    @SerializedName("prediction_period")
    private String predictionPeriod;

    @SerializedName("sales_trend")
    private double salesTrend;

    @SerializedName("sentiment_trend")
    private double sentimentTrend;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    // Joined fields
    @SerializedName("foods")
    private Food food;

    public FoodTrend() {}

    public FoodTrend(String foodId, String trendType, double confidenceScore, 
                     double salesTrend, double sentimentTrend) {
        this.foodId = foodId;
        this.trendType = trendType;
        this.confidenceScore = confidenceScore;
        this.salesTrend = salesTrend;
        this.sentimentTrend = sentimentTrend;
        this.predictionPeriod = "30_days";
    }

    // Getters
    public String getId() { return id; }
    public String getFoodId() { return foodId; }
    public String getTrendType() { return trendType; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getPredictionPeriod() { return predictionPeriod; }
    public double getSalesTrend() { return salesTrend; }
    public double getSentimentTrend() { return sentimentTrend; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
    public Food getFood() { return food; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public void setTrendType(String trendType) { this.trendType = trendType; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public void setPredictionPeriod(String predictionPeriod) { this.predictionPeriod = predictionPeriod; }
    public void setSalesTrend(double salesTrend) { this.salesTrend = salesTrend; }
    public void setSentimentTrend(double sentimentTrend) { this.sentimentTrend = sentimentTrend; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setFood(Food food) { this.food = food; }

    // Helper methods
    public String getTrendTypeName() {
        switch (trendType) {
            case "hot_seller": return "Hot Seller 🔥";
            case "declining": return "Declining 📉";
            case "at_risk": return "At Risk ⚠️";
            case "stable": return "Stable ➡️";
            default: return "Unknown";
        }
    }

    public int getTrendColor() {
        switch (trendType) {
            case "hot_seller": return 0xFFFF5722; // Deep Orange
            case "declining": return 0xFFFF9800; // Orange
            case "at_risk": return 0xFFF44336; // Red
            case "stable": return 0xFF4CAF50; // Green
            default: return 0xFF9E9E9E; // Grey
        }
    }
}
