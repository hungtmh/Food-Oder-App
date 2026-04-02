package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.FoodSentimentStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SentimentStatsAdapter extends RecyclerView.Adapter<SentimentStatsAdapter.ViewHolder> {
    private List<FoodSentimentStats> stats = new ArrayList<>();
    private Map<String, String> insights = new HashMap<>();
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodSentimentStats stat);
    }

    public SentimentStatsAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setStats(List<FoodSentimentStats> stats) {
        this.stats = stats;
        notifyDataSetChanged();
    }

    public void setInsights(Map<String, String> insights) {
        this.insights = insights != null ? insights : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sentiment_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodSentimentStats stat = stats.get(position);

        // Food info
        holder.tvFoodName.setText(stat.getFoodName());
        holder.tvReviewCount.setText(stat.getTotalReviews() + " đánh giá");

        // Load image
        if (stat.getImageUrl() != null && !stat.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(stat.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(holder.ivFoodImage);
        }

        // Sentiment info
        String dominantSentiment = stat.getDominantSentiment();
        holder.tvSentimentEmoji.setText(getSentimentEmoji(dominantSentiment));
        holder.tvSentimentName.setText(getSentimentName(dominantSentiment));

        double percent = 0;
        if (dominantSentiment.equals("positive")) {
            percent = stat.getPositivePercent();
        } else if (dominantSentiment.equals("negative")) {
            percent = stat.getNegativePercent();
        } else {
            percent = stat.getNeutralPercent();
        }
        holder.tvSentimentPercent.setText(String.format(Locale.getDefault(), "%.0f%%", percent));
        holder.tvSentimentPercent.setTextColor(getSentimentColor(dominantSentiment));

        // Rating
        holder.tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", stat.getAvgRating()));

        String insight = insights.get(stat.getFoodId());
        if (insight == null || insight.trim().isEmpty()) {
            holder.tvAiInsight.setText("Đang tạo insight AI...");
        } else {
            holder.tvAiInsight.setText(insight.trim());
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(stat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    private String getSentimentEmoji(String sentiment) {
        switch (sentiment) {
            case "positive":
                return "😊";
            case "negative":
                return "😞";
            case "neutral":
            default:
                return "😐";
        }
    }

    private String getSentimentName(String sentiment) {
        switch (sentiment) {
            case "positive":
                return "Tich cuc";
            case "negative":
                return "Tieu cuc";
            case "neutral":
            default:
                return "Trung tinh";
        }
    }

    private int getSentimentColor(String sentiment) {
        switch (sentiment) {
            case "positive":
                return 0xFF4CAF50;
            case "negative":
                return 0xFFF44336;
            case "neutral":
            default:
                return 0xFFFF9800;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvSentimentEmoji, tvSentimentName, tvSentimentPercent;
        TextView tvReviewCount, tvAvgRating, tvAiInsight;

        ViewHolder(View view) {
            super(view);
            ivFoodImage = view.findViewById(R.id.ivFoodImage);
            tvFoodName = view.findViewById(R.id.tvFoodName);
            tvSentimentEmoji = view.findViewById(R.id.tvSentimentEmoji);
            tvSentimentName = view.findViewById(R.id.tvSentimentName);
            tvSentimentPercent = view.findViewById(R.id.tvSentimentPercent);
            tvReviewCount = view.findViewById(R.id.tvReviewCount);
            tvAvgRating = view.findViewById(R.id.tvAvgRating);
            tvAiInsight = view.findViewById(R.id.tvAiInsight);
        }
    }
}
