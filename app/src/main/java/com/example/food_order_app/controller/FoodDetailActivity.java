package com.example.food_order_app.controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.adapter.ReviewAdapter;
import com.example.food_order_app.config.SupabaseConfig;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
import com.example.food_order_app.model.Favorite;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.Review;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.network.SupabaseStorageService;
import com.example.food_order_app.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailActivity extends AppCompatActivity {
    private static final String TAG = "FoodDetailActivity";
    private static final String REVIEW_BUCKET = "reviews";

    private ImageView imgFoodDetail;
    private ImageView btnDetailFavorite;
    private TextView btnBack, tvDetailDiscount, tvDetailName, tvDetailRating, tvDetailReviewCount;
    private TextView tvDetailDiscountedPrice, tvDetailOriginalPrice, tvDetailDescription;
    private TextView tvDetailQuantity, tvWriteReview, tvNoReviews, tvViewAllReviews;
    private ImageButton btnDetailMinus, btnDetailPlus;
    private Button btnAddToCart;
    private FloatingActionButton fabQuickCart;
    private RecyclerView rvReviews;

    private SupabaseDbService dbService;
    private SupabaseStorageService storageService;
    private SessionManager sessionManager;
    private ReviewAdapter reviewAdapter;
    private NumberFormat nf;
    private ProgressDialog progressDialog;

    private Food currentFood;
    private int quantity = 1;
    private String foodId;
    private boolean isFavorite = false;

    // Review header views
    private View layoutReviewHeader;
    private TextView tvDetailReviewHeaderTitle;
    private RatingBar rbDetailReviewHeader;
    private TextView tvDetailReviewHeaderScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        dbService = RetrofitClient.getDbService();
        storageService = RetrofitClient.getStorageService();
        sessionManager = new SessionManager(this);
        nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        foodId = getIntent().getStringExtra("food_id");
        if (foodId == null) {
            finish();
            return;
        }

        initViews();
        loadFoodDetail();
        loadReviews();
        checkFavoriteStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (foodId != null) {
            loadFoodDetail();
            // loadReviews will be called automatically but just to be safe:
            loadReviews();
        }
    }



    private void initViews() {
        imgFoodDetail = findViewById(R.id.imgFoodDetail);
        btnBack = findViewById(R.id.btnBack);
        btnDetailFavorite = findViewById(R.id.btnDetailFavorite);
        tvDetailDiscount = findViewById(R.id.tvDetailDiscount);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailRating = findViewById(R.id.tvDetailRating);
        tvDetailReviewCount = findViewById(R.id.tvDetailReviewCount);
        tvDetailDiscountedPrice = findViewById(R.id.tvDetailDiscountedPrice);
        tvDetailOriginalPrice = findViewById(R.id.tvDetailOriginalPrice);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        btnDetailMinus = findViewById(R.id.btnDetailMinus);
        btnDetailPlus = findViewById(R.id.btnDetailPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        fabQuickCart = findViewById(R.id.fabQuickCart);
        rvReviews = findViewById(R.id.rvReviews);

        layoutReviewHeader = findViewById(R.id.layoutReviewHeader);
        tvDetailReviewHeaderTitle = findViewById(R.id.tvDetailReviewHeaderTitle);
        rbDetailReviewHeader = findViewById(R.id.rbDetailReviewHeader);
        tvDetailReviewHeaderScore = findViewById(R.id.tvDetailReviewHeaderScore);

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

        fabQuickCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        layoutReviewHeader.setOnClickListener(v -> {
            Intent intent = new Intent(this, FoodReviewsActivity.class);
            intent.putExtra("food_id", foodId);
            intent.putExtra("food_name", currentFood != null ? currentFood.getName() : "");
            startActivity(intent);
        });

        btnDetailFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void checkFavoriteStatus() {
        if (!sessionManager.isLoggedIn()) return;
        dbService.checkFavorite("eq." + sessionManager.getUserId(), "eq." + foodId)
                .enqueue(new Callback<List<Favorite>>() {
                    @Override
                    public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            isFavorite = true;
                            btnDetailFavorite.setImageResource(R.drawable.ic_favorite_filled);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Favorite>> call, Throwable t) {
                        Log.e(TAG, "checkFavorite failed: " + t.getMessage());
                    }
                });
    }

    private void toggleFavorite() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();

        if (isFavorite) {
            dbService.removeFavorite("eq." + userId, "eq." + foodId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isFavorite = false;
                        btnDetailFavorite.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(FoodDetailActivity.this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "removeFavorite failed: " + t.getMessage());
                }
            });
        } else {
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("user_id", userId);
            data.put("food_id", foodId);
            dbService.addFavorite(data).enqueue(new Callback<List<Favorite>>() {
                @Override
                public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                    if (response.isSuccessful()) {
                        isFavorite = true;
                        btnDetailFavorite.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(FoodDetailActivity.this, "Đã thêm vào yêu thích ❤", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<Favorite>> call, Throwable t) {
                    Log.e(TAG, "addFavorite failed: " + t.getMessage());
                }
            });
        }
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

        // Review Header
        tvDetailReviewHeaderTitle.setText("Đánh giá (" + currentFood.getTotalReviews() + ")");
        rbDetailReviewHeader.setRating((float) currentFood.getAvgRating());
        tvDetailReviewHeaderScore.setText(String.format(Locale.getDefault(), "%.1f", currentFood.getAvgRating()));
    }

    private void loadReviews() {
        dbService.getReviews("eq." + foodId, "*,users(full_name,avatar_url)", "created_at.desc").enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();

                    int total = reviews.size();
                    double sum = 0;
                    for (Review r : reviews) sum += r.getRating();
                    double avg = total > 0 ? (sum / total) : 0;
                    avg = Math.round(avg * 10.0) / 10.0;
                    
                    tvDetailRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
                    tvDetailReviewCount.setText("(" + total + " đánh giá)");
                    tvDetailReviewHeaderTitle.setText("Đánh giá (" + total + ")");
                    rbDetailReviewHeader.setRating((float) avg);
                    tvDetailReviewHeaderScore.setText(String.format(Locale.getDefault(), "%.1f", avg));

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
        dbService.getCartItems("eq." + cartId, "eq." + foodId, "*,foods(*)").enqueue(new Callback<List<CartItem>>() {
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


}
