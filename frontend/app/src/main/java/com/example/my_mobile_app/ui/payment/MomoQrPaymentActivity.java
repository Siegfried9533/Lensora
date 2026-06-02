package com.example.my_mobile_app.ui.payment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.my_mobile_app.BuildConfig;
import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.PaymentService;
import com.example.my_mobile_app.model.PaymentResult;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.util.PriceFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Shows the MoMo QR payload returned by the backend and tracks payment status after confirmation. */
public class MomoQrPaymentActivity extends BaseActivity {

    public static final String EXTRA_ORDER_CODE = "order_code";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_RENTAL_ID = "rental_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_QR_CODE_URL = "qr_code_url";
    public static final String EXTRA_PAY_URL = "pay_url";
    public static final String EXTRA_DEEPLINK = "deeplink";

    private String orderCode;
    private String orderId;
    private String rentalId;
    private String qrCodeUrl;
    private String payUrl;
    private String deeplink;
    private double amount;
    private MaterialButton btnConfirm;
    private boolean officialPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireLogin()) return;
        setContentView(R.layout.activity_momo_qr_payment);

        orderCode = getIntent().getStringExtra(EXTRA_ORDER_CODE);
        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        rentalId = getIntent().getStringExtra(EXTRA_RENTAL_ID);
        qrCodeUrl = clean(getIntent().getStringExtra(EXTRA_QR_CODE_URL));
        payUrl = clean(getIntent().getStringExtra(EXTRA_PAY_URL));
        deeplink = clean(getIntent().getStringExtra(EXTRA_DEEPLINK));
        officialPayment = !TextUtils.isEmpty(qrCodeUrl)
                || !TextUtils.isEmpty(payUrl)
                || !TextUtils.isEmpty(deeplink);
        amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        if (TextUtils.isEmpty(orderCode)) {
            showError(getString(R.string.error_missing_transaction_code));
            goHome();
            return;
        }

        ((TextView) findViewById(R.id.txt_momo_amount)).setText(PriceFormatter.format(amount));
        ((TextView) findViewById(R.id.txt_momo_order_code)).setText(orderCode);
        ((TextView) findViewById(R.id.txt_momo_account)).setText(accountLabel());
        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(v -> finish());
        btnConfirm = findViewById(R.id.btn_confirm_payment);
        // Test/demo mode: MoMo sandbox QR cannot be paid by a real wallet and has no IPN,
        // so "I've paid" confirms the order directly via /momo/manual-confirm instead of
        // tracking a status that never completes. Restore the officialPayment branch when
        // production MoMo merchant credentials + a public IPN URL are configured.
        btnConfirm.setOnClickListener(v -> confirmPayment());

        MaterialButton btnOpenMomo = findViewById(R.id.btn_open_momo);
        if (btnOpenMomo != null && officialPayment && !TextUtils.isEmpty(openPaymentTarget())) {
            btnOpenMomo.setVisibility(View.VISIBLE);
            btnOpenMomo.setOnClickListener(v -> openPaymentPage());
        }

        renderQr();
    }

    private String accountLabel() {
        String phone = BuildConfig.MOMO_QR_ACCOUNT_PHONE == null ? "" : BuildConfig.MOMO_QR_ACCOUNT_PHONE.trim();
        String name = BuildConfig.MOMO_QR_ACCOUNT_NAME == null ? "" : BuildConfig.MOMO_QR_ACCOUNT_NAME.trim();
        if (!phone.isEmpty()) {
            return getString(R.string.momo_qr_account_format, name.isEmpty() ? "MoMo" : name, phone);
        }
        return name.isEmpty() ? getString(R.string.momo_qr_account_not_configured) : name;
    }

    private void renderQr() {
        ImageView qr = findViewById(R.id.img_momo_qr);
        String imageUrl = BuildConfig.MOMO_QR_IMAGE_URL == null ? "" : BuildConfig.MOMO_QR_IMAGE_URL.trim();
        if (!officialPayment && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(qr);
            return;
        }

        String payload = qrPayload();
        if (TextUtils.isEmpty(payload)) {
            btnConfirm.setEnabled(false);
            showError(getString(R.string.error_momo_qr_missing));
            return;
        }

        try {
            qr.setImageBitmap(generateQrBitmap(payload, 720));
        } catch (Exception e) {
            showError(getString(R.string.error_create_momo));
        }
    }

    private String qrPayload() {
        if (!TextUtils.isEmpty(qrCodeUrl)) {
            return qrCodeUrl;
        }
        if (!TextUtils.isEmpty(deeplink)) {
            return deeplink;
        }
        if (!TextUtils.isEmpty(payUrl)) {
            return payUrl;
        }
        String configured = BuildConfig.MOMO_QR_PAYLOAD == null ? "" : BuildConfig.MOMO_QR_PAYLOAD.trim();
        if (configured.isEmpty()) {
            return "";
        }
        return configured
                .replace("{orderCode}", orderCode)
                .replace("{amount}", String.valueOf(Math.round(amount)))
                .replace("{amountDecimal}", String.format(Locale.US, "%.0f", amount));
    }

    private Bitmap generateQrBitmap(String value, int size) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private void openPaymentPage() {
        String target = openPaymentTarget();
        if (TextUtils.isEmpty(target)) {
            showError(getString(R.string.error_create_momo));
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(target)));
        } catch (Exception e) {
            showError(getString(R.string.error_create_momo_connection));
        }
    }

    private String openPaymentTarget() {
        return !TextUtils.isEmpty(deeplink) ? deeplink : payUrl;
    }

    private void trackPaymentStatus() {
        Intent i = new Intent(this, OrderStatusActivity.class);
        i.putExtra(OrderStatusActivity.EXTRA_ORDER_CODE, orderCode);
        if (!TextUtils.isEmpty(orderId)) {
            i.putExtra(OrderStatusActivity.EXTRA_ORDER_ID, orderId);
        }
        if (!TextUtils.isEmpty(rentalId)) {
            i.putExtra(OrderStatusActivity.EXTRA_RENTAL_ID, rentalId);
        }
        if (!TextUtils.isEmpty(payUrl)) {
            i.putExtra(OrderStatusActivity.EXTRA_PAY_URL, payUrl);
        }
        startActivity(i);
        finish();
    }

    private void confirmPayment() {
        btnConfirm.setEnabled(false);
        showLoading();
        Map<String, String> body = new HashMap<>();
        body.put("orderCode", orderCode);
        ApiClient.get(this).create(PaymentService.class).confirmManualMoMoPayment(body)
                .enqueue(new Callback<ApiResponse<PaymentResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<PaymentResult>> call,
                                           @NonNull Response<ApiResponse<PaymentResult>> response) {
                        hideLoading();
                        btnConfirm.setEnabled(true);
                        ApiResponse<PaymentResult> b = response.body();
                        if (b == null || !b.success) {
                            showError(errorMessage(response));
                            return;
                        }
                        goSuccess();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<PaymentResult>> call, @NonNull Throwable t) {
                        hideLoading();
                        btnConfirm.setEnabled(true);
                        showError(getString(R.string.error_confirm_momo_qr));
                    }
                });
    }

    private String errorMessage(Response<? extends ApiResponse<?>> response) {
        ApiResponse<?> body = response.body();
        if (body != null && body.message != null && !body.message.isEmpty()) {
            return body.message;
        }
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return getString(R.string.error_confirm_momo_qr);
        }
        try {
            JSONObject obj = new JSONObject(errorBody.string());
            String message = obj.optString("message");
            return message.isEmpty() ? getString(R.string.error_confirm_momo_qr) : message;
        } catch (Exception ignored) {
            return getString(R.string.error_confirm_momo_qr);
        }
    }

    private void goSuccess() {
        Intent i = new Intent(this, PaymentSuccessActivity.class);
        if (!TextUtils.isEmpty(orderId)) {
            i.putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, orderId);
        }
        if (!TextUtils.isEmpty(rentalId)) {
            i.putExtra(PaymentSuccessActivity.EXTRA_RENTAL_ID, rentalId);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
