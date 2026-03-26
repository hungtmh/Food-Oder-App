package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatMessage implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("room_user_id")
    private String roomUserId;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("message")
    private String message;

    @SerializedName("created_at")
    private String createdAt;

    public ChatMessage() {
    }

    public ChatMessage(String roomUserId, String senderId, String message) {
        this.roomUserId = roomUserId;
        this.senderId = senderId;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomUserId() {
        return roomUserId;
    }

    public void setRoomUserId(String roomUserId) {
        this.roomUserId = roomUserId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
