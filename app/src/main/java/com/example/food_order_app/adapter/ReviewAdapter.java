package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews = new ArrayList<>();
    private final Context context;

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName, tvDate, tvComment, tvTitle;
        RatingBar ratingBar;
        HorizontalScrollView hsvReviewImages;
        LinearLayout layoutReviewImages;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgReviewAvatar);
            tvUserName = itemView.findViewById(R.id.tvReviewUserName);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvTitle = itemView.findViewById(R.id.tvReviewTitle);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            ratingBar = itemView.findViewById(R.id.rbReviewRating);
            hsvReviewImages = itemView.findViewById(R.id.hsvReviewImages);
            layoutReviewImages = itemView.findViewById(R.id.layoutReviewImages);
        }

        void bind(Review review) {
            tvComment.setText(review.getComment());
            ratingBar.setRating(review.getRating());

            // Title
            if (review.getTitle() != null && !review.getTitle().isEmpty()) {
                tvTitle.setText(review.getTitle());
                tvTitle.setVisibility(View.VISIBLE);
            } else {
                tvTitle.setVisibility(View.GONE);
            }

            // Review images
            if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
                hsvReviewImages.setVisibility(View.VISIBLE);
                layoutReviewImages.removeAllViews();
                String[] urls = review.getImageUrl().split(",");
                for (String url : urls) {
                    if (url.trim().isEmpty()) continue;
                    ImageView iv = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            dip2px(context, 100), dip2px(context, 100)
                    );
                    params.setMarginEnd(dip2px(context, 8));
                    iv.setLayoutParams(params);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(context).load(url.trim()).placeholder(R.drawable.bg_card).into(iv);
                    layoutReviewImages.addView(iv);
                }
            } else {
                hsvReviewImages.setVisibility(View.GONE);
                layoutReviewImages.removeAllViews();
            }

            if (review.getUser() != null) {
                tvUserName.setText(review.getUser().getFullName());
                Glide.with(context).load(review.getUser().getAvatarUrl())
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                tvUserName.setText("Ẩn danh");
            }

            try {
                SimpleDateFormat inputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String raw = review.getCreatedAt();
                if (raw != null && raw.contains(".")) raw = raw.substring(0, raw.indexOf('.'));
                if (raw != null && raw.contains("+")) raw = raw.substring(0, raw.indexOf('+'));
                Date date = inputFmt.parse(raw);
                tvDate.setText(outputFmt.format(date));
            } catch (Exception e) {
                tvDate.setText("");
            }
        }

        private int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
    }
}
