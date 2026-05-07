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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Review> reviews = new ArrayList<>();
    private final Context context;
    private boolean hasMoreReviews = false;
    private LoadMoreListener loadMoreListener;
    
    private static final int VIEW_TYPE_REVIEW = 0;
    private static final int VIEW_TYPE_LOAD_MORE = 1;

    public interface LoadMoreListener {
        void onLoadMore();
    }

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }
    
    public void setHasMoreReviews(boolean hasMore) {
        this.hasMoreReviews = hasMore;
        notifyDataSetChanged();
    }
    
    public void setLoadMoreListener(LoadMoreListener listener) {
        this.loadMoreListener = listener;
    }
    
    public void updateData(List<Review> reviews, boolean hasMore, LoadMoreListener listener) {
        this.reviews = reviews;
        this.hasMoreReviews = hasMore;
        this.loadMoreListener = listener;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == reviews.size() && hasMoreReviews) {
            Log.d("ReviewAdapter", "getItemViewType: position=" + position + " -> LOAD_MORE (reviews.size=" + reviews.size() + ")");
            return VIEW_TYPE_LOAD_MORE;
        }
        Log.d("ReviewAdapter", "getItemViewType: position=" + position + " -> REVIEW (reviews.size=" + reviews.size() + ")");
        return VIEW_TYPE_REVIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOAD_MORE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_load_more, parent, false);
            return new LoadMoreViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_LOAD_MORE) {
            ((LoadMoreViewHolder) holder).bind(loadMoreListener);
        } else {
            ((ReviewViewHolder) holder).bind(reviews.get(position));
        }
    }

    @Override
    public int getItemCount() { 
        int cnt = hasMoreReviews ? reviews.size() + 1 : reviews.size();
        Log.d("ReviewAdapter", "getItemCount: reviews=" + reviews.size() + ", hasMore=" + hasMoreReviews + ", returning=" + cnt);
        return cnt;
    }

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
            // Comment
            String comment = review.getComment();
            if (comment != null && !comment.isEmpty()) {
                tvComment.setText(comment);
                tvComment.setVisibility(View.VISIBLE);
            } else {
                tvComment.setVisibility(View.GONE);
            }
            
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
                String avatarUrl = review.getUser().getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(context).load(avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_person);
                }
            } else {
                tvUserName.setText("Ẩn danh");
                imgAvatar.setImageResource(R.drawable.ic_person);
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

    class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        Button btnLoadMore;

        LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            btnLoadMore = itemView.findViewById(R.id.btnLoadMore);
        }

        void bind(LoadMoreListener listener) {
            btnLoadMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLoadMore();
                }
            });
        }
    }
}
