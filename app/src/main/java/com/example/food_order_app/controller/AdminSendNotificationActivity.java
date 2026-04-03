package com.example.food_order_app.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Notification;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.AdminDrawerHelper;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSendNotificationActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnBack;
    private EditText etTitle, etMessage;
    private Button btnSendAll, btnClear;
    private TextView tvCharCount, tvStatus;

    private SupabaseDbService dbService;
    private boolean isSending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_notification);

        dbService = RetrofitClient.getDbService();
        initViews();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
        btnBack = findViewById(R.id.btnBackNotifSend);
        etTitle = findViewById(R.id.etNotifTitle);
        etMessage = findViewById(R.id.etNotifMessage);
        btnSendAll = findViewById(R.id.btnSendToAll);
        btnClear = findViewById(R.id.btnClearForm);
        tvCharCount = findViewById(R.id.tvCharCount);
        tvStatus = findViewById(R.id.tvStatus);

        AdminDrawerHelper.setupDrawer(this, drawerLayout, navigationView, btnBack, R.id.navSendNotification);
        btnClear.setOnClickListener(v -> clearForm());
        btnSendAll.setOnClickListener(v -> sendNotificationToAll());

        etMessage.setOnFocusChangeListener((v, hasFocus) -> updateCharCount());
        etTitle.setText("Khuyến mãi đặc biệt");
    }

    private void updateCharCount() {
        int count = etMessage.getText().length();
        tvCharCount.setText(count + "/500");
    }

    private void clearForm() {
        etTitle.setText("");
        etMessage.setText("");
        tvCharCount.setText("0/500");
        tvStatus.setText("");
        tvStatus.setVisibility(View.GONE);
    }

    private void sendNotificationToAll() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }

        if (message.isEmpty()) {
            etMessage.setError("Vui lòng nhập nội dung");
            return;
        }

        if (message.length() > 500) {
            etMessage.setError("Nội dung không vượt quá 500 ký tự");
            return;
        }

        // Confirm before sending
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Xác nhận gửi thông báo")
                .setMessage("Gửi thông báo này đến tất cả khách hàng?")
                .setPositiveButton("Gửi", (dialog, which) -> performSending(title, message))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performSending(String title, String message) {
        if (isSending)
            return;
        isSending = true;

        disableButtons();
        showStatus("Đang gửi thông báo...", false);

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("message", message);
        payload.put("is_broadcast", true);

        dbService.sendBroadcastNotification(payload)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        isSending = false;
                        enableButtons();

                        if (response.isSuccessful()) {
                            int count = 0;
                            if (response.body() != null && response.body().containsKey("count")) {
                                Object obj = response.body().get("count");
                                if (obj instanceof Double) {
                                    count = ((Double) obj).intValue();
                                } else if (obj instanceof Integer) {
                                    count = (Integer) obj;
                                }
                            }

                            String successMsg = "Gửi thành công đến " + count + " khách hàng!";
                            showStatus(successMsg, true);
                            Toast.makeText(AdminSendNotificationActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            showStatus("Gửi thất bại: HTTP " + response.code(), false);
                            Toast.makeText(AdminSendNotificationActivity.this, "Gửi thất bại", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        isSending = false;
                        enableButtons();
                        showStatus("Lỗi: " + t.getMessage(), false);
                        Toast.makeText(AdminSendNotificationActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void showStatus(String text, boolean success) {
        tvStatus.setText(text);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setTextColor(getResources().getColor(success ? R.color.success : R.color.error));
    }

    private void disableButtons() {
        btnSendAll.setEnabled(false);
        btnClear.setEnabled(false);
        etTitle.setEnabled(false);
        etMessage.setEnabled(false);
    }

    private void enableButtons() {
        btnSendAll.setEnabled(true);
        btnClear.setEnabled(true);
        etTitle.setEnabled(true);
        etMessage.setEnabled(true);
    }
}
