package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.AuthRequest;
import com.example.food_order_app.model.AuthResponse;
import com.example.food_order_app.model.ChangePasswordRequest;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseAuthService;
import com.example.food_order_app.utils.SessionManager;
import com.example.food_order_app.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình Đổi mật khẩu
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

        // Validation
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

        // Bước 1: Xác thực mật khẩu cũ bằng cách đăng nhập lại
        SupabaseAuthService authService = RetrofitClient.getAuthService();
        String email = sessionManager.getEmail();

        AuthRequest verifyRequest = new AuthRequest(email, currentPassword);
        authService.signIn(verifyRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    // Mật khẩu cũ đúng, tiến hành đổi mật khẩu
                    String newToken = response.body().getAccessToken();
                    updatePassword(newToken, newPassword);
                } else {
                    progressDialog.dismiss();
                    edtCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                    edtCurrentPassword.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ChangePasswordActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật mật khẩu mới qua Supabase Auth
     */
    private void updatePassword(String accessToken, String newPassword) {
        SupabaseAuthService authService = RetrofitClient.getAuthService();
        String bearerToken = "Bearer " + accessToken;

        ChangePasswordRequest request = new ChangePasswordRequest(newPassword);

        authService.changePassword(bearerToken, request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    // Cập nhật token mới trong session
                    sessionManager.saveAuthToken(accessToken, sessionManager.getRefreshToken());

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
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ChangePasswordActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
