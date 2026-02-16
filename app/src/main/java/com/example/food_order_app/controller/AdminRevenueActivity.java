package com.example.food_order_app.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.TopFoodAdapter;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRevenueActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnDateFrom, btnDateTo, btnToday, btnThisMonth, btnLastMonth, btnApplyFilter;
    private TextView tvTotalRevenue, tvTotalOrders;
    private TextView tvStatPending, tvStatConfirmed, tvStatDelivering, tvStatDelivered, tvStatCancelled;
    private RecyclerView rvTopFoods;
    private TextView tvNoTopFoods;

    private SupabaseDbService dbService;
    private TopFoodAdapter topFoodAdapter;

    private Calendar dateFrom = Calendar.getInstance();
    private Calendar dateTo = Calendar.getInstance();
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_revenue);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();

        // Default: today
        setToday();
        loadRevenue();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackRevenue);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        btnToday = findViewById(R.id.btnToday);
        btnThisMonth = findViewById(R.id.btnThisMonth);
        btnLastMonth = findViewById(R.id.btnLastMonth);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatConfirmed = findViewById(R.id.tvStatConfirmed);
        tvStatDelivering = findViewById(R.id.tvStatDelivering);
        tvStatDelivered = findViewById(R.id.tvStatDelivered);
        tvStatCancelled = findViewById(R.id.tvStatCancelled);

        tvNoTopFoods = findViewById(R.id.tvNoTopFoods);
        rvTopFoods = findViewById(R.id.rvTopFoods);

        topFoodAdapter = new TopFoodAdapter(this);
        rvTopFoods.setLayoutManager(new LinearLayoutManager(this));
        rvTopFoods.setAdapter(topFoodAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDateFrom.setOnClickListener(v -> showDatePicker(true));
        btnDateTo.setOnClickListener(v -> showDatePicker(false));

        btnToday.setOnClickListener(v -> { setToday(); loadRevenue(); });
        btnThisMonth.setOnClickListener(v -> { setThisMonth(); loadRevenue(); });
        btnLastMonth.setOnClickListener(v -> { setLastMonth(); loadRevenue(); });
        btnApplyFilter.setOnClickListener(v -> loadRevenue());
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = isFrom ? dateFrom : dateTo;
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            updateDateButtons();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setToday() {
        dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.HOUR_OF_DAY, 0);
        dateFrom.set(Calendar.MINUTE, 0);
        dateFrom.set(Calendar.SECOND, 0);
        dateTo = Calendar.getInstance();
        updateDateButtons();
    }

    private void setThisMonth() {
        dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);
        dateTo = Calendar.getInstance();
        updateDateButtons();
    }

    private void setLastMonth() {
        dateFrom = Calendar.getInstance();
        dateFrom.add(Calendar.MONTH, -1);
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);
        dateTo = Calendar.getInstance();
        dateTo.set(Calendar.DAY_OF_MONTH, 1);
        dateTo.add(Calendar.DAY_OF_MONTH, -1);
        updateDateButtons();
    }

    private void updateDateButtons() {
        btnDateFrom.setText(displayFormat.format(dateFrom.getTime()));
        btnDateTo.setText(displayFormat.format(dateTo.getTime()));
    }

    private void loadRevenue() {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fromStr = isoFormat.format(dateFrom.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(dateTo.getTime()) + "T23:59:59";

        // Load ALL orders in date range (no status filter) to get breakdown
        Map<String, String> extraFilters = new HashMap<>();
        extraFilters.put("created_at", "lte." + toStr);

        dbService.getOrdersByDateRange(
                null, // no status filter – get all
                "gte." + fromStr,
                extraFilters,
                "*"
        ).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processOrders(response.body());
                } else {
                    resetStats();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AdminRevenueActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrders(List<Order> orders) {
        double totalRevenue = 0;
        int deliveredCount = 0;
        int pending = 0, confirmed = 0, delivering = 0, delivered = 0, cancelled = 0;

        List<String> deliveredOrderIds = new ArrayList<>();

        for (Order order : orders) {
            String status = order.getStatus();
            if (status == null) status = "";

            switch (status) {
                case "pending":
                    pending++;
                    break;
                case "confirmed":
                case "preparing":
                    confirmed++;
                    break;
                case "delivering":
                    delivering++;
                    break;
                case "delivered":
                    delivered++;
                    totalRevenue += order.getTotalAmount();
                    deliveredOrderIds.add(order.getId());
                    break;
                case "cancelled":
                    cancelled++;
                    break;
            }
        }

        deliveredCount = delivered;
        tvTotalRevenue.setText(nf.format(totalRevenue) + " VNĐ");
        tvTotalOrders.setText(String.valueOf(deliveredCount));
        tvStatPending.setText(String.valueOf(pending));
        tvStatConfirmed.setText(String.valueOf(confirmed));
        tvStatDelivering.setText(String.valueOf(delivering));
        tvStatDelivered.setText(String.valueOf(delivered));
        tvStatCancelled.setText(String.valueOf(cancelled));

        // Load top foods from delivered orders
        if (!deliveredOrderIds.isEmpty()) {
            loadTopFoods(deliveredOrderIds);
        } else {
            topFoodAdapter.setItems(new ArrayList<>());
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            rvTopFoods.setVisibility(android.view.View.GONE);
        }
    }

    private void loadTopFoods(List<String> orderIds) {
        // Build filter: order_id=in.(id1,id2,...)
        StringBuilder sb = new StringBuilder("in.(");
        for (int i = 0; i < orderIds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(orderIds.get(i));
        }
        sb.append(")");

        dbService.getOrderItems(sb.toString(), "*").enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    buildTopFoods(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
                rvTopFoods.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void buildTopFoods(List<OrderItem> items) {
        // Aggregate by food name
        Map<String, int[]> qtyMap = new HashMap<>(); // name -> [quantity]
        Map<String, double[]> revMap = new HashMap<>(); // name -> [revenue]

        for (OrderItem item : items) {
            String name = item.getFoodName();
            if (!qtyMap.containsKey(name)) {
                qtyMap.put(name, new int[]{0});
                revMap.put(name, new double[]{0});
            }
            qtyMap.get(name)[0] += item.getQuantity();
            revMap.get(name)[0] += item.getSubtotal();
        }

        // Build list and sort by revenue descending
        List<TopFoodAdapter.TopFood> topFoods = new ArrayList<>();
        for (String name : qtyMap.keySet()) {
            topFoods.add(new TopFoodAdapter.TopFood(name, qtyMap.get(name)[0], revMap.get(name)[0]));
        }
        Collections.sort(topFoods, (a, b) -> Double.compare(b.revenue, a.revenue));

        // Take top 10
        if (topFoods.size() > 10) {
            topFoods = topFoods.subList(0, 10);
        }

        if (topFoods.isEmpty()) {
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            rvTopFoods.setVisibility(android.view.View.GONE);
        } else {
            tvNoTopFoods.setVisibility(android.view.View.GONE);
            rvTopFoods.setVisibility(android.view.View.VISIBLE);
        }

        topFoodAdapter.setItems(topFoods);
    }

    private void resetStats() {
        tvTotalRevenue.setText("0 VNĐ");
        tvTotalOrders.setText("0");
        tvStatPending.setText("0");
        tvStatConfirmed.setText("0");
        tvStatDelivering.setText("0");
        tvStatDelivered.setText("0");
        tvStatCancelled.setText("0");
        topFoodAdapter.setItems(new ArrayList<>());
        tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
        rvTopFoods.setVisibility(android.view.View.GONE);
    }
}
