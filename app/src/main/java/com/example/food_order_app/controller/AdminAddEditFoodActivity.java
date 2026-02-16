package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Category;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddEditFoodActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView imgPreview;
    private EditText edtImageUrl, edtName, edtPrice, edtDiscount, edtDescription;
    private Spinner spinnerCategory;
    private CheckBox cbPopular, cbRecommended, cbAvailable;
    private Button btnSave;

    private SupabaseDbService dbService;
    private List<Category> categories = new ArrayList<>();
    private String foodId = null; // null = add mode
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_food);

        dbService = RetrofitClient.getDbService();
        foodId = getIntent().getStringExtra("food_id");

        initViews();
        loadCategories();

        edtImageUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = edtImageUrl.getText().toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(this).load(url).placeholder(R.drawable.bg_search_bar).into(imgPreview);
                }
            }
        });

        btnSave.setOnClickListener(v -> saveFood());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvAddEditTitle);
        imgPreview = findViewById(R.id.imgFoodPreview);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        edtName = findViewById(R.id.edtFoodName);
        edtPrice = findViewById(R.id.edtFoodPrice);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtDescription = findViewById(R.id.edtFoodDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbPopular = findViewById(R.id.cbPopular);
        cbRecommended = findViewById(R.id.cbRecommended);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnSave = findViewById(R.id.btnSaveFood);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        if (foodId != null) {
            tvTitle.setText("Chỉnh sửa món ăn");
            btnSave.setText("Cập nhật");
        }
    }

    private void loadCategories() {
        dbService.getCategories("eq.true", "sort_order").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    // Filter out "Tất cả"
                    List<String> names = new ArrayList<>();
                    List<Category> filtered = new ArrayList<>();
                    for (Category c : categories) {
                        if (!"Tất cả".equals(c.getName())) {
                            names.add(c.getName());
                            filtered.add(c);
                        }
                    }
                    categories = filtered;
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AdminAddEditFoodActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            names);
                    spinnerCategory.setAdapter(adapter);

                    // Load food data if editing
                    if (foodId != null) {
                        loadFoodData();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AdminAddEditFoodActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodData() {
        dbService.getFoodById("eq." + foodId).enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Food food = response.body().get(0);
                    edtName.setText(food.getName());
                    edtPrice.setText(String.valueOf((long) food.getPrice()));
                    edtDiscount.setText(String.valueOf(food.getDiscountPercent()));
                    edtDescription.setText(food.getDescription());
                    edtImageUrl.setText(food.getImageUrl());
                    cbPopular.setChecked(food.isPopular());
                    cbRecommended.setChecked(food.isRecommended());
                    cbAvailable.setChecked(food.isAvailable());

                    if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                        Glide.with(AdminAddEditFoodActivity.this)
                                .load(food.getImageUrl())
                                .into(imgPreview);
                    }

                    // Set spinner selection
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId().equals(food.getCategoryId())) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                Toast.makeText(AdminAddEditFoodActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFood() {
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String discountStr = edtDiscount.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên món");
            edtName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            edtPrice.setError("Vui lòng nhập giá");
            edtPrice.requestFocus();
            return;
        }

        if (categories.isEmpty()) {
            Toast.makeText(this, "Chưa có danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        double price = Double.parseDouble(priceStr);
        int discount = discountStr.isEmpty() ? 0 : Integer.parseInt(discountStr);
        String categoryId = categories.get(spinnerCategory.getSelectedItemPosition()).getId();

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("price", price);
        data.put("discount_percent", discount);
        data.put("description", description);
        data.put("image_url", imageUrl);
        data.put("category_id", categoryId);
        data.put("is_popular", cbPopular.isChecked());
        data.put("is_recommended", cbRecommended.isChecked());
        data.put("is_available", cbAvailable.isChecked());

        if (foodId != null) {
            // Update
            dbService.updateFood("eq." + foodId, data).enqueue(new Callback<List<Food>>() {
                @Override
                public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminAddEditFoodActivity.this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminAddEditFoodActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Food>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AdminAddEditFoodActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create
            dbService.createFood(data).enqueue(new Callback<List<Food>>() {
                @Override
                public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminAddEditFoodActivity.this, "Đã thêm món mới!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminAddEditFoodActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Food>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AdminAddEditFoodActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
