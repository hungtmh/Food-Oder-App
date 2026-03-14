package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminReportStatisticsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Button btnOpenRevenueDashboard;
    private Button btnOpenRevenueReport;
    private Button btnOpenCustomerStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report_statistics);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_admin_revenue);
    }

    private void initViews() {
        bottomNav = findViewById(R.id.adminBottomNav);
        btnOpenRevenueDashboard = findViewById(R.id.btnOpenRevenueDashboard);
        btnOpenRevenueReport = findViewById(R.id.btnOpenRevenueReport);
        btnOpenCustomerStats = findViewById(R.id.btnOpenCustomerStats);
    }

    private void setupListeners() {
        btnOpenRevenueDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminRevenueActivity.class)));
        btnOpenRevenueReport.setOnClickListener(v -> startActivity(new Intent(this, AdminRevenueActivity.class)));
        btnOpenCustomerStats.setOnClickListener(v -> startActivity(new Intent(this, AdminRevenueActivity.class)));

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_food) {
                startActivity(new Intent(this, AdminHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin_revenue) {
                return true;
            } else if (id == R.id.nav_admin_feedback) {
                startActivity(new Intent(this, AdminFeedbackActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin_orders) {
                startActivity(new Intent(this, AdminOrdersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}
