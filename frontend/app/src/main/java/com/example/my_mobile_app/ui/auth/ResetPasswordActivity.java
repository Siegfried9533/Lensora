package com.example.my_mobile_app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.dto.ResetPasswordRequest;
import com.example.my_mobile_app.ui.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reset password — mirrors frontend/app/reset-password.tsx.
 *
 * TODO Phase 4+: add intent-filter for HTTPS deep link
 * (https://camerashop.example/reset-password?token=...) and prefill token.
 */
public class ResetPasswordActivity extends BaseActivity {

    private TextInputEditText inputToken;
    private TextInputEditText inputNewPassword;
    private MaterialButton btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ImageButton btnBack = findViewById(R.id.btn_back);
        inputToken = findViewById(R.id.input_token);
        inputNewPassword = findViewById(R.id.input_new_password);
        btnReset = findViewById(R.id.btn_reset);

        btnBack.setOnClickListener(v -> finish());
        btnReset.setOnClickListener(v -> attemptReset());

        // Allow caller to prefill the token via Intent extra.
        String extraToken = getIntent().getStringExtra("token");
        if (!TextUtils.isEmpty(extraToken)) inputToken.setText(extraToken);
    }

    private void attemptReset() {
        String token = inputToken.getText() == null ? "" : inputToken.getText().toString().trim();
        String newPassword = inputNewPassword.getText() == null
                ? "" : inputNewPassword.getText().toString();

        if (TextUtils.isEmpty(token)) {
            showError("Vui lòng nhập mã đặt lại từ email");
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            showError("Vui lòng nhập mật khẩu mới");
            return;
        }
        if (newPassword.length() < 6) {
            showError(getString(R.string.error_password_too_short));
            return;
        }

        setBusy(true);
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.resetPassword(new ResetPasswordRequest(token, newPassword))
                .enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                setBusy(false);
                ApiResponse<Void> body = response.body();
                if (response.isSuccessful() && body != null && body.success) {
                    showSuccess("Mật khẩu của bạn đã được đặt lại thành công");
                    startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                    finishAffinity();
                } else {
                    String msg = body != null ? body.message : null;
                    showError(msg != null ? msg : "Đặt lại mật khẩu thất bại");
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
        btnReset.setEnabled(!busy);
        if (busy) showLoading(); else hideLoading();
    }
}
