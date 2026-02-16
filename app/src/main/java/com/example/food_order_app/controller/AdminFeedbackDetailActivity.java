package com.example.food_order_app.controller;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Feedback;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFeedbackDetailActivity extends AppCompatActivity {

    private ImageView btnBack, imgAvatar;
    private TextView tvUserName, tvEmail, tvStatus, tvDate, tvSubject, tvContent;
    private RatingBar ratingBar;
    private Button btnDelete;

    private SupabaseDbService dbService;
    private String feedbackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_feedback_detail);

        dbService = RetrofitClient.getDbService();

        initViews();
        loadData();

        // Mark as read
        feedbackId = getIntent().getStringExtra("feedback_id");
        boolean isRead = getIntent().getBooleanExtra("feedback_is_read", false);
        if (!isRead && feedbackId != null) {
            markAsRead();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackFeedback);
        imgAvatar = findViewById(R.id.imgDetailAvatar);
        tvUserName = findViewById(R.id.tvDetailUserName);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvDate = findViewById(R.id.tvDetailDate);
        tvSubject = findViewById(R.id.tvDetailSubject);
        tvContent = findViewById(R.id.tvDetailContent);
        ratingBar = findViewById(R.id.ratingDetail);
        btnDelete = findViewById(R.id.btnDeleteFeedback);

        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadData() {
        tvUserName.setText(getIntent().getStringExtra("feedback_user_name"));
        tvEmail.setText(getIntent().getStringExtra("feedback_user_email"));
        tvContent.setText(getIntent().getStringExtra("feedback_content"));
        tvSubject.setText(getIntent().getStringExtra("feedback_subject"));
        ratingBar.setRating(getIntent().getIntExtra("feedback_rating", 0));

        boolean isRead = getIntent().getBooleanExtra("feedback_is_read", false);
        tvStatus.setText(isRead ? "Đã đọc" : "Mới");
        tvStatus.setBackgroundResource(isRead ? R.drawable.bg_status_read : R.drawable.bg_status_unread);

        String avatar = getIntent().getStringExtra("feedback_user_avatar");
        Glide.with(this).load(avatar)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(imgAvatar);

        // Format date
        try {
            String dateStr = getIntent().getStringExtra("feedback_date");
            if (dateStr != null && dateStr.length() > 19) {
                dateStr = dateStr.substring(0, 19);
            }
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = input.parse(dateStr);
            tvDate.setText("Gửi lúc: " + output.format(date));
        } catch (Exception e) {
            tvDate.setText(getIntent().getStringExtra("feedback_date"));
        }
    }

    private void markAsRead() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);
        dbService.updateFeedback("eq." + feedbackId, updates).enqueue(new Callback<List<Feedback>>() {
            @Override
            public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> response) {
                if (response.isSuccessful()) {
                    tvStatus.setText("Đã đọc");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_read);
                }
            }

            @Override
            public void onFailure(Call<List<Feedback>> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phản hồi")
                .setMessage("Bạn có chắc muốn xóa phản hồi này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    dbService.deleteFeedback("eq." + feedbackId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminFeedbackDetailActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(AdminFeedbackDetailActivity.this, "Lỗi xóa", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
