package com.example.my_mobile_app.ui.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.util.DateUtils;
import com.example.my_mobile_app.util.PriceFormatter;
import com.example.my_mobile_app.util.StatusUtils;

import java.util.Date;
import java.util.List;

/** Renders order rows for the Transactions tab. */
public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.VH> {

    public interface OnClick {
        void onOrderClick(Order order);
    }

    private final List<Order> items;
    private final OnClick onClick;

    public OrderListAdapter(List<Order> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order order = items.get(position);
        h.txtCode.setText(h.itemView.getContext().getString(R.string.order_code_format, safe(order.orderId)));
        h.txtTotal.setText(PriceFormatter.format(order.totalAmount));

        Date date = DateUtils.parseIso(order.orderDate);
        String dateText = DateUtils.formatDisplay(date);
        h.txtDate.setText(h.itemView.getContext().getString(R.string.order_date_format,
                dateText.isEmpty() ? "--" : dateText));

        int count = order.orderItems == null ? 0 : order.orderItems.size();
        h.txtMeta.setText(h.itemView.getContext().getString(R.string.order_meta_format,
                count, safe(order.paymentMethod)));

        StatusUtils.Style style = StatusUtils.forOrder(h.itemView.getContext(), order.status);
        h.txtStatus.setText(style.label);
        h.txtStatus.setTextColor(h.itemView.getContext().getColor(style.colorRes));
        h.txtStatus.setBackgroundResource(style.chipBgRes);

        h.itemView.setOnClickListener(v -> onClick.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String safe(String value) {
        return value == null || value.isEmpty() ? "--" : value;
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView txtCode, txtTotal, txtDate, txtMeta, txtStatus;

        VH(@NonNull View v) {
            super(v);
            txtCode = v.findViewById(R.id.txt_order_code);
            txtTotal = v.findViewById(R.id.txt_order_total);
            txtDate = v.findViewById(R.id.txt_order_date);
            txtMeta = v.findViewById(R.id.txt_order_meta);
            txtStatus = v.findViewById(R.id.txt_order_status);
        }
    }
}
