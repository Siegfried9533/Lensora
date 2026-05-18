package com.example.my_mobile_app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.ui.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Verify email — mirrors frontend/app/verify-email.tsx. */
public class VerifyEmailActivity extends BaseActivity {

    private TextInputEditText inputToken;
    private MaterialButton btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        ImageButton btnBack = findViewById(R.id.btn_back);
        inputToken = findViewById(R.id.input_token);
        btnVerify = findViewById(R.id.btn_verify);

        btnBack.setOnClickListener(v -> finish());
        btnVerify.setOnClickListener(v -> attemptVerify());

        String extraToken = getIntent().getStringExtra("token");
        if (!TextUtils.isEmpty(extraToken)) inputToken.setText(extraToken);
    }

    private void attemptVerify() {
        String token = inputToken.getText() == null ? "" : inputToken.getText().toString().trim();
        if (TextUtils.isEmpty(token)) {
            showError("Vui lòng nhập mã xác minh từ email của bạn");
            return;
        }

        setBusy(true);
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.verifyEmail(token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                setBusy(false);
                ApiResponse<Void> body = response.body();
                if (response.isSuccessful() && body != null && body.success) {
                    showSuccess("Xác minh email thành công!");
                    finish();
                } else {
                    String msg = body != null ? body.message : null;
                    showError(msg != null ? msg : "Mã xác minh không hợp lệ hoặc đã hết hạn");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                setBusy(false);
                showError("Không thể kết nối đến máy chủ");
            }
        });
    }

    private void setBusy(boolean busy) {
        btnVerify.setEnabled(!busy);
        if (busy) showLoading(); else hideLoading();
    }
}
