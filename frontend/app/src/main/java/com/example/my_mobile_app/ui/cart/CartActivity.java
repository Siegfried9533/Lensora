package com.example.my_mobile_app.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.CartService;
import com.example.my_mobile_app.api.dto.UpdateQuantityRequest;
import com.example.my_mobile_app.model.CartItem;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.checkout.CheckoutActivity;
import com.example.my_mobile_app.ui.equipment.EquipmentDetailActivity;
import com.example.my_mobile_app.util.PriceFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Cart screen — mirrors {@code frontend/app/cart.tsx}. */
public class CartActivity extends BaseActivity implements CartItemAdapter.Callbacks {

    private final List<CartItem> items = new ArrayList<>();
    private CartItemAdapter adapter;

    private RecyclerView rv;
    private SwipeRefreshLayout swipe;
    private LinearLayout emptyState, bottomBar;
    private TextView txtTotal;
    private MaterialButton btnCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_cart);

        rv = findViewById(R.id.rv_cart);
        swipe = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.empty_state);
        bottomBar = findViewById(R.id.bottom_bar);
        txtTotal = findViewById(R.id.txt_total);
        btnCheckout = findViewById(R.id.btn_checkout);

        adapter = new CartItemAdapter(items, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.txt_clear_cart).setOnClickListener(v -> clearCart());
        findViewById(R.id.btn_continue_shopping).setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> {
            if (items.isEmpty()) return;
            startActivity(new Intent(this, CheckoutActivity.class));
        });
        swipe.setOnRefreshListener(this::load);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        swipe.setRefreshing(true);
        ApiClient.get(this).create(CartService.class).getCartItems()
                .enqueue(new Callback<ApiResponse<List<CartItem>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Response<ApiResponse<List<CartItem>>> response) {
                        swipe.setRefreshing(false);
                        ApiResponse<List<CartItem>> body = response.body();
                        items.clear();
                        if (body != null && body.success && body.data != null) {
                            items.addAll(body.data);
                        }
                        render();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Throwable t) {
                        swipe.setRefreshing(false);
                        showError(getString(R.string.error_load_cart));
                    }
                });
    }

    private void render() {
        boolean empty = items.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        swipe.setVisibility(empty ? View.GONE : View.VISIBLE);
        bottomBar.setVisibility(empty ? View.GONE : View.VISIBLE);
        adapter.notifyDataSetChanged();

        double total = 0;
        for (CartItem c : items) {
            double price = c.price == null ? 0 : c.price;
            total += price * Math.max(1, c.quantity);
        }
        txtTotal.setText(PriceFormatter.format(total));
    }

    private void clearCart() {
        if (items.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.cart_clear)
                .setMessage(R.string.cart_clear_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    showLoading();
                    ApiClient.get(this).create(CartService.class).clearCart()
                            .enqueue(new Callback<ApiResponse<Void>>() {
                                @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                                                  @NonNull Response<ApiResponse<Void>> response) {
                                    hideLoading();
                                    load();
                                }

                                @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call,
                                                                @NonNull Throwable t) {
                                    hideLoading();
                                    showError(getString(R.string.error_clear_cart));
                                }
                            });
                })
                .show();
    }

    @Override
    public void onQtyChange(CartItem item, int newQty) {
        ApiClient.get(this).create(CartService.class)
                .updateQuantity(item.cartItemId, new UpdateQuantityRequest(newQty))
                .enqueue(new Callback<ApiResponse<CartItem>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Response<ApiResponse<CartItem>> response) {
                        load();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<CartItem>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_update_quantity));
                    }
                });
    }

    @Override
    public void onRemove(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cart_remove_title)
                .setMessage(R.string.cart_remove_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    ApiClient.get(this).create(CartService.class).removeFromCart(item.cartItemId)
                            .enqueue(new Callback<ApiResponse<Void>>() {
                                @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                                    load();
                                }
                                @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                                    showError(getString(R.string.error_delete_item));
                                }
                            });
                })
                .show();
    }

    @Override
    public void onItemClick(CartItem item) {
        Intent i = new Intent(this, EquipmentDetailActivity.class);
        boolean isProduct = "PRODUCT".equals(item.type);
        i.putExtra(EquipmentDetailActivity.EXTRA_ID, isProduct ? item.productId : item.assetId);
        i.putExtra(EquipmentDetailActivity.EXTRA_TYPE, item.type);
        startActivity(i);
    }
}
