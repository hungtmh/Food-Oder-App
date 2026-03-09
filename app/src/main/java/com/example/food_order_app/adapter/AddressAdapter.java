package com.example.food_order_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnAddressActionListener {
        void onSetDefault(Address address, int position);
        void onEdit(Address address, int position);
        void onDelete(Address address, int position);
        /** Chỉ dùng ở chế độ chọn (Checkout) */
        void onPick(Address address);
    }

    private List<Address> addresses = new ArrayList<>();
    private final Context context;
    private final OnAddressActionListener listener;
    private final boolean pickMode;

    public AddressAdapter(Context context, OnAddressActionListener listener, boolean pickMode) {
        this.context = context;
        this.listener = listener;
        this.pickMode = pickMode;
    }

    public void setAddresses(List<Address> list) {
        this.addresses = list;
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < addresses.size()) {
            addresses.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, addresses.size());
        }
    }

    public void updateAt(int position, Address updated) {
        if (position >= 0 && position < addresses.size()) {
            addresses.set(position, updated);
            notifyItemChanged(position);
        }
    }

    /** Cập nhật lại toàn bộ trạng thái mặc định sau khi đổi */
    public void refreshDefaults(String newDefaultId) {
        for (int i = 0; i < addresses.size(); i++) {
            boolean wasDefault = addresses.get(i).isDefault();
            boolean shouldBeDefault = addresses.get(i).getId().equals(newDefaultId);
            if (wasDefault != shouldBeDefault) {
                addresses.get(i).setDefault(shouldBeDefault);
                notifyItemChanged(i);
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.bind(addresses.get(position), position);
    }

    @Override
    public int getItemCount() { return addresses.size(); }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvReceiverName, tvPhone, tvAddress, tvDefaultBadge;
        Button btnSetDefault;
        ImageButton btnEdit, btnDelete;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
            btnEdit = itemView.findViewById(R.id.btnEditAddress);
            btnDelete = itemView.findViewById(R.id.btnDeleteAddress);
        }

        void bind(Address addr, int pos) {
            tvReceiverName.setText(addr.getReceiverName());
            tvPhone.setText("📞 " + addr.getPhone());
            tvAddress.setText("📍 " + addr.getAddress());

            if (addr.isDefault()) {
                tvDefaultBadge.setVisibility(View.VISIBLE);
                btnSetDefault.setVisibility(View.GONE);
            } else {
                tvDefaultBadge.setVisibility(View.GONE);
                btnSetDefault.setVisibility(View.VISIBLE);
            }

            btnSetDefault.setOnClickListener(v -> listener.onSetDefault(addr, pos));
            btnEdit.setOnClickListener(v -> listener.onEdit(addr, pos));
            btnDelete.setOnClickListener(v -> listener.onDelete(addr, pos));

            if (pickMode) {
                itemView.setOnClickListener(v -> listener.onPick(addr));
            }
        }
    }
}
