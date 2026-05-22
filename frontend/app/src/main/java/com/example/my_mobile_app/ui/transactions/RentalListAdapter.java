package com.example.my_mobile_app.ui.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.util.DateUtils;
import com.example.my_mobile_app.util.PriceFormatter;
import com.example.my_mobile_app.util.StatusUtils;

import java.util.Date;
import java.util.List;

/** Renders rental rows for the Transactions tab. */
public class RentalListAdapter extends RecyclerView.Adapter<RentalListAdapter.VH> {

    public interface OnClick {
        void onRentalClick(Rental rental);
    }

    private final List<Rental> items;
    private final OnClick onClick;

    public RentalListAdapter(List<Rental> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rental, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Rental rental = items.get(position);
        h.txtName.setText(safe(rental.assetName));
        h.txtBrand.setText(safe(rental.assetBrand));
        h.txtTotal.setText(PriceFormatter.format(rental.totalRentFee));

        Date start = DateUtils.parseIso(rental.startDate);
        Date end = DateUtils.parseIso(rental.endDate);
        String range = DateUtils.formatDisplay(start) + " - " + DateUtils.formatDisplay(end);
        h.txtPeriod.setText(h.itemView.getContext().getString(R.string.rental_period_format,
                range.trim().equals("-") ? "--" : range));

        StatusUtils.Style style = StatusUtils.forRental(h.itemView.getContext(), rental.status);
        h.txtStatus.setText(style.label);
        h.txtStatus.setTextColor(h.itemView.getContext().getColor(style.colorRes));
        h.txtStatus.setBackgroundResource(style.chipBgRes);

        if (rental.primaryImageUrl != null && !rental.primaryImageUrl.isEmpty()) {
            Glide.with(h.itemView).load(rental.primaryImageUrl).into(h.imgThumb);
        } else {
            h.imgThumb.setImageDrawable(null);
        }

        h.itemView.setOnClickListener(v -> onClick.onRentalClick(rental));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String safe(String value) {
        return value == null || value.isEmpty() ? "--" : value;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView imgThumb;
        final TextView txtName, txtBrand, txtPeriod, txtTotal, txtStatus;

        VH(@NonNull View v) {
            super(v);
            imgThumb = v.findViewById(R.id.img_rental_thumb);
            txtName = v.findViewById(R.id.txt_rental_name);
            txtBrand = v.findViewById(R.id.txt_rental_brand);
            txtPeriod = v.findViewById(R.id.txt_rental_period);
            txtTotal = v.findViewById(R.id.txt_rental_total);
            txtStatus = v.findViewById(R.id.txt_rental_status);
        }
    }
}
