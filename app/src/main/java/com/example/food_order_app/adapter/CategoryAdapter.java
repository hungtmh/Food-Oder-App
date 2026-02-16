package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        notifyItemChanged(old);
        notifyItemChanged(position);
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
        holder.tvName.setSelected(position == selectedPosition);

        if (position == selectedPosition) {
            holder.tvName.setBackgroundResource(R.drawable.bg_category_selected);
            holder.tvName.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.tvName.setBackgroundResource(R.drawable.bg_category_normal);
            holder.tvName.setTextColor(context.getResources().getColor(R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(holder.getAdapterPosition());
            if (listener != null) listener.onCategoryClick(cat, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
