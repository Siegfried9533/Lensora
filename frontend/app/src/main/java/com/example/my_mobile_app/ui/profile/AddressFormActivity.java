package com.example.my_mobile_app.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AddressService;
import com.example.my_mobile_app.api.PaymentService;
import com.example.my_mobile_app.model.Address;
import com.example.my_mobile_app.model.District;
import com.example.my_mobile_app.model.Province;
import com.example.my_mobile_app.model.Ward;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.util.TextNormalizer;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Add or edit a saved shipping address. Reuses the GHN province → district → ward cascade
 * from checkout. In edit mode the saved IDs are passed in and re-selected as each list loads.
 */
public class AddressFormActivity extends BaseActivity {

    public static final String EXTRA_ADDRESS_ID = "address_id";
    public static final String EXTRA_RECIPIENT_NAME = "recipient_name";
    public static final String EXTRA_RECIPIENT_PHONE = "recipient_phone";
    public static final String EXTRA_PROVINCE_ID = "province_id";
    public static final String EXTRA_DISTRICT_ID = "district_id";
    public static final String EXTRA_WARD_CODE = "ward_code";
    public static final String EXTRA_STREET = "street";
    public static final String EXTRA_NOTE = "note";
    public static final String EXTRA_POSTAL_CODE = "postal_code";
    public static final String EXTRA_IS_DEFAULT = "is_default";

    private EditText etRecipientName, etRecipientPhone, etStreet, etNote, etPostalCode;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private CheckBox cbDefault;
    private MaterialButton btnSave;

    private final List<Province> provinces = new ArrayList<>();
    private final List<District> districts = new ArrayList<>();
    private final List<Ward> wards = new ArrayList<>();

