package com.example.food_order_app.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.food_order_app.model.Voucher;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Address;
import com.example.food_order_app.model.Cart;
import com.example.food_order_app.model.CartItem;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.network.SupabaseEdgeService;
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
    private SupabaseEdgeService edgeService;
    private SessionManager sessionManager;
    private NumberFormat nf;

    private String cartId;
    private double totalAmount;
    private List<CartItem> cartItems;

    // Voucher fields
    private EditText etVoucherCode;
    private Button btnApplyVoucher;
    private Button btnSelectVoucher;
    private double discountAmount = 0;
    private String appliedVoucherId = null;
    private String appliedVoucherCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbService = RetrofitClient.getDbService();
        edgeService = RetrofitClient.getEdgeService();
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
        
        // Voucher
        etVoucherCode = findViewById(R.id.etVoucherCode);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        btnSelectVoucher = findViewById(R.id.btnSelectVoucher);

        btnBack.setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        btnApplyVoucher.setOnClickListener(v -> handleApplyVoucher(etVoucherCode.getText().toString().trim()));
        btnSelectVoucher.setOnClickListener(v -> showVoucherSelectionDialog());
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
                    updateTotalsUI();
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Log.e(TAG, "loadCartItems failed: " + t.getMessage());
            }
        });
    }

    private void showVoucherSelectionDialog() {
        dbService.getVouchers("eq.true", "eq.true", "discount_value.desc").enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Voucher> vouchers = response.body();
                    ListView listView = new ListView(CheckoutActivity.this);
                    listView.setDividerHeight(1);
                    listView.setPadding(24, 8, 24, 8);

                    BaseAdapter adapter = new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return vouchers.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return vouchers.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View row = convertView;
                            if (row == null) {
                                row = LayoutInflater.from(CheckoutActivity.this)
                                        .inflate(android.R.layout.simple_list_item_2, parent, false);
                                row.setPadding(28, 24, 28, 24);
                            }

                            TextView line1 = row.findViewById(android.R.id.text1);
                            TextView line2 = row.findViewById(android.R.id.text2);
                            Voucher v = vouchers.get(position);

                            line1.setText(v.getCode() + "  •  " + v.getTitle());
                            line1.setTextSize(15f);
                            line1.setSingleLine(false);

                            line2.setText(buildVoucherSubtitle(v));
                            line2.setTextSize(13f);
                            line2.setSingleLine(false);
                            return row;
                        }
                    };

                    listView.setAdapter(adapter);

                    AlertDialog dialog = new AlertDialog.Builder(CheckoutActivity.this)
                            .setTitle("Chọn mã giảm giá")
                            .setView(listView)
                            .setNegativeButton("Đóng", null)
                            .create();

                    listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                        Voucher selected = vouchers.get(position);
                        etVoucherCode.setText(selected.getCode());
                        handleApplyVoucherReal(selected);
                        dialog.dismiss();
                    });

                    dialog.show();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Không có mã giảm giá nào phù hợp!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi tải mã giảm giá", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildVoucherSubtitle(Voucher voucher) {
        String discountLabel;
        if ("percent".equals(voucher.getDiscountType())) {
            String maxLabel = voucher.getMaxDiscountAmount() != null
                    ? " (tối đa " + nf.format(voucher.getMaxDiscountAmount()) + "đ)"
                    : "";
            discountLabel = "Giảm " + ((int) voucher.getDiscountValue()) + "%" + maxLabel;
        } else {
            discountLabel = "Giảm " + nf.format(voucher.getDiscountValue()) + "đ";
        }
        return discountLabel + " • Đơn tối thiểu " + nf.format(voucher.getMinOrderValue()) + "đ";
    }

    private void handleApplyVoucher(String inputCode) {
        if (inputCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnApplyVoucher.setEnabled(false);
        dbService.getVoucherByCode("eq." + inputCode, "eq.true").enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(Call<List<Voucher>> call, Response<List<Voucher>> response) {
                btnApplyVoucher.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    handleApplyVoucherReal(response.body().get(0));
                } else {
                    Toast.makeText(CheckoutActivity.this, "Mã không hợp lệ hoặc đã lưu!", Toast.LENGTH_SHORT).show();
                    discountAmount = 0;
                    appliedVoucherCode = null;
                    appliedVoucherId = null;
                    updateTotalsUI();
                }
            }

            @Override
            public void onFailure(Call<List<Voucher>> call, Throwable t) {
                btnApplyVoucher.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Lỗi kiểm tra mã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApplyVoucherReal(Voucher voucher) {
        if (totalAmount < voucher.getMinOrderValue()) {
            Toast.makeText(this, "Đơn hàng chưa đạt tối thiểu " + nf.format(voucher.getMinOrderValue()) + "đ", Toast.LENGTH_SHORT).show();
            return;
        }

        double calculatedDiscount = 0;
        if ("percent".equals(voucher.getDiscountType())) {
            calculatedDiscount = totalAmount * (voucher.getDiscountValue() / 100.0);
            if (voucher.getMaxDiscountAmount() != null && calculatedDiscount > voucher.getMaxDiscountAmount()) {
                calculatedDiscount = voucher.getMaxDiscountAmount();
            }
        } else {
            calculatedDiscount = voucher.getDiscountValue();
        }

        discountAmount = calculatedDiscount;
        appliedVoucherCode = voucher.getCode();
        appliedVoucherId = voucher.getId();
        
        updateTotalsUI();
        Toast.makeText(this, "Áp dụng thành công!", Toast.LENGTH_SHORT).show();
    }

    private void updateTotalsUI() {
        tvSubtotal.setText(nf.format(totalAmount) + " VNĐ");
        if (discountAmount > 0) {
            tvDiscount.setText("-" + nf.format(discountAmount) + " VNĐ");
        } else {
            tvDiscount.setText("0 VNĐ");
        }
        double finalAmount = Math.max(0, totalAmount - discountAmount);
        tvTotalAmount.setText(nf.format(finalAmount) + " VNĐ");
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

        double finalAmount = Math.max(0, totalAmount - discountAmount);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", sessionManager.getUserId());
        orderData.put("order_code", orderCode);
        orderData.put("payment_method", paymentMethod);
        orderData.put("order_type", orderType);
        orderData.put("note", note);
        orderData.put("subtotal", totalAmount);
        orderData.put("discount_amount", discountAmount);
        orderData.put("total_amount", finalAmount);
        orderData.put("status", "pending");
        
        if (appliedVoucherCode != null) {
            orderData.put("applied_voucher_code", appliedVoucherCode);
            orderData.put("voucher_id", appliedVoucherId);
        }

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

        boolean isBanking = rgPayment.getCheckedRadioButtonId() == R.id.rbBanking;
        if (isBanking) {
            createBankingOrder(orderCode, note);
            return;
        }

        dbService.createOrder(orderData).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Order order = response.body().get(0);
                    createOrderItems(order.getId(), orderCode, null, false);
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

    private void createBankingOrder(String orderCode, String note) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", sessionManager.getUserId());
        payload.put("order_code", orderCode);
        payload.put("subtotal", totalAmount);
        payload.put("payment_mode", "BANK_QR");
        payload.put("note", note);

        if (appliedVoucherCode != null) {
            payload.put("voucher_code", appliedVoucherCode);
        }

        if (isDineIn) {
            payload.put("receiver_name", edtDineInName.getText().toString().trim());
            payload.put("phone", sessionManager.getPhone() != null ? sessionManager.getPhone() : "");
            payload.put("address", "Ăn tại quán - " + edtDineInTable.getText().toString().trim());
        } else {
            payload.put("receiver_name", selectedReceiverName);
            payload.put("phone", selectedPhone);
            payload.put("address", selectedAddressText);
        }

        edgeService.createSepayPayment(payload).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(CheckoutActivity.this, "Không tạo được thanh toán QR", Toast.LENGTH_SHORT).show();
                    resetButton();
                    return;
                }

                Map<String, Object> body = response.body();
                Map<String, Object> orderMap = asMap(body.get("order"));
                Map<String, Object> paymentMap = asMap(body.get("payment"));

                String orderId = asString(orderMap.get("id"));
                String qrUrl = asString(paymentMap.get("qr_url"));
                String transferContent = asString(paymentMap.get("transfer_content"));
                double amount = asDouble(paymentMap.get("amount"));

                if (orderId == null || qrUrl == null) {
                    Toast.makeText(CheckoutActivity.this, "Thiếu dữ liệu QR thanh toán", Toast.LENGTH_SHORT).show();
                    resetButton();
                    return;
                }

                createOrderItems(orderId, orderCode, new BankingMeta(qrUrl, transferContent, amount), true);
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "createBankingOrder failed: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Lỗi tạo thanh toán QR", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void createOrderItems(String orderId, String orderCode, BankingMeta bankingMeta, boolean waitForPayment) {
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
                        if (waitForPayment && bankingMeta != null) {
                            showBankingQrDialog(orderId, orderCode, bankingMeta);
                        } else {
                            clearCartAndFinish(orderCode, Math.max(0, totalAmount - discountAmount));
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    completed[0]++;
                    Log.e(TAG, "createOrderItem failed: " + t.getMessage());
                    if (completed[0] == total) {
                        if (waitForPayment && bankingMeta != null) {
                            showBankingQrDialog(orderId, orderCode, bankingMeta);
                        } else {
                            clearCartAndFinish(orderCode, Math.max(0, totalAmount - discountAmount));
                        }
                    }
                }
            });
        }
    }

    private void showBankingQrDialog(String orderId, String orderCode, BankingMeta bankingMeta) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment_qr, null, false);
        ImageView imgQr = view.findViewById(R.id.imgQrPayment);
        TextView tvQrOrderCode = view.findViewById(R.id.tvQrOrderCode);
        TextView tvQrAmount = view.findViewById(R.id.tvQrAmount);
        TextView tvQrContent = view.findViewById(R.id.tvQrContent);
        Button btnPaid = view.findViewById(R.id.btnQrPaid);
        Button btnCancel = view.findViewById(R.id.btnQrCancel);

        tvQrOrderCode.setText("Mã đơn: " + orderCode);
        tvQrAmount.setText("Số tiền: " + nf.format(bankingMeta.amount) + " VNĐ");
        tvQrContent.setText("Nội dung CK: " + (bankingMeta.transferContent != null ? bankingMeta.transferContent : orderCode));

        Glide.with(this).load(bankingMeta.qrUrl).into(imgQr);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            resetButton();
            Toast.makeText(this, "Đơn hàng đang chờ thanh toán", Toast.LENGTH_SHORT).show();
        });

        btnPaid.setOnClickListener(v -> verifyWebhookPaymentThenFinish(orderId, orderCode, bankingMeta.amount, dialog, btnPaid));

        dialog.show();
    }

    private void verifyWebhookPaymentThenFinish(String orderId, String orderCode, double paidAmount, AlertDialog dialog, Button btnPaid) {
        btnPaid.setEnabled(false);
        btnPaid.setText("Đang kiểm tra...");

        dbService.getOrderById("eq." + orderId, "id,status,payment_status").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                btnPaid.setEnabled(true);
                btnPaid.setText("Đã thanh toán");

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "Không kiểm tra được trạng thái đơn", Toast.LENGTH_SHORT).show();
                    return;
                }

                Order order = response.body().get(0);
                boolean paid = "paid".equalsIgnoreCase(order.getPaymentStatus());
                boolean processing = "processing".equalsIgnoreCase(order.getStatus());

                if (paid || processing) {
                    dialog.dismiss();
                    clearCartAndFinish(orderCode, paidAmount);
                } else {
                    Toast.makeText(CheckoutActivity.this, "Thanh toán chưa thành công, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                btnPaid.setEnabled(true);
                btnPaid.setText("Đã thanh toán");
                Toast.makeText(CheckoutActivity.this, "Lỗi kiểm tra thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCartAndFinish(String orderCode, double finalAmount) {
        dbService.clearCart("eq." + cartId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                navigateToConfirmation(orderCode, finalAmount);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                navigateToConfirmation(orderCode, finalAmount);
            }
        });
    }

    private void navigateToConfirmation(String orderCode, double finalAmount) {
        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        intent.putExtra("order_code", orderCode);
        intent.putExtra("total_amount", finalAmount);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>) value;
        }
        return new HashMap<>();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value == null ? 0 : Double.parseDouble(String.valueOf(value));
        } catch (Exception ignore) {
            return 0;
        }
    }

    private static class BankingMeta {
        final String qrUrl;
        final String transferContent;
        final double amount;

        BankingMeta(String qrUrl, String transferContent, double amount) {
            this.qrUrl = qrUrl;
            this.transferContent = transferContent;
            this.amount = amount;
        }
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
