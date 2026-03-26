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
import android.widget.Button;
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

import java.io.IOException;
import java.io.OutputStream;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportStatisticsActivity extends AppCompatActivity {

    private Button btnOpenRevenueDashboard;
    private Button btnOpenRevenueReport;
    private Button btnOpenCustomerStats;
    private Button btnReportDateFrom;
    private Button btnReportDateTo;
    private Button btnReportToday;
    private Button btnReportThisMonth;
    private Button btnExportReportPdf;
    private Button btnExportReportExcel;
    private Button btnSendReportEmail;

    private TextView tvDashboardRevenueToday;
    private TextView tvDashboardOrdersToday;
    private TextView tvDashboardNewCustomers;
    private TextView tvDashboardTop5Foods;
    private BarChart barChartDashboard7Days;

    private TextView tvReportRevenue;
    private TextView tvReportCompletedOrders;
    private TextView tvReportTotalOrders;
    private TextView tvReportTopFoods;

    private TextView tvCustomerTotal;
    private TextView tvCustomerNewRange;
    private TextView tvCustomerTopBuyer;
    private TextView tvCustomerFrequency;

    private SupabaseDbService dbService;
    private final Calendar reportFrom = Calendar.getInstance();
    private final Calendar reportTo = Calendar.getInstance();
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    private double reportRevenueValue = 0;
    private int reportCompletedOrders = 0;
    private int reportTotalOrders = 0;
    private final List<TopFoodStat> reportTopFoods = new ArrayList<>();
    private List<Order> cachedRangeOrders = new ArrayList<>();
    private boolean isRefreshingCustomerStats = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report_statistics);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupDashboardChart();
        setupListeners();

        setThisMonthRange();
        loadAllSections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdminBottomNavHelper.setup(this, AdminBottomNavHelper.TAB_REVENUE);
    }

    private void initViews() {
        btnOpenRevenueDashboard = findViewById(R.id.btnOpenRevenueDashboard);
        btnOpenRevenueReport = findViewById(R.id.btnOpenRevenueReport);
        btnOpenCustomerStats = findViewById(R.id.btnOpenCustomerStats);
        btnReportDateFrom = findViewById(R.id.btnReportDateFrom);
        btnReportDateTo = findViewById(R.id.btnReportDateTo);
        btnReportToday = findViewById(R.id.btnReportToday);
        btnReportThisMonth = findViewById(R.id.btnReportThisMonth);
        btnExportReportPdf = findViewById(R.id.btnExportReportPdf);
        btnExportReportExcel = findViewById(R.id.btnExportReportExcel);
        btnSendReportEmail = findViewById(R.id.btnSendReportEmail);

        tvDashboardRevenueToday = findViewById(R.id.tvDashboardRevenueToday);
        tvDashboardOrdersToday = findViewById(R.id.tvDashboardOrdersToday);
        tvDashboardNewCustomers = findViewById(R.id.tvDashboardNewCustomers);
        tvDashboardTop5Foods = findViewById(R.id.tvDashboardTop5Foods);
        barChartDashboard7Days = findViewById(R.id.barChartDashboard7Days);

        tvReportRevenue = findViewById(R.id.tvReportRevenue);
        tvReportCompletedOrders = findViewById(R.id.tvReportCompletedOrders);
        tvReportTotalOrders = findViewById(R.id.tvReportTotalOrders);
        tvReportTopFoods = findViewById(R.id.tvReportTopFoods);

        tvCustomerTotal = findViewById(R.id.tvCustomerTotal);
        tvCustomerNewRange = findViewById(R.id.tvCustomerNewRange);
        tvCustomerTopBuyer = findViewById(R.id.tvCustomerTopBuyer);
        tvCustomerFrequency = findViewById(R.id.tvCustomerFrequency);
    }

    private void setupDashboardChart() {
        barChartDashboard7Days.getDescription().setEnabled(false);
        barChartDashboard7Days.setDrawGridBackground(false);
        barChartDashboard7Days.setFitBars(true);
        barChartDashboard7Days.setPinchZoom(false);
        barChartDashboard7Days.setDoubleTapToZoomEnabled(false);
        barChartDashboard7Days.getLegend().setEnabled(false);
        barChartDashboard7Days.setNoDataText("Chưa có dữ liệu");
        barChartDashboard7Days.animateY(600);

        XAxis xAxis = barChartDashboard7Days.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        barChartDashboard7Days.getAxisLeft().setAxisMinimum(0f);
        barChartDashboard7Days.getAxisLeft().setTextSize(10f);
        barChartDashboard7Days.getAxisRight().setEnabled(false);
    }

    private void setupListeners() {
        btnOpenRevenueDashboard.setOnClickListener(v -> loadDashboardOverview());
        btnOpenRevenueReport.setOnClickListener(v -> loadReportAndCustomerStats());
        btnOpenCustomerStats.setOnClickListener(v -> {
            if (isRefreshingCustomerStats) return;
            setCustomerRefreshLoading(true);
            Toast.makeText(this, "Đang thống kê khách hàng...", Toast.LENGTH_SHORT).show();
            loadReportAndCustomerStats();
        });

        btnReportDateFrom.setOnClickListener(v -> showDatePicker(true));
        btnReportDateTo.setOnClickListener(v -> showDatePicker(false));

        btnReportToday.setOnClickListener(v -> {
            setTodayRange();
            loadReportAndCustomerStats();
        });
        btnReportThisMonth.setOnClickListener(v -> {
            setThisMonthRange();
            loadReportAndCustomerStats();
        });

        btnExportReportPdf.setOnClickListener(v -> exportReportPdf());
        btnExportReportExcel.setOnClickListener(v -> exportReportExcel());
        btnSendReportEmail.setOnClickListener(v -> sendReportEmail());
    }

    private void loadAllSections() {
        loadDashboardOverview();
        loadReportAndCustomerStats();
    }

    private void showDatePicker(boolean isFrom) {
        Calendar target = isFrom ? reportFrom : reportTo;
        new DatePickerDialog(this, (view, year, month, day) -> {
            target.set(Calendar.YEAR, year);
            target.set(Calendar.MONTH, month);
            target.set(Calendar.DAY_OF_MONTH, day);

            if (reportFrom.after(reportTo)) {
                if (isFrom) {
                    reportTo.setTimeInMillis(reportFrom.getTimeInMillis());
                } else {
                    reportFrom.setTimeInMillis(reportTo.getTimeInMillis());
                }
            }

            updateDateButtons();
            loadReportAndCustomerStats();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setTodayRange() {
        reportFrom.setTimeInMillis(System.currentTimeMillis());
        reportTo.setTimeInMillis(System.currentTimeMillis());
        updateDateButtons();
    }

    private void setThisMonthRange() {
        reportFrom.setTimeInMillis(System.currentTimeMillis());
        reportFrom.set(Calendar.DAY_OF_MONTH, 1);
        reportTo.setTimeInMillis(System.currentTimeMillis());
        updateDateButtons();
    }

    private void updateDateButtons() {
        btnReportDateFrom.setText(displayFormat.format(reportFrom.getTime()));
        btnReportDateTo.setText(displayFormat.format(reportTo.getTime()));
    }

    private void loadDashboardOverview() {
        Calendar now = Calendar.getInstance();
        Calendar start7 = Calendar.getInstance();
        start7.add(Calendar.DAY_OF_MONTH, -6);
        start7.set(Calendar.HOUR_OF_DAY, 0);
        start7.set(Calendar.MINUTE, 0);
        start7.set(Calendar.SECOND, 0);
        start7.set(Calendar.MILLISECOND, 0);

        String fromStr = isoFormat.format(start7.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(now.getTime()) + "T23:59:59";

        Map<String, String> filters = new HashMap<>();
        filters.put("and", "(created_at.gte." + fromStr + ",created_at.lte." + toStr + ")");

        dbService.getOrdersByDateRange(filters, "*").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resetDashboard();
                    return;
                }

                List<Order> validOrders = new ArrayList<>();
                for (Order order : response.body()) {
                    if (order.getCreatedAt() != null && order.getCreatedAt().compareTo(toStr) <= 0) {
                        validOrders.add(order);
                    }
                }

                String todayIso = isoFormat.format(now.getTime());
                double todayRevenue = 0;
                int todayOrders = 0;
                List<String> servedOrderIds = new ArrayList<>();
                LinkedHashMap<String, Double> dayRevenue = initDailyRevenueMap(start7);

                for (Order order : validOrders) {
                    if (!"served".equals(order.getStatus())) {
                        continue;
                    }
                    String dayKey = safeDate(order.getCreatedAt());
                    if (dayRevenue.containsKey(dayKey)) {
                        dayRevenue.put(dayKey, dayRevenue.get(dayKey) + order.getTotalAmount());
                    }
                    if (todayIso.equals(dayKey)) {
                        todayRevenue += order.getTotalAmount();
                        todayOrders++;
                    }
                    if (order.getId() != null) {
                        servedOrderIds.add(order.getId());
                    }
                }

                tvDashboardRevenueToday.setText(nf.format(todayRevenue) + " VNĐ");
                tvDashboardOrdersToday.setText(String.valueOf(todayOrders));
                updateDashboardChart(dayRevenue);

                if (servedOrderIds.isEmpty()) {
                    tvDashboardTop5Foods.setText("Top 5 món: chưa có dữ liệu");
                } else {
                    loadDashboardTopFoods(servedOrderIds);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                resetDashboard();
            }
        });

        dbService.getUsersByRole("eq.user", "*", "created_at.desc").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvDashboardNewCustomers.setText("0");
                    return;
                }
                String todayIso = isoFormat.format(now.getTime());
                int newToday = 0;
                for (User user : response.body()) {
                    if (todayIso.equals(safeDate(user.getCreatedAt()))) {
                        newToday++;
                    }
                }
                tvDashboardNewCustomers.setText(String.valueOf(newToday));
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                tvDashboardNewCustomers.setText("0");
            }
        });
    }

    private LinkedHashMap<String, Double> initDailyRevenueMap(Calendar start7) {
        LinkedHashMap<String, Double> dayRevenue = new LinkedHashMap<>();
        Calendar cursor = (Calendar) start7.clone();
        for (int i = 0; i < 7; i++) {
            dayRevenue.put(isoFormat.format(cursor.getTime()), 0.0);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dayRevenue;
    }

    private void updateDashboardChart(LinkedHashMap<String, Double> dayRevenue) {
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
                if (value <= 0f) return "";
                return formatCompactCurrency(value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.65f);
        barChartDashboard7Days.setData(data);
        barChartDashboard7Days.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartDashboard7Days.getXAxis().setLabelCount(labels.size(), true);
        barChartDashboard7Days.invalidate();
    }

    private void loadDashboardTopFoods(List<String> servedOrderIds) {
        String orderFilter = buildInFilter(servedOrderIds);
        dbService.getOrderItems(orderFilter, "*").enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    tvDashboardTop5Foods.setText("Top 5 món: chưa có dữ liệu");
                    return;
                }
                List<TopFoodStat> topFoods = aggregateTopFoods(response.body());
                int max = Math.min(5, topFoods.size());
                StringBuilder sb = new StringBuilder("Top 5 món: ");
                for (int i = 0; i < max; i++) {
                    if (i > 0) sb.append(" | ");
                    sb.append(i + 1).append(". ").append(topFoods.get(i).name);
                }
                tvDashboardTop5Foods.setText(sb.toString());
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                tvDashboardTop5Foods.setText("Top 5 món: chưa có dữ liệu");
            }
        });
    }

    private void loadReportAndCustomerStats() {
        String fromStr = isoFormat.format(reportFrom.getTime()) + "T00:00:00";
        String toStr = isoFormat.format(reportTo.getTime()) + "T23:59:59";

        Map<String, String> filters = new HashMap<>();
        filters.put("and", "(created_at.gte." + fromStr + ",created_at.lte." + toStr + ")");

        dbService.getOrdersByDateRange(filters, "*").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resetReportSection();
                    resetCustomerStats();
                    setCustomerRefreshLoading(false);
                    return;
                }

                List<Order> validOrders = new ArrayList<>();
                for (Order order : response.body()) {
                    if (order.getCreatedAt() != null && order.getCreatedAt().compareTo(toStr) <= 0) {
                        validOrders.add(order);
                    }
                }
                cachedRangeOrders = validOrders;

                reportTotalOrders = validOrders.size();
                reportCompletedOrders = 0;
                reportRevenueValue = 0;
                List<String> servedOrderIds = new ArrayList<>();

                for (Order order : validOrders) {
                    if ("served".equals(order.getStatus())) {
                        reportCompletedOrders++;
                        reportRevenueValue += order.getTotalAmount();
                        if (order.getId() != null) {
                            servedOrderIds.add(order.getId());
                        }
                    }
                }

                tvReportRevenue.setText(nf.format(reportRevenueValue) + " VNĐ");
                tvReportCompletedOrders.setText("Đơn hoàn thành: " + reportCompletedOrders);
                tvReportTotalOrders.setText("Tổng đơn: " + reportTotalOrders);

                if (servedOrderIds.isEmpty()) {
                    reportTopFoods.clear();
                    tvReportTopFoods.setText("Top món: chưa có dữ liệu");
                } else {
                    loadReportTopFoods(servedOrderIds);
                }

                loadCustomerStatistics(validOrders);
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                resetReportSection();
                resetCustomerStats();
                setCustomerRefreshLoading(false);
            }
        });
    }

    private void loadReportTopFoods(List<String> servedOrderIds) {
        String orderFilter = buildInFilter(servedOrderIds);
        dbService.getOrderItems(orderFilter, "*").enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    reportTopFoods.clear();
                    tvReportTopFoods.setText("Top món: chưa có dữ liệu");
                    return;
                }

                reportTopFoods.clear();
                reportTopFoods.addAll(aggregateTopFoods(response.body()));
                int max = Math.min(5, reportTopFoods.size());
                StringBuilder sb = new StringBuilder("Top món: ");
                for (int i = 0; i < max; i++) {
                    if (i > 0) sb.append(" | ");
                    TopFoodStat stat = reportTopFoods.get(i);
                    sb.append(i + 1).append(". ").append(stat.name).append(" (").append(stat.quantity).append(")");
                }
                tvReportTopFoods.setText(sb.toString());
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                reportTopFoods.clear();
                tvReportTopFoods.setText("Top món: chưa có dữ liệu");
            }
        });
    }

    private List<TopFoodStat> aggregateTopFoods(List<OrderItem> items) {
        Map<String, int[]> qtyMap = new HashMap<>();
        Map<String, double[]> revenueMap = new HashMap<>();

        for (OrderItem item : items) {
            String name = item.getFoodName();
            if (name == null || name.trim().isEmpty()) {
                name = "Món không tên";
            }
            if (!qtyMap.containsKey(name)) {
                qtyMap.put(name, new int[]{0});
                revenueMap.put(name, new double[]{0});
            }
            qtyMap.get(name)[0] += item.getQuantity();
            revenueMap.get(name)[0] += item.getSubtotal();
        }

        List<TopFoodStat> result = new ArrayList<>();
        for (String name : qtyMap.keySet()) {
            result.add(new TopFoodStat(name, qtyMap.get(name)[0], revenueMap.get(name)[0]));
        }

        Collections.sort(result, (a, b) -> {
            if (b.quantity != a.quantity) return Integer.compare(b.quantity, a.quantity);
            return Double.compare(b.revenue, a.revenue);
        });
        return result;
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
        final String fromIso = isoFormat.format(reportFrom.getTime());
        final String toIso = isoFormat.format(reportTo.getTime());

        dbService.getUsersByRole("eq.user", "*", "created_at.desc").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resetCustomerStats();
                    setCustomerRefreshLoading(false);
                    return;
                }

                List<User> users = response.body();
                int newRange = 0;
                String topBuyerName = "-";

                for (User user : users) {
                    String createdDate = safeDate(user.getCreatedAt());
                    if (!createdDate.isEmpty() && createdDate.compareTo(fromIso) >= 0 && createdDate.compareTo(toIso) <= 0) {
                        newRange++;
                    }
                    if (!finalTopBuyerId.isEmpty() && finalTopBuyerId.equals(user.getId())) {
                        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                            topBuyerName = user.getFullName();
                        } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                            topBuyerName = user.getEmail();
                        } else {
                            topBuyerName = "Khách hàng";
                        }
                    }
                }

                tvCustomerTotal.setText("Tổng khách: " + users.size());
                tvCustomerNewRange.setText("Khách mới (khoảng lọc): " + newRange);
                if (finalMaxOrders > 0) {
                    tvCustomerTopBuyer.setText("Khách mua nhiều: " + topBuyerName + " (" + finalMaxOrders + " đơn)");
                } else {
                    tvCustomerTopBuyer.setText("Khách mua nhiều: -");
                }

                int uniqueBuyers = ordersByUser.size();
                if (uniqueBuyers > 0) {
                    double frequency = (double) finalTotalOrdersInRange / uniqueBuyers;
                    tvCustomerFrequency.setText(String.format(Locale.getDefault(), "Tần suất mua: %.1f đơn/khách", frequency));
                } else {
                    tvCustomerFrequency.setText("Tần suất mua: 0 đơn/khách");
                }
                setCustomerRefreshLoading(false);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                resetCustomerStats();
                setCustomerRefreshLoading(false);
            }
        });
    }

    private void exportReportPdf() {
        Uri uri = createReportPdf(false);
        if (uri != null) {
            Toast.makeText(this, "Đã xuất PDF vào Downloads/FoodOrderReports", Toast.LENGTH_LONG).show();
        }
    }

    private void exportReportExcel() {
        String fileName = "BaoCaoThongKe_Admin_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime()) + ".csv";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FoodOrderReports");

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, "Không thể tạo file Excel", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Bao cao & thong ke admin\n");
        csv.append("Khoang ngay,").append(btnReportDateFrom.getText()).append(" - ").append(btnReportDateTo.getText()).append("\n");
        csv.append("Tong doanh thu,").append(nf.format(reportRevenueValue)).append(" VNĐ\n");
        csv.append("Don hoan thanh,").append(reportCompletedOrders).append("\n");
        csv.append("Tong don,").append(reportTotalOrders).append("\n");
        csv.append("Tong khach,").append(tvCustomerTotal.getText().toString().replace("Tổng khách: ", "")).append("\n");
        csv.append("Khach moi,").append(tvCustomerNewRange.getText().toString().replace("Khách mới (khoảng lọc): ", "")).append("\n\n");
        csv.append("Top mon\n");
        csv.append("STT,Ten mon,So luong,Doanh thu\n");

        for (int i = 0; i < reportTopFoods.size(); i++) {
            TopFoodStat item = reportTopFoods.get(i);
            csv.append(i + 1).append(",")
                    .append(item.name.replace(",", " ")).append(",")
                    .append(item.quantity).append(",")
                    .append(nf.format(item.revenue)).append("\n");
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                Toast.makeText(this, "Không thể mở file để ghi", Toast.LENGTH_SHORT).show();
                return;
            }
            outputStream.write(csv.toString().getBytes());
            outputStream.flush();
            Toast.makeText(this, "Đã xuất Excel vào Downloads/FoodOrderReports", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi xuất Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendReportEmail() {
        Uri pdfUri = createReportPdf(true);
        if (pdfUri == null) {
            Toast.makeText(this, "Không thể tạo tệp PDF để gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Báo cáo & Thống kê Admin");
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Báo cáo đính kèm.\n" +
                        "Khoảng ngày: " + btnReportDateFrom.getText() + " - " + btnReportDateTo.getText() + "\n" +
                        "Tổng doanh thu: " + nf.format(reportRevenueValue) + " VNĐ\n" +
                        "Đơn hoàn thành: " + reportCompletedOrders + "\n" +
                        "Tổng đơn: " + reportTotalOrders);
        emailIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(emailIntent, "Gửi báo cáo qua email"));
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createReportPdf(boolean silent) {
        String fileName = "BaoCaoThongKe_Admin_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime()) + ".pdf";

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

        page.getCanvas().drawText("BAO CAO & THONG KE ADMIN", x, y, titlePaint);
        y += 24;
        page.getCanvas().drawText("Khoang ngay: " + btnReportDateFrom.getText() + " - " + btnReportDateTo.getText(), x, y, bodyPaint);
        y += 16;
        page.getCanvas().drawText("Ngay xuat: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime()), x, y, bodyPaint);
        y += 20;

        page.getCanvas().drawLine(x, y, 555, y, bodyPaint);
        y += 22;

        page.getCanvas().drawText("Tong quan", x, y, headingPaint);
        y += 18;
        page.getCanvas().drawText("- Tong doanh thu: " + nf.format(reportRevenueValue) + " VNĐ", x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- Don hoan thanh: " + reportCompletedOrders, x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- Tong don: " + reportTotalOrders, x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- " + tvCustomerTotal.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- " + tvCustomerNewRange.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- " + tvCustomerTopBuyer.getText(), x, y, bodyPaint);
        y += 15;
        page.getCanvas().drawText("- " + tvCustomerFrequency.getText(), x, y, bodyPaint);
        y += 20;

        page.getCanvas().drawText("Top mon theo khoang loc", x, y, headingPaint);
        y += 16;

        if (reportTopFoods.isEmpty()) {
            page.getCanvas().drawText("- Chua co du lieu top mon.", x, y, bodyPaint);
        } else {
            int max = Math.min(10, reportTopFoods.size());
            for (int i = 0; i < max; i++) {
                TopFoodStat item = reportTopFoods.get(i);
                String line = String.format(Locale.getDefault(),
                        "%d. %s | So luong: %d | Doanh thu: %s VNĐ",
                        i + 1,
                        item.name,
                        item.quantity,
                        nf.format(item.revenue));
                page.getCanvas().drawText(line, x, y, bodyPaint);
                y += 15;
                if (y > 800) break;
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
                Toast.makeText(this, "Không thể tạo file PDF", Toast.LENGTH_SHORT).show();
            }
            document.close();
            return null;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                if (!silent) {
                    Toast.makeText(this, "Không thể mở file để ghi", Toast.LENGTH_SHORT).show();
                }
                return null;
            }
            document.writeTo(outputStream);
            return uri;
        } catch (IOException e) {
            if (!silent) {
                Toast.makeText(this, "Lỗi xuất PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        } finally {
            document.close();
        }
    }

    private String buildInFilter(List<String> ids) {
        StringBuilder sb = new StringBuilder("in.(");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ids.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    private String safeDate(String timestamp) {
        if (timestamp == null || timestamp.length() < 10) return "";
        return timestamp.substring(0, 10);
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

    private void resetDashboard() {
        tvDashboardRevenueToday.setText("0 VNĐ");
        tvDashboardOrdersToday.setText("0");
        tvDashboardNewCustomers.setText("0");
        tvDashboardTop5Foods.setText("Top 5 món: chưa có dữ liệu");
        barChartDashboard7Days.clear();
        barChartDashboard7Days.invalidate();
    }

    private void resetReportSection() {
        reportRevenueValue = 0;
        reportCompletedOrders = 0;
        reportTotalOrders = 0;
        reportTopFoods.clear();
        tvReportRevenue.setText("0 VNĐ");
        tvReportCompletedOrders.setText("Đơn hoàn thành: 0");
        tvReportTotalOrders.setText("Tổng đơn: 0");
        tvReportTopFoods.setText("Top món: chưa có dữ liệu");
    }

    private void resetCustomerStats() {
        tvCustomerTotal.setText("Tổng khách: 0");
        tvCustomerNewRange.setText("Khách mới (khoảng lọc): 0");
        tvCustomerTopBuyer.setText("Khách mua nhiều: -");
        tvCustomerFrequency.setText("Tần suất mua: 0 đơn/khách");
    }

    private void setCustomerRefreshLoading(boolean loading) {
        isRefreshingCustomerStats = loading;
        btnOpenCustomerStats.setEnabled(!loading);
        btnOpenCustomerStats.setText(loading ? "Đang thống kê..." : "Thống kê khách hàng");
    }

    private static class TopFoodStat {
        final String name;
        final int quantity;
        final double revenue;

        TopFoodStat(String name, int quantity, double revenue) {
            this.name = name;
            this.quantity = quantity;
            this.revenue = revenue;
        }
    }
}
