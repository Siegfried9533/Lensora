package com.example.my_mobile_app.ui.transactions;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.OrderService;
import com.example.my_mobile_app.api.RentalService;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.orders.OrderDetailActivity;
import com.example.my_mobile_app.ui.rentals.RentalDetailActivity;
import com.example.my_mobile_app.util.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Transactions tab: calls GET /orders and GET /rentals for the current user. */
public class TransactionsActivity extends BaseActivity
        implements OrderListAdapter.OnClick, RentalListAdapter.OnClick {

    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    /** Chu kỳ tự làm mới danh sách để trạng thái đơn cập nhật gần như realtime khi đang xem. */
    private static final long POLL_INTERVAL_MS = 12_000L;

    private final List<Order> orders = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            load(false); // làm mới im lặng (không hiện spinner / không báo lỗi)
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    private RecyclerView rvOrders, rvRentals;
    private TextView txtOrdersEmpty, txtRentalsEmpty;
    private MaterialButton btnOrders, btnRentals;
    private OrderListAdapter orderAdapter;
    private RentalListAdapter rentalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_transactions);

        rvOrders = findViewById(R.id.rv_orders);
        rvRentals = findViewById(R.id.rv_rentals);
        txtOrdersEmpty = findViewById(R.id.txt_orders_empty);
        txtRentalsEmpty = findViewById(R.id.txt_rentals_empty);
        btnOrders = findViewById(R.id.btn_orders);
        btnRentals = findViewById(R.id.btn_rentals);

        orderAdapter = new OrderListAdapter(orders, this);
        rentalAdapter = new RentalListAdapter(rentals, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
        rvRentals.setLayoutManager(new LinearLayoutManager(this));
        rvRentals.setAdapter(rentalAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        BottomNavHelper.attachTo(this, bottomNav, R.id.nav_transactions);

        btnOrders.setOnClickListener(v -> showTab(0));
        btnRentals.setOnClickListener(v -> showTab(1));

        showTab(getIntent().getIntExtra(EXTRA_INITIAL_TAB, 0));
        load(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rvOrders == null) return;
        load(false); // làm mới ngay khi quay lại màn hình (vd: sau khi hủy đơn ở màn chi tiết)
        pollHandler.removeCallbacks(pollRunnable);
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable); // ngừng poll khi không còn xem màn hình
    }

    private void load(boolean showSpinner) {
        if (showSpinner) showLoading();
        AtomicInteger remaining = new AtomicInteger(2);
        Runnable done = () -> {
            if (remaining.decrementAndGet() == 0) {
                if (showSpinner) hideLoading();
                render();
            }
        };

        ApiClient.get(this).create(OrderService.class).getOrders()
                .enqueue(new Callback<ApiResponse<List<Order>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call,
                                                      @NonNull Response<ApiResponse<List<Order>>> response) {
                        orders.clear();
                        ApiResponse<List<Order>> body = response.body();
                        if (body != null && body.success && body.data != null) {
                            orders.addAll(body.data);
                        }
                        done.run();
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call,
                                                    @NonNull Throwable t) {
                        if (showSpinner) showError(getString(R.string.error_load_orders));
                        done.run();
                    }
                });

        ApiClient.get(this).create(RentalService.class).getRentals()
                .enqueue(new Callback<ApiResponse<List<Rental>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Rental>>> call,
                                                      @NonNull Response<ApiResponse<List<Rental>>> response) {
                        rentals.clear();
                        ApiResponse<List<Rental>> body = response.body();
                        if (body != null && body.success && body.data != null) {
                            rentals.addAll(body.data);
                        }
                        done.run();
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Rental>>> call,
                                                    @NonNull Throwable t) {
                        if (showSpinner) showError(getString(R.string.error_load_rentals));
                        done.run();
                    }
                });
    }

    private void render() {
        orderAdapter.notifyDataSetChanged();
        rentalAdapter.notifyDataSetChanged();
        txtOrdersEmpty.setVisibility(orders.isEmpty() && rvOrders.getVisibility() == View.VISIBLE
                ? View.VISIBLE : View.GONE);
        txtRentalsEmpty.setVisibility(rentals.isEmpty() && rvRentals.getVisibility() == View.VISIBLE
                ? View.VISIBLE : View.GONE);
    }

    private void showTab(int position) {
        boolean orderTab = position == 0;
        btnOrders.setBackgroundResource(orderTab ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnOrders.setTextColor(getColor(orderTab ? R.color.black : R.color.nav_inactive));
        btnRentals.setBackgroundResource(!orderTab ? R.drawable.bg_segment_active : R.drawable.bg_segment_inactive);
        btnRentals.setTextColor(getColor(!orderTab ? R.color.black : R.color.nav_inactive));
        rvOrders.setVisibility(orderTab ? View.VISIBLE : View.GONE);
        rvRentals.setVisibility(orderTab ? View.GONE : View.VISIBLE);
        txtOrdersEmpty.setVisibility(orderTab && orders.isEmpty() ? View.VISIBLE : View.GONE);
        txtRentalsEmpty.setVisibility(!orderTab && rentals.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onOrderClick(Order order) {
        Intent i = new Intent(this, OrderDetailActivity.class);
        i.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.orderId);
        startActivity(i);
    }

    @Override
    public void onRentalClick(Rental rental) {
        Intent i = new Intent(this, RentalDetailActivity.class);
        i.putExtra(RentalDetailActivity.EXTRA_RENTAL_ID, rental.rentalId);
        startActivity(i);
    }
}
