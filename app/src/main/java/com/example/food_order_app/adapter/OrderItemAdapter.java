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
import com.example.food_order_app.model.OrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private List<OrderItem> items = new ArrayList<>();
    private final Context context;

    public OrderItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView tvName, tvPrice, tvQty, tvSubtotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgOrderItem);
            tvName = itemView.findViewById(R.id.tvOrderItemName);
            tvPrice = itemView.findViewById(R.id.tvOrderItemPrice);
            tvQty = itemView.findViewById(R.id.tvOrderItemQty);
            tvSubtotal = itemView.findViewById(R.id.tvOrderItemSubtotal);
        }

        void bind(OrderItem item) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvName.setText(item.getFoodName());
            tvPrice.setText(nf.format(item.getPrice()) + " VNĐ");
            tvQty.setText("x" + item.getQuantity());
            tvSubtotal.setText(nf.format(item.getSubtotal()) + " VNĐ");

            Glide.with(context).load(item.getFoodImage())
                    .placeholder(R.drawable.bg_button_primary)
                    .into(imgItem);
        }
    }
}
