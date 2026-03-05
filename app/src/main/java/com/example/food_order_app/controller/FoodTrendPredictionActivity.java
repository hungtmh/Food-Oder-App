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
import com.example.food_order_app.adapter.FoodTrendAdapter;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.FoodSentimentStats;
import com.example.food_order_app.model.FoodTrend;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodTrendPredictionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnGeneratePredictions;
    private Button btnFilterAll, btnFilterHotSeller, btnFilterDeclining, btnFilterAtRisk, btnFilterStable;
    private RecyclerView rvTrends;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private SupabaseDbService dbService;
    private FoodTrendAdapter adapter;
    private List<FoodTrend> allTrends = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_trend_prediction);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();
        loadTrends();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnGeneratePredictions = findViewById(R.id.btnGeneratePredictions);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterHotSeller = findViewById(R.id.btnFilterHotSeller);
        btnFilterDeclining = findViewById(R.id.btnFilterDeclining);
        btnFilterAtRisk = findViewById(R.id.btnFilterAtRisk);
        btnFilterStable = findViewById(R.id.btnFilterStable);
        rvTrends = findViewById(R.id.rvTrends);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        adapter = new FoodTrendAdapter(this, trend -> {
            // Show trend details
            if (trend.getFood() != null) {
                Toast.makeText(this, "Chi tiết xu hướng: " + trend.getFood().getName(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
        rvTrends.setLayoutManager(new LinearLayoutManager(this));
        rvTrends.setAdapter(adapter);

        updateFilterButtonUI();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnGeneratePredictions.setOnClickListener(v -> generatePredictions());

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            filterTrends();
            updateFilterButtonUI();
        });

        btnFilterHotSeller.setOnClickListener(v -> {
            currentFilter = "hot_seller";
            filterTrends();
            updateFilterButtonUI();
        });

        btnFilterDeclining.setOnClickListener(v -> {
            currentFilter = "declining";
            filterTrends();
            updateFilterButtonUI();
        });

        btnFilterAtRisk.setOnClickListener(v -> {
            currentFilter = "at_risk";
            filterTrends();
            updateFilterButtonUI();
        });

        btnFilterStable.setOnClickListener(v -> {
            currentFilter = "stable";
            filterTrends();
            updateFilterButtonUI();
        });
    }

    private void updateFilterButtonUI() {
        btnFilterAll.setBackgroundTintList(null);
        btnFilterHotSeller.setBackgroundTintList(null);
        btnFilterDeclining.setBackgroundTintList(null);
        btnFilterAtRisk.setBackgroundTintList(null);
        btnFilterStable.setBackgroundTintList(null);

        Button selectedButton = null;
        switch (currentFilter) {
            case "all":
                selectedButton = btnFilterAll;
                break;
            case "hot_seller":
                selectedButton = btnFilterHotSeller;
                break;
            case "declining":
                selectedButton = btnFilterDeclining;
                break;
            case "at_risk":
                selectedButton = btnFilterAtRisk;
                break;
            case "stable":
                selectedButton = btnFilterStable;
                break;
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(
                    getResources().getColorStateList(R.color.primary, null));
        }
    }

    private void loadTrends() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvTrends.setVisibility(View.GONE);

        String select = "*,foods(*)";
        dbService.getFoodTrends(select, "confidence_score.desc").enqueue(new Callback<List<FoodTrend>>() {
            @Override
            public void onResponse(Call<List<FoodTrend>> call, Response<List<FoodTrend>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    allTrends = response.body();
                    filterTrends();
                    tvEmpty.setVisibility(View.GONE);
                    rvTrends.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvTrends.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<FoodTrend>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                rvTrends.setVisibility(View.GONE);
                Toast.makeText(FoodTrendPredictionActivity.this, 
                    "Lỗi: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTrends() {
        List<FoodTrend> filteredTrends;
        
        if (currentFilter.equals("all")) {
            filteredTrends = new ArrayList<>(allTrends);
        } else {
            filteredTrends = new ArrayList<>();
            for (FoodTrend trend : allTrends) {
                if (trend.getTrendType().equals(currentFilter)) {
                    filteredTrends.add(trend);
                }
            }
        }

        adapter.setTrends(filteredTrends);

        if (filteredTrends.isEmpty() && !allTrends.isEmpty()) {
            Toast.makeText(this, "Không có món ăn nào trong danh mục này", Toast.LENGTH_SHORT).show();
        }
    }

    private void generatePredictions() {
        btnGeneratePredictions.setEnabled(false);
        btnGeneratePredictions.setText("Đang phân tích...");
        progressBar.setVisibility(View.VISIBLE);

        // Step 1: Get all foods
        dbService.getAllFoods("eq.true", "name.asc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = response.body();
                    analyzeFoodsTrends(foods, 0);
                } else {
                    btnGeneratePredictions.setEnabled(true);
                    btnGeneratePredictions.setText("Phân tích");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(FoodTrendPredictionActivity.this, 
                        "Không tìm thấy món ăn", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                btnGeneratePredictions.setEnabled(true);
                btnGeneratePredictions.setText("Phân tích");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodTrendPredictionActivity.this, 
                    "Lỗi: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void analyzeFoodsTrends(List<Food> foods, int index) {
        if (index >= foods.size()) {
            // Done
            btnGeneratePredictions.setEnabled(true);
            btnGeneratePredictions.setText("Phân tích");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Đã phân tích " + foods.size() + " món ăn", Toast.LENGTH_SHORT).show();
            loadTrends();
            return;
        }

        Food food = foods.get(index);
        predictFoodTrend(food, () -> analyzeFoodsTrends(foods, index + 1));
    }

    private void predictFoodTrend(Food food, Runnable onComplete) {
        // Get last 30 days orders for this food
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateFrom = sdf.format(cal.getTime());

        // Get sentiment stats
        dbService.getFoodSentimentStatsByFood("eq." + food.getId()).enqueue(new Callback<List<FoodSentimentStats>>() {
            @Override
            public void onResponse(Call<List<FoodSentimentStats>> call, Response<List<FoodSentimentStats>> response) {
                FoodSentimentStats sentimentStats = null;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    sentimentStats = response.body().get(0);
                }

                // Calculate trend based on sentiment and sales (simplified AI logic)
                String trendType;
                double confidenceScore;
                double salesTrend = 0; // This should be calculated from order history
                double sentimentTrend = 0;

                if (sentimentStats != null && sentimentStats.getTotalReviews() > 0) {
                    sentimentTrend = sentimentStats.getAvgSentimentScore();
                    
                    if (sentimentStats.getPositivePercent() > 70 && food.isPopular()) {
                        trendType = "hot_seller";
                        confidenceScore = 0.85;
                        salesTrend = 25.0;
                    } else if (sentimentStats.getNegativePercent() > 50) {
                        trendType = "at_risk";
                        confidenceScore = 0.75;
                        salesTrend = -15.0;
                    } else if (sentimentStats.getNegativePercent() > 30) {
                        trendType = "declining";
                        confidenceScore = 0.70;
                        salesTrend = -8.0;
                    } else {
                        trendType = "stable";
                        confidenceScore = 0.65;
                        salesTrend = 2.0;
                    }
                } else {
                    // No data, assume stable
                    trendType = "stable";
                    confidenceScore = 0.50;
                    salesTrend = 0;
                    sentimentTrend = 0.5;
                }

                // Save trend to database
                saveFoodTrend(food.getId(), trendType, confidenceScore, salesTrend, sentimentTrend, onComplete);
            }

            @Override
            public void onFailure(Call<List<FoodSentimentStats>> call, Throwable t) {
                // Skip this food
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void saveFoodTrend(String foodId, String trendType, double confidenceScore, 
                              double salesTrend, double sentimentTrend, Runnable onComplete) {
        // First, delete existing trend for this food
        dbService.deleteFoodTrend("eq." + foodId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Create new trend
                Map<String, Object> trendData = new HashMap<>();
                trendData.put("food_id", foodId);
                trendData.put("trend_type", trendType);
                trendData.put("confidence_score", confidenceScore);
                trendData.put("prediction_period", "30_days");
                trendData.put("sales_trend", salesTrend);
                trendData.put("sentiment_trend", sentimentTrend);

                dbService.createFoodTrend(trendData).enqueue(new Callback<List<FoodTrend>>() {
                    @Override
                    public void onResponse(Call<List<FoodTrend>> call, Response<List<FoodTrend>> response) {
                        if (onComplete != null) onComplete.run();
                    }

                    @Override
                    public void onFailure(Call<List<FoodTrend>> call, Throwable t) {
                        if (onComplete != null) onComplete.run();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Continue anyway
                if (onComplete != null) onComplete.run();
            }
        });
    }
}
