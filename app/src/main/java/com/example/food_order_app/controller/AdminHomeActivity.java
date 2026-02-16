package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
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
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeActivity extends AppCompatActivity implements AdminFoodAdapter.OnAdminFoodListener {

    private RecyclerView rvFoods;
    private EditText edtSearch;
    private ImageView btnSearch;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;

    private AdminFoodAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

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
        bottomNav.setSelectedItemId(R.id.nav_admin_food);
        loadFoods();
    }

    private void initViews() {
        rvFoods = findViewById(R.id.rvAdminFoods);
        edtSearch = findViewById(R.id.edtAdminSearch);
        btnSearch = findViewById(R.id.btnAdminSearch);
        tvEmpty = findViewById(R.id.tvAdminEmpty);
        fabAdd = findViewById(R.id.fabAddFood);
        bottomNav = findViewById(R.id.adminBottomNav);

        adapter = new AdminFoodAdapter(this, this);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAddEditFoodActivity.class));
        });

        btnSearch.setOnClickListener(v -> performSearch());

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_food) {
                return true;
            } else if (id == R.id.nav_admin_feedback) {
                startActivity(new Intent(this, AdminFeedbackActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_admin_orders) {
                startActivity(new Intent(this, AdminOrdersActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_admin_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadFoods() {
        dbService.getAdminAllFoods("*", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = response.body();
                    adapter.setFoods(foods);
                    tvEmpty.setVisibility(foods.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Toast.makeText(AdminHomeActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String query = edtSearch.getText().toString().trim();
        if (query.isEmpty()) {
            loadFoods();
            return;
        }

        dbService.adminSearchFoods("ilike.*" + query + "*", "*").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = response.body();
                    adapter.setFoods(foods);
                    tvEmpty.setVisibility(foods.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Toast.makeText(AdminHomeActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });
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
