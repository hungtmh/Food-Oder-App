package com.example.food_order_app.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CategoryAdapter;
import com.example.food_order_app.adapter.FoodAdapter;
import com.example.food_order_app.adapter.HotOfferFoodAdapter;
import com.example.food_order_app.adapter.SliderAdapter;
import com.example.food_order_app.model.Address;
import com.example.food_order_app.model.Category;
import com.example.food_order_app.model.Favorite;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.NotificationHelper;
import com.example.food_order_app.utils.PushRegistrationManager;
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.food_order_app.database.AppDatabase;
import com.example.food_order_app.database.OfflineCache;
import java.lang.reflect.Type;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

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
    private ImageView imgWaveBackground;

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
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);
        setupNotificationPermissionLauncher();

        initViews();
        setupAdapters();
        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermissionIfNeeded();
        PushRegistrationManager.requestCurrentTokenAndSync(this);
        loadDeliveryAddress();
        loadData();
    }

    private void setupNotificationPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Thong bao he thong da bi tu choi", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
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
        imgWaveBackground = findViewById(R.id.imgWaveBackground);

        // Load wave background via Glide to avoid "too large bitmap" crash
        Glide.with(this)
                .load(R.drawable.wave)
                .apply(new RequestOptions()
                        .override(1080, 500)
                        .format(DecodeFormat.PREFER_RGB_565))
                .into(imgWaveBackground);

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
            forceRefreshData();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                if (sessionManager != null && sessionManager.isLoggedIn()) {
                    startActivity(new Intent(this, ChatRoomActivity.class));
                } else {
                    Toast.makeText(this, "Vui lòng đăng nhập để chat", Toast.LENGTH_SHORT).show();
                }
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

    private void saveCache(String key, Object data) {
        new Thread(() -> {
            AppDatabase.getInstance(this).offlineCacheDao().insertCache(
                new OfflineCache(key, new Gson().toJson(data), System.currentTimeMillis())
            );
            Log.d(TAG, "💾 Cache saved: " + key);
        }).start();
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
        
        new Thread(() -> {
            long currentTime = System.currentTimeMillis();
            
            // Load cache metadata
            OfflineCache cachedCat = AppDatabase.getInstance(this).offlineCacheDao().getCacheObject("home_categories");
            OfflineCache cachedRec = AppDatabase.getInstance(this).offlineCacheDao().getCacheObject("home_recommended");
            OfflineCache cachedHot = AppDatabase.getInstance(this).offlineCacheDao().getCacheObject("home_hot_offers");
            
            // Check TTL and decide if cache is valid
            boolean cacheValid = true;
            StringBuilder cacheStatus = new StringBuilder();
            
            if (cachedCat != null && (currentTime - cachedCat.getTimestamp()) > CACHE_TTL_MS) {
                cacheStatus.append("Categories expired ");
                cacheValid = false;
                AppDatabase.getInstance(this).offlineCacheDao().deleteCache("home_categories");
            }
            if (cachedRec != null && (currentTime - cachedRec.getTimestamp()) > CACHE_TTL_MS) {
                cacheStatus.append("Foods expired ");
                cacheValid = false;
                AppDatabase.getInstance(this).offlineCacheDao().deleteCache("home_recommended");
            }
            if (cachedHot != null && (currentTime - cachedHot.getTimestamp()) > CACHE_TTL_MS) {
                cacheStatus.append("HotOffers expired ");
                cacheValid = false;
                AppDatabase.getInstance(this).offlineCacheDao().deleteCache("home_hot_offers");
            }
            
            String cachedCategories = cachedCat != null && cacheValid ? cachedCat.getData() : null;
            String cachedRecommended = cachedRec != null && cacheValid ? cachedRec.getData() : null;
            String cachedHotOffers = cachedHot != null && cacheValid ? cachedHot.getData() : null;
            
            if (cacheStatus.length() > 0) {
                Log.d(TAG, "⏰ Cache TTL expired: " + cacheStatus.toString());
            }
            
            runOnUiThread(() -> {
                Gson gson = new Gson();
                boolean hasCache = false;
                
                if (cachedCategories != null) {
                    Type type = new TypeToken<List<Category>>(){}.getType();
                    List<Category> cats = gson.fromJson(cachedCategories, type);
                    if (cats != null) {
                        categories = cats;
                        applyCategoryThumbnails();
                        hasCache = true;
                        long age = (currentTime - cachedCat.getTimestamp()) / 1000;
                        Log.d(TAG, "✓ Loaded categories from cache (" + age + "s old): " + cats.size());
                    }
                }
                
                if (cachedRecommended != null) {
                    Type type = new TypeToken<List<Food>>(){}.getType();
                    List<Food> recs = gson.fromJson(cachedRecommended, type);
                    if (recs != null) {
                        recommendedFoodsCache = recs;
                        foodAdapter.setFoods(recs);
                        hasCache = true;
                        long age = (currentTime - cachedRec.getTimestamp()) / 1000;
                        Log.d(TAG, "✓ Loaded foods from cache (" + age + "s old): " + recs.size());
                    }
                }
                
                if (cachedHotOffers != null) {
                    Type type = new TypeToken<List<Food>>(){}.getType();
                    List<Food> offers = gson.fromJson(cachedHotOffers, type);
                    if (offers != null) {
                        applyHotOffersFromList(offers);
                        long age = (currentTime - cachedHot.getTimestamp()) / 1000;
                        Log.d(TAG, "✓ Loaded hot offers from cache (" + age + "s old)");
                    }
                }

                if (!isNetworkAvailable()) {
                    if (hasCache) {
                        Toast.makeText(this, "📦 Dữ liệu cache - Không có mạng", Toast.LENGTH_LONG).show();
                        loadSystemBanners();
                    } else {
                        showNetworkError();
                        Toast.makeText(this, "❌ Không có mạng và không có cache", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                
                if (hasCache) {
                    Log.d(TAG, "🔄 Có cache hợp lệ, sẽ fetch dữ liệu mới từ API");
                } else {
                    Log.d(TAG, "🔄 Không có cache, fetch dữ liệu từ API");
                }
                
                loadSystemBanners();
                loadCategories();
                loadRecommendedFoods();
                loadMaybeLikeFoods();
                loadHotOffers();
            });
        }).start();
    }

    private void forceRefreshData() {
        Log.d(TAG, "🔃 Force Refresh - Bypass cache, gọi API trực tiếp");
        Toast.makeText(this, "⏳ Đang làm mới dữ liệu...", Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG, "🌐 [API] Fresh hot offers loaded");
                    applyHotOffersFromList(response.body());
                    saveCache("home_hot_offers", response.body());
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

        dbService.getAllFoods("eq.true", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    if (recommendedFoodsCache != null && !recommendedFoodsCache.isEmpty()) {
                        loadRuleBasedRecommendations(userId, recommendedFoodsCache);
                    } else {
                        showMaybeLikeFallback();
                    }
                    return;
                }

                recommendedFoodsCache = response.body();
                saveCache("home_recommended", response.body());
                loadRuleBasedRecommendations(userId, recommendedFoodsCache);
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "loadMaybeLikeFoods getAllFoods failed: " + t.getMessage());
                if (recommendedFoodsCache != null && !recommendedFoodsCache.isEmpty()) {
                    loadRuleBasedRecommendations(userId, recommendedFoodsCache);
                } else {
                    showMaybeLikeFallback();
                }
            }
        });
    }

    private interface RecentViewedCallback {
        void onLoaded(List<String> recentIds);
    }

    private void loadRecentViewedIds(String userId, int limit, RecentViewedCallback callback) {
        new Thread(() -> {
            List<String> ids = AppDatabase.getInstance(this).recentViewDao().getRecentFoodIds(userId, limit);
            runOnUiThread(() -> callback.onLoaded(ids != null ? ids : new ArrayList<>()));
        }).start();
    }

    private void loadRuleBasedRecommendations(String userId, List<Food> sourceFoods) {
        if (sourceFoods == null || sourceFoods.isEmpty()) {
            showMaybeLikeFallback();
            return;
        }

        loadRecentViewedIds(userId, 10, recentIds -> {
            dbService.getFavorites("eq." + userId, "food_id", "created_at.desc").enqueue(new Callback<List<Favorite>>() {
                @Override
                public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> favResponse) {
                    Set<String> favoriteIds = new HashSet<>();
                    if (favResponse.isSuccessful() && favResponse.body() != null) {
                        for (Favorite favorite : favResponse.body()) {
                            if (favorite.getFoodId() != null) {
                                favoriteIds.add(favorite.getFoodId());
                            }
                        }
                    }

                    dbService.getOrders("eq." + userId, "id,order_items(food_id,quantity)", "created_at.desc")
                            .enqueue(new Callback<List<Order>>() {
                                @Override
                                public void onResponse(Call<List<Order>> call, Response<List<Order>> orderResponse) {
                                    Map<String, Integer> orderCountByFoodId = new HashMap<>();
                                    if (orderResponse.isSuccessful() && orderResponse.body() != null) {
                                        for (Order order : orderResponse.body()) {
                                            if (order.getOrderItems() == null) continue;
                                            for (OrderItem item : order.getOrderItems()) {
                                                if (item.getFoodId() == null) continue;
                                                int qty = Math.max(item.getQuantity(), 1);
                                                int prev = orderCountByFoodId.getOrDefault(item.getFoodId(), 0);
                                                orderCountByFoodId.put(item.getFoodId(), prev + qty);
                                            }
                                        }
                                    }

                                    applyRuleBasedScores(sourceFoods, orderCountByFoodId, favoriteIds, new HashSet<>(recentIds));
                                }

                                @Override
                                public void onFailure(Call<List<Order>> call, Throwable t) {
                                    Log.e(TAG, "loadRuleBasedRecommendations orders failed: " + t.getMessage());
                                    applyRuleBasedScores(sourceFoods, new HashMap<>(), favoriteIds, new HashSet<>(recentIds));
                                }
                            });
                }

                @Override
                public void onFailure(Call<List<Favorite>> call, Throwable t) {
                    Log.e(TAG, "loadRuleBasedRecommendations favorites failed: " + t.getMessage());
                    dbService.getOrders("eq." + userId, "id,order_items(food_id,quantity)", "created_at.desc")
                            .enqueue(new Callback<List<Order>>() {
                                @Override
                                public void onResponse(Call<List<Order>> call, Response<List<Order>> orderResponse) {
                                    Map<String, Integer> orderCountByFoodId = new HashMap<>();
                                    if (orderResponse.isSuccessful() && orderResponse.body() != null) {
                                        for (Order order : orderResponse.body()) {
                                            if (order.getOrderItems() == null) continue;
                                            for (OrderItem item : order.getOrderItems()) {
                                                if (item.getFoodId() == null) continue;
                                                int qty = Math.max(item.getQuantity(), 1);
                                                int prev = orderCountByFoodId.getOrDefault(item.getFoodId(), 0);
                                                orderCountByFoodId.put(item.getFoodId(), prev + qty);
                                            }
                                        }
                                    }

                                    applyRuleBasedScores(sourceFoods, orderCountByFoodId, new HashSet<>(), new HashSet<>(recentIds));
                                }

                                @Override
                                public void onFailure(Call<List<Order>> call, Throwable t) {
                                    Log.e(TAG, "loadRuleBasedRecommendations orders fallback failed: " + t.getMessage());
                                    applyRuleBasedScores(sourceFoods, new HashMap<>(), new HashSet<>(), new HashSet<>(recentIds));
                                }
                            });
                }
            });
        });
    }

    private void applyRuleBasedScores(List<Food> sourceFoods,
                                      Map<String, Integer> orderCountByFoodId,
                                      Set<String> favoriteIds,
                                      Set<String> recentIds) {
        if (sourceFoods == null || sourceFoods.isEmpty()) {
            showMaybeLikeFallback();
            return;
        }

        int maxOrderCount = 0;
        int minTotalReviews = Integer.MAX_VALUE;
        int maxTotalReviews = Integer.MIN_VALUE;
        double minAvgRating = Double.MAX_VALUE;
        double maxAvgRating = Double.MIN_VALUE;

        for (Food food : sourceFoods) {
            if (food.getId() == null) continue;
            int orderCount = orderCountByFoodId.getOrDefault(food.getId(), 0);
            maxOrderCount = Math.max(maxOrderCount, orderCount);

            minTotalReviews = Math.min(minTotalReviews, food.getTotalReviews());
            maxTotalReviews = Math.max(maxTotalReviews, food.getTotalReviews());

            minAvgRating = Math.min(minAvgRating, food.getAvgRating());
            maxAvgRating = Math.max(maxAvgRating, food.getAvgRating());
        }

        if (minTotalReviews == Integer.MAX_VALUE) minTotalReviews = 0;
        if (maxTotalReviews == Integer.MIN_VALUE) maxTotalReviews = 0;
        if (minAvgRating == Double.MAX_VALUE) minAvgRating = 0;
        if (maxAvgRating == Double.MIN_VALUE) maxAvgRating = 0;

        List<ScoredFood> scored = new ArrayList<>();
        for (Food food : sourceFoods) {
            if (food.getId() == null) continue;

            double orderScore = normalize(orderCountByFoodId.getOrDefault(food.getId(), 0), 0, maxOrderCount) * 0.6;
            double reviewScore = normalize(food.getTotalReviews(), minTotalReviews, maxTotalReviews) * 0.25
                    + normalize(food.getAvgRating(), minAvgRating, maxAvgRating) * 0.15;
            double favoriteBoost = favoriteIds.contains(food.getId()) ? 0.10 : 0.0;
            double recentBoost = recentIds.contains(food.getId()) ? 0.10 : 0.0;

            double finalScore = orderScore + reviewScore + favoriteBoost + recentBoost;
            scored.add(new ScoredFood(food, finalScore));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<Food> topFoods = new ArrayList<>();
        int limit = Math.min(10, scored.size());
        for (int i = 0; i < limit; i++) {
            topFoods.add(scored.get(i).food);
        }

        if (topFoods.isEmpty()) {
            showMaybeLikeFallback();
            return;
        }

        maybeLikeAdapter.setFoods(topFoods);
        layoutMaybeLike.setVisibility(View.VISIBLE);
        Log.d(TAG, "rule_based_recommendation: top=" + topFoods.size());
    }

    private static class ScoredFood {
        final Food food;
        final double score;

        ScoredFood(Food food, double score) {
            this.food = food;
            this.score = score;
        }
    }

    private double normalize(double value, double min, double max) {
        if (max <= min) return 0.0;
        double normalized = (value - min) / (max - min);
        if (normalized < 0) return 0.0;
        if (normalized > 1) return 1.0;
        return normalized;
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
                    Log.d(TAG, "🌐 [API] Fresh categories loaded");
                    applyCategoryThumbnails();
                    saveCache("home_categories", categories);
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
                    Log.d(TAG, "🌐 [API] Fresh foods loaded");
                    recommendedFoodsCache = response.body();
                    foodAdapter.setFoods(response.body());
                    saveCache("home_recommended", response.body());
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
