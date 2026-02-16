package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CategoryAdapter;
import com.example.food_order_app.adapter.FoodAdapter;
import com.example.food_order_app.model.Category;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.SearchHistory;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    private EditText etSearch;
    private ImageView btnClearSearch;
    private TextView btnBack, tvSortPrice, tvSortRating, tvSortDiscount, tvNoResults, tvClearHistory;
    private RecyclerView rvSearchResults, rvSearchCategories, rvSearchHistory;
    private View searchHistorySection;

    private FoodAdapter foodAdapter;
    private CategoryAdapter categoryAdapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    private List<Food> allFoods = new ArrayList<>();
    private List<Food> filteredFoods = new ArrayList<>();
    private String selectedCategoryId = null;

    // Sort states: 0=none, 1=asc, 2=desc
    private int sortPriceState = 0;
    private int sortRatingState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        loadCategories();
        showSearchHistory();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnBack = findViewById(R.id.btnBack);
        tvSortPrice = findViewById(R.id.tvSortPrice);
        tvSortRating = findViewById(R.id.tvSortRating);
        tvSortDiscount = findViewById(R.id.tvSortDiscount);
        tvNoResults = findViewById(R.id.tvNoResults);
        tvClearHistory = findViewById(R.id.tvClearHistory);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvSearchCategories = findViewById(R.id.rvSearchCategories);
        rvSearchHistory = findViewById(R.id.rvSearchHistory);
        searchHistorySection = findViewById(R.id.searchHistorySection);

        // Food results
        foodAdapter = new FoodAdapter(this, food -> {
            Intent intent = new Intent(this, FoodDetailActivity.class);
            intent.putExtra("food_id", food.getId());
            startActivity(intent);
        });
        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvSearchResults.setAdapter(foodAdapter);

        // Categories
        categoryAdapter = new CategoryAdapter(this, (category, position) -> {
            if (position == 0) {
                selectedCategoryId = null;
            } else {
                selectedCategoryId = category.getId();
            }
            applyFilters();
        });
        rvSearchCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSearchCategories.setAdapter(categoryAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            showSearchHistory();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() == 0) {
                    showSearchHistory();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    saveSearchHistory(query);
                }
                return true;
            }
            return false;
        });

        // Sort buttons
        tvSortPrice.setOnClickListener(v -> {
            sortPriceState = (sortPriceState + 1) % 3;
            sortRatingState = 0;
            updateSortUI();
            applyFilters();
        });

        tvSortRating.setOnClickListener(v -> {
            sortRatingState = (sortRatingState + 1) % 3;
            sortPriceState = 0;
            updateSortUI();
            applyFilters();
        });

        tvSortDiscount.setOnClickListener(v -> {
            sortPriceState = 0;
            sortRatingState = 0;
            // Sort by discount desc
            Collections.sort(filteredFoods, (a, b) -> b.getDiscountPercent() - a.getDiscountPercent());
            foodAdapter.setFoods(filteredFoods);
            updateSortUI();
        });

        tvClearHistory.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                dbService.deleteSearchHistory("eq." + sessionManager.getUserId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        showSearchHistory();
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }
        });

        etSearch.requestFocus();
    }

    private void performSearch(String query) {
        searchHistorySection.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);

        dbService.searchFoods("ilike.*" + query + "*", "eq.true").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFoods = response.body();
                    applyFilters();
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "search failed: " + t.getMessage());
            }
        });
    }

    private void applyFilters() {
        filteredFoods = new ArrayList<>(allFoods);

        // Filter by category
        if (selectedCategoryId != null) {
            List<Food> filtered = new ArrayList<>();
            for (Food f : filteredFoods) {
                if (selectedCategoryId.equals(f.getCategoryId())) {
                    filtered.add(f);
                }
            }
            filteredFoods = filtered;
        }

        // Sort
        if (sortPriceState == 1) {
            Collections.sort(filteredFoods, (a, b) -> Double.compare(a.getDiscountedPrice(), b.getDiscountedPrice()));
        } else if (sortPriceState == 2) {
            Collections.sort(filteredFoods, (a, b) -> Double.compare(b.getDiscountedPrice(), a.getDiscountedPrice()));
        }

        if (sortRatingState == 1) {
            Collections.sort(filteredFoods, (a, b) -> Double.compare(a.getAvgRating(), b.getAvgRating()));
        } else if (sortRatingState == 2) {
            Collections.sort(filteredFoods, (a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()));
        }

        foodAdapter.setFoods(filteredFoods);
        tvNoResults.setVisibility(filteredFoods.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateSortUI() {
        String priceText = "Giá";
        if (sortPriceState == 1) priceText = "Giá ↑";
        else if (sortPriceState == 2) priceText = "Giá ↓";
        else priceText = "Giá ↕";
        tvSortPrice.setText(priceText);

        String ratingText = "Đánh giá";
        if (sortRatingState == 1) ratingText = "Đánh giá ↑";
        else if (sortRatingState == 2) ratingText = "Đánh giá ↓";
        else ratingText = "Đánh giá ↕";
        tvSortRating.setText(ratingText);
    }

    private void loadCategories() {
        dbService.getCategories("eq.true", "sort_order").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Category all = new Category();
                    all.setName("Tất cả");
                    List<Category> cats = new ArrayList<>();
                    cats.add(all);
                    cats.addAll(response.body());
                    categoryAdapter.setCategories(cats);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "loadCategories failed: " + t.getMessage());
            }
        });
    }

    private void showSearchHistory() {
        rvSearchResults.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        if (!sessionManager.isLoggedIn()) {
            searchHistorySection.setVisibility(View.GONE);
            return;
        }

        dbService.getSearchHistory("eq." + sessionManager.getUserId(), "created_at.desc", 10).enqueue(new Callback<List<SearchHistory>>() {
            @Override
            public void onResponse(Call<List<SearchHistory>> call, Response<List<SearchHistory>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    searchHistorySection.setVisibility(View.VISIBLE);
                    setupHistoryList(response.body());
                } else {
                    searchHistorySection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<SearchHistory>> call, Throwable t) {
                searchHistorySection.setVisibility(View.GONE);
            }
        });
    }

    private void setupHistoryList(List<SearchHistory> histories) {
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(this));
        rvSearchHistory.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_search_history, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                SearchHistory h = histories.get(position);
                TextView tv = holder.itemView.findViewById(R.id.tvHistoryKeyword);
                tv.setText(h.getKeyword());
                holder.itemView.setOnClickListener(v -> {
                    etSearch.setText(h.getKeyword());
                    etSearch.setSelection(h.getKeyword().length());
                    performSearch(h.getKeyword());
                });
            }

            @Override
            public int getItemCount() { return histories.size(); }
        });
    }

    private void saveSearchHistory(String keyword) {
        if (!sessionManager.isLoggedIn()) return;

        Map<String, String> data = new HashMap<>();
        data.put("user_id", sessionManager.getUserId());
        data.put("keyword", keyword);

        dbService.saveSearch(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
