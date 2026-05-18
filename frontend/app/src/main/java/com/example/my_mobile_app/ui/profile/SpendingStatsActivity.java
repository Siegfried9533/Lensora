package com.example.my_mobile_app.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.OrderService;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.util.PriceFormatter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpendingStatsActivity extends BaseActivity {

    private TextView txtTotal, txtCount, txtAvg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_spending_stats);

        ImageButton btnBack = findViewById(R.id.btn_back);
        txtTotal = findViewById(R.id.txt_total);
        txtCount = findViewById(R.id.txt_count);
        txtAvg = findViewById(R.id.txt_avg);

        btnBack.setOnClickListener(v -> finish());
        bind(0, 0);
        load();
    }

    private void load() {
        showLoading();
        ApiClient.get(this).create(OrderService.class).getOrders()
                .enqueue(new Callback<ApiResponse<List<Order>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call,
                                                      @NonNull Response<ApiResponse<List<Order>>> response) {
                        hideLoading();
                        ApiResponse<List<Order>> body = response.body();
                        if (body == null || !body.success || body.data == null) {
                            bind(0, 0);
                            return;
                        }
                        double total = 0;
                        int count = 0;
                        for (Order o : body.data) {
                            if ("CANCELLED".equals(o.status)) continue;
                            total += o.totalAmount;
                            count++;
                        }
                        bind(total, count);
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void bind(double total, int count) {
        txtTotal.setText(PriceFormatter.format(total));
        txtCount.setText(String.valueOf(count));
        txtAvg.setText(PriceFormatter.format(count == 0 ? 0 : total / count));
    }
}
