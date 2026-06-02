package com.example.my_mobile_app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AddressService;
import com.example.my_mobile_app.model.Address;
import com.example.my_mobile_app.ui.BaseActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Address book screen. Two modes:
 *  - manage (default): list saved addresses, add / edit / delete / set default.
 *  - pick ({@code EXTRA_PICK_MODE} = true): tapping a row returns it to the caller so checkout
 *    can prefill without re-entering the address.
 */
public class AddressBookActivity extends BaseActivity {

    public static final String EXTRA_PICK_MODE = "pick_mode";
    // Result extras carrying the chosen address back to the caller.
    public static final String RESULT_RECIPIENT_NAME = "recipient_name";
    public static final String RESULT_RECIPIENT_PHONE = "recipient_phone";
    public static final String RESULT_PROVINCE_NAME = "province_name";
    public static final String RESULT_DISTRICT_ID = "district_id";
    public static final String RESULT_DISTRICT_NAME = "district_name";
    public static final String RESULT_WARD_CODE = "ward_code";
    public static final String RESULT_WARD_NAME = "ward_name";
    public static final String RESULT_STREET = "street";
    public static final String RESULT_NOTE = "note";
    public static final String RESULT_FULL_ADDRESS = "full_address";

    private LinearLayout listContainer;
    private TextView txtEmpty;
    private boolean pickMode;
    private final List<Address> addresses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_address_book);

        pickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);
        listContainer = findViewById(R.id.list_addresses);
        txtEmpty = findViewById(R.id.txt_addresses_empty);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        MaterialButton btnAdd = findViewById(R.id.btn_add_address);
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddressFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    private void loadAddresses() {
        ApiClient.get(this).create(AddressService.class).getAddresses()
                .enqueue(new Callback<ApiResponse<List<Address>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Address>>> call,
                                                     @NonNull Response<ApiResponse<List<Address>>> response) {
                        ApiResponse<List<Address>> b = response.body();
                        addresses.clear();
                        if (b != null && b.success && b.data != null) {
                            addresses.addAll(b.data);
                        }
                        renderList();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Address>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_addresses));
                    }
                });
    }

    private void renderList() {
        listContainer.removeAllViews();
        txtEmpty.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Address address : addresses) {
            View row = inflater.inflate(R.layout.item_address, listContainer, false);

            ((TextView) row.findViewById(R.id.txt_address_recipient))
                    .setText(getString(R.string.address_recipient_line,
                            safe(address.recipientName), safe(address.recipientPhone)));
            ((TextView) row.findViewById(R.id.txt_address_detail)).setText(address.fullAddress());
            row.findViewById(R.id.badge_default)
                    .setVisibility(address.isDefault ? View.VISIBLE : View.GONE);

            MaterialButton btnDefault = row.findViewById(R.id.btn_address_default);
            btnDefault.setVisibility(address.isDefault ? View.GONE : View.VISIBLE);
            btnDefault.setOnClickListener(v -> setDefault(address));

            row.findViewById(R.id.btn_address_edit).setOnClickListener(v -> openEdit(address));
            row.findViewById(R.id.btn_address_delete).setOnClickListener(v -> confirmDelete(address));

            if (pickMode) {
                row.setOnClickListener(v -> returnPicked(address));
            }
            listContainer.addView(row);
        }
    }

    private void returnPicked(Address a) {
        Intent result = new Intent();
        result.putExtra(RESULT_RECIPIENT_NAME, a.recipientName);
        result.putExtra(RESULT_RECIPIENT_PHONE, a.recipientPhone);
        result.putExtra(RESULT_PROVINCE_NAME, a.provinceName);
        result.putExtra(RESULT_DISTRICT_ID, a.districtId);
        result.putExtra(RESULT_DISTRICT_NAME, a.districtName);
        result.putExtra(RESULT_WARD_CODE, a.wardCode);
        result.putExtra(RESULT_WARD_NAME, a.wardName);
        result.putExtra(RESULT_STREET, a.street);
        result.putExtra(RESULT_NOTE, a.note);
        result.putExtra(RESULT_FULL_ADDRESS, a.fullAddress());
        setResult(RESULT_OK, result);
        finish();
    }

    private void openEdit(Address a) {
        Intent i = new Intent(this, AddressFormActivity.class);
        i.putExtra(AddressFormActivity.EXTRA_ADDRESS_ID, a.addressId);
        i.putExtra(AddressFormActivity.EXTRA_RECIPIENT_NAME, a.recipientName);
        i.putExtra(AddressFormActivity.EXTRA_RECIPIENT_PHONE, a.recipientPhone);
        i.putExtra(AddressFormActivity.EXTRA_PROVINCE_ID, a.provinceId);
        i.putExtra(AddressFormActivity.EXTRA_DISTRICT_ID, a.districtId);
        i.putExtra(AddressFormActivity.EXTRA_WARD_CODE, a.wardCode);
        i.putExtra(AddressFormActivity.EXTRA_STREET, a.street);
        i.putExtra(AddressFormActivity.EXTRA_NOTE, a.note);
        i.putExtra(AddressFormActivity.EXTRA_IS_DEFAULT, a.isDefault);
        startActivity(i);
    }

    private void confirmDelete(Address a) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.address_delete)
                .setMessage(R.string.address_delete_confirm)
                .setPositiveButton(R.string.address_delete, (d, w) -> deleteAddress(a))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void deleteAddress(Address a) {
        ApiClient.get(this).create(AddressService.class).deleteAddress(a.addressId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                                     @NonNull Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> b = response.body();
                        if (b != null && b.success) {
                            showSuccess(getString(R.string.address_deleted));
                            loadAddresses();
                        } else {
                            showError(b != null && b.message != null ? b.message
                                    : getString(R.string.error_save_address));
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_save_address));
                    }
                });
    }

    private void setDefault(Address a) {
        ApiClient.get(this).create(AddressService.class).setDefault(a.addressId)
                .enqueue(new Callback<ApiResponse<Address>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Address>> call,
                                                     @NonNull Response<ApiResponse<Address>> response) {
                        ApiResponse<Address> b = response.body();
                        if (b != null && b.success) {
                            loadAddresses();
                        } else {
                            showError(b != null && b.message != null ? b.message
                                    : getString(R.string.error_save_address));
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Address>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_save_address));
                    }
                });
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
