package com.example.food_order_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.model.CategoryProductStat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryProductStatAdapter extends RecyclerView.Adapter<CategoryProductStatAdapter.ProductStatViewHolder> {

    private final List<CategoryProductStat> items = new ArrayList<>();

    public void setItems(List<CategoryProductStat> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductStatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_product_stat, parent, false);
        return new ProductStatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductStatViewHolder holder, int position) {
        CategoryProductStat item = items.get(position);
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvProductName.setText(item.getProductName());

        String remainingText = item.getRemainingQuantity() == null ? "chưa thiết lập" : String.valueOf(item.getRemainingQuantity());
        holder.tvProductStats.setText("Đã bán: " + item.getSoldQuantity()
                + " | Doanh thu: " + nf.format(item.getRevenue()) + "đ"
                + " | Còn lại: " + remainingText);

        holder.tvProductStatus.setText(item.isActive() ? "Đang bán" : "Đang ẩn");
        holder.tvProductStatus.setBackgroundResource(item.isActive() ? R.drawable.bg_status_available : R.drawable.bg_status_unavailable);
        int statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.white);
        holder.tvProductStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductStatViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductStats;
        TextView tvProductStatus;

        ProductStatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductStats = itemView.findViewById(R.id.tvProductStats);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
        }
    }
}
