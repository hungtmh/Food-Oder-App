package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Voucher {
    @SerializedName("id")
    private String id;

    @SerializedName("code")
    private String code;

    @SerializedName("title")
    private String title;
    
    @SerializedName("discount_type")
    private String discountType; // "percent", "fixed_amount"

    @SerializedName("discount_value")
    private double discountValue;

    @SerializedName("max_discount_amount")
    private Double maxDiscountAmount;

    @SerializedName("min_order_value")
    private double minOrderValue;

    @SerializedName("description")
    private String description;

    @SerializedName("usage_limit")
    private Integer usageLimit;

    @SerializedName("used_count")
    private int usedCount;

    @SerializedName("limit_per_user")
    private Integer limitPerUser;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("is_public")
    private Boolean isPublic;

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDiscountType() { return discountType; }
    public double getDiscountValue() { return discountValue; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public double getMinOrderValue() { return minOrderValue; }
    public String getDescription() { return description; }
    public Integer getUsageLimit() { return usageLimit; }
    public int getUsedCount() { return usedCount; }
    public Integer getLimitPerUser() { return limitPerUser; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }
    public Boolean getIsPublic() { return isPublic; }
}
