package com.example.food_order_app.adapter;

import android.content.Context;
import android.graphics.Paint;
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

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<Food> foods = new ArrayList<>();
    private final Context context;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public FoodAdapter(Context context, OnFoodClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foods.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() { return foods.size(); }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName, tvOriginalPrice, tvDiscountedPrice, tvDiscount;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscountedPrice = itemView.findViewById(R.id.tvDiscountedPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
        }

        void bind(Food food) {
            tvName.setText(food.getName());
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

            if (food.getDiscountPercent() > 0) {
                tvOriginalPrice.setText(nf.format(food.getPrice()) + " VNĐ");
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvDiscountedPrice.setText(nf.format(food.getDiscountedPrice()) + " VNĐ");
                tvDiscount.setText("Giảm " + food.getDiscountPercent() + "%");
                tvDiscount.setVisibility(View.VISIBLE);
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountedPrice.setText(nf.format(food.getPrice()) + " VNĐ");
                tvDiscount.setVisibility(View.GONE);
            }

            Glide.with(context).load(food.getImageUrl())
                    .placeholder(R.drawable.bg_button_primary)
                    .into(imgFood);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFoodClick(food);
            });
        }
    }
}
