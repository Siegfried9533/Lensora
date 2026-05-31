package com.example.my_mobile_app.ui.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.CartService;
import com.example.my_mobile_app.api.OrderService;
import com.example.my_mobile_app.api.PaymentService;
import com.example.my_mobile_app.api.RentalService;
import com.example.my_mobile_app.api.dto.CreateMoMoPaymentRequest;
import com.example.my_mobile_app.api.dto.CreateOrderRequest;
import com.example.my_mobile_app.api.dto.CreateRentalRequest;
import com.example.my_mobile_app.model.CartItem;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.payment.OrderStatusActivity;
import com.example.my_mobile_app.ui.payment.PaymentSuccessActivity;
import com.example.my_mobile_app.util.PriceFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Checkout screen. Customers enter their shipping address directly. Submit
 * calls {@code createOrder()}, then either routes to PaymentSuccess (COD) or
 * opens MoMo payUrl + OrderStatusActivity.
 *
 * Note: cart ASSET items cannot be ordered via this flow because the backend
 * {@code CreateOrderRequest.Item} only carries {@code productId}. ASSET items
 * are filtered out and a message is shown if no PRODUCT items remain.
 */
public class CheckoutActivity extends BaseActivity {

    public static final String EXTRA_MODE = "checkout_mode";
    public static final String MODE_CART = "CART";
    public static final String MODE_PRODUCT = "PRODUCT";
    public static final String MODE_RENTAL = "RENTAL";
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_ITEM_NAME = "item_name";
    public static final String EXTRA_ITEM_PRICE = "item_price";
    public static final String EXTRA_ITEM_QUANTITY = "item_quantity";
    public static final String EXTRA_RENTAL_START_MILLIS = "rental_start_millis";
    public static final String EXTRA_RENTAL_END_MILLIS = "rental_end_millis";

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;
    private static final SimpleDateFormat DISPLAY_DATE_FMT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private static final SimpleDateFormat API_DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private EditText etProvince, etDistrict, etWard, etStreet, etPostalCode, etNote;
    private RadioGroup rgPayment;
    private TextView txtSubtotal, txtShippingFee, txtTotal, txtRentalDays, txtRentalDeposit;
    private LinearLayout summaryItems, rentalPeriodSection, rentalDepositRow;
    private MaterialButton btnPlaceOrder, btnRentalStartDate, btnRentalEndDate;

