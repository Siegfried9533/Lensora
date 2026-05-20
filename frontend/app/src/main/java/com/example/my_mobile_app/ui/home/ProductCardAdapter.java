package com.example.my_mobile_app.ui.home;

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
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.util.PriceFormatter;

import java.util.List;
import java.util.Set;

/** Grid adapter for {@link DisplayItem} cards on the Discovery screen. */
public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.VH> {

    public interface OnItemClick {
        void onItemClick(DisplayItem item);
    }

    public interface OnFavoriteClick {
        void onFavoriteClick(DisplayItem item);
    }

    private final Context context;
    private List<DisplayItem> items;
    private Set<String> favoriteIds;
    private final OnItemClick itemListener;
    private final OnFavoriteClick favListener;

    public ProductCardAdapter(Context context,
                              List<DisplayItem> items,
                              Set<String> favoriteIds,
                              OnItemClick itemListener,
                              OnFavoriteClick favListener) {
        this.context = context;
        this.items = items;
        this.favoriteIds = favoriteIds;
        this.itemListener = itemListener;
        this.favListener = favListener;
    }

    public void setItems(List<DisplayItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setFavoriteIds(Set<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DisplayItem it = items.get(position);
        h.caption.setText(it.categoryName == null ? "" : it.categoryName.toUpperCase());
        h.title.setText(it.title);
        h.price.setText(PriceFormatter.format(it.price));

        boolean isAsset = "ASSET".equals(it.type);
        h.perDay.setVisibility(isAsset ? View.VISIBLE : View.GONE);
        h.typeBadge.setText(isAsset ? "ASSET" : "PRODUCT");

        Glide.with(context)
                .load(it.primaryImageUrl)
                .placeholder(R.color.bg_dark_tertiary)
                .error(R.color.bg_dark_tertiary)
                .into(h.image);

        boolean faved = favoriteIds != null && favoriteIds.contains(it.id);
        h.favorite.setImageResource(faved ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        h.itemView.setOnClickListener(v -> {
            if (itemListener != null) itemListener.onItemClick(it);
        });
        h.favorite.setOnClickListener(v -> {
            if (favListener != null) favListener.onFavoriteClick(it);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton favorite;
        TextView typeBadge;
        TextView caption;
        TextView title;
        TextView price;
        TextView perDay;

        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.img_product);
            favorite = v.findViewById(R.id.btn_favorite);
            typeBadge = v.findViewById(R.id.txt_type_badge);
            caption = v.findViewById(R.id.txt_category);
            title = v.findViewById(R.id.txt_title);
            price = v.findViewById(R.id.txt_price);
            perDay = v.findViewById(R.id.txt_per_day);
        }
    }
}
