package com.example.my_mobile_app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.dto.ForgotPasswordRequest;
import com.example.my_mobile_app.ui.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Forgot password — mirrors frontend/app/(auth)/forgot-password.tsx. */
public class ForgotPasswordActivity extends BaseActivity {

    private TextInputEditText inputEmail;
    private MaterialButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ImageButton btnBack = findViewById(R.id.btn_back);
        inputEmail = findViewById(R.id.input_email);
        btnSend = findViewById(R.id.btn_send);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> attemptSend());
    }

    private void attemptSend() {
        String email = inputEmail.getText() == null ? "" : inputEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            showError(getString(R.string.error_required_field));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }

        setBusy(true);
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.forgotPassword(new ForgotPasswordRequest(email.toLowerCase()))
                .enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                setBusy(false);
                ApiResponse<Void> body = response.body();
                if (response.isSuccessful() && body != null && body.success) {
                    showSuccess(getString(R.string.success_email_sent));
                    finish();
                } else {
                    String msg = body != null ? body.message : null;
                    showError(msg != null ? msg : getString(R.string.error_forgot_account_not_found));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                setBusy(false);
                showError(getString(R.string.error_server_connection));
            }
        });
    }

    private void setBusy(boolean busy) {
        btnSend.setEnabled(!busy);
        if (busy) showLoading(); else hideLoading();
    }
}
