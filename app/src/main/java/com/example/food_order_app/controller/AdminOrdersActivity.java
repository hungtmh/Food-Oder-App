package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminOrderAdapter;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private EditText edtSearch;
    private ImageView btnSearch;
    private TextView tvEmpty, tvStats;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabRevenue;
    private Button btnStatusAll, btnStatusPending, btnStatusConfirmed, btnStatusDelivered, btnStatusCancelled;

    private AdminOrderAdapter adapter;
    private SupabaseDbService dbService;
    private String currentFilter = "all";
    private List<Order> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        dbService = RetrofitClient.getDbService();
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_admin_orders);
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvAdminOrders);
        edtSearch = findViewById(R.id.edtOrderSearch);
        btnSearch = findViewById(R.id.btnOrderSearch);
        tvEmpty = findViewById(R.id.tvOrderEmpty);
        tvStats = findViewById(R.id.tvOrderStats);
        bottomNav = findViewById(R.id.adminBottomNav);
        btnStatusAll = findViewById(R.id.btnStatusAll);
        btnStatusPending = findViewById(R.id.btnStatusPending);
        btnStatusConfirmed = findViewById(R.id.btnStatusConfirmed);
        btnStatusDelivered = findViewById(R.id.btnStatusDelivered);
        btnStatusCancelled = findViewById(R.id.btnStatusCancelled);
        fabRevenue = findViewById(R.id.fabRevenue);

        adapter = new AdminOrderAdapter(this, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupListeners() {
        View.OnClickListener filterClick = v -> {
            int id = v.getId();
            if (id == R.id.btnStatusAll) currentFilter = "all";
            else if (id == R.id.btnStatusPending) currentFilter = "pending";
            else if (id == R.id.btnStatusConfirmed) currentFilter = "confirmed";
            else if (id == R.id.btnStatusDelivered) currentFilter = "delivered";
            else if (id == R.id.btnStatusCancelled) currentFilter = "cancelled";
            updateFilterUI();
            filterOrders();
        };

        btnStatusAll.setOnClickListener(filterClick);
        btnStatusPending.setOnClickListener(filterClick);
        btnStatusConfirmed.setOnClickListener(filterClick);
        btnStatusDelivered.setOnClickListener(filterClick);
        btnStatusCancelled.setOnClickListener(filterClick);

        btnSearch.setOnClickListener(v -> searchOrders());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchOrders();
                return true;
            }
            return false;
        });

        fabRevenue.setOnClickListener(v ->
                startActivity(new Intent(this, AdminRevenueActivity.class)));

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_food) {
                startActivity(new Intent(this, AdminHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin_feedback) {
                startActivity(new Intent(this, AdminFeedbackActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_admin_orders) {
                return true;
            } else if (id == R.id.nav_admin_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void updateFilterUI() {
        Button[] buttons = {btnStatusAll, btnStatusPending, btnStatusConfirmed, btnStatusDelivered, btnStatusCancelled};
        String[] filters = {"all", "pending", "confirmed", "delivered", "cancelled"};
        for (int i = 0; i < buttons.length; i++) {
            boolean selected = currentFilter.equals(filters[i]);
            buttons[i].setBackgroundResource(selected ? R.drawable.bg_category_selected : R.drawable.bg_category_normal);
            buttons[i].setTextColor(getResources().getColor(selected ? R.color.white : R.color.text_primary));
        }
    }

    private void loadOrders() {
        dbService.getAllOrders("*", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    tvStats.setText("Tổng: " + allOrders.size() + " đơn");
                    filterOrders();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(AdminOrdersActivity.this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
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
        adapter.setOrders(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void searchOrders() {
        String query = edtSearch.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            filterOrders();
            return;
        }
        List<Order> results = new ArrayList<>();
        for (Order o : allOrders) {
            if ((o.getOrderCode() != null && o.getOrderCode().toLowerCase().contains(query)) ||
                    (o.getReceiverName() != null && o.getReceiverName().toLowerCase().contains(query))) {
                results.add(o);
            }
        }
        adapter.setOrders(results);
        tvEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        intent.putExtra("order_code", order.getOrderCode());
        intent.putExtra("order_status", order.getStatus());
        intent.putExtra("order_customer", order.getReceiverName());
        intent.putExtra("order_phone", order.getPhone());
        intent.putExtra("order_address", order.getAddress());
        intent.putExtra("order_payment", order.getPaymentMethod());
        intent.putExtra("order_note", order.getNote());
        intent.putExtra("order_subtotal", order.getSubtotal());
        intent.putExtra("order_discount", order.getDiscountAmount());
        intent.putExtra("order_total", order.getTotalAmount());
        intent.putExtra("order_date", order.getCreatedAt());
        startActivity(intent);
    }
}
