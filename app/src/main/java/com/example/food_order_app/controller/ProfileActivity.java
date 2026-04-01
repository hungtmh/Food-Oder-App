package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.config.SupabaseConfig;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseAuthService;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.network.SupabaseStorageService;
import com.example.food_order_app.utils.SessionManager;
import com.example.food_order_app.utils.ValidationUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Controller: Màn hình Hồ sơ cá nhân với sections thu gọn
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final String STORAGE_BUCKET = "avatars";

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

    // Favorites section (navigates to FavoritesActivity)
    private LinearLayout sectionFavoritesHeader;

    // Action buttons
    private LinearLayout btnChangePassword, btnLogout;

    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private SupabaseDbService dbService;
    private SupabaseStorageService storageService;

    // Image picker launcher
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        dbService = RetrofitClient.getDbService();
        storageService = RetrofitClient.getStorageService();

        initViews();
        loadUserData();
        setupListeners();
        setupImagePicker();
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

        // Admin checks
        if (sessionManager.isAdmin()) {
            sectionOrdersHeader.setVisibility(View.GONE);
            sectionFavoritesHeader.setVisibility(View.GONE);
        }
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

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadAvatarToStorage(imageUri);
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
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

    // ============ AVATAR UPLOAD ============

    private void uploadAvatarToStorage(Uri imageUri) {
        progressDialog.setMessage("Đang tải ảnh lên...");
        progressDialog.show();

        try {
            // Read and compress image
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Resize if too large (max 800px)
            int maxSize = 800;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float scale = Math.min((float) maxSize / width, (float) maxSize / height);
                width = Math.round(width * scale);
                height = Math.round(height * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            // Compress to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();

            // Generate unique file path
            String userId = sessionManager.getUserId();
            String fileName = (userId != null ? userId : UUID.randomUUID().toString()) + ".jpg";

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("image/jpeg"), imageBytes);

            // Upload to Supabase Storage
            storageService.uploadFile(STORAGE_BUCKET, fileName, "image/jpeg", "true", requestBody)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                // Build public URL
                                String publicUrl = SupabaseConfig.STORAGE_URL
                                        + "object/public/" + STORAGE_BUCKET + "/" + fileName;
                                saveAvatarUrl(publicUrl);
                            } else {
                                progressDialog.dismiss();
                                String errorMsg = "Upload thất bại";
                                try {
                                    if (response.errorBody() != null) {
                                        errorMsg += ": " + response.errorBody().string();
                                    }
                                } catch (Exception e) {
                                    // ignore
                                }
                                Log.e(TAG, "Upload failed: " + response.code() + " " + errorMsg);
                                Toast.makeText(ProfileActivity.this, "Upload ảnh thất bại. Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            progressDialog.dismiss();
                            Log.e(TAG, "Upload error: " + t.getMessage());
                            Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Image processing error: " + e.getMessage());
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAvatarUrl(String newUrl) {
        String userId = sessionManager.getUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("avatar_url", newUrl);

        if (userId == null || userId.isEmpty()) {
            saveAvatarUrlByEmail(updates, newUrl);
            return;
        }

        dbService.updateUser("eq." + userId, updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    sessionManager.updateProfile(sessionManager.getFullName(),
                            sessionManager.getPhone(), sessionManager.getAddress(), newUrl);
                    loadUserData();
                    Toast.makeText(ProfileActivity.this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAvatarUrlByEmail(Map<String, Object> updates, String newUrl) {
        String email = sessionManager.getEmail();
        dbService.getUserByEmail("eq." + email, "*").enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    dbService.updateUser("eq." + user.getId(), updates).enqueue(new Callback<List<User>>() {
                        @Override
                        public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                            progressDialog.dismiss();
                            if (response.isSuccessful()) {
                                sessionManager.updateProfile(sessionManager.getFullName(),
                                        sessionManager.getPhone(), sessionManager.getAddress(), newUrl);
                                loadUserData();
                                Toast.makeText(ProfileActivity.this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<User>> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
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
                    sessionManager.updateProfile(fullName, phone, sessionManager.getAddress(), sessionManager.getAvatarUrl());
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
                                        sessionManager.updateProfile(fullName, phone, sessionManager.getAddress(), sessionManager.getAvatarUrl());
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
