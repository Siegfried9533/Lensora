package com.example.my_mobile_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.FavoriteService;
import com.example.my_mobile_app.model.Favorite;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.equipment.EquipmentDetailActivity;
import com.example.my_mobile_app.util.PriceFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends BaseActivity {

    private final List<Favorite> items = new ArrayList<>();
    private FavoriteAdapter adapter;
    private TextView txtEmpty;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_favorites);

        ImageButton btnBack = findViewById(R.id.btn_back);
        RecyclerView recycler = findViewById(R.id.recycler);
        txtEmpty = findViewById(R.id.txt_empty);
        refresh = findViewById(R.id.refresh);

        adapter = new FavoriteAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        refresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        refresh.setRefreshing(true);
        ApiClient.get(this).create(FavoriteService.class).getFavorites()
                .enqueue(new Callback<ApiResponse<List<Favorite>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Favorite>>> call,
                                                      @NonNull Response<ApiResponse<List<Favorite>>> response) {
                        refresh.setRefreshing(false);
                        items.clear();
                        ApiResponse<List<Favorite>> body = response.body();
                        if (body != null && body.success && body.data != null) {
                            items.addAll(body.data);
                        }
                        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Favorite>>> call,
                                                    @NonNull Throwable t) {
                        refresh.setRefreshing(false);
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void remove(Favorite f) {
        Map<String, String> body = new HashMap<>();
        body.put("itemId", "PRODUCT".equals(f.type) ? f.productId : f.assetId);
        body.put("type", f.type);
        ApiClient.get(this).create(FavoriteService.class).toggleFavorite(body)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                                      @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                        load();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                                    @NonNull Throwable t) {
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void openDetail(Favorite f) {
        String id = "PRODUCT".equals(f.type) ? f.productId : f.assetId;
        if (id == null) return;
        Intent i = new Intent(this, EquipmentDetailActivity.class);
        i.putExtra(EquipmentDetailActivity.EXTRA_ID, id);
        i.putExtra(EquipmentDetailActivity.EXTRA_TYPE, f.type);
        startActivity(i);
    }

    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Favorite f = items.get(position);
            boolean isAsset = "ASSET".equals(f.type);
            h.txtType.setText(isAsset ? "Thiết bị thuê" : "Sản phẩm");
            String name = f.productName != null ? f.productName : f.assetName;
            h.txtName.setText(name == null ? "--" : name);
            if (f.price != null) {
                h.txtPrice.setText(PriceFormatter.format(f.price) + (isAsset ? "/ngày" : ""));
                h.txtPrice.setVisibility(View.VISIBLE);
            } else {
                h.txtPrice.setVisibility(View.GONE);
            }
            if (f.primaryImageUrl != null && !f.primaryImageUrl.isEmpty()) {
                Glide.with(FavoritesActivity.this).load(f.primaryImageUrl).into(h.img);
            } else {
                h.img.setImageDrawable(null);
            }
            h.itemView.setOnClickListener(v -> openDetail(f));
            h.btnRemove.setOnClickListener(v -> remove(f));
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ImageView img;
            final TextView txtType, txtName, txtPrice;
            final ImageButton btnRemove;
            VH(@NonNull View v) {
                super(v);
                img = v.findViewById(R.id.img_thumb);
                txtType = v.findViewById(R.id.txt_type);
                txtName = v.findViewById(R.id.txt_name);
                txtPrice = v.findViewById(R.id.txt_price);
                btnRemove = v.findViewById(R.id.btn_remove);
            }
        }
    }
}
