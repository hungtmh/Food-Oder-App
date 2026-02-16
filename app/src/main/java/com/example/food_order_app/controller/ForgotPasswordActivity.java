package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.PasswordResetCode;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.EmailSender;
import com.example.food_order_app.utils.ValidationUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình Quên mật khẩu (Bước 1: Nhập email, tạo mã code)
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView btnBack;
    private EditText edtEmail;
    private Button btnSendReset;
    private TextView tvBackToLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtEmail = findViewById(R.id.edtEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvBackToLogin.setOnClickListener(v -> finish());

        btnSendReset.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleForgotPassword() {
        String email = edtEmail.getText().toString().trim();

        // Validation
        if (ValidationUtils.isEmpty(email)) {
            edtEmail.setError(getString(R.string.error_email_required));
            edtEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            edtEmail.setError(getString(R.string.error_email_invalid));
            edtEmail.requestFocus();
            return;
        }

        progressDialog.show();

        SupabaseDbService dbService = RetrofitClient.getDbService();

        // Bước 1: Kiểm tra email có tồn tại không
        Log.d("ForgotPassword", "Kiểm tra email tồn tại: " + email);
        dbService.getUserByEmail("eq." + email, "*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            // Email tồn tại -> tạo mã code
                            Log.d("ForgotPassword", "Email tồn tại, tạo mã code");
                            generateAndSaveResetCode(email, dbService);
                        } else {
                            // Email không tồn tại (vẫn hiển thị thành công để bảo mật)
                            progressDialog.dismiss();
                            Log.d("ForgotPassword", "Email không tồn tại");
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Nếu email tồn tại, mã xác nhận đã được gửi!",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("ForgotPassword", "Lỗi kiểm tra email: " + t.getMessage(), t);
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void generateAndSaveResetCode(String email, SupabaseDbService dbService) {
        // Tạo mã 6 số ngẫu nhiên
        String code = String.format(Locale.US, "%06d", new Random().nextInt(1000000));

        // Thời gian hết hạn: 10 phút từ bây giờ
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
        String expiresAt = sdf.format(calendar.getTime());

        PasswordResetCode resetCode = new PasswordResetCode(email, code, expiresAt);

        Log.d("ForgotPassword", "Tạo mã reset: " + code + " cho email: " + email);

        // Lưu vào database
        dbService.createResetCode(resetCode)
                .enqueue(new Callback<List<PasswordResetCode>>() {
                    @Override
                    public void onResponse(Call<List<PasswordResetCode>> call, Response<List<PasswordResetCode>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            Log.d("ForgotPassword", "Lưu mã code thành công vào DB, bắt đầu gửi email...");

                            // Gửi email thật
                            EmailSender.sendResetCode(email, code, new EmailSender.EmailCallback() {
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();
                                    Log.d("ForgotPassword", "Gửi email thành công!");

                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Mã xác nhận đã được gửi về email " + email,
                                            Toast.LENGTH_LONG).show();

                                    // Chuyển sang màn hình nhập code
                                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyResetCodeActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onError(String error) {
                                    progressDialog.dismiss();
                                    Log.e("ForgotPassword", "Lỗi gửi email: " + error);

                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Lỗi gửi email: " + error + "\nVui lòng kiểm tra cấu hình EmailSender.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Log.e("ForgotPassword", "Lỗi lưu mã code, HTTP " + response.code());
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Lỗi tạo mã xác nhận. Vui lòng thử lại!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PasswordResetCode>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("ForgotPassword", "Network error khi tạo mã: " + t.getMessage(), t);
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

