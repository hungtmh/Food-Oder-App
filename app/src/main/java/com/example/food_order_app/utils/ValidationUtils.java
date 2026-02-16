package com.example.food_order_app.utils;

import java.util.regex.Pattern;

/**
 * Utility class cho validation dữ liệu đầu vào
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0[3|5|7|8|9])+([0-9]{8})$"
    );

    /**
     * Kiểm tra email hợp lệ
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Kiểm tra mật khẩu hợp lệ (ít nhất 6 ký tự)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Kiểm tra số điện thoại VN hợp lệ
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Kiểm tra chuỗi rỗng
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Kiểm tra tên hợp lệ (ít nhất 2 ký tự)
     */
    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }
}
