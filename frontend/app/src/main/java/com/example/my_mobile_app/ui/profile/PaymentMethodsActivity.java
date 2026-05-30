package com.example.my_mobile_app.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.PaymentMethodService;
import com.example.my_mobile_app.model.SavedPaymentMethod;
import com.example.my_mobile_app.ui.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manage saved payment methods (bank / MoMo) from settings. Methods are stored as
 * masked metadata on the backend — this screen never holds full account numbers.
 */
public class PaymentMethodsActivity extends BaseActivity {

    private LinearLayout methodsContainer;
    private TextView txtEmpty;
    private final List<SavedPaymentMethod> methods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin())
            return;
        setContentView(R.layout.activity_payment_methods);

        methodsContainer = findViewById(R.id.methods_container);
        txtEmpty = findViewById(R.id.txt_empty);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_method).setOnClickListener(v -> showAddDialog());

        loadMethods();
    }

    private void loadMethods() {
        showLoading();
        ApiClient.get(this).create(PaymentMethodService.class).getMethods()
                .enqueue(new Callback<ApiResponse<List<SavedPaymentMethod>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<SavedPaymentMethod>>> call,
                            @NonNull Response<ApiResponse<List<SavedPaymentMethod>>> response) {
                        hideLoading();
                        methods.clear();
                        ApiResponse<List<SavedPaymentMethod>> b = response.body();
                        if (b != null && b.success && b.data != null) {
                            methods.addAll(b.data);
                        }
                        renderMethods();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<SavedPaymentMethod>>> call,
                            @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.payment_methods_load_error));
                        renderMethods();
                    }
                });
    }

    private void renderMethods() {
        methodsContainer.removeAllViews();
        txtEmpty.setVisibility(methods.isEmpty() ? View.VISIBLE : View.GONE);
        for (SavedPaymentMethod m : methods) {
            methodsContainer.addView(buildRow(m));
        }
    }

    private View buildRow(SavedPaymentMethod m) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        int pad = dp(12);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = dp(12);
        card.setLayoutParams(cardLp);

        // Left: text column
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        String typeLabel = "BANK".equalsIgnoreCase(m.type)
                ? getString(R.string.payment_methods_type_bank)
                : getString(R.string.payment_methods_type_momo);
        title.setText(m.label + "  ·  " + typeLabel);
        title.setTextColor(getColor(R.color.white));
        title.setTextSize(15);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        col.addView(title);

        StringBuilder sub = new StringBuilder();
        if (!TextUtils.isEmpty(m.accountHolder)) {
            sub.append(m.accountHolder);
        }
        if (!TextUtils.isEmpty(m.maskedAccount)) {
            if (sub.length() > 0) sub.append("  ·  ");
            sub.append(m.maskedAccount);
        }
        if (sub.length() > 0) {
            TextView subTv = new TextView(this);
            subTv.setText(sub.toString());
            subTv.setTextColor(getColor(R.color.text_muted));
            subTv.setTextSize(13);
            subTv.setPadding(0, dp(2), 0, 0);
            col.addView(subTv);
        }

        if (m.isDefault) {
            TextView badge = new TextView(this);
            badge.setText(getString(R.string.payment_methods_default_badge));
            badge.setTextColor(getColor(R.color.orange));
            badge.setTextSize(12);
            badge.setPadding(0, dp(2), 0, 0);
            col.addView(badge);
        }
        card.addView(col);

        // Right: delete button
        ImageButton delete = new ImageButton(this);
        delete.setImageResource(android.R.drawable.ic_menu_delete);
        delete.setBackgroundResource(R.drawable.bg_circle_dark);
        delete.setColorFilter(getColor(R.color.text_muted));
        LinearLayout.LayoutParams delLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        delete.setLayoutParams(delLp);
        delete.setContentDescription(getString(R.string.action_delete));
        delete.setOnClickListener(v -> confirmDelete(m));
        card.addView(delete);

        // Tap card (not delete) to set as default.
        if (!m.isDefault) {
            col.setOnClickListener(v -> setDefault(m));
        }
        return card;
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_payment_method, null);
        Spinner spType = dialogView.findViewById(R.id.sp_type);
        EditText etLabel = dialogView.findViewById(R.id.et_label);
        EditText etHolder = dialogView.findViewById(R.id.et_holder);
        EditText etAccount = dialogView.findViewById(R.id.et_account);
        CheckBox cbDefault = dialogView.findViewById(R.id.cb_default);

        final String[] typeValues = {"MOMO", "BANK"};
        List<String> typeLabels = new ArrayList<>();
        typeLabels.add(getString(R.string.payment_methods_type_momo));
        typeLabels.add(getString(R.string.payment_methods_type_bank));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.payment_methods_add)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String label = etLabel.getText().toString().trim();
                    if (TextUtils.isEmpty(label)) {
                        showError(getString(R.string.payment_methods_label_required));
                        return;
                    }
                    int pos = spType.getSelectedItemPosition();
                    String type = typeValues[Math.max(0, Math.min(pos, typeValues.length - 1))];
                    addMethod(type, label,
                            etHolder.getText().toString().trim(),
                            etAccount.getText().toString().trim(),
                            cbDefault.isChecked());
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void addMethod(String type, String label, String holder, String account, boolean makeDefault) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("label", label);
        body.put("accountHolder", holder);
        body.put("account", account);
        body.put("isDefault", makeDefault);

        showLoading();
        ApiClient.get(this).create(PaymentMethodService.class).addMethod(body)
                .enqueue(new Callback<ApiResponse<SavedPaymentMethod>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<SavedPaymentMethod>> call,
                            @NonNull Response<ApiResponse<SavedPaymentMethod>> response) {
                        hideLoading();
                        ApiResponse<SavedPaymentMethod> b = response.body();
                        if (b == null || !b.success) {
                            showError(b != null && b.message != null
                                    ? b.message
                                    : getString(R.string.payment_methods_load_error));
                            return;
                        }
                        showSuccess(getString(R.string.payment_methods_added));
                        loadMethods();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<SavedPaymentMethod>> call,
                            @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.payment_methods_load_error));
                    }
                });
    }

    private void confirmDelete(SavedPaymentMethod m) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.payment_methods_delete_confirm)
                .setPositiveButton(R.string.action_delete, (d, w) -> deleteMethod(m))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void deleteMethod(SavedPaymentMethod m) {
        showLoading();
        ApiClient.get(this).create(PaymentMethodService.class).deleteMethod(m.paymentMethodId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                            @NonNull Response<ApiResponse<Void>> response) {
                        hideLoading();
                        showSuccess(getString(R.string.payment_methods_deleted));
                        loadMethods();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.payment_methods_load_error));
                    }
                });
    }

    private void setDefault(SavedPaymentMethod m) {
        showLoading();
        ApiClient.get(this).create(PaymentMethodService.class).setDefault(m.paymentMethodId)
                .enqueue(new Callback<ApiResponse<SavedPaymentMethod>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<SavedPaymentMethod>> call,
                            @NonNull Response<ApiResponse<SavedPaymentMethod>> response) {
                        hideLoading();
                        showSuccess(getString(R.string.payment_methods_default_done));
                        loadMethods();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<SavedPaymentMethod>> call,
                            @NonNull Throwable t) {
                        hideLoading();
                        showError(getString(R.string.payment_methods_load_error));
                    }
                });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
