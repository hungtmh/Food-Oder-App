package com.example.food_order_app.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "recent_views", primaryKeys = {"user_id", "food_id"})
public class RecentView {
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "food_id")
    private String foodId;

    @ColumnInfo(name = "viewed_at")
    private long viewedAt;

    public RecentView(@NonNull String userId, @NonNull String foodId, long viewedAt) {
        this.userId = userId;
        this.foodId = foodId;
        this.viewedAt = viewedAt;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    public long getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(long viewedAt) {
        this.viewedAt = viewedAt;
    }
}
