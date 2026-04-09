package com.example.food_order_app.model;

public class CategoryProductStat {
    private final String productName;
    private final boolean active;
    private final int soldQuantity;
    private final double revenue;
    private final Integer remainingQuantity;

    public CategoryProductStat(String productName, boolean active, int soldQuantity, double revenue, Integer remainingQuantity) {
        this.productName = productName;
        this.active = active;
        this.soldQuantity = soldQuantity;
        this.revenue = revenue;
        this.remainingQuantity = remainingQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public boolean isActive() {
        return active;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public double getRevenue() {
        return revenue;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }
}
