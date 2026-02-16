package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Food {
    @SerializedName("id")
    private String id;
    @SerializedName("category_id")
    private String categoryId;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("price")
    private double price;
    @SerializedName("discount_percent")
    private int discountPercent;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("is_popular")
    private boolean isPopular;
    @SerializedName("is_recommended")
    private boolean isRecommended;
    @SerializedName("is_available")
    private boolean isAvailable;
    @SerializedName("avg_rating")
    private double avgRating;
    @SerializedName("total_reviews")
    private int totalReviews;
    @SerializedName("created_at")
    private String createdAt;

    public Food() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }
    public boolean isRecommended() { return isRecommended; }
    public void setRecommended(boolean recommended) { isRecommended = recommended; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public double getAvgRating() { return avgRating; }
    public int getTotalReviews() { return totalReviews; }
    public String getCreatedAt() { return createdAt; }

    public double getDiscountedPrice() {
        return price * (100 - discountPercent) / 100;
    }
}
