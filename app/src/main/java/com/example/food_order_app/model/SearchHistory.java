package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class SearchHistory {
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("keyword")
    private String keyword;
    @SerializedName("created_at")
    private String createdAt;

    public SearchHistory() {}
    public SearchHistory(String userId, String keyword) {
        this.userId = userId;
        this.keyword = keyword;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getKeyword() { return keyword; }
    public String getCreatedAt() { return createdAt; }
}
