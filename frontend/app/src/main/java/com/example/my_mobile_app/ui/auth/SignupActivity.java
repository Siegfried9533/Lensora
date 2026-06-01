package com.example.my_mobile_app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.dto.RegisterRequest;
import com.example.my_mobile_app.model.AuthResponse;
import com.example.my_mobile_app.model.User;
import android.content.Intent;

import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.util.TextNormalizer;
import com.example.my_mobile_app.util.TokenManager;
import com.example.my_mobile_app.util.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Account creation — mirrors frontend/app/(auth)/signup.tsx. */
public class SignupActivity extends BaseActivity {

    private TextInputEditText inputUsername;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirm;
    private MaterialButton btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ImageButton btnBack = findViewById(R.id.btn_back);
        inputUsername = findViewById(R.id.input_username);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputConfirm = findViewById(R.id.input_confirm);
        btnSignup = findViewById(R.id.btn_signup);
        TextView linkLogin = findViewById(R.id.link_login);

        btnBack.setOnClickListener(v -> finish());
        linkLogin.setOnClickListener(v -> finish());
        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String userName = TextNormalizer.trimAndNormalize(textOf(inputUsername));
        String email = textOf(inputEmail).trim();
        String password = textOf(inputPassword);
        String confirm = textOf(inputConfirm);

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            showError(getString(R.string.error_signup_missing_fields));
            return;
        }
        if (userName.length() < 2) {
            showError(getString(R.string.error_username_too_short));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }
        if (password.length() < 6) {
            showError(getString(R.string.error_password_too_short));
            return;
        }
        if (!password.equals(confirm)) {
            showError(getString(R.string.error_password_mismatch));
            return;
        }

        setBusy(true);
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.register(new RegisterRequest(userName, email, password))
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call,
                                   @NonNull Response<ApiResponse<AuthResponse>> response) {
                ApiResponse<AuthResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.success && body.data != null) {
                    TokenManager.saveToken(SignupActivity.this, body.data.token);
                    fetchMeThenGoHome();
                } else {
                    setBusy(false);
                    showError(LoginActivity.translateError(SignupActivity.this, body != null ? body.message : null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call,
                                  @NonNull Throwable t) {
                setBusy(false);
                showError(getString(R.string.error_server_connection));
            }
        });
    }

    private void fetchMeThenGoHome() {
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.getCurrentUser().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call,
                                   @NonNull Response<ApiResponse<User>> response) {
                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.success && body.data != null) {
                    UserManager.saveUser(SignupActivity.this, body.data);
                }
                setBusy(false);
                goHome();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                setBusy(false);
                goHome();
            }
        });
    }

    private void goHome() {
        showSuccess(getString(R.string.success_account_created));
        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finishAffinity();
    }

    private void setBusy(boolean busy) {
        btnSignup.setEnabled(!busy);
        if (busy) showLoading(); else hideLoading();
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
