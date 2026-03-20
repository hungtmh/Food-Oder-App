package com.example.food_order_app.controller;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CategoryAdapter;
import com.example.food_order_app.adapter.FoodAdapter;
import com.example.food_order_app.adapter.HotOfferFoodAdapter;
import com.example.food_order_app.adapter.SliderAdapter;
import com.example.food_order_app.model.Address;
import com.example.food_order_app.model.Category;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.SearchHistory;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private ViewPager2 viewPagerSlider;
    private LinearLayout dotsIndicator;
    private RecyclerView rvCategories, rvFoods;
    private RecyclerView rvMaybeLike;
    private RecyclerView rvHotOffers;
    private BottomNavigationView bottomNav;
    private View searchBar;
    private View btnNotification;
    private View btnFavorites;
    private android.widget.TextView tvNotifBadge;
    private android.widget.TextView tvDefaultAddress;
    private LinearLayout layoutDeliveryAddress;
    private LinearLayout layoutError;
    private LinearLayout layoutMaybeLike;
    private LinearLayout layoutHotOffers;
    private ScrollView scrollView;
    private Button btnRetry;

    private SliderAdapter sliderAdapter;
    private CategoryAdapter categoryAdapter;
    private FoodAdapter foodAdapter;
    private HotOfferFoodAdapter maybeLikeAdapter;
    private HotOfferFoodAdapter hotOfferAdapter;

    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private int currentSliderPage = 0;

    private List<Category> categories = new ArrayList<>();
    private List<Food> recommendedFoodsCache = new ArrayList<>();
    private String selectedCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        setupAdapters();
        loadDeliveryAddress();
        loadData();
    }

    private void initViews() {
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        rvCategories = findViewById(R.id.rvCategories);
        rvFoods = findViewById(R.id.rvFoods);
        rvMaybeLike = findViewById(R.id.rvMaybeLike);
        rvHotOffers = findViewById(R.id.rvHotOffers);
        bottomNav = findViewById(R.id.bottomNav);
        searchBar = findViewById(R.id.searchBar);
        btnNotification = findViewById(R.id.btnNotification);
        btnFavorites = findViewById(R.id.btnFavorites);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);
        layoutDeliveryAddress = findViewById(R.id.layoutDeliveryAddress);
        tvDefaultAddress = findViewById(R.id.tvDefaultAddress);
        layoutError = findViewById(R.id.layoutError);
        layoutMaybeLike = findViewById(R.id.layoutMaybeLike);
        layoutHotOffers = findViewById(R.id.layoutHotOffers);
        scrollView = findViewById(R.id.scrollView);
        btnRetry = findViewById(R.id.btnRetry);

        searchBar.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoritesActivity.class));
        });

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        layoutDeliveryAddress.setOnClickListener(v -> {
            openAddressSelector();
        });

        btnRetry.setOnClickListener(v -> {
            layoutError.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            loadData();
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
                startActivity(new Intent(this, ContactActivity.class));
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
        sliderAdapter = new SliderAdapter(this, bannerResId -> {
            startActivity(new Intent(this, SearchActivity.class));
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
                if (recommendedFoodsCache != null && !recommendedFoodsCache.isEmpty()) {
                    foodAdapter.setFoods(recommendedFoodsCache);
                } else {
                    loadRecommendedFoods();
                }
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

        // Maybe like foods horizontal list
        maybeLikeAdapter = new HotOfferFoodAdapter(this, food -> openFoodDetail(food));
        rvMaybeLike.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMaybeLike.setAdapter(maybeLikeAdapter);

        hotOfferAdapter = new HotOfferFoodAdapter(this, food -> openFoodDetail(food));
        rvHotOffers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHotOffers.setAdapter(hotOfferAdapter);
    }

    private int loadFailCount = 0;

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNetworkError() {
        runOnUiThread(() -> {
            scrollView.setVisibility(View.GONE);
            layoutError.setVisibility(View.VISIBLE);
        });
    }

    private void onLoadFailed() {
        loadFailCount++;
        // Nếu cả 3 API đều fail thì hiện lỗi
        if (loadFailCount >= 3) {
            showNetworkError();
        }
    }

    private void loadData() {
        loadFailCount = 0;
        if (!isNetworkAvailable()) {
            showNetworkError();
            Toast.makeText(this, "Không có kết nối mạng. Vui lòng kiểm tra WiFi/Data.", Toast.LENGTH_LONG).show();
            return;
        }
        loadSystemBanners();
        loadCategories();
        loadRecommendedFoods();
        loadMaybeLikeFoods();
        loadHotOffers();
    }

    private void loadHotOffers() {
        if (recommendedFoodsCache != null && !recommendedFoodsCache.isEmpty()) {
            applyHotOffersFromList(recommendedFoodsCache);
            return;
        }

        dbService.getAllFoods("eq.true", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyHotOffersFromList(response.body());
                } else {
                    layoutHotOffers.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                layoutHotOffers.setVisibility(View.GONE);
            }
        });
    }

    private void applyHotOffersFromList(List<Food> source) {
        List<Food> discounted = new ArrayList<>();
        for (Food food : source) {
            if (food.getDiscountPercent() > 0) {
                discounted.add(food);
            }
        }

        if (discounted.isEmpty()) {
            layoutHotOffers.setVisibility(View.GONE);
            return;
        }

        int limit = Math.min(10, discounted.size());
        hotOfferAdapter.setFoods(discounted.subList(0, limit));
        layoutHotOffers.setVisibility(View.VISIBLE);
    }

    private void loadMaybeLikeFoods() {
        if (!sessionManager.isLoggedIn()) {
            showMaybeLikeFallback();
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            showMaybeLikeFallback();
            return;
        }

        dbService.getSearchHistory("eq." + userId, "created_at.desc", 6)
                .enqueue(new Callback<List<SearchHistory>>() {
                    @Override
                    public void onResponse(Call<List<SearchHistory>> call, Response<List<SearchHistory>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            showMaybeLikeFallback();
                            return;
                        }

                        List<String> keywords = new ArrayList<>();
                        Set<String> seen = new HashSet<>();
                        for (SearchHistory item : response.body()) {
                            String keyword = item.getKeyword() != null ? item.getKeyword().trim() : "";
                            if (!keyword.isEmpty() && !seen.contains(keyword)) {
                                seen.add(keyword);
                                keywords.add(keyword);
                            }
                            if (keywords.size() >= 3) break;
                        }

                        if (keywords.isEmpty()) {
                            showMaybeLikeFallback();
                            return;
                        }

                        fetchFoodsByKeywords(keywords);
                    }

                    @Override
                    public void onFailure(Call<List<SearchHistory>> call, Throwable t) {
                        Log.e(TAG, "loadMaybeLikeFoods history failed: " + t.getMessage());
                        showMaybeLikeFallback();
                    }
                });
    }

    private void fetchFoodsByKeywords(List<String> keywords) {
        final LinkedHashMap<String, Food> merged = new LinkedHashMap<>();
        final int[] remaining = {keywords.size()};

        for (String keyword : keywords) {
            dbService.searchFoods("ilike.*" + keyword + "*", "eq.true")
                    .enqueue(new Callback<List<Food>>() {
                        @Override
                        public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                for (Food food : response.body()) {
                                    if (food.getId() != null && !merged.containsKey(food.getId())) {
                                        merged.put(food.getId(), food);
                                    }
                                    if (merged.size() >= 10) break;
                                }
                            }
                            onKeywordDone(merged, remaining);
                        }

                        @Override
                        public void onFailure(Call<List<Food>> call, Throwable t) {
                            Log.e(TAG, "fetchFoodsByKeywords failed: " + t.getMessage());
                            onKeywordDone(merged, remaining);
                        }
                    });
        }
    }

    private void onKeywordDone(LinkedHashMap<String, Food> merged, int[] remaining) {
        remaining[0]--;
        if (remaining[0] > 0) return;

        List<Food> foods = new ArrayList<>(merged.values());
        if (foods.isEmpty()) {
            showMaybeLikeFallback();
            return;
        }

        maybeLikeAdapter.setFoods(foods);
        layoutMaybeLike.setVisibility(View.VISIBLE);
    }

    private void showMaybeLikeFallback() {
        if (recommendedFoodsCache != null && !recommendedFoodsCache.isEmpty()) {
            List<Food> fallback = new ArrayList<>();
            int limit = Math.min(10, recommendedFoodsCache.size());
            for (int i = 0; i < limit; i++) {
                fallback.add(recommendedFoodsCache.get(i));
            }
            maybeLikeAdapter.setFoods(fallback);
            layoutMaybeLike.setVisibility(View.VISIBLE);
            return;
        }

        dbService.getRecommendedFoods("eq.true", "eq.true").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Food> fallback = response.body();
                    int limit = Math.min(10, fallback.size());
                    maybeLikeAdapter.setFoods(fallback.subList(0, limit));
                    layoutMaybeLike.setVisibility(View.VISIBLE);
                } else {
                    layoutMaybeLike.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                layoutMaybeLike.setVisibility(View.GONE);
            }
        });
    }

    private void loadSystemBanners() {
        List<Integer> banners = new ArrayList<>();
        banners.add(R.drawable.spring);
        banners.add(R.drawable.summer);
        sliderAdapter.setBanners(banners);
        setupDots(banners.size());
        startAutoSlide(banners.size());
    }

    private void loadCategories() {
        dbService.getCategories("eq.true", "sort_order").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lọc bỏ category "Tất cả" từ DB (đã được thêm vào code phía dưới)
                    categories = new ArrayList<>();
                    for (Category c : response.body()) {
                        if (!"Tất cả".equals(c.getName())) {
                            categories.add(c);
                        }
                    }
                    Log.d(TAG, "loadCategories: " + categories.size() + " items");
                    applyCategoryThumbnails();
                } else if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "loadCategories HTTP " + response.code() + ": " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "loadCategories HTTP " + response.code());
                    }
                } else {
                    Log.w(TAG, "loadCategories: body null");
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "loadCategories failed: " + t.getMessage());
                onLoadFailed();
            }
        });
    }

    private void loadRecommendedFoods() {
        dbService.getAllFoods("eq.true", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadAllFoodsForAllCategory: " + response.body().size() + " items");
                    recommendedFoodsCache = response.body();
                    foodAdapter.setFoods(response.body());
                    applyCategoryThumbnails();
                    loadHotOffers();
                } else if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "loadAllFoodsForAllCategory HTTP " + response.code() + ": " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "loadAllFoodsForAllCategory HTTP " + response.code());
                    }
                } else {
                    Log.w(TAG, "loadAllFoodsForAllCategory: body null");
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "loadAllFoodsForAllCategory failed: " + t.getMessage());
                onLoadFailed();
            }
        });
    }

    private void applyCategoryThumbnails() {
        if (categories == null || categories.isEmpty()) return;

        Map<String, String> thumbByCategory = new HashMap<>();
        if (recommendedFoodsCache != null) {
            for (Food food : recommendedFoodsCache) {
                if (food.getCategoryId() == null || food.getImageUrl() == null || food.getImageUrl().trim().isEmpty()) {
                    continue;
                }
                if (!thumbByCategory.containsKey(food.getCategoryId())) {
                    thumbByCategory.put(food.getCategoryId(), food.getImageUrl());
                }
            }
        }

        Category all = new Category();
        all.setName("Tất cả");
        if (recommendedFoodsCache != null && !recommendedFoodsCache.isEmpty()) {
            String allThumb = recommendedFoodsCache.get(0).getImageUrl();
            if (allThumb != null && !allThumb.trim().isEmpty()) {
                all.setIconUrl(allThumb);
            }
        }

        List<Category> withAll = new ArrayList<>();
        withAll.add(all);
        for (Category c : categories) {
            String thumb = thumbByCategory.get(c.getId());
            if (thumb != null && !thumb.trim().isEmpty()) {
                c.setIconUrl(thumb);
            }
            withAll.add(c);
        }

        categoryAdapter.setCategories(withAll);
    }

    private void loadFoodsByCategory(String categoryId) {
        dbService.getFoodsByCategory("eq." + categoryId, "eq.true").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadFoodsByCategory: " + response.body().size() + " items");
                    foodAdapter.setFoods(response.body());
                } else if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "loadFoodsByCategory HTTP " + response.code() + ": " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "loadFoodsByCategory HTTP " + response.code());
                    }
                } else {
                    Log.w(TAG, "loadFoodsByCategory: body null");
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

    private void loadDeliveryAddress() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            tvDefaultAddress.setText("Thêm địa chỉ giao hàng");
            return;
        }

        dbService.getAddresses("eq." + userId, "is_default.desc,created_at.asc")
                .enqueue(new Callback<List<Address>>() {
                    @Override
                    public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Address> addresses = response.body();
                            // Find the default address
                            Address defaultAddr = null;
                            for (Address addr : addresses) {
                                if (addr.isDefault()) {
                                    defaultAddr = addr;
                                    break;
                                }
                            }
                            
                            // If no default found, use the first one
                            if (defaultAddr == null && !addresses.isEmpty()) {
                                defaultAddr = addresses.get(0);
                            }
                            
                            if (defaultAddr != null) {
                                tvDefaultAddress.setText(defaultAddr.getAddress());
                            } else {
                                tvDefaultAddress.setText("Thêm địa chỉ giao hàng");
                            }
                        } else {
                            tvDefaultAddress.setText("Thêm địa chỉ giao hàng");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Address>> call, Throwable t) {
                        Log.e(TAG, "loadDeliveryAddress failed: " + t.getMessage());
                        tvDefaultAddress.setText("Thêm địa chỉ giao hàng");
                    }
                });
    }

    private void openAddressSelector() {
        Intent intent = new Intent(this, AddressActivity.class);
        intent.putExtra(AddressActivity.EXTRA_PICK_MODE, true);
        startActivityForResult(intent, 100);
    }

    private void loadUnreadNotificationCount() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        dbService.getUnreadNotificationCount("eq." + userId, "eq.false", "id")
                .enqueue(new Callback<List<com.example.food_order_app.model.Notification>>() {
                    @Override
                    public void onResponse(Call<List<com.example.food_order_app.model.Notification>> call, Response<List<com.example.food_order_app.model.Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int count = response.body().size();
                            if (count > 0) {
                                tvNotifBadge.setText(String.valueOf(count));
                                tvNotifBadge.setVisibility(View.VISIBLE);
                            } else {
                                tvNotifBadge.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.food_order_app.model.Notification>> call, Throwable t) {
                        // Silent fail for badge
                    }
                });
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
        loadUnreadNotificationCount();
        loadDeliveryAddress();
        loadMaybeLikeFoods();
        loadHotOffers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Address was selected, refresh the delivery address display
            loadDeliveryAddress();
        }
    }
}
