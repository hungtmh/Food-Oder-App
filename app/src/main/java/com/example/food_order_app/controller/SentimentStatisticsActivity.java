package com.example.food_order_app.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.SentimentStatsAdapter;
import com.example.food_order_app.model.FoodSentimentStats;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SentimentStatisticsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnSortPositive, btnSortNegative, btnSortRating, btnSortReviews;
    private RecyclerView rvSentimentStats;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private SupabaseDbService dbService;
    private SentimentStatsAdapter adapter;
    private List<FoodSentimentStats> allStats = new ArrayList<>();
    private String currentSort = "positive"; // positive, negative, rating, reviews

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentiment_statistics);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();
        loadSentimentStats();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSortPositive = findViewById(R.id.btnSortPositive);
        btnSortNegative = findViewById(R.id.btnSortNegative);
        btnSortRating = findViewById(R.id.btnSortRating);
        btnSortReviews = findViewById(R.id.btnSortReviews);
        rvSentimentStats = findViewById(R.id.rvSentimentStats);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        adapter = new SentimentStatsAdapter(this, stat -> {
            // Show details
            Toast.makeText(this, stat.getFoodName() + "\n" +
                    "Tích cực: " + String.format("%.0f%%", stat.getPositivePercent()) + "\n" +
                    "Tiêu cực: " + String.format("%.0f%%", stat.getNegativePercent()) + "\n" +
                    "Trung tính: " + String.format("%.0f%%", stat.getNeutralPercent()),
                    Toast.LENGTH_LONG).show();
        });
        rvSentimentStats.setLayoutManager(new LinearLayoutManager(this));
        rvSentimentStats.setAdapter(adapter);

        updateSortButtonUI();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSortPositive.setOnClickListener(v -> {
            currentSort = "positive";
            sortStats();
            updateSortButtonUI();
        });

        btnSortNegative.setOnClickListener(v -> {
            currentSort = "negative";
            sortStats();
            updateSortButtonUI();
        });

        btnSortRating.setOnClickListener(v -> {
            currentSort = "rating";
            sortStats();
            updateSortButtonUI();
        });

        btnSortReviews.setOnClickListener(v -> {
            currentSort = "reviews";
            sortStats();
            updateSortButtonUI();
        });
    }

    private void updateSortButtonUI() {
        // Reset all buttons
        btnSortPositive.setBackgroundTintList(null);
        btnSortNegative.setBackgroundTintList(null);
        btnSortRating.setBackgroundTintList(null);
        btnSortReviews.setBackgroundTintList(null);

        // Highlight selected button
        Button selectedButton = null;
        switch (currentSort) {
            case "positive":
                selectedButton = btnSortPositive;
                break;
            case "negative":
                selectedButton = btnSortNegative;
                break;
            case "rating":
                selectedButton = btnSortRating;
                break;
            case "reviews":
                selectedButton = btnSortReviews;
                break;
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(
                    getResources().getColorStateList(R.color.primary, null));
        }
    }

    private void loadSentimentStats() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvSentimentStats.setVisibility(View.GONE);

        dbService.getFoodSentimentStats("food_name.asc").enqueue(new Callback<List<FoodSentimentStats>>() {
            @Override
            public void onResponse(Call<List<FoodSentimentStats>> call, Response<List<FoodSentimentStats>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    allStats = response.body();
                    
                    // Filter out foods with no reviews
                    List<FoodSentimentStats> filteredStats = new ArrayList<>();
                    for (FoodSentimentStats stat : allStats) {
                        if (stat.getTotalReviews() > 0) {
                            filteredStats.add(stat);
                        }
                    }
                    
                    if (filteredStats.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvSentimentStats.setVisibility(View.GONE);
                    } else {
                        allStats = filteredStats;
                        sortStats();
                        tvEmpty.setVisibility(View.GONE);
                        rvSentimentStats.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvSentimentStats.setVisibility(View.GONE);
                    Toast.makeText(SentimentStatisticsActivity.this, 
                        "Không thể tải dữ liệu", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FoodSentimentStats>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                rvSentimentStats.setVisibility(View.GONE);
                Toast.makeText(SentimentStatisticsActivity.this, 
                    "Lỗi: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortStats() {
        if (allStats.isEmpty()) return;

        List<FoodSentimentStats> sortedStats = new ArrayList<>(allStats);

        switch (currentSort) {
            case "positive":
                Collections.sort(sortedStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Double.compare(o2.getPositivePercent(), o1.getPositivePercent());
                    }
                });
                break;

            case "negative":
                Collections.sort(sortedStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Double.compare(o2.getNegativePercent(), o1.getNegativePercent());
                    }
                });
                break;

            case "rating":
                Collections.sort(sortedStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Double.compare(o2.getAvgRating(), o1.getAvgRating());
                    }
                });
                break;

            case "reviews":
                Collections.sort(sortedStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Integer.compare(o2.getTotalReviews(), o1.getTotalReviews());
                    }
                });
                break;
        }

        adapter.setStats(sortedStats);
    }
}
