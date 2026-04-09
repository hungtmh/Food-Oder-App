package com.example.food_order_app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();
    private final Context context;
    private final OnCategoryActionListener listener;
    private boolean allowEdit = true;

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
        void onCategoryClick(Category category);
        void onToggleActive(Category category, boolean isActive);
        void onStartDrag(RecyclerView.ViewHolder holder);
        void onMoreOptions(Category category, View anchorView);
        void onSelectionChanged(int selectedCount);
    }

    private final Set<String> selectedIds = new HashSet<>();

    public AdminCategoryAdapter(Context context, OnCategoryActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        selectedIds.retainAll(extractIds(categories));
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
        notifyDataSetChanged();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public List<Category> getSelectedCategories() {
        List<Category> selected = new ArrayList<>();
        for (Category category : categories) {
            if (category.getId() != null && selectedIds.contains(category.getId())) {
                selected.add(category);
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedIds.clear();
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
        notifyDataSetChanged();
    }

    public void toggleSelectAll(boolean selectAll) {
        selectedIds.clear();
        if (selectAll) {
            for (Category category : categories) {
                if (category.getId() != null) {
                    selectedIds.add(category.getId());
                }
            }
        }
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
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
        holder.tvName.setText(String.format(Locale.getDefault(), "%s (%d món)", resolveDisplayName(cat), cat.getTotalFoods()));
        
        String status = cat.isActive() ? "Bật" : "Tắt";
        holder.tvInfo.setText("STT:" + cat.getSortOrder() + " | " + status);
        holder.tvStats.setVisibility(View.GONE);

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

        boolean isSelected = cat.getId() != null && selectedIds.contains(cat.getId());
        holder.itemView.setAlpha(isSelected ? 0.82f : 1f);
        holder.itemView.setBackgroundColor(isSelected ? Color.parseColor("#EAF4FF") : Color.TRANSPARENT);

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setEnabled(allowEdit);
        holder.cbSelect.setChecked(isSelected);
        holder.cbSelect.setOnCheckedChangeListener((buttonView, checked) -> {
            if (!allowEdit || cat.getId() == null) {
                return;
            }
            if (checked) {
                selectedIds.add(cat.getId());
            } else {
                selectedIds.remove(cat.getId());
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size());
            }
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(cat);
            }
        });

        holder.btnDrag.setEnabled(allowEdit);
        int moreTint = allowEdit ? R.color.text_primary : R.color.text_hint;
        holder.btnMore.setColorFilter(ContextCompat.getColor(context, moreTint));
        holder.btnMore.setEnabled(allowEdit);
        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreOptions(cat, holder.btnMore);
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
        ImageView imgIcon, btnMore, btnDrag;
        TextView tvName, tvInfo, tvStats;
        SwitchMaterial switchActive;
        CheckBox cbSelect;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvInfo = itemView.findViewById(R.id.tvCategoryInfo);
            tvStats = itemView.findViewById(R.id.tvCategoryStats);
            btnMore = itemView.findViewById(R.id.btnCategoryMore);
            btnDrag = itemView.findViewById(R.id.btnDragCategory);
            switchActive = itemView.findViewById(R.id.switchCategoryActive);
            cbSelect = itemView.findViewById(R.id.cbCategorySelect);
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

    private Set<String> extractIds(List<Category> list) {
        Set<String> ids = new HashSet<>();
        for (Category category : list) {
            if (category.getId() != null) {
                ids.add(category.getId());
            }
        }
        return ids;
    }

}
