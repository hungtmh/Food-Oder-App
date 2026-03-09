package com.example.food_order_app.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.OrderItemAdapter;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvOrderCode, tvCustomer, tvPhone, tvAddress, tvPayment, tvNote;
    private TextView tvSubtotal, tvDiscount, tvTotal, tvDate;
    private TextView tvCurrentStatus, tvStatusMessage;
    private LinearLayout layoutActionButtons;
    private Button btnConfirmOrder, btnServeOrder, btnCancelOrder;
    private RecyclerView rvItems;

    private SupabaseDbService dbService;
    private OrderItemAdapter itemAdapter;
    private String orderId;
    private String currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        dbService = RetrofitClient.getDbService();
        initViews();
        loadData();
        loadOrderItems();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackOrder);
        tvOrderCode = findViewById(R.id.tvDetailOrderCode);
        tvCustomer = findViewById(R.id.tvDetailCustomerName);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvAddress = findViewById(R.id.tvDetailAddress);
        tvPayment = findViewById(R.id.tvDetailPayment);
        tvNote = findViewById(R.id.tvDetailNote);
        tvSubtotal = findViewById(R.id.tvDetailSubtotal);
        tvDiscount = findViewById(R.id.tvDetailDiscount);
        tvTotal = findViewById(R.id.tvDetailTotal);
        tvDate = findViewById(R.id.tvDetailOrderDate);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnServeOrder = findViewById(R.id.btnServeOrder);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        rvItems = findViewById(R.id.rvOrderItems);

        itemAdapter = new OrderItemAdapter(this);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);

        btnBack.setOnClickListener(v -> finish());

        // Confirm: pending -> processing
        btnConfirmOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đơn hàng")
                    .setMessage("Bạn có chắc muốn xác nhận đơn hàng này?\nĐơn hàng sẽ chuyển sang trạng thái \"Chờ chế biến\".")
                    .setPositiveButton("Xác nhận", (dialog, which) -> updateStatus("processing"))
                    .setNegativeButton("Không", null)
                    .show();
        });

        // Serve: processing -> served
        btnServeOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Phục vụ đơn hàng")
                    .setMessage("Xác nhận đã phục vụ đơn hàng này?")
                    .setPositiveButton("Đã phục vụ", (dialog, which) -> updateStatus("served"))
                    .setNegativeButton("Không", null)
                    .show();
        });

        // Cancel: pending/processing -> cancelled
        btnCancelOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hủy đơn hàng")
                    .setMessage("Bạn có chắc muốn hủy đơn hàng này?\nĐơn hàng sau khi hủy sẽ không thể khôi phục.")
                    .setPositiveButton("Hủy đơn", (dialog, which) -> updateStatus("cancelled"))
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    private void loadData() {
        orderId = getIntent().getStringExtra("order_id");
        currentStatus = getIntent().getStringExtra("order_status");

        tvOrderCode.setText(getIntent().getStringExtra("order_code"));
        tvCustomer.setText("Tên: " + getIntent().getStringExtra("order_customer"));
        tvPhone.setText("SĐT: " + getIntent().getStringExtra("order_phone"));
        tvAddress.setText("Địa chỉ: " + getIntent().getStringExtra("order_address"));

        String payment = getIntent().getStringExtra("order_payment");
        tvPayment.setText("Thanh toán: " + ("cod".equals(payment) ? "Tiền mặt (COD)" : "Chuyển khoản"));

        String note = getIntent().getStringExtra("order_note");
        if (note != null && !note.isEmpty()) {
            tvNote.setText("Ghi chú: " + note);
            tvNote.setVisibility(View.VISIBLE);
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvSubtotal.setText(nf.format(getIntent().getDoubleExtra("order_subtotal", 0)) + " VNĐ");
        tvDiscount.setText("-" + nf.format(getIntent().getDoubleExtra("order_discount", 0)) + " VNĐ");
        tvTotal.setText(nf.format(getIntent().getDoubleExtra("order_total", 0)) + " VNĐ");

        // Format date
        try {
            String dateStr = getIntent().getStringExtra("order_date");
            if (dateStr != null && dateStr.length() > 19) dateStr = dateStr.substring(0, 19);
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = input.parse(dateStr);
            tvDate.setText("Ngày đặt: " + output.format(date));
        } catch (Exception e) {
            tvDate.setText(getIntent().getStringExtra("order_date"));
        }

        // Update UI based on current status
        updateStatusUI();
    }

    private void updateStatusUI() {
        // Reset all buttons
        btnConfirmOrder.setVisibility(View.GONE);
        btnServeOrder.setVisibility(View.GONE);
        btnCancelOrder.setVisibility(View.GONE);
        tvStatusMessage.setVisibility(View.GONE);

        // Set status badge
        tvCurrentStatus.setText(getStatusText(currentStatus));
        tvCurrentStatus.setBackgroundResource(getStatusBackground(currentStatus));

        switch (currentStatus != null ? currentStatus : "") {
            case "pending":
                // Show: Xác nhận + Hủy
                btnConfirmOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setVisibility(View.VISIBLE);
                break;

            case "processing":
                // Show: Phục vụ + Hủy
                btnServeOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setVisibility(View.VISIBLE);
                break;

            case "served":
                // No buttons - order completed
                tvStatusMessage.setText("✅ Đơn hàng đã được phục vụ hoàn tất");
                tvStatusMessage.setVisibility(View.VISIBLE);
                break;

            case "cancelled":
                // No buttons - order cancelled permanently
                tvStatusMessage.setText("❌ Đơn hàng đã bị hủy, không thể thay đổi trạng thái");
                tvStatusMessage.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "processing": return "Chờ chế biến";
            case "served": return "Đã phục vụ";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        switch (status) {
            case "pending": return R.drawable.bg_status_pending;
            case "processing": return R.drawable.bg_status_processing;
            case "served": return R.drawable.bg_status_delivered;
            case "cancelled": return R.drawable.bg_status_unavailable;
            default: return R.drawable.bg_status_pending;
        }
    }

    private void loadOrderItems() {
        dbService.getOrderItems("eq." + orderId, "*").enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemAdapter.setItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Lỗi tải chi tiết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(String newStatus) {
        // Disable buttons during update
        btnConfirmOrder.setEnabled(false);
        btnServeOrder.setEnabled(false);
        btnCancelOrder.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        dbService.updateOrderStatus("eq." + orderId, updates).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    currentStatus = newStatus;
                    updateStatusUI();
                    Toast.makeText(AdminOrderDetailActivity.this,
                            "Đã cập nhật: " + getStatusText(newStatus),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    enableButtons();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                enableButtons();
            }
        });
    }

    private void enableButtons() {
        btnConfirmOrder.setEnabled(true);
        btnServeOrder.setEnabled(true);
        btnCancelOrder.setEnabled(true);
    }
}
