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

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDiscountType() { return discountType; }
    public double getDiscountValue() { return discountValue; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public double getMinOrderValue() { return minOrderValue; }
}
