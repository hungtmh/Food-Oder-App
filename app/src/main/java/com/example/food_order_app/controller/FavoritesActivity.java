package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.FavoritesAdapter;
import com.example.food_order_app.model.Favorite;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {
    private static final String TAG = "FavoritesActivity";

    private ImageView btnBack;
    private TextView tvFavCount;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private RecyclerView rvFavorites;

    private FavoritesAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        loadFavorites();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvFavCount = findViewById(R.id.tvFavCount);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rvFavorites = findViewById(R.id.rvFavorites);

        adapter = new FavoritesAdapter(this, new FavoritesAdapter.OnFavoriteActionListener() {
            @Override
            public void onFoodClick(Food food) {
                Intent intent = new Intent(FavoritesActivity.this, FoodDetailActivity.class);
                intent.putExtra("food_id", food.getId());
                startActivity(intent);
            }

            @Override
            public void onRemoveFavorite(Favorite favorite, int position) {
                confirmRemoveFavorite(favorite, position);
            }
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (!sessionManager.isLoggedIn()) {
            showEmpty();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        dbService.getFavorites("eq." + sessionManager.getUserId(), "*,foods(*)", "created_at.desc")
                .enqueue(new Callback<List<Favorite>>() {
                    @Override
                    public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Favorite> favorites = response.body();
                            adapter.setFavorites(favorites);
                            tvFavCount.setText(favorites.size() + " món");
                            rvFavorites.setVisibility(View.VISIBLE);
                            layoutEmpty.setVisibility(View.GONE);
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Favorite>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showEmpty();
                        Log.e(TAG, "loadFavorites failed: " + t.getMessage());
                    }
                });
    }

    private void confirmRemoveFavorite(Favorite favorite, int position) {
        String foodName = favorite.getFood() != null ? favorite.getFood().getName() : "món ăn này";
        new AlertDialog.Builder(this)
                .setTitle("Bỏ yêu thích")
                .setMessage("Bạn muốn bỏ \"" + foodName + "\" khỏi danh sách yêu thích?")
                .setPositiveButton("Bỏ", (dialog, which) -> removeFavorite(favorite, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeFavorite(Favorite favorite, int position) {
        dbService.removeFavorite("eq." + sessionManager.getUserId(), "eq." + favorite.getFoodId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            adapter.removeItem(position);
                            int remaining = adapter.getItemCount();
                            tvFavCount.setText(remaining + " món");
                            Toast.makeText(FavoritesActivity.this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                            if (remaining == 0) {
                                showEmpty();
                            }
                        } else {
                            Toast.makeText(FavoritesActivity.this, "Lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(FavoritesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "removeFavorite failed: " + t.getMessage());
                    }
                });
    }

    private void showEmpty() {
        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        tvFavCount.setText("");
    }
}
