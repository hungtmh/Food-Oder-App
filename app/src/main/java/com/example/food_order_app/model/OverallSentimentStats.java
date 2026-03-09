package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class OverallSentimentStats {
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

    public OverallSentimentStats() {}

    // Getters
    public int getTotalReviews() { return totalReviews; }
    public int getPositiveCount() { return positiveCount; }
    public int getNegativeCount() { return negativeCount; }
    public int getNeutralCount() { return neutralCount; }
    public double getPositivePercent() { return positivePercent; }
    public double getNegativePercent() { return negativePercent; }
    public double getNeutralPercent() { return neutralPercent; }
    public double getAvgSentimentScore() { return avgSentimentScore; }

    // Setters
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
    public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; }
    public void setNegativeCount(int negativeCount) { this.negativeCount = negativeCount; }
    public void setNeutralCount(int neutralCount) { this.neutralCount = neutralCount; }
    public void setPositivePercent(double positivePercent) { this.positivePercent = positivePercent; }
    public void setNegativePercent(double negativePercent) { this.negativePercent = negativePercent; }
    public void setNeutralPercent(double neutralPercent) { this.neutralPercent = neutralPercent; }
    public void setAvgSentimentScore(double avgSentimentScore) { this.avgSentimentScore = avgSentimentScore; }
}
