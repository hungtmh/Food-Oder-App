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
import com.example.food_order_app.model.AiRecommendationTask;
import com.example.food_order_app.model.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiComboFoodAdapter extends RecyclerView.Adapter<AiComboFoodAdapter.ViewHolder> {
    private final Context context;
    private List<AiRecommendationTask.AiRecommendedFood> foods = new ArrayList<>();
    private List<Food> allFoods = new ArrayList<>();
    private OnFoodRemoveListener removeListener;

    public interface OnFoodRemoveListener {
        void onFoodRemove(AiRecommendationTask.AiRecommendedFood food);
    }

    public AiComboFoodAdapter(Context context, OnFoodRemoveListener listener) {
        this.context = context;
        this.removeListener = listener;
    }

    public void setFoods(List<AiRecommendationTask.AiRecommendedFood> foods, List<Food> allFoods) {
        this.foods = foods != null ? foods : new ArrayList<>();
        this.allFoods = allFoods != null ? allFoods : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<AiRecommendationTask.AiRecommendedFood> getFoods() {
        return foods;
    }

    private Food findFoodById(String foodId) {
        if (foodId == null || allFoods == null) return null;
        for (Food f : allFoods) {
            if (foodId.equals(f.getId())) return f;
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ai_combo_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AiRecommendationTask.AiRecommendedFood item = foods.get(position);
        Food matchedFood = findFoodById(item.getFoodId());

        holder.tvFoodName.setText(item.getFoodName());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        double price = item.getPrice() != null ? item.getPrice() : 0;
        holder.tvFoodPrice.setText(nf.format(price) + " VNĐ");

        holder.tvFoodReason.setText(item.getReason() != null ? item.getReason() : "");

        if (matchedFood != null && matchedFood.getImageUrl() != null) {
            Glide.with(context)
                    .load(matchedFood.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.imgFood);
        } else {
            holder.imgFood.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvFoodPrice, tvFoodReason;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvFoodReason = itemView.findViewById(R.id.tvFoodReason);
        }
    }
}
