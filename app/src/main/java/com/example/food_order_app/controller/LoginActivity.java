package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.PasswordUtils;
import com.example.food_order_app.utils.SessionManager;
import com.example.food_order_app.utils.ValidationUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình Đăng nhập (CUSTOM AUTH - không dùng Supabase Auth)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private RadioGroup radioGroupRole;
    private RadioButton radioAdmin, radioUser;
    private Button btnLogin;
    private TextView tvForgotPassword, tvGoToRegister;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Kiểm tra nếu đã đăng nhập và ghi nhớ
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioUser = findViewById(R.id.radioUser);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

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

        if (ValidationUtils.isEmpty(password)) {
            edtPassword.setError(getString(R.string.error_password_required));
            edtPassword.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            edtPassword.setError(getString(R.string.error_password_short));
            edtPassword.requestFocus();
            return;
        }

        // Lấy vai trò đã chọn
        String selectedRole = radioAdmin.isChecked() ? "admin" : "user";

        progressDialog.show();

        // Tìm user trong bảng users theo email
        SupabaseDbService dbService = RetrofitClient.getDbService();

        dbService.getUserByEmail("eq." + email, "*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        progressDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            User user = response.body().get(0);

                            // Verify password
                            if (!PasswordUtils.verifyPassword(password, user.getPassword())) {
                                Toast.makeText(LoginActivity.this,
                                        "Email hoặc mật khẩu không đúng!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Kiểm tra vai trò
                            if (!selectedRole.equalsIgnoreCase(user.getRole())) {
                                Toast.makeText(LoginActivity.this,
                                        "Vai trò không phù hợp với tài khoản này!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Đăng nhập thành công
                            sessionManager.saveAuthToken("custom_" + user.getId(), "");
                            sessionManager.setRememberMe(true);

                            // Lưu thông tin user
                            sessionManager.saveUserInfo(
                                    user.getId(),
                                    user.getId(), // authId = userId trong custom auth
                                    user.getEmail(),
                                    user.getFullName(),
                                    user.getPhone(),
                                    user.getAddress(),
                                    user.getAvatarUrl(),
                                    user.getRole()
                            );

                            Toast.makeText(LoginActivity.this,
                                    "Đăng nhập thành công!",
                                    Toast.LENGTH_SHORT).show();

                            navigateToMain();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.error_login_failed) + "\nTài khoản không tồn tại.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Chuyển đến trang chính dựa trên vai trò
     */
    private void navigateToMain() {
        Intent intent;
        if (sessionManager.isAdmin()) {
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
