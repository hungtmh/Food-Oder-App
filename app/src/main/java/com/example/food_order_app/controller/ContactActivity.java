package com.example.food_order_app.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;

public class ContactActivity extends AppCompatActivity {

    // Restaurant contact info
    private static final String PHONE_NUMBER = "0123456789";
    private static final String EMAIL = "foodorderapp@gmail.com";
    private static final String FACEBOOK_URL = "https://www.facebook.com/foodorderapp";
    private static final String ZALO_PHONE = "0123456789";
    private static final String SKYPE_ID = "foodorderapp.skype";
    private static final String YOUTUBE_URL = "https://www.youtube.com/@foodorderapp";

    // Restaurant location
    private static final double LATITUDE = 10.8225;
    private static final double LONGITUDE = 106.6877;
    private static final String ADDRESS = "12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        setupListeners();
    }

    private void setupListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Phone
        LinearLayout btnPhone = findViewById(R.id.btnPhone);
        btnPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
            startActivity(intent);
        });

        // Email
        LinearLayout btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ từ Food Order App");
            try {
                startActivity(Intent.createChooser(intent, "Gửi email qua..."));
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
            }
        });

        // Facebook
        LinearLayout btnFacebook = findViewById(R.id.btnFacebook);
        btnFacebook.setOnClickListener(v -> {
            try {
                // Try to open Facebook app first
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + FACEBOOK_URL));
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
                startActivity(intent);
            }
        });

        // Zalo
        LinearLayout btnZalo = findViewById(R.id.btnZalo);
        btnZalo.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me/" + ZALO_PHONE));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở Zalo", Toast.LENGTH_SHORT).show();
            }
        });

        // Skype
        LinearLayout btnSkype = findViewById(R.id.btnSkype);
        btnSkype.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("skype:" + SKYPE_ID + "?chat"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng Skype", Toast.LENGTH_SHORT).show();
            }
        });

        // YouTube
        LinearLayout btnYoutube = findViewById(R.id.btnYoutube);
        btnYoutube.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_URL));
            startActivity(intent);
        });

        // View Map
        Button btnViewMap = findViewById(R.id.btnViewMap);
        btnViewMap.setOnClickListener(v -> {
            Uri gmmUri = Uri.parse("geo:" + LATITUDE + "," + LONGITUDE + "?q=" + Uri.encode(ADDRESS));
            Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(ADDRESS)));
                startActivity(browserIntent);
            }
        });

        // Directions
        Button btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(v -> {
            Uri gmmUri = Uri.parse("google.navigation:q=" + LATITUDE + "," + LONGITUDE);
            Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                // Fallback to browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(ADDRESS)));
                startActivity(browserIntent);
            }
        });
    }
}
