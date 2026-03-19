package com.example.food_order_app.controller;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.TopFoodAdapter;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.AdminBottomNavHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
import java.io.IOException;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRevenueActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_SECTION = "open_section";
    public static final String SECTION_DASHBOARD = "dashboard";
    public static final String SECTION_REPORT = "report";
    public static final String SECTION_CUSTOMER = "customer";

    private ImageButton btnBack;
    private Button btnDateFrom, btnDateTo, btnToday, btnThisMonth, btnLastMonth, btnSingleDay, btnApplyFilter, btnExportRevenuePdf, btnExportRevenueExcel, btnSendRevenueEmail;
    private TextView tvTotalRevenue, tvTotalOrders;
    private TextView tvStatTotal, tvStatPending, tvStatConfirmed, tvStatDelivering, tvStatDelivered, tvStatCancelled;
    private RecyclerView rvTopFoods;
    private TextView tvNoTopFoods;
    
    // Daily revenue chart and comparison views
    private LinearLayout llDailyRevenueChart;
    private TextView tvDailyRevenueEmpty;
    private TextView tvCurrentMonthRevenue, tvCurrentMonthOrders;
    private TextView tvPreviousMonthRevenue, tvPreviousMonthOrders;
    private TextView tvRevenueChange, tvOrdersChange;
    private TextView tvDashRevenueToday, tvDashOrdersToday, tvDashNewCustomersToday, tvDashTop5Foods;
    private TextView tvCustomerTotal, tvCustomerNewRange, tvCustomerTopBuyer, tvCustomerFrequency;
    private ScrollView scrollRevenue;
    private android.view.View cardDashboardOverview;
    private android.view.View cardRevenueReport;
    private android.view.View cardCustomerStats;
    private BarChart barChartLast7Days;

    private SupabaseDbService dbService;
    private TopFoodAdapter topFoodAdapter;

    private Calendar dateFrom = Calendar.getInstance();
    private Calendar dateTo = Calendar.getInstance();
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private double currentRevenueValue = 0;
    private int currentDeliveredCount = 0;
    private final List<TopFoodAdapter.TopFood> currentTopFoods = new ArrayList<>();

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
        handleSectionNavigationIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdminBottomNavHelper.setup(this, AdminBottomNavHelper.TAB_REVENUE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackRevenue);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        btnToday = findViewById(R.id.btnToday);
        btnThisMonth = findViewById(R.id.btnThisMonth);
        btnLastMonth = findViewById(R.id.btnLastMonth);
        btnSingleDay = findViewById(R.id.btnSingleDay);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnExportRevenuePdf = findViewById(R.id.btnExportRevenuePdf);
        btnExportRevenueExcel = findViewById(R.id.btnExportRevenueExcel);
        btnSendRevenueEmail = findViewById(R.id.btnSendRevenueEmail);

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
        
        // Daily revenue chart views
        llDailyRevenueChart = findViewById(R.id.llDailyRevenueChart);
        tvDailyRevenueEmpty = findViewById(R.id.tvDailyRevenueEmpty);
        
        // Comparison views
        tvCurrentMonthRevenue = findViewById(R.id.tvCurrentMonthRevenue);
        tvCurrentMonthOrders = findViewById(R.id.tvCurrentMonthOrders);
        tvPreviousMonthRevenue = findViewById(R.id.tvPreviousMonthRevenue);
        tvPreviousMonthOrders = findViewById(R.id.tvPreviousMonthOrders);
        tvRevenueChange = findViewById(R.id.tvRevenueChange);
        tvOrdersChange = findViewById(R.id.tvOrdersChange);

        // Dashboard overview
        tvDashRevenueToday = findViewById(R.id.tvDashRevenueToday);
        tvDashOrdersToday = findViewById(R.id.tvDashOrdersToday);
        tvDashNewCustomersToday = findViewById(R.id.tvDashNewCustomersToday);
        tvDashTop5Foods = findViewById(R.id.tvDashTop5Foods);
        barChartLast7Days = findViewById(R.id.barChartLast7Days);

        // Customer statistics
        tvCustomerTotal = findViewById(R.id.tvCustomerTotal);
        tvCustomerNewRange = findViewById(R.id.tvCustomerNewRange);
        tvCustomerTopBuyer = findViewById(R.id.tvCustomerTopBuyer);
        tvCustomerFrequency = findViewById(R.id.tvCustomerFrequency);
        scrollRevenue = findViewById(R.id.scrollRevenue);
        cardDashboardOverview = findViewById(R.id.cardDashboardOverview);
        cardRevenueReport = findViewById(R.id.cardRevenueReport);
        cardCustomerStats = findViewById(R.id.cardCustomerStats);

        topFoodAdapter = new TopFoodAdapter(this);
        rvTopFoods.setLayoutManager(new LinearLayoutManager(this));
        rvTopFoods.setAdapter(topFoodAdapter);
    }

    private void handleSectionNavigationIntent() {
        String section = getIntent().getStringExtra(EXTRA_OPEN_SECTION);
        if (section == null || scrollRevenue == null) {
            return;
        }

        android.view.View target = null;
        if (SECTION_DASHBOARD.equals(section)) {
            target = cardDashboardOverview;
        } else if (SECTION_REPORT.equals(section)) {
            target = cardRevenueReport;
        } else if (SECTION_CUSTOMER.equals(section)) {
            target = cardCustomerStats;
        }

        if (target != null) {
            android.view.View finalTarget = target;
            scrollRevenue.post(() -> scrollRevenue.smoothScrollTo(0, finalTarget.getTop()));
        }
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
        btnSingleDay.setOnClickListener(v -> showSingleDayPickerAndLoad());
        btnApplyFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            loadRevenue();
        });
        btnExportRevenuePdf.setOnClickListener(v -> exportRevenueReportPdf());
        btnExportRevenueExcel.setOnClickListener(v -> exportRevenueReportExcel());
        btnSendRevenueEmail.setOnClickListener(v -> sendRevenueReportEmail());
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = isFrom ? dateFrom : dateTo;
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            updateDateButtons();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showSingleDayPickerAndLoad() {
        Calendar pickerCal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            dateFrom = Calendar.getInstance();
            dateFrom.set(year, month, day, 0, 0, 0);
            dateFrom.set(Calendar.MILLISECOND, 0);

            dateTo = Calendar.getInstance();
            dateTo.set(year, month, day, 23, 59, 59);
            dateTo.set(Calendar.MILLISECOND, 0);

            updateDateButtons();
            loadRevenue();
        }, pickerCal.get(Calendar.YEAR), pickerCal.get(Calendar.MONTH), pickerCal.get(Calendar.DAY_OF_MONTH)).show();
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
        String fromStr = isoFormat.format(dateFrom.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(dateTo.getTime()) + "T23:59:59";

        // Load ALL orders in date range (no status filter) to get breakdown
        Map<String, String> filters = new HashMap<>();
        filters.put("created_at", "gte." + fromStr);

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
                            loadCustomerStatistics(validOrders);
                            loadDashboardOverview();
                            
                            // Load comparison data if this is "this month" or custom range
                            if (isThisMonthRange()) {
                                loadPreviousMonthComparison();
                            } else {
                                resetComparisonStats();
                            }
                        } else {
                            resetStats();
                            resetDashboardOverview();
                            resetCustomerStatistics();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Order>> call, Throwable t) {
                        Toast.makeText(AdminRevenueActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private boolean isThisMonthRange() {
        Calendar today = Calendar.getInstance();
        Calendar dayOne = Calendar.getInstance();
        dayOne.set(Calendar.DAY_OF_MONTH, 1);
        dayOne.set(Calendar.HOUR_OF_DAY, 0);
        dayOne.set(Calendar.MINUTE, 0);
        dayOne.set(Calendar.SECOND, 0);
        
        return dateFrom.get(Calendar.YEAR) == dayOne.get(Calendar.YEAR) &&
               dateFrom.get(Calendar.MONTH) == dayOne.get(Calendar.MONTH) &&
               dateFrom.get(Calendar.DAY_OF_MONTH) == dayOne.get(Calendar.DAY_OF_MONTH) &&
               Math.abs(dateTo.getTimeInMillis() - today.getTimeInMillis()) < 86400000;
    }

    private void processOrders(List<Order> orders) {
        double totalRevenue = 0;
        int deliveredCount = 0;
        int pending = 0, confirmed = 0, delivering = 0, delivered = 0, cancelled = 0;

        List<String> deliveredOrderIds = new ArrayList<>();
        LinkedHashMap<String, LinkedHashMap<Integer, Double>> monthlyRevenue = initializeMonthlyRangeBuckets();

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

                    // Collect revenue per day and separate per month in selected range.
                    String dateStr = extractDateFromTimestamp(order.getCreatedAt());
                    if (!dateStr.isEmpty() && dateStr.length() >= 10) {
                        String monthKey = dateStr.substring(0, 7); // yyyy-MM
                        int day = Integer.parseInt(dateStr.substring(8, 10));
                        if (monthlyRevenue.containsKey(monthKey)) {
                            LinkedHashMap<Integer, Double> dayMap = monthlyRevenue.get(monthKey);
                            if (dayMap.containsKey(day)) {
                                dayMap.put(day, dayMap.get(day) + order.getTotalAmount());
                            }
                        }
                    }
                    break;
                case "cancelled":
                    cancelled++;
                    break;
            }
        }

        deliveredCount = delivered;
        int totalOrdersCount = pending + confirmed + delivering + delivered + cancelled;
        currentRevenueValue = totalRevenue;
        currentDeliveredCount = deliveredCount;
        
        tvTotalRevenue.setText(nf.format(totalRevenue) + " VNĐ");
        tvTotalOrders.setText(String.valueOf(deliveredCount));
        tvCurrentMonthRevenue.setText(nf.format(totalRevenue) + " VNĐ");
        tvCurrentMonthOrders.setText(String.valueOf(deliveredCount));
        
        tvStatTotal.setText(String.valueOf(totalOrdersCount));
        tvStatPending.setText(String.valueOf(pending));
        tvStatConfirmed.setText(String.valueOf(confirmed));
        tvStatDelivering.setText(String.valueOf(delivering));
        tvStatDelivered.setText(String.valueOf(delivered));
        tvStatCancelled.setText(String.valueOf(cancelled));

        // Draw daily revenue chart by month blocks (e.g., Thang 2 va Thang 3 separately).
        drawDailyRevenueChart(monthlyRevenue);

        // Load top foods from delivered orders
        if (!deliveredOrderIds.isEmpty()) {
            loadTopFoods(deliveredOrderIds);
        } else {
            topFoodAdapter.setItems(new ArrayList<>());
            currentTopFoods.clear();
            updateTop5Text();
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            rvTopFoods.setVisibility(android.view.View.GONE);
        }
    }
    
    private String extractDateFromTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            return timestamp.substring(0, 10); // Extract YYYY-MM-DD
        } catch (Exception e) {
            return "";
        }
    }

    private LinkedHashMap<String, LinkedHashMap<Integer, Double>> initializeMonthlyRangeBuckets() {
        LinkedHashMap<String, LinkedHashMap<Integer, Double>> buckets = new LinkedHashMap<>();
        Calendar cursor = (Calendar) dateFrom.clone();
        cursor.set(Calendar.HOUR_OF_DAY, 0);
        cursor.set(Calendar.MINUTE, 0);
        cursor.set(Calendar.SECOND, 0);
        cursor.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) dateTo.clone();
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        while (!cursor.after(end)) {
            String monthKey = String.format(Locale.getDefault(), "%04d-%02d",
                    cursor.get(Calendar.YEAR), cursor.get(Calendar.MONTH) + 1);
            int day = cursor.get(Calendar.DAY_OF_MONTH);

            if (!buckets.containsKey(monthKey)) {
                buckets.put(monthKey, new LinkedHashMap<>());
            }
            buckets.get(monthKey).put(day, 0.0);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        return buckets;
    }

    private void drawDailyRevenueChart(LinkedHashMap<String, LinkedHashMap<Integer, Double>> monthlyRevenue) {
        llDailyRevenueChart.removeAllViews();

        if (monthlyRevenue.isEmpty()) {
            tvDailyRevenueEmpty.setVisibility(android.view.View.VISIBLE);
            llDailyRevenueChart.setVisibility(android.view.View.GONE);
            return;
        }

        tvDailyRevenueEmpty.setVisibility(android.view.View.GONE);
        llDailyRevenueChart.setVisibility(android.view.View.VISIBLE);

        for (Map.Entry<String, LinkedHashMap<Integer, Double>> monthEntry : monthlyRevenue.entrySet()) {
            String monthKey = monthEntry.getKey();
            LinkedHashMap<Integer, Double> dayMap = monthEntry.getValue();

            LinearLayout monthContainer = new LinearLayout(this);
            monthContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams monthParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            monthParams.topMargin = dpToPx(10);
            monthContainer.setLayoutParams(monthParams);

            TextView monthTitle = new TextView(this);
            monthTitle.setText(buildMonthTitle(monthKey));
            monthTitle.setTextColor(getResources().getColor(R.color.text_primary));
            monthTitle.setTextSize(13f);
            monthTitle.setTypeface(monthTitle.getTypeface(), android.graphics.Typeface.BOLD);
            monthContainer.addView(monthTitle);

            BarChart barChart = new BarChart(this);
            LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(240));
            chartParams.topMargin = dpToPx(6);
            barChart.setLayoutParams(chartParams);
            configureDailyBarChart(barChart);

            List<BarEntry> entries = new ArrayList<>();
            List<String> xLabels = new ArrayList<>();
            int index = 0;

            for (Map.Entry<Integer, Double> dayEntry : dayMap.entrySet()) {
                entries.add(new BarEntry(index, dayEntry.getValue().floatValue()));
                xLabels.add(String.valueOf(dayEntry.getKey()));
                index++;
            }

            BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VND)");
            dataSet.setColor(Color.parseColor("#FF6B6B"));
            dataSet.setValueTextSize(9f);
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value == 0f ? "" : formatCompactCurrency(value);
                }
            });

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.7f);
            barChart.setData(data);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
            xAxis.setLabelCount(Math.min(xLabels.size(), 12), false);

            if (xLabels.size() > 12) {
                barChart.setVisibleXRangeMaximum(12f);
                barChart.moveViewToX(0f);
            }

            barChart.invalidate();
            monthContainer.addView(barChart);
            llDailyRevenueChart.addView(monthContainer);
        }
    }

    private void configureDailyBarChart(BarChart barChart) {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setFitBars(true);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.animateY(600);
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setTextSize(10f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setTextSize(10f);
        xAxis.setAxisLineColor(Color.parseColor("#BDBDBD"));

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setTextSize(10f);
        barChart.getAxisLeft().setAxisLineColor(Color.parseColor("#BDBDBD"));
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatCompactCurrency(value);
            }
        });
        barChart.getAxisRight().setEnabled(false);
    }

    private String buildMonthTitle(String monthKey) {
        // monthKey format yyyy-MM
        if (monthKey == null || monthKey.length() != 7) return "Thang";
        String year = monthKey.substring(0, 4);
        String month = monthKey.substring(5, 7);
        return "Thang " + month + "/" + year;
    }

    private String formatCompactCurrency(float value) {
        if (value >= 1_000_000f) {
            return String.format(Locale.getDefault(), "%.1fM", value / 1_000_000f);
        }
        if (value >= 1_000f) {
            return String.format(Locale.getDefault(), "%.0fK", value / 1_000f);
        }
        return String.format(Locale.getDefault(), "%.0f", value);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics());
    }
    
    private void loadPreviousMonthComparison() {
        Calendar prevMonth = Calendar.getInstance();
        prevMonth.add(Calendar.MONTH, -1);
        
        Calendar prevFrom = Calendar.getInstance();
        prevFrom.setTimeInMillis(prevMonth.getTimeInMillis());
        prevFrom.set(Calendar.DAY_OF_MONTH, 1);
        prevFrom.set(Calendar.HOUR_OF_DAY, 0);
        prevFrom.set(Calendar.MINUTE, 0);
        prevFrom.set(Calendar.SECOND, 0);
        
        Calendar prevTo = Calendar.getInstance();
        prevTo.setTimeInMillis(prevMonth.getTimeInMillis());
        prevTo.set(Calendar.DAY_OF_MONTH, prevTo.getActualMaximum(Calendar.DAY_OF_MONTH));
        prevTo.set(Calendar.HOUR_OF_DAY, 23);
        prevTo.set(Calendar.MINUTE, 59);
        prevTo.set(Calendar.SECOND, 59);
        
        String fromStr = isoFormat.format(prevFrom.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(prevTo.getTime()) + "T23:59:59";
        
        Map<String, String> filters = new HashMap<>();
        filters.put("created_at", "gte." + fromStr);
        
        dbService.getOrdersByDateRange(filters, "*").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> validOrders = new ArrayList<>();
                    for (Order o : response.body()) {
                        if (o.getCreatedAt() != null && o.getCreatedAt().compareTo(toStr) <= 0) {
                            validOrders.add(o);
                        }
                    }
                    calculateComparisonStats(validOrders);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                resetComparisonStats();
            }
        });
    }
    
    private void calculateComparisonStats(List<Order> prevOrders) {
        double prevRevenue = 0;
        int prevDelivered = 0;
        
        for (Order o : prevOrders) {
            if ("served".equals(o.getStatus())) {
                prevRevenue += o.getTotalAmount();
                prevDelivered++;
            }
        }
        
        double currentRevenue = currentRevenueValue;
        int currentDelivered = currentDeliveredCount;
        
        tvPreviousMonthRevenue.setText(nf.format(prevRevenue) + " VNĐ");
        tvPreviousMonthOrders.setText(String.valueOf(prevDelivered));
        
        // Calculate percentage changes
        double revenueChangePercent = prevRevenue > 0 ? ((currentRevenue - prevRevenue) / prevRevenue) * 100 : 0;
        double ordersChangePercent = prevDelivered > 0 ? ((currentDelivered - prevDelivered) / (double) prevDelivered) * 100 : 0;
        
        updateChangeIndicator(tvRevenueChange, revenueChangePercent);
        updateChangeIndicator(tvOrdersChange, ordersChangePercent);
    }
    
    private void updateChangeIndicator(TextView tv, double changePercent) {
        String changeText = (changePercent >= 0 ? "+" : "") + String.format("%.0f%%", changePercent);
        tv.setText(changeText);
        
        if (changePercent > 0) {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.parseColor("#66BB6A")); // Green success color
        } else if (changePercent < 0) {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.parseColor("#EF5350")); // Red error color
        } else {
            tv.setTextColor(Color.parseColor("#999999"));
            tv.setBackgroundColor(Color.parseColor("#F0F0F0")); // Gray neutral
        }
    }
    
    private void resetComparisonStats() {
        tvPreviousMonthRevenue.setText("0 VNĐ");
        tvPreviousMonthOrders.setText("0");
        tvRevenueChange.setText("+0%");
        tvOrdersChange.setText("+0%");
    }

    private void loadDashboardOverview() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);

        Calendar last7Start = Calendar.getInstance();
        last7Start.add(Calendar.DAY_OF_MONTH, -6);
        last7Start.set(Calendar.HOUR_OF_DAY, 0);
        last7Start.set(Calendar.MINUTE, 0);
        last7Start.set(Calendar.SECOND, 0);
        last7Start.set(Calendar.MILLISECOND, 0);

        String fromStr = isoFormat.format(last7Start.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(Calendar.getInstance().getTime()) + "T23:59:59";

        Map<String, String> filters = new HashMap<>();
        filters.put("created_at", "gte." + fromStr);

        dbService.getOrdersByDateRange(filters, "*").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resetDashboardOverview();
                    return;
                }

                List<Order> validOrders = new ArrayList<>();
                for (Order o : response.body()) {
                    if (o.getCreatedAt() != null && o.getCreatedAt().compareTo(toStr) <= 0) {
                        validOrders.add(o);
                    }
                }

                updateDashboardFromOrders(validOrders, todayStart);
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                resetDashboardOverview();
            }
        });

        // New customers today
        dbService.getUsersByRole("eq.user", "*", "created_at.desc").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvDashNewCustomersToday.setText("0");
                    return;
                }

                int newToday = 0;
                String todayIso = isoFormat.format(Calendar.getInstance().getTime());
                for (User user : response.body()) {
                    if (user.getCreatedAt() != null && user.getCreatedAt().startsWith(todayIso)) {
                        newToday++;
                    }
                }
                tvDashNewCustomersToday.setText(String.valueOf(newToday));
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                tvDashNewCustomersToday.setText("0");
            }
        });
    }

    private void updateDashboardFromOrders(List<Order> orders, Calendar todayStart) {
        double todayRevenue = 0;
        int todayOrders = 0;

        LinkedHashMap<String, Double> dayRevenue = new LinkedHashMap<>();
        Calendar cursor = (Calendar) todayStart.clone();
        cursor.add(Calendar.DAY_OF_MONTH, -6);
        for (int i = 0; i < 7; i++) {
            String key = isoFormat.format(cursor.getTime());
            dayRevenue.put(key, 0.0);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Order order : orders) {
            if (!"served".equals(order.getStatus()) || order.getCreatedAt() == null) {
                continue;
            }

            String dayKey = order.getCreatedAt().substring(0, 10);
            if (dayRevenue.containsKey(dayKey)) {
                dayRevenue.put(dayKey, dayRevenue.get(dayKey) + order.getTotalAmount());
            }

            if (dayKey.equals(isoFormat.format(Calendar.getInstance().getTime()))) {
                todayRevenue += order.getTotalAmount();
                todayOrders++;
            }
        }

        tvDashRevenueToday.setText(nf.format(todayRevenue) + " VNĐ");
        tvDashOrdersToday.setText(String.valueOf(todayOrders));

        updateLast7DaysChart(dayRevenue);
        updateTop5Text();
    }

    private void updateLast7DaysChart(LinkedHashMap<String, Double> dayRevenue) {
        if (barChartLast7Days == null) {
            return;
        }

        configureDailyBarChart(barChartLast7Days);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Double> entry : dayRevenue.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey().substring(8, 10));
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu 7 ngày");
        dataSet.setColor(Color.parseColor("#42A5F5"));
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0f ? "" : formatCompactCurrency(value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.65f);
        barChartLast7Days.setData(data);
        barChartLast7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartLast7Days.getXAxis().setLabelCount(labels.size(), true);
        barChartLast7Days.invalidate();
    }

    private void updateTop5Text() {
        if (tvDashTop5Foods == null) {
            return;
        }

        if (currentTopFoods.isEmpty()) {
            tvDashTop5Foods.setText("Top 5 món: chưa có dữ liệu");
            return;
        }

        StringBuilder sb = new StringBuilder("Top 5 món: ");
        int max = Math.min(5, currentTopFoods.size());
        for (int i = 0; i < max; i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(i + 1).append(".").append(currentTopFoods.get(i).name);
        }
        tvDashTop5Foods.setText(sb.toString());
    }

    private void loadCustomerStatistics(List<Order> rangeOrders) {
        Map<String, Integer> ordersByUser = new HashMap<>();
        int totalOrdersInRange = 0;

        for (Order order : rangeOrders) {
            if (order.getUserId() == null || order.getUserId().isEmpty()) {
                continue;
            }
            totalOrdersInRange++;
            ordersByUser.put(order.getUserId(), ordersByUser.getOrDefault(order.getUserId(), 0) + 1);
        }

        String topBuyerId = "";
        int maxOrders = 0;
        for (Map.Entry<String, Integer> entry : ordersByUser.entrySet()) {
            if (entry.getValue() > maxOrders) {
                maxOrders = entry.getValue();
                topBuyerId = entry.getKey();
            }
        }

        final String finalTopBuyerId = topBuyerId;
        final int finalMaxOrders = maxOrders;
        final int finalTotalOrdersInRange = totalOrdersInRange;

        dbService.getUsersByRole("eq.user", "*", "created_at.desc").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resetCustomerStatistics();
                    return;
                }

                List<User> users = response.body();
                tvCustomerTotal.setText(String.valueOf(users.size()));

                int newInRange = 0;
                String fromIso = isoFormat.format(dateFrom.getTime());
                String toIso = isoFormat.format(dateTo.getTime());
                String topBuyerName = "-";

                for (User user : users) {
                    if (user.getCreatedAt() != null) {
                        String createdDate = user.getCreatedAt().substring(0, 10);
                        if (createdDate.compareTo(fromIso) >= 0 && createdDate.compareTo(toIso) <= 0) {
                            newInRange++;
                        }
                    }
                    if (!finalTopBuyerId.isEmpty() && finalTopBuyerId.equals(user.getId())) {
                        topBuyerName = (user.getFullName() == null || user.getFullName().isEmpty())
                                ? user.getEmail()
                                : user.getFullName();
                    }
                }

                tvCustomerNewRange.setText(String.valueOf(newInRange));
                if (finalMaxOrders > 0) {
                    tvCustomerTopBuyer.setText(topBuyerName + " (" + finalMaxOrders + " đơn)");
                } else {
                    tvCustomerTopBuyer.setText("-");
                }

                int uniqueBuyers = ordersByUser.size();
                if (uniqueBuyers > 0) {
                    double frequency = (double) finalTotalOrdersInRange / uniqueBuyers;
                    tvCustomerFrequency.setText(String.format(Locale.getDefault(), "%.1f đơn/khách", frequency));
                } else {
                    tvCustomerFrequency.setText("0 đơn/khách");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                resetCustomerStatistics();
            }
        });
    }

    private void resetDashboardOverview() {
        tvDashRevenueToday.setText("0 VNĐ");
        tvDashOrdersToday.setText("0");
        tvDashNewCustomersToday.setText("0");
        tvDashTop5Foods.setText("Top 5 món: chưa có dữ liệu");
        if (barChartLast7Days != null) {
            barChartLast7Days.clear();
            barChartLast7Days.invalidate();
        }
    }

    private void resetCustomerStatistics() {
        tvCustomerTotal.setText("0");
        tvCustomerNewRange.setText("0");
        tvCustomerTopBuyer.setText("-");
        tvCustomerFrequency.setText("0 đơn/khách");
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

        currentTopFoods.clear();
        currentTopFoods.addAll(topFoods);

        if (topFoods.isEmpty()) {
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            rvTopFoods.setVisibility(android.view.View.GONE);
        } else {
            tvNoTopFoods.setVisibility(android.view.View.GONE);
            rvTopFoods.setVisibility(android.view.View.VISIBLE);
        }

        topFoodAdapter.setItems(topFoods);
        updateTop5Text();
    }

    private void resetStats() {
        tvTotalRevenue.setText("0 VNĐ");
        tvTotalOrders.setText("0");
        currentRevenueValue = 0;
        currentDeliveredCount = 0;
        tvCurrentMonthRevenue.setText("0 VNĐ");
        tvCurrentMonthOrders.setText("0");
        tvStatTotal.setText("0");
        tvStatPending.setText("0");
        tvStatConfirmed.setText("0");
        tvStatDelivering.setText("0");
        tvStatDelivered.setText("0");
        tvStatCancelled.setText("0");
        topFoodAdapter.setItems(new ArrayList<>());
        currentTopFoods.clear();
        tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
        rvTopFoods.setVisibility(android.view.View.GONE);
        llDailyRevenueChart.removeAllViews();
        tvDailyRevenueEmpty.setVisibility(android.view.View.VISIBLE);
        resetComparisonStats();
        resetDashboardOverview();
        resetCustomerStatistics();
    }

    private void exportRevenueReportPdf() {
        Uri uri = createRevenueReportPdf(false);
        if (uri != null) {
            Toast.makeText(this, "Da xuat PDF vao Downloads/FoodOrderReports", Toast.LENGTH_LONG).show();
        }
    }

    private void exportRevenueReportExcel() {
        String fileName = "BaoCaoDoanhThu_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime()) + ".csv";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FoodOrderReports");

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, "Khong the tao file Excel", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Bao cao doanh thu\n");
        csv.append("Khoang ngay,").append(btnDateFrom.getText()).append(" - ").append(btnDateTo.getText()).append("\n");
        csv.append("Tong doanh thu,").append(tvTotalRevenue.getText()).append("\n");
        csv.append("Don hoan thanh,").append(tvTotalOrders.getText()).append("\n");
        csv.append("Tong don,").append(tvStatTotal.getText()).append("\n");
        csv.append("Cho xac nhan,").append(tvStatPending.getText()).append("\n");
        csv.append("Dang xu ly,").append(tvStatConfirmed.getText()).append("\n");
        csv.append("Da giao,").append(tvStatDelivering.getText()).append("\n");
        csv.append("Da huy,").append(tvStatCancelled.getText()).append("\n\n");
        csv.append("Top mon ban chay\n");
        csv.append("STT,Ten mon,So luong,Doanh thu\n");

        for (int i = 0; i < currentTopFoods.size(); i++) {
            TopFoodAdapter.TopFood food = currentTopFoods.get(i);
            csv.append(i + 1).append(",")
                    .append(food.name.replace(",", " ")).append(",")
                    .append(food.quantity).append(",")
                    .append(nf.format(food.revenue)).append("\n");
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                Toast.makeText(this, "Khong the mo file de ghi", Toast.LENGTH_SHORT).show();
                return;
            }
            outputStream.write(csv.toString().getBytes());
            outputStream.flush();
            Toast.makeText(this, "Da xuat Excel vao Downloads/FoodOrderReports", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Loi xuat Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRevenueReportEmail() {
        Uri pdfUri = createRevenueReportPdf(true);
        if (pdfUri == null) {
            Toast.makeText(this, "Khong the tao tep PDF de gui", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bao cao doanh thu admin");
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Bao cao doanh thu dinh kem.\n" +
                        "Khoang ngay: " + btnDateFrom.getText() + " - " + btnDateTo.getText() + "\n" +
                        "Tong doanh thu: " + tvTotalRevenue.getText() + "\n" +
                        "Don hoan thanh: " + tvTotalOrders.getText());
        emailIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(emailIntent, "Gui bao cao qua email"));
        } catch (Exception e) {
            Toast.makeText(this, "Khong tim thay ung dung email", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createRevenueReportPdf(boolean silent) {
        String fileName = "BaoCaoDoanhThu_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime()) + ".pdf";

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.BLACK);

        Paint headingPaint = new Paint();
        headingPaint.setTextSize(13f);
        headingPaint.setFakeBoldText(true);
        headingPaint.setColor(Color.BLACK);

        Paint bodyPaint = new Paint();
        bodyPaint.setTextSize(11f);
        bodyPaint.setColor(Color.BLACK);

        int x = 40;
        int y = 50;

        page.getCanvas().drawText("BAO CAO DOANH THU", x, y, titlePaint);
        y += 24;
        page.getCanvas().drawText("Khoang ngay: " + btnDateFrom.getText() + " - " + btnDateTo.getText(), x, y, bodyPaint);
        y += 16;
        page.getCanvas().drawText("Ngay xuat: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime()), x, y, bodyPaint);
        y += 20;

        page.getCanvas().drawLine(x, y, 555, y, bodyPaint);
        y += 22;

        page.getCanvas().drawText("Tong quan", x, y, headingPaint);
        y += 18;
        page.getCanvas().drawText("- Tong doanh thu: " + tvTotalRevenue.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- Don hoan thanh: " + tvTotalOrders.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- Tong don: " + tvStatTotal.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- Cho xac nhan: " + tvStatPending.getText() + " | Dang xu ly: " + tvStatConfirmed.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- Dang giao: " + tvStatDelivering.getText() + " | Da huy: " + tvStatCancelled.getText(), x, y, bodyPaint);
        y += 20;

        page.getCanvas().drawText("Top mon ban chay", x, y, headingPaint);
        y += 16;

        if (currentTopFoods.isEmpty()) {
            page.getCanvas().drawText("- Chua co du lieu top mon.", x, y, bodyPaint);
            y += 15;
        } else {
            for (int i = 0; i < currentTopFoods.size(); i++) {
                TopFoodAdapter.TopFood topFood = currentTopFoods.get(i);
                String line = String.format(Locale.getDefault(),
                        "%d. %s | So luong: %d | Doanh thu: %s VNĐ",
                        i + 1,
                        topFood.name,
                        topFood.quantity,
                        nf.format(topFood.revenue));
                page.getCanvas().drawText(line, x, y, bodyPaint);
                y += 15;
                if (y > 800) {
                    break;
                }
            }
        }

        document.finishPage(page);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FoodOrderReports");

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            if (!silent) {
                Toast.makeText(this, "Khong the tao file PDF", Toast.LENGTH_SHORT).show();
            }
            document.close();
            return null;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                if (!silent) {
                    Toast.makeText(this, "Khong the mo file de ghi", Toast.LENGTH_SHORT).show();
                }
                return null;
            } else {
                document.writeTo(outputStream);
                return uri;
            }
        } catch (IOException e) {
            if (!silent) {
                Toast.makeText(this, "Loi xuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        } finally {
            document.close();
        }
    }
}
