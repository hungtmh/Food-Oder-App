package com.example.food_order_app.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CategoryProductStatAdapter;
import com.example.food_order_app.model.CategoryProductStat;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private String categoryId;
    private String categoryName;

    private TextView tvCategoryDetailTitle;
    private TextView tvCategorySummary;
    private TextView tvCategoryProductsEmpty;
    private RecyclerView rvCategoryProducts;

    private final List<Food> foods = new ArrayList<>();
    private final Map<String, Integer> soldByFood = new HashMap<>();
    private final Map<String, Double> revenueByFood = new HashMap<>();

    private SupabaseDbService dbService;
    private CategoryProductStatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category_detail);

        dbService = RetrofitClient.getDbService();

        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        if (TextUtils.isEmpty(categoryId)) {
            Toast.makeText(this, "Không tìm thấy danh mục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecycler();
        loadData();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBackCategoryDetail);
        tvCategoryDetailTitle = findViewById(R.id.tvCategoryDetailTitle);
        tvCategorySummary = findViewById(R.id.tvCategorySummary);
        tvCategoryProductsEmpty = findViewById(R.id.tvCategoryProductsEmpty);
        rvCategoryProducts = findViewById(R.id.rvCategoryProducts);

        String title = TextUtils.isEmpty(categoryName) ? "Chi tiết danh mục" : "Danh mục: " + categoryName;
        tvCategoryDetailTitle.setText(title);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        rvCategoryProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryProductStatAdapter();
        rvCategoryProducts.setAdapter(adapter);
    }

    private void loadData() {
        loadFoodsByCategory(true);
    }

    private void loadFoodsByCategory(boolean includeStock) {
        String select = includeStock ? "id,name,price,is_available,stock_quantity" : "id,name,price,is_available";
        dbService.getFoodsByCategoryFilter("eq." + categoryId, select, "name.asc")
                .enqueue(new Callback<List<Food>>() {
                    @Override
                    public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            if (includeStock) {
                                loadFoodsByCategory(false);
                                return;
                            }
                            Toast.makeText(AdminCategoryDetailActivity.this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        foods.clear();
                        foods.addAll(response.body());
                        loadSoldStats();
                    }

                    @Override
                    public void onFailure(Call<List<Food>> call, Throwable t) {
                        if (includeStock) {
                            loadFoodsByCategory(false);
                            return;
                        }
                        Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSoldStats() {
        dbService.getAllOrders("id,status", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    renderData();
                    return;
                }

                List<Order> validOrders = new ArrayList<>();
                for (Order order : response.body()) {
                    if (!"cancelled".equalsIgnoreCase(order.getStatus())) {
                        validOrders.add(order);
                    }
                }

                if (validOrders.isEmpty()) {
                    renderData();
                    return;
                }

                StringBuilder inBuilder = new StringBuilder("in.(");
                for (int i = 0; i < validOrders.size(); i++) {
                    inBuilder.append(validOrders.get(i).getId());
                    if (i < validOrders.size() - 1) {
                        inBuilder.append(",");
                    }
                }
                inBuilder.append(")");

                dbService.getOrderItems(inBuilder.toString(), "food_id,quantity,subtotal")
                        .enqueue(new Callback<List<OrderItem>>() {
                            @Override
                            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                                soldByFood.clear();
                                revenueByFood.clear();

                                Set<String> categoryFoodIds = new HashSet<>();
                                for (Food food : foods) {
                                    if (!TextUtils.isEmpty(food.getId())) {
                                        categoryFoodIds.add(food.getId());
                                    }
                                }

                                if (response.isSuccessful() && response.body() != null) {
                                    for (OrderItem item : response.body()) {
                                        if (TextUtils.isEmpty(item.getFoodId()) || !categoryFoodIds.contains(item.getFoodId())) {
                                            continue;
                                        }
                                        soldByFood.put(item.getFoodId(), soldByFood.getOrDefault(item.getFoodId(), 0) + item.getQuantity());
                                        revenueByFood.put(item.getFoodId(), revenueByFood.getOrDefault(item.getFoodId(), 0d) + item.getSubtotal());
                                    }
                                }
                                renderData();
                            }

                            @Override
                            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                                renderData();
                            }
                        });
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                renderData();
            }
        });
    }

    private void renderData() {
        List<CategoryProductStat> stats = new ArrayList<>();
        int totalFoods = foods.size();
        int activeFoods = 0;
        double totalRevenue = 0d;

        for (Food food : foods) {
            if (food.isAvailable()) {
                activeFoods++;
            }
            int soldQty = soldByFood.getOrDefault(food.getId(), 0);
            double revenue = revenueByFood.getOrDefault(food.getId(), 0d);
            totalRevenue += revenue;

            stats.add(new CategoryProductStat(
                    food.getName() == null ? "(Chưa có tên)" : food.getName(),
                    food.isAvailable(),
                    soldQty,
                    revenue,
                    food.getStockQuantity()
            ));
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvCategorySummary.setText("Tổng món: " + totalFoods
                + " | Đang bán: " + activeFoods
                + " | Doanh thu: " + nf.format(totalRevenue) + "đ");

        adapter.setItems(stats);
        tvCategoryProductsEmpty.setVisibility(stats.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
