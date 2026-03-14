package com.example.food_order_app.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.graphics.Color;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRevenueActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnDateFrom, btnDateTo, btnToday, btnThisMonth, btnLastMonth, btnApplyFilter;
    private TextView tvTotalRevenue, tvTotalOrders;
    private TextView tvStatTotal, tvStatPending, tvStatConfirmed, tvStatDelivering, tvStatDelivered, tvStatCancelled;
    private RecyclerView rvTopFoods;
    private TextView tvNoTopFoods;
    private PieChart pieChartStatus;
    private BarChart barChartTimeline;

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
        setupPieChart();
        setupBarChart();
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
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatConfirmed = findViewById(R.id.tvStatConfirmed);
        tvStatDelivering = findViewById(R.id.tvStatDelivering);
        tvStatDelivered = findViewById(R.id.tvStatDelivered);
        tvStatCancelled = findViewById(R.id.tvStatCancelled);

        tvNoTopFoods = findViewById(R.id.tvNoTopFoods);
        rvTopFoods = findViewById(R.id.rvTopFoods);
        pieChartStatus = findViewById(R.id.pieChartStatus);
        barChartTimeline = findViewById(R.id.barChartTimeline);

        topFoodAdapter = new TopFoodAdapter(this);
        rvTopFoods.setLayoutManager(new LinearLayoutManager(this));
        rvTopFoods.setAdapter(topFoodAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDateFrom.setOnClickListener(v -> showDatePicker(true));
        btnDateTo.setOnClickListener(v -> showDatePicker(false));

        btnToday.setOnClickListener(v -> {
            setToday();
            loadRevenue();
        });
        btnThisMonth.setOnClickListener(v -> {
            setThisMonth();
            loadRevenue();
        });
        btnLastMonth.setOnClickListener(v -> {
            setLastMonth();
            loadRevenue();
        });
        btnApplyFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            loadRevenue();
        });
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
        Map<String, String> filters = new HashMap<>();
        filters.put("created_at", "gte." + fromStr);
        // Supabase doesn't easily support two filters on the same column via query map keys without a custom filter string.
        // We will filter lte locally.

        dbService.getOrdersByDateRange(
                filters,
                "*").enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Order> validOrders = new ArrayList<>();
                            // Filter lte locally
                            for (Order o : response.body()) {
                                if (o.getCreatedAt() != null && o.getCreatedAt().compareTo(toStr) <= 0) {
                                    validOrders.add(o);
                                }
                            }
                            processOrders(validOrders);
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

        // For bar chart: group by date
        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat sortKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        LinkedHashMap<String, Integer> dailyOrders = new LinkedHashMap<>();
        Map<String, String> displayToSortKey = new HashMap<>();

        // Pre-fill all dates in range
        Calendar cal = (Calendar) dateFrom.clone();
        while (!cal.after(dateTo)) {
            String displayKey = dateKeyFormat.format(cal.getTime());
            String sortKey = sortKeyFormat.format(cal.getTime());
            dailyOrders.put(sortKey, 0);
            displayToSortKey.put(sortKey, displayKey);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Order order : orders) {
            String status = order.getStatus();
            if (status == null)
                status = "";

            switch (status) {
                case "pending":
                    pending++;
                    break;
                case "processing":
                    confirmed++;
                    break;
                case "served":
                    delivered++;
                    totalRevenue += order.getTotalAmount();
                    deliveredOrderIds.add(order.getId());
                    break;
                case "cancelled":
                    cancelled++;
                    break;
            }

            // Parse date for bar chart
            try {
                String createdAt = order.getCreatedAt();
                if (createdAt != null && createdAt.length() >= 10) {
                    String dateStr = createdAt.substring(0, 10);
                    if (dailyOrders.containsKey(dateStr)) {
                        dailyOrders.put(dateStr, dailyOrders.get(dateStr) + 1);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        deliveredCount = delivered;
        int totalOrdersCount = pending + confirmed + delivering + delivered + cancelled;
        
        tvTotalRevenue.setText(nf.format(totalRevenue) + " VNĐ");
        tvTotalOrders.setText(String.valueOf(deliveredCount));
        tvStatTotal.setText(String.valueOf(totalOrdersCount));
        tvStatPending.setText(String.valueOf(pending));
        tvStatConfirmed.setText(String.valueOf(confirmed));
        tvStatDelivering.setText(String.valueOf(delivering));
        tvStatDelivered.setText(String.valueOf(delivered));
        tvStatCancelled.setText(String.valueOf(cancelled));

        // Update charts
        updatePieChart(pending, confirmed, delivered, cancelled);
        updateBarChart(dailyOrders, displayToSortKey);

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
            if (i > 0)
                sb.append(",");
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
                qtyMap.put(name, new int[] { 0 });
                revMap.put(name, new double[] { 0 });
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
        tvStatTotal.setText("0");
        tvStatPending.setText("0");
        tvStatConfirmed.setText("0");
        tvStatDelivering.setText("0");
        tvStatDelivered.setText("0");
        tvStatCancelled.setText("0");
        topFoodAdapter.setItems(new ArrayList<>());
        tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
        rvTopFoods.setVisibility(android.view.View.GONE);
        pieChartStatus.clear();
        pieChartStatus.invalidate();
        barChartTimeline.clear();
        barChartTimeline.invalidate();
    }

    // ==================== PIE CHART ====================

    private void setupPieChart() {
        pieChartStatus.setUsePercentValues(true);
        pieChartStatus.getDescription().setEnabled(false);
        pieChartStatus.setDrawHoleEnabled(true);
        pieChartStatus.setHoleColor(Color.WHITE);
        pieChartStatus.setHoleRadius(45f);
        pieChartStatus.setTransparentCircleRadius(50f);
        pieChartStatus.setDrawEntryLabels(true);
        pieChartStatus.setEntryLabelColor(Color.BLACK);
        pieChartStatus.setEntryLabelTextSize(10f);
        pieChartStatus.getLegend().setEnabled(true);
        pieChartStatus.getLegend().setTextSize(11f);
        pieChartStatus.setNoDataText("Chưa có dữ liệu");
        pieChartStatus.animateY(800);
    }

    private void updatePieChart(int pending, int confirmed, int delivered, int cancelled) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (pending > 0) {
            entries.add(new PieEntry(pending, "Chờ xác nhận"));
            colors.add(Color.parseColor("#FF9800"));
        }
        if (confirmed > 0) {
            entries.add(new PieEntry(confirmed, "Đang xử lý"));
            colors.add(Color.parseColor("#2196F3"));
        }
        if (delivered > 0) {
            entries.add(new PieEntry(delivered, "Hoàn thành"));
            colors.add(Color.parseColor("#4CAF50"));
        }
        if (cancelled > 0) {
            entries.add(new PieEntry(cancelled, "Đã hủy"));
            colors.add(Color.parseColor("#F44336"));
        }

        if (entries.isEmpty()) {
            pieChartStatus.clear();
            pieChartStatus.setNoDataText("Không có đơn hàng");
            pieChartStatus.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f%%", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChartStatus.setData(data);
        pieChartStatus.setCenterText("Tổng\n" + (pending + confirmed + delivered + cancelled));
        pieChartStatus.setCenterTextSize(14f);
        pieChartStatus.invalidate();
    }

    // ==================== BAR CHART ====================

    private void setupBarChart() {
        barChartTimeline.getDescription().setEnabled(false);
        barChartTimeline.setDrawGridBackground(false);
        barChartTimeline.setDrawBarShadow(false);
        barChartTimeline.setFitBars(true);
        barChartTimeline.setPinchZoom(false);
        barChartTimeline.setDoubleTapToZoomEnabled(false);
        barChartTimeline.getLegend().setEnabled(false);
        barChartTimeline.setNoDataText("Chưa có dữ liệu");
        barChartTimeline.animateY(800);

        XAxis xAxis = barChartTimeline.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(-45f);

        barChartTimeline.getAxisLeft().setAxisMinimum(0f);
        barChartTimeline.getAxisLeft().setGranularity(1f);
        barChartTimeline.getAxisLeft().setTextSize(10f);
        barChartTimeline.getAxisRight().setEnabled(false);
    }

    private void updateBarChart(LinkedHashMap<String, Integer> dailyOrders, Map<String, String> displayToSortKey) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : dailyOrders.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            String displayLabel = displayToSortKey.containsKey(entry.getKey())
                    ? displayToSortKey.get(entry.getKey())
                    : entry.getKey();
            labels.add(displayLabel);
            index++;
        }

        if (entries.isEmpty()) {
            barChartTimeline.clear();
            barChartTimeline.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Đơn hàng");
        dataSet.setColor(Color.parseColor("#E53935"));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0)
                    return "";
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartTimeline.setData(data);

        XAxis xAxis = barChartTimeline.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 10), false);

        if (labels.size() > 10) {
            barChartTimeline.setVisibleXRangeMaximum(10);
            barChartTimeline.moveViewToX(entries.size() - 10);
        } else {
            barChartTimeline.setVisibleXRangeMaximum(labels.size());
            barChartTimeline.moveViewToX(0);
        }

        barChartTimeline.notifyDataSetChanged();
        barChartTimeline.invalidate();
    }
}
