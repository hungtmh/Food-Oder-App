package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("id")
    private String id;
    @SerializedName("cart_id")
    private String cartId;
    @SerializedName("food_id")
    private String foodId;
    @SerializedName("quantity")
    private int quantity;

    // Joined food data (from select=*,foods(*))
    @SerializedName("foods")
    private Food food;

    public CartItem() {}
    public CartItem(String cartId, String foodId, int quantity) {
        this.cartId = cartId;
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCartId() { return cartId; }
    public String getFoodId() { return foodId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }

    public double getSubtotal() {
        if (food == null) return 0;
        return food.getDiscountedPrice() * quantity;
    }
}
