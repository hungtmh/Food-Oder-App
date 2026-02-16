package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model cho request đăng ký / đăng nhập qua Supabase Auth
 */
public class AuthRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
