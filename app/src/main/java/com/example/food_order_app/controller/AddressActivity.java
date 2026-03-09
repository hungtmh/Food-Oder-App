package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AddressAdapter;
import com.example.food_order_app.model.Address;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình quản lý địa chỉ giao hàng.
 * Hỗ trợ 2 chế độ:
 *  - MANAGE (từ Profile): xem, thêm, sửa, xóa, đặt mặc định
 *  - PICK (từ Checkout): chọn địa chỉ → trả kết quả về
 */
public class AddressActivity extends AppCompatActivity
        implements AddressAdapter.OnAddressActionListener {

    private static final String TAG = "AddressActivity";

    /** Extra để bật chế độ chọn địa chỉ khi đặt hàng */
    public static final String EXTRA_PICK_MODE = "pick_mode";
    /** Extra trả về: tên người nhận */
    public static final String RESULT_RECEIVER_NAME = "receiver_name";
    /** Extra trả về: số điện thoại */
    public static final String RESULT_PHONE = "phone";
    /** Extra trả về: địa chỉ */
    public static final String RESULT_ADDRESS = "address";

    private ImageView btnBack;
    private TextView tvTitle;
    private RecyclerView rvAddresses;
    private LinearLayout layoutEmpty;
    private Button btnAddAddress;

    private AddressAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private boolean pickMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        pickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);
        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();
        loadAddresses();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        rvAddresses = findViewById(R.id.rvAddresses);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnAddAddress = findViewById(R.id.btnAddAddress);

        if (pickMode) tvTitle.setText("Chọn địa chỉ");

        adapter = new AddressAdapter(this, this, pickMode);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnAddAddress.setOnClickListener(v -> showAddressDialog(null, -1));
    }

    private void loadAddresses() {
        String userId = sessionManager.getUserId();
        if (userId == null) { showEmpty(); return; }

        dbService.getAddresses("eq." + userId, "is_default.desc,created_at.asc")
                .enqueue(new Callback<List<Address>>() {
                    @Override
                    public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            adapter.setAddresses(response.body());
                            showList();
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Address>> call, Throwable t) {
                        Log.e(TAG, "loadAddresses failed: " + t.getMessage());
                        showEmpty();
                    }
                });
    }

    // ============ INTERFACE CALLBACKS ============

    @Override
    public void onPick(Address address) {
        Intent result = new Intent();
        result.putExtra(RESULT_RECEIVER_NAME, address.getReceiverName());
        result.putExtra(RESULT_PHONE, address.getPhone());
        result.putExtra(RESULT_ADDRESS, address.getAddress());
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onSetDefault(Address address, int position) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        // Bỏ mặc định tất cả → set mặc định cho địa chỉ này
        Map<String, Object> clearMap = new HashMap<>();
        clearMap.put("is_default", false);

        dbService.clearDefaultAddresses("eq." + userId, clearMap)
                .enqueue(new Callback<List<Address>>() {
                    @Override
                    public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                        Map<String, Object> setMap = new HashMap<>();
                        setMap.put("is_default", true);
                        dbService.updateAddress("eq." + address.getId(), setMap)
                                .enqueue(new Callback<List<Address>>() {
                                    @Override
                                    public void onResponse(Call<List<Address>> c, Response<List<Address>> r) {
                                        if (r.isSuccessful()) {
                                            address.setDefault(true);
                                            adapter.refreshDefaults(address.getId());
                                            Toast.makeText(AddressActivity.this,
                                                    "Đã đặt mặc định", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Address>> c, Throwable t) {
                                        Log.e(TAG, "setDefault failed: " + t.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<List<Address>> call, Throwable t) {
                        Log.e(TAG, "clearDefault failed: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onEdit(Address address, int position) {
        showAddressDialog(address, position);
    }

    @Override
    public void onDelete(Address address, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (d, w) -> deleteAddress(address, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ============ ADD / EDIT DIALOG ============

    private void showAddressDialog(Address existing, int position) {
        boolean isEdit = existing != null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        form.setPadding(pad, pad, pad, 0);

        EditText etName = makeEditText("Tên người nhận *", InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        EditText etPhone = makeEditText("Số điện thoại *", InputType.TYPE_CLASS_PHONE);
        EditText etAddr = makeEditText("Địa chỉ giao hàng *", InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        if (isEdit) {
            etName.setText(existing.getReceiverName());
            etPhone.setText(existing.getPhone());
            etAddr.setText(existing.getAddress());
        }

        form.addView(etName);
        form.addView(etPhone);
        form.addView(etAddr);

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa địa chỉ" : "Thêm địa chỉ mới")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String addr = etAddr.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!phone.matches("^0[3|5|7|8|9][0-9]{8}$")) {
                        Toast.makeText(this, "Số điện thoại không hợp lệ (VD: 0912345678)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        updateAddress(existing, name, phone, addr, position);
                    } else {
                        createAddress(name, phone, addr);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private EditText makeEditText(String hint, int inputType) {
        EditText et = new EditText(this);
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (48 * dp));
        lp.bottomMargin = (int) (10 * dp);
        et.setLayoutParams(lp);
        et.setHint(hint);
        et.setInputType(inputType);
        et.setBackground(getResources().getDrawable(R.drawable.bg_edit_text, getTheme()));
        et.setPaddingRelative((int) (12 * dp), 0, (int) (12 * dp), 0);
        et.setTextSize(14);
        return et;
    }

    // ============ API CALLS ============

    private void createAddress(String name, String phone, String addr) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("receiver_name", name);
        body.put("phone", phone);
        body.put("address", addr);
        body.put("is_default", false);

        dbService.createAddress(body).enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    loadAddresses();
                    Toast.makeText(AddressActivity.this, "Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddressActivity.this, "Lỗi thêm địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                Log.e(TAG, "createAddress failed: " + t.getMessage());
            }
        });
    }

    private void updateAddress(Address existing, String name, String phone, String addr, int position) {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver_name", name);
        body.put("phone", phone);
        body.put("address", addr);

        dbService.updateAddress("eq." + existing.getId(), body).enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Address updated = response.body().get(0);
                    adapter.updateAt(position, updated);
                    Toast.makeText(AddressActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                Log.e(TAG, "updateAddress failed: " + t.getMessage());
            }
        });
    }

    private void deleteAddress(Address address, int position) {
        dbService.deleteAddress("eq." + address.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                adapter.removeAt(position);
                if (adapter.getItemCount() == 0) showEmpty();
                Toast.makeText(AddressActivity.this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "deleteAddress failed: " + t.getMessage());
            }
        });
    }

    private void showList() {
        rvAddresses.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        rvAddresses.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }
}
