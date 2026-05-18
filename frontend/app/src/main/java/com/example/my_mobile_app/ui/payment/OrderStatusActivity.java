package com.example.my_mobile_app.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.PaymentService;
import com.example.my_mobile_app.model.PaymentResult;
import com.example.my_mobile_app.ui.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Polls {@code getPaymentStatus(orderCode)} every 3 s for up to 30 tries
 * (~90 s). Routes to PaymentSuccess or PaymentFailed depending on outcome.
 */
public class OrderStatusActivity extends BaseActivity {

    public static final String EXTRA_ORDER_CODE = "order_code";
    public static final String EXTRA_ORDER_ID = "order_id";

    private static final int POLL_INTERVAL_MS = 3000;
    private static final int MAX_ATTEMPTS = 30;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String orderCode;
    private String orderId;
    private int attempts;
    private boolean cancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        orderCode = getIntent().getStringExtra(EXTRA_ORDER_CODE);
        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderCode == null) {
            failWith("Thiếu mã giao dịch");
            return;
        }
        scheduleNext();
    }

    private void scheduleNext() {
        if (cancelled) return;
        if (attempts >= MAX_ATTEMPTS) {
            failWith("Hết thời gian chờ thanh toán");
            return;
        }
        handler.postDelayed(this::pollOnce, POLL_INTERVAL_MS);
    }

    private void pollOnce() {
        if (cancelled || isFinishing()) return;
        attempts++;
        ApiClient.get(this).create(PaymentService.class).getPaymentStatus(orderCode)
                .enqueue(new Callback<ApiResponse<PaymentResult>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<PaymentResult>> call, @NonNull Response<ApiResponse<PaymentResult>> response) {
                        if (cancelled || isFinishing()) return;
                        ApiResponse<PaymentResult> b = response.body();
                        if (b != null && b.success && b.data != null) {
                            if (b.data.success) {
                                succeed();
                                return;
                            }
                            // Server explicitly marked failure (non-pending failure)
                            String msg = b.data.message;
                            if (msg != null && (msg.toLowerCase().contains("fail") || msg.toLowerCase().contains("thất bại"))) {
                                failWith(msg);
                                return;
                            }
                        }
                        scheduleNext();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<PaymentResult>> call, @NonNull Throwable t) {
                        if (cancelled || isFinishing()) return;
                        scheduleNext();
                    }
                });
    }

    private void succeed() {
        Intent i = new Intent(this, PaymentSuccessActivity.class);
        if (orderId != null) i.putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, orderId);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void failWith(String reason) {
        Intent i = new Intent(this, PaymentFailedActivity.class);
        if (reason != null) i.putExtra(PaymentFailedActivity.EXTRA_REASON, reason);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        cancelled = true;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
