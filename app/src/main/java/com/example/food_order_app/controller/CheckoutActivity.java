package com.example.food_order_app.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Address;
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

    // Order type
    private RadioGroup rgOrderType;
    private LinearLayout layoutDeliveryAddress;
    private boolean isDineIn = false;
    private LinearLayout layoutDineIn;
    private EditText edtDineInName, edtDineInTable;

    // Address card views
    private LinearLayout layoutSelectedAddress, layoutAddressDetail;
    private TextView tvNoAddressHint, tvSelectedName, tvSelectedPhone, tvSelectedAddress;
    // Note + payment
    private EditText etNote;
    private RadioGroup rgPayment;
    private TextView btnBack, tvSubtotal, tvDiscount, tvTotalAmount;
    private Button btnPlaceOrder, btnPickAddress;

    private static final int REQ_PICK_ADDRESS = 1001;

    // Selected address fields
    private String selectedReceiverName, selectedPhone, selectedAddressText;

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
        loadDefaultAddress();
        loadCartItems();
    }

    private void initViews() {
        // Order type
        rgOrderType = findViewById(R.id.rgOrderType);
        layoutDeliveryAddress = findViewById(R.id.layoutDeliveryAddress);
        layoutDineIn = findViewById(R.id.layoutDineIn);
        edtDineInName = findViewById(R.id.edtDineInName);
        edtDineInTable = findViewById(R.id.edtDineInTable);
        
        edtDineInName.setText(sessionManager.getFullName());

        layoutSelectedAddress = findViewById(R.id.layoutSelectedAddress);
        layoutAddressDetail = findViewById(R.id.layoutAddressDetail);
        tvNoAddressHint = findViewById(R.id.tvNoAddressHint);
        tvSelectedName = findViewById(R.id.tvSelectedName);
        tvSelectedPhone = findViewById(R.id.tvSelectedPhone);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        etNote = findViewById(R.id.etNote);
        rgPayment = findViewById(R.id.rgPayment);
        btnBack = findViewById(R.id.btnBack);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPickAddress = findViewById(R.id.btnPickAddress);

        btnBack.setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        btnPickAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            intent.putExtra(AddressActivity.EXTRA_PICK_MODE, true);
            startActivityForResult(intent, REQ_PICK_ADDRESS);
        });

        // Order type toggle
        rgOrderType.setOnCheckedChangeListener((group, checkedId) -> {
            isDineIn = (checkedId == R.id.rbDineIn);
            layoutDeliveryAddress.setVisibility(isDineIn ? View.GONE : View.VISIBLE);
            layoutDineIn.setVisibility(isDineIn ? View.VISIBLE : View.GONE);
        });

        tvSubtotal.setText(nf.format(totalAmount) + " VNĐ");
        tvTotalAmount.setText(nf.format(totalAmount) + " VNĐ");
        showNoAddress();
    }

    private void loadDefaultAddress() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;
        dbService.getAddresses("eq." + userId, "is_default.desc,created_at.asc")
                .enqueue(new Callback<List<Address>>() {
                    @Override
                    public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Address addr = response.body().get(0);
                            applyAddress(addr.getReceiverName(), addr.getPhone(), addr.getAddress());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Address>> call, Throwable t) {
                        Log.e(TAG, "loadDefaultAddress failed: " + t.getMessage());
                    }
                });
    }

    private void applyAddress(String name, String phone, String address) {
        selectedReceiverName = name;
        selectedPhone = phone;
        selectedAddressText = address;
        tvSelectedName.setText(name);
        tvSelectedPhone.setText("📞 " + phone);
        tvSelectedAddress.setText("📍 " + address);
        tvNoAddressHint.setVisibility(View.GONE);
        layoutAddressDetail.setVisibility(View.VISIBLE);
    }

    private void showNoAddress() {
        tvNoAddressHint.setVisibility(View.VISIBLE);
        layoutAddressDetail.setVisibility(View.GONE);
    }

    private void loadCartItems() {
        if (cartId == null) return;

        dbService.getCartItems("eq." + cartId, null, "*,foods(*)").enqueue(new Callback<List<CartItem>>() {
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
        // Only require address for delivery
        if (!isDineIn) {
            if (selectedReceiverName == null || selectedPhone == null || selectedAddressText == null) {
                Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            String dineInName = edtDineInName.getText().toString().trim();
            String dineInTable = edtDineInTable.getText().toString().trim();
            if (dineInName.isEmpty() || dineInTable.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và số bàn", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String note = etNote.getText().toString().trim();

        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rgPayment.getCheckedRadioButtonId() == R.id.rbCOD ? "cod" : "banking";
        String orderType = isDineIn ? "dine_in" : "delivery";
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", sessionManager.getUserId());
        orderData.put("order_code", orderCode);
        orderData.put("payment_method", paymentMethod);
        orderData.put("order_type", orderType);
        orderData.put("note", note);
        orderData.put("subtotal", totalAmount);
        orderData.put("discount_amount", 0);
        orderData.put("total_amount", totalAmount);
        orderData.put("status", "pending");

        if (isDineIn) {
            String dineInName = edtDineInName.getText().toString().trim();
            String dineInTable = edtDineInTable.getText().toString().trim();
            orderData.put("receiver_name", dineInName);
            orderData.put("phone", sessionManager.getPhone() != null ? sessionManager.getPhone() : "");
            orderData.put("address", "Ăn tại quán - " + dineInTable);
        } else {
            orderData.put("receiver_name", selectedReceiverName);
            orderData.put("phone", selectedPhone);
            orderData.put("address", selectedAddressText);
        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_ADDRESS && resultCode == Activity.RESULT_OK && data != null) {
            applyAddress(
                    data.getStringExtra(AddressActivity.RESULT_RECEIVER_NAME),
                    data.getStringExtra(AddressActivity.RESULT_PHONE),
                    data.getStringExtra(AddressActivity.RESULT_ADDRESS)
            );
        }
    }
}
