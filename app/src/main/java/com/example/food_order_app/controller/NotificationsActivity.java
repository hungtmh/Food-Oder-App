package com.example.food_order_app.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.NotificationAdapter;
import com.example.food_order_app.model.Notification;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private TextView tvEmpty, tvMarkAllRead;
    private ImageView btnBack;

    private NotificationAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        loadNotifications();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvNotifEmpty);
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
        btnBack = findViewById(R.id.btnBackNotif);

        adapter = new NotificationAdapter(this, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        dbService.getUserNotifications("eq." + userId, "*", "created_at.desc")
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Notification> notifications = response.body();
                            adapter.setNotifications(notifications);
                            tvEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        Toast.makeText(NotificationsActivity.this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            markAsRead(notification);
        }
    }

    private void markAsRead(Notification notification) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);

        dbService.markNotificationRead("eq." + notification.getId(), updates)
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful()) {
                            notification.setRead(true);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        // Silent fail
                    }
                });
    }

    private void markAllAsRead() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        // Mark all unread notifications as read using a batch update
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);

        dbService.markAllNotificationsRead("eq." + userId, "eq.false", updates)
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(NotificationsActivity.this, "Đã đọc tất cả", Toast.LENGTH_SHORT).show();
                            loadNotifications();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        Toast.makeText(NotificationsActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
