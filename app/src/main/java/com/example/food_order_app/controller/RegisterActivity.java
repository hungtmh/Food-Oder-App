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
import com.example.food_order_app.utils.ValidationUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

/**
 * Controller: Màn hình Đăng ký tài khoản (CUSTOM AUTH - không dùng Supabase Auth)
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private RadioGroup radioGroupRole;
    private RadioButton radioAdmin, radioUser;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioUser = findViewById(R.id.radioUser);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
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

        String selectedRole = radioAdmin.isChecked() ? "admin" : "user";

        progressDialog.show();

        // Hash password
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Tạo user object
        User newUser = new User(email, hashedPassword, "", "", selectedRole);

        SupabaseDbService dbService = RetrofitClient.getDbService();

        // Bước 1: Kiểm tra email đã tồn tại chưa
        Log.d("Register", "Bước 1: Kiểm tra email tồn tại: " + email);
        dbService.getUserByEmail("eq." + email, "*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        Log.d("Register", "Check email response code: " + response.code());
                        Log.d("Register", "Check email body: " + response.body());

                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            // Email ĐÃ tồn tại
                            progressDialog.dismiss();
                            Log.d("Register", "Email đã tồn tại, số user tìm thấy: " + response.body().size());
                            Toast.makeText(RegisterActivity.this,
                                    "Email đã được đăng ký. Vui lòng sử dụng email khác.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Email chưa tồn tại -> Bước 2: Tạo user mới
                        Log.d("Register", "Email chưa tồn tại, tiến hành tạo user mới");
                        createNewUser(dbService, newUser, email);
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("Register", "Lỗi kiểm tra email: " + t.getMessage(), t);
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createNewUser(SupabaseDbService dbService, User newUser, String email) {
        Log.d("Register", "Bước 2: Gọi POST tạo user mới cho email: " + email);

        dbService.createUser(newUser)
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        progressDialog.dismiss();

                        Log.d("Register", "Create user response code: " + response.code());
                        Log.d("Register", "Create user isSuccessful: " + response.isSuccessful());

                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            // Đăng ký thành công
                            Log.d("Register", "Đăng ký thành công! User ID: " + response.body().get(0).getId());
                            Toast.makeText(RegisterActivity.this,
                                    getString(R.string.success_register),
                                    Toast.LENGTH_LONG).show();

                            // Chuyển sang màn hình đăng nhập
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("registered_email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            // Xử lý lỗi chi tiết
                            String errorMsg = getString(R.string.error_register_failed);
                            String errorDetail = "";

                            try {
                                if (response.errorBody() != null) {
                                    errorDetail = response.errorBody().string();
                                    Log.e("Register", "Error body: " + errorDetail);
                                    Log.e("Register", "Error code: " + response.code());

                                    if (errorDetail.contains("duplicate") || errorDetail.contains("unique")
                                            || response.code() == 409) {
                                        errorMsg = "Email đã được đăng ký. Vui lòng sử dụng email khác.";
                                    } else if (errorDetail.contains("password") || errorDetail.contains("column")) {
                                        errorMsg = "Lỗi cấu trúc bảng database. Vui lòng kiểm tra bảng users có cột 'password' chưa.";
                                    } else {
                                        errorMsg = "Lỗi đăng ký (HTTP " + response.code() + "): " + errorDetail;
                                    }
                                } else {
                                    Log.e("Register", "Response body null hoặc rỗng. Code: " + response.code());
                                    errorMsg = "Lỗi đăng ký: Server trả về dữ liệu rỗng (HTTP " + response.code() + ")";
                                }
                            } catch (Exception e) {
                                Log.e("Register", "Exception khi đọc error: " + e.getMessage(), e);
                            }

                            Toast.makeText(RegisterActivity.this, errorMsg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        progressDialog.dismiss();
                        Log.e("Register", "Network error khi tạo user: " + t.getMessage(), t);
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.error_network) + "\n" + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
