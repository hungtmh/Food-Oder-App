package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model cho request đổi mật khẩu
 */
public class ChangePasswordRequest {
    @SerializedName("password")
    private String password;

    public ChangePasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
