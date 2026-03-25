package com.example.food_order_app.model;

import java.util.Date;

public class AdminChatPreview {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    
    // For Header
    private String headerTitle;

    // For Item
    private String roomUserId;
    private String userName;
    private String lastMessage;
    private Date time;

    // Constructor for header
    public AdminChatPreview(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor for item
    public AdminChatPreview(String roomUserId, String userName, String lastMessage, Date time) {
        this.type = TYPE_ITEM;
        this.roomUserId = roomUserId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.time = time;
    }

    public int getType() { return type; }
    public String getHeaderTitle() { return headerTitle; }
    public String getRoomUserId() { return roomUserId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getLastMessage() { return lastMessage; }
    public Date getTime() { return time; }
}
