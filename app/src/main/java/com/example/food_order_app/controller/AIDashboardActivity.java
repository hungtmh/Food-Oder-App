package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.SentimentStatsAdapter;
import com.example.food_order_app.model.FoodSentimentStats;
import com.example.food_order_app.model.OverallSentimentStats;
import com.example.food_order_app.model.Review;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.network.SupabaseFunctionsService;
import com.example.food_order_app.utils.AdminDrawerHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvTotalReviews, tvPositivePercent, tvPositiveCount;
    private TextView tvNeutralPercent, tvNeutralCount, tvNegativePercent, tvNegativeCount;
    private Button btnAnalyzeAllReviews;
    private MaterialCardView cardViewSentiment, cardPredictTrends;
    private RecyclerView rvTopPositive, rvTopNegative;
    private TextView tvTopPositiveEmpty, tvTopNegativeEmpty;
    private ImageView btnBack;

    private SupabaseDbService dbService;
    private SupabaseFunctionsService functionsService;
    private SentimentStatsAdapter topPositiveAdapter, topNegativeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_dashboard);

        dbService = RetrofitClient.getDbService();
        functionsService = RetrofitClient.getFunctionsService();
        initViews();
        setupListeners();
        loadOverallSentimentStats();
        loadTopFoods();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
        btnBack = findViewById(R.id.btnBack);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        tvPositivePercent = findViewById(R.id.tvPositivePercent);
        tvPositiveCount = findViewById(R.id.tvPositiveCount);
        tvNeutralPercent = findViewById(R.id.tvNeutralPercent);
        tvNeutralCount = findViewById(R.id.tvNeutralCount);
        tvNegativePercent = findViewById(R.id.tvNegativePercent);
        tvNegativeCount = findViewById(R.id.tvNegativeCount);
        btnAnalyzeAllReviews = findViewById(R.id.btnAnalyzeAllReviews);
        cardViewSentiment = findViewById(R.id.cardViewSentiment);
        cardPredictTrends = findViewById(R.id.cardPredictTrends);
        rvTopPositive = findViewById(R.id.rvTopPositive);
        rvTopNegative = findViewById(R.id.rvTopNegative);
        tvTopPositiveEmpty = findViewById(R.id.tvTopPositiveEmpty);
        tvTopNegativeEmpty = findViewById(R.id.tvTopNegativeEmpty);

        // Setup RecyclerViews
        topPositiveAdapter = new SentimentStatsAdapter(this, stat -> {
            // Navigate to food detail or sentiment detail
            Toast.makeText(this, "Xem chi tiết: " + stat.getFoodName(), Toast.LENGTH_SHORT).show();
        });
        rvTopPositive.setLayoutManager(new LinearLayoutManager(this));
        rvTopPositive.setAdapter(topPositiveAdapter);

        topNegativeAdapter = new SentimentStatsAdapter(this, stat -> {
            Toast.makeText(this, "Xem chi tiết: " + stat.getFoodName(), Toast.LENGTH_SHORT).show();
        });
        rvTopNegative.setLayoutManager(new LinearLayoutManager(this));
        rvTopNegative.setAdapter(topNegativeAdapter);

        AdminDrawerHelper.setupDrawer(this, drawerLayout, navigationView, btnBack, R.id.navAiInsights);
    }

    private void setupListeners() {
        btnAnalyzeAllReviews.setOnClickListener(v -> analyzeAllReviews());

        cardViewSentiment.setOnClickListener(v -> {
            startActivity(new Intent(this, SentimentStatisticsActivity.class));
        });

        cardPredictTrends.setOnClickListener(v -> {
            startActivity(new Intent(this, FoodTrendPredictionActivity.class));
        });
    }

    private void loadOverallSentimentStats() {
        dbService.getOverallSentimentStats().enqueue(new Callback<List<OverallSentimentStats>>() {
            @Override
            public void onResponse(Call<List<OverallSentimentStats>> call,
                    Response<List<OverallSentimentStats>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    OverallSentimentStats stats = response.body().get(0);
                    updateOverallStats(stats);
                } else {
                    resetOverallStats();
                    Toast.makeText(AIDashboardActivity.this,
                            "Chưa có dữ liệu cảm xúc hoặc không thể tải thống kê.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OverallSentimentStats>> call, Throwable t) {
                resetOverallStats();
                Toast.makeText(AIDashboardActivity.this,
                        "Lỗi tải dữ liệu: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetOverallStats() {
        tvTotalReviews.setText("Tổng đánh giá: 0");
        tvPositivePercent.setText("0%");
        tvPositiveCount.setText("(0)");
        tvNeutralPercent.setText("0%");
        tvNeutralCount.setText("(0)");
        tvNegativePercent.setText("0%");
        tvNegativeCount.setText("(0)");
    }

    private void updateOverallStats(OverallSentimentStats stats) {
        tvTotalReviews.setText("Tổng đánh giá: " + stats.getTotalReviews());

        tvPositivePercent.setText(String.format(Locale.getDefault(), "%.0f%%", stats.getPositivePercent()));
        tvPositiveCount.setText("(" + stats.getPositiveCount() + ")");

        tvNeutralPercent.setText(String.format(Locale.getDefault(), "%.0f%%", stats.getNeutralPercent()));
        tvNeutralCount.setText("(" + stats.getNeutralCount() + ")");

        tvNegativePercent.setText(String.format(Locale.getDefault(), "%.0f%%", stats.getNegativePercent()));
        tvNegativeCount.setText("(" + stats.getNegativeCount() + ")");
    }

    private void loadTopFoods() {
        // Load top 5 foods with highest positive sentiment
        String orderPositive = "positive_percent.desc";
        dbService.getFoodSentimentStats(orderPositive).enqueue(new Callback<List<FoodSentimentStats>>() {
            @Override
            public void onResponse(Call<List<FoodSentimentStats>> call, Response<List<FoodSentimentStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodSentimentStats> allStats = response.body();
                    List<FoodSentimentStats> topPositive = new ArrayList<>();

                    // Get top 5 with reviews
                    for (FoodSentimentStats stat : allStats) {
                        if (stat.getTotalReviews() > 0 && topPositive.size() < 5) {
                            topPositive.add(stat);
                        }
                    }

                    if (topPositive.isEmpty()) {
                        tvTopPositiveEmpty.setVisibility(View.VISIBLE);
                        rvTopPositive.setVisibility(View.GONE);
                    } else {
                        tvTopPositiveEmpty.setVisibility(View.GONE);
                        rvTopPositive.setVisibility(View.VISIBLE);
                        topPositiveAdapter.setStats(topPositive);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FoodSentimentStats>> call, Throwable t) {
                tvTopPositiveEmpty.setVisibility(View.VISIBLE);
                rvTopPositive.setVisibility(View.GONE);
            }
        });

        // Load top 5 foods with highest negative sentiment
        String orderNegative = "negative_percent.desc";
        dbService.getFoodSentimentStats(orderNegative).enqueue(new Callback<List<FoodSentimentStats>>() {
            @Override
            public void onResponse(Call<List<FoodSentimentStats>> call, Response<List<FoodSentimentStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodSentimentStats> allStats = response.body();
                    List<FoodSentimentStats> topNegative = new ArrayList<>();

                    // Get top 5 with reviews and negative sentiment
                    for (FoodSentimentStats stat : allStats) {
                        if (stat.getTotalReviews() > 0 && stat.getNegativePercent() > 0 && topNegative.size() < 5) {
                            topNegative.add(stat);
                        }
                    }

                    if (topNegative.isEmpty()) {
                        tvTopNegativeEmpty.setVisibility(View.VISIBLE);
                        rvTopNegative.setVisibility(View.GONE);
                    } else {
                        tvTopNegativeEmpty.setVisibility(View.GONE);
                        rvTopNegative.setVisibility(View.VISIBLE);
                        topNegativeAdapter.setStats(topNegative);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FoodSentimentStats>> call, Throwable t) {
                tvTopNegativeEmpty.setVisibility(View.VISIBLE);
                rvTopNegative.setVisibility(View.GONE);
            }
        });
    }

    private void analyzeAllReviews() {
        btnAnalyzeAllReviews.setEnabled(false);
        btnAnalyzeAllReviews.setText("Đang phân tích...");

        // Get all reviews
        dbService.getAllReviews("*", "created_at.desc").enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();
                    analyzeReviewsBatch(reviews, 0, 0, 0);
                } else {
                    btnAnalyzeAllReviews.setEnabled(true);
                    btnAnalyzeAllReviews.setText("Phân tích lại tất cả đánh giá");
                    Toast.makeText(AIDashboardActivity.this,
                            "Không tìm thấy đánh giá nào",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                btnAnalyzeAllReviews.setEnabled(true);
                btnAnalyzeAllReviews.setText("Phân tích lại tất cả đánh giá");
                Toast.makeText(AIDashboardActivity.this,
                        "Lỗi: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void analyzeReviewsBatch(List<Review> reviews, int index, int successCount, int failureCount) {
        if (index >= reviews.size()) {
            // Done analyzing all reviews
            btnAnalyzeAllReviews.setEnabled(true);
            btnAnalyzeAllReviews.setText("Phân tích lại tất cả đánh giá");
            String message = "Đã phân tích xong. Thành công: " + successCount + ", lỗi: " + failureCount;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Reload stats
            loadOverallSentimentStats();
            loadTopFoods();
            return;
        }

        Review review = reviews.get(index);

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("review_id", review.getId());
        payload.put("content", review.getComment() == null ? "" : review.getComment());
        payload.put("rating", review.getRating());

        functionsService.analyzeReview(payload).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call,
                    Response<java.util.Map<String, Object>> response) {
                // HTTP errors also trigger onResponse in Retrofit, so check isSuccessful
                // explicitly.
                if (response.isSuccessful()) {
                    analyzeReviewsBatch(reviews, index + 1, successCount + 1, failureCount);
                } else {
                    analyzeReviewsBatch(reviews, index + 1, successCount, failureCount + 1);
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                // Continue anyway to avoid breaking whole batch.
                analyzeReviewsBatch(reviews, index + 1, successCount, failureCount + 1);
            }
        });
    }
}
