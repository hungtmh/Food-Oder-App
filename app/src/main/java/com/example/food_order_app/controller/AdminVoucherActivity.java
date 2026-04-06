package com.example.food_order_app.controller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminVoucherAdapter;
import com.example.food_order_app.model.Voucher;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.AdminDrawerHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminVoucherActivity extends AppCompatActivity implements AdminVoucherAdapter.OnVoucherActionListener {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvVouchers;
    private EditText edtSearch;
    private ImageView btnSearch;
    private ImageView btnMenuDrawer;
    private TextView tvEmpty;
    private Button btnFilterAll;
    private Button btnFilterActive;
    private Button btnFilterInactive;
    private FloatingActionButton fabAdd;

    private SupabaseDbService dbService;
    private AdminVoucherAdapter adapter;
    private List<Voucher> allVouchers = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_voucher);

        dbService = RetrofitClient.getDbService();

        initViews();
        setupRecycler();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVouchers();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
        rvVouchers = findViewById(R.id.rvAdminVouchers);
        edtSearch = findViewById(R.id.edtVoucherSearch);
        btnSearch = findViewById(R.id.btnVoucherSearch);
        btnMenuDrawer = findViewById(R.id.btnMenuDrawerVoucher);
        tvEmpty = findViewById(R.id.tvVoucherEmpty);
        btnFilterAll = findViewById(R.id.btnFilterVoucherAll);
        btnFilterActive = findViewById(R.id.btnFilterVoucherActive);
        btnFilterInactive = findViewById(R.id.btnFilterVoucherInactive);
        fabAdd = findViewById(R.id.fabAddVoucher);

        AdminDrawerHelper.setupDrawer(this, drawerLayout, navigationView, btnMenuDrawer, R.id.navAdminVoucher);
    }

    private void setupRecycler() {
        adapter = new AdminVoucherAdapter(this, this);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvVouchers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> applyFilters());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterUI();
            applyFilters();
        });

        btnFilterActive.setOnClickListener(v -> {
            currentFilter = "active";
            updateFilterUI();
            applyFilters();
        });

        btnFilterInactive.setOnClickListener(v -> {
            currentFilter = "inactive";
            updateFilterUI();
            applyFilters();
        });

        fabAdd.setOnClickListener(v -> showCreateVoucherDialog());
    }

    private void loadVouchers() {
        String select = "id,code,title,description,discount_type,discount_value,max_discount_amount,min_order_value,start_date,end_date,usage_limit,used_count,limit_per_user,is_active,is_public";
        dbService.getAdminVouchers(select, "created_at.desc").enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allVouchers = response.body();
                    applyFilters();
                } else {
                    Toast.makeText(AdminVoucherActivity.this, "Không thể tải danh sách mã", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(AdminVoucherActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = normalizeText(edtSearch.getText().toString().trim());
        List<Voucher> filtered = new ArrayList<>();

        for (Voucher voucher : allVouchers) {
            if (voucher == null) {
                continue;
            }

            if ("active".equals(currentFilter) && !voucher.isActive()) {
                continue;
            }
            if ("inactive".equals(currentFilter) && voucher.isActive()) {
                continue;
            }

            if (!query.isEmpty()) {
                String code = normalizeText(voucher.getCode());
                String title = normalizeText(voucher.getTitle());
                if (!code.contains(query) && !title.contains(query)) {
                    continue;
                }
            }

            filtered.add(voucher);
        }

        adapter.setVouchers(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateFilterUI() {
        Button[] buttons = { btnFilterAll, btnFilterActive, btnFilterInactive };
        String[] values = { "all", "active", "inactive" };
        for (int i = 0; i < buttons.length; i++) {
            boolean selected = values[i].equals(currentFilter);
            buttons[i]
                    .setBackgroundResource(selected ? R.drawable.bg_category_selected : R.drawable.bg_category_normal);
            buttons[i].setTextColor(getResources().getColor(selected ? R.color.white : R.color.text_primary));
        }
    }

    private void showCreateVoucherDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_voucher, null, false);

        TextInputEditText edtCode = dialogView.findViewById(R.id.edtVoucherCode);
        TextInputEditText edtTitle = dialogView.findViewById(R.id.edtVoucherTitle);
        TextInputEditText edtValue = dialogView.findViewById(R.id.edtVoucherValue);
        TextInputEditText edtMaxDiscount = dialogView.findViewById(R.id.edtVoucherMaxDiscount);
        TextInputEditText edtMinOrder = dialogView.findViewById(R.id.edtVoucherMinOrder);
        TextInputEditText edtUsageLimit = dialogView.findViewById(R.id.edtVoucherUsageLimit);
        TextInputEditText edtStartDate = dialogView.findViewById(R.id.edtVoucherStartDate);
        TextInputEditText edtEndDate = dialogView.findViewById(R.id.edtVoucherEndDate);
        Spinner spDiscountType = dialogView.findViewById(R.id.spVoucherDiscountType);

        ArrayAdapter<String> discountTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] { "Phần trăm", "Số tiền" });
        spDiscountType.setAdapter(discountTypeAdapter);

        spDiscountType.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> {
            boolean percent = spDiscountType.getSelectedItemPosition() == 0;
            edtMaxDiscount.setEnabled(percent);
            if (!percent) {
                edtMaxDiscount.setText("");
            }
        }));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tạo mã giảm giá")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = valueOf(edtCode).toUpperCase(Locale.ROOT);
            String title = valueOf(edtTitle);
            String valueText = valueOf(edtValue);
            String maxDiscountText = valueOf(edtMaxDiscount);
            String minOrderText = valueOf(edtMinOrder);
            String usageLimitText = valueOf(edtUsageLimit);
            String startDateText = valueOf(edtStartDate);
            String endDateText = valueOf(edtEndDate);

            if (code.isEmpty() || title.isEmpty() || valueText.isEmpty() || startDateText.isEmpty()
                    || endDateText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDate(startDateText) || !isValidDate(endDateText)) {
                Toast.makeText(this, "Ngày phải đúng định dạng yyyy-MM-dd", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date start = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDateText);
                Date end = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDateText);
                if (start != null && end != null && end.before(start)) {
                    Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException ignored) {
                Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            double discountValue;
            double minOrderValue;
            Double maxDiscountValue = null;
            Integer usageLimit = null;

            try {
                discountValue = Double.parseDouble(valueText);
                minOrderValue = minOrderText.isEmpty() ? 0 : Double.parseDouble(minOrderText);
                if (!usageLimitText.isEmpty()) {
                    usageLimit = Integer.parseInt(usageLimitText);
                }
                if (spDiscountType.getSelectedItemPosition() == 0 && !maxDiscountText.isEmpty()) {
                    maxDiscountValue = Double.parseDouble(maxDiscountText);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (discountValue <= 0) {
                Toast.makeText(this, "Giá trị giảm phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spDiscountType.getSelectedItemPosition() == 0 && discountValue > 100) {
                Toast.makeText(this, "Giảm phần trăm không vượt quá 100", Toast.LENGTH_SHORT).show();
                return;
            }

            String discountType = spDiscountType.getSelectedItemPosition() == 0 ? "percent" : "fixed_amount";

            createVoucher(code, title, discountType, discountValue, maxDiscountValue, minOrderValue, usageLimit,
                    startDateText + "T00:00:00Z", endDateText + "T23:59:59Z", dialog);
        }));

        dialog.show();
    }

    private void createVoucher(String code,
            String title,
            String discountType,
            double discountValue,
            Double maxDiscountValue,
            double minOrderValue,
            Integer usageLimit,
            String startDate,
            String endDate,
            AlertDialog dialog) {
        dbService.getVoucherByCode("eq." + code, null).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(AdminVoucherActivity.this, "Không kiểm tra được mã trùng", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(AdminVoucherActivity.this, "Mã đã tồn tại", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("code", code);
                payload.put("title", title);
                payload.put("description", "");
                payload.put("discount_type", discountType);
                payload.put("discount_value", Math.round(discountValue));
                payload.put("max_discount_amount", maxDiscountValue == null ? null : Math.round(maxDiscountValue));
                payload.put("min_order_value", Math.round(minOrderValue));
                payload.put("start_date", startDate);
                payload.put("end_date", endDate);
                payload.put("usage_limit", usageLimit);
                payload.put("limit_per_user", 1);
                payload.put("is_active", true);
                payload.put("is_public", true);

                dbService.createVoucher(payload).enqueue(new Callback<List<Voucher>>() {
                    @Override
                    public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminVoucherActivity.this, "Tạo mã thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadVouchers();
                        } else {
                            Toast.makeText(AdminVoucherActivity.this, "Tạo mã thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Voucher>> call, Throwable t) {
                        Toast.makeText(AdminVoucherActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(AdminVoucherActivity.this, "Lỗi kiểm tra mã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onToggleActive(Voucher voucher, boolean isActive) {
        if (voucher == null || TextUtils.isEmpty(voucher.getId())) {
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_active", isActive);

        dbService.updateVoucher("eq." + voucher.getId(), updates).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminVoucherActivity.this,
                            isActive ? "Đã bật mã" : "Đã vô hiệu hóa mã",
                            Toast.LENGTH_SHORT).show();
                    loadVouchers();
                } else {
                    Toast.makeText(AdminVoucherActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(AdminVoucherActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                loadVouchers();
            }
        });
    }

    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String removed = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        return removed.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    private boolean isValidDate(String value) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private String valueOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable callback;

        SimpleItemSelectedListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            if (callback != null) {
                callback.run();
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
