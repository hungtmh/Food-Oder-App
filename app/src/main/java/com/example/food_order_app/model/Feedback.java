package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

public class Feedback {
    @SerializedName("id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("subject")
    private String subject;
    @SerializedName("content")
    private String content;
    @SerializedName("rating")
    private int rating;
    @SerializedName("is_read")
    private boolean isRead;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    // Joined field
    @SerializedName("users")
    private User user;

    public Feedback() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
