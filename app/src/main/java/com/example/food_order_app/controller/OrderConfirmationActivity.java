package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        String orderCode = getIntent().getStringExtra("order_code");
        double totalAmount = getIntent().getDoubleExtra("total_amount", 0);

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

        TextView tvOrderCode = findViewById(R.id.tvOrderCode);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        tvOrderCode.setText("Mã đơn: " + orderCode);
        tvTotalAmount.setText(nf.format(totalAmount) + " VNĐ");

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
