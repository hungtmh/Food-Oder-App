package com.example.food_order_app.utils;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.food_order_app.R;
import com.example.food_order_app.controller.AdminFeedbackActivity;
import com.example.food_order_app.controller.AdminHomeActivity;
import com.example.food_order_app.controller.AdminOrdersActivity;
import com.example.food_order_app.controller.AdminReportStatisticsActivity;
import com.example.food_order_app.controller.AdminRevenueActivity;
import com.example.food_order_app.controller.ProfileActivity;

public final class AdminBottomNavHelper {

    public static final int TAB_FOOD = R.id.navAdminFood;
    public static final int TAB_REVENUE = R.id.navAdminRevenue;
    public static final int TAB_REPORT = R.id.navAdminReport;
    public static final int TAB_FEEDBACK = R.id.navAdminFeedback;
    public static final int TAB_ORDERS = R.id.navAdminOrders;
    public static final int TAB_PROFILE = R.id.navAdminProfile;

    private AdminBottomNavHelper() {
    }

    public static void setup(AppCompatActivity activity, @IdRes int selectedTabId) {
        bindItem(activity, TAB_FOOD, R.id.navAdminFoodIconWrap, R.id.navAdminFoodIcon, R.id.navAdminFoodLabel,
                selectedTabId, AdminHomeActivity.class);
        bindItem(activity, TAB_REVENUE, R.id.navAdminRevenueIconWrap, R.id.navAdminRevenueIcon, R.id.navAdminRevenueLabel,
                selectedTabId, AdminRevenueActivity.class);
        bindItem(activity, TAB_REPORT, R.id.navAdminReportIconWrap, R.id.navAdminReportIcon, R.id.navAdminReportLabel,
                selectedTabId, AdminReportStatisticsActivity.class);
        bindItem(activity, TAB_FEEDBACK, R.id.navAdminFeedbackIconWrap, R.id.navAdminFeedbackIcon, R.id.navAdminFeedbackLabel,
                selectedTabId, AdminFeedbackActivity.class);
        bindItem(activity, TAB_ORDERS, R.id.navAdminOrdersIconWrap, R.id.navAdminOrdersIcon, R.id.navAdminOrdersLabel,
                selectedTabId, AdminOrdersActivity.class);
        bindItem(activity, TAB_PROFILE, R.id.navAdminProfileIconWrap, R.id.navAdminProfileIcon, R.id.navAdminProfileLabel,
                selectedTabId, ProfileActivity.class);
    }

    private static void bindItem(
            AppCompatActivity activity,
            @IdRes int tabRootId,
            @IdRes int iconWrapId,
            @IdRes int iconId,
            @IdRes int labelId,
            @IdRes int selectedTabId,
            Class<?> targetActivity
    ) {
        View tabRoot = activity.findViewById(tabRootId);
        View iconWrap = activity.findViewById(iconWrapId);
        ImageView icon = activity.findViewById(iconId);
        TextView label = activity.findViewById(labelId);

        if (tabRoot == null || iconWrap == null || icon == null || label == null) return;

        boolean selected = tabRootId == selectedTabId;
        int selectedColor = ContextCompat.getColor(activity, R.color.primary);
        int normalColor = ContextCompat.getColor(activity, R.color.text_hint);

        iconWrap.setBackgroundResource(selected ? R.drawable.bg_admin_nav_selected : android.R.color.transparent);
        icon.setColorFilter(selected ? selectedColor : normalColor);
        label.setTextColor(selected ? selectedColor : normalColor);

        tabRoot.setOnClickListener(v -> {
            if (selected || activity.getClass().equals(targetActivity)) return;
            Intent intent = new Intent(activity, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
        });
    }
}
