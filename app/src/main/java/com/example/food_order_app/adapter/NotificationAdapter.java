package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications = new ArrayList<>();
    private final Context context;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, OnNotificationClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() { return notifications.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView imgIcon;
        View viewUnreadDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            imgIcon = itemView.findViewById(R.id.imgNotifIcon);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }

        void bind(Notification notif) {
            tvTitle.setText(notif.getTitle());
            tvMessage.setText(notif.getMessage());

            // Format time
            try {
                String dateStr = notif.getCreatedAt();
                if (dateStr != null && dateStr.length() > 19) {
                    dateStr = dateStr.substring(0, 19);
                }
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = input.parse(dateStr);
                tvTime.setText(output.format(date));
            } catch (Exception e) {
                tvTime.setText(notif.getCreatedAt());
            }

            // Unread indicator
            if (notif.isRead()) {
                viewUnreadDot.setVisibility(View.GONE);
                itemView.setAlpha(0.7f);
            } else {
                viewUnreadDot.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> listener.onNotificationClick(notif));
        }
    }
}
