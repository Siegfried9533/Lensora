package com.example.my_mobile_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.my_mobile_app.api.ApiClient;
import com.example.my_mobile_app.api.ApiResponse;
import com.example.my_mobile_app.api.AuthService;
import com.example.my_mobile_app.model.User;
import com.example.my_mobile_app.ui.BaseActivity;
import com.example.my_mobile_app.ui.auth.LoginActivity;
import com.example.my_mobile_app.ui.home.HomeActivity;
import com.example.my_mobile_app.util.TokenManager;
import com.example.my_mobile_app.util.UserManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Splash + routing entry point.
 *
 * Flow (Phase 3):
 *   - No token in SharedPreferences -> LoginActivity.
 *   - Token present -> call /auth/me to validate.
 *       success -> save User, then route to HomeActivity (Phase 4).
 *       fail    -> clear token, route to LoginActivity.
 *
 * NOTE: HomeActivity does not yet exist (Phase 4). Until then, even on
 * successful token validation we route to LoginActivity with a TODO.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String token = TokenManager.getToken(this);
        if (token == null) {
            goToHome();
            return;
        }
        validateToken();
    }

    private void validateToken() {
        AuthService api = ApiClient.get(this).create(AuthService.class);
        api.getCurrentUser().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call,
                                   @NonNull Response<ApiResponse<User>> response) {
                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.success && body.data != null) {
                    UserManager.saveUser(MainActivity.this, body.data);
                    goToHome();
                } else {
                    // 401 or other failure -> token invalid
                    TokenManager.clear(MainActivity.this);
                    UserManager.clear(MainActivity.this);
                    goToHome();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                // Network failure: keep the app browsable as a guest.
                TokenManager.clear(MainActivity.this);
                UserManager.clear(MainActivity.this);
                goToHome();
            }
        });
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
