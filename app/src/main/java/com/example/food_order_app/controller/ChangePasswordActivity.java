package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.PasswordUtils;
import com.example.food_order_app.utils.SessionManager;
import com.example.food_order_app.utils.ValidationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình Đổi mật khẩu (Custom Auth - SHA-256)
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtCurrentPassword, edtNewPassword, edtConfirmNewPassword;
    private Button btnChangePassword;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sessionManager = new SessionManager(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        if (ValidationUtils.isEmpty(currentPassword)) {
            edtCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            edtCurrentPassword.requestFocus();
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
        if (!newPassword.equals(confirmNewPassword)) {
            edtConfirmNewPassword.setError(getString(R.string.error_password_mismatch));
            edtConfirmNewPassword.requestFocus();
            return;
        }
        if (currentPassword.equals(newPassword)) {
            edtNewPassword.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            edtNewPassword.requestFocus();
            return;
        }

        progressDialog.show();

        // Bước 1: Lấy user từ bảng users để verify mật khẩu cũ (Custom Auth)
        String email = sessionManager.getEmail();
        SupabaseDbService dbService = RetrofitClient.getDbService();

        dbService.getUserByEmail("eq." + email, "*").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    User user = response.body().get(0);

                    // Bước 2: Verify mật khẩu cũ bằng SHA-256
                    if (!PasswordUtils.verifyPassword(currentPassword, user.getPassword())) {
                        progressDialog.dismiss();
                        edtCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                        edtCurrentPassword.requestFocus();
                        return;
                    }

                    // Bước 3: Hash mật khẩu mới và cập nhật vào bảng users
                    String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("password", hashedNewPassword);

                    dbService.updatePasswordByEmail("eq." + email, updates)
                            .enqueue(new Callback<List<User>>() {
                                @Override
                                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                                    progressDialog.dismiss();
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this,
                                                getString(R.string.success_change_password),
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this,
                                                "Đổi mật khẩu thất bại. Vui lòng thử lại.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<User>> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChangePasswordActivity.this,
                                            getString(R.string.error_network),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ChangePasswordActivity.this,
                            "Không tìm thấy tài khoản. Vui lòng đăng nhập lại.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ChangePasswordActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
