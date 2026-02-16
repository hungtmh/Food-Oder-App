package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminFeedbackAdapter;
import com.example.food_order_app.model.Feedback;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFeedbackActivity extends AppCompatActivity implements AdminFeedbackAdapter.OnFeedbackClickListener {

    private RecyclerView rvFeedbacks;
    private TextView tvEmpty;
    private Button btnFilterAll, btnFilterUnread, btnFilterRead;
    private BottomNavigationView bottomNav;

    private AdminFeedbackAdapter adapter;
    private SupabaseDbService dbService;
    private String currentFilter = "all"; // all, unread, read

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_feedback);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_admin_feedback);
        loadFeedbacks();
    }

    private void initViews() {
        rvFeedbacks = findViewById(R.id.rvFeedbacks);
        tvEmpty = findViewById(R.id.tvFeedbackEmpty);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUnread = findViewById(R.id.btnFilterUnread);
        btnFilterRead = findViewById(R.id.btnFilterRead);
        bottomNav = findViewById(R.id.adminBottomNav);

        adapter = new AdminFeedbackAdapter(this, this);
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
        rvFeedbacks.setAdapter(adapter);
    }

    private void setupListeners() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterUI();
            loadFeedbacks();
        });
        btnFilterUnread.setOnClickListener(v -> {
            currentFilter = "unread";
            updateFilterUI();
            loadFeedbacks();
        });
        btnFilterRead.setOnClickListener(v -> {
            currentFilter = "read";
            updateFilterUI();
            loadFeedbacks();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_food) {
                startActivity(new Intent(this, AdminHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin_feedback) {
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

    private void updateFilterUI() {
        btnFilterAll.setBackgroundResource(currentFilter.equals("all") ? R.drawable.bg_category_selected : R.drawable.bg_category_normal);
        btnFilterAll.setTextColor(getResources().getColor(currentFilter.equals("all") ? R.color.white : R.color.text_primary));
        btnFilterUnread.setBackgroundResource(currentFilter.equals("unread") ? R.drawable.bg_category_selected : R.drawable.bg_category_normal);
        btnFilterUnread.setTextColor(getResources().getColor(currentFilter.equals("unread") ? R.color.white : R.color.text_primary));
        btnFilterRead.setBackgroundResource(currentFilter.equals("read") ? R.drawable.bg_category_selected : R.drawable.bg_category_normal);
        btnFilterRead.setTextColor(getResources().getColor(currentFilter.equals("read") ? R.color.white : R.color.text_primary));
    }

    private void loadFeedbacks() {
        Callback<List<Feedback>> callback = new Callback<List<Feedback>>() {
            @Override
            public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Feedback> feedbacks = response.body();
                    adapter.setFeedbacks(feedbacks);
                    tvEmpty.setVisibility(feedbacks.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Feedback>> call, Throwable t) {
                Toast.makeText(AdminFeedbackActivity.this, "Lỗi tải phản hồi", Toast.LENGTH_SHORT).show();
            }
        };

        String select = "*,users(full_name,email,avatar_url)";
        if (currentFilter.equals("all")) {
            dbService.getAllFeedbacks(select, "created_at.desc").enqueue(callback);
        } else {
            String isRead = currentFilter.equals("read") ? "eq.true" : "eq.false";
            dbService.getFeedbacksByReadStatus(isRead, select, "created_at.desc").enqueue(callback);
        }
    }

    @Override
    public void onFeedbackClick(Feedback feedback) {
        Intent intent = new Intent(this, AdminFeedbackDetailActivity.class);
        intent.putExtra("feedback_id", feedback.getId());
        intent.putExtra("feedback_user_name", feedback.getUser() != null ?
                (feedback.getUser().getFullName() != null && !feedback.getUser().getFullName().isEmpty()
                        ? feedback.getUser().getFullName() : feedback.getUser().getEmail()) : "Khách hàng");
        intent.putExtra("feedback_user_email", feedback.getUser() != null ? feedback.getUser().getEmail() : "");
        intent.putExtra("feedback_user_avatar", feedback.getUser() != null ? feedback.getUser().getAvatarUrl() : "");
        intent.putExtra("feedback_content", feedback.getContent());
        intent.putExtra("feedback_subject", feedback.getSubject());
        intent.putExtra("feedback_rating", feedback.getRating());
        intent.putExtra("feedback_is_read", feedback.isRead());
        intent.putExtra("feedback_date", feedback.getCreatedAt());
        startActivity(intent);
    }
}
