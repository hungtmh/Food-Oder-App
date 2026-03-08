package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Favorite;
import com.example.food_order_app.model.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavViewHolder> {
    private List<Favorite> favorites = new ArrayList<>();
    private final Context context;
    private OnFavoriteActionListener listener;

    public interface OnFavoriteActionListener {
        void onFoodClick(Food food);
        void onRemoveFavorite(Favorite favorite, int position);
    }

    public FavoritesAdapter(Context context, OnFavoriteActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < favorites.size()) {
            favorites.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, favorites.size());
        }
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        Favorite fav = favorites.get(position);
        holder.bind(fav);
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFavFood, btnRemoveFav;
        TextView tvFavFoodName, tvFavRating, tvFavPrice;

        FavViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavFood = itemView.findViewById(R.id.imgFavFood);
            btnRemoveFav = itemView.findViewById(R.id.btnRemoveFav);
            tvFavFoodName = itemView.findViewById(R.id.tvFavFoodName);
            tvFavRating = itemView.findViewById(R.id.tvFavRating);
            tvFavPrice = itemView.findViewById(R.id.tvFavPrice);
        }

        void bind(Favorite fav) {
            Food food = fav.getFood();
            if (food == null) return;

            tvFavFoodName.setText(food.getName());

            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvFavPrice.setText(nf.format(food.getDiscountedPrice()) + " VNĐ");

            tvFavRating.setText(String.format(Locale.getDefault(), "%.1f (%d đánh giá)",
                    food.getAvgRating(), food.getTotalReviews()));

            Glide.with(context).load(food.getImageUrl())
                    .placeholder(R.drawable.bg_button_primary)
                    .into(imgFavFood);

            btnRemoveFav.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveFavorite(fav, getAdapterPosition());
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFoodClick(food);
            });
        }
    }
}
