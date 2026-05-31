package com.example.my_mobile_app.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.model.Category;

import java.util.ArrayList;
import java.util.List;

/** Horizontal chip adapter for category filters. */
public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.VH> {

    public interface OnCategoryClick {
        void onCategoryClick(Category category);
    }

    public static final String ALL_ID = "all";

    private final List<Category> categories = new ArrayList<>();
    private String selectedId = ALL_ID;
    private final OnCategoryClick listener;

    public CategoryChipAdapter(OnCategoryClick listener) {
        this.listener = listener;
        Category all = new Category();
        all.categoryId = ALL_ID;
        all.categoryName = "";
        all.type = "ALL";
        categories.add(all);
    }

    public void setCategories(List<Category> cats) {
        categories.clear();
        Category all = new Category();
        all.categoryId = ALL_ID;
        all.categoryName = "";
        all.type = "ALL";
        categories.add(all);
        if (cats != null) categories.addAll(cats);
        notifyDataSetChanged();
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Category c = categories.get(position);
        boolean selected = c.categoryId != null && c.categoryId.equals(selectedId);
        h.text.setText(ALL_ID.equals(c.categoryId)
                ? h.text.getContext().getString(R.string.home_category_all)
                : c.categoryName);
        h.text.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        h.text.setTextColor(ContextCompat.getColor(h.text.getContext(),
                selected ? R.color.black : R.color.text_primary));
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView text;
        VH(@NonNull View v) {
            super(v);
            text = v.findViewById(R.id.txt_chip);
        }
    }
}
