package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminChatListAdapter;
import com.example.food_order_app.model.AdminChatPreview;
import com.example.food_order_app.model.ChatMessage;
import com.example.food_order_app.model.User;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.AdminDrawerHelper;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatListActivity extends AppCompatActivity {

    private ImageView btnMenuDrawer;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvChatList;
    private TextView tvEmpty;

    private AdminChatListAdapter adapter;
    private List<AdminChatPreview> previewItems = new ArrayList<>();
    private SupabaseDbService dbService;

    // Cache user names
    private Map<String, String> userNamesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        dbService = RetrofitClient.getDbService();

        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
        btnMenuDrawer = findViewById(R.id.btnMenuDrawerChat);
        rvChatList = findViewById(R.id.rvAdminChatList);
        tvEmpty = findViewById(R.id.tvEmptyChat);

        AdminDrawerHelper.setupDrawer(this, drawerLayout, navigationView, btnMenuDrawer, R.id.navAdminChat);

        adapter = new AdminChatListAdapter(this, previewItems, roomUserId -> {
            Intent intent = new Intent(AdminChatListActivity.this, ChatRoomActivity.class);
            intent.putExtra("room_user_id", roomUserId);
            startActivity(intent);
        });

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(adapter);

        loadAllUsersAndMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh messages when returning
        if (!userNamesMap.isEmpty()) {
            loadMessages();
        }
    }

    private void loadAllUsersAndMessages() {
        // First load all users to get their names
        dbService.getUsersByRole("eq.user", "id,full_name", null).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User u : response.body()) {
                        userNamesMap.put(u.getId(), u.getFullName());
                    }
                }
                // Then load messages
                loadMessages();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                loadMessages(); // fallback to load messages anyway
            }
        });
    }

    private void loadMessages() {
        dbService.getAllChatMessages("*", "created_at.desc").enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processMessages(response.body());
                } else {
                    Toast.makeText(AdminChatListActivity.this, "Lỗi tải danh sách chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Toast.makeText(AdminChatListActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processMessages(List<ChatMessage> allMessages) {
        // Group by roomUserId, keep first occurance (which is the latest due to DESC
        // order)
        Map<String, ChatMessage> latestMsgs = new LinkedHashMap<>();
        for (ChatMessage msg : allMessages) {
            if (!latestMsgs.containsKey(msg.getRoomUserId())) {
                latestMsgs.put(msg.getRoomUserId(), msg);
            }
        }

        if (latestMsgs.isEmpty()) {
            previewItems.clear();
            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);

        // Grouping variables
        List<AdminChatPreview> todayList = new ArrayList<>();
        List<AdminChatPreview> last3DaysList = new ArrayList<>();
        List<AdminChatPreview> lastWeekList = new ArrayList<>();
        List<AdminChatPreview> lastMonthList = new ArrayList<>();
        List<AdminChatPreview> olderList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        // Get midnight today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayMillis = cal.getTimeInMillis();

        long threeDaysAgo = todayMillis - (3L * 24 * 60 * 60 * 1000);
        long sevenDaysAgo = todayMillis - (7L * 24 * 60 * 60 * 1000);
        long thirtyDaysAgo = todayMillis - (30L * 24 * 60 * 60 * 1000);

        for (ChatMessage msg : latestMsgs.values()) {
            Date date = null;
            try {
                if (msg.getCreatedAt() != null) {
                    date = sdf.parse(msg.getCreatedAt());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (date == null)
                continue;

            String userName = userNamesMap.containsKey(msg.getRoomUserId()) ? userNamesMap.get(msg.getRoomUserId())
                    : "Khách (" + msg.getRoomUserId().substring(0, 4) + ")";
            String previewText = msg.getSenderId().equals(msg.getRoomUserId()) ? userName + ": " + msg.getMessage()
                    : "Bạn: " + msg.getMessage();

            AdminChatPreview item = new AdminChatPreview(msg.getRoomUserId(), userName, previewText, date);
            long msgTime = date.getTime();

            if (msgTime >= todayMillis) {
                todayList.add(item);
            } else if (msgTime >= threeDaysAgo) {
                last3DaysList.add(item);
            } else if (msgTime >= sevenDaysAgo) {
                lastWeekList.add(item);
            } else if (msgTime >= thirtyDaysAgo) {
                lastMonthList.add(item);
            } else {
                olderList.add(item);
            }
        }

        // Assemble flat list
        previewItems.clear();

        if (!todayList.isEmpty()) {
            previewItems.add(new AdminChatPreview("Hôm nay"));
            previewItems.addAll(todayList);
        }
        if (!last3DaysList.isEmpty()) {
            previewItems.add(new AdminChatPreview("3 ngày gần nhất"));
            previewItems.addAll(last3DaysList);
        }
        if (!lastWeekList.isEmpty()) {
            previewItems.add(new AdminChatPreview("Tuần trước"));
            previewItems.addAll(lastWeekList);
        }
        if (!lastMonthList.isEmpty()) {
            previewItems.add(new AdminChatPreview("Tháng trước"));
            previewItems.addAll(lastMonthList);
        }
        if (!olderList.isEmpty()) {
            previewItems.add(new AdminChatPreview("Cũ hơn"));
            previewItems.addAll(olderList);
        }

        adapter.notifyDataSetChanged();
    }
}
