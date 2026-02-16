package com.example.food_order_app.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.ViewHolder> {
    private List<Food> foods = new ArrayList<>();
    private final Context context;
    private final OnAdminFoodListener listener;

    public interface OnAdminFoodListener {
        void onEditFood(Food food);
        void onDeleteFood(Food food);
        void onFoodClick(Food food);
    }

    public AdminFoodAdapter(Context context, OnAdminFoodListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(foods.get(position));
    }

    @Override
    public int getItemCount() { return foods.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName, tvOriginalPrice, tvPrice, tvPopular, tvDesc;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgAdminFood);
            tvName = itemView.findViewById(R.id.tvAdminFoodName);
            tvOriginalPrice = itemView.findViewById(R.id.tvAdminOriginalPrice);
            tvPrice = itemView.findViewById(R.id.tvAdminFoodPrice);
            tvPopular = itemView.findViewById(R.id.tvAdminFoodPopular);
            tvDesc = itemView.findViewById(R.id.tvAdminFoodDesc);
            btnEdit = itemView.findViewById(R.id.btnEditFood);
            btnDelete = itemView.findViewById(R.id.btnDeleteFood);
        }

        void bind(Food food) {
            tvName.setText(food.getName());
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

            if (food.getDiscountPercent() > 0) {
                tvOriginalPrice.setText(nf.format(food.getPrice()) + " VNĐ");
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvPrice.setText(nf.format(food.getDiscountedPrice()) + " VNĐ");
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvPrice.setText(nf.format(food.getPrice()) + " VNĐ");
            }

            tvPopular.setText("Phổ biến: " + (food.isPopular() ? "Có" : "Không"));
            tvDesc.setText(food.getDescription());

            Glide.with(context).load(food.getImageUrl())
                    .placeholder(R.drawable.bg_button_primary)
                    .into(imgFood);

            itemView.setOnClickListener(v -> listener.onFoodClick(food));
            btnEdit.setOnClickListener(v -> listener.onEditFood(food));
            btnDelete.setOnClickListener(v -> listener.onDeleteFood(food));
        }
    }
}