    private String addressId;
    // Pending selections to apply after each cascade list finishes loading (edit mode).
    private String pendingProvinceId, pendingDistrictId, pendingWardCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_address_form);

        etRecipientName = findViewById(R.id.et_recipient_name);
        etRecipientPhone = findViewById(R.id.et_recipient_phone);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerWard = findViewById(R.id.spinner_ward);
        etStreet = findViewById(R.id.et_street);
        etPostalCode = findViewById(R.id.et_postal_code);
        etNote = findViewById(R.id.et_note);
        cbDefault = findViewById(R.id.cb_default);
        btnSave = findViewById(R.id.btn_save_address);

        addressId = getIntent().getStringExtra(EXTRA_ADDRESS_ID);
        boolean editMode = !TextUtils.isEmpty(addressId);
        ((TextView) findViewById(R.id.txt_form_title)).setText(
                editMode ? R.string.address_edit : R.string.address_add);

        if (editMode) {
            etRecipientName.setText(getIntent().getStringExtra(EXTRA_RECIPIENT_NAME));
            etRecipientPhone.setText(getIntent().getStringExtra(EXTRA_RECIPIENT_PHONE));
            etStreet.setText(getIntent().getStringExtra(EXTRA_STREET));
            etPostalCode.setText(getIntent().getStringExtra(EXTRA_POSTAL_CODE));
            etNote.setText(getIntent().getStringExtra(EXTRA_NOTE));
            cbDefault.setChecked(getIntent().getBooleanExtra(EXTRA_IS_DEFAULT, false));
            pendingProvinceId = getIntent().getStringExtra(EXTRA_PROVINCE_ID);
            pendingDistrictId = getIntent().getStringExtra(EXTRA_DISTRICT_ID);
            pendingWardCode = getIntent().getStringExtra(EXTRA_WARD_CODE);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());

        setupAddressSpinners();
    }

    // ----- GHN cascade -----

    private void setupAddressSpinners() {
        applyAdapter(spinnerProvince, getString(R.string.checkout_select_province), new ArrayList<>());
        applyAdapter(spinnerDistrict, getString(R.string.checkout_select_district), new ArrayList<>());
        applyAdapter(spinnerWard, getString(R.string.checkout_select_ward), new ArrayList<>());

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                districts.clear();
                wards.clear();
                applyAdapter(spinnerDistrict, getString(R.string.checkout_select_district), new ArrayList<>());
                applyAdapter(spinnerWard, getString(R.string.checkout_select_ward), new ArrayList<>());
                Province p = selectedProvince();
                if (p != null) loadDistricts(p.provinceId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wards.clear();
                applyAdapter(spinnerWard, getString(R.string.checkout_select_ward), new ArrayList<>());
                District d = selectedDistrict();
                if (d != null) loadWards(d.districtId);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        loadProvinces();
    }

    private void loadProvinces() {
        ApiClient.get(this).create(PaymentService.class).getProvinces()
                .enqueue(new Callback<ApiResponse<List<Province>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Province>>> call,
                                                     @NonNull Response<ApiResponse<List<Province>>> response) {
                        ApiResponse<List<Province>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(getString(R.string.error_load_address_options));
                            return;
                        }
                        provinces.clear();
                        provinces.addAll(b.data);
                        List<String> labels = new ArrayList<>();
                        for (Province p : provinces) labels.add(p.provinceName);
                        applyAdapter(spinnerProvince, getString(R.string.checkout_select_province), labels);
                        if (pendingProvinceId != null) {
                            int idx = indexOfProvince(pendingProvinceId);
                            pendingProvinceId = null;
                            if (idx >= 0) spinnerProvince.setSelection(idx + 1);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Province>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_address_options));
                    }
                });
    }

    private void loadDistricts(String provinceId) {
        ApiClient.get(this).create(PaymentService.class).getDistricts(provinceId)
                .enqueue(new Callback<ApiResponse<List<District>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<District>>> call,
                                                     @NonNull Response<ApiResponse<List<District>>> response) {
                        ApiResponse<List<District>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(getString(R.string.error_load_address_options));
                            return;
                        }
                        districts.clear();
                        districts.addAll(b.data);
                        List<String> labels = new ArrayList<>();
                        for (District d : districts) labels.add(d.districtName);
                        applyAdapter(spinnerDistrict, getString(R.string.checkout_select_district), labels);
                        if (pendingDistrictId != null) {
                            int idx = indexOfDistrict(pendingDistrictId);
                            pendingDistrictId = null;
                            if (idx >= 0) spinnerDistrict.setSelection(idx + 1);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<District>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_address_options));
                    }
                });
    }

    private void loadWards(String districtId) {
        ApiClient.get(this).create(PaymentService.class).getWards(districtId)
                .enqueue(new Callback<ApiResponse<List<Ward>>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<List<Ward>>> call,
                                                     @NonNull Response<ApiResponse<List<Ward>>> response) {
                        ApiResponse<List<Ward>> b = response.body();
                        if (b == null || !b.success || b.data == null) {
                            showError(getString(R.string.error_load_address_options));
                            return;
                        }
                        wards.clear();
                        wards.addAll(b.data);
                        List<String> labels = new ArrayList<>();
                        for (Ward w : wards) labels.add(w.wardName);
                        applyAdapter(spinnerWard, getString(R.string.checkout_select_ward), labels);
                        if (pendingWardCode != null) {
                            int idx = indexOfWard(pendingWardCode);
                            pendingWardCode = null;
                            if (idx >= 0) spinnerWard.setSelection(idx + 1);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<List<Ward>>> call, @NonNull Throwable t) {
                        showError(getString(R.string.error_load_address_options));
                    }
                });
    }

    private void applyAdapter(Spinner spinner, String prompt, List<String> labels) {
        List<String> entries = new ArrayList<>();
        entries.add(prompt);
        entries.addAll(labels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, entries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private Province selectedProvince() {
        int pos = spinnerProvince.getSelectedItemPosition();
        return pos > 0 && pos - 1 < provinces.size() ? provinces.get(pos - 1) : null;
    }

    private District selectedDistrict() {
        int pos = spinnerDistrict.getSelectedItemPosition();
        return pos > 0 && pos - 1 < districts.size() ? districts.get(pos - 1) : null;
    }

    private Ward selectedWard() {
        int pos = spinnerWard.getSelectedItemPosition();
        return pos > 0 && pos - 1 < wards.size() ? wards.get(pos - 1) : null;
    }

    private int indexOfProvince(String id) {
        for (int i = 0; i < provinces.size(); i++) {
            if (provinces.get(i).provinceId != null && provinces.get(i).provinceId.equals(id)) return i;
        }
        return -1;
    }

    private int indexOfDistrict(String id) {
        for (int i = 0; i < districts.size(); i++) {
            if (districts.get(i).districtId != null && districts.get(i).districtId.equals(id)) return i;
        }
        return -1;
    }

    private int indexOfWard(String code) {
        for (int i = 0; i < wards.size(); i++) {
            if (wards.get(i).wardCode != null && wards.get(i).wardCode.equals(code)) return i;
        }
        return -1;
    }

    // ----- Save -----

    private void save() {
        String recipientName = textOf(etRecipientName);
        if (TextUtils.isEmpty(recipientName)) {
            showError(getString(R.string.error_enter_recipient_name));
            return;
        }
        String recipientPhone = textOf(etRecipientPhone);
        if (TextUtils.isEmpty(recipientPhone)) {
            showError(getString(R.string.error_enter_recipient_phone));
            return;
        }
        Province province = selectedProvince();
        if (province == null) {
            showError(getString(R.string.error_select_province));
            return;
        }
        District district = selectedDistrict();
        if (district == null) {
            showError(getString(R.string.error_select_district));
            return;
        }
        Ward ward = selectedWard();
        if (ward == null) {
            showError(getString(R.string.error_select_ward));
            return;
        }
        String street = textOf(etStreet);
        if (TextUtils.isEmpty(street)) {
            showError(getString(R.string.error_enter_street));
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("recipientName", recipientName);
        body.put("recipientPhone", recipientPhone);
        body.put("provinceId", province.provinceId);
        body.put("provinceName", province.provinceName);
        body.put("districtId", district.districtId);
        body.put("districtName", district.districtName);
        body.put("wardCode", ward.wardCode);
        body.put("wardName", ward.wardName);
        body.put("street", street);
        body.put("postalCode", textOf(etPostalCode));
        body.put("note", textOf(etNote));
        body.put("isDefault", cbDefault.isChecked());

        btnSave.setEnabled(false);
        showLoading();
        AddressService service = ApiClient.get(this).create(AddressService.class);
        Call<ApiResponse<Address>> call = TextUtils.isEmpty(addressId)
                ? service.addAddress(body)
                : service.updateAddress(addressId, body);
        call.enqueue(new Callback<ApiResponse<Address>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Address>> call,
                                             @NonNull Response<ApiResponse<Address>> response) {
                hideLoading();
                btnSave.setEnabled(true);
                ApiResponse<Address> b = response.body();
                if (b == null || !b.success) {
                    showError(b != null && b.message != null ? b.message
                            : getString(R.string.error_save_address));
                    return;
                }
                showSuccess(getString(R.string.address_saved));
                setResult(RESULT_OK);
                finish();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Address>> call, @NonNull Throwable t) {
                hideLoading();
                btnSave.setEnabled(true);
                showError(getString(R.string.error_save_address));
            }
        });
    }

    private String textOf(EditText input) {
        return input.getText() == null ? "" : TextNormalizer.trimAndNormalize(input.getText().toString());
    }
}
