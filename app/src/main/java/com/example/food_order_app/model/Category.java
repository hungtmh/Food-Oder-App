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
    @SerializedName("is_deleted")
    private boolean isDeleted;
    @SerializedName("name_en")
    private String nameEn;
    @SerializedName("name_ja")
    private String nameJa;

    // Client-side computed fields for admin dashboard (must not be sent to API payload).
    private transient int totalFoods;
    private transient int activeFoods;
    private transient double revenueLast7Days;

    public Category() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameJa() { return nameJa; }
    public void setNameJa(String nameJa) { this.nameJa = nameJa; }

    public int getTotalFoods() { return totalFoods; }
    public void setTotalFoods(int totalFoods) { this.totalFoods = totalFoods; }
    public int getActiveFoods() { return activeFoods; }
    public void setActiveFoods(int activeFoods) { this.activeFoods = activeFoods; }
    public double getRevenueLast7Days() { return revenueLast7Days; }
    public void setRevenueLast7Days(double revenueLast7Days) { this.revenueLast7Days = revenueLast7Days; }
}
