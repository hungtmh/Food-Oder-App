package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CategoryAdapter;
import com.example.food_order_app.adapter.FoodAdapter;
import com.example.food_order_app.adapter.SliderAdapter;
import com.example.food_order_app.model.Category;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private ViewPager2 viewPagerSlider;
    private LinearLayout dotsIndicator;
    private RecyclerView rvCategories, rvFoods;
    private BottomNavigationView bottomNav;
    private View searchBar;

    private SliderAdapter sliderAdapter;
    private CategoryAdapter categoryAdapter;
    private FoodAdapter foodAdapter;

    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private int currentSliderPage = 0;

    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        setupAdapters();
        loadData();
    }

    private void initViews() {
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        rvCategories = findViewById(R.id.rvCategories);
        rvFoods = findViewById(R.id.rvFoods);
        bottomNav = findViewById(R.id.bottomNav);
        searchBar = findViewById(R.id.searchBar);

        searchBar.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_feedback) {
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_contact) {
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupAdapters() {
        // Slider
        sliderAdapter = new SliderAdapter(this, food -> {
            openFoodDetail(food);
        });
        viewPagerSlider.setAdapter(sliderAdapter);
        viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentSliderPage = position;
                updateDots(position);
            }
        });

        // Categories
        categoryAdapter = new CategoryAdapter(this, (category, position) -> {
            if (position == 0) {
                selectedCategoryId = null;
                loadRecommendedFoods();
            } else {
                selectedCategoryId = category.getId();
                loadFoodsByCategory(category.getId());
            }
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Foods grid
        foodAdapter = new FoodAdapter(this, food -> {
            openFoodDetail(food);
        });
        rvFoods.setLayoutManager(new GridLayoutManager(this, 2));
        rvFoods.setAdapter(foodAdapter);
    }

    private void loadData() {
        loadPopularFoods();
        loadCategories();
        loadRecommendedFoods();
    }

    private void loadPopularFoods() {
        dbService.getPopularFoods("eq.true", "eq.true").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = response.body();
                    sliderAdapter.setFoods(foods);
                    setupDots(foods.size());
                    startAutoSlide(foods.size());
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "loadPopularFoods failed: " + t.getMessage());
            }
        });
    }

    private void loadCategories() {
        dbService.getCategories("eq.true", "sort_order").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    // Add "Tất cả" at beginning
                    Category all = new Category();
                    all.setName("Tất cả");
                    List<Category> withAll = new ArrayList<>();
                    withAll.add(all);
                    withAll.addAll(categories);
                    categoryAdapter.setCategories(withAll);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "loadCategories failed: " + t.getMessage());
            }
        });
    }

    private void loadRecommendedFoods() {
        dbService.getRecommendedFoods("eq.true", "eq.true").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodAdapter.setFoods(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "loadRecommendedFoods failed: " + t.getMessage());
            }
        });
    }

    private void loadFoodsByCategory(String categoryId) {
        dbService.getFoodsByCategory("eq." + categoryId, "eq.true").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foodAdapter.setFoods(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "loadFoodsByCategory failed: " + t.getMessage());
            }
        });
    }

    private void openFoodDetail(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("food_id", food.getId());
        startActivity(intent);
    }

    // Slider dots
    private void setupDots(int count) {
        dotsIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsIndicator.addView(dot);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            dotsIndicator.getChildAt(i).setBackgroundResource(
                    i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void startAutoSlide(int totalPages) {
        sliderRunnable = () -> {
            if (totalPages > 0) {
                currentSliderPage = (currentSliderPage + 1) % totalPages;
                viewPagerSlider.setCurrentItem(currentSliderPage, true);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
