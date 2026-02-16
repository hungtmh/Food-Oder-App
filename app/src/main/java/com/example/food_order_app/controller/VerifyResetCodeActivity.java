package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.PasswordResetCode;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.PasswordUtils;
import com.example.food_order_app.utils.ValidationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình xác nhận mã code và đặt lại mật khẩu
 */
public class VerifyResetCodeActivity extends AppCompatActivity {

    private TextView btnBack;
    private TextView tvEmail;
    private EditText edtCode, edtNewPassword, edtConfirmPassword;
    private Button btnResetPassword;
    private ProgressDialog progressDialog;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_reset_code);

        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không nhận được email!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEmail = findViewById(R.id.tvEmail);
        edtCode = findViewById(R.id.edtCode);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        tvEmail.setText(email);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnResetPassword.setOnClickListener(v -> handleVerifyAndReset());
    }

    private void handleVerifyAndReset() {
        String code = edtCode.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validation
        if (ValidationUtils.isEmpty(code)) {
            edtCode.setError("Vui lòng nhập mã xác nhận");
            edtCode.requestFocus();
            return;
        }

        if (code.length() != 6) {
            edtCode.setError("Mã xác nhận phải có 6 chữ số");
            edtCode.requestFocus();
            return;
        }

        if (ValidationUtils.isEmpty(newPassword)) {
            edtNewPassword.setError(getString(R.string.error_password_required));
            edtNewPassword.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            edtNewPassword.setError(getString(R.string.error_password_short));
            edtNewPassword.requestFocus();
            return;
        }

        if (ValidationUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmPassword.requestFocus();
            return;
        }

        progressDialog.show();

        SupabaseDbService dbService = RetrofitClient.getDbService();

        // Bước 1: Kiểm tra mã code
        Log.d("VerifyReset", "Kiểm tra mã code: " + code + " cho email: " + email);
        dbService.getResetCode("eq." + email, "eq." + code, "eq.false", "*")
                .enqueue(new Callback<List<PasswordResetCode>>() {
                    @Override
                    public void onResponse(Call<List<PasswordResetCode>> call, Response<List<PasswordResetCode>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            PasswordResetCode resetCode = response.body().get(0);

                            // Kiểm tra mã đã hết hạn chưa
                            if (isExpired(resetCode.getExpiresAt())) {
                                progressDialog.dismiss();
                                Log.d("VerifyReset", "Mã code đã hết hạn");
                                Toast.makeText(VerifyResetCodeActivity.this,
                                        "Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới!",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Mã hợp lệ -> Bước 2: Cập nhật mật khẩu
                            Log.d("VerifyReset", "Mã code hợp lệ, tiến hành cập nhật mật khẩu");
                            updatePassword(resetCode.getId(), newPassword, dbService);
                        } else {
                            progressDialog.dismiss();
                            Log.d("VerifyReset", "Mã code không hợp lệ hoặc đã được sử dụng");
                            Toast.makeText(VerifyResetCodeActivity.this,
                                    "Mã xác nhận không đúng hoặc đã được sử dụng!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PasswordResetCode>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("VerifyReset", "Lỗi kiểm tra mã code: " + t.getMessage(), t);
                        Toast.makeText(VerifyResetCodeActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updatePassword(String resetCodeId, String newPassword, SupabaseDbService dbService) {
        // Hash mật khẩu mới
        String hashedPassword = PasswordUtils.hashPassword(newPassword);

        // Cập nhật password trong bảng users
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", hashedPassword);

        Log.d("VerifyReset", "Cập nhật password cho email: " + email);
        dbService.updatePasswordByEmail("eq." + email, updates)
                .enqueue(new Callback<List<com.example.food_order_app.model.User>>() {
                    @Override
                    public void onResponse(Call<List<com.example.food_order_app.model.User>> call,
                                           Response<List<com.example.food_order_app.model.User>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            // Đánh dấu mã code đã sử dụng
                            Log.d("VerifyReset", "Cập nhật password thành công, đánh dấu code đã dùng");
                            markCodeAsUsed(resetCodeId, dbService);
                        } else {
                            progressDialog.dismiss();
                            Log.e("VerifyReset", "Lỗi cập nhật password, HTTP " + response.code());
                            Toast.makeText(VerifyResetCodeActivity.this,
                                    "Lỗi cập nhật mật khẩu. Vui lòng thử lại!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.food_order_app.model.User>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("VerifyReset", "Network error khi cập nhật password: " + t.getMessage(), t);
                        Toast.makeText(VerifyResetCodeActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void markCodeAsUsed(String resetCodeId, SupabaseDbService dbService) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("used", true);

        dbService.updateResetCode("eq." + resetCodeId, updates)
                .enqueue(new Callback<List<PasswordResetCode>>() {
                    @Override
                    public void onResponse(Call<List<PasswordResetCode>> call, Response<List<PasswordResetCode>> response) {
                        progressDialog.dismiss();

                        if (response.isSuccessful()) {
                            Log.d("VerifyReset", "Đánh dấu code đã sử dụng thành công");
                            Toast.makeText(VerifyResetCodeActivity.this,
                                    "Đặt lại mật khẩu thành công!",
                                    Toast.LENGTH_LONG).show();

                            // Chuyển về màn hình đăng nhập
                            Intent intent = new Intent(VerifyResetCodeActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("VerifyReset", "Lỗi đánh dấu code, nhưng password đã được cập nhật");
                            Toast.makeText(VerifyResetCodeActivity.this,
                                    "Đặt lại mật khẩu thành công!",
                                    Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(VerifyResetCodeActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PasswordResetCode>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("VerifyReset", "Network error khi đánh dấu code: " + t.getMessage(), t);

                        // Vẫn thông báo thành công vì password đã được update
                        Toast.makeText(VerifyResetCodeActivity.this,
                                "Đặt lại mật khẩu thành công!",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(VerifyResetCodeActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private boolean isExpired(String expiresAt) {
        try {
            // Supabase trả về timestamp có thể có nhiều format khác nhau
            // Ví dụ: "2026-02-16T10:30:00+00:00" hoặc "2026-02-16T10:30:00.123456+00:00"
            SimpleDateFormat sdf;
            
            if (expiresAt.contains("+") || expiresAt.contains("Z")) {
                // Có timezone
                if (expiresAt.contains(".")) {
                    // Có milliseconds
                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
                } else {
                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
                }
            } else {
                // Không có timezone
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            }
            
            Date expiry = sdf.parse(expiresAt);
            Date now = new Date();
            
            Log.d("VerifyReset", "Expires at: " + expiresAt);
            Log.d("VerifyReset", "Parsed expiry: " + expiry);
            Log.d("VerifyReset", "Current time: " + now);
            Log.d("VerifyReset", "Is expired? " + now.after(expiry));
            
            return now.after(expiry);
        } catch (ParseException e) {
            Log.e("VerifyReset", "Lỗi parse expiry date: " + e.getMessage(), e);
            return false; // Nếu không parse được, coi như chưa hết hạn
        }
    }
}
