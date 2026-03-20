package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();
    private final Context context;
    private int selectedPosition = 0;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int old = selectedPosition;
        selectedPosition = position;
        if (old >= 0 && old < getItemCount()) notifyItemChanged(old);
        if (position >= 0 && position < getItemCount()) notifyItemChanged(position);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category cat = categories.get(position);
        holder.tvName.setText(cat.getName());
        boolean isSelected = position == selectedPosition;
        holder.tvName.setSelected(isSelected);

        if (isSelected) {
            holder.imgCategory.setBackgroundResource(R.drawable.bg_category_thumb_selected);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.primary));
        } else {
            holder.imgCategory.setBackgroundResource(R.drawable.bg_category_thumb_normal);
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }

        if (cat.getIconUrl() != null && !cat.getIconUrl().trim().isEmpty()) {
            Glide.with(context)
                    .load(cat.getIconUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(holder.imgCategory);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_placeholder)
                    .into(holder.imgCategory);
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            setSelectedPosition(adapterPosition);
            if (listener != null) listener.onCategoryClick(cat, adapterPosition);
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutItem;
        ImageView imgCategory;
        TextView tvName;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutItem = itemView.findViewById(R.id.layoutCategoryItem);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
