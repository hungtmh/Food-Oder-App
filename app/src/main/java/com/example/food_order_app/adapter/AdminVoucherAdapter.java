package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Voucher;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.VoucherViewHolder> {

    public interface OnVoucherActionListener {
        void onToggleActive(Voucher voucher, boolean isActive);
    }

    private final Context context;
    private final NumberFormat nf;
    private final OnVoucherActionListener listener;
    private List<Voucher> vouchers = new ArrayList<>();

    public AdminVoucherAdapter(Context context, OnVoucherActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers == null ? new ArrayList<>() : vouchers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        holder.bind(vouchers.get(position));
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode;
        private final TextView tvTitle;
        private final TextView tvDiscount;
        private final TextView tvCondition;
        private final TextView tvUsage;
        private final TextView tvTime;
        private final TextView tvStatus;
        private final SwitchMaterial switchActive;

        VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvCondition = itemView.findViewById(R.id.tvVoucherCondition);
            tvUsage = itemView.findViewById(R.id.tvVoucherUsage);
            tvTime = itemView.findViewById(R.id.tvVoucherTime);
            tvStatus = itemView.findViewById(R.id.tvVoucherStatus);
            switchActive = itemView.findViewById(R.id.switchVoucherActive);
        }

        void bind(Voucher voucher) {
            tvCode.setText(voucher.getCode());
            tvTitle.setText(voucher.getTitle() == null ? "Không có tiêu đề" : voucher.getTitle());

            String discountText;
            if ("percent".equals(voucher.getDiscountType())) {
                String maxText = voucher.getMaxDiscountAmount() == null ? ""
                        : " (tối đa " + nf.format(voucher.getMaxDiscountAmount()) + "đ)";
                discountText = "Giảm " + ((int) voucher.getDiscountValue()) + "%" + maxText;
            } else {
                discountText = "Giảm " + nf.format(voucher.getDiscountValue()) + "đ";
            }
            tvDiscount.setText(discountText);

            tvCondition.setText("Đơn tối thiểu " + nf.format(voucher.getMinOrderValue()) + "đ");

            String usageLimitText = voucher.getUsageLimit() == null ? "không giới hạn"
                    : String.valueOf(voucher.getUsageLimit());
            tvUsage.setText("Đã dùng: " + voucher.getUsedCount() + " / " + usageLimitText);

            tvTime.setText(
                    "Thời gian: " + formatDate(voucher.getStartDate()) + " - " + formatDate(voucher.getEndDate()));

            boolean active = voucher.isActive();
            tvStatus.setText(active ? "Đang bật" : "Đã tắt");
            tvStatus.setBackgroundResource(active ? R.drawable.bg_status_available : R.drawable.bg_status_unavailable);

            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(active);
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActive(voucher, isChecked);
                }
            });
        }

        private String formatDate(String input) {
            if (input == null || input.trim().isEmpty()) {
                return "-";
            }
            try {
                String normalized = input;
                if (normalized.endsWith("Z")) {
                    normalized = normalized.substring(0, normalized.length() - 1);
                }
                if (normalized.length() >= 19) {
                    normalized = normalized.substring(0, 19);
                }
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = in.parse(normalized);
                return date == null ? input : out.format(date);
            } catch (Exception e) {
                return input;
            }
        }
    }
}
