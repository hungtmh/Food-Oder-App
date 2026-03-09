package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseAuthService;
import com.example.food_order_app.network.SupabaseDbService;
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
 * Controller: Màn hình Hồ sơ cá nhân với sections thu gọn
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    // Header
    private ImageView btnBack, btnChangeAvatar;
    private CircleImageView imgAvatar;
    private TextView tvProfileName;

    // Personal info section
    private LinearLayout sectionPersonalHeader, sectionPersonalContent;
    private ImageView ivExpandPersonal;
    private EditText edtEmail, edtFullName, edtPhone;
    private Button btnSaveProfile;
    private boolean isPersonalExpanded = false;

    // Orders section (navigates to OrderHistoryActivity)
    private LinearLayout sectionOrdersHeader;
    // Address section
    private LinearLayout sectionAddressHeader;

    // Favorites section (navigates to FavoritesActivity)
    private LinearLayout sectionFavoritesHeader;

    // Action buttons
    private LinearLayout btnChangePassword, btnLogout;

    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private SupabaseDbService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        dbService = RetrofitClient.getDbService();

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);

        // Personal info section
        sectionPersonalHeader = findViewById(R.id.sectionPersonalHeader);
        sectionPersonalContent = findViewById(R.id.sectionPersonalContent);
        ivExpandPersonal = findViewById(R.id.ivExpandPersonal);
        edtEmail = findViewById(R.id.edtEmail);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Orders section
        sectionOrdersHeader = findViewById(R.id.sectionOrdersHeader);
        // Address section
        sectionAddressHeader = findViewById(R.id.sectionAddressHeader);

        // Favorites section
        sectionFavoritesHeader = findViewById(R.id.sectionFavoritesHeader);

        // Action buttons
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);

        // Set initial expand state
        updateSectionVisibility(sectionPersonalContent, ivExpandPersonal, isPersonalExpanded);
    }

    private void loadUserData() {
        String fullName = sessionManager.getFullName();
        tvProfileName.setText(fullName != null && !fullName.isEmpty() ? fullName : "Người dùng");

        edtEmail.setText(sessionManager.getEmail());
        edtFullName.setText(fullName);
        edtPhone.setText(sessionManager.getPhone());

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
            Toast.makeText(this, "Chức năng đổi ảnh đại diện sẽ được cập nhật!",
                    Toast.LENGTH_SHORT).show();
        });

        // Collapsible: Personal info
        sectionPersonalHeader.setOnClickListener(v -> {
            isPersonalExpanded = !isPersonalExpanded;
            toggleSection(sectionPersonalContent, ivExpandPersonal, isPersonalExpanded);
        });

        // Orders: navigate to OrderHistoryActivity
        sectionOrdersHeader.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrderHistoryActivity.class));
        });

        // Address: navigate to AddressActivity
        sectionAddressHeader.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AddressActivity.class));
        });

        // Favorites: navigate to FavoritesActivity
        sectionFavoritesHeader.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class));
        });

        // Save profile
        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());

        // Change password
        btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // ============ SECTION TOGGLE ANIMATION ============

    private void toggleSection(View content, ImageView arrow, boolean expanded) {
        if (expanded) {
            content.setVisibility(View.VISIBLE);
            content.setAlpha(0f);
            content.animate().alpha(1f).setDuration(200).start();
        } else {
            content.animate().alpha(0f).setDuration(200).withEndAction(() ->
                    content.setVisibility(View.GONE)
            ).start();
        }

        float fromDeg = expanded ? 0f : 180f;
        float toDeg = expanded ? 180f : 0f;
        RotateAnimation rotate = new RotateAnimation(fromDeg, toDeg,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(200);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }

    private void updateSectionVisibility(View content, ImageView arrow, boolean expanded) {
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        arrow.setRotation(expanded ? 180f : 0f);
    }

    // ============ SAVE PROFILE ============

    private void handleSaveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (!ValidationUtils.isEmpty(phone) && !ValidationUtils.isValidPhone(phone)) {
            edtPhone.setError(getString(R.string.error_phone_invalid));
            edtPhone.requestFocus();
            return;
        }

        progressDialog.show();

        String userId = sessionManager.getUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", fullName);
        updates.put("phone", phone);

        if (userId == null || userId.isEmpty()) {
            updateByEmail(updates, fullName, phone);
            return;
        }

        dbService.updateUser("eq." + userId, updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    sessionManager.updateProfile(fullName, phone, sessionManager.getAddress(), null);
                    tvProfileName.setText(fullName.isEmpty() ? "Người dùng" : fullName);
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

    private void updateByEmail(Map<String, Object> updates,
                               String fullName, String phone) {
        String email = sessionManager.getEmail();
        dbService.getUserByEmail("eq." + email, "*")
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            User user = response.body().get(0);
                            String id = user.getId();

                            sessionManager.saveUserInfo(
                                    id, sessionManager.getAuthId(),
                                    email, fullName, phone, sessionManager.getAddress(),
                                    user.getAvatarUrl(), user.getRole()
                            );

                            dbService.updateUser("eq." + id, updates).enqueue(new Callback<List<User>>() {
                                @Override
                                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                                    progressDialog.dismiss();
                                    if (response.isSuccessful()) {
                                        sessionManager.updateProfile(fullName, phone, sessionManager.getAddress(), null);
                                        tvProfileName.setText(fullName.isEmpty() ? "Người dùng" : fullName);
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

    // ============ LOGOUT ============

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
