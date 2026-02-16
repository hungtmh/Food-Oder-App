package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.R;
import com.example.food_order_app.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems = new ArrayList<>();
    private final Context context;
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChange(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item, int position);
    }

    public CartAdapter(Context context, OnCartActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
        }
    }

    public List<CartItem> getCartItems() { return cartItems; }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName, tvPrice, tvQuantity, tvSubtotal;
        ImageButton btnMinus, btnPlus, btnDelete;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgCartFood);
            tvName = itemView.findViewById(R.id.tvCartFoodName);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartSubtotal);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }

        void bind(CartItem item, int position) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

            if (item.getFood() != null) {
                tvName.setText(item.getFood().getName());
                tvPrice.setText(nf.format(item.getFood().getDiscountedPrice()) + " VNĐ");
                Glide.with(context).load(item.getFood().getImageUrl())
                        .placeholder(R.drawable.bg_button_primary)
                        .into(imgFood);
            }

            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvSubtotal.setText(nf.format(item.getSubtotal()) + " VNĐ");

            btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    listener.onQuantityChange(item, item.getQuantity() - 1);
                }
            });

            btnPlus.setOnClickListener(v -> {
                listener.onQuantityChange(item, item.getQuantity() + 1);
            });

            btnDelete.setOnClickListener(v -> {
                listener.onRemoveItem(item, position);
            });
        }
    }
}
