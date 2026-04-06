package com.example.food_order_app.controller;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;

import java.util.Locale;

public class TrendInsightDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_NAME = "extra_food_name";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_TREND_TYPE = "extra_trend_type";
    public static final String EXTRA_CONFIDENCE_SCORE = "extra_confidence_score";
    public static final String EXTRA_SALES_TREND = "extra_sales_trend";
    public static final String EXTRA_SENTIMENT_TREND = "extra_sentiment_trend";
    public static final String EXTRA_PREDICTION_PERIOD = "extra_prediction_period";
    public static final String EXTRA_NOTES = "extra_notes";

    private ImageView ivFood;
    private TextView tvFoodName;
    private TextView tvTrendType;
    private TextView tvConfidence;
    private TextView tvSalesTrend;
    private TextView tvSentimentTrend;
    private TextView tvPredictionPeriod;
    private TextView tvAiInsight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_insight_detail);

        initViews();
        bindData();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackTrendDetail);
        ivFood = findViewById(R.id.ivFoodTrendDetail);
        tvFoodName = findViewById(R.id.tvFoodNameTrendDetail);
        tvTrendType = findViewById(R.id.tvTrendTypeDetail);
        tvConfidence = findViewById(R.id.tvConfidenceDetail);
        tvSalesTrend = findViewById(R.id.tvSalesTrendDetail);
        tvSentimentTrend = findViewById(R.id.tvSentimentTrendDetail);
        tvPredictionPeriod = findViewById(R.id.tvPredictionPeriodDetail);
        tvAiInsight = findViewById(R.id.tvAiTrendInsightDetail);

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindData() {
        String foodName = getIntent().getStringExtra(EXTRA_FOOD_NAME);
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String trendType = getIntent().getStringExtra(EXTRA_TREND_TYPE);
        double confidenceScore = getIntent().getDoubleExtra(EXTRA_CONFIDENCE_SCORE, 0.0);
        double salesTrend = getIntent().getDoubleExtra(EXTRA_SALES_TREND, 0.0);
        double sentimentTrend = getIntent().getDoubleExtra(EXTRA_SENTIMENT_TREND, 0.0);
        String predictionPeriod = getIntent().getStringExtra(EXTRA_PREDICTION_PERIOD);
        String notes = getIntent().getStringExtra(EXTRA_NOTES);

        tvFoodName.setText(foodName == null ? "Không rõ món" : foodName);
        tvTrendType.setText("Loại xu hướng: " + mapTrendTypeName(trendType));
        tvConfidence.setText(String.format(Locale.getDefault(), "Độ tin cậy: %.0f%%", confidenceScore * 100));
        tvSalesTrend.setText(String.format(Locale.getDefault(), "Xu hướng doanh số: %.0f%%", salesTrend));
        tvSentimentTrend.setText(String.format(Locale.getDefault(), "Điểm cảm xúc: %.2f", sentimentTrend));

        String periodText = predictionPeriod == null || predictionPeriod.trim().isEmpty()
                ? "30 ngày"
                : predictionPeriod.replace("_", " ");
        tvPredictionPeriod.setText("Chu kỳ dự đoán: " + periodText);

        tvAiInsight.setText(buildTrendInsight(trendType, salesTrend, confidenceScore, notes));

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivFood);
        } else {
            ivFood.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private String mapTrendTypeName(String trendType) {
        if ("hot_seller".equals(trendType)) {
            return "Hot Seller";
        }
        if ("declining".equals(trendType)) {
            return "Declining";
        }
        if ("at_risk".equals(trendType)) {
            return "At Risk";
        }
        if ("stable".equals(trendType)) {
            return "Stable";
        }
        return "Không xác định";
    }

    private String buildTrendInsight(String trendType, double salesTrend, double confidenceScore, String notes) {
        String baseInsight;
        if ("hot_seller".equals(trendType)) {
            baseInsight = "Món có xu hướng tăng trưởng tốt. Nên ưu tiên hàng tồn và đẩy khuyến nghị trên trang chủ.";
        } else if ("at_risk".equals(trendType)) {
            baseInsight = "Món có dấu hiệu rủi ro cao. Cần rà soát chất lượng và phản hồi khách hàng ngay.";
        } else if ("declining".equals(trendType)) {
            baseInsight = "Món đang giảm sức hút. Có thể thử combo/ưu đãi ngắn hạn để kéo nhu cầu.";
        } else {
            baseInsight = "Món đang ổn định. Tiếp tục theo dõi dữ liệu và tối ưu nhẹ theo mùa.";
        }

        String salesText = String.format(Locale.getDefault(), " Xu hướng doanh số hiện tại: %.0f%%.", salesTrend);
        String confidenceText = String.format(Locale.getDefault(), " Độ tin cậy mô hình: %.0f%%.",
                confidenceScore * 100);

        if (notes == null || notes.trim().isEmpty()) {
            return baseInsight + salesText + confidenceText;
        }

        return baseInsight + salesText + confidenceText + " Ghi chú hệ thống: " + notes + ".";
    }
}
