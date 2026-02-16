package com.example.food_order_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý phiên đăng nhập - lưu trữ token, thông tin user vào SharedPreferences
 */
public class SessionManager {
    private static final String PREF_NAME = "FoodOrderAppSession";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_AUTH_ID = "auth_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Lưu thông tin đăng nhập
     */
    public void saveAuthToken(String accessToken, String refreshToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Lưu thông tin user profile
     */
    public void saveUserInfo(String userId, String authId, String email,
                             String fullName, String phone, String address,
                             String avatarUrl, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_AUTH_ID, authId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_AVATAR_URL, avatarUrl);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    /**
     * Cập nhật thông tin profile
     */
    public void updateProfile(String fullName, String phone, String address, String avatarUrl) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ADDRESS, address);
        if (avatarUrl != null) {
            editor.putString(KEY_AVATAR_URL, avatarUrl);
        }
        editor.apply();
    }

    /**
     * Lưu trạng thái "Ghi nhớ đăng nhập"
     */
    public void setRememberMe(boolean remember) {
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        editor.apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getBearerToken() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getAuthId() {
        return prefs.getString(KEY_AUTH_ID, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    public String getAddress() {
        return prefs.getString(KEY_ADDRESS, null);
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "user");
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getRole());
    }

    /**
     * Xóa toàn bộ session (đăng xuất)
     */
    public void logout() {
        boolean rememberMe = isRememberMe();
        editor.clear();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }
}
