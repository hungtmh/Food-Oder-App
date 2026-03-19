package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminFoodAdapter;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.AdminBottomNavHelper;
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeActivity extends AppCompatActivity implements AdminFoodAdapter.OnAdminFoodListener {

    private RecyclerView rvFoods;
    private EditText edtSearch;
    private ImageView btnSearch, btnAIInsights;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private Button btnFilterAll, btnFilterAvailable, btnFilterUnavailable, btnFilterPopular, btnFilterDiscount;

    private AdminFoodAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private List<Food> allFoods = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdminBottomNavHelper.setup(this, AdminBottomNavHelper.TAB_FOOD);
        loadFoods();
    }

    private void initViews() {
        rvFoods = findViewById(R.id.rvAdminFoods);
        edtSearch = findViewById(R.id.edtAdminSearch);
        btnSearch = findViewById(R.id.btnAdminSearch);
        btnAIInsights = findViewById(R.id.btnAIInsights);
        tvEmpty = findViewById(R.id.tvAdminEmpty);
        fabAdd = findViewById(R.id.fabAddFood);

        adapter = new AdminFoodAdapter(this, this);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(adapter);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterAvailable = findViewById(R.id.btnFilterAvailable);
        btnFilterUnavailable = findViewById(R.id.btnFilterUnavailable);
        btnFilterPopular = findViewById(R.id.btnFilterPopular);
        btnFilterDiscount = findViewById(R.id.btnFilterDiscount);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAddEditFoodActivity.class));
        });
        
        btnAIInsights.setOnClickListener(v -> {
            startActivity(new Intent(this, AIDashboardActivity.class));
        });

        btnSearch.setOnClickListener(v -> performSearch());

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Filter button click handlers
        android.view.View.OnClickListener filterClick = v -> {
            int id = v.getId();
            if (id == R.id.btnFilterAll) currentFilter = "all";
            else if (id == R.id.btnFilterAvailable) currentFilter = "available";
            else if (id == R.id.btnFilterUnavailable) currentFilter = "unavailable";
            else if (id == R.id.btnFilterPopular) currentFilter = "popular";
            else if (id == R.id.btnFilterDiscount) currentFilter = "discount";
            updateFilterUI();
            performSearch();
        };
        btnFilterAll.setOnClickListener(filterClick);
        btnFilterAvailable.setOnClickListener(filterClick);
        btnFilterUnavailable.setOnClickListener(filterClick);
        btnFilterPopular.setOnClickListener(filterClick);
        btnFilterDiscount.setOnClickListener(filterClick);
    }

    private void loadFoods() {
        dbService.getAdminAllFoods("*", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFoods = response.body();
                    adapter.setFoods(allFoods);
                    tvEmpty.setVisibility(allFoods.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Toast.makeText(AdminHomeActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String query = removeDiacritics(edtSearch.getText().toString().trim().toLowerCase());

        // First apply filter
        List<Food> filtered = filterFoods(allFoods);

        // Then apply search if query is not empty
        if (!query.isEmpty()) {
            List<Food> results = new ArrayList<>();
            for (Food food : filtered) {
                if (food.getName() != null && removeDiacritics(food.getName().toLowerCase()).contains(query)) {
                    results.add(food);
                    continue;
                }
                if (food.getDescription() != null && removeDiacritics(food.getDescription().toLowerCase()).contains(query)) {
                    results.add(food);
                }
            }
            filtered = results;
        }

        adapter.setFoods(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private List<Food> filterFoods(List<Food> foods) {
        if (currentFilter.equals("all")) return foods;

        List<Food> filtered = new ArrayList<>();
        for (Food food : foods) {
            switch (currentFilter) {
                case "available":
                    if (food.isAvailable()) filtered.add(food);
                    break;
                case "unavailable":
                    if (!food.isAvailable()) filtered.add(food);
                    break;
                case "popular":
                    if (food.isPopular()) filtered.add(food);
                    break;
                case "discount":
                    if (food.getDiscountPercent() > 0) filtered.add(food);
                    break;
            }
        }
        return filtered;
    }

    private void updateFilterUI() {
        Button[] buttons = {btnFilterAll, btnFilterAvailable, btnFilterUnavailable, btnFilterPopular, btnFilterDiscount};
        String[] filters = {"all", "available", "unavailable", "popular", "discount"};
        for (int i = 0; i < buttons.length; i++) {
            boolean selected = currentFilter.equals(filters[i]);
            buttons[i].setBackgroundResource(selected ? R.drawable.bg_category_selected : R.drawable.bg_category_normal);
            buttons[i].setTextColor(getResources().getColor(selected ? R.color.white : R.color.text_primary));
        }
    }

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private String removeDiacritics(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String result = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        result = result.replace('đ', 'd').replace('Đ', 'D');
        return result;
    }

    @Override
    public void onEditFood(Food food) {
        Intent intent = new Intent(this, AdminAddEditFoodActivity.class);
        intent.putExtra("food_id", food.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteFood(Food food) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có chắc muốn xóa \"" + food.getName() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> {
                    dbService.deleteFood("eq." + food.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminHomeActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadFoods();
                            } else {
                                Toast.makeText(AdminHomeActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(AdminHomeActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onFoodClick(Food food) {
        onEditFood(food);
    }
}
