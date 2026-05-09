package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class AiRecommendationTask {
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("status")
    private String status;
    @SerializedName("budget")
    private Double budget;
    @SerializedName("people_count")
    private Integer peopleCount;
    @SerializedName("taste")
    private String taste;
    @SerializedName("description")
    private String description;
    @SerializedName("result")
    private AiRecommendationResult result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    public AiRecommendationTask() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }
    public String getTaste() { return taste; }
    public void setTaste(String taste) { this.taste = taste; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AiRecommendationResult getResult() { return result; }
    public void setResult(AiRecommendationResult result) { this.result = result; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static class AiRecommendationResult {
        @SerializedName("rationale")
        private String rationale;
        @SerializedName("foods")
        private java.util.List<AiRecommendedFood> foods;

        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
        public java.util.List<AiRecommendedFood> getFoods() { return foods; }
        public void setFoods(java.util.List<AiRecommendedFood> foods) { this.foods = foods; }
    }

    public static class AiRecommendedFood {
        @SerializedName("food_id")
        private String foodId;
        @SerializedName("food_name")
        private String foodName;
        @SerializedName("price")
        private Double price;
        @SerializedName("reason")
        private String reason;

        public String getFoodId() { return foodId; }
        public void setFoodId(String foodId) { this.foodId = foodId; }
        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
