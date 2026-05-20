package com.example.my_mobile_app.ui.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.CartService;
import com.example.my_mobile_app.api.OrderService;
import com.example.my_mobile_app.api.PaymentService;
import com.example.my_mobile_app.api.dto.CreateMoMoPaymentRequest;
import com.example.my_mobile_app.api.dto.CreateOrderRequest;
import com.example.my_mobile_app.model.CartItem;
import com.example.my_mobile_app.model.District;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.model.Province;
import com.example.my_mobile_app.model.ShippingFee;
import com.example.my_mobile_app.model.Ward;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.payment.OrderStatusActivity;
import com.example.my_mobile_app.ui.payment.PaymentSuccessActivity;
import com.example.my_mobile_app.util.PriceFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Checkout screen. Address spinners (province → district → ward) feed
 * {@code calculateShippingFee}. Submit calls {@code createOrder()}, then either
 * routes to PaymentSuccess (COD) or opens MoMo payUrl + OrderStatusActivity.
 *
 * Note: cart ASSET items cannot be ordered via this flow because the backend
 * {@code CreateOrderRequest.Item} only carries {@code productId}. ASSET items
 * are filtered out and a message is shown if no PRODUCT items remain.
 */
public class CheckoutActivity extends BaseActivity {

    private Spinner spProvince, spDistrict, spWard;
    private EditText etStreet, etNote;
    private RadioGroup rgPayment;
    private TextView txtSubtotal, txtShippingFee, txtTotal;
    private LinearLayout summaryItems;
    private MaterialButton btnPlaceOrder;

    private List<Province> provinces = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private List<Ward> wards = new ArrayList<>();

    private Province selProvince;
    private District selDistrict;
    private Ward selWard;

