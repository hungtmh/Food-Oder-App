package com.example.food_order_app.controller;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderStatisticsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnDateFrom, btnDateTo, btn7Days, btn30Days, btnThisMonth;
    private TextView tvTotalOrders, tvTotalRevenue;
    private TextView tvStatPending, tvStatConfirmed, tvStatDelivered, tvStatCancelled;
    private PieChart pieChartStatus;
    private BarChart barChartTimeline;

    private SupabaseDbService dbService;
    private Calendar dateFrom = Calendar.getInstance();
    private Calendar dateTo = Calendar.getInstance();
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_statistics);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();

        // Default: last 30 days
        setLast30Days();
        loadStatistics();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackStats);
        btnDateFrom = findViewById(R.id.btnStatsDateFrom);
        btnDateTo = findViewById(R.id.btnStatsDateTo);
        btn7Days = findViewById(R.id.btnStats7Days);
        btn30Days = findViewById(R.id.btnStats30Days);
        btnThisMonth = findViewById(R.id.btnStatsThisMonth);

        tvTotalOrders = findViewById(R.id.tvStatsTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvStatsTotalRevenue);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatConfirmed = findViewById(R.id.tvStatConfirmed);
        tvStatDelivered = findViewById(R.id.tvStatDelivered);
        tvStatCancelled = findViewById(R.id.tvStatCancelled);

        pieChartStatus = findViewById(R.id.pieChartStatus);
        barChartTimeline = findViewById(R.id.barChartTimeline);

        setupPieChart();
        setupBarChart();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnDateFrom.setOnClickListener(v -> showDatePicker(true));
        btnDateTo.setOnClickListener(v -> showDatePicker(false));

        btn7Days.setOnClickListener(v -> {
            setLast7Days();
            loadStatistics();
        });
        btn30Days.setOnClickListener(v -> {
            setLast30Days();
            loadStatistics();
        });
        btnThisMonth.setOnClickListener(v -> {
            setThisMonth();
            loadStatistics();
        });
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = isFrom ? dateFrom : dateTo;
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            updateDateButtons();
            loadStatistics();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setLast7Days() {
        dateTo = Calendar.getInstance();
        dateFrom = Calendar.getInstance();
        dateFrom.add(Calendar.DAY_OF_MONTH, -6);
        updateDateButtons();
    }

    private void setLast30Days() {
        dateTo = Calendar.getInstance();
        dateFrom = Calendar.getInstance();
        dateFrom.add(Calendar.DAY_OF_MONTH, -29);
        updateDateButtons();
    }

    private void setThisMonth() {
        dateFrom = Calendar.getInstance();
        dateFrom.set(Calendar.DAY_OF_MONTH, 1);
        dateTo = Calendar.getInstance();
        updateDateButtons();
    }

    private void updateDateButtons() {
        btnDateFrom.setText(displayFormat.format(dateFrom.getTime()));
        btnDateTo.setText(displayFormat.format(dateTo.getTime()));
    }

    private void loadStatistics() {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fromStr = isoFormat.format(dateFrom.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(dateTo.getTime()) + "T23:59:59";

        Map<String, String> extraFilters = new HashMap<>();
        extraFilters.put("created_at", "lte." + toStr);

        dbService.getOrdersByDateRange(
                null,
                "gte." + fromStr,
                extraFilters,
                "*"
        ).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processOrders(response.body());
                } else {
                    resetAll();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AdminOrderStatisticsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrders(List<Order> orders) {
        int total = orders.size();
        int pending = 0, confirmed = 0, delivered = 0, cancelled = 0;
        double revenue = 0;

        // For bar chart: group by date (LinkedHashMap giữ thứ tự thêm vào = thứ tự thời gian)
        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat sortKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        LinkedHashMap<String, Integer> dailyOrders = new LinkedHashMap<>();
        // Map từ display key -> sort key để lookup
        Map<String, String> displayToSortKey = new HashMap<>();

        // Pre-fill all dates in range theo thứ tự thời gian
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
            if (status == null) status = "";

            switch (status) {
                case "pending":
                    pending++;
                    break;
                case "confirmed":
                    confirmed++;
                    break;
                case "delivered":
                    delivered++;
                    revenue += order.getTotalAmount();
                    break;
                case "cancelled":
                    cancelled++;
                    break;
            }

            // Parse date for bar chart
            try {
                String createdAt = order.getCreatedAt();
                if (createdAt != null && createdAt.length() >= 10) {
                    String dateStr = createdAt.substring(0, 10); // yyyy-MM-dd
                    if (dailyOrders.containsKey(dateStr)) {
                        dailyOrders.put(dateStr, dailyOrders.get(dateStr) + 1);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Update summary
        tvTotalOrders.setText(String.valueOf(total));
        tvTotalRevenue.setText(nf.format(revenue) + " VNĐ");
        tvStatPending.setText(String.valueOf(pending));
        tvStatConfirmed.setText(String.valueOf(confirmed));
        tvStatDelivered.setText(String.valueOf(delivered));
        tvStatCancelled.setText(String.valueOf(cancelled));

        // Update charts
        updatePieChart(pending, confirmed, delivered, cancelled);
        updateBarChart(dailyOrders, displayToSortKey);
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
            // Hiển thị dd/MM thay vì yyyy-MM-dd
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
                if (value == 0) return "";
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        barChartTimeline.setData(data);

        XAxis xAxis = barChartTimeline.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 10), false);

        // Cho phép cuộn ngang nếu nhiều ngày
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

    private void resetAll() {
        tvTotalOrders.setText("0");
        tvTotalRevenue.setText("0 VNĐ");
        tvStatPending.setText("0");
        tvStatConfirmed.setText("0");
        tvStatDelivered.setText("0");
        tvStatCancelled.setText("0");
        pieChartStatus.clear();
        pieChartStatus.invalidate();
        barChartTimeline.clear();
        barChartTimeline.invalidate();
    }
}
