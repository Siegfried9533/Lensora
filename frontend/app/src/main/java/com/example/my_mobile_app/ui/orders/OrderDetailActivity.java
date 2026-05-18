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

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Order detail screen: calls GET /orders/{orderId}. */
public class OrderDetailActivity extends BaseActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private TextView txtCode, txtStatus, txtDate, txtTimeline, txtAddress, txtPayment, txtSubtotal, txtShipping, txtTotal;
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
        txtPayment = findViewById(R.id.txt_payment_method);
        txtSubtotal = findViewById(R.id.txt_subtotal);
        txtShipping = findViewById(R.id.txt_shipping_fee);
        txtTotal = findViewById(R.id.txt_total);
        itemsContainer = findViewById(R.id.items_container);
        btnCancel = findViewById(R.id.btn_cancel_order);

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v ->
                showError("Backend hiện chưa có endpoint huỷ đơn cho khách hàng"));
        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null || orderId.isEmpty()) {
            showError("Thiếu mã đơn hàng");
            finish();
            return;
        }
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
                            showError("Không tìm thấy đơn hàng");
                            return;
                        }
                        bind(body.data);
                    }

                    @Override public void onFailure(@NonNull Call<ApiResponse<Order>> call,
                                                    @NonNull Throwable t) {
                        hideLoading();
                        showError("Không thể tải chi tiết đơn hàng");
                    }
                });
    }

    private void bind(Order order) {
        txtCode.setText("Mã đơn: " + safe(order.orderId));
        StatusUtils.Style style = StatusUtils.forOrder(order.status);
        txtStatus.setText(style.label);
        txtStatus.setTextColor(getColor(style.colorRes));
        txtStatus.setBackgroundResource(style.chipBgRes);

        Date date = DateUtils.parseIso(order.orderDate);
        txtDate.setText("Ngày đặt: " + valueOrDash(DateUtils.formatDisplay(date)));
        txtTimeline.setText(buildTimeline(order.status));
        btnCancel.setVisibility("PENDING".equalsIgnoreCase(order.status) ? View.VISIBLE : View.GONE);
        txtAddress.setText(valueOrDash(order.shippingAddress));
        txtPayment.setText(valueOrDash(order.paymentMethod) + " | " + valueOrDash(order.paymentStatus));

        double shipping = order.shippingFee == null ? 0 : order.shippingFee;
        txtSubtotal.setText("Tạm tính: " + PriceFormatter.format(Math.max(0, order.totalAmount - shipping)));
        txtShipping.setText("Phí vận chuyển: " + PriceFormatter.format(shipping));
        txtTotal.setText("Tổng cộng: " + PriceFormatter.format(order.totalAmount));

        itemsContainer.removeAllViews();
        if (order.orderItems == null || order.orderItems.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Không có sản phẩm");
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

    private static String buildTimeline(String status) {
        String s = status == null ? "" : status.toUpperCase();
        String[] steps = {"PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"};
        String[] labels = {"Chờ xử lý", "Đã xác nhận", "Đang xử lý", "Đang vận chuyển", "Đã giao"};
        int active = 0;
        for (int i = 0; i < steps.length; i++) {
            if (steps[i].equals(s)) active = i;
        }
        if ("CANCELLED".equals(s)) return "Trạng thái: Đơn hàng đã huỷ";
        StringBuilder sb = new StringBuilder("Theo dõi: ");
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) sb.append("  >  ");
            sb.append(i <= active ? "[x] " : "[ ] ").append(labels[i]);
        }
        return sb.toString();
    }
}
