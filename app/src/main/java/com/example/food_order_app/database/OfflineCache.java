package com.example.food_order_app.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_cache")
public class OfflineCache {
    @PrimaryKey
    @NonNull
    private String key;
    private String data;
    private long timestamp;

    public OfflineCache(@NonNull String key, String data, long timestamp) {
        this.key = key;
        this.data = data;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
