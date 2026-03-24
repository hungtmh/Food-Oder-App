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
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.ReviewAdapter;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodReviewsActivity extends AppCompatActivity {
    private static final String TAG = "FoodReviewsActivity";

    private static final String REVIEW_BUCKET = "reviews";

    private TextView btnBack, tvToolbarTitle, tvAvgRating, tvTotalReviews, tvNoReviews, tvTotalReviewsLabel;
    private RatingBar rbAvgRating;
    private RecyclerView rvReviews;

    private ProgressBar[] pbStars = new ProgressBar[5];
    private TextView[] tvCounts = new TextView[5];
    private TextView btnWriteReview, tvFilterStar;

    private SupabaseDbService dbService;
    private SupabaseStorageService storageService;
    private SessionManager sessionManager;
    private ReviewAdapter reviewAdapter;
    private ProgressDialog progressDialog;

    private List<Review> allReviews = new ArrayList<>();
    private String foodId;
    private String foodName;
    private int currentFilter = 0; // 0 = all

    // WriteReviewLauncher to refresh reviews
    private ActivityResultLauncher<Intent> writeReviewLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_reviews);

        dbService = RetrofitClient.getDbService();
        storageService = RetrofitClient.getStorageService();
        sessionManager = new SessionManager(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        foodId = getIntent().getStringExtra("food_id");
        foodName = getIntent().getStringExtra("food_name");

        if (foodId == null) {
            finish();
            return;
        }

        initViews();
        setupWriteReviewLauncher();
        loadAllReviews();
    }

    private void setupWriteReviewLauncher() {
        writeReviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAllReviews();
                    }
                }
        );
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        rbAvgRating = findViewById(R.id.rbAvgRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        rvReviews = findViewById(R.id.rvReviews);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        tvTotalReviewsLabel = findViewById(R.id.tvTotalReviewsLabel);
        tvFilterStar = findViewById(R.id.tvFilterStar);

        pbStars[4] = findViewById(R.id.pbStar5);
        pbStars[3] = findViewById(R.id.pbStar4);
        pbStars[2] = findViewById(R.id.pbStar3);
        pbStars[1] = findViewById(R.id.pbStar2);
        pbStars[0] = findViewById(R.id.pbStar1);

        tvCounts[4] = findViewById(R.id.tvCount5);
        tvCounts[3] = findViewById(R.id.tvCount4);
        tvCounts[2] = findViewById(R.id.tvCount3);
        tvCounts[1] = findViewById(R.id.tvCount2);
        tvCounts[0] = findViewById(R.id.tvCount1);

        if (foodName != null && !foodName.isEmpty()) {
            tvToolbarTitle.setText("Đánh giá - " + foodName);
        }

        reviewAdapter = new ReviewAdapter(this);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnWriteReview.setOnClickListener(v -> openWriteReviewActivity());
        tvFilterStar.setOnClickListener(v -> showFilterDialog());
        tvFilterStar.setOnClickListener(v -> showFilterDialog());
    }

    private void loadAllReviews() {
        dbService.getReviews("eq." + foodId, "*,users(full_name,avatar_url)", "created_at.desc")
                .enqueue(new Callback<List<Review>>() {
                    @Override
                    public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            allReviews = response.body();
                            updateRatingSummary();
                            applyFilter(currentFilter);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Review>> call, Throwable t) {
                        Log.e(TAG, "loadAllReviews failed: " + t.getMessage());
                    }
                });
    }

    private void updateRatingSummary() {
        int total = allReviews.size();
        tvTotalReviews.setText(total + " đánh giá");
        tvTotalReviewsLabel.setText(total + " Nhận xét");

        if (total == 0) {
            tvAvgRating.setText("0.0");
            rbAvgRating.setRating(0);
            for (int i = 0; i < 5; i++) {
                pbStars[i].setProgress(0);
                tvCounts[i].setText("0");
            }
            return;
        }

        double sum = 0;
        int[] counts = new int[6]; // index 0=unused, 1-5 = star counts

        for (Review r : allReviews) {
            sum += r.getRating();
            int star = r.getRating();
            if (star >= 1 && star <= 5) counts[star]++;
        }

        double avg = sum / total;
        tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
        rbAvgRating.setRating((float) avg);

        // Update progress bars (index 4 is 5 stars, index 0 is 1 star)
        for (int i = 0; i < 5; i++) {
            int starValue = i + 1;
            int countForStar = counts[starValue];
            tvCounts[i].setText(String.valueOf(countForStar));
            int percentage = (int) ((countForStar * 100.0f) / total);
            pbStars[i].setProgress(percentage);
        }
    }

    private void showFilterDialog() {
        String[] options = {"Tất cả (" + allReviews.size() + ")", "5 Sao", "4 Sao", "3 Sao", "2 Sao", "1 Sao"};
        int selectedOption = (currentFilter == 0) ? 0 : (6 - currentFilter); // map: 5 star -> idx 1, 4 star -> idx 2

        new AlertDialog.Builder(this)
                .setTitle("Lọc đánh giá")
                .setSingleChoiceItems(options, selectedOption, (dialog, which) -> {
                    if (which == 0) {
                        applyFilter(0);
                    } else {
                        applyFilter(6 - which); // map back 1 -> 5, 2 -> 4 etc.
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void applyFilter(int starFilter) {
        currentFilter = starFilter;

        String filterText = (starFilter == 0) ? "Lọc theo sao ↓" : starFilter + " Sao ↓";
        tvFilterStar.setText(filterText);

        // Filter reviews
        List<Review> filtered;
        if (starFilter == 0) {
            filtered = allReviews;
        } else {
            filtered = new ArrayList<>();
            for (Review r : allReviews) {
                if (r.getRating() == starFilter) {
                    filtered.add(r);
                }
            }
        }

        if (filtered.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
        } else {
            tvNoReviews.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
            reviewAdapter.setReviews(filtered);
        }
    }

    // ============ END FILTER AND VIEW ============

    private void openWriteReviewActivity() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, WriteReviewActivity.class);
        intent.putExtra("food_id", foodId);
        writeReviewLauncher.launch(intent);
    }
}
