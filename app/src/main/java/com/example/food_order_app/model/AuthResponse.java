package com.example.food_order_app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model cho response từ Supabase Auth
 */
public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private int expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("user")
    private AuthUser user;

    // Error fields
    @SerializedName("error")
    private String error;

    @SerializedName("error_description")
    private String errorDescription;

    @SerializedName("msg")
    private String msg;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public int getExpiresIn() { return expiresIn; }
    public String getRefreshToken() { return refreshToken; }
    public AuthUser getUser() { return user; }
    public String getError() { return error; }
    public String getErrorDescription() { return errorDescription; }
    public String getMsg() { return msg; }

    public boolean isSuccess() {
        return accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Inner class đại diện cho user trong Auth response
     */
    public static class AuthUser {
        @SerializedName("id")
        private String id;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("created_at")
        private String createdAt;

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getCreatedAt() { return createdAt; }
    }
}
