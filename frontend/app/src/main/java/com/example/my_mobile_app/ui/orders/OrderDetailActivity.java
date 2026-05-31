package com.example.my_mobile_app.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.OrderService;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.model.OrderItem;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.util.DateUtils;
import com.example.my_mobile_app.util.PriceFormatter;
import com.example.my_mobile_app.util.StatusUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Order detail screen: calls GET /orders/{orderId}. */
public class OrderDetailActivity extends BaseActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private TextView txtCode, txtStatus, txtDate, txtTimeline, txtAddress, txtGhn, txtPayment, txtSubtotal, txtShipping, txtTotal;
    private LinearLayout itemsContainer;
    private MaterialButton btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_order_detail);

        ImageButton btnBack = findViewById(R.id.btn_back);
        txtCode = findViewById(R.id.txt_order_code);
        txtStatus = findViewById(R.id.txt_order_status);
        txtDate = findViewById(R.id.txt_order_date);
        txtTimeline = findViewById(R.id.txt_status_timeline);
        txtAddress = findViewById(R.id.txt_shipping_address);
        txtGhn = findViewById(R.id.txt_ghn_code);
        txtPayment = findViewById(R.id.txt_payment_method);
        txtSubtotal = findViewById(R.id.txt_subtotal);
        txtShipping = findViewById(R.id.txt_shipping_fee);
        txtTotal = findViewById(R.id.txt_total);
        itemsContainer = findViewById(R.id.items_container);
        btnCancel = findViewById(R.id.btn_cancel_order);

        btnBack.setOnClickListener(v -> finish());
        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null || orderId.isEmpty()) {
            showError(getString(R.string.error_missing_order_id));
            finish();
            return;
        }
        btnCancel.setOnClickListener(v -> confirmCancel(orderId));
        load(orderId);
    }

    private void load(String orderId) {
        showLoading();
        ApiClient.get(this).create(OrderService.class).getOrderById(orderId)
                .enqueue(new Callback<ApiResponse<Order>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                                      @NonNull Response<ApiResponse<Order>> response) {
                        hideLoading();
                        ApiResponse<Order> body = response.body();
                        if (body == null || !body.success || body.data == null) {
                            showError(getString(R.string.error_order_not_found));
                            return;
                        }
                        bind(body.data);
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Order>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_load_order_detail));
                    }
                });
    }

    private void confirmCancel(String orderId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.order_detail_cancel)
                .setMessage(R.string.order_cancel_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> cancelOrder(orderId))
                .show();
    }

    private void cancelOrder(String orderId) {
        setCancelBusy(true);
        OrderService service = ApiClient.get(this).create(OrderService.class);
        service.cancelOrder(orderId)
                .enqueue(new Callback<ApiResponse<Order>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                                      @NonNull Response<ApiResponse<Order>> response) {
                        ApiResponse<Order> body = response.body();
                        if (response.isSuccessful() && body != null && body.success && body.data != null) {
                            onCancelSuccess(body.data);
                            return;
                        }
                        cancelOrderViaStatus(service, orderId, errorMessage(response));
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Order>> call,
                                                    @NonNull Throwable t) {
                        cancelOrderViaStatus(service, orderId, getString(R.string.error_cancel_order_connection));
                    }
                });
    }

    private void cancelOrderViaStatus(OrderService service, String orderId, String firstError) {
        Map<String, String> body = new HashMap<>();
        body.put("status", "CANCELLED");
        service.updateOrderStatus(orderId, body)
                .enqueue(new Callback<ApiResponse<Order>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                                      @NonNull Response<ApiResponse<Order>> response) {
                        ApiResponse<Order> result = response.body();
                        if (response.isSuccessful() && result != null && result.success && result.data != null) {
                            onCancelSuccess(result.data);
                            return;
                        }
                        setCancelBusy(false);
                        String message = errorMessage(response);
                        showError(message != null ? message : firstError);
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Order>> call,
                                                    @NonNull Throwable t) {
                        setCancelBusy(false);
                        showError(firstError != null
                                ? firstError
                                : getString(R.string.error_cancel_order_connection));
                    }
                });
    }

    private void onCancelSuccess(Order order) {
        setCancelBusy(false);
        showSuccess(getString(R.string.success_order_cancelled));
        bind(order);
    }

    private void setCancelBusy(boolean busy) {
        btnCancel.setEnabled(!busy);
        if (busy) showLoading(); else hideLoading();
    }

    private String errorMessage(Response<ApiResponse<Order>> response) {
        ApiResponse<Order> body = response.body();
        if (body != null && body.message != null && !body.message.isEmpty()) {
            return body.message;
        }
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return null;
        }
        try {
            JSONObject obj = new JSONObject(errorBody.string());
            String message = obj.optString("message");
            return message.isEmpty() ? null : message;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void bind(Order order) {
        txtCode.setText(getString(R.string.order_code_format, safe(order.orderId)));
        StatusUtils.Style style = StatusUtils.forOrder(this, order.status);
        txtStatus.setText(style.label);
        txtStatus.setTextColor(getColor(style.colorRes));
        txtStatus.setBackgroundResource(style.chipBgRes);

        Date date = DateUtils.parseIso(order.orderDate);
        txtDate.setText(getString(R.string.order_date_format, valueOrDash(DateUtils.formatDisplay(date))));
        txtTimeline.setText(buildTimeline(order.status));
        btnCancel.setVisibility("PENDING".equalsIgnoreCase(order.status) ? View.VISIBLE : View.GONE);
        txtAddress.setText(valueOrDash(order.shippingAddress));
        if (order.ghnOrderId != null && !order.ghnOrderId.isEmpty()) {
            txtGhn.setText(getString(R.string.order_detail_ghn_code, order.ghnOrderId));
            txtGhn.setVisibility(View.VISIBLE);
        } else {
            txtGhn.setVisibility(View.GONE);
        }
        txtPayment.setText(valueOrDash(order.paymentMethod) + " | " + valueOrDash(order.paymentStatus));

        double shipping = order.shippingFee == null ? 0 : order.shippingFee;
        txtSubtotal.setText(getString(R.string.subtotal_format,
                PriceFormatter.format(Math.max(0, order.totalAmount - shipping))));
        txtShipping.setText(getString(R.string.shipping_fee_format, PriceFormatter.format(shipping)));
        txtTotal.setText(getString(R.string.total_format, PriceFormatter.format(order.totalAmount)));

        itemsContainer.removeAllViews();
        if (order.orderItems == null || order.orderItems.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.order_detail_no_products);
            empty.setTextColor(getColor(R.color.text_muted));
            empty.setTextSize(14);
            itemsContainer.addView(empty);
            return;
        }
        for (OrderItem item : order.orderItems) {
            addItemRow(item);
        }
    }

    private void addItemRow(OrderItem item) {
        View row = getLayoutInflater().inflate(R.layout.item_order_detail_product, itemsContainer, false);
        android.widget.ImageView image = row.findViewById(R.id.img_product);
        TextView name = row.findViewById(R.id.txt_product_name);
        TextView qty = row.findViewById(R.id.txt_quantity);
        TextView price = row.findViewById(R.id.txt_price);

        name.setText(valueOrDash(item.productName));
        qty.setText("x" + item.quantity);
        price.setText(PriceFormatter.format(item.priceAtPurchase));
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Glide.with(row).load(item.imageUrl).into(image);
        }
        itemsContainer.addView(row);
    }

    private static String valueOrDash(String value) {
        return value == null || value.isEmpty() ? "--" : value;
    }

    private static String safe(String value) {
        return valueOrDash(value);
    }

    private String buildTimeline(String status) {
        String s = status == null ? "" : status.toUpperCase();
        String[] steps = {"PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"};
        String[] labels = {
                getString(R.string.status_pending),
                getString(R.string.status_confirmed),
                getString(R.string.status_processing),
                getString(R.string.status_in_transit),
                getString(R.string.status_delivered)
        };
        int active = 0;
        for (int i = 0; i < steps.length; i++) {
            if (steps[i].equals(s)) active = i;
        }
        if ("CANCELLED".equals(s)) return getString(R.string.order_timeline_cancelled);
        StringBuilder sb = new StringBuilder(getString(R.string.order_timeline_prefix));
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) sb.append("  >  ");
            sb.append(i <= active ? "[x] " : "[ ] ").append(labels[i]);
        }
        return sb.toString();
    }
}
