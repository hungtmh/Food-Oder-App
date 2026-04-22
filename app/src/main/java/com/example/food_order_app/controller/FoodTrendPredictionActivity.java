package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    // Minimum data thresholds to avoid unreliable predictions.
    private static final int MIN_REVIEWS = 5;
    private static final int MIN_ORDERS = 10;
    private static final String TAG = "FoodTrendPrediction";

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

    private interface SentimentStatsCallback {
        void onResult(FoodSentimentStats stats);
    }

    private interface IntCallback {
        void onResult(int value);
    }

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

        adapter = new FoodTrendAdapter(this, this::openTrendInsightDetail);
        rvTrends.setLayoutManager(new LinearLayoutManager(this));
        rvTrends.setAdapter(adapter);

        updateFilterButtonUI();
    }

    private void openTrendInsightDetail(FoodTrend trend) {
        Intent detailIntent = new Intent(this, TrendInsightDetailActivity.class);

        if (trend.getFood() != null) {
            detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_FOOD_NAME, trend.getFood().getName());
            detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_IMAGE_URL, trend.getFood().getImageUrl());
        }

        detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_TREND_TYPE, trend.getTrendType());
        detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_CONFIDENCE_SCORE, trend.getConfidenceScore());
        detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_SALES_TREND, trend.getSalesTrend());
        detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_SENTIMENT_TREND, trend.getSentimentTrend());
        detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_PREDICTION_PERIOD, trend.getPredictionPeriod());
        detailIntent.putExtra(TrendInsightDetailActivity.EXTRA_NOTES, trend.getNotes());
        startActivity(detailIntent);
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
        styleFilterButton(btnFilterAll, "all".equals(currentFilter));
        styleFilterButton(btnFilterHotSeller, "hot_seller".equals(currentFilter));
        styleFilterButton(btnFilterDeclining, "declining".equals(currentFilter));
        styleFilterButton(btnFilterAtRisk, "at_risk".equals(currentFilter));
        styleFilterButton(btnFilterStable, "stable".equals(currentFilter));
    }

    private void styleFilterButton(Button button, boolean isSelected) {
        button.setSelected(isSelected);
        button.setBackgroundTintList(null);

        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_trend_filter_button_selected);
            button.setTextColor(ContextCompat.getColor(this, R.color.white)); // Chữ trắng cho nổi
        } else {
            button.setBackgroundResource(R.drawable.bg_trend_filter_button_default);
            button.setTextColor(ContextCompat.getColor(this, R.color.trend_filter_button_text_default));
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
        Calendar now = Calendar.getInstance();

        Calendar currentFrom = (Calendar) now.clone();
        currentFrom.add(Calendar.DAY_OF_MONTH, -29);
        Calendar currentTo = (Calendar) now.clone();

        Calendar prevFrom = (Calendar) now.clone();
        prevFrom.add(Calendar.DAY_OF_MONTH, -59);
        Calendar prevTo = (Calendar) now.clone();
        prevTo.add(Calendar.DAY_OF_MONTH, -30);

        Log.d(TAG, "predictFoodTrend START | foodId=" + food.getId()
                + " | foodName=" + food.getName()
                + " | currentRange=" + formatDateTime(currentFrom, true) + " -> " + formatDateTime(currentTo, false)
                + " | prevRange=" + formatDateTime(prevFrom, true) + " -> " + formatDateTime(prevTo, false));

        fetchSentimentStats(food.getId(),
                sentimentStats -> getOrderCountByFoodInRange(food.getId(), currentFrom, currentTo,
                        currentSales -> getOrderCountByFoodInRange(food.getId(), prevFrom, prevTo, prevSales -> {
                            double actualSalesTrend = calculateSalesTrendPercent(currentSales, prevSales);

                            int totalReviews = sentimentStats != null ? sentimentStats.getTotalReviews() : 0;
                            double positivePercent = sentimentStats != null ? sentimentStats.getPositivePercent() : 0.0;
                            double negativePercent = sentimentStats != null ? sentimentStats.getNegativePercent() : 0.0;
                            double avgSentimentScore = sentimentStats != null ? sentimentStats.getAvgSentimentScore()
                                    : 0.5;

                            String trendType;
                            double confidenceScore;
                            String reason = "Rule based result";
                            String matchedRule;

                            Log.d(TAG, "predictFoodTrend METRICS | foodId=" + food.getId()
                                    + " | currentSales=" + currentSales
                                    + " | prevSales=" + prevSales
                                    + " | actualSalesTrend=" + actualSalesTrend
                                    + " | totalReviews=" + totalReviews
                                    + " | positivePercent=" + positivePercent
                                    + " | negativePercent=" + negativePercent
                                    + " | avgSentimentScore=" + avgSentimentScore);

                            if (totalReviews < MIN_REVIEWS && currentSales < MIN_ORDERS
                                    && prevSales < MIN_ORDERS) {
                                matchedRule = "rule_1_not_enough_data";
                                trendType = "stable";
                                confidenceScore = 0.40;
                                reason = "Not enough data to predict";
                            } else if (negativePercent >= 50.0 || actualSalesTrend <= -30.0) {
                                matchedRule = "rule_2_at_risk";
                                trendType = "at_risk";
                                confidenceScore = totalReviews > 20 ? 0.90 : 0.75;
                                reason = "High negative sentiment or severe sales drop";
                            } else if ((positivePercent >= 70.0 && actualSalesTrend >= 5.0 && currentSales >= 20)
                                    || (actualSalesTrend >= 20.0 && currentSales >= 20)) {
                                matchedRule = "rule_3_hot_seller";
                                trendType = "hot_seller";
                                confidenceScore = 0.85;
                                reason = "Strong positive sentiment or sharp sales growth";
                            } else if (negativePercent >= 30.0 || actualSalesTrend <= -10.0) {
                                matchedRule = "rule_4_declining";
                                trendType = "declining";
                                confidenceScore = 0.70;
                                reason = "Moderate risk from sentiment or sales decline";
                            } else {
                                matchedRule = "rule_5_stable_default";
                                trendType = "stable";
                                confidenceScore = 0.80;
                                reason = "Stable within current rule thresholds";
                            }

                            Log.d(TAG, "predictFoodTrend RESULT | foodId=" + food.getId()
                                    + " | matchedRule=" + matchedRule
                                    + " | trendType=" + trendType
                                    + " | confidenceScore=" + confidenceScore
                                    + " | reason=" + reason);

                            saveFoodTrend(
                                    food.getId(),
                                    trendType,
                                    confidenceScore,
                                    actualSalesTrend,
                                    avgSentimentScore,
                                    reason,
                                    onComplete);
                        })));
    }

    private void saveFoodTrend(String foodId, String trendType, double confidenceScore,
            double salesTrend, double sentimentTrend, String notes, Runnable onComplete) {
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
                trendData.put("notes", notes);

                dbService.createFoodTrend(trendData).enqueue(new Callback<List<FoodTrend>>() {
                    @Override
                    public void onResponse(Call<List<FoodTrend>> call, Response<List<FoodTrend>> response) {
                        if (onComplete != null)
                            onComplete.run();
                    }

                    @Override
                    public void onFailure(Call<List<FoodTrend>> call, Throwable t) {
                        if (onComplete != null)
                            onComplete.run();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Continue anyway
                if (onComplete != null)
                    onComplete.run();
            }
        });
    }

    private void fetchSentimentStats(String foodId, SentimentStatsCallback callback) {
        dbService.getFoodSentimentStatsByFood("eq." + foodId).enqueue(new Callback<List<FoodSentimentStats>>() {
            @Override
            public void onResponse(Call<List<FoodSentimentStats>> call, Response<List<FoodSentimentStats>> response) {
                FoodSentimentStats sentimentStats = null;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    sentimentStats = response.body().get(0);
                }
                callback.onResult(sentimentStats);
            }

            @Override
            public void onFailure(Call<List<FoodSentimentStats>> call, Throwable t) {
                callback.onResult(null);
            }
        });
    }

    private void getOrderCountByFoodInRange(String foodId, Calendar from, Calendar to, IntCallback callback) {
        String fromStr = formatDateTime(from, true);
        String toStr = formatDateTime(to, false);

        Log.d(TAG, "getOrderCountByFoodInRange START | foodId=" + foodId
                + " | from=" + fromStr + " | to=" + toStr);

        String select = "quantity,orders!inner(status,created_at)";
        dbService.getFoodQuantityInDateRange(
                select,
                "eq." + foodId,
                "eq.served",
                "gte." + fromStr,
                "lte." + toStr)
                .enqueue(new Callback<List<OrderItem>>() {
                    @Override
                    public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                        int itemCount = response.body() != null ? response.body().size() : 0;
                        Log.d(TAG, "getFoodQuantityInDateRange RESPONSE | foodId=" + foodId
                                + " | code=" + response.code()
                                + " | success=" + response.isSuccessful()
                                + " | bodySize=" + itemCount);

                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            Log.w(TAG, "getFoodQuantityInDateRange EMPTY/FAILED | foodId=" + foodId
                                    + " | code=" + response.code());
                            callback.onResult(0);
                            return;
                        }

                        int totalQuantity = 0;
                        for (OrderItem item : response.body()) {
                            totalQuantity += item.getQuantity();
                        }

                        Log.d(TAG, "getOrderCountByFoodInRange RESULT | foodId=" + foodId
                                + " | totalQuantity=" + totalQuantity);
                        callback.onResult(totalQuantity);
                    }

                    @Override
                    public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                        Log.e(TAG, "getFoodQuantityInDateRange FAILURE | foodId=" + foodId
                                + " | from=" + fromStr + " | to=" + toStr
                                + " | message=" + t.getMessage(), t);
                        callback.onResult(0);
                    }
                });
    }

    private double calculateSalesTrendPercent(int currentSales, int previousSales) {
        if (previousSales == 0) {
            return currentSales > 0 ? 100.0 : 0.0;
        }
        return ((double) (currentSales - previousSales) / previousSales) * 100.0;
    }

    private String formatDateTime(Calendar calendar, boolean startOfDay) {
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return isoDateFormat.format(calendar.getTime()) + (startOfDay ? "T00:00:00" : "T23:59:59");
    }

    private String buildInFilter(List<String> ids) {
        StringBuilder sb = new StringBuilder("in.(");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ids.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
