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
import android.widget.SearchView;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
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
    private Button btnDateFrom, btnDateTo, btnApplyFilter, btnExportRevenuePdf;
    private TextView tvTotalRevenue, tvTotalOrders;
    private TextView tvStatTotal, tvStatPending, tvStatConfirmed, tvStatDelivering, tvStatDelivered, tvStatCancelled;
    private android.widget.TableLayout tableTopFoods;
    private TextView tvNoTopFoods;
    // Daily revenue chart and monthly trend views
    private LinearLayout llDailyRevenueChart;
    private TextView tvDailyRevenueEmpty;
    private android.widget.TableLayout tableMonthlyTrend;
    private com.github.mikephil.charting.charts.BarChart chartMonthlyRevenue;
    private com.github.mikephil.charting.charts.BarChart chartMonthlyOrders;
    private TextView tvMonthlyTrendComment;
    private TextView tvDashRevenueToday, tvDashOrdersToday, tvDashNewCustomersToday, tvDashTop5Foods;
    private TextView tvDashAvgOrderValue, tvDashSuccessRate, tvDashCancelRate;
    private android.widget.TableLayout tableCustomerStats;
    private TextView tvNoCustomerData;
    private ScrollView scrollRevenue;
    private android.view.View cardDashboardOverview;
    private android.view.View cardRevenueReport;
    private AutoCompleteTextView atvMonthSelector;
    private AutoCompleteTextView atvYearSelector;

    private SupabaseDbService dbService;
    private final Map<String, List<CustomerRecord>> monthlyCustomerData = new LinkedHashMap<>();

    private Calendar dateFrom = Calendar.getInstance();
    private Calendar dateTo = Calendar.getInstance();
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private double currentRevenueValue = 0;
    private int currentDeliveredCount = 0;
    private final List<TopFoodRecord> currentTopFoods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_revenue);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();

        // Default: today (no comparison on first load)
        setToday();
        loadRevenue(false);
        loadAllTimeTopFoods();
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
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnExportRevenuePdf = findViewById(R.id.btnExportRevenuePdf);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatConfirmed = findViewById(R.id.tvStatConfirmed);
        tvStatDelivering = findViewById(R.id.tvStatDelivering);
        tvStatDelivered = findViewById(R.id.tvStatDelivered);
        tvStatCancelled = findViewById(R.id.tvStatCancelled);

        tvNoTopFoods = findViewById(R.id.tvNoTopFoods);
        tableTopFoods = findViewById(R.id.tableTopFoods);

        // Daily revenue chart views
        llDailyRevenueChart = findViewById(R.id.llDailyRevenueChart);
        tvDailyRevenueEmpty = findViewById(R.id.tvDailyRevenueEmpty);

        // Monthly trend views
        tableMonthlyTrend = findViewById(R.id.tableMonthlyTrend);
        chartMonthlyRevenue = findViewById(R.id.chartMonthlyRevenue);
        chartMonthlyOrders = findViewById(R.id.chartMonthlyOrders);
        tvMonthlyTrendComment = findViewById(R.id.tvMonthlyTrendComment);

        // Dashboard overview
        tvDashRevenueToday = findViewById(R.id.tvDashRevenueToday);
        tvDashOrdersToday = findViewById(R.id.tvDashOrdersToday);
        tvDashNewCustomersToday = findViewById(R.id.tvDashNewCustomersToday);
        tvDashTop5Foods = findViewById(R.id.tvDashTop5Foods);
        tvDashAvgOrderValue = findViewById(R.id.tvDashAvgOrderValue);
        tvDashSuccessRate = findViewById(R.id.tvDashSuccessRate);
        tvDashCancelRate = findViewById(R.id.tvDashCancelRate);

        // Customer statistics
        tableCustomerStats = findViewById(R.id.tableCustomerStats);
        tvNoCustomerData = findViewById(R.id.tvNoCustomerData);
        atvMonthSelector = findViewById(R.id.atvMonthSelector);
        atvYearSelector = findViewById(R.id.atvYearSelector);
        scrollRevenue = findViewById(R.id.scrollRevenue);
        cardDashboardOverview = findViewById(R.id.cardDashboardOverview);
        cardRevenueReport = findViewById(R.id.cardRevenueReport);
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
            target = tableCustomerStats;
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

        btnApplyFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            loadRevenue(true);
        });
        btnExportRevenuePdf.setOnClickListener(v -> exportRevenueReportPdf());
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

    private void updateDateButtons() {
        btnDateFrom.setText(displayFormat.format(dateFrom.getTime()));
        btnDateTo.setText(displayFormat.format(dateTo.getTime()));
    }

    private void loadRevenue(boolean includeComparison) {
        String fromStr = formatToUtcIso(dateFrom, true);
        String toStr = formatToUtcIso(dateTo, false);

        // Load ALL orders in date range (no status filter) to get breakdown
        Map<String, String> filters = new HashMap<>();
        filters.put("and", "(created_at.gte." + fromStr + ",created_at.lte." + toStr + ")");

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
                            loadCustomerStatistics();
                            loadDashboardOverview();

                            // Load monthly trend data (independent of date filter)
                            loadMonthlyTrend();
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

    // Removed isThisMonthRange() as comparison is now dynamic

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

        tvStatTotal.setText(String.valueOf(totalOrdersCount));
        tvStatPending.setText(String.valueOf(pending));
        tvStatConfirmed.setText(String.valueOf(confirmed));
        tvStatDelivering.setText(String.valueOf(delivering));
        tvStatDelivered.setText(String.valueOf(delivered));
        tvStatCancelled.setText(String.valueOf(cancelled));

        // Draw daily revenue chart by month blocks (e.g., Thang 2 va Thang 3
        // separately).
        drawDailyRevenueChart(monthlyRevenue);
    }

    private String extractDateFromTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty())
            return "";
        try {
            java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                    Locale.getDefault());
            parser.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            String safeString = timestamp.length() >= 19 ? timestamp.substring(0, 19) : timestamp;
            java.util.Date date = parser.parse(safeString);

            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            formatter.setTimeZone(java.util.TimeZone.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return timestamp.length() >= 10 ? timestamp.substring(0, 10) : "";
        }
    }

    private String formatToUtcIso(Calendar cal, boolean isStartOfDay) {
        Calendar cloned = (Calendar) cal.clone();
        if (isStartOfDay) {
            cloned.set(Calendar.HOUR_OF_DAY, 0);
            cloned.set(Calendar.MINUTE, 0);
            cloned.set(Calendar.SECOND, 0);
        } else {
            cloned.set(Calendar.HOUR_OF_DAY, 23);
            cloned.set(Calendar.MINUTE, 59);
            cloned.set(Calendar.SECOND, 59);
        }
        java.text.SimpleDateFormat apiFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.getDefault());
        apiFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return apiFormat.format(cloned.getTime());
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
            xAxis.setLabelCount(xLabels.size(), false);

            if (xLabels.size() > 10) {
                barChart.setVisibleXRangeMaximum(10f);
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
        barChart.setPinchZoom(true);
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
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
        if (monthKey == null || monthKey.length() != 7)
            return "Thang";
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

    private void loadMonthlyTrend() {
        if (tvMonthlyTrendComment != null)
            tvMonthlyTrendComment.setText("Đang tải dữ liệu biến động...");

        dbService.getOrdersByStatus("eq.served", "id,total_amount,created_at", "created_at.asc")
                .enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            if (tvMonthlyTrendComment != null)
                                tvMonthlyTrendComment.setText("Chưa có dữ liệu đơn hoàn thành.");
                            return;
                        }

                        // Group by month
                        LinkedHashMap<String, double[]> monthData = new LinkedHashMap<>(); // key=MM/yyyy,
                                                                                           // value=[revenue,
                                                                                           // orderCount]
                        for (Order o : response.body()) {
                            if (o.getCreatedAt() == null)
                                continue;
                            String dateISO = extractDateFromTimestamp(o.getCreatedAt());
                            if (dateISO.length() < 7)
                                continue;
                            String y = dateISO.substring(0, 4);
                            String m = dateISO.substring(5, 7);
                            String monthKey = m + "/" + y;

                            double[] vals = monthData.get(monthKey);
                            if (vals == null) {
                                vals = new double[] { 0, 0 };
                                monthData.put(monthKey, vals);
                            }
                            vals[0] += o.getTotalAmount();
                            vals[1] += 1;
                        }

                        if (monthData.isEmpty()) {
                            if (tvMonthlyTrendComment != null)
                                tvMonthlyTrendComment.setText("Chưa có dữ liệu.");
                            return;
                        }

                        populateMonthlyTrend(monthData);
                    }

                    @Override
                    public void onFailure(Call<List<Order>> call, Throwable t) {
                        if (tvMonthlyTrendComment != null)
                            tvMonthlyTrendComment.setText("Lỗi kết nối khi tải dữ liệu biến động.");
                    }
                });
    }

    private void populateMonthlyTrend(LinkedHashMap<String, double[]> monthData) {
        // Clear old rows (keep header)
        if (tableMonthlyTrend != null) {
            int count = tableMonthlyTrend.getChildCount();
            if (count > 1)
                tableMonthlyTrend.removeViews(1, count - 1);
        }

        List<String> months = new ArrayList<>(monthData.keySet());
        List<Float> revenues = new ArrayList<>();
        List<Float> orders = new ArrayList<>();

        double prevRevenue = -1;
        double prevOrders = -1;
        StringBuilder aiDataSummary = new StringBuilder();

        for (int i = 0; i < months.size(); i++) {
            String monthKey = months.get(i);
            double[] vals = monthData.get(monthKey);
            double revenue = vals[0];
            int orderCount = (int) vals[1];

            revenues.add((float) revenue);
            orders.add((float) orderCount);

            String revChangeStr = "---";
            String ordChangeStr = "---";
            int revChangeColor = Color.parseColor("#999999");
            int ordChangeColor = Color.parseColor("#999999");

            if (prevRevenue >= 0 && i > 0) {
                double revPercent = prevRevenue > 0 ? ((revenue - prevRevenue) / prevRevenue) * 100
                        : (revenue > 0 ? 100 : 0);
                double ordPercent = prevOrders > 0 ? ((orderCount - prevOrders) / prevOrders) * 100
                        : (orderCount > 0 ? 100 : 0);

                revChangeStr = (revPercent >= 0 ? "+" : "") + String.format("%.0f%%", revPercent);
                ordChangeStr = (ordPercent >= 0 ? "+" : "") + String.format("%.0f%%", ordPercent);
                revChangeColor = revPercent >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
                ordChangeColor = ordPercent >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
            }

            addTrendTableRow(monthKey, nf.format(revenue) + "đ", String.valueOf(orderCount), revChangeStr,
                    revChangeColor, ordChangeStr, ordChangeColor);
            aiDataSummary.append("Tháng ").append(monthKey).append(": Doanh thu=").append(nf.format(revenue))
                    .append("đ, Đơn hàng=").append(orderCount).append("\n");

            prevRevenue = revenue;
            prevOrders = orderCount;
        }

        // Build two separate charts
        buildSingleBarChart(chartMonthlyRevenue, months, revenues, "Doanh thu", Color.parseColor("#FF5722"), true);
        buildSingleBarChart(chartMonthlyOrders, months, orders, "Đơn hàng", Color.parseColor("#4CAF50"), false);

        // Request AI commentary
        requestAiCommentary(aiDataSummary.toString());
    }

    private void addTrendTableRow(String month, String revenue, String orderCount, String revChange, int revColor,
            String ordChange, int ordColor) {
        if (tableMonthlyTrend == null)
            return;

        android.widget.TableRow row = new android.widget.TableRow(this);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

        // Month
        android.widget.TextView tvM = new android.widget.TextView(this);
        tvM.setText(month);
        tvM.setTextColor(Color.parseColor("#333333"));
        tvM.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvM.setPadding(pad, pad, pad, pad);
        tvM.setGravity(android.view.Gravity.CENTER);
        tvM.setMinWidth(dpToPx(70));
        row.addView(tvM);

        row.addView(createTrendDivider());

        // Revenue
        android.widget.TextView tvR = new android.widget.TextView(this);
        tvR.setText(revenue);
        tvR.setTextColor(Color.parseColor("#FF5722"));
        tvR.setTypeface(null, android.graphics.Typeface.BOLD);
        tvR.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvR.setPadding(pad, pad, pad, pad);
        tvR.setGravity(android.view.Gravity.CENTER);
        tvR.setMinWidth(dpToPx(100));
        row.addView(tvR);

        row.addView(createTrendDivider());

        // Order count
        android.widget.TextView tvO = new android.widget.TextView(this);
        tvO.setText(orderCount);
        tvO.setTextColor(Color.parseColor("#333333"));
        tvO.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvO.setPadding(pad, pad, pad, pad);
        tvO.setGravity(android.view.Gravity.CENTER);
        tvO.setMinWidth(dpToPx(55));
        row.addView(tvO);

        row.addView(createTrendDivider());

        // Revenue change %
        android.widget.TextView tvRC = new android.widget.TextView(this);
        tvRC.setText(revChange);
        tvRC.setTextColor(revColor);
        tvRC.setTypeface(null, android.graphics.Typeface.BOLD);
        tvRC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvRC.setPadding(pad, pad, pad, pad);
        tvRC.setGravity(android.view.Gravity.CENTER);
        tvRC.setMinWidth(dpToPx(60));
        row.addView(tvRC);

        row.addView(createTrendDivider());

        // Order change %
        android.widget.TextView tvOC = new android.widget.TextView(this);
        tvOC.setText(ordChange);
        tvOC.setTextColor(ordColor);
        tvOC.setTypeface(null, android.graphics.Typeface.BOLD);
        tvOC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvOC.setPadding(pad, pad, pad, pad);
        tvOC.setGravity(android.view.Gravity.CENTER);
        tvOC.setMinWidth(dpToPx(60));
        row.addView(tvOC);

        tableMonthlyTrend.addView(row);

        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableLayout.LayoutParams lpDiv = new android.widget.TableLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tableMonthlyTrend.addView(divider, lpDiv);
    }

    private android.view.View createTrendDivider() {
        android.view.View v = new android.view.View(this);
        v.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lp = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        v.setLayoutParams(lp);
        return v;
    }

    private void buildSingleBarChart(com.github.mikephil.charting.charts.BarChart chart, List<String> months,
            List<Float> values, String label, int color, boolean isCurrency) {
        if (chart == null)
            return;

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setValueTextSize(9f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (isCurrency)
                    return formatCompactCurrency(value);
                return String.format("%.0f", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.setFitBars(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.animateY(500);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        final List<String> labels = months;
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < labels.size())
                    return labels.get(idx);
                return "";
            }
        });

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private void requestAiCommentary(String dataSummary) {
        if (tvMonthlyTrendComment == null)
            return;
        tvMonthlyTrendComment.setText("🤖 Đang phân tích dữ liệu...");

        String prompt = "Bạn là chuyên gia phân tích kinh doanh nhà hàng. Dựa vào dữ liệu biến động doanh thu và đơn hàng theo từng tháng dưới đây, hãy đưa ra nhận xét ngắn gọn (3-5 câu) bằng tiếng Việt. Tập trung vào xu hướng tăng/giảm, tháng nào tốt nhất/kém nhất, và đề xuất 1 hành động cải thiện.\n\nDữ liệu:\n"
                + dataSummary;

        String apiKey = com.example.food_order_app.config.GeminiAiConfig.GEMINI_API_KEY;
        String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";

        org.json.JSONObject requestBody = new org.json.JSONObject();
        try {
            org.json.JSONArray contents = new org.json.JSONArray();
            org.json.JSONObject content = new org.json.JSONObject();
            content.put("role", "user");
            org.json.JSONArray parts = new org.json.JSONArray();
            org.json.JSONObject part = new org.json.JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
        } catch (org.json.JSONException e) {
            tvMonthlyTrendComment.setText("Lỗi tạo yêu cầu AI.");
            return;
        }

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                requestBody.toString(),
                okhttp3.MediaType.parse("application/json"));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> tvMonthlyTrendComment
                        .setText("Lỗi kết nối AI: " + e.getMessage() + ". Vui lòng kiểm tra internet."));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        String errMsg = "Lỗi " + response.code();
                        try {
                            org.json.JSONObject errorJson = new org.json.JSONObject(responseBody);
                            if (errorJson.has("error")) {
                                errMsg += ": " + errorJson.getJSONObject("error").getString("message");
                            }
                        } catch (Exception ignored) {
                        }
                        tvMonthlyTrendComment.setText("AI trả về lỗi (" + errMsg
                                + "). Kiểm tra giới hạn Quota hoặc vùng hỗ trợ của API Key.");
                        return;
                    }
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(responseBody);
                        String aiText = json.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                        tvMonthlyTrendComment.setText(aiText.trim());
                    } catch (org.json.JSONException e) {
                        tvMonthlyTrendComment.setText("Phản hồi AI không đúng định dạng. Chi tiết: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void resetMonthlyTrend() {
        if (tableMonthlyTrend != null) {
            int count = tableMonthlyTrend.getChildCount();
            if (count > 1)
                tableMonthlyTrend.removeViews(1, count - 1);
        }
        if (chartMonthlyRevenue != null)
            chartMonthlyRevenue.clear();
        if (chartMonthlyOrders != null)
            chartMonthlyOrders.clear();
        if (tvMonthlyTrendComment != null)
            tvMonthlyTrendComment.setText("Đang phân tích dữ liệu...");
    }

    private void loadDashboardOverview() {
        String fromStr = isoFormat.format(dateFrom.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(dateTo.getTime()) + "T23:59:59";

        Map<String, String> filters = new HashMap<>();
        filters.put("and", "(created_at.gte." + fromStr + ",created_at.lte." + toStr + ")");

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

                updateDashboardFromOrders(validOrders);
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                resetDashboardOverview();
            }
        });

        // New customers in selected range
        dbService.getUsersByRole("eq.user", "*", "created_at.desc").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvDashNewCustomersToday.setText("0");
                    return;
                }

                int newInRange = 0;
                String fromIso = isoFormat.format(dateFrom.getTime());
                String toIso = isoFormat.format(dateTo.getTime());
                for (User user : response.body()) {
                    if (user.getCreatedAt() != null) {
                        String createdDate = extractDateFromTimestamp(user.getCreatedAt());
                        if (createdDate.compareTo(fromIso) >= 0 && createdDate.compareTo(toIso) <= 0) {
                            newInRange++;
                        }
                    }
                }
                tvDashNewCustomersToday.setText(String.valueOf(newInRange));
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                tvDashNewCustomersToday.setText("0");
            }
        });
    }

    private void updateDashboardFromOrders(List<Order> orders) {
        double rangeRevenue = 0;
        int rangeOrders = 0;

        // Build day buckets from dateFrom to dateTo
        LinkedHashMap<String, Double> dayRevenue = new LinkedHashMap<>();
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
            String key = isoFormat.format(cursor.getTime());
            dayRevenue.put(key, 0.0);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Order order : orders) {
            if (!"served".equals(order.getStatus()) || order.getCreatedAt() == null) {
                continue;
            }

            String dayKey = extractDateFromTimestamp(order.getCreatedAt());
            if (dayRevenue.containsKey(dayKey)) {
                dayRevenue.put(dayKey, dayRevenue.get(dayKey) + order.getTotalAmount());
            }

            rangeRevenue += order.getTotalAmount();
            rangeOrders++;
        }

        // Count total orders and cancelled for rates
        int totalAll = orders.size();
        int cancelledCount = 0;
        for (Order order : orders) {
            if ("cancelled".equals(order.getStatus())) {
                cancelledCount++;
            }
        }

        tvDashRevenueToday.setText(nf.format(rangeRevenue) + " VNĐ");
        tvDashOrdersToday.setText(String.valueOf(rangeOrders));

        // Average order value
        if (rangeOrders > 0) {
            double avgValue = rangeRevenue / rangeOrders;
            tvDashAvgOrderValue.setText(nf.format(avgValue) + " VNĐ");
        } else {
            tvDashAvgOrderValue.setText("0 VNĐ");
        }

        // Success rate
        if (totalAll > 0) {
            double successRate = (double) rangeOrders / totalAll * 100;
            tvDashSuccessRate.setText(String.format(Locale.getDefault(), "%.0f%%", successRate));
        } else {
            tvDashSuccessRate.setText("0%");
        }

        // Cancel rate
        if (totalAll > 0) {
            double cancelRate = (double) cancelledCount / totalAll * 100;
            tvDashCancelRate.setText(String.format(Locale.getDefault(), "%.0f%%", cancelRate));
        } else {
            tvDashCancelRate.setText("0%");
        }

        updateTop5Text();
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

    private void loadCustomerStatistics() {
        if (tvNoCustomerData != null) {
            tvNoCustomerData.setVisibility(android.view.View.VISIBLE);
            tvNoCustomerData.setText("Đang tải dữ liệu...");
        }

        if (atvMonthSelector != null)
            atvMonthSelector.setText("", false);
        if (atvYearSelector != null)
            atvYearSelector.setText("", false);
        resetCustomerStatistics();

        // Fetch ALL orders (all-time) to build the monthly history
        dbService.getAllOrders("receiver_name,created_at,status", "created_at.desc")
                .enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            if (tvNoCustomerData != null)
                                tvNoCustomerData.setText("Chưa có dữ liệu khách hàng.");
                            return;
                        }

                        monthlyCustomerData.clear();
                        Map<String, Map<String, Integer>> tempMap = new LinkedHashMap<>();

                        for (Order order : response.body()) {
                            if (!"served".equals(order.getStatus()) || order.getCreatedAt() == null
                                    || order.getReceiverName() == null) {
                                continue;
                            }
                            String fullDateISO = extractDateFromTimestamp(order.getCreatedAt()); // yyyy-MM-dd
                            if (fullDateISO.length() < 10)
                                continue;

                            String monthKey = fullDateISO.substring(0, 7); // yyyy-MM

                            // Convert yyyy-MM-dd to dd/MM/yyyy
                            String y = fullDateISO.substring(0, 4);
                            String m = fullDateISO.substring(5, 7);
                            String d = fullDateISO.substring(8, 10);
                            String dateDisplay = d + "/" + m + "/" + y;

                            tempMap.putIfAbsent(monthKey, new LinkedHashMap<>());
                            Map<String, Integer> userDateCounts = tempMap.get(monthKey);

                            String name = order.getReceiverName();
                            String entryKey = name + "|" + dateDisplay;
                            userDateCounts.put(entryKey, userDateCounts.getOrDefault(entryKey, 0) + 1);
                        }

                        if (tempMap.isEmpty()) {
                            if (tvNoCustomerData != null)
                                tvNoCustomerData.setText("Chưa có dữ liệu đơn thành công.");
                            return;
                        }

                        // Convert to sorted lists
                        for (String month : tempMap.keySet()) {
                            List<CustomerRecord> records = new ArrayList<>();
                            for (Map.Entry<String, Integer> e : tempMap.get(month).entrySet()) {
                                String[] parts = e.getKey().split("\\|");
                                String name = parts[0];
                                String dateDisplay = (parts.length > 1) ? parts[1] : "";
                                records.add(new CustomerRecord(name, e.getValue(), dateDisplay));
                            }
                            // Sort descending by order count
                            Collections.sort(records, (a, b) -> Integer.compare(b.orderCount, a.orderCount));
                            monthlyCustomerData.put(month, records);
                        }

                        setupMonthYearDropdowns();

                        // Defaults are set inside setupMonthYearDropdowns
                    }

                    @Override
                    public void onFailure(Call<List<Order>> call, Throwable t) {
                        if (tvNoCustomerData != null)
                            tvNoCustomerData.setText("Lỗi kết nối khi tải thống kê khách");
                    }
                });
    }

    private void setupMonthYearDropdowns() {
        if (atvMonthSelector == null || atvYearSelector == null)
            return;

        // Months 01 - 12
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format(Locale.getDefault(), "%02d", i));
        }
        android.widget.ArrayAdapter<String> monthAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, months);
        atvMonthSelector.setAdapter(monthAdapter);

        // Years (expanded range: 2000 to 2100)
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = 2100; y >= 2000; y--) {
            years.add(String.valueOf(y));
        }
        android.widget.ArrayAdapter<String> yearAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, years);
        atvYearSelector.setAdapter(yearAdapter);

        // Initial selection: current month and year
        String curMonth = String.format(Locale.getDefault(), "%02d", Calendar.getInstance().get(Calendar.MONTH) + 1);
        String curYear = String.valueOf(currentYear);
        atvMonthSelector.setText(curMonth, false);
        atvYearSelector.setText(curYear, false);

        atvMonthSelector.setOnItemClickListener((parent, view, position, id) -> updateCustomerStatsFromSelection());
        atvYearSelector.setOnItemClickListener((parent, view, position, id) -> updateCustomerStatsFromSelection());

        // Initial trigger
        updateCustomerStatsFromSelection();
    }

    private void updateCustomerStatsFromSelection() {
        String m = atvMonthSelector.getText().toString();
        String y = atvYearSelector.getText().toString();
        if (m.isEmpty() || y.isEmpty())
            return;

        String monthKey = y + "-" + m; // yyyy-MM
        showCustomerStatsForMonth(monthKey);
    }

    private void showCustomerStatsForMonth(String monthKey) {
        resetCustomerStatistics();

        List<CustomerRecord> records = monthlyCustomerData.get(monthKey);
        if (records == null || records.isEmpty()) {
            if (tvNoCustomerData != null) {
                tvNoCustomerData.setVisibility(android.view.View.VISIBLE);
                tvNoCustomerData.setText("Không có dữ liệu cho tháng này.");
            }
            return;
        }

        String displayMonth = monthKey.substring(5, 7) + "/" + monthKey.substring(0, 4);
        int rank = 1;
        for (CustomerRecord record : records) {
            addCustomerTableRow(record.dateDisplay, record.name, String.valueOf(record.orderCount),
                    String.valueOf(rank));
            rank++;
        }
    }

    private void addCustomerTableRow(String date, String name, String count, String rank) {
        android.widget.TableRow row = new android.widget.TableRow(this);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        android.widget.TextView tvDate = new android.widget.TextView(this);
        tvDate.setText(date);
        tvDate.setTextColor(Color.parseColor("#333333"));
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvDate.setPadding(pad, pad, pad, pad);
        tvDate.setGravity(android.view.Gravity.CENTER);
        android.widget.TableRow.LayoutParams lpDate = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f);
        tvDate.setLayoutParams(lpDate);

        android.view.View vLine1 = new android.view.View(this);
        vLine1.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lpLine1 = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        vLine1.setLayoutParams(lpLine1);

        android.widget.TextView tvName = new android.widget.TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#333333"));
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvName.setPadding(pad, pad, pad, pad);
        android.widget.TableRow.LayoutParams lpName = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        tvName.setLayoutParams(lpName);

        android.view.View vLine2 = new android.view.View(this);
        vLine2.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lpLine2 = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        vLine2.setLayoutParams(lpLine2);

        android.widget.TextView tvCount = new android.widget.TextView(this);
        tvCount.setText(count);
        tvCount.setTextColor(Color.parseColor("#333333"));
        tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvCount.setPadding(pad, pad, pad, pad);
        tvCount.setGravity(android.view.Gravity.CENTER);
        android.widget.TableRow.LayoutParams lpCount = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f);
        tvCount.setLayoutParams(lpCount);

        android.view.View vLine3 = new android.view.View(this);
        vLine3.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lpLine3 = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        vLine3.setLayoutParams(lpLine3);

        android.widget.TextView tvRank = new android.widget.TextView(this);
        tvRank.setText(rank);
        tvRank.setTextColor(Color.parseColor("#333333"));
        tvRank.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvRank.setPadding(pad, pad, pad, pad);
        tvRank.setGravity(android.view.Gravity.CENTER);
        android.widget.TableRow.LayoutParams lpRank = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        tvRank.setLayoutParams(lpRank);

        row.addView(tvDate);
        row.addView(vLine1);
        row.addView(tvName);
        row.addView(vLine2);
        row.addView(tvCount);
        row.addView(vLine3);
        row.addView(tvRank);
        tableCustomerStats.addView(row);

        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableLayout.LayoutParams lpDiv = new android.widget.TableLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tableCustomerStats.addView(divider, lpDiv);
    }

    private static class CustomerRecord {
        String name;
        int orderCount;
        String dateDisplay;

        CustomerRecord(String name, int orderCount, String dateDisplay) {
            this.name = name;
            this.orderCount = orderCount;
            this.dateDisplay = dateDisplay;
        }
    }

    private void resetDashboardOverview() {
        tvDashRevenueToday.setText("0 VNĐ");
        tvDashOrdersToday.setText("0");
        tvDashNewCustomersToday.setText("0");
        tvDashAvgOrderValue.setText("0 VNĐ");
        tvDashSuccessRate.setText("0%");
        tvDashCancelRate.setText("0%");
        tvDashTop5Foods.setText("Top 5 món: chưa có dữ liệu");
    }

    private void resetCustomerStatistics() {
        if (tvNoCustomerData != null) {
            tvNoCustomerData.setVisibility(android.view.View.GONE);
        }
        if (tableCustomerStats != null) {
            int count = tableCustomerStats.getChildCount();
            if (count > 1)
                tableCustomerStats.removeViews(1, count - 1);
        }
    }

    private void loadAllTimeTopFoods() {
        if (tvNoTopFoods != null) {
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            tvNoTopFoods.setText("Đang tải Top 10...");
        }
        // rvTopFoods.setVisibility(android.view.View.GONE); // This line was commented
        // out or removed in a previous edit, keeping it out.

        dbService.getOrdersByStatus("eq.served", "id", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<String> allServedIds = new ArrayList<>();
                    for (Order o : response.body()) {
                        if (o.getId() != null)
                            allServedIds.add(o.getId());
                    }
                    if (!allServedIds.isEmpty()) {
                        loadTopFoods(allServedIds);
                    } else {
                        showNoTopFoods();
                    }
                } else {
                    showNoTopFoods();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                showNoTopFoods();
            }
        });
    }

    private void showNoTopFoods() {
        if (tvNoTopFoods != null) {
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            tvNoTopFoods.setText("Chưa có dữ liệu Top 10.");
        }
        if (tableTopFoods != null) {
            tableTopFoods.setVisibility(android.view.View.GONE);
            int count = tableTopFoods.getChildCount();
            if (count > 1)
                tableTopFoods.removeViews(1, count - 1);
        }
        currentTopFoods.clear();
        updateTop5Text();
    }

    private void loadTopFoods(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            android.util.Log.d("Top10Debug", "No orderIds provided for Top 10");
            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
            if (tableTopFoods != null)
                tableTopFoods.setVisibility(android.view.View.GONE);
            return;
        }

        android.util.Log.d("Top10Debug", "Loading Top 10 for " + orderIds.size() + " orders");

        List<OrderItem> allItems = new ArrayList<>();
        int batchSize = 30;
        int totalBatches = (int) Math.ceil((double) orderIds.size() / batchSize);
        final int[] completedBatches = { 0 };
        final boolean[] hasError = { false };

        for (int i = 0; i < totalBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, orderIds.size());
            List<String> batchIds = orderIds.subList(start, end);

            StringBuilder sb = new StringBuilder("in.(");
            for (int j = 0; j < batchIds.size(); j++) {
                if (j > 0)
                    sb.append(",");
                sb.append(batchIds.get(j));
            }
            sb.append(")");

            dbService.getOrderItems(sb.toString(), "*").enqueue(new Callback<List<OrderItem>>() {
                @Override
                public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                    completedBatches[0]++;
                    if (response.isSuccessful() && response.body() != null) {
                        android.util.Log.d("Top10Debug", "Batch loaded " + response.body().size() + " items");
                        allItems.addAll(response.body());
                    } else {
                        android.util.Log.e("Top10Debug",
                                "Batch load failed with code: " + response.code() + ", error: " + response.message());
                        try {
                            if (response.errorBody() != null) {
                                android.util.Log.e("Top10Debug", "Error body: " + response.errorBody().string());
                            }
                        } catch (Exception e) {
                        }
                        hasError[0] = true;
                    }

                    if (completedBatches[0] == totalBatches) {
                        android.util.Log.d("Top10Debug",
                                "All batches done. Total items: " + allItems.size() + ", hasError: " + hasError[0]);
                        if (allItems.isEmpty() && hasError[0]) {
                            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
                            if (tableTopFoods != null)
                                tableTopFoods.setVisibility(android.view.View.GONE);
                        } else {
                            buildTopFoods(allItems);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                    android.util.Log.e("Top10Debug", "API call failed", t);
                    completedBatches[0]++;
                    hasError[0] = true;
                    if (completedBatches[0] == totalBatches) {
                        if (allItems.isEmpty()) {
                            tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
                            if (tableTopFoods != null)
                                tableTopFoods.setVisibility(android.view.View.GONE);
                        } else {
                            buildTopFoods(allItems);
                        }
                    }
                }
            });
        }
    }

    private void buildTopFoods(List<OrderItem> items) {
        // Aggregate by food name
        Map<String, int[]> qtyMap = new HashMap<>(); // name -> [quantity]
        Map<String, double[]> revMap = new HashMap<>(); // name -> [revenue]

        for (OrderItem item : items) {
            String name = item.getFoodName();
            if (name == null || name.trim().isEmpty()) {
                name = "Món không tên";
            }

            if (!qtyMap.containsKey(name)) {
                qtyMap.put(name, new int[] { 0 });
                revMap.put(name, new double[] { 0 });
            }
            qtyMap.get(name)[0] += item.getQuantity();
            revMap.get(name)[0] += item.getSubtotal();
        }

        // Build list and sort by quantity descending, then by revenue
        List<TopFoodRecord> topFoods = new ArrayList<>();
        for (String name : qtyMap.keySet()) {
            topFoods.add(new TopFoodRecord(name, qtyMap.get(name)[0], revMap.get(name)[0]));
        }
        Collections.sort(topFoods, (a, b) -> {
            if (b.quantity != a.quantity) {
                return Integer.compare(b.quantity, a.quantity);
            }
            return Double.compare(b.revenue, a.revenue);
        });

        // Take top 10
        if (topFoods.size() > 10) {
            topFoods = topFoods.subList(0, 10);
        }

        currentTopFoods.clear();
        currentTopFoods.addAll(topFoods);

        if (tableTopFoods != null) {
            int count = tableTopFoods.getChildCount();
            if (count > 1)
                tableTopFoods.removeViews(1, count - 1);

            if (topFoods.isEmpty()) {
                tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
                tableTopFoods.setVisibility(android.view.View.GONE);
            } else {
                tvNoTopFoods.setVisibility(android.view.View.GONE);
                tableTopFoods.setVisibility(android.view.View.VISIBLE);

                int rank = 1;
                for (TopFoodRecord tf : topFoods) {
                    addTopFoodTableRow(String.valueOf(rank), tf.name, String.valueOf(tf.quantity),
                            nf.format(tf.revenue) + "đ");
                    rank++;
                }
            }
        }
        updateTop5Text();
    }

    private void addTopFoodTableRow(String rank, String name, String qty, String rev) {
        android.widget.TableRow row = new android.widget.TableRow(this);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        android.widget.TextView tvRank = new android.widget.TextView(this);
        tvRank.setText(rank);
        tvRank.setTextColor(Color.parseColor("#333333"));
        tvRank.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvRank.setPadding(pad, pad, pad, pad);
        tvRank.setGravity(android.view.Gravity.CENTER);
        android.widget.TableRow.LayoutParams lpRank = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f);
        tvRank.setLayoutParams(lpRank);

        android.view.View vLine1 = new android.view.View(this);
        vLine1.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lpLine1 = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        vLine1.setLayoutParams(lpLine1);

        android.widget.TextView tvName = new android.widget.TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#333333"));
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvName.setPadding(pad, pad, pad, pad);
        android.widget.TableRow.LayoutParams lpName = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        tvName.setLayoutParams(lpName);

        android.view.View vLine2 = new android.view.View(this);
        vLine2.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lpLine2 = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        vLine2.setLayoutParams(lpLine2);

        android.widget.TextView tvQty = new android.widget.TextView(this);
        tvQty.setText(qty);
        tvQty.setTextColor(Color.parseColor("#333333"));
        tvQty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvQty.setPadding(pad, pad, pad, pad);
        tvQty.setGravity(android.view.Gravity.CENTER);
        android.widget.TableRow.LayoutParams lpQty = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f);
        tvQty.setLayoutParams(lpQty);

        android.view.View vLine3 = new android.view.View(this);
        vLine3.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableRow.LayoutParams lpLine3 = new android.widget.TableRow.LayoutParams(1,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        vLine3.setLayoutParams(lpLine3);

        android.widget.TextView tvRev = new android.widget.TextView(this);
        tvRev.setText(rev);
        tvRev.setTextColor(Color.parseColor("#FF5722")); // Highlighting revenue
        tvRev.setTypeface(null, android.graphics.Typeface.BOLD);
        tvRev.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvRev.setPadding(pad, pad, pad, pad);
        tvRev.setGravity(android.view.Gravity.CENTER);
        android.widget.TableRow.LayoutParams lpRev = new android.widget.TableRow.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f);
        tvRev.setLayoutParams(lpRev);

        row.addView(tvRank);
        row.addView(vLine1);
        row.addView(tvName);
        row.addView(vLine2);
        row.addView(tvQty);
        row.addView(vLine3);
        row.addView(tvRev);
        tableTopFoods.addView(row);

        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
        android.widget.TableLayout.LayoutParams lpDiv = new android.widget.TableLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tableTopFoods.addView(divider, lpDiv);
    }

    private static class TopFoodRecord {
        String name;
        int quantity;
        double revenue;

        TopFoodRecord(String name, int quantity, double revenue) {
            this.name = name;
            this.quantity = quantity;
            this.revenue = revenue;
        }
    }

    private void resetStats() {
        tvTotalRevenue.setText("0 VNĐ");
        tvTotalOrders.setText("0");
        currentRevenueValue = 0;
        currentDeliveredCount = 0;
        tvStatTotal.setText("0");
        tvStatPending.setText("0");
        tvStatConfirmed.setText("0");
        tvStatDelivering.setText("0");
        tvStatDelivered.setText("0");
        tvStatCancelled.setText("0");
        tvStatCancelled.setText("0");
        currentTopFoods.clear();
        tvNoTopFoods.setVisibility(android.view.View.VISIBLE);
        if (tableTopFoods != null) {
            tableTopFoods.setVisibility(android.view.View.GONE);
            int count = tableTopFoods.getChildCount();
            if (count > 1)
                tableTopFoods.removeViews(1, count - 1);
        }
        llDailyRevenueChart.removeAllViews();
        tvDailyRevenueEmpty.setVisibility(android.view.View.VISIBLE);
        resetMonthlyTrend();
        resetDashboardOverview();
        resetCustomerStatistics();
    }

    private void exportRevenueReportPdf() {
        Uri uri = createRevenueReportPdf(false);
        if (uri != null) {
            Toast.makeText(this, "Da xuat PDF vao Downloads/FoodOrderReports", Toast.LENGTH_LONG).show();
        }
    }

    private Uri createRevenueReportPdf(boolean silent) {
        String fileName = "BaoCaoDoanhThu_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime())
                + ".pdf";

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
        page.getCanvas().drawText("Khoang ngay: " + btnDateFrom.getText() + " - " + btnDateTo.getText(), x, y,
                bodyPaint);
        y += 16;
        page.getCanvas().drawText("Ngay xuat: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Calendar.getInstance().getTime()), x, y, bodyPaint);
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
        page.getCanvas().drawText(
                "- Cho xac nhan: " + tvStatPending.getText() + " | Dang xu ly: " + tvStatConfirmed.getText(), x, y,
                bodyPaint);
        y += 15;
        page.getCanvas().drawText(
                "- Dang giao: " + tvStatDelivering.getText() + " | Da huy: " + tvStatCancelled.getText(), x, y,
                bodyPaint);
        y += 20;

        page.getCanvas().drawText("Top mon ban chay", x, y, headingPaint);
        y += 16;

        if (currentTopFoods.isEmpty()) {
            page.getCanvas().drawText("- Chua co du lieu top mon.", x, y, bodyPaint);
            y += 15;
        } else {
            for (int i = 0; i < currentTopFoods.size(); i++) {
                TopFoodRecord topFood = currentTopFoods.get(i);
                String rank = String.valueOf(i + 1);
                String name = topFood.name;
                String qty = String.valueOf(topFood.quantity);
                String rev = nf.format(topFood.revenue) + "đ";
                String line = String.format(Locale.getDefault(),
                        "%s. %s | So luong: %s | Doanh thu: %s",
                        rank, name, qty, rev);
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
