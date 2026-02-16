package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model: Mã reset mật khẩu
 */
public class PasswordResetCode {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("code")
    private String code;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("used")
    private boolean used;

    @SerializedName("created_at")
    private String createdAt;

    // Constructor
    public PasswordResetCode(String email, String code, String expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
