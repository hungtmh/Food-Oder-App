package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Food;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<Food> popularFoods = new ArrayList<>();
    private final Context context;
    private OnSliderClickListener listener;

    public interface OnSliderClickListener {
        void onSliderClick(Food food);
    }

    public SliderAdapter(Context context, OnSliderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFoods(List<Food> foods) {
        this.popularFoods = foods;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Food food = popularFoods.get(position);
        Glide.with(context)
                .load(food.getImageUrl())
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(holder.imgSlider);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSliderClick(food);
        });
    }

    @Override
    public int getItemCount() { return popularFoods.size(); }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSlider;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlider = itemView.findViewById(R.id.imgSlider);
        }
    }
}
