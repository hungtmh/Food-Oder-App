package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model cho request quên mật khẩu (gửi email reset)
 */
public class ForgotPasswordRequest {
    @SerializedName("email")
    private String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
