package com.example.my_mobile_app.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.example.my_mobile_app.R;
import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.api.dto.LoginRequest;
import com.example.my_mobile_app.model.AuthResponse;
import com.example.my_mobile_app.model.User;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.util.TokenManager;
import com.example.my_mobile_app.util.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Email/password login screen — mirrors frontend/app/(auth)/login.tsx. */
public class LoginActivity extends BaseActivity {

    public static final String EXTRA_RETURN_HOME_ON_CANCEL = "return_home_on_cancel";

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton btnBack = findViewById(R.id.btn_back);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);
        TextView linkForgot = findViewById(R.id.link_forgot);
        TextView linkSignup = findViewById(R.id.link_signup);

        btnBack.setOnClickListener(v -> closeWithoutLogin());
        btnLogin.setOnClickListener(v -> attemptLogin());
        linkForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
        linkSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closeWithoutLogin();
            }
        });
    }

    private void attemptLogin() {
        String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
        String password = inputPassword.getText() != null ? inputPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError(getString(R.string.error_required_field));
            return;
        }

        setBusy(true);
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.login(new LoginRequest(email, password))
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call,
                                   @NonNull Response<ApiResponse<AuthResponse>> response) {
                ApiResponse<AuthResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.success && body.data != null) {
                    TokenManager.saveToken(LoginActivity.this, body.data.token);
                    fetchMeThenGoHome();
                } else {
                    setBusy(false);
                    String msg = (body != null) ? body.message : null;
                    showError(translateError(LoginActivity.this, msg));
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
                    UserManager.saveUser(LoginActivity.this, body.data);
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
        showSuccess(getString(R.string.success_login));
        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finishAffinity();
    }

    private void closeWithoutLogin() {
        if (getIntent().getBooleanExtra(EXTRA_RETURN_HOME_ON_CANCEL, false) || isTaskRoot()) {
            startActivity(new Intent(this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        finish();
    }

    private void setBusy(boolean busy) {
        btnLogin.setEnabled(!busy);
        if (busy) showLoading(); else hideLoading();
    }

    /** Translate backend error messages to app-localized copy. */
    static String translateError(Context context, String msg) {
        if (msg == null) return context.getString(R.string.error_login_failed);
        String lower = msg.toLowerCase();
        if (lower.contains("invalid email or password") || lower.contains("unauthorized")
                || lower.contains("bad credentials")) {
            return context.getString(R.string.error_invalid_credentials);
        }
        if (lower.contains("user not found")) return context.getString(R.string.error_user_not_found);
        if (lower.contains("email") && lower.contains("exists")) {
            return context.getString(R.string.error_email_registered);
        }
        return msg;
    }
}
