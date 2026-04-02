package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

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

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {
    private List<Order> orders = new ArrayList<>();
    private final Context context;
    private final OnOrderClickListener listener;
    private final SupabaseDbService dbService;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onOrderStatusChanged();
    }

    public AdminOrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.dbService = RetrofitClient.getDbService();
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
            case "processing": return "Chờ chế biến";
            case "delivering": return "Đang giao";
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
            case "delivering": return R.drawable.bg_status_delivering;
            case "served": return R.drawable.bg_status_delivered;
            case "cancelled": return R.drawable.bg_status_unavailable;
            default: return R.drawable.bg_status_pending;
        }
    }

    private void updateOrderStatus(Order order, String newStatus, String actionName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        dbService.updateOrderStatus("eq." + order.getId(), updates).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    order.setStatus(newStatus);
                    Toast.makeText(context, actionName + " thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Send notification to user
                    sendNotification(order, newStatus);
                    
                    if (listener != null) {
                        listener.onOrderStatusChanged();
                    }
                } else {
                    Toast.makeText(context, "Cập nhật thất bại (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(Order order, String newStatus) {
        String title = "Cập nhật đơn hàng " + order.getOrderCode();
        String message = "";
        
        switch (newStatus) {
            case "processing":
                message = "Đơn hàng của bạn đã được xác nhận và đang được chế biến.";
                break;
            case "delivering":
                message = "Đơn hàng của bạn đang được giao tới.";
                break;
            case "served":
                message = "Đơn hàng của bạn đã làm xong và sẵn sàng phục vụ/giao thành công!";
                break;
            case "cancelled":
                message = "Rất tiếc, đơn hàng của bạn đã bị hủy.";
                break;
            default:
                return;
        }

        Map<String, Object> notif = new HashMap<>();
        notif.put("user_id", order.getUserId());
        notif.put("order_id", order.getId());
        notif.put("order_code", order.getOrderCode());
        notif.put("title", title);
        notif.put("message", message);
        notif.put("is_read", false);

        dbService.createNotification(notif).enqueue(new Callback<List<com.example.food_order_app.model.Notification>>() {
            @Override
            public void onResponse(Call<List<com.example.food_order_app.model.Notification>> call, Response<List<com.example.food_order_app.model.Notification>> response) {
                // Silently succeed
            }

            @Override
            public void onFailure(Call<List<com.example.food_order_app.model.Notification>> call, Throwable t) {
                // Silently fail
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvCustomer, tvDate, tvTotal, tvFoodSummary, tvPhone, tvAddress;
        LinearLayout layoutQuickActions, layoutFoodImages, layoutPhone, layoutAddress;
        Button btnQuickConfirm, btnQuickServe, btnQuickCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvOrderCode);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvFoodSummary = itemView.findViewById(R.id.tvFoodSummary);
            tvPhone = itemView.findViewById(R.id.tvOrderPhone);
            tvAddress = itemView.findViewById(R.id.tvOrderAddress);
            layoutFoodImages = itemView.findViewById(R.id.layoutFoodImages);
            layoutPhone = itemView.findViewById(R.id.layoutPhone);
            layoutAddress = itemView.findViewById(R.id.layoutAddress);
            layoutQuickActions = itemView.findViewById(R.id.layoutQuickActions);
            btnQuickConfirm = itemView.findViewById(R.id.btnQuickConfirm);
            btnQuickServe = itemView.findViewById(R.id.btnQuickServe);
            btnQuickCancel = itemView.findViewById(R.id.btnQuickCancel);
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

            // Display phone
            if (order.getPhone() != null && !order.getPhone().isEmpty()) {
                tvPhone.setText(order.getPhone());
                layoutPhone.setVisibility(View.VISIBLE);
            } else {
                layoutPhone.setVisibility(View.GONE);
            }

            // Display address
            if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                tvAddress.setText(order.getAddress());
                layoutAddress.setVisibility(View.VISIBLE);
            } else {
                layoutAddress.setVisibility(View.GONE);
            }

            // Display food images and summary
            displayFoodItems(order);

            // Setup quick action buttons based on status
            setupQuickActions(order);

            itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }

        private void displayFoodItems(Order order) {
            layoutFoodImages.removeAllViews();

            List<OrderItem> items = order.getOrderItems();
            if (items == null || items.isEmpty()) {
                tvFoodSummary.setVisibility(View.GONE);
                return;
            }

            // Build food summary text: "Bò cuộn phô mai x2, Trà sữa x1"
            StringBuilder summary = new StringBuilder();
            int totalQty = 0;
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                if (i > 0) summary.append(", ");
                summary.append(item.getFoodName()).append(" x").append(item.getQuantity());
                totalQty += item.getQuantity();

                // Add food image (max 4 images to save space)
                if (i < 4) {
                    ImageView imgView = new ImageView(context);
                    int size = dpToPx(52);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMarginEnd(dpToPx(6));
                    imgView.setLayoutParams(params);
                    imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    String imageUrl = item.getFoodImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(context)
                                .load(imageUrl)
                                .transform(new CenterCrop(), new RoundedCorners(dpToPx(8)))
                                .placeholder(R.drawable.placeholder_food)
                                .into(imgView);
                    } else {
                        imgView.setImageResource(R.drawable.placeholder_food);
                    }
                    layoutFoodImages.addView(imgView);
                }
            }

            // If more than 4 items, show "+N" indicator
            if (items.size() > 4) {
                TextView tvMore = new TextView(context);
                int size = dpToPx(52);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                tvMore.setLayoutParams(params);
                tvMore.setGravity(android.view.Gravity.CENTER);
                tvMore.setText("+" + (items.size() - 4));
                tvMore.setTextSize(14);
                tvMore.setTextColor(context.getResources().getColor(R.color.text_secondary));
                tvMore.setBackgroundResource(R.drawable.bg_order_card);
                layoutFoodImages.addView(tvMore);
            }

            summary.append(" (").append(totalQty).append(" món)");
            tvFoodSummary.setText(summary.toString());
            tvFoodSummary.setVisibility(View.VISIBLE);
        }

        private void setupQuickActions(Order order) {
            layoutQuickActions.setVisibility(View.GONE);
            btnQuickConfirm.setVisibility(View.GONE);
            btnQuickServe.setVisibility(View.GONE);
            btnQuickCancel.setVisibility(View.GONE);

            String status = order.getStatus();
            if (status == null) return;

            switch (status) {
                case "pending":
                    layoutQuickActions.setVisibility(View.VISIBLE);
                    btnQuickConfirm.setVisibility(View.VISIBLE);
                    btnQuickCancel.setVisibility(View.VISIBLE);

                    btnQuickConfirm.setOnClickListener(v -> {
                        new AlertDialog.Builder(context)
                                .setTitle("Xác nhận đơn hàng")
                                .setMessage("Xác nhận đơn " + order.getOrderCode() + "?\nĐơn sẽ chuyển sang \"Chờ chế biến\".")
                                .setPositiveButton("Xác nhận", (d, w) -> updateOrderStatus(order, "processing", "Xác nhận đơn"))
                                .setNegativeButton("Không", null)
                                .show();
                    });

                    btnQuickCancel.setOnClickListener(v -> {
                        new AlertDialog.Builder(context)
                                .setTitle("Hủy đơn hàng")
                                .setMessage("Hủy đơn " + order.getOrderCode() + "?\nĐơn sau khi hủy không thể khôi phục.")
                                .setPositiveButton("Hủy đơn", (d, w) -> updateOrderStatus(order, "cancelled", "Hủy đơn"))
                                .setNegativeButton("Không", null)
                                .show();
                    });
                    break;

                case "processing":
                    layoutQuickActions.setVisibility(View.VISIBLE);
                    btnQuickServe.setVisibility(View.VISIBLE);
                    btnQuickCancel.setVisibility(View.VISIBLE);

                    String orderType = order.getOrderType();
                    if (orderType == null || orderType.isEmpty()) orderType = "delivery";

                    if ("delivery".equals(orderType)) {
                        btnQuickServe.setText("Giao hàng");
                        btnQuickServe.setOnClickListener(v -> {
                            new AlertDialog.Builder(context)
                                    .setTitle("Giao đơn hàng")
                                    .setMessage("Chuyển trạng thái đơn sang Đang giao?")
                                    .setPositiveButton("Giao hàng", (d, w) -> updateOrderStatus(order, "delivering", "Giao hàng"))
                                    .setNegativeButton("Không", null)
                                    .show();
                        });
                    } else {
                        btnQuickServe.setText("Phục vụ");
                        btnQuickServe.setOnClickListener(v -> {
                            new AlertDialog.Builder(context)
                                    .setTitle("Phục vụ đơn hàng")
                                    .setMessage("Xác nhận đã phục vụ đơn " + order.getOrderCode() + "?")
                                    .setPositiveButton("Đã phục vụ", (d, w) -> updateOrderStatus(order, "served", "Phục vụ đơn"))
                                    .setNegativeButton("Không", null)
                                    .show();
                        });
                    }

                    btnQuickCancel.setOnClickListener(v -> {
                        new AlertDialog.Builder(context)
                                .setTitle("Hủy đơn hàng")
                                .setMessage("Hủy đơn " + order.getOrderCode() + "?\nĐơn sau khi hủy không thể khôi phục.")
                                .setPositiveButton("Hủy đơn", (d, w) -> updateOrderStatus(order, "cancelled", "Hủy đơn"))
                                .setNegativeButton("Không", null)
                                .show();
                    });
                    break;

                case "delivering":
                    layoutQuickActions.setVisibility(View.VISIBLE);
                    btnQuickServe.setVisibility(View.VISIBLE);
                    btnQuickCancel.setVisibility(View.GONE);

                    btnQuickServe.setText("Đã giao");
                    btnQuickServe.setOnClickListener(v -> {
                        new AlertDialog.Builder(context)
                                .setTitle("Hoàn thành đơn hàng")
                                .setMessage("Xác nhận đã giao thành công đơn " + order.getOrderCode() + "?")
                                .setPositiveButton("Đã giao", (d, w) -> updateOrderStatus(order, "served", "Hoàn thành đơn"))
                                .setNegativeButton("Không", null)
                                .show();
                    });
                    break;

                default:
                    break;
            }
        }

        private int dpToPx(int dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }
    }
}
