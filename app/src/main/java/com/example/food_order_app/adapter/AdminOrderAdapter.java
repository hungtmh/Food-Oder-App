package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {
    private List<Order> orders = new ArrayList<>();
    private final Context context;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public AdminOrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() { return orders.size(); }

    public static String getStatusText(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đang xử lý";
            case "preparing": return "Đang chuẩn bị";
            case "delivering": return "Đang giao";
            case "delivered": return "Hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    public static int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        switch (status) {
            case "pending": return R.drawable.bg_status_pending;
            case "confirmed":
            case "preparing": return R.drawable.bg_status_unread;
            case "delivering": return R.drawable.bg_status_unread;
            case "delivered": return R.drawable.bg_status_delivered;
            case "cancelled": return R.drawable.bg_status_unavailable;
            default: return R.drawable.bg_status_pending;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvCustomer, tvDate, tvTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvOrderCode);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
        }

        void bind(Order order) {
            tvCode.setText(order.getOrderCode());
            tvCustomer.setText(order.getReceiverName());
            tvStatus.setText(getStatusText(order.getStatus()));
            tvStatus.setBackgroundResource(getStatusBackground(order.getStatus()));

            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTotal.setText(nf.format(order.getTotalAmount()) + " VNĐ");

            try {
                String dateStr = order.getCreatedAt();
                if (dateStr != null && dateStr.length() > 19) {
                    dateStr = dateStr.substring(0, 19);
                }
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = input.parse(dateStr);
                tvDate.setText(output.format(date));
            } catch (Exception e) {
                tvDate.setText(order.getCreatedAt());
            }

            itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }
    }
}
