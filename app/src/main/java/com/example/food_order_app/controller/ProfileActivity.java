package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseAuthService;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.model.User;
import com.example.food_order_app.utils.SessionManager;
import com.example.food_order_app.utils.ValidationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình Hồ sơ cá nhân
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, btnChangeAvatar;
    private CircleImageView imgAvatar;
    private EditText edtEmail, edtFullName, edtPhone, edtAddress;
    private Button btnSaveProfile, btnChangePassword, btnLogout;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        edtEmail = findViewById(R.id.edtEmail);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    /**
     * Hiển thị thông tin user từ session
     */
    private void loadUserData() {
        edtEmail.setText(sessionManager.getEmail());
        edtFullName.setText(sessionManager.getFullName());
        edtPhone.setText(sessionManager.getPhone());
        edtAddress.setText(sessionManager.getAddress());

        // Load avatar
        String avatarUrl = sessionManager.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(imgAvatar);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangeAvatar.setOnClickListener(v -> {
            // TODO: Implement image picker cho avatar
            Toast.makeText(this, "Chức năng đổi ảnh đại diện sẽ được cập nhật!",
                    Toast.LENGTH_SHORT).show();
        });

        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());

        btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void handleSaveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        // Validation (cho phép rỗng nhưng nếu có giá trị thì phải hợp lệ)
        if (!ValidationUtils.isEmpty(phone) && !ValidationUtils.isValidPhone(phone)) {
            edtPhone.setError(getString(R.string.error_phone_invalid));
            edtPhone.requestFocus();
            return;
        }

        progressDialog.show();

        // Cập nhật trong bảng users
        SupabaseDbService dbService = RetrofitClient.getDbService();
        String bearerToken = sessionManager.getBearerToken();
        String userId = sessionManager.getUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", fullName);
        updates.put("phone", phone);
        updates.put("address", address);

        if (userId == null || userId.isEmpty()) {
            // Nếu chưa có userId, tìm bằng email
            updateByEmail(dbService, updates, fullName, phone, address);
            return;
        }

        dbService.updateUser(
                "eq." + userId, updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    // Cập nhật session
                    sessionManager.updateProfile(fullName, phone, address, null);
                    Toast.makeText(ProfileActivity.this,
                            getString(R.string.success_update_profile),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "Cập nhật thất bại. Vui lòng thử lại.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật user qua email khi chưa có userId
     */
    private void updateByEmail(SupabaseDbService dbService,
                               Map<String, Object> updates,
                               String fullName, String phone, String address) {
        String email = sessionManager.getEmail();
        dbService.getUserByEmail("eq." + email, "*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            User user = response.body().get(0);
                            String id = user.getId();

                            // Lưu userId vào session
                            sessionManager.saveUserInfo(
                                    id, sessionManager.getAuthId(),
                                    email, fullName, phone, address,
                                    user.getAvatarUrl(), user.getRole()
                            );

                            // Gọi lại update
                            dbService.updateUser(
                                    "eq." + id, updates).enqueue(new Callback<List<User>>() {
                                @Override
                                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                                    progressDialog.dismiss();
                                    if (response.isSuccessful()) {
                                        sessionManager.updateProfile(fullName, phone, address, null);
                                        Toast.makeText(ProfileActivity.this,
                                                getString(R.string.success_update_profile),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ProfileActivity.this,
                                                "Cập nhật thất bại.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<User>> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this,
                                            getString(R.string.error_network),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this,
                                    "Không tìm thấy thông tin tài khoản.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this,
                                getString(R.string.error_network),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout_confirm_title))
                .setMessage(getString(R.string.logout_confirm_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> handleLogout())
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void handleLogout() {
        progressDialog.show();

        // Gọi Supabase Auth signout
        SupabaseAuthService authService = RetrofitClient.getAuthService();
        String bearerToken = sessionManager.getBearerToken();

        if (bearerToken != null) {
            authService.signOut(bearerToken).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    performLogout();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Vẫn logout local kể cả khi API lỗi
                    performLogout();
                }
            });
        } else {
            performLogout();
        }
    }

    private void performLogout() {
        progressDialog.dismiss();
        sessionManager.logout();

        Toast.makeText(this, getString(R.string.success_logout), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
