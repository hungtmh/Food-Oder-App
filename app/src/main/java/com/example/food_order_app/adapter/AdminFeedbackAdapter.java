package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Feedback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminFeedbackAdapter extends RecyclerView.Adapter<AdminFeedbackAdapter.ViewHolder> {
    private List<Feedback> feedbacks = new ArrayList<>();
    private final Context context;
    private final OnFeedbackClickListener listener;

    public interface OnFeedbackClickListener {
        void onFeedbackClick(Feedback feedback);
    }

    public AdminFeedbackAdapter(Context context, OnFeedbackClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(feedbacks.get(position));
    }

    @Override
    public int getItemCount() { return feedbacks.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName, tvDate, tvStatus, tvContent;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgFeedbackAvatar);
            tvUserName = itemView.findViewById(R.id.tvFeedbackUserName);
            tvDate = itemView.findViewById(R.id.tvFeedbackDate);
            tvStatus = itemView.findViewById(R.id.tvFeedbackStatus);
            tvContent = itemView.findViewById(R.id.tvFeedbackContent);
            ratingBar = itemView.findViewById(R.id.ratingFeedback);
        }

        void bind(Feedback feedback) {
            if (feedback.getUser() != null) {
                String name = feedback.getUser().getFullName();
                tvUserName.setText(name != null && !name.isEmpty() ? name : feedback.getUser().getEmail());
                Glide.with(context).load(feedback.getUser().getAvatarUrl())
                        .placeholder(R.drawable.ic_person_placeholder)
                        .into(imgAvatar);
            } else {
                tvUserName.setText("Khách hàng");
            }

            ratingBar.setRating(feedback.getRating());
            tvContent.setText(feedback.getContent());

            if (feedback.isRead()) {
                tvStatus.setText("Đã đọc");
                tvStatus.setBackgroundResource(R.drawable.bg_status_read);
            } else {
                tvStatus.setText("Mới");
                tvStatus.setBackgroundResource(R.drawable.bg_status_unread);
            }

            // Format date
            try {
                String dateStr = feedback.getCreatedAt();
                if (dateStr != null && dateStr.length() > 19) {
                    dateStr = dateStr.substring(0, 19);
                }
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = input.parse(dateStr);
                tvDate.setText(output.format(date));
            } catch (Exception e) {
                tvDate.setText(feedback.getCreatedAt());
            }

            itemView.setOnClickListener(v -> listener.onFeedbackClick(feedback));
        }
    }
}
