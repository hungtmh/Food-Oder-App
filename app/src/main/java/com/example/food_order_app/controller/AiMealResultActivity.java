package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AiComboFoodAdapter;
import com.example.food_order_app.model.AiRecommendationTask;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiMealResultActivity extends AppCompatActivity {
    private static final String TAG = "AiMealResult";
    public static final String EXTRA_TASK_ID = "task_id";

    private ImageView btnBack;
    private TextView tvRationale, tvTotalPrice;
    private RecyclerView rvComboFoods;
    private Button btnAddAllToCart, btnReset;

    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private AiComboFoodAdapter adapter;
    private AiRecommendationTask task;
    private List<Food> allFoods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_meal_result);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        loadTaskData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRationale = findViewById(R.id.tvRationale);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvComboFoods = findViewById(R.id.rvComboFoods);
        btnAddAllToCart = findViewById(R.id.btnAddAllToCart);
        btnReset = findViewById(R.id.btnReset);

        btnBack.setOnClickListener(v -> {
            clearTaskAndFinish();
        });

        adapter = new AiComboFoodAdapter(this, null);
        rvComboFoods.setLayoutManager(new LinearLayoutManager(this));
        rvComboFoods.setAdapter(adapter);

        btnAddAllToCart.setOnClickListener(v -> addAllToCart());
        btnReset.setOnClickListener(v -> {
            clearTaskAndFinish();
        });
    }

    private void loadTaskData() {
        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (taskId == null) {
            Toast.makeText(this, "Không tìm thấy task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbService.getAiTaskById("eq." + taskId, "*").enqueue(new Callback<List<AiRecommendationTask>>() {
            @Override
            public void onResponse(Call<List<AiRecommendationTask>> call, Response<List<AiRecommendationTask>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    task = response.body().get(0);
                    if (task.getResult() != null) {
                        tvRationale.setText(task.getResult().getRationale());
                        loadAllFoodsAndDisplay(task.getResult().getFoods());
                    } else {
                        tvRationale.setText("Không có kết quả từ AI.");
                    }
                } else {
                    Toast.makeText(AiMealResultActivity.this, "Không tìm thấy task", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<AiRecommendationTask>> call, Throwable t) {
                Log.e(TAG, "loadTaskData failed: " + t.getMessage());
                Toast.makeText(AiMealResultActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllFoodsAndDisplay(List<AiRecommendationTask.AiRecommendedFood> aiFoods) {
        dbService.getAllFoods("eq.true", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFoods = response.body();
                }
                adapter.setFoods(aiFoods, allFoods);
                updateTotalPrice(aiFoods);
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                adapter.setFoods(aiFoods, new ArrayList<>());
                updateTotalPrice(aiFoods);
            }
        });
    }

    private void updateTotalPrice(List<AiRecommendationTask.AiRecommendedFood> aiFoods) {
        double total = 0;
        for (AiRecommendationTask.AiRecommendedFood f : aiFoods) {
            if (f.getPrice() != null) {
                total += f.getPrice();
            }
        }
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(nf.format(total) + " VNĐ");
    }

    private void addAllToCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (task == null || task.getResult() == null || task.getResult().getFoods() == null
                || task.getResult().getFoods().isEmpty()) {
            Toast.makeText(this, "Không có món để thêm", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddAllToCart.setEnabled(false);
        btnAddAllToCart.setText("Đang thêm vào giỏ...");

        String userId = sessionManager.getUserId();
        dbService.getCart("eq." + userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    addAllItemsToCart(response.body().get(0).getId());
                } else {
                    Map<String, String> cartData = new HashMap<>();
                    cartData.put("user_id", userId);
                    dbService.createCart(cartData).enqueue(new Callback<List<Cart>>() {
                        @Override
                        public void onResponse(Call<List<Cart>> call2, Response<List<Cart>> response2) {
                            if (response2.isSuccessful() && response2.body() != null && !response2.body().isEmpty()) {
                                addAllItemsToCart(response2.body().get(0).getId());
                            } else {
                                onAddFailed("Lỗi tạo giỏ hàng");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Cart>> call2, Throwable t) {
                            onAddFailed("Lỗi kết nối");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                onAddFailed("Lỗi kết nối");
            }
        });
    }

    private void addAllItemsToCart(String cartId) {
        List<AiRecommendationTask.AiRecommendedFood> aiFoods = task.getResult().getFoods();
        final int total = aiFoods.size();
        final int[] completed = {0};
        final boolean[] hasError = {false};

        for (AiRecommendationTask.AiRecommendedFood aiFood : aiFoods) {
            if (aiFood.getFoodId() == null) {
                synchronized (completed) {
                    completed[0]++;
                    if (completed[0] >= total) finishAdding(hasError[0]);
                }
                continue;
            }

            dbService.getCartItems("eq." + cartId, "eq." + aiFood.getFoodId(), "*,foods(*)")
                    .enqueue(new Callback<List<CartItem>>() {
                        @Override
                        public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                CartItem existing = response.body().get(0);
                                int newQty = existing.getQuantity() + 1;
                                Map<String, Object> update = new HashMap<>();
                                update.put("quantity", newQty);
                                dbService.updateCartItem("eq." + existing.getId(), update).enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call2, Response<Void> response2) {
                                        checkDone();
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call2, Throwable t) {
                                        hasError[0] = true;
                                        checkDone();
                                    }
                                });
                            } else {
                                Map<String, Object> itemData = new HashMap<>();
                                itemData.put("cart_id", cartId);
                                itemData.put("food_id", aiFood.getFoodId());
                                itemData.put("quantity", 1);
                                dbService.addCartItem(itemData).enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call2, Response<Void> response2) {
                                        checkDone();
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call2, Throwable t) {
                                        hasError[0] = true;
                                        checkDone();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<CartItem>> call, Throwable t) {
                            hasError[0] = true;
                            checkDone();
                        }

                        private void checkDone() {
                            synchronized (completed) {
                                completed[0]++;
                                if (completed[0] >= total) finishAdding(hasError[0]);
                            }
                        }
                    });
        }
    }

    private void finishAdding(boolean hasError) {
        runOnUiThread(() -> {
            btnAddAllToCart.setEnabled(true);
            btnAddAllToCart.setText("Thêm toàn bộ combo vào giỏ hàng");
            if (hasError) {
                Toast.makeText(this, "Một số món thêm thất bại", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã thêm combo vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAddFailed(String msg) {
        runOnUiThread(() -> {
            btnAddAllToCart.setEnabled(true);
            btnAddAllToCart.setText("Thêm toàn bộ combo vào giỏ hàng");
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void clearTaskAndFinish() {
        getSharedPreferences("ai_meal_task", MODE_PRIVATE).edit().clear().apply();
        finish();
    }
}
