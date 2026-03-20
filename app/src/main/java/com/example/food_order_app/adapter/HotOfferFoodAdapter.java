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
import com.example.food_order_app.model.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotOfferFoodAdapter extends RecyclerView.Adapter<HotOfferFoodAdapter.ViewHolder> {
    private final Context context;
    private final OnFoodClickListener listener;
    private final List<Food> foods = new ArrayList<>();

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public HotOfferFoodAdapter(Context context, OnFoodClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFoods(List<Food> items) {
        foods.clear();
        if (items != null) foods.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hot_offer_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);
        holder.tvFoodName.setText(food.getName());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        double price = food.getDiscountPercent() > 0 ? food.getDiscountedPrice() : food.getPrice();
        holder.tvFoodPrice.setText(nf.format(price) + "đ");
        holder.tvFoodRating.setText(String.format(Locale.US, "%.1f", food.getAvgRating()));

        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.imgFood);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFoodClick(food);
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName;
        TextView tvFoodPrice;
        TextView tvFoodRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvFoodRating = itemView.findViewById(R.id.tvFoodRating);
        }
    }
}
