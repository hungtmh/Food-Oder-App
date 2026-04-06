package com.example.food_order_app.controller;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;

import java.util.Locale;

public class FoodInsightDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_NAME = "extra_food_name";
    public static final String EXTRA_TOTAL_REVIEWS = "extra_total_reviews";
    public static final String EXTRA_AVG_RATING = "extra_avg_rating";
    public static final String EXTRA_POSITIVE_PERCENT = "extra_positive_percent";
    public static final String EXTRA_NEUTRAL_PERCENT = "extra_neutral_percent";
    public static final String EXTRA_NEGATIVE_PERCENT = "extra_negative_percent";
    public static final String EXTRA_AVG_SENTIMENT_SCORE = "extra_avg_sentiment_score";

    private TextView tvFoodName;
    private TextView tvReviewCount;
    private TextView tvAvgRating;
    private TextView tvPositivePercent;
    private TextView tvNeutralPercent;
    private TextView tvNegativePercent;
    private TextView tvAiInsight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_insight_detail);

        initViews();
        bindData();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackFoodInsightDetail);
        tvFoodName = findViewById(R.id.tvFoodNameDetail);
        tvReviewCount = findViewById(R.id.tvTotalReviewsDetail);
        tvAvgRating = findViewById(R.id.tvAvgRatingDetail);
        tvPositivePercent = findViewById(R.id.tvPositivePercentDetail);
        tvNeutralPercent = findViewById(R.id.tvNeutralPercentDetail);
        tvNegativePercent = findViewById(R.id.tvNegativePercentDetail);
        tvAiInsight = findViewById(R.id.tvAiInsightDetail);

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindData() {
        String foodName = getIntent().getStringExtra(EXTRA_FOOD_NAME);
        int totalReviews = getIntent().getIntExtra(EXTRA_TOTAL_REVIEWS, 0);
        double avgRating = getIntent().getDoubleExtra(EXTRA_AVG_RATING, 0.0);
        double positivePercent = getIntent().getDoubleExtra(EXTRA_POSITIVE_PERCENT, 0.0);
        double neutralPercent = getIntent().getDoubleExtra(EXTRA_NEUTRAL_PERCENT, 0.0);
        double negativePercent = getIntent().getDoubleExtra(EXTRA_NEGATIVE_PERCENT, 0.0);
        double avgSentimentScore = getIntent().getDoubleExtra(EXTRA_AVG_SENTIMENT_SCORE, 0.0);

        tvFoodName.setText(foodName == null ? "Không rõ món" : foodName);
        tvReviewCount.setText("Tổng đánh giá: " + totalReviews);
        tvAvgRating.setText(String.format(Locale.getDefault(), "Điểm trung bình: %.1f/5.0", avgRating));
        tvPositivePercent.setText(String.format(Locale.getDefault(), "Tích cực: %.0f%%", positivePercent));
        tvNeutralPercent.setText(String.format(Locale.getDefault(), "Trung tính: %.0f%%", neutralPercent));
        tvNegativePercent.setText(String.format(Locale.getDefault(), "Tiêu cực: %.0f%%", negativePercent));

        tvAiInsight.setText(buildInsight(avgRating, negativePercent, totalReviews, avgSentimentScore));
    }

    private String buildInsight(double avgRating, double negativePercent, int totalReviews, double avgSentimentScore) {
        if (totalReviews == 0) {
            return "Chưa đủ dữ liệu để AI tạo insight cho món này.";
        }

        if (avgRating >= 4.0) {
            return String.format(
                    Locale.getDefault(),
                    "Món này đang được khách hàng yêu thích (%.1f/5). Nên giữ ổn định chất lượng và có thể ưu tiên đẩy truyền thông. Điểm cảm xúc trung bình: %.2f.",
                    avgRating,
                    avgSentimentScore);
        }

        if (avgRating < 2.0) {
            return String.format(
                    Locale.getDefault(),
                    "Món này có mức hài lòng thấp (%.1f/5). Cần kiểm tra nguyên liệu, khẩu vị và quy trình phục vụ. Tỷ lệ phản hồi tiêu cực hiện là %.0f%%.",
                    avgRating,
                    negativePercent);
        }

        if (negativePercent >= 35.0) {
            return String.format(
                    Locale.getDefault(),
                    "Món đang có rủi ro do phản hồi tiêu cực khá cao (%.0f%%). Nên đọc kỹ nhận xét gần đây để xử lý các vấn đề lặp lại.",
                    negativePercent);
        }

        return String.format(
                Locale.getDefault(),
                "Món có hiệu suất trung bình (%.1f/5). Nên theo dõi thêm review mới để tối ưu trải nghiệm khách hàng. Điểm cảm xúc trung bình: %.2f.",
                avgRating,
                avgSentimentScore);
    }
}
