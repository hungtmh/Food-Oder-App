package com.example.food_order_app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.FoodTrend;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodTrendAdapter extends RecyclerView.Adapter<FoodTrendAdapter.ViewHolder> {
    private List<FoodTrend> trends = new ArrayList<>();
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodTrend trend);
    }

    public FoodTrendAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTrends(List<FoodTrend> trends) {
        this.trends = trends;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_trend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodTrend trend = trends.get(position);

        // Food info
        if (trend.getFood() != null) {
            holder.tvFoodName.setText(trend.getFood().getName());
            
            // Load image
            if (trend.getFood().getImageUrl() != null && !trend.getFood().getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(trend.getFood().getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(holder.ivFoodImage);
            }
        }

        // Trend type
        holder.tvTrendType.setText(trend.getTrendTypeName());
        holder.tvTrendType.setTextColor(trend.getTrendColor());

        // Confidence
        holder.tvConfidence.setText(String.format(Locale.getDefault(), "%.0f%%", 
                trend.getConfidenceScore() * 100));

        // Sales trend
        double salesTrend = trend.getSalesTrend();
        String salesTrendText;
        int salesTrendColor;
        if (salesTrend > 0) {
            salesTrendText = String.format(Locale.getDefault(), "+%.0f%%", salesTrend);
            salesTrendColor = Color.parseColor("#4CAF50"); // Green
        } else if (salesTrend < 0) {
            salesTrendText = String.format(Locale.getDefault(), "%.0f%%", salesTrend);
            salesTrendColor = Color.parseColor("#F44336"); // Red
        } else {
            salesTrendText = "0%";
            salesTrendColor = Color.parseColor("#9E9E9E"); // Grey
        }
        holder.tvSalesTrend.setText(salesTrendText);
        holder.tvSalesTrend.setTextColor(salesTrendColor);

        // Sentiment trend
        holder.tvSentimentTrend.setText(String.format(Locale.getDefault(), "%.2f", 
                trend.getSentimentTrend()));

        // Notes
        if (trend.getNotes() != null && !trend.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(trend.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Prediction period
        String periodText = "Dự đoán cho 30 ngày tới";
        if (trend.getPredictionPeriod() != null) {
            periodText = "Dự đoán cho " + trend.getPredictionPeriod().replace("_", " ");
        }
        holder.tvPredictionPeriod.setText(periodText);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(trend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvTrendType, tvConfidence;
        TextView tvSalesTrend, tvSentimentTrend, tvNotes, tvPredictionPeriod;

        ViewHolder(View view) {
            super(view);
            ivFoodImage = view.findViewById(R.id.ivFoodImage);
            tvFoodName = view.findViewById(R.id.tvFoodName);
            tvTrendType = view.findViewById(R.id.tvTrendType);
            tvConfidence = view.findViewById(R.id.tvConfidence);
            tvSalesTrend = view.findViewById(R.id.tvSalesTrend);
            tvSentimentTrend = view.findViewById(R.id.tvSentimentTrend);
            tvNotes = view.findViewById(R.id.tvNotes);
            tvPredictionPeriod = view.findViewById(R.id.tvPredictionPeriod);
        }
    }
}
