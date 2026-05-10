package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Feedback;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitFeedbackActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RatingBar ratingFeedback;
    private EditText edtSubject;
    private EditText edtContent;
    private Button btnSubmitFeedback;
    
    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_feedback);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ratingFeedback = findViewById(R.id.ratingFeedback);
        edtSubject = findViewById(R.id.edtSubject);
        edtContent = findViewById(R.id.edtContent);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi phản hồi...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = (int) ratingFeedback.getRating();
        if (rating == 0) {
            Toast.makeText(this, getString(R.string.feedback_error_rating), Toast.LENGTH_SHORT).show();
            return;
        }

        String content = edtContent.getText().toString().trim();
        if (content.isEmpty()) {
            edtContent.setError(getString(R.string.feedback_error_content));
            edtContent.requestFocus();
            return;
        }

        String subject = edtSubject.getText().toString().trim();
        String userId = sessionManager.getUserId();

        progressDialog.show();

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("user_id", userId);
        feedbackData.put("subject", subject.isEmpty() ? "Góp ý dịch vụ" : subject);
        feedbackData.put("content", content);
        feedbackData.put("rating", rating);
        feedbackData.put("is_read", false);

        dbService.createFeedback(feedbackData).enqueue(new Callback<List<Feedback>>() {
            @Override
            public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(SubmitFeedbackActivity.this, getString(R.string.feedback_success), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(SubmitFeedbackActivity.this, "Có lỗi xảy ra, vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Feedback>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(SubmitFeedbackActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
