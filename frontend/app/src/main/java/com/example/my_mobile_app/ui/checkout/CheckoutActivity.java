package com.example.my_mobile_app.ui.checkout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
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
import com.example.my_mobile_app.api.RentalService;
import com.example.my_mobile_app.api.dto.CreateMoMoPaymentRequest;
import com.example.my_mobile_app.api.dto.CreateOrderRequest;
import com.example.my_mobile_app.api.dto.CreateRentalRequest;
import com.example.my_mobile_app.model.CartItem;
import com.example.my_mobile_app.model.District;
import com.example.my_mobile_app.model.Order;
import com.example.my_mobile_app.model.Province;
import com.example.my_mobile_app.model.Rental;
import com.example.my_mobile_app.model.ShippingFee;
import com.example.my_mobile_app.model.Ward;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String PREFS_ADDRESSES = "checkout_addresses";
    private static final String KEY_SAVED_ADDRESSES = "saved_addresses";
    private static final int MAX_SAVED_ADDRESSES = 5;

    private Spinner spSavedAddress, spProvince, spDistrict, spWard;
    private EditText etStreet, etNote;
    private RadioGroup rgPayment;
    private TextView txtSubtotal, txtShippingFee, txtTotal, txtRentalDays, txtRentalDeposit;
    private LinearLayout summaryItems, rentalPeriodSection, rentalDepositRow;
    private MaterialButton btnPlaceOrder, btnRentalStartDate, btnRentalEndDate;

    private List<Province> provinces = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private List<Ward> wards = new ArrayList<>();
    private final List<SavedAddress> savedAddresses = new ArrayList<>();

    private Province selProvince;
    private District selDistrict;
    private Ward selWard;

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
    private SavedAddress pendingSavedAddress;
    private boolean loadingProvinces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin())
            return;
        setContentView(R.layout.activity_checkout);

        spSavedAddress = findViewById(R.id.sp_saved_address);
        spProvince = findViewById(R.id.sp_province);
        spDistrict = findViewById(R.id.sp_district);
        spWard = findViewById(R.id.sp_ward);
        etStreet = findViewById(R.id.et_street);
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

        loadSavedAddresses();
        spSavedAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position - 1 >= savedAddresses.size())
                    return;
                applySavedAddress(savedAddresses.get(position - 1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position - 1 >= provinces.size()) {
                    selProvince = null;
                    selDistrict = null;
                    selWard = null;
                    setDistrictOptions(new ArrayList<>());
                    setWardOptions(new ArrayList<>());
                    return;
                }
                selProvince = provinces.get(position - 1);
                selDistrict = null;
                selWard = null;
                loadDistricts(selProvince.provinceId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position - 1 >= districts.size()) {
                    selDistrict = null;
                    selWard = null;
                    setWardOptions(new ArrayList<>());
                    return;
                }
                selDistrict = districts.get(position - 1);
                selWard = null;
                loadWards(selDistrict.districtId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position - 1 >= wards.size()) {
                    selWard = null;
                    return;
                }
                selWard = wards.get(position - 1);
                recalculateShipping();
                pendingSavedAddress = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Show placeholders immediately so the address fields aren't blank before
        // the API responds (or if it fails). Tapping province retries the load.
        setProvinceOptions();
        setDistrictOptions(new ArrayList<>());
        setWardOptions(new ArrayList<>());
        spProvince.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && provinces.isEmpty()) {
                loadProvinces();
            }
            return false;
        });

        loadProvinces();
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

    private void loadSavedAddresses() {
        savedAddresses.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_ADDRESSES, MODE_PRIVATE);
        String raw = prefs.getString(KEY_SAVED_ADDRESSES, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                savedAddresses.add(SavedAddress.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException ignored) {
            savedAddresses.clear();
        }
        renderSavedAddressOptions();
    }

    private void renderSavedAddressOptions() {
        List<String> names = new ArrayList<>();
        names.add(savedAddresses.isEmpty()
                ? getString(R.string.checkout_no_saved_address)
                : getString(R.string.checkout_select_saved_address));
        for (int i = 0; i < savedAddresses.size(); i++) {
            names.add(getString(R.string.checkout_saved_address_number,
                    i + 1, savedAddresses.get(i).label()));
        }
        spSavedAddress.setAdapter(buildSpinnerAdapter(names));
        spSavedAddress.setEnabled(!savedAddresses.isEmpty());
    }

    private void applySavedAddress(SavedAddress address) {
        pendingSavedAddress = address;
        etStreet.setText(address.street);
        etNote.setText(address.note);
        if (provinces.isEmpty()) {
            return;
        }
        selectProvince(address.provinceId);
    }

    private void saveCurrentAddress() {
        if (selProvince == null || selDistrict == null || selWard == null)
            return;
        String street = etStreet.getText().toString().trim();
        if (TextUtils.isEmpty(street))
            return;

        SavedAddress address = new SavedAddress(
                selProvince.provinceId,
                selProvince.provinceName,
                selDistrict.districtId,
                selDistrict.districtName,
                selWard.wardCode,
                selWard.wardName,
                street,
                etNote.getText().toString().trim());

        for (int i = savedAddresses.size() - 1; i >= 0; i--) {
            if (address.samePlaceAndStreet(savedAddresses.get(i))) {
                savedAddresses.remove(i);
            }
        }
        savedAddresses.add(0, address);
        while (savedAddresses.size() > MAX_SAVED_ADDRESSES) {
            savedAddresses.remove(savedAddresses.size() - 1);
        }

        JSONArray arr = new JSONArray();
        for (SavedAddress saved : savedAddresses) {
            arr.put(saved.toJson());
        }
        getSharedPreferences(PREFS_ADDRESSES, MODE_PRIVATE)
                .edit()
                .putString(KEY_SAVED_ADDRESSES, arr.toString())
                .apply();
        renderSavedAddressOptions();
    }

    private void loadProvinces() {
        if (loadingProvinces) {
            return;
        }
        loadingProvinces = true;
        ApiClient.get(this).create(PaymentService.class).getProvinces()
                .enqueue(new Callback<ApiResponse<List<Province>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Province>>> call,
                            @NonNull Response<ApiResponse<List<Province>>> response) {
                        loadingProvinces = false;
                        ApiResponse<List<Province>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(getString(R.string.error_load_provinces));
                            return;
                        }
                        provinces = b.data;
                        setProvinceOptions();
                        if (pendingSavedAddress != null) {
                            selectProvince(pendingSavedAddress.provinceId);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Province>>> call, @NonNull Throwable t) {
                        loadingProvinces = false;
                        showError(getString(R.string.error_load_address_connection));
                    }
                });
    }

    private void loadDistricts(String provinceId) {
        spDistrict.setEnabled(false);
        spWard.setEnabled(false);
        setDistrictOptions(new ArrayList<>());
        setWardOptions(new ArrayList<>());
        ApiClient.get(this).create(PaymentService.class).getDistricts(provinceId)
                .enqueue(new Callback<ApiResponse<List<District>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<District>>> call,
                            @NonNull Response<ApiResponse<List<District>>> response) {
                        ApiResponse<List<District>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError("Backend error: cannot load districts");
                            return;
                        }
                        districts = b.data;
                        setDistrictOptions(districts);
                        if (pendingSavedAddress != null) {
                            selectDistrict(pendingSavedAddress.districtId);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<District>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_address_connection));
                    }
                });
    }

    private void loadWards(String districtId) {
        spWard.setEnabled(false);
        setWardOptions(new ArrayList<>());
        ApiClient.get(this).create(PaymentService.class).getWards(districtId)
                .enqueue(new Callback<ApiResponse<List<Ward>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Ward>>> call,
                            @NonNull Response<ApiResponse<List<Ward>>> response) {
                        ApiResponse<List<Ward>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError("Backend error: cannot load wards");
                            return;
                        }
                        wards = b.data;
                        setWardOptions(wards);
                        if (pendingSavedAddress != null) {
                            selectWard(pendingSavedAddress.wardCode);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Ward>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_address_connection));
                    }
                });
    }

    private void setProvinceOptions() {
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.checkout_select_province));
        for (Province p : provinces)
            names.add(p.provinceName);
        spProvince.setAdapter(buildSpinnerAdapter(names));
    }

    private void setDistrictOptions(List<District> values) {
        districts = values;
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.checkout_select_district));
        for (District d : districts)
            names.add(d.districtName);
        spDistrict.setAdapter(buildSpinnerAdapter(names));
        spDistrict.setEnabled(!districts.isEmpty());
    }

    private void setWardOptions(List<Ward> values) {
        wards = values;
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.checkout_select_ward));
        for (Ward w : wards)
            names.add(w.wardName);
        spWard.setAdapter(buildSpinnerAdapter(names));
        spWard.setEnabled(!wards.isEmpty());
    }

    private void selectProvince(String provinceId) {
        if (provinceId == null) return;
        for (int i = 0; i < provinces.size(); i++) {
            if (provinceId.equals(provinces.get(i).provinceId)) {
                if (spProvince.getSelectedItemPosition() == i + 1) {
                    selProvince = provinces.get(i);
                    loadDistricts(selProvince.provinceId);
                } else {
                    spProvince.setSelection(i + 1);
                }
                return;
            }
        }
    }

    private void selectDistrict(String districtId) {
        if (districtId == null) return;
        for (int i = 0; i < districts.size(); i++) {
            if (districtId.equals(districts.get(i).districtId)) {
                if (spDistrict.getSelectedItemPosition() == i + 1) {
                    selDistrict = districts.get(i);
                    loadWards(selDistrict.districtId);
                } else {
                    spDistrict.setSelection(i + 1);
                }
                return;
            }
        }
    }

    private void selectWard(String wardCode) {
        if (wardCode == null) return;
        for (int i = 0; i < wards.size(); i++) {
            if (wardCode.equals(wards.get(i).wardCode)) {
                if (spWard.getSelectedItemPosition() == i + 1) {
                    selWard = wards.get(i);
                    recalculateShipping();
                    pendingSavedAddress = null;
                } else {
                    spWard.setSelection(i + 1);
                }
                return;
            }
        }
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
        name.setTextColor(getColor(R.color.white));
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

    private void recalculateShipping() {
        if (selDistrict == null || selWard == null)
            return;
        Map<String, Object> body = new HashMap<>();
        body.put("toDistrict", selDistrict.districtId);
        body.put("toWard", selWard.wardCode);
        body.put("weight", 1500);
        body.put("insuranceValue", subtotal + rentalDeposit());
        ApiClient.get(this).create(PaymentService.class).calculateShippingFee(body)
                .enqueue(new Callback<ApiResponse<ShippingFee>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ShippingFee>> call,
                            @NonNull Response<ApiResponse<ShippingFee>> response) {
                        ApiResponse<ShippingFee> b = response.body();
                        if (b != null && b.success && b.data != null) {
                            shippingFee = b.data.shippingFee;
                            updateTotals();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ShippingFee>> call, @NonNull Throwable t) {
                    }
                });
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
            recalculateShipping();
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

    private ArrayAdapter<String> buildSpinnerAdapter(List<String> names) {
        ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                styleSpinnerText(tv, position);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                styleSpinnerText(tv, position);
                tv.setBackgroundColor(getColor(R.color.bg_dark_secondary));
                return tv;
            }
        };
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void styleSpinnerText(TextView tv, int position) {
        tv.setTextColor(position == 0 ? getColor(R.color.text_muted) : getColor(R.color.white));
        tv.setTextSize(14);
        tv.setSingleLine(false);
        tv.setPadding(12, 0, 12, 0);
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
        if (selProvince == null || selDistrict == null || selWard == null) {
            showError(getString(R.string.error_select_full_address));
            return;
        }
        String street = etStreet.getText().toString().trim();
        if (TextUtils.isEmpty(street)) {
            showError(getString(R.string.error_enter_street));
            return;
        }
        String address = buildAddress(street);
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
                        saveCurrentAddress();
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

    private String buildAddress(String street) {
        String note = etNote.getText().toString().trim();
        StringBuilder addr = new StringBuilder();
        addr.append(street).append(", ").append(selWard.wardName)
                .append(", ").append(selDistrict.districtName)
                .append(", ").append(selProvince.provinceName);
        if (!note.isEmpty())
            addr.append(" (").append(note).append(")");
        return addr.toString();
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
                        saveCurrentAddress();
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

    private static class SavedAddress {
        final String provinceId;
        final String provinceName;
        final String districtId;
        final String districtName;
        final String wardCode;
        final String wardName;
        final String street;
        final String note;

        SavedAddress(String provinceId, String provinceName, String districtId, String districtName,
                     String wardCode, String wardName, String street, String note) {
            this.provinceId = provinceId;
            this.provinceName = provinceName;
            this.districtId = districtId;
            this.districtName = districtName;
            this.wardCode = wardCode;
            this.wardName = wardName;
            this.street = street;
            this.note = note;
        }

        static SavedAddress fromJson(JSONObject obj) {
            return new SavedAddress(
                    obj.optString("provinceId"),
                    obj.optString("provinceName"),
                    obj.optString("districtId"),
                    obj.optString("districtName"),
                    obj.optString("wardCode"),
                    obj.optString("wardName"),
                    obj.optString("street"),
                    obj.optString("note"));
        }

        JSONObject toJson() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("provinceId", provinceId);
                obj.put("provinceName", provinceName);
                obj.put("districtId", districtId);
                obj.put("districtName", districtName);
                obj.put("wardCode", wardCode);
                obj.put("wardName", wardName);
                obj.put("street", street);
                obj.put("note", note);
            } catch (JSONException ignored) {
            }
            return obj;
        }

        String label() {
            return street + ", " + wardName + ", " + districtName + ", " + provinceName;
        }

        boolean samePlaceAndStreet(SavedAddress other) {
            return provinceId.equals(other.provinceId)
                    && districtId.equals(other.districtId)
                    && wardCode.equals(other.wardCode)
                    && street.equalsIgnoreCase(other.street);
        }
    }
}
