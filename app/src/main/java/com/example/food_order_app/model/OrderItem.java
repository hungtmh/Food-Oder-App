package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("id")
    private String id;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("food_id")
    private String foodId;
    @SerializedName("food_name")
    private String foodName;
    @SerializedName("food_image")
    private String foodImage;
    @SerializedName("price")
    private double price;
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("subtotal")
    private double subtotal;

    public OrderItem() {}
    public OrderItem(String orderId, String foodId, String foodName, String foodImage,
                     double price, int quantity, double subtotal) {
        this.orderId = orderId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodImage = foodImage;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public String getFoodImage() { return foodImage; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }
}
