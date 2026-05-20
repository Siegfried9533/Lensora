package com.example.my_mobile_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.RentalService;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.rentals.RentalDetailActivity;
import com.example.my_mobile_app.ui.transactions.RentalListAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyEquipmentActivity extends BaseActivity {

    private final List<Rental> items = new ArrayList<>();
    private RentalListAdapter adapter;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_my_equipment);

        ImageButton btnBack = findViewById(R.id.btn_back);
        RecyclerView recycler = findViewById(R.id.recycler);
        txtEmpty = findViewById(R.id.txt_empty);

        adapter = new RentalListAdapter(items, this::openDetail);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        load();
    }

    private void load() {
        showLoading();
        ApiClient.get(this).create(RentalService.class).getRentals()
                .enqueue(new Callback<ApiResponse<List<Rental>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Rental>>> call,
                                                      @NonNull Response<ApiResponse<List<Rental>>> response) {
                        hideLoading();
                        items.clear();
                        ApiResponse<List<Rental>> body = response.body();
                        if (body != null && body.success && body.data != null) {
                            items.addAll(body.data);
                        }
                        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Rental>>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void openDetail(Rental r) {
        Intent i = new Intent(this, RentalDetailActivity.class);
        i.putExtra(RentalDetailActivity.EXTRA_RENTAL_ID, r.rentalId);
        startActivity(i);
    }
}
