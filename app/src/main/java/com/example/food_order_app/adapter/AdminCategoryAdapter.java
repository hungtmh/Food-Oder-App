package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Category;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();
    private final Context context;
    private final OnCategoryActionListener listener;
    private boolean allowEdit = true;

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
        void onToggleActive(Category category, boolean isActive);
        void onStartDrag(RecyclerView.ViewHolder holder);
    }

    public AdminCategoryAdapter(Context context, OnCategoryActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= categories.size() || toPosition >= categories.size()) {
            return;
        }
        Category moved = categories.remove(fromPosition);
        categories.add(toPosition, moved);
        notifyItemMoved(fromPosition, toPosition);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category cat = categories.get(position);
        holder.tvName.setText(resolveDisplayName(cat));
        
        String status = cat.isActive() ? "Bật" : "Tắt";
        holder.tvInfo.setText("STT:" + cat.getSortOrder() + " | " + status);
        holder.tvStats.setText(String.format(Locale.getDefault(),
            "M:%d | B:%d | DT7:%s",
            cat.getTotalFoods(), cat.getActiveFoods(), formatCompactVnd(cat.getRevenueLast7Days())));

        if (cat.getIconUrl() != null && !cat.getIconUrl().trim().isEmpty()) {
            Glide.with(context)
                    .load(cat.getIconUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(holder.imgIcon);
        } else {
            holder.imgIcon.setImageResource(R.drawable.placeholder_food);
        }

        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(cat.isActive());
        holder.switchActive.setEnabled(allowEdit);
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggleActive(cat, isChecked);
        });

        holder.btnEdit.setEnabled(allowEdit);
        holder.btnDelete.setEnabled(allowEdit);
        holder.btnDrag.setEnabled(allowEdit);
        int editTint = allowEdit ? R.color.primary : R.color.text_hint;
        int deleteTint = allowEdit ? R.color.error : R.color.text_hint;
        holder.btnEdit.setColorFilter(ContextCompat.getColor(context, editTint));
        holder.btnDelete.setColorFilter(ContextCompat.getColor(context, deleteTint));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(cat);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(cat);
        });

        holder.btnDrag.setOnTouchListener((v, event) -> {
            if (!allowEdit || listener == null) return false;
            listener.onStartDrag(holder);
            return false;
        });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, btnEdit, btnDelete, btnDrag;
        TextView tvName, tvInfo, tvStats;
        SwitchMaterial switchActive;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvInfo = itemView.findViewById(R.id.tvCategoryInfo);
            tvStats = itemView.findViewById(R.id.tvCategoryStats);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
            btnDrag = itemView.findViewById(R.id.btnDragCategory);
            switchActive = itemView.findViewById(R.id.switchCategoryActive);
        }
    }

    private String resolveDisplayName(Category category) {
        return category.getName();
    }

    private String formatCompactVnd(double amount) {
        if (amount >= 1_000_000_000d) {
            return String.format(Locale.getDefault(), "%.1fty", amount / 1_000_000_000d);
        }
        if (amount >= 1_000_000d) {
            return String.format(Locale.getDefault(), "%.1ftr", amount / 1_000_000d);
        }
        if (amount >= 1_000d) {
            return String.format(Locale.getDefault(), "%.1fk", amount / 1_000d);
        }
        return String.format(Locale.getDefault(), "%.0fđ", amount);
    }
}
