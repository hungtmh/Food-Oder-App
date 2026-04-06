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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.SentimentStatsAdapter;
import com.example.food_order_app.config.AiConfig;
import com.example.food_order_app.model.FoodSentimentStats;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.InsightCacheManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

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
    private final Map<String, String> insightCache = new HashMap<>();
    private int insightRequestVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentiment_statistics);

        dbService = RetrofitClient.getDbService();
        insightCache.putAll(InsightCacheManager.loadInsights(this));
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

        adapter = new SentimentStatsAdapter(this, this::openFoodInsightDetail);
        rvSentimentStats.setLayoutManager(new LinearLayoutManager(this));
        rvSentimentStats.setAdapter(adapter);

        updateSortButtonUI();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSortPositive.setOnClickListener(v -> {
            currentSort = "positive";
            applyFilterAndSort();
            updateSortButtonUI();
        });

        btnSortNegative.setOnClickListener(v -> {
            currentSort = "negative";
            applyFilterAndSort();
            updateSortButtonUI();
        });

        btnSortRating.setOnClickListener(v -> {
            currentSort = "rating";
            applyFilterAndSort();
            updateSortButtonUI();
        });

        btnSortReviews.setOnClickListener(v -> {
            currentSort = "reviews";
            applyFilterAndSort();
            updateSortButtonUI();
        });
    }

    private void updateSortButtonUI() {
        styleSortButton(btnSortPositive, "positive".equals(currentSort));
        styleSortButton(btnSortNegative, "negative".equals(currentSort));
        styleSortButton(btnSortRating, "rating".equals(currentSort));
        styleSortButton(btnSortReviews, "reviews".equals(currentSort));
    }

    private void styleSortButton(Button button, boolean isSelected) {
        button.setSelected(isSelected);
        button.setBackgroundTintList(null);

        if (isSelected) {
            button.setBackgroundResource(R.drawable.bg_trend_filter_button_selected);
            button.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            button.setBackgroundResource(R.drawable.bg_trend_filter_button_default);
            button.setTextColor(ContextCompat.getColor(this, R.color.trend_filter_button_text_default));
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
                        applyFilterAndSort();
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

    private void applyFilterAndSort() {
        if (allStats.isEmpty()) {
            adapter.setStats(new ArrayList<>());
            adapter.setInsights(new HashMap<>());
            tvEmpty.setVisibility(View.VISIBLE);
            rvSentimentStats.setVisibility(View.GONE);
            return;
        }

        List<FoodSentimentStats> visibleStats = new ArrayList<>();

        switch (currentSort) {
            case "positive":
                for (FoodSentimentStats stat : allStats) {
                    if ("positive".equals(stat.getDominantSentiment())) {
                        visibleStats.add(stat);
                    }
                }
                break;

            case "negative":
                for (FoodSentimentStats stat : allStats) {
                    if ("negative".equals(stat.getDominantSentiment())) {
                        visibleStats.add(stat);
                    }
                }
                break;

            case "rating":
            case "reviews":
            default:
                visibleStats.addAll(allStats);
                break;
        }

        switch (currentSort) {
            case "positive":
                Collections.sort(visibleStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Double.compare(o2.getPositivePercent(), o1.getPositivePercent());
                    }
                });
                break;

            case "negative":
                Collections.sort(visibleStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Double.compare(o2.getNegativePercent(), o1.getNegativePercent());
                    }
                });
                break;

            case "rating":
                Collections.sort(visibleStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Double.compare(o2.getAvgRating(), o1.getAvgRating());
                    }
                });
                break;

            case "reviews":
                Collections.sort(visibleStats, new Comparator<FoodSentimentStats>() {
                    @Override
                    public int compare(FoodSentimentStats o1, FoodSentimentStats o2) {
                        return Integer.compare(o2.getTotalReviews(), o1.getTotalReviews());
                    }
                });
                break;
        }

        if (visibleStats.isEmpty()) {
            tvEmpty.setText(getEmptyMessageForCurrentFilter());
            tvEmpty.setVisibility(View.VISIBLE);
            rvSentimentStats.setVisibility(View.GONE);
            adapter.setStats(new ArrayList<>());
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        rvSentimentStats.setVisibility(View.VISIBLE);
        adapter.setStats(visibleStats);
        adapter.setInsights(insightCache);

        fetchAiInsightsForVisibleStats(visibleStats);
    }

    private String getEmptyMessageForCurrentFilter() {
        switch (currentSort) {
            case "positive":
                return "Không có món nào thuộc nhóm Tích cực cao.";
            case "negative":
                return "Không có món nào thuộc nhóm Tiêu cực cao.";
            default:
                return "Không có dữ liệu thống kê.";
        }
    }

    private void openFoodInsightDetail(FoodSentimentStats stat) {
        Intent detailIntent = new Intent(this, FoodInsightDetailActivity.class);
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_FOOD_NAME, stat.getFoodName());
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_TOTAL_REVIEWS, stat.getTotalReviews());
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_AVG_RATING, stat.getAvgRating());
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_POSITIVE_PERCENT, stat.getPositivePercent());
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_NEUTRAL_PERCENT, stat.getNeutralPercent());
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_NEGATIVE_PERCENT, stat.getNegativePercent());
        detailIntent.putExtra(FoodInsightDetailActivity.EXTRA_AVG_SENTIMENT_SCORE, stat.getAvgSentimentScore());
        startActivity(detailIntent);
    }

    private void fetchAiInsightsForVisibleStats(List<FoodSentimentStats> visibleStats) {
        insightRequestVersion++;
        final int requestVersion = insightRequestVersion;

        List<FoodSentimentStats> pending = new ArrayList<>();
        for (FoodSentimentStats stat : visibleStats) {
            String key = stat.getFoodId();
            if (key != null && !key.isEmpty() && !insightCache.containsKey(key)) {
                pending.add(stat);
            }
        }

        if (pending.isEmpty()) {
            return;
        }

        String hfToken = AiConfig.HF_TOKEN;
        if (hfToken == null || hfToken.trim().isEmpty()) {
            for (FoodSentimentStats stat : pending) {
                insightCache.put(stat.getFoodId(), buildFallbackInsight(stat));
                InsightCacheManager.saveInsight(this, stat.getFoodId(), insightCache.get(stat.getFoodId()));
            }
            adapter.setInsights(insightCache);
            return;
        }

        fetchInsightSequentially(pending, 0, hfToken, requestVersion);
    }

    private void fetchInsightSequentially(List<FoodSentimentStats> pending, int index, String hfToken,
            int requestVersion) {
        if (requestVersion != insightRequestVersion) {
            return;
        }

        if (index >= pending.size()) {
            adapter.setInsights(insightCache);
            return;
        }

        FoodSentimentStats stat = pending.get(index);
        requestAiInsightForFood(stat, hfToken, insight -> {
            if (requestVersion != insightRequestVersion) {
                return;
            }

            if (stat.getFoodId() != null && !stat.getFoodId().isEmpty()) {
                insightCache.put(stat.getFoodId(), insight);
                InsightCacheManager.saveInsight(SentimentStatisticsActivity.this, stat.getFoodId(), insight);
            }
            adapter.setInsights(insightCache);
            fetchInsightSequentially(pending, index + 1, hfToken, requestVersion);
        });
    }

    private void requestAiInsightForFood(FoodSentimentStats stat, String hfToken, InsightCallback callback) {
        String prompt = "Hãy viết 1 câu insight ngắn (tối đa 25 từ) cho quản trị viên về món ăn sau, bằng tiếng Việt, có thể hành động ngay. "
                + "Món: " + stat.getFoodName()
                + ", Tích cực: " + String.format(Locale.getDefault(), "%.0f%%", stat.getPositivePercent())
                + ", Tiêu cực: " + String.format(Locale.getDefault(), "%.0f%%", stat.getNegativePercent())
                + ", Trung tính: " + String.format(Locale.getDefault(), "%.0f%%", stat.getNeutralPercent())
                + ", Rating: " + String.format(Locale.getDefault(), "%.1f", stat.getAvgRating())
                + ", Tổng review: " + stat.getTotalReviews() + "."
                + " Bối cảnh filter hiện tại: " + currentSort + ".";

        org.json.JSONObject requestBody = new org.json.JSONObject();
        try {
            org.json.JSONArray messages = new org.json.JSONArray();

            org.json.JSONObject systemMessage = new org.json.JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content",
                    "Bạn là AI phân tích phản hồi món ăn. Trả lời đúng 1 câu insight ngắn, rõ ràng, có đề xuất hành động.");
            messages.put(systemMessage);

            org.json.JSONObject userMessage = new org.json.JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            requestBody.put("model", "Qwen/Qwen2.5-72B-Instruct");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2);
            requestBody.put("max_tokens", 120);
        } catch (org.json.JSONException e) {
            callback.onDone(buildFallbackInsight(stat));
            return;
        }

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                requestBody.toString(),
                okhttp3.MediaType.parse("application/json"));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://router.huggingface.co/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + hfToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> callback.onDone(buildFallbackInsight(stat)));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        callback.onDone(buildFallbackInsight(stat));
                        return;
                    }

                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseBody);
                        String aiText = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        if (aiText.isEmpty()) {
                            callback.onDone(buildFallbackInsight(stat));
                        } else {
                            callback.onDone(aiText);
                        }
                    } catch (org.json.JSONException e) {
                        callback.onDone(buildFallbackInsight(stat));
                    }
                });
            }
        });
    }

    private String buildFallbackInsight(FoodSentimentStats stat) {
        if ("negative".equals(stat.getDominantSentiment())) {
            return "Tỉ lệ tiêu cực cao, nên rà soát chất lượng và phản hồi khách trong 24h để giảm đánh giá xấu.";
        }
        if ("positive".equals(stat.getDominantSentiment())) {
            return "Món đang được phản hồi tích cực, nên ưu tiên hiển thị và giữ chất lượng ổn định để tăng chuyển đổi.";
        }
        return "Cảm xúc còn trung tính, nên cải thiện mô tả và khuyến mãi nhẹ để tăng trải nghiệm tích cực.";
    }

    private interface InsightCallback {
        void onDone(String insight);
    }
}