    private final List<CartItem> cartItems = new ArrayList<>();
    private double subtotal;
    private double shippingFee;
    private String checkoutMode = MODE_CART;
    private String directItemId;
    private String directItemName;
    private double directItemPrice;
    private int directItemQuantity = 1;
    private long rentalStartMillis;
    private long rentalEndMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin())
            return;
        setContentView(R.layout.activity_checkout);

        etProvince = findViewById(R.id.et_province);
        etDistrict = findViewById(R.id.et_district);
        etWard = findViewById(R.id.et_ward);
        etStreet = findViewById(R.id.et_street);
        etPostalCode = findViewById(R.id.et_postal_code);
        etNote = findViewById(R.id.et_note);
        rgPayment = findViewById(R.id.rg_payment);
        txtSubtotal = findViewById(R.id.txt_subtotal);
        txtShippingFee = findViewById(R.id.txt_shipping_fee);
        txtTotal = findViewById(R.id.txt_total);
        txtRentalDays = findViewById(R.id.txt_rental_days);
        txtRentalDeposit = findViewById(R.id.txt_rental_deposit);
        summaryItems = findViewById(R.id.summary_items);
        rentalPeriodSection = findViewById(R.id.rental_period_section);
        rentalDepositRow = findViewById(R.id.rental_deposit_row);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        btnRentalStartDate = findViewById(R.id.btn_rental_start_date);
        btnRentalEndDate = findViewById(R.id.btn_rental_end_date);

        readCheckoutMode();
        rentalPeriodSection.setVisibility(isRentalCheckout() ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setText(isRentalCheckout()
                ? R.string.checkout_place_rental
                : R.string.checkout_place_order);
        configurePaymentOptions();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        btnRentalStartDate.setOnClickListener(v -> pickRentalDate(true));
        btnRentalEndDate.setOnClickListener(v -> pickRentalDate(false));

        if (isCartCheckout()) {
            loadCart();
        } else {
            renderSummary();
        }
    }

    private void readCheckoutMode() {
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        checkoutMode = mode == null ? MODE_CART : mode;
        if (!MODE_PRODUCT.equals(checkoutMode) && !MODE_RENTAL.equals(checkoutMode)) {
            checkoutMode = MODE_CART;
        }

        directItemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        directItemName = getIntent().getStringExtra(EXTRA_ITEM_NAME);
        directItemPrice = getIntent().getDoubleExtra(EXTRA_ITEM_PRICE, 0);
        directItemQuantity = Math.max(1, getIntent().getIntExtra(EXTRA_ITEM_QUANTITY, 1));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long today = c.getTimeInMillis();
        rentalStartMillis = getIntent().getLongExtra(EXTRA_RENTAL_START_MILLIS, today);
        rentalEndMillis = getIntent().getLongExtra(EXTRA_RENTAL_END_MILLIS, today + DAY_MS);
        if (rentalEndMillis <= rentalStartMillis) {
            rentalEndMillis = rentalStartMillis + DAY_MS;
        }
    }

    private boolean isCartCheckout() {
        return MODE_CART.equals(checkoutMode);
    }

    private boolean isRentalCheckout() {
        return MODE_RENTAL.equals(checkoutMode);
    }

    private void loadCart() {
        ApiClient.get(this).create(CartService.class).getCartItems()
                .enqueue(new Callback<ApiResponse<List<CartItem>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<CartItem>>> call,
                            @NonNull Response<ApiResponse<List<CartItem>>> response) {
                        cartItems.clear();
                        ApiResponse<List<CartItem>> b = response.body();
                        if (b != null && b.success && b.data != null)
                            cartItems.addAll(b.data);
                        renderSummary();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<CartItem>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_cart));
                    }
                });
    }

    private void renderSummary() {
        summaryItems.removeAllViews();
        subtotal = 0;
        if (MODE_PRODUCT.equals(checkoutMode)) {
            subtotal = directItemPrice * directItemQuantity;
            addSummaryRow("• " + safeName(directItemName) + " × " + directItemQuantity, subtotal);
        } else if (isRentalCheckout()) {
            long days = rentalDays();
            subtotal = directItemPrice * days;
            updateRentalDateViews(days);
            addSummaryRow("• " + safeName(directItemName) + " × " + days + " "
                    + getString(R.string.checkout_days_suffix), subtotal);
        } else {
            for (CartItem c : cartItems) {
                double price = c.price == null ? 0 : c.price;
                int qty = Math.max(1, c.quantity);
                subtotal += price * qty;

                String label = "PRODUCT".equals(c.type) ? c.productName : c.assetName;
                addSummaryRow("• " + safeName(label) + " × " + qty, price * qty);
            }
        }
        if (summaryItems.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(R.string.cart_empty);
            empty.setTextColor(getColor(R.color.text_muted));
            summaryItems.addView(empty);
        }
        updateTotals();
    }

    private void addSummaryRow(String label, double amount) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 6, 0, 6);

        TextView name = new TextView(this);
        name.setText(label);
        name.setTextColor(getColor(R.color.text_primary));
        name.setTextSize(13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(name, lp);

        TextView priceTv = new TextView(this);
        priceTv.setText(PriceFormatter.format(amount));
        priceTv.setTextColor(getColor(R.color.orange));
        priceTv.setTextSize(13);
        row.addView(priceTv);

        summaryItems.addView(row);
    }

    private void updateTotals() {
        txtSubtotal.setText(PriceFormatter.format(subtotal));
        txtShippingFee.setText(PriceFormatter.format(shippingFee));
        double deposit = rentalDeposit();
        rentalDepositRow.setVisibility(isRentalCheckout() ? View.VISIBLE : View.GONE);
        txtRentalDeposit.setText(PriceFormatter.format(deposit));
        txtTotal.setText(PriceFormatter.format(subtotal + deposit + shippingFee));
    }

    private void pickRentalDate(boolean isStart) {
        long initial = isStart ? rentalStartMillis : rentalEndMillis;
        long minDate = isStart ? todayMillis() : rentalStartMillis + DAY_MS;

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minDate))
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStart
                        ? getString(R.string.equipment_select_start_date)
                        : getString(R.string.equipment_select_return_date))
                .setSelection(initial)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (isStart) {
                rentalStartMillis = selection;
                if (rentalEndMillis <= rentalStartMillis) {
                    rentalEndMillis = rentalStartMillis + DAY_MS;
                }
            } else {
                if (selection <= rentalStartMillis) {
                    showError(getString(R.string.error_return_after_start));
                    return;
                }
                rentalEndMillis = selection;
            }
            renderSummary();
        });
        picker.show(getSupportFragmentManager(), "checkout_rental_date_picker");
    }

    private long todayMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long rentalDays() {
        return Math.max(1, (rentalEndMillis - rentalStartMillis) / DAY_MS);
    }

    private double rentalDeposit() {
        return isRentalCheckout() ? directItemPrice * 3 : 0;
    }

    private void updateRentalDateViews(long days) {
        btnRentalStartDate.setText(getString(R.string.equipment_start_date_format,
                DISPLAY_DATE_FMT.format(new Date(rentalStartMillis))));
        btnRentalEndDate.setText(getString(R.string.equipment_end_date_format,
                DISPLAY_DATE_FMT.format(new Date(rentalEndMillis))));
        txtRentalDays.setText(getString(R.string.checkout_rental_days_format,
                (int) days, PriceFormatter.format(subtotal)));
    }

    private boolean hasValidRentalDates() {
        if (rentalStartMillis < todayMillis()) {
            showError(getString(R.string.error_rental_start_in_past));
            return false;
        }
        if (rentalEndMillis <= rentalStartMillis) {
            showError(getString(R.string.error_return_after_start));
            return false;
        }
        return true;
    }

    private String formatApiDate(long millis) {
        return API_DATE_FMT.format(new Date(millis));
    }

    private String safeName(String value) {
        return value == null ? "" : value;
    }

    /**
     * Buying offers COD + online; renting is pay-first, so COD is hidden and online
     * payment is preselected with an explanatory note.
     */
    private void configurePaymentOptions() {
        if (!isRentalCheckout()) {
            return;
        }
        View codOption = findViewById(R.id.rb_cod);
        if (codOption != null) {
            codOption.setVisibility(View.GONE);
        }
        android.widget.RadioButton momoOption = findViewById(R.id.rb_momo);
        if (momoOption != null) {
            momoOption.setChecked(true);
        }
        if (rgPayment.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) rgPayment.getParent();
            int idx = parent.indexOfChild(rgPayment) + 1;
            TextView note = new TextView(this);
            note.setText(R.string.checkout_rental_prepay_note);
            note.setTextColor(getColor(R.color.orange));
            note.setTextSize(13);
            note.setPadding(0, 8, 0, 0);
            parent.addView(note, idx);
        }
    }

    private void placeOrder() {
        String province = textOf(etProvince);
        if (TextUtils.isEmpty(province)) {
            showError(getString(R.string.error_enter_province));
            return;
        }
        String district = textOf(etDistrict);
        if (TextUtils.isEmpty(district)) {
            showError(getString(R.string.error_enter_district));
            return;
        }
        String ward = textOf(etWard);
        if (TextUtils.isEmpty(ward)) {
            showError(getString(R.string.error_enter_ward));
            return;
        }
        String street = textOf(etStreet);
        if (TextUtils.isEmpty(street)) {
            showError(getString(R.string.error_enter_street));
            return;
        }
        String address = buildAddress(street, ward, district, province);
        // Rentals are pay-first: COD is not offered, so always pay online.
        boolean useMomo = isRentalCheckout()
                || rgPayment.getCheckedRadioButtonId() == R.id.rb_momo;
        String paymentMethod = useMomo ? "MoMo" : "COD";

        if (isRentalCheckout()) {
            createRental(address, "MoMo", true);
            return;
        }

        List<CreateOrderRequest.Item> orderItems = new ArrayList<>();
        if (MODE_PRODUCT.equals(checkoutMode)) {
            if (TextUtils.isEmpty(directItemId)) {
                showError(getString(R.string.error_missing_item_info));
                return;
            }
            orderItems.add(new CreateOrderRequest.Item(directItemId, directItemQuantity));
        } else {
            // Filter PRODUCT items only — order endpoint cannot handle ASSET items.
            for (CartItem c : cartItems) {
                if ("PRODUCT".equals(c.type) && c.productId != null) {
                    orderItems.add(new CreateOrderRequest.Item(c.productId, c.quantity));
                }
            }
        }
        if (orderItems.isEmpty()) {
            showError(getString(R.string.error_empty_order_cart));
            return;
        }

        CreateOrderRequest req = new CreateOrderRequest(address, paymentMethod, orderItems);
        req.shippingFee = shippingFee;
        req.clearCart = isCartCheckout();

        btnPlaceOrder.setEnabled(false);
        showLoading();
        ApiClient.get(this).create(OrderService.class).createOrder(req)
                .enqueue(new Callback<ApiResponse<Order>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                            @NonNull Response<ApiResponse<Order>> response) {
                        hideLoading();
                        btnPlaceOrder.setEnabled(true);
                        ApiResponse<Order> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(b != null && b.message != null
                                    ? b.message
                                    : getString(R.string.error_place_order_failed));
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

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                        hideLoading();
                        btnPlaceOrder.setEnabled(true);
                        showError(getString(R.string.error_place_order_connection));
                    }
                });
    }

    private String buildAddress(String street, String ward, String district, String province) {
        String postalCode = textOf(etPostalCode);
        String note = textOf(etNote);
        StringBuilder addr = new StringBuilder();
        addr.append(street).append(", ").append(ward)
                .append(", ").append(district)
                .append(", ").append(province);
        if (!postalCode.isEmpty()) {
            addr.append(", ")
                    .append(getString(R.string.checkout_postal_code))
                    .append(": ")
                    .append(postalCode);
        }
        if (!note.isEmpty())
            addr.append(" (").append(note).append(")");
        return addr.toString();
    }

    private String textOf(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void createRental(String address, String paymentMethod, boolean useMomo) {
        if (TextUtils.isEmpty(directItemId)) {
            showError(getString(R.string.error_missing_item_info));
            return;
        }
        if (!hasValidRentalDates()) {
            return;
        }

        CreateRentalRequest req = new CreateRentalRequest(
                directItemId,
                formatApiDate(rentalStartMillis),
                formatApiDate(rentalEndMillis));
        req.shippingAddress = address;
        req.paymentMethod = paymentMethod;
        req.shippingFee = Math.round(shippingFee);

        btnPlaceOrder.setEnabled(false);
        showLoading();
        ApiClient.get(this).create(RentalService.class).createRental(req)
                .enqueue(new Callback<ApiResponse<Rental>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Rental>> call,
                            @NonNull Response<ApiResponse<Rental>> response) {
                        hideLoading();
                        btnPlaceOrder.setEnabled(true);
                        ApiResponse<Rental> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(b != null && b.message != null
                                    ? b.message
                                    : getString(R.string.error_place_rental_failed));
                            return;
                        }
                        Rental rental = b.data;
                        if (useMomo) {
                            createMomoPaymentRental(rental);
                        } else {
                            Intent i = new Intent(CheckoutActivity.this, PaymentSuccessActivity.class);
                            i.putExtra(PaymentSuccessActivity.EXTRA_RENTAL_ID, rental.rentalId);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Rental>> call, @NonNull Throwable t) {
                        hideLoading();
                        btnPlaceOrder.setEnabled(true);
                        showError(getString(R.string.error_place_rental_connection));
                    }
                });
    }

    private void createMomoPayment(Order order) {
        double total = subtotal + shippingFee;
        CreateMoMoPaymentRequest req = new CreateMoMoPaymentRequest(
                order.orderId, total, getString(R.string.checkout_momo_order_info, order.orderId));
        req.requestType = "captureWallet";
        showLoading();
        ApiClient.get(this).create(PaymentService.class).createMoMoPayment(req)
                .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Map<String, String>>> call,
                            @NonNull Response<ApiResponse<Map<String, String>>> response) {
                        hideLoading();
                        ApiResponse<Map<String, String>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(getString(R.string.error_create_momo));
                            return;
                        }
                        String payUrl = b.data.get("payUrl");
                        String orderCode = b.data.get("orderId");
                        if (orderCode == null)
                            orderCode = order.orderId;
                        if (payUrl != null && !payUrl.isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl)));
                        }
                        Intent i = new Intent(CheckoutActivity.this, OrderStatusActivity.class);
                        i.putExtra(OrderStatusActivity.EXTRA_ORDER_CODE, orderCode);
                        i.putExtra(OrderStatusActivity.EXTRA_ORDER_ID, order.orderId);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Map<String, String>>> call, @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_create_momo_connection));
                    }
                });
    }

    private void createMomoPaymentRental(Rental rental) {
        Map<String, String> req = new HashMap<>();
        req.put("rentalId", rental.rentalId);
        req.put("orderInfo", getString(R.string.checkout_momo_rental_info, rental.rentalId));

        showLoading();
        ApiClient.get(this).create(PaymentService.class).createMoMoPaymentRental(req)
                .enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Map<String, String>>> call,
                            @NonNull Response<ApiResponse<Map<String, String>>> response) {
                        hideLoading();
                        ApiResponse<Map<String, String>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(getString(R.string.error_create_momo));
                            return;
                        }
                        String payUrl = b.data.get("payUrl");
                        String orderCode = b.data.get("rentalId");
                        if (orderCode == null)
                            orderCode = rental.rentalId;
                        if (payUrl != null && !payUrl.isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl)));
                        }
                        Intent i = new Intent(CheckoutActivity.this, OrderStatusActivity.class);
                        i.putExtra(OrderStatusActivity.EXTRA_ORDER_CODE, orderCode);
                        i.putExtra(OrderStatusActivity.EXTRA_RENTAL_ID, rental.rentalId);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Map<String, String>>> call, @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.error_create_momo_connection));
                    }
                });
    }

}
