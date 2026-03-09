package com.example.food_order_app.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.OrderHistoryAdapter;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity: Quản lý đơn hàng đã đặt
 * Hiển thị tất cả đơn hàng với filter theo trạng thái:
 * - Chờ xác nhận (pending): user có thể hủy
 * - Chờ chế biến (processing): không thể hủy
 * - Đã phục vụ (served): hoàn thành
 * - Đã hủy (cancelled)
 */
public class OrderHistoryActivity extends AppCompatActivity
        implements OrderHistoryAdapter.OnOrderCancelledListener {

    private static final String TAG = "OrderHistoryActivity";

    private ImageView btnBack;
    private Button btnFilterAll, btnFilterPending, btnFilterProcessing, btnFilterServed, btnFilterCancelled;
    private ProgressBar progressOrders;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private RecyclerView rvOrders;

    private OrderHistoryAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPending = findViewById(R.id.btnFilterPending);
        btnFilterProcessing = findViewById(R.id.btnFilterProcessing);
        btnFilterServed = findViewById(R.id.btnFilterServed);
        btnFilterCancelled = findViewById(R.id.btnFilterCancelled);
        progressOrders = findViewById(R.id.progressOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        rvOrders = findViewById(R.id.rvOrders);

        adapter = new OrderHistoryAdapter(this, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener filterClick = v -> {
            int id = v.getId();
            if (id == R.id.btnFilterAll) currentFilter = "all";
            else if (id == R.id.btnFilterPending) currentFilter = "pending";
            else if (id == R.id.btnFilterProcessing) currentFilter = "processing";
            else if (id == R.id.btnFilterServed) currentFilter = "served";
            else if (id == R.id.btnFilterCancelled) currentFilter = "cancelled";
            updateFilterUI();
            filterOrders();
        };

        btnFilterAll.setOnClickListener(filterClick);
        btnFilterPending.setOnClickListener(filterClick);
        btnFilterProcessing.setOnClickListener(filterClick);
        btnFilterServed.setOnClickListener(filterClick);
        btnFilterCancelled.setOnClickListener(filterClick);
    }

    private void updateFilterUI() {
        Button[] buttons = {btnFilterAll, btnFilterPending, btnFilterProcessing, btnFilterServed, btnFilterCancelled};
        String[] filters = {"all", "pending", "processing", "served", "cancelled"};

        for (int i = 0; i < buttons.length; i++) {
            boolean selected = currentFilter.equals(filters[i]);
            buttons[i].setBackgroundResource(selected ? R.drawable.bg_category_selected : R.drawable.bg_button_outline);
            buttons[i].setTextColor(getResources().getColor(selected ? R.color.white : R.color.primary));
        }
    }

    private void loadOrders() {
        if (!sessionManager.isLoggedIn()) {
            showEmpty("Vui lòng đăng nhập để xem đơn hàng");
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showEmpty("Không tìm thấy thông tin tài khoản");
            return;
        }

        progressOrders.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvOrders.setVisibility(View.GONE);

        dbService.getOrders("eq." + userId, "*", "created_at.desc")
                .enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                        progressOrders.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            allOrders = response.body();
                            filterOrders();
                        } else {
                            allOrders = new ArrayList<>();
                            showEmpty("Chưa có đơn hàng nào");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Order>> call, Throwable t) {
                        progressOrders.setVisibility(View.GONE);
                        Log.e(TAG, "loadOrders failed: " + t.getMessage());
                        showEmpty("Lỗi tải đơn hàng. Kiểm tra kết nối mạng.");
                    }
                });
    }

    private void filterOrders() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            if (currentFilter.equals("all") || currentFilter.equals(o.getStatus())) {
                filtered.add(o);
            }
        }

        if (filtered.isEmpty()) {
            String msg;
            switch (currentFilter) {
                case "pending": msg = "Không có đơn hàng chờ xác nhận"; break;
                case "processing": msg = "Không có đơn hàng chờ chế biến"; break;
                case "served": msg = "Không có đơn hàng đã phục vụ"; break;
                case "cancelled": msg = "Không có đơn hàng đã hủy"; break;
                default: msg = "Chưa có đơn hàng nào"; break;
            }
            showEmpty(msg);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
            adapter.setOrders(filtered);
        }
    }

    private void showEmpty(String message) {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
    }

    // ============ CALLBACK: Order cancelled ============

    @Override
    public void onOrderCancelled() {
        // Reload orders to reflect the cancellation
        loadOrders();
    }
}
