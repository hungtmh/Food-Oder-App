package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.food_order_app.R;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<Integer> bannerResIds = new ArrayList<>();
    private final Context context;
    private OnSliderClickListener listener;

    public interface OnSliderClickListener {
        void onSliderClick(int bannerResId);
    }

    public SliderAdapter(Context context, OnSliderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setBanners(List<Integer> banners) {
        this.bannerResIds = banners;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        int bannerResId = bannerResIds.get(position);
        Glide.with(context)
                .load(bannerResId)
                .apply(new RequestOptions()
                        .override(1080, 600)
                        .format(DecodeFormat.PREFER_RGB_565))
                .transform(new CenterCrop(), new RoundedCorners(24))
                .into(holder.imgSlider);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSliderClick(bannerResId);
        });
    }

    @Override
    public int getItemCount() { return bannerResIds.size(); }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSlider;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlider = itemView.findViewById(R.id.imgSlider);
        }
    }
}
