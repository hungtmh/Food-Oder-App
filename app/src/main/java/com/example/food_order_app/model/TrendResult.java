package com.example.food_order_app.model;

public class TrendResult {
    public int totalScore;
    public String trendType;
    public double confidenceScore;

    public TrendResult(int totalScore, String trendType, double confidenceScore) {
        this.totalScore = totalScore;
        this.trendType = trendType;
        this.confidenceScore = confidenceScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public String getTrendType() {
        return trendType;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }
}