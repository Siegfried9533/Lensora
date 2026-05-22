package com.example.my_mobile_app.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.NotificationService;
import com.example.my_mobile_app.model.Notification;
import com.example.my_mobile_app.model.PaginatedResponse;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.orders.OrderDetailActivity;
import com.example.my_mobile_app.ui.rentals.RentalDetailActivity;
import com.example.my_mobile_app.util.BottomNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Notifications tab: lists notifications and marks read before navigation. */
public class NotificationsActivity extends BaseActivity implements NotificationAdapter.OnClick {

    private final List<Notification> items = new ArrayList<>();
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipe;
    private TextView emptyState;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_notifications);

        RecyclerView rv = findViewById(R.id.rv_notifications);
        swipe = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.txt_empty);
        bottomNav = findViewById(R.id.bottomNav);
        MaterialButton btnReadAll = findViewById(R.id.btn_mark_all_read);

        adapter = new NotificationAdapter(items, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        BottomNavHelper.attachTo(this, bottomNav, R.id.nav_notifications);
        swipe.setOnRefreshListener(this::load);
        btnReadAll.setOnClickListener(v -> markAllAsRead());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (swipe != null) load();
    }

    private void load() {
        swipe.setRefreshing(true);
        ApiClient.get(this).create(NotificationService.class)
                .getNotifications(0, 50)
                .enqueue(new Callback<ApiResponse<PaginatedResponse<Notification>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<PaginatedResponse<Notification>>> call,
                                                      @NonNull Response<ApiResponse<PaginatedResponse<Notification>>> response) {
                        swipe.setRefreshing(false);
                        items.clear();
                        ApiResponse<PaginatedResponse<Notification>> body = response.body();
                        if (body != null && body.success && body.data != null && body.data.content != null) {
                            items.addAll(body.data.content);
                        }
                        render();
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<PaginatedResponse<Notification>>> call,
                                                    @NonNull Throwable t) {
                        swipe.setRefreshing(false);
                        showError(getString(R.string.error_load_notifications));
                        render();
                    }
                });
        refreshBadge();
    }

    private void render() {
        adapter.notifyDataSetChanged();
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void refreshBadge() {
        ApiClient.get(this).create(NotificationService.class).getUnreadCount()
                .enqueue(new Callback<ApiResponse<Map<String, Integer>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Integer>>> call,
                                                      @NonNull Response<ApiResponse<Map<String, Integer>>> response) {
                        int count = 0;
                        ApiResponse<Map<String, Integer>> body = response.body();
                        if (body != null && body.success && body.data != null && body.data.get("count") != null) {
                            count = body.data.get("count");
                        }
                        if (count > 0) {
                            bottomNav.getOrCreateBadge(R.id.nav_notifications).setNumber(count);
                        } else {
                            bottomNav.removeBadge(R.id.nav_notifications);
                        }
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Integer>>> call,
                                                    @NonNull Throwable t) {
                        bottomNav.removeBadge(R.id.nav_notifications);
                    }
                });
    }

    private void markAllAsRead() {
        ApiClient.get(this).create(NotificationService.class).markAllAsRead()
                .enqueue(new Callback<ApiResponse<Map<String, Integer>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Integer>>> call,
                                                      @NonNull Response<ApiResponse<Map<String, Integer>>> response) {
                        showSuccess(getString(R.string.success_marked_all_read));
                        load();
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Integer>>> call,
                                                    @NonNull Throwable t) {
                        showError(getString(R.string.error_update_notifications));
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (notification == null) return;
        ApiClient.get(this).create(NotificationService.class)
                .markAsRead(notification.notificationId)
                .enqueue(new Callback<ApiResponse<Notification>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Notification>> call,
                                                      @NonNull Response<ApiResponse<Notification>> response) {
                        notification.isRead = true;
                        adapter.notifyDataSetChanged();
                        refreshBadge();
                        navigate(notification);
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Notification>> call,
                                                    @NonNull Throwable t) {
                        navigate(notification);
                    }
                });
    }

    private void navigate(Notification n) {
        String type = n.referenceType == null ? "" : n.referenceType;
        String id = n.referenceId;
        if ("ORDER".equals(type) && id != null && !id.isEmpty()) {
            startActivity(new Intent(this, OrderDetailActivity.class)
                    .putExtra(OrderDetailActivity.EXTRA_ORDER_ID, id));
        } else if ("RENTAL".equals(type) && id != null && !id.isEmpty()) {
            startActivity(new Intent(this, RentalDetailActivity.class)
                    .putExtra(RentalDetailActivity.EXTRA_RENTAL_ID, id));
        } else {
            showSuccess(getString(R.string.success_notification_read));
        }
    }
}
