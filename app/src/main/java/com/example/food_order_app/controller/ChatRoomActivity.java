package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.ChatAdapter;
import com.example.food_order_app.model.ChatMessage;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private BottomNavigationView bottomNav;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    private String roomUserId;
    private boolean isAdminViewer = false;

    // Polling mechanism
    private Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private static final int POLLING_INTERVAL = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);

        initViews();

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Determine room logic
        Intent intent = getIntent();
        if (intent.hasExtra("room_user_id")) {
            roomUserId = intent.getStringExtra("room_user_id");
            isAdminViewer = true; // since it was passed in, we assume it's the admin opening it
            toolbar.setTitle("Chat với Khách hàng");
            bottomNav.setVisibility(View.GONE);
        } else {
            roomUserId = sessionManager.getUserId();
            isAdminViewer = false;
            toolbar.setNavigationIcon(null);
            bottomNav.setVisibility(View.VISIBLE);
            setupBottomNav();
        }

        chatAdapter = new ChatAdapter(this, messageList, sessionManager.getUserId(), isAdminViewer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);

        loadMessages();
        startPolling();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAdminViewer && bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_chat);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarChat);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        bottomNav = findViewById(R.id.bottomNav);

        toolbar.setNavigationOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_chat);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent nIntent = new Intent(this, HomeActivity.class);
                nIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nIntent);
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                Intent nIntent = new Intent(this, CartActivity.class);
                nIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nIntent);
                finish();
                return true;
            } else if (id == R.id.nav_chat) {
                return true;
            } else if (id == R.id.nav_contact) {
                Intent nIntent = new Intent(this, ContactActivity.class);
                nIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nIntent);
                finish();
                return true;
            } else if (id == R.id.nav_account) {
                Intent nIntent = new Intent(this, ProfileActivity.class);
                nIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(nIntent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadMessages() {
        dbService.getChatMessages("eq." + roomUserId, "*", "created_at.asc")
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (messageList.size() != response.body().size()) {
                                messageList.clear();
                                messageList.addAll(response.body());
                                chatAdapter.notifyDataSetChanged();
                                rvChatMessages.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        btnSend.setEnabled(false);

        Map<String, Object> payload = new HashMap<>();
        payload.put("room_user_id", roomUserId);
        payload.put("sender_id", sessionManager.getUserId());
        payload.put("message", text);

        dbService.createChatMessage(payload).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                btnSend.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    etMessage.setText("");
                    messageList.add(response.body().get(0));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    rvChatMessages.scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(ChatRoomActivity.this, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                btnSend.setEnabled(true);
                Toast.makeText(ChatRoomActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPolling() {
        pollingRunnable = () -> {
            loadMessages();
            pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
        };
        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }
}
