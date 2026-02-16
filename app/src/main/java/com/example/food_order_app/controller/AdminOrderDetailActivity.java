package com.example.food_order_app.controller;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminOrderAdapter;
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
    private Spinner spinnerStatus;
    private Button btnUpdateStatus;
    private RecyclerView rvItems;

    private SupabaseDbService dbService;
    private OrderItemAdapter itemAdapter;
    private String orderId;
    private String currentStatus;

    private static final String[] STATUS_VALUES = {"pending", "confirmed", "preparing", "delivering", "delivered", "cancelled"};
    private static final String[] STATUS_LABELS = {"Chờ xác nhận", "Đang xử lý", "Đang chuẩn bị", "Đang giao", "Hoàn thành", "Đã hủy"};

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
        spinnerStatus = findViewById(R.id.spinnerOrderStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        rvItems = findViewById(R.id.rvOrderItems);

        itemAdapter = new OrderItemAdapter(this);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);

        // Setup status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, STATUS_LABELS);
        spinnerStatus.setAdapter(statusAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnUpdateStatus.setOnClickListener(v -> updateStatus());
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
            tvNote.setVisibility(android.view.View.VISIBLE);
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

        // Set spinner to current status
        for (int i = 0; i < STATUS_VALUES.length; i++) {
            if (STATUS_VALUES[i].equals(currentStatus)) {
                spinnerStatus.setSelection(i);
                break;
            }
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

    private void updateStatus() {
        String newStatus = STATUS_VALUES[spinnerStatus.getSelectedItemPosition()];
        if (newStatus.equals(currentStatus)) {
            Toast.makeText(this, "Trạng thái không thay đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        dbService.updateOrderStatus("eq." + orderId, updates).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    currentStatus = newStatus;
                    Toast.makeText(AdminOrderDetailActivity.this,
                            "Đã cập nhật: " + STATUS_LABELS[spinnerStatus.getSelectedItemPosition()],
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
