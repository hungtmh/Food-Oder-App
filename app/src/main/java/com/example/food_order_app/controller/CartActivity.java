package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.CartAdapter;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
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

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {
    private static final String TAG = "CartActivity";

    private RecyclerView rvCartItems;
    private LinearLayout emptyState;
    private TextView tvTotalAmount, tvClearCart;
    private Button btnCheckout, btnContinueShopping;
    private BottomNavigationView bottomNav;

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
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
        }
        loadCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        emptyState = findViewById(R.id.emptyState);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvClearCart = findViewById(R.id.tvClearCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        bottomNav = findViewById(R.id.bottomNav);

        setupBottomNav();

        cartAdapter = new CartAdapter(this, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {
            private final Paint bgPaint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView r,
                                  @NonNull RecyclerView.ViewHolder v,
                                  @NonNull RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                List<CartItem> items = cartAdapter.getCartItems();
                if (pos != RecyclerView.NO_ID && pos < items.size()) {
                    deleteItemWithUndo(items.get(pos), pos);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                // Fade trash icon on item theo tỉ lệ kéo
                View btnDelete = itemView.findViewById(R.id.btnDeleteItem);
                if (btnDelete != null) {
                    float fraction = Math.min(1f, Math.abs(dX) / (itemView.getWidth() * 0.4f));
                    btnDelete.setAlpha(1f - fraction);
                }

                if (dX < 0) {
                    bgPaint.setColor(ContextCompat.getColor(CartActivity.this, R.color.error));
                    RectF bg = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop() + 4f,
                            itemView.getRight(),
                            itemView.getBottom() - 4f);
                    c.drawRoundRect(bg, 16f, 16f, bgPaint);

                    Drawable icon = ContextCompat.getDrawable(CartActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        int iconSize = icon.getIntrinsicHeight();
                        int margin = (itemView.getHeight() - iconSize) / 2;
                        int iconTop = itemView.getTop() + margin;
                        int iconRight = itemView.getRight() - margin;
                        icon.setBounds(iconRight - iconSize, iconTop, iconRight, iconTop + iconSize);
                        icon.setTint(Color.WHITE);
                        icon.draw(c);
                    }
                } else {
                    // Khôi phục alpha khi không kéo
                    if (btnDelete != null) btnDelete.setAlpha(1f);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCartItems);

        if (btnContinueShopping != null) {
            btnContinueShopping.setOnClickListener(v -> finish());
        }

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
            if (cartAdapter.getCartItems().isEmpty()) return;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa giỏ hàng")
                    .setMessage("Bạn có chắc muốn xóa tất cả món?")
                    .setPositiveButton("Xóa", (d, w) -> clearCartWithUndo())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_cart);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                return true;
            } else if (id == R.id.nav_chat) {
                if (sessionManager != null && sessionManager.isLoggedIn()) {
                    Intent intent = new Intent(this, ChatRoomActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Vui lòng đăng nhập để chat", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.nav_contact) {
                Intent intent = new Intent(this, ContactActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_account) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
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
        dbService.getCartItems("eq." + cartId, null, "*,foods(*)").enqueue(new Callback<List<CartItem>>() {
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
        deleteItemWithUndo(item, position);
    }

    private void deleteItemWithUndo(CartItem item, int position) {
        String itemName = item.getFood() != null ? item.getFood().getName() : "món ăn";
        cartAdapter.removeItem(position);
        updateTotal();
        if (cartAdapter.getCartItems().isEmpty()) showEmptyState();

        Snackbar.make(findViewById(android.R.id.content),
                "Dã xóa: " + itemName, Snackbar.LENGTH_LONG)
                .setAction("Hoàn tác", v -> {
                    cartAdapter.addItemAt(position, item);
                    updateTotal();
                    showCartItems();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar sb, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            dbService.deleteCartItem("eq." + item.getId())
                                    .enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {}
                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Log.e(TAG, "deleteItem failed: " + t.getMessage());
                                        }
                                    });
                        }
                    }
                }).show();
    }

    private void clearCartWithUndo() {
        if (cartId == null || cartAdapter.getCartItems().isEmpty()) return;
        List<CartItem> backup = new ArrayList<>(cartAdapter.getCartItems());

        cartAdapter.setCartItems(new ArrayList<>());
        showEmptyState();
        updateTotal();

        Snackbar.make(findViewById(android.R.id.content),
                "Dã xóa toàn bộ giỏ hàng", Snackbar.LENGTH_LONG)
                .setAction("Hoàn tác", v -> {
                    cartAdapter.setCartItems(backup);
                    showCartItems();
                    updateTotal();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar sb, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            dbService.clearCart("eq." + cartId)
                                    .enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {}
                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Log.e(TAG, "clearCart failed: " + t.getMessage());
                                        }
                                    });
                        }
                    }
                }).show();
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
