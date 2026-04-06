package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class UserVoucherUsage {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("voucher_id")
    private String voucherId;

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("used_at")
    private String usedAt;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUsedAt() {
        return usedAt;
    }
}