    private final List<CartItem> cartItems = new ArrayList<>();
    private double subtotal;
    private double shippingFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_checkout);

        spProvince = findViewById(R.id.sp_province);
        spDistrict = findViewById(R.id.sp_district);
        spWard = findViewById(R.id.sp_ward);
        etStreet = findViewById(R.id.et_street);
        etNote = findViewById(R.id.et_note);
        rgPayment = findViewById(R.id.rg_payment);
        txtSubtotal = findViewById(R.id.txt_subtotal);
        txtShippingFee = findViewById(R.id.txt_shipping_fee);
        txtTotal = findViewById(R.id.txt_total);
        summaryItems = findViewById(R.id.summary_items);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= provinces.size()) return;
                selProvince = provinces.get(position);
                selDistrict = null; selWard = null;
                loadDistricts(selProvince.provinceId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= districts.size()) return;
                selDistrict = districts.get(position);
                selWard = null;
                loadWards(selDistrict.districtId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= wards.size()) return;
                selWard = wards.get(position);
                recalculateShipping();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadProvinces();
        loadCart();
    }

    private void loadProvinces() {
        ApiClient.get(this).create(PaymentService.class).getProvinces()
                .enqueue(new Callback<ApiResponse<List<Province>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Province>>> call, @NonNull Response<ApiResponse<List<Province>>> response) {
                        ApiResponse<List<Province>> b = response.body();
                        if (b == null || !b.success || b.data == null) return;
                        provinces = b.data;
                        List<String> names = new ArrayList<>();
                        for (Province p : provinces) names.add(p.provinceName);
                        spProvince.setAdapter(buildSpinnerAdapter(names));
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Province>>> call, @NonNull Throwable t) {
                        showError("Lỗi tải tỉnh/thành");
                    }
                });
    }

    private void loadDistricts(String provinceId) {
        spDistrict.setEnabled(false);
        spWard.setEnabled(false);
        ApiClient.get(this).create(PaymentService.class).getDistricts(provinceId)
                .enqueue(new Callback<ApiResponse<List<District>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<District>>> call, @NonNull Response<ApiResponse<List<District>>> response) {
                        ApiResponse<List<District>> b = response.body();
                        if (b == null || !b.success || b.data == null) return;
                        districts = b.data;
                        List<String> names = new ArrayList<>();
                        for (District d : districts) names.add(d.districtName);
                        spDistrict.setAdapter(buildSpinnerAdapter(names));
                        spDistrict.setEnabled(true);
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<District>>> call, @NonNull Throwable t) {}
                });
    }

    private void loadWards(String districtId) {
        spWard.setEnabled(false);
        ApiClient.get(this).create(PaymentService.class).getWards(districtId)
                .enqueue(new Callback<ApiResponse<List<Ward>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Ward>>> call, @NonNull Response<ApiResponse<List<Ward>>> response) {
                        ApiResponse<List<Ward>> b = response.body();
                        if (b == null || !b.success || b.data == null) return;
                        wards = b.data;
                        List<String> names = new ArrayList<>();
                        for (Ward w : wards) names.add(w.wardName);
                        spWard.setAdapter(buildSpinnerAdapter(names));
                        spWard.setEnabled(true);
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Ward>>> call, @NonNull Throwable t) {}
                });
    }

    private void loadCart() {
        ApiClient.get(this).create(CartService.class).getCartItems()
                .enqueue(new Callback<ApiResponse<List<CartItem>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Response<ApiResponse<List<CartItem>>> response) {
                        cartItems.clear();
                        ApiResponse<List<CartItem>> b = response.body();
                        if (b != null && b.success && b.data != null) cartItems.addAll(b.data);
                        renderSummary();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Throwable t) {
                        showError("Lỗi tải giỏ hàng");
                    }
                });
    }

    private void renderSummary() {
        summaryItems.removeAllViews();
        subtotal = 0;
        for (CartItem c : cartItems) {
            double price = c.price == null ? 0 : c.price;
            int qty = Math.max(1, c.quantity);
            subtotal += price * qty;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 6, 0, 6);

            TextView name = new TextView(this);
            String label = "PRODUCT".equals(c.type) ? c.productName : c.assetName;
            name.setText("• " + (label == null ? "" : label) + " × " + qty);
            name.setTextColor(getColor(R.color.white));
            name.setTextSize(13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(name, lp);

            TextView priceTv = new TextView(this);
            priceTv.setText(PriceFormatter.format(price * qty));
            priceTv.setTextColor(getColor(R.color.orange));
            priceTv.setTextSize(13);
            row.addView(priceTv);

            summaryItems.addView(row);
        }
        if (summaryItems.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Giỏ hàng trống");
            empty.setTextColor(getColor(R.color.text_muted));
            summaryItems.addView(empty);
        }
        updateTotals();
    }

    private void updateTotals() {
        txtSubtotal.setText(PriceFormatter.format(subtotal));
        txtShippingFee.setText(PriceFormatter.format(shippingFee));
        txtTotal.setText(PriceFormatter.format(subtotal + shippingFee));
    }

    private void recalculateShipping() {
        if (selDistrict == null || selWard == null) return;
        Map<String, Object> body = new HashMap<>();
        body.put("toDistrict", selDistrict.districtId);
        body.put("toWard", selWard.wardCode);
        body.put("weight", 1500);
        body.put("insuranceValue", subtotal);
        ApiClient.get(this).create(PaymentService.class).calculateShippingFee(body)
                .enqueue(new Callback<ApiResponse<ShippingFee>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<ShippingFee>> call, @NonNull Response<ApiResponse<ShippingFee>> response) {
                        ApiResponse<ShippingFee> b = response.body();
                        if (b != null && b.success && b.data != null) {
                            shippingFee = b.data.shippingFee;
                            updateTotals();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<ShippingFee>> call, @NonNull Throwable t) {}
                });
    }

    private ArrayAdapter<String> buildSpinnerAdapter(List<String> names) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void placeOrder() {
        if (selProvince == null || selDistrict == null || selWard == null) {
            showError("Vui lòng chọn đầy đủ địa chỉ");
            return;
        }
        String street = etStreet.getText().toString().trim();
        if (TextUtils.isEmpty(street)) {
            showError("Vui lòng nhập số nhà, tên đường");
            return;
        }
        // Filter PRODUCT items only — order endpoint cannot handle ASSET items.
        List<CreateOrderRequest.Item> orderItems = new ArrayList<>();
        for (CartItem c : cartItems) {
            if ("PRODUCT".equals(c.type) && c.productId != null) {
                orderItems.add(new CreateOrderRequest.Item(c.productId, c.quantity));
            }
        }
        if (orderItems.isEmpty()) {
            showError("Giỏ không có sản phẩm để đặt. Thiết bị thuê hãy đặt từ trang chi tiết.");
            return;
        }

        String note = etNote.getText().toString().trim();
        StringBuilder addr = new StringBuilder();
        addr.append(street).append(", ").append(selWard.wardName)
                .append(", ").append(selDistrict.districtName)
                .append(", ").append(selProvince.provinceName);
        if (!note.isEmpty()) addr.append(" (").append(note).append(")");

        boolean useMomo = rgPayment.getCheckedRadioButtonId() == R.id.rb_momo;
        String paymentMethod = useMomo ? "MoMo" : "COD";

        CreateOrderRequest req = new CreateOrderRequest(addr.toString(), paymentMethod, orderItems);
        req.shippingFee = shippingFee;

        btnPlaceOrder.setEnabled(false);
        showLoading();
        ApiClient.get(this).create(OrderService.class).createOrder(req)
                .enqueue(new Callback<ApiResponse<Order>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                        hideLoading();
                        btnPlaceOrder.setEnabled(true);
                        ApiResponse<Order> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(b != null && b.message != null ? b.message : "Đặt hàng thất bại");
                            return;
                        }
                        Order order = b.data;
                        if (useMomo) {
                            createMomoPayment(order);
                        } else {
                            Intent i = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                            i.putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, order.orderId);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                        hideLoading();
                        btnPlaceOrder.setEnabled(true);
                        showError("Lỗi kết nối khi đặt hàng");
                    }
                });
    }

    private void createMomoPayment(Order order) {
        double total = subtotal + shippingFee;
        CreateMoMoPaymentRequest req = new CreateMoMoPaymentRequest(
                order.orderId, total, "Thanh toán đơn hàng: " + order.orderId);
        req.requestType = "captureWallet";
        showLoading();
        ApiClient.get(this).create(PaymentService.class).createMoMoPayment(req)
                .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, String>>> call, @NonNull Response<ApiResponse<Map<String, String>>> response) {
                        hideLoading();
                        ApiResponse<Map<String, String>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError("Không tạo được thanh toán MoMo");
                            return;
                        }
                        String payUrl = b.data.get("payUrl");
                        String orderCode = b.data.get("orderId");
                        if (orderCode == null) orderCode = order.orderId;
                        if (payUrl != null && !payUrl.isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl)));
                        }
                        Intent i = new Intent(CheckoutActivity.this, OrderStatusActivity.class);
                        i.putExtra(OrderStatusActivity.EXTRA_ORDER_CODE, orderCode);
                        i.putExtra(OrderStatusActivity.EXTRA_ORDER_ID, order.orderId);
                        startActivity(i);
                        finish();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, String>>> call, @NonNull Throwable t) {
                        hideLoading();
                        showError("Lỗi kết nối khi tạo thanh toán MoMo");
                    }
                });
    }
}
