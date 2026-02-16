package com.example.food_order_app.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.adapter.ReviewAdapter;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.Review;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailActivity extends AppCompatActivity {
    private static final String TAG = "FoodDetailActivity";

    private ImageView imgFoodDetail;
    private TextView btnBack, tvDetailDiscount, tvDetailName, tvDetailRating, tvDetailReviewCount;
    private TextView tvDetailDiscountedPrice, tvDetailOriginalPrice, tvDetailDescription;
    private TextView tvDetailQuantity, tvWriteReview, tvNoReviews;
    private ImageButton btnDetailMinus, btnDetailPlus;
    private Button btnAddToCart;
    private RecyclerView rvReviews;

    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private ReviewAdapter reviewAdapter;
    private NumberFormat nf;

    private Food currentFood;
    private int quantity = 1;
    private String foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);
        nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        foodId = getIntent().getStringExtra("food_id");
        if (foodId == null) {
            finish();
            return;
        }

        initViews();
        loadFoodDetail();
        loadReviews();
    }

    private void initViews() {
        imgFoodDetail = findViewById(R.id.imgFoodDetail);
        btnBack = findViewById(R.id.btnBack);
        tvDetailDiscount = findViewById(R.id.tvDetailDiscount);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailRating = findViewById(R.id.tvDetailRating);
        tvDetailReviewCount = findViewById(R.id.tvDetailReviewCount);
        tvDetailDiscountedPrice = findViewById(R.id.tvDetailDiscountedPrice);
        tvDetailOriginalPrice = findViewById(R.id.tvDetailOriginalPrice);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvWriteReview = findViewById(R.id.tvWriteReview);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        btnDetailMinus = findViewById(R.id.btnDetailMinus);
        btnDetailPlus = findViewById(R.id.btnDetailPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        rvReviews = findViewById(R.id.rvReviews);

        reviewAdapter = new ReviewAdapter(this);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnDetailMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvDetailQuantity.setText(String.valueOf(quantity));
            }
        });

        btnDetailPlus.setOnClickListener(v -> {
            quantity++;
            tvDetailQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> addToCart());

        tvWriteReview.setOnClickListener(v -> showReviewDialog());
    }

    private void loadFoodDetail() {
        dbService.getFoodById("eq." + foodId).enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentFood = response.body().get(0);
                    displayFoodDetail();
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Log.e(TAG, "loadFoodDetail failed: " + t.getMessage());
                Toast.makeText(FoodDetailActivity.this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFoodDetail() {
        tvDetailName.setText(currentFood.getName());
        tvDetailDescription.setText(currentFood.getDescription());

        Glide.with(this).load(currentFood.getImageUrl()).into(imgFoodDetail);

        // Rating
        tvDetailRating.setText(String.format(Locale.getDefault(), "%.1f", currentFood.getAvgRating()));
        tvDetailReviewCount.setText("(" + currentFood.getTotalReviews() + " đánh giá)");

        // Price
        double discountedPrice = currentFood.getDiscountedPrice();
        tvDetailDiscountedPrice.setText(nf.format(discountedPrice) + " VNĐ");

        if (currentFood.getDiscountPercent() > 0) {
            tvDetailOriginalPrice.setVisibility(View.VISIBLE);
            tvDetailOriginalPrice.setText(nf.format(currentFood.getPrice()) + " VNĐ");
            tvDetailOriginalPrice.setPaintFlags(tvDetailOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            tvDetailDiscount.setVisibility(View.VISIBLE);
            tvDetailDiscount.setText("-" + currentFood.getDiscountPercent() + "%");
        } else {
            tvDetailOriginalPrice.setVisibility(View.GONE);
            tvDetailDiscount.setVisibility(View.GONE);
        }
    }

    private void loadReviews() {
        dbService.getReviews("eq." + foodId, "users(full_name,avatar_url)", "created_at.desc").enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();
                    if (reviews.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        rvReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviews.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                        reviewAdapter.setReviews(reviews);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Log.e(TAG, "loadReviews failed: " + t.getMessage());
            }
        });
    }

    private void addToCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();

        // First get or create cart
        dbService.getCart("eq." + userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    addItemToCart(response.body().get(0).getId());
                } else {
                    // Create new cart
                    Map<String, String> cartData = new HashMap<>();
                    cartData.put("user_id", userId);
                    dbService.createCart(cartData).enqueue(new Callback<List<Cart>>() {
                        @Override
                        public void onResponse(Call<List<Cart>> call2, Response<List<Cart>> response2) {
                            if (response2.isSuccessful() && response2.body() != null && !response2.body().isEmpty()) {
                                addItemToCart(response2.body().get(0).getId());
                            } else {
                                Toast.makeText(FoodDetailActivity.this, "Lỗi tạo giỏ hàng", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Cart>> call2, Throwable t) {
                            Log.e(TAG, "createCart failed: " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e(TAG, "getCart failed: " + t.getMessage());
            }
        });
    }

    private void addItemToCart(String cartId) {
        // Check if item already exists
        dbService.getCartItems("eq." + cartId, "eq." + foodId, "foods(*)").enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Update quantity
                    CartItem existingItem = response.body().get(0);
                    int newQty = existingItem.getQuantity() + quantity;
                    Map<String, Object> update = new HashMap<>();
                    update.put("quantity", newQty);
                    dbService.updateCartItem("eq." + existingItem.getId(), update).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call2, Response<Void> response2) {
                            Toast.makeText(FoodDetailActivity.this, "Đã cập nhật giỏ hàng!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call2, Throwable t) {
                            Log.e(TAG, "updateCartItem failed: " + t.getMessage());
                        }
                    });
                } else {
                    // Add new item
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("cart_id", cartId);
                    itemData.put("food_id", foodId);
                    itemData.put("quantity", quantity);
                    dbService.addCartItem(itemData).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call2, Response<Void> response2) {
                            Toast.makeText(FoodDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call2, Throwable t) {
                            Log.e(TAG, "addCartItem failed: " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Log.e(TAG, "getCartItems failed: " + t.getMessage());
            }
        });
    }

    private void showReviewDialog() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar rbRating = dialogView.findViewById(R.id.rbDialogRating);
        EditText etComment = dialogView.findViewById(R.id.etDialogComment);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    int rating = (int) rbRating.getRating();
                    String comment = etComment.getText().toString().trim();
                    submitReview(rating, comment);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void submitReview(int rating, String comment) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("food_id", foodId);
        reviewData.put("user_id", sessionManager.getUserId());
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);

        dbService.createReview(reviewData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FoodDetailActivity.this, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                } else {
                    Toast.makeText(FoodDetailActivity.this, "Lỗi gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "submitReview failed: " + t.getMessage());
            }
        });
    }
}
