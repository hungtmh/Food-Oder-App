package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopFoodAdapter extends RecyclerView.Adapter<TopFoodAdapter.ViewHolder> {

    public static class TopFood {
        public String name;
        public int quantity;
        public double revenue;

        public TopFood(String name, int quantity, double revenue) {
            this.name = name;
            this.quantity = quantity;
            this.revenue = revenue;
        }
    }

    private List<TopFood> items = new ArrayList<>();
    private final Context context;

    public TopFoodAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<TopFood> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position + 1);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvQty, tvRevenue;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvTopRank);
            tvName = itemView.findViewById(R.id.tvTopFoodName);
            tvQty = itemView.findViewById(R.id.tvTopQty);
            tvRevenue = itemView.findViewById(R.id.tvTopRevenue);
        }

        void bind(TopFood item, int rank) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvRank.setText(String.valueOf(rank));
            tvName.setText(item.name);
            tvQty.setText(item.quantity + " phần");
            tvRevenue.setText(nf.format(item.revenue) + "đ");
        }
    }
}
