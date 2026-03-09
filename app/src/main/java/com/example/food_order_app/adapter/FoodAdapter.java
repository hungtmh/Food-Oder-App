package com.example.food_order_app.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.Favorite;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private static final String TAG = "FoodAdapter";
    private List<Food> foods = new ArrayList<>();
    private final Context context;
    private OnFoodClickListener listener;
    private Set<String> favoriteFoodIds = new HashSet<>();
    private SupabaseDbService dbService;
    private SessionManager sessionManager;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public FoodAdapter(Context context, OnFoodClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.dbService = RetrofitClient.getDbService();
        this.sessionManager = new SessionManager(context);
        loadFavorites();
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
        notifyDataSetChanged();
    }

    public void setFavoriteFoodIds(Set<String> ids) {
        this.favoriteFoodIds = ids;
        notifyDataSetChanged();
    }

    private void loadFavorites() {
        if (!sessionManager.isLoggedIn()) return;
        dbService.getFavorites("eq." + sessionManager.getUserId(), "food_id", "created_at.desc")
                .enqueue(new Callback<List<Favorite>>() {
                    @Override
                    public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            favoriteFoodIds.clear();
                            for (Favorite fav : response.body()) {
                                favoriteFoodIds.add(fav.getFoodId());
                            }
                            notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Favorite>> call, Throwable t) {
                        Log.e(TAG, "loadFavorites failed: " + t.getMessage());
                    }
                });
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foods.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() { return foods.size(); }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood, btnFavorite;
        TextView tvName, tvOriginalPrice, tvDiscountedPrice, tvDiscount;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscountedPrice = itemView.findViewById(R.id.tvDiscountedPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
        }

        void bind(Food food) {
            tvName.setText(food.getName());
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

            if (food.getDiscountPercent() > 0) {
                tvOriginalPrice.setText(nf.format(food.getPrice()) + " VNĐ");
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvDiscountedPrice.setText(nf.format(food.getDiscountedPrice()) + " VNĐ");
                tvDiscount.setText("Giảm " + food.getDiscountPercent() + "%");
                tvDiscount.setVisibility(View.VISIBLE);
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountedPrice.setText(nf.format(food.getPrice()) + " VNĐ");
                tvDiscount.setVisibility(View.GONE);
            }

            Glide.with(context).load(food.getImageUrl())
                    .placeholder(R.drawable.bg_button_primary)
                    .into(imgFood);

            // Favorite state
            boolean isFav = favoriteFoodIds.contains(food.getId());
            btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            btnFavorite.setOnClickListener(v -> {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleFavorite(food, this);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFoodClick(food);
            });
        }
    }

    private void toggleFavorite(Food food, FoodViewHolder holder) {
        String userId = sessionManager.getUserId();
        boolean isFav = favoriteFoodIds.contains(food.getId());

        if (isFav) {
            // Remove favorite
            dbService.removeFavorite("eq." + userId, "eq." + food.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        favoriteFoodIds.remove(food.getId());
                        holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "removeFavorite failed: " + t.getMessage());
                }
            });
        } else {
            // Add favorite
            Map<String, String> data = new HashMap<>();
            data.put("user_id", userId);
            data.put("food_id", food.getId());
            dbService.addFavorite(data).enqueue(new Callback<List<Favorite>>() {
                @Override
                public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                    if (response.isSuccessful()) {
                        favoriteFoodIds.add(food.getId());
                        holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(context, "Đã thêm vào yêu thích ❤", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<Favorite>> call, Throwable t) {
                    Log.e(TAG, "addFavorite failed: " + t.getMessage());
                }
            });
        }
    }
}
