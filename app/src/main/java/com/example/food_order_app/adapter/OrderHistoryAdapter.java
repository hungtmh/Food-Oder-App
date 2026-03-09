package com.example.food_order_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.controller.CartActivity;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    private static final String TAG = "OrderHistoryAdapter";

    private List<Order> orders = new ArrayList<>();
    private final Context context;
    private final SupabaseDbService dbService;
    private final NumberFormat nf;
    private OnOrderCancelledListener cancelListener;

    // Cache loaded order items
    private final Map<String, List<OrderItem>> orderItemsCache = new HashMap<>();
    // Track expanded state
    private final Map<String, Boolean> expandedState = new HashMap<>();

    public interface OnOrderCancelledListener {
        void onOrderCancelled();
    }

    public OrderHistoryAdapter(Context context, OnOrderCancelledListener listener) {
        this.context = context;
        this.dbService = RetrofitClient.getDbService();
        this.nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.cancelListener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        this.orderItemsCache.clear();
        this.expandedState.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    // ============ STATUS HELPERS ============

    public static String getStatusText(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "processing": return "Chờ chế biến";
            case "served": return "Đã phục vụ";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    public static int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        switch (status) {
            case "pending": return R.drawable.bg_status_pending;
            case "processing": return R.drawable.bg_status_processing;
            case "served": return R.drawable.bg_status_delivered;
            case "cancelled": return R.drawable.bg_status_cancelled;
            default: return R.drawable.bg_status_pending;
        }
    }

    public static int getStatusTextColor(Context ctx, String status) {
        if (status == null) return ctx.getColor(R.color.text_secondary);
        switch (status) {
            case "pending": return ctx.getColor(R.color.warning);
            case "processing": return ctx.getColor(R.color.info);
            case "served": return ctx.getColor(R.color.success);
            case "cancelled": return ctx.getColor(R.color.error);
            default: return ctx.getColor(R.color.text_secondary);
        }
    }

    // ============ VIEW HOLDER ============

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvOrderStatus, tvOrderDate, tvOrderAddress;
        TextView tvPaymentMethod, tvOrderTotal, tvToggleDetails;
        LinearLayout layoutOrderItems;
        RecyclerView rvOrderItems;
        Button btnCancelOrder, btnReorder;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderAddress = itemView.findViewById(R.id.tvOrderAddress);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvToggleDetails = itemView.findViewById(R.id.tvToggleDetails);
            layoutOrderItems = itemView.findViewById(R.id.layoutOrderItems);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnReorder = itemView.findViewById(R.id.btnReorder);

            rvOrderItems.setLayoutManager(new LinearLayoutManager(context));
        }

        void bind(Order order) {
            // Order code
            tvOrderCode.setText(order.getOrderCode() != null ? order.getOrderCode() : "Đơn hàng");

            // Status
            String status = order.getStatus();
            tvOrderStatus.setText(getStatusText(status));
            tvOrderStatus.setBackgroundResource(getStatusBackground(status));
            tvOrderStatus.setTextColor(getStatusTextColor(context, status));

            // Date
            tvOrderDate.setText(formatDate(order.getCreatedAt()));

            // Address
            String addressText = order.getReceiverName();
            if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                addressText += " - " + order.getAddress();
            }
            tvOrderAddress.setText(addressText);

            // Payment method
            String payment = "cod".equals(order.getPaymentMethod())
                    ? "💵 Thanh toán khi nhận hàng"
                    : "🏦 Chuyển khoản ngân hàng";
            tvPaymentMethod.setText(payment);

            // Total
            tvOrderTotal.setText(nf.format(order.getTotalAmount()) + " VNĐ");

            // Cancel button: only visible for pending status
            if ("pending".equals(status)) {
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setOnClickListener(v -> showCancelDialog(order));
            } else {
                btnCancelOrder.setVisibility(View.GONE);
            }

            // Reorder button
            btnReorder.setOnClickListener(v -> reorder(order));

            // Expand/collapse state
            boolean isExpanded = Boolean.TRUE.equals(expandedState.get(order.getId()));
            layoutOrderItems.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            tvToggleDetails.setText(isExpanded ? "Thu gọn ▲" : "Xem chi tiết ▼");

            // Toggle click
            tvToggleDetails.setOnClickListener(v -> {
                boolean nowExpanded = !Boolean.TRUE.equals(expandedState.get(order.getId()));
                expandedState.put(order.getId(), nowExpanded);

                layoutOrderItems.setVisibility(nowExpanded ? View.VISIBLE : View.GONE);
                tvToggleDetails.setText(nowExpanded ? "Thu gọn ▲" : "Xem chi tiết ▼");

                if (nowExpanded && !orderItemsCache.containsKey(order.getId())) {
                    loadOrderItems(order.getId());
                }
            });

            // Load items if expanded and cached
            if (isExpanded && orderItemsCache.containsKey(order.getId())) {
                setOrderItemsAdapter(orderItemsCache.get(order.getId()));
            }
        }

        private void showCancelDialog(Order order) {
            new AlertDialog.Builder(context)
                    .setTitle("Hủy đơn hàng")
                    .setMessage("Bạn có chắc muốn hủy đơn hàng " + order.getOrderCode() + "?")
                    .setPositiveButton("Hủy đơn", (dialog, which) -> cancelOrder(order))
                    .setNegativeButton("Không", null)
                    .show();
        }

        private void cancelOrder(Order order) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "cancelled");

            dbService.updateOrderStatus("eq." + order.getId(), updates)
                    .enqueue(new Callback<List<Order>>() {
                        @Override
                        public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                            if (response.isSuccessful()) {
                                order.setStatus("cancelled");
                                if (cancelListener != null) {
                                    cancelListener.onOrderCancelled();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Order>> call, Throwable t) {
                            Log.e(TAG, "cancelOrder failed: " + t.getMessage());
                        }
                    });
        }

        private void loadOrderItems(String orderId) {
            dbService.getOrderItems("eq." + orderId, "*").enqueue(new Callback<List<OrderItem>>() {
                @Override
                public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<OrderItem> items = response.body();
                        orderItemsCache.put(orderId, items);
                        setOrderItemsAdapter(items);
                    }
                }

                @Override
                public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                    Log.e(TAG, "loadOrderItems failed: " + t.getMessage());
                }
            });
        }

        private void reorder(Order order) {
            SessionManager sm = new SessionManager(context);
            String userId = sm.getUserId();
            if (userId == null) {
                Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            btnReorder.setEnabled(false);
            // Use cached items if available, else load from API
            if (orderItemsCache.containsKey(order.getId())) {
                getOrCreateCartAndAdd(userId, orderItemsCache.get(order.getId()));
            } else {
                dbService.getOrderItems("eq." + order.getId(), "*").enqueue(new Callback<List<OrderItem>>() {
                    @Override
                    public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            getOrCreateCartAndAdd(userId, response.body());
                        } else {
                            btnReorder.setEnabled(true);
                            Toast.makeText(context, "Không tìm thấy món trong đơn hàng", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                        btnReorder.setEnabled(true);
                        Toast.makeText(context, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void getOrCreateCartAndAdd(String userId, List<OrderItem> items) {
            dbService.getCart("eq." + userId).enqueue(new Callback<List<Cart>>() {
                @Override
                public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        addAllToCart(response.body().get(0).getId(), items);
                    } else {
                        Map<String, String> cartData = new HashMap<>();
                        cartData.put("user_id", userId);
                        dbService.createCart(cartData).enqueue(new Callback<List<Cart>>() {
                            @Override
                            public void onResponse(Call<List<Cart>> c, Response<List<Cart>> r) {
                                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                                    addAllToCart(r.body().get(0).getId(), items);
                                } else {
                                    btnReorder.setEnabled(true);
                                    Toast.makeText(context, "Lỗi tạo giỏ hàng", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<List<Cart>> c, Throwable t) {
                                btnReorder.setEnabled(true);
                            }
                        });
                    }
                }
                @Override
                public void onFailure(Call<List<Cart>> call, Throwable t) {
                    btnReorder.setEnabled(true);
                }
            });
        }

        private void addAllToCart(String cartId, List<OrderItem> items) {
            int[] done = {0};
            int total = items.size();
            for (OrderItem item : items) {
                Map<String, Object> data = new HashMap<>();
                data.put("cart_id", cartId);
                data.put("food_id", item.getFoodId());
                data.put("quantity", item.getQuantity());
                dbService.addCartItem(data).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        done[0]++;
                        if (done[0] == total) navigateToCart();
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        done[0]++;
                        if (done[0] == total) navigateToCart();
                    }
                });
            }
        }

        private void navigateToCart() {
            btnReorder.setEnabled(true);
            Toast.makeText(context, "Đã thêm vào giỏ hàng! Kiểm tra và đặt lại.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, CartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void setOrderItemsAdapter(List<OrderItem> items) {
            OrderItemAdapter adapter = new OrderItemAdapter(context);
            adapter.setItems(items);
            rvOrderItems.setAdapter(adapter);
        }

        private String formatDate(String isoDate) {
            if (isoDate == null) return "";
            try {
                SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFmt = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                String raw = isoDate;
                if (raw.contains(".")) raw = raw.substring(0, raw.indexOf('.'));
                if (raw.contains("+")) raw = raw.substring(0, raw.indexOf('+'));
                Date date = inputFmt.parse(raw);
                return outputFmt.format(date);
            } catch (Exception e) {
                return isoDate;
            }
        }
    }
}
