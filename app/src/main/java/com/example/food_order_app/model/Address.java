package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("phone")
    private String phone;
    @SerializedName("address")
    private String address;
    @SerializedName("is_default")
    private boolean isDefault;

    public Address() {}
    public Address(String userId, String receiverName, String phone, String address, boolean isDefault) {
        this.userId = userId;
        this.receiverName = receiverName;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}
