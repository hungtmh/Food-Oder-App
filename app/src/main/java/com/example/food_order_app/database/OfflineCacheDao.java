package com.example.food_order_app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface OfflineCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCache(OfflineCache cache);

    @Query("SELECT data FROM offline_cache WHERE key = :key LIMIT 1")
    String getCacheData(String key);
    
    @Query("SELECT * FROM offline_cache WHERE key = :key LIMIT 1")
    OfflineCache getCacheObject(String key);

    @Query("DELETE FROM offline_cache WHERE key = :key")
    void deleteCache(String key);

    @Query("DELETE FROM offline_cache")
    void clearAll();
}
