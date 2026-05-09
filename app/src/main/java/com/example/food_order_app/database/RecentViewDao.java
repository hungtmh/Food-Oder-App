package com.example.food_order_app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecentViewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(RecentView recentView);

    @Query("SELECT food_id FROM recent_views WHERE user_id = :userId ORDER BY viewed_at DESC LIMIT :limit")
    List<String> getRecentFoodIds(String userId, int limit);

    @Query("DELETE FROM recent_views WHERE user_id = :userId AND food_id NOT IN (SELECT food_id FROM recent_views WHERE user_id = :userId ORDER BY viewed_at DESC LIMIT :limit)")
    void trimToLimit(String userId, int limit);

    @Query("DELETE FROM recent_views WHERE user_id = :userId")
    void clearByUser(String userId);
}
