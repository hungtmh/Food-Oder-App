package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("icon_url")
    private String iconUrl;
    @SerializedName("sort_order")
    private int sortOrder;
    @SerializedName("is_active")
    private boolean isActive;

    public Category() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return isActive; }
}
