package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CartAdapter;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
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

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {
    private static final String TAG = "CartActivity";

    private RecyclerView rvCartItems;
    private LinearLayout emptyState;
    private TextView tvTotalAmount, tvClearCart, btnBack;
    private Button btnCheckout, btnContinueShopping;

    private CartAdapter cartAdapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private NumberFormat nf;

    private String cartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);
        nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        emptyState = findViewById(R.id.emptyState);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvClearCart = findViewById(R.id.tvClearCart);
        btnBack = findViewById(R.id.btnBack);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        cartAdapter = new CartAdapter(this, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnContinueShopping.setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            if (cartAdapter.getCartItems().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("cart_id", cartId);
            intent.putExtra("total_amount", calculateTotal());
            startActivity(intent);
        });

        tvClearCart.setOnClickListener(v -> {
            if (cartId == null) return;
            new AlertDialog.Builder(this)
                    .setTitle("Xóa giỏ hàng")
                    .setMessage("Bạn có chắc muốn xóa tất cả?")
                    .setPositiveButton("Xóa", (d, w) -> clearCart())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadCart() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyState();
            return;
        }

        String userId = sessionManager.getUserId();
        dbService.getCart("eq." + userId).enqueue(new Callback<List<Cart>>() {
            @Override
            public void onResponse(Call<List<Cart>> call, Response<List<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cartId = response.body().get(0).getId();
                    loadCartItems();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Cart>> call, Throwable t) {
                Log.e(TAG, "loadCart failed: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void loadCartItems() {
        dbService.getCartItems("eq." + cartId, null, "foods(*)").enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cartAdapter.setCartItems(response.body());
                    updateTotal();
                    showCartItems();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Log.e(TAG, "loadCartItems failed: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    @Override
    public void onQuantityChange(CartItem item, int newQuantity) {
        Map<String, Object> update = new HashMap<>();
        update.put("quantity", newQuantity);
        dbService.updateCartItem("eq." + item.getId(), update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                item.setQuantity(newQuantity);
                cartAdapter.notifyDataSetChanged();
                updateTotal();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "updateQuantity failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void onRemoveItem(CartItem item, int position) {
        dbService.deleteCartItem("eq." + item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                cartAdapter.removeItem(position);
                updateTotal();
                if (cartAdapter.getCartItems().isEmpty()) {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "deleteItem failed: " + t.getMessage());
            }
        });
    }

    private void clearCart() {
        dbService.clearCart("eq." + cartId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                cartAdapter.setCartItems(new java.util.ArrayList<>());
                showEmptyState();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "clearCart failed: " + t.getMessage());
            }
        });
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartAdapter.getCartItems()) {
            total += item.getSubtotal();
        }
        return total;
    }

    private void updateTotal() {
        tvTotalAmount.setText(nf.format(calculateTotal()) + " VNĐ");
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvCartItems.setVisibility(View.GONE);
    }

    private void showCartItems() {
        emptyState.setVisibility(View.GONE);
        rvCartItems.setVisibility(View.VISIBLE);
    }
}
