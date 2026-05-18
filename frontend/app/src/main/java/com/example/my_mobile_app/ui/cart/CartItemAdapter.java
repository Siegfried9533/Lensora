package com.example.my_mobile_app.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.model.CartItem;
import com.example.my_mobile_app.util.DateUtils;
import com.example.my_mobile_app.util.PriceFormatter;

import java.util.Date;
import java.util.List;

/** RecyclerView adapter for cart items. */
public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.VH> {

    public interface Callbacks {
        void onQtyChange(CartItem item, int newQty);
        void onRemove(CartItem item);
        void onItemClick(CartItem item);
    }

    private final List<CartItem> items;
    private final Callbacks cb;

    public CartItemAdapter(List<CartItem> items, Callbacks cb) {
        this.items = items;
        this.cb = cb;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CartItem item = items.get(position);
        boolean isProduct = "PRODUCT".equals(item.type);

        h.txtType.setText(isProduct ? "PRODUCT" : "ASSET");
        h.txtName.setText(isProduct ? item.productName : item.assetName);

        double unit = item.price == null ? 0 : item.price;
        String priceStr = PriceFormatter.format(unit);
        if (!isProduct) priceStr += " /ngày";
        h.txtPrice.setText(priceStr);

        if (item.primaryImageUrl != null && !item.primaryImageUrl.isEmpty()) {
            Glide.with(h.itemView).load(item.primaryImageUrl).into(h.imgThumb);
        } else {
            h.imgThumb.setImageDrawable(null);
        }

        if (isProduct) {
            h.qtyRow.setVisibility(View.VISIBLE);
            h.txtDateRange.setVisibility(View.GONE);
            h.txtQty.setText(String.valueOf(item.quantity));
            h.btnMinus.setOnClickListener(v -> {
                if (item.quantity > 1) cb.onQtyChange(item, item.quantity - 1);
            });
            h.btnPlus.setOnClickListener(v -> cb.onQtyChange(item, item.quantity + 1));
        } else {
            h.qtyRow.setVisibility(View.GONE);
            if (item.startDate != null && item.endDate != null) {
                Date s = DateUtils.parseIso(item.startDate);
                Date e = DateUtils.parseIso(item.endDate);
                long days = Math.max(1, DateUtils.daysBetween(s, e));
                h.txtDateRange.setText(DateUtils.formatDisplay(s) + " - "
                        + DateUtils.formatDisplay(e) + " (" + days + " ngày)");
                h.txtDateRange.setVisibility(View.VISIBLE);
            } else {
                h.txtDateRange.setVisibility(View.GONE);
            }
        }

        h.btnRemove.setOnClickListener(v -> cb.onRemove(item));
        h.itemView.setOnClickListener(v -> cb.onItemClick(item));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView imgThumb;
        final TextView txtType, txtName, txtPrice, txtQty, txtDateRange;
        final ImageButton btnMinus, btnPlus, btnRemove;
        final LinearLayout qtyRow;
        VH(@NonNull View v) {
            super(v);
            imgThumb = v.findViewById(R.id.img_thumb);
            txtType = v.findViewById(R.id.txt_type);
            txtName = v.findViewById(R.id.txt_name);
            txtPrice = v.findViewById(R.id.txt_price);
            txtQty = v.findViewById(R.id.txt_qty);
            txtDateRange = v.findViewById(R.id.txt_date_range);
            btnMinus = v.findViewById(R.id.btn_minus);
            btnPlus = v.findViewById(R.id.btn_plus);
            btnRemove = v.findViewById(R.id.btn_remove);
            qtyRow = v.findViewById(R.id.qty_row);
        }
    }
}
