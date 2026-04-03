package com.example.food_order_app.utils;

import android.content.Intent;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.food_order_app.R;
import com.example.food_order_app.controller.AIDashboardActivity;
import com.example.food_order_app.controller.AdminCategoryActivity;
import com.example.food_order_app.controller.AdminChatListActivity;
import com.example.food_order_app.controller.AdminHomeActivity;
import com.example.food_order_app.controller.AdminOrdersActivity;
import com.example.food_order_app.controller.AdminRevenueActivity;
import com.example.food_order_app.controller.AdminSendNotificationActivity;
import com.example.food_order_app.controller.ProfileActivity;
import com.google.android.material.navigation.NavigationView;

public final class AdminDrawerHelper {

    private AdminDrawerHelper() {
    }

    public static void setupDrawer(
            AppCompatActivity activity,
            DrawerLayout drawerLayout,
            NavigationView navigationView,
            View menuButton,
            @IdRes int selectedItemId) {
        if (drawerLayout == null || navigationView == null) {
            return;
        }

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (selectedItemId != 0) {
            navigationView.setCheckedItem(selectedItemId);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> targetActivity = resolveTarget(itemId);

            drawerLayout.closeDrawer(GravityCompat.START);

            if (targetActivity != null && !activity.getClass().equals(targetActivity)) {
                Intent intent = new Intent(activity, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private static Class<?> resolveTarget(@IdRes int itemId) {
        if (itemId == R.id.navAdminFood) {
            return AdminHomeActivity.class;
        }
        if (itemId == R.id.navAdminCategory) {
            return AdminCategoryActivity.class;
        }
        if (itemId == R.id.navAdminRevenue) {
            return AdminRevenueActivity.class;
        }
        if (itemId == R.id.navAdminChat) {
            return AdminChatListActivity.class;
        }
        if (itemId == R.id.navAdminOrders) {
            return AdminOrdersActivity.class;
        }
        if (itemId == R.id.navAdminProfile) {
            return ProfileActivity.class;
        }
        if (itemId == R.id.navAiInsights) {
            return AIDashboardActivity.class;
        }
        if (itemId == R.id.navSendNotification) {
            return AdminSendNotificationActivity.class;
        }
        return null;
    }
}
