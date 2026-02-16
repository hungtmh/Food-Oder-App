package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";

    private EditText etReceiverName, etPhone, etAddress, etNote;
    private RadioGroup rgPayment;
    private TextView btnBack, tvSubtotal, tvDiscount, tvTotalAmount;
    private Button btnPlaceOrder;

    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private NumberFormat nf;

    private String cartId;
    private double totalAmount;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);
        nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        cartId = getIntent().getStringExtra("cart_id");
        totalAmount = getIntent().getDoubleExtra("total_amount", 0);

        initViews();
        prefillUserInfo();
        loadCartItems();
    }

    private void initViews() {
        etReceiverName = findViewById(R.id.etReceiverName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etNote = findViewById(R.id.etNote);
        rgPayment = findViewById(R.id.rgPayment);
        btnBack = findViewById(R.id.btnBack);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        btnBack.setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        tvSubtotal.setText(nf.format(totalAmount) + " VNĐ");
        tvTotalAmount.setText(nf.format(totalAmount) + " VNĐ");
    }

    private void prefillUserInfo() {
        String name = sessionManager.getFullName();
        String phone = sessionManager.getPhone();
        String address = sessionManager.getAddress();

        if (name != null && !name.isEmpty()) etReceiverName.setText(name);
        if (phone != null && !phone.isEmpty()) etPhone.setText(phone);
        if (address != null && !address.isEmpty()) etAddress.setText(address);
    }

    private void loadCartItems() {
        if (cartId == null) return;

        dbService.getCartItems("eq." + cartId, null, "foods(*)").enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartItems = response.body();
                    // Recalculate totals
                    double subtotal = 0;
                    for (CartItem item : cartItems) {
                        subtotal += item.getSubtotal();
                    }
                    totalAmount = subtotal;
                    tvSubtotal.setText(nf.format(subtotal) + " VNĐ");
                    tvTotalAmount.setText(nf.format(subtotal) + " VNĐ");
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Log.e(TAG, "loadCartItems failed: " + t.getMessage());
            }
        });
    }

    private void placeOrder() {
        String receiverName = etReceiverName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (receiverName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rgPayment.getCheckedRadioButtonId() == R.id.rbCOD ? "cod" : "banking";
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", sessionManager.getUserId());
        orderData.put("order_code", orderCode);
        orderData.put("receiver_name", receiverName);
        orderData.put("phone", phone);
        orderData.put("address", address);
        orderData.put("payment_method", paymentMethod);
        orderData.put("note", note);
        orderData.put("subtotal", totalAmount);
        orderData.put("discount_amount", 0);
        orderData.put("total_amount", totalAmount);
        orderData.put("status", "pending");

        dbService.createOrder(orderData).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Order order = response.body().get(0);
                    createOrderItems(order.getId(), orderCode);
                } else {
                    Log.e(TAG, "createOrder failed: " + response.code());
                    Toast.makeText(CheckoutActivity.this, "Lỗi tạo đơn hàng", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e(TAG, "createOrder failed: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void createOrderItems(String orderId, String orderCode) {
        final int[] completed = {0};
        final int total = cartItems.size();

        for (CartItem item : cartItems) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("order_id", orderId);
            itemData.put("food_id", item.getFoodId());
            itemData.put("food_name", item.getFood() != null ? item.getFood().getName() : "");
            itemData.put("food_image", item.getFood() != null ? item.getFood().getImageUrl() : "");
            itemData.put("price", item.getFood() != null ? item.getFood().getDiscountedPrice() : 0);
            itemData.put("quantity", item.getQuantity());
            itemData.put("subtotal", item.getSubtotal());

            dbService.createOrderItem(itemData).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    completed[0]++;
                    if (completed[0] == total) {
                        clearCartAndFinish(orderCode);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    completed[0]++;
                    Log.e(TAG, "createOrderItem failed: " + t.getMessage());
                    if (completed[0] == total) {
                        clearCartAndFinish(orderCode);
                    }
                }
            });
        }
    }

    private void clearCartAndFinish(String orderCode) {
        dbService.clearCart("eq." + cartId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                navigateToConfirmation(orderCode);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                navigateToConfirmation(orderCode);
            }
        });
    }

    private void navigateToConfirmation(String orderCode) {
        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        intent.putExtra("order_code", orderCode);
        intent.putExtra("total_amount", totalAmount);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void resetButton() {
        btnPlaceOrder.setEnabled(true);
        btnPlaceOrder.setText("Xác nhận đặt hàng");
    }
}
