package com.example.food_order_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.controller.AdminHomeActivity;
import com.example.food_order_app.controller.HomeActivity;
import com.example.food_order_app.controller.LoginActivity;
import com.example.food_order_app.utils.SessionManager;

/**
 * MainActivity - Splash screen / Router
 * Kiểm tra trạng thái đăng nhập và chuyển hướng phù hợp
 */
public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(this);

            Intent intent;
            if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
                // Đã đăng nhập và ghi nhớ -> vào trang chính
                if (sessionManager.isAdmin()) {
                    intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, HomeActivity.class);
                }
            } else {
                // Chưa đăng nhập -> vào trang đăng nhập
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}