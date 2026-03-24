package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.config.SupabaseConfig;
import com.example.food_order_app.model.Review;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.network.SupabaseStorageService;
import com.example.food_order_app.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.text.TextUtils;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends AppCompatActivity {
    private static final String TAG = "WriteReviewActivity";
    private static final String REVIEW_BUCKET = "reviews";

    private RatingBar rbWriteReview;
    private TextView tvRatingStatus;
    private LinearLayout layoutAddImageLarge, layoutImageSelected, btnAddMoreImage, containerImages;
    private EditText etReviewComment;
    private Button btnSubmitReview;

    private SupabaseDbService dbService;
    private SupabaseStorageService storageService;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private String foodId;
    
    // Image selection
    private ActivityResultLauncher<Intent> reviewImageLauncher;
    private List<Uri> selectedReviewImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        dbService = RetrofitClient.getDbService();
        storageService = RetrofitClient.getStorageService();
        sessionManager = new SessionManager(this);

        foodId = getIntent().getStringExtra("food_id");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        initViews();
        setupImagePicker();
        updateImageSelectionUI();
    }

    private void initViews() {
        rbWriteReview = findViewById(R.id.rbWriteReview);
        tvRatingStatus = findViewById(R.id.tvRatingStatus);
        
        layoutAddImageLarge = findViewById(R.id.layoutAddImageLarge);
        layoutImageSelected = findViewById(R.id.layoutImageSelected);
        btnAddMoreImage = findViewById(R.id.btnAddMoreImage);
        containerImages = findViewById(R.id.containerImages);
        
        etReviewComment = findViewById(R.id.etReviewComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        // Initial rating status
        tvRatingStatus.setText("Vui lòng chọn số sao");

        rbWriteReview.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingStatus((int) rating);
        });

        layoutAddImageLarge.setOnClickListener(v -> openImagePicker());
        btnAddMoreImage.setOnClickListener(v -> openImagePicker());

        btnSubmitReview.setOnClickListener(v -> submitReviewProcess());
    }

    private void updateRatingStatus(int rating) {
        switch (rating) {
            case 5: tvRatingStatus.setText("Rất hài lòng"); break;
            case 4: tvRatingStatus.setText("Hài lòng"); break;
            case 3: tvRatingStatus.setText("Bình thường"); break;
            case 2: tvRatingStatus.setText("Không hài lòng"); break;
            case 1: tvRatingStatus.setText("Rất tệ"); break;
            default: tvRatingStatus.setText("Vui lòng chọn số sao"); break;
        }
    }

    private void setupImagePicker() {
        reviewImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedReviewImages.add(result.getData().getData());
                        updateImageSelectionUI();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        reviewImageLauncher.launch(intent);
    }

    private void updateImageSelectionUI() {
        if (!selectedReviewImages.isEmpty()) {
            layoutAddImageLarge.setVisibility(View.GONE);
            layoutImageSelected.setVisibility(View.VISIBLE);
            containerImages.removeAllViews();
            for (int i = 0; i < selectedReviewImages.size(); i++) {
                Uri uri = selectedReviewImages.get(i);
                View view = getLayoutInflater().inflate(R.layout.item_review_image_picker, containerImages, false);
                ImageView img = view.findViewById(R.id.imgSelectedThumbnail);
                ImageView btnRemove = view.findViewById(R.id.btnRemoveImage);
                img.setImageURI(uri);
                final int index = i;
                btnRemove.setOnClickListener(v -> {
                    selectedReviewImages.remove(index);
                    updateImageSelectionUI();
                });
                containerImages.addView(view);
            }
        } else {
            layoutAddImageLarge.setVisibility(View.VISIBLE);
            layoutImageSelected.setVisibility(View.GONE);
        }
    }

    private void submitReviewProcess() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = (int) rbWriteReview.getRating();
        String comment = etReviewComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedReviewImages.isEmpty()) {
            progressDialog.setMessage("Đang tải ảnh lên (0/" + selectedReviewImages.size() + ")...");
            progressDialog.show();
            uploadNextImage(0, rating, comment, new ArrayList<>());
        } else {
            submitReviewToDb(rating, comment, null);
        }
    }

    private void uploadNextImage(int index, int rating, String comment, List<String> uploadedUrls) {
        if (index >= selectedReviewImages.size()) {
            String combinedUrls = TextUtils.join(",", uploadedUrls);
            submitReviewToDb(rating, comment, combinedUrls);
            return;
        }

        progressDialog.setMessage("Đang tải ảnh lên (" + (index + 1) + "/" + selectedReviewImages.size() + ")...");
        Uri uri = selectedReviewImages.get(index);

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                // Skip this image on error
                uploadNextImage(index + 1, rating, comment, uploadedUrls);
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                uploadNextImage(index + 1, rating, comment, uploadedUrls);
                return;
            }

            int maxSize = 800;
            int w = bitmap.getWidth(), h = bitmap.getHeight();
            if (w > maxSize || h > maxSize) {
                float scale = Math.min((float) maxSize / w, (float) maxSize / h);
                w = Math.round(w * scale);
                h = Math.round(h * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();

            String fileName = "review_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

            storageService.uploadFile(REVIEW_BUCKET, fileName, "image/jpeg", "true", requestBody)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                String imageUrl = SupabaseConfig.STORAGE_URL
                                        + "object/public/" + REVIEW_BUCKET + "/" + fileName;
                                uploadedUrls.add(imageUrl);
                            } else {
                                Log.e(TAG, "Review image upload failed: " + response.code());
                            }
                            // Proceed to next regardless
                            uploadNextImage(index + 1, rating, comment, uploadedUrls);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "Review image upload error: " + t.getMessage());
                            uploadNextImage(index + 1, rating, comment, uploadedUrls);
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Image processing error: " + e.getMessage());
            uploadNextImage(index + 1, rating, comment, uploadedUrls);
        }
    }

    private void submitReviewToDb(int rating, String comment, String imageUrl) {
        progressDialog.setMessage("Đang gửi đánh giá...");
        if(!progressDialog.isShowing()) progressDialog.show();

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("food_id", foodId);
        reviewData.put("user_id", sessionManager.getUserId());
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            reviewData.put("image_url", imageUrl);
        }

        dbService.createReview(reviewData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Review submitted! Now fetch all reviews to update the average rating on food table
                    updateFoodAverageRating();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(WriteReviewActivity.this, "Lỗi gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "submitReviewToDb failed: " + t.getMessage());
                Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFoodAverageRating() {
        progressDialog.setMessage("Đang cập nhật đánh giá trung bình...");
        dbService.getReviews("eq." + foodId, "rating", "").enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();
                    int total = reviews.size();
                    double sum = 0;
                    for(Review r : reviews) {
                        sum += r.getRating();
                    }
                    double avg = total > 0 ? (sum / total) : 0;
                    
                    // Round to 1 decimal place safely
                    avg = Math.round(avg * 10.0) / 10.0;

                    Map<String, Object> updateFood = new HashMap<>();
                    updateFood.put("avg_rating", avg);
                    updateFood.put("total_reviews", total);

                    dbService.updateFood("eq." + foodId, updateFood).enqueue(new Callback<List<com.example.food_order_app.model.Food>>() {
                        @Override
                        public void onResponse(Call<List<com.example.food_order_app.model.Food>> call, Response<List<com.example.food_order_app.model.Food>> response) {
                            progressDialog.dismiss();
                            Toast.makeText(WriteReviewActivity.this, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onFailure(Call<List<com.example.food_order_app.model.Food>> call, Throwable t) {
                            // Even if updating food fails, the review is already created
                            progressDialog.dismiss();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    setResult(RESULT_OK);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                progressDialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
