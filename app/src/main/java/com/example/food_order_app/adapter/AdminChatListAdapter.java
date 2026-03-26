package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.model.AdminChatPreview;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<AdminChatPreview> items;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(String roomUserId);
    }

    public AdminChatListAdapter(Context context, List<AdminChatPreview> items, OnChatClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<AdminChatPreview> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AdminChatPreview.TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_admin_chat_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_admin_chat_preview, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdminChatPreview item = items.get(position);

        if (getItemViewType(position) == AdminChatPreview.TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeaderTitle.setText(item.getHeaderTitle());
        } else {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.tvUserName.setText(item.getUserName());
            itemHolder.tvLastMessage.setText(item.getLastMessage());

            if (item.getTime() != null) {
                SimpleDateFormat outSdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
                itemHolder.tvTime.setText(outSdf.format(item.getTime()));
            } else {
                itemHolder.tvTime.setText("");
            }

            itemHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatClick(item.getRoomUserId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage, tvTime;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
